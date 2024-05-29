package com.homemedics.app.ui.fragment.auth

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.text.InputType
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.auth.EconSubscribeRequest
import com.fatron.network_module.models.request.auth.LoginRequest
import com.fatron.network_module.models.request.chat.TwilioTokenRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.chat.TwilioTokenResponse
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.BuildConfig
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogSetTenantBinding
import com.homemedics.app.databinding.FragmentLoginBinding
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.ui.activity.HomeActivity
import com.homemedics.app.ui.fragment.packages.PackagesViewModel
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.AuthViewModel
import java.util.*


class LoginFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentLoginBinding
    private var phoneLength = 10
    private var startsWith = 0
    private val viewModel: AuthViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            AuthViewModel.ViewModelFactory()
        )[AuthViewModel::class.java]
    }
    private val packagesViewModel: PackagesViewModel by activityViewModels()


    private val loginRequest = LoginRequest()
    private var network: String = ""
    private var isOtpMode = false

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            etMobileNumber.hint = langData?.globalString?.mobileNumber.getSafe()
            etPassword.hint = langData?.globalString?.password.getSafe()
            cdCountryCode.hint = langData?.globalString?.countryCode.getSafe()

            etNewAppMsg.text = Html.fromHtml(
                langData?.userAuthScreen?.newAppMessage.getSafe(),
                Html.FROM_HTML_MODE_LEGACY
            )
        }

    }

    override fun getFragmentLayout(): Int = R.layout.fragment_login

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentLoginBinding
        mBinding.loginModel = loginRequest
    }

    override fun setListeners() {
        handleBackPress()
        mBinding.include.ivBack.setOnClickListener(this)
        mBinding.bStartJourney.setOnClickListener(this)
        mBinding.bLogin.setOnClickListener(this)
        mBinding.tvForgotPassword.setOnClickListener(this)
        mBinding.bDownloadApp.setOnClickListener(this)
        mBinding.etPassword.imeOptionCallback = {
            login()
        }
        mBinding.etMobileNumber.mBinding.editText.addTextChangedListener {
            mBinding.bStartJourney.isEnabled = mBinding.etMobileNumber.text.isNotEmpty()
        }
        mBinding.etPassword.mBinding.editText.addTextChangedListener {
            mBinding.bLogin.isEnabled = mBinding.etPassword.text.isNotEmpty()
        }

        mBinding.include.imageView.setOnLongClickListener {
            if (BuildConfig.DEBUG)
                showSelectTenantDialog()
            return@setOnLongClickListener false
        }

        mBinding.etPassword.mBinding.editText.doAfterTextChanged {
            val textEntered = it.toString()
            if (textEntered.isNotEmpty() && textEntered.contains("  ")) {
                mBinding.etPassword.mBinding.editText.setText(
                    mBinding.etPassword.mBinding.editText.text.toString().replace("  ", "")
                );
                mBinding.etPassword.mBinding.editText.text?.length?.let { it1 ->
                    mBinding.etPassword.mBinding.editText.setSelection(
                        it1
                    )
                }
                mBinding.etPassword.errorText =
                    mBinding.langData?.fieldValidationStrings?.passwordCorrectValidation.getSafe()
                mBinding.etPassword.requestFocus()
            }
        }
    }

    private fun showSelectTenantDialog() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            val dialogAddPromoBinding = DialogSetTenantBinding.inflate(layoutInflater)

            setView(dialogAddPromoBinding.root)
            setTitle("Set Tenant") //because its for testing
            setPositiveButton("Set") { _, _ ->
                val tid = dialogAddPromoBinding.etTenantId.text.trim()
                if (tid.isNotEmpty())
                    TinyDB.instance.putString(Enums.TinyDBKeys.TENANT_ID.key, tid)
            }
            setNegativeButton(R.string.cancel, null)
        }.create()

        builder.show()
    }

    override fun init() {
        requireActivity().statusBarColor(R.color.white, false)
//        mBinding.etMobileNumber.numberInitials = "3"
        // Observe data from ViewModel
        packagesViewModel.isPackagesScreenCancelled.observe(viewLifecycleOwner) { data ->
            //user have cancelled the optional packages flow
            passwordLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        setupCountryCode()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivBack -> {
                if (requireActivity().supportFragmentManager.backStackEntryCount == 0) {
                    startActivity(Intent(requireActivity(), HomeActivity::class.java))
                    requireActivity().finish()
                } else {
                    handleBack()
                }
            }
            R.id.bLogin -> login()
            R.id.bStartJourney -> verify()
            R.id.bDownloadApp -> openNewApp()
            R.id.tvForgotPassword -> {
                mBinding.etPassword.text = ""
                findNavController().safeNavigate(
                    R.id.action_loginFragment_to_forgotPasswordFragment,
                    bundleOf(
                        "code" to loginRequest.countryCode,
                        "phone" to loginRequest.phoneNumber
                    )
                )
            }
        }
    }

    private fun openNewApp() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=com.homemedics.corporate")
        startActivity(intent)
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBack()
                }
            }
        )
    }

    private fun handleBack() {
        if (mBinding.etPassword.visibility == View.VISIBLE) {
            mBinding.apply {
                mBinding.cdCountryCode.isDropdownEnabled = true
                etMobileNumber.inputType = InputType.TYPE_CLASS_PHONE
                bLogin.setVisible(false)
                bStartJourney.setVisible(true)
                tvForgotPassword.setVisible(false)
                etPassword.setVisible(false)
            }
        } else
            requireActivity().finish()
    }


    private fun setupCountryCode() {
        val countryCodeList = getCountryCodeList()
        mBinding.cdCountryCode.data = countryCodeList.getSafe()
        var selectedIndex = 0
        if (loginRequest.countryCode.isEmpty()) {
            val index = metaData?.countries?.indexOfFirst { it.isDefault == 1 }
            if (index != -1)
                selectedIndex = index.getSafe()
        } else {
            val countryCodeIndex =
                metaData?.countries?.indexOfFirst { it.shortName + '(' + it.phoneCode + ')' == loginRequest.countryCode }
            selectedIndex = countryCodeIndex.getSafe()
            if (countryCodeIndex == -1)
                selectedIndex = 0
        }
        if (countryCodeList?.size.getSafe() > 0)
            mBinding.cdCountryCode.selectionIndex = selectedIndex
        mBinding.etMobileNumber.numberInitials =
            metaData?.countries?.get(selectedIndex)?.phoneNoInitial.getSafe().toString()
        loginRequest.countryCode =
            countryCodeList?.get(mBinding.cdCountryCode.selectionIndex).getSafe()
        setPhoneLength(mBinding.cdCountryCode.selectionIndex)
        mBinding.cdCountryCode.onItemSelectedListener = { _, position: Int ->
            setPhoneLength(position)
            mBinding.etMobileNumber.numberInitials =
                metaData?.countries?.get(position)?.phoneNoInitial.getSafe().toString()
        }
    }


    private fun setPhoneLength(index: Int) {
        mBinding.etMobileNumber.errorText = null
        phoneLength = metaData?.countries?.get(index = index)?.phoneNoLimit.getSafe()
        startsWith = metaData?.countries?.get(index = index)?.phoneNoInitial.getSafe()
        mBinding.etMobileNumber.numberLimit = phoneLength
    }

    private fun verify() {
        if (loginRequest.phoneNumber.startsWith("0"))
            loginRequest.phoneNumber = loginRequest.phoneNumber.substring(1)
        val mobileNumber = loginRequest.phoneNumber
        if (isValid(mobileNumber).not()) {
            mBinding.apply {
                etMobileNumber.errorText = langData?.fieldValidationStrings?.mobileNumEmpty
                etMobileNumber.requestFocus()
            }
            return
        }
        if (isValidPhoneLength(mobileNumber, phoneLength).not()) {
            mBinding.apply {
                etMobileNumber.errorText = langData?.fieldValidationStrings?.mobileNumberValidation
                etMobileNumber.requestFocus()
            }
            return
        }
        val numberInitials = ""
        val numberLimit = 0
        mBinding.etMobileNumber.errorText =
            if (mobileNumber.startsWith("0") && mobileNumber.length == 1 && mobileNumber.length <= numberLimit) //accept 0 if only one digit and have valid limit
                null
            else if (mobileNumber.startsWith(numberInitials) && mobileNumber.length <= numberLimit) //accept if start with valid initial and have valid limit
                null
            else if (mobileNumber.startsWith("0$numberInitials") && mobileNumber.length <= numberLimit + 1 /* to accept with zero */)
                null
            else if (mobileNumber.startsWith(startsWith.toString()) && mobileNumber.length == phoneLength)
                null
            else {
                mBinding.langData?.fieldValidationStrings?.mobileNumberValidation
                mBinding.etMobileNumber.requestFocus()
                return
            }

        mBinding.apply {
            etPassword.apply {
                if (this.visibility == View.GONE) {
                    loginRequest.type = "login"
                    val req = Gson().fromJson(Gson().toJson(loginRequest), LoginRequest::class.java)
                    req.countryCode = getCountryCode(loginRequest.countryCode)
                    verifyPhoneNumApiCall(req)
                } else {
                    if (isOtpMode)
                        otpLoginApiCall()
                    else login()
                }
            }
        }

    }

    private fun otpLoginApiCall() {
        //  val password = mBinding.etPassword.text
//        if (isValidName(password).not()) {
//            mBinding.etPassword.errorText =
//                mBinding.langData?.fieldValidationStrings?.passwordCorrectValidation
//            mBinding.etPassword.requestFocus()
//            return
//        }

//        if (password.startsWith(" ").getSafe()) {
//            mBinding.etPassword.errorText =
//                mBinding.langData?.fieldValidationStrings?.passwordCorrectValidation
//            mBinding.etPassword.requestFocus()
//            return
//        }
//        loginRequest.deviceToken = getAndroidID(requireContext())
//        loginRequest.deviceMeta = getDeviceMeta()
//        loginRequest.fcmToken = tinydb.getString(Enums.TinyDBKeys.FCM_TOKEN.key)
//        loginRequest.timeZone = TimeZone.getDefault().id
//        val req = Gson().fromJson(Gson().toJson(loginRequest), LoginRequest::class.java)
//        req.countryCode = getCountryCode(loginRequest.countryCode)
//        loginApiCall(req)


        val request = EconSubscribeRequest(
            network = network,
            phone = mBinding.etMobileNumber.text.getSafe(),
            otp = mBinding.etPassword.text.getSafe(),
            packageId = null
        )
        if (isOnline(requireActivity())) {
            viewModel.verifyEconOtpApiCall(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {

                        val response = it.data as ResponseGeneral<UserResponse>
                        val user = response.data

                        user?.let {
                            if (it.messageBookingCount != 0)
                                getTwilioChatTokenCall(it.id.getSafe())
                            tinydb.putObject(Enums.TinyDBKeys.USER.key, it)
                            tinydb.putString(
                                Enums.TinyDBKeys.TOKEN_USER.key,
                                it.accessToken.getSafe()
                            )

                            (requireActivity() as AuthActivity).navigateToHome()


                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = "OTP Verification Failed",
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun login() {
        val password = mBinding.etPassword.text
        if (isValidName(password).not()) {
            mBinding.etPassword.errorText =
                mBinding.langData?.fieldValidationStrings?.passwordCorrectValidation
            mBinding.etPassword.requestFocus()
            return
        }

        if (password.startsWith(" ").getSafe()) {
            mBinding.etPassword.errorText =
                mBinding.langData?.fieldValidationStrings?.passwordCorrectValidation
            mBinding.etPassword.requestFocus()
            return
        }
        loginRequest.deviceToken = getAndroidID(requireContext())
        loginRequest.deviceMeta = getDeviceMeta()
        loginRequest.fcmToken = tinydb.getString(Enums.TinyDBKeys.FCM_TOKEN.key)
        loginRequest.timeZone = TimeZone.getDefault().id
        val req = Gson().fromJson(Gson().toJson(loginRequest), LoginRequest::class.java)
        req.countryCode = getCountryCode(loginRequest.countryCode)
        loginApiCall(req)
    }

    private fun getTwilioChatTokenCall(participantId: Int) {
        val request = TwilioTokenRequest(
            participantId = participantId,
            deviceType = getString(R.string.device)
        )
        viewModel.getTwilioChatToken(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {

                    val response = it.data as ResponseGeneral<TwilioTokenResponse>
                    tinydb.putString(Enums.TinyDBKeys.CHATTOKEN.key, response.data?.token.getSafe())

                    if (DataCenter.getUser() != null && DataCenter.getUser()?.messageBookingCount != 0 &&
                        response.data?.token?.isNotEmpty() == true && ApplicationClass.twilioChatManager?.conversationClients == null
                    ) {
                        ApplicationClass.twilioChatManager?.initializeWithAccessToken(
                            requireContext(), response.data?.token.getSafe(),
                            TinyDB.instance.getString(
                                Enums.TinyDBKeys.CHATTOKEN.key
                            )
                        )
                    }

                }
                is ResponseResult.Failure -> {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = it.error.message.getSafe(),
                            buttonCallback = {},
                        )
                }
                is ResponseResult.ApiError -> {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                            buttonCallback = {},
                        )
                }
                is ResponseResult.Pending -> {
                    hideLoader()
                }
                is ResponseResult.Complete -> {
                    hideLoader()
                }
                else -> {
                    hideLoader()
                }
            }
        }
    }

    private fun loginApiCall(request: LoginRequest) {
        if (isOnline(requireActivity())) {
            viewModel.loginApiCall(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {

                        val response = it.data as ResponseGeneral<UserResponse>
                        val user = response.data

                        user?.let {
                            if (it.messageBookingCount != 0)
                                getTwilioChatTokenCall(it.id.getSafe())
                            tinydb.putObject(Enums.TinyDBKeys.USER.key, it)
                            tinydb.putString(
                                Enums.TinyDBKeys.TOKEN_USER.key,
                                it.accessToken.getSafe()
                            )

                            (requireActivity() as AuthActivity).navigateToHome()


                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun verifyPhoneNumApiCall(request: LoginRequest) {
        if (isOnline(requireActivity())) {
            viewModel.verifyPhoneNumApiCall(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<UserResponse>
                        // if(response.status in 200..299) {
                        checkUserType(
                            response.data?.isSubscribed,
                            response.data?.isCorporateUser,
                            response.data?.redirectToLogin,
                            response.data?.network
                        )
//                        mBinding.apply {
//                            mBinding.etMobileNumber.inputType = InputType.TYPE_NULL
//                            mBinding.cdCountryCode.isDropdownEnabled = false
//                            mBinding.etPassword.requestFocus()
//                            bLogin.setVisible(true)
//                            tvForgotPassword.setVisible(true)
//                            etPassword.setVisible(true)
//                            bStartJourney.setVisible(true)
//                        }
                        //}
                        // else
                        //  LoginFragmentDirections.actionLoginFragmentToSignupFragment()
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        findNavController().safeNavigate(
                            R.id.action_loginFragment_to_signupFragment,
                            bundleOf(
                                "code" to loginRequest.countryCode,
                                "phone" to loginRequest.phoneNumber
                            )
                        )

                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {}
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }

    }

    private fun checkUserType(
        subscribed: Int?,
        corporateUser: Int?,
        redirectToLogin: Int?,
        network: String?
    ) {
        if (network != null) {
            this.network = network
        }
//        findNavController().safeNavigate(R.id.action_loginFragment_to_packagesFragment, bundleOf(
//            "network" to network,
//            "phone" to loginRequest.phoneNumber
//        ))
//        return
        when {
            redirectToLogin == 0 && subscribed == 0 && corporateUser == 0 -> {
                // optional subscription flow and signup
                findNavController().safeNavigate(
                    R.id.action_loginFragment_to_signupFragment,
                    bundleOf(
                        "code" to loginRequest.countryCode,
                        "phone" to loginRequest.phoneNumber
                    )
                )

            }
            redirectToLogin == 0 && subscribed == 0 && corporateUser == 1 -> {
                // signup
                findNavController().safeNavigate(
                    R.id.action_loginFragment_to_signupFragment,
                    bundleOf(
                        "code" to loginRequest.countryCode,
                        "phone" to loginRequest.phoneNumber
                    )
                )
            }
            redirectToLogin == 0 && subscribed == 1 && corporateUser == 0 -> {
                // signup
                findNavController().safeNavigate(
                    R.id.action_loginFragment_to_signupFragment,
                    bundleOf(
                        "code" to loginRequest.countryCode,
                        "phone" to loginRequest.phoneNumber
                    )
                )
            }
            redirectToLogin == 0 && subscribed == 1 && corporateUser == 1 -> {
                // sign up
                findNavController().safeNavigate(
                    R.id.action_loginFragment_to_signupFragment,
                    bundleOf(
                        "code" to loginRequest.countryCode,
                        "phone" to loginRequest.phoneNumber
                    )
                )
            }
            redirectToLogin == 1 && subscribed == 0 && corporateUser == 0 -> {
                // optional telco subcription dialog and password login
                if (network != "Other") {
                    findNavController().safeNavigate(
                        R.id.action_loginFragment_to_packagesFragment, bundleOf(
                            "network" to network,
                            "phone" to loginRequest.phoneNumber
                        )
                    )
                } else {
                    isOtpMode = false
                    passwordLogin()
                }

            }
            redirectToLogin == 1 && subscribed == 0 && corporateUser == 1 -> {
                // password login
                isOtpMode = false
                passwordLogin()
            }
            redirectToLogin == 1 && subscribed == 1 && corporateUser == 0 -> {
                // otp login
                isOtpMode = true
                otpLogin()
            }
            redirectToLogin == 1 && subscribed == 1 && corporateUser == 1 -> {
                // otp login
                isOtpMode = true
                otpLogin()
            }
        }
    }

    private fun passwordLogin() {
        mBinding.apply {
            mBinding.etMobileNumber.inputType = InputType.TYPE_NULL
            mBinding.cdCountryCode.isDropdownEnabled = false
            mBinding.etPassword.requestFocus()
            bLogin.setVisible(true)
            tvForgotPassword.setVisible(true)
            etPassword.setVisible(true)
            bStartJourney.setVisible(true)
        }
    }

    private fun otpLogin() {
        mBinding.apply {

            mBinding.etPassword.inputType = InputType.TYPE_CLASS_NUMBER
            mBinding.etPassword.maxLength = 4
            mBinding.etPassword.hint = "OTP"

            mBinding.cdCountryCode.isDropdownEnabled = false
            mBinding.etPassword.requestFocus()
            bLogin.setVisible(true)
            tvForgotPassword.setVisible(false)
            etPassword.setVisible(true)
            bStartJourney.setVisible(true)
        }
    }
}