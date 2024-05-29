package com.homemedics.app.ui.fragment.auth

import android.content.Intent
import android.content.IntentFilter
import android.os.CountDownTimer
import android.text.InputFilter
import android.view.View
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.auth.OtpRequest
import com.fatron.network_module.models.request.auth.SignupRequest
import com.fatron.network_module.models.request.chat.TwilioTokenRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.auth.OtpResponse
import com.fatron.network_module.models.response.chat.TwilioTokenResponse
import com.fatron.network_module.models.response.meta.City
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSignupBinding
import com.homemedics.app.model.UserModel
import com.homemedics.app.receiver.OTPReceiveListener
import com.homemedics.app.receiver.SMSBroadcastReceiver
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.AuthViewModel
import timber.log.Timber


class SignupFragment : BaseFragment(), View.OnClickListener {
    private lateinit var smsBroadcastReceiver: SMSBroadcastReceiver
    private var phoneLength = 10
    private var citylist: List<City>? = null
    private lateinit var mBinding: FragmentSignupBinding
    private lateinit var timer: CountDownTimer
    private var otpCode: String = ""
    private val userDetail = UserModel()
    private val viewModel: AuthViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            AuthViewModel.ViewModelFactory()
        )[AuthViewModel::class.java]
    }
    val filter = InputFilter { source, _, _, _, _, _ ->
        return@InputFilter when {
            source?.matches(regex) == true -> ""
            else -> null
        }
    }


    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            etConfirmPassword.hint =langData?.userAuthScreen?.confirmPassword.getSafe()
            langData?.globalString?.let {
                etMobileNumber.hint = it.mobileNumber.getSafe()
                etPassword.hint = it.password.getSafe()
                etVerificationCode.hint = it.verificationCode.getSafe()
                etName.hint = it.name.getSafe()
                cdCountryCode.hint = it.countryCode.getSafe()
                cdCountry.hint = it.country.getSafe()
                cdCity.hint = it.city.getSafe()
                dob.hint = it.dateOfBirth.getSafe()
                cdGender.hint =it.gender.getSafe()
            }
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_signup

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSignupBinding
    }

    override fun setListeners() {

        mBinding.cbTermsText.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_privacyPolicyFragment)
        }

        mBinding.include.ivBack.setOnClickListener(this)
        mBinding.dob.clickCallback = {
            mBinding.dob.textColorCheck = true
            openCalender(mBinding.dob.mBinding.editText, canSelectToday = true)
        }
        mBinding.bSignup.setOnClickListener(this)
        mBinding.dob.setOnClickListener(this)
        mBinding.etVerificationCode.mBinding.send.setOnClickListener(this)
        mBinding.apply {
            etPassword.mBinding.editText.doAfterTextChanged {
                etPassword.errorText = null
                etConfirmPassword.errorText = null

                val textEntered = it.toString()
                if (textEntered.isNotEmpty() && textEntered.contains("  ")) {
                    etPassword.mBinding.editText.setText(
                        etPassword.mBinding.editText.text.toString().replace("  ", "")
                    )
                    etPassword.mBinding.editText.text?.length?.let { it1 ->
                        etPassword.mBinding.editText.setSelection(
                            it1
                        )
                    }
                    etPassword.errorText = langData?.fieldValidationStrings?.passwordCorrectValidation.getSafe()
                    etPassword.requestFocus()
                }
            }
            etConfirmPassword.mBinding.editText.doAfterTextChanged {
                etPassword.errorText = null
                etConfirmPassword.errorText = null

                val textEntered = it.toString()
                if (textEntered.isNotEmpty() && textEntered.contains("  ")) {
                    etConfirmPassword.mBinding.editText.setText(
                        etConfirmPassword.mBinding.editText.text.toString().replace("  ", "")
                    )
                    etConfirmPassword.mBinding.editText.text?.length?.let { it1 ->
                        etConfirmPassword.mBinding.editText.setSelection(
                            it1
                        )
                    }
                    etConfirmPassword.errorText = langData?.fieldValidationStrings?.confirmPasswordCorrectValidation.getSafe()
                    etConfirmPassword.requestFocus()
                }
            }
            etName.mBinding.editText.filters = arrayOf(filter)

        }
    }

    override fun init() {
        requireActivity().statusBarColor(R.color.white, false)
        startSMSRetrieverClient()
        mBinding.lifecycleOwner = this
        userDetail.countryCode = arguments?.getString("code").getSafe()
        userDetail.phoneNumber = arguments?.getString("phone").getSafe()

        mBinding.etVerificationCode.mBinding.editText.addTextChangedListener(DataCenter.otpMaskListener)
        mBinding.userModel = userDetail
        dropdownList()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivBack -> findNavController().popBackStack()
            R.id.bSignup -> signupForm()
            R.id.send -> sendCodeFunctionality()
        }
    }



    private fun dropdownList()
    {
        val countryCodeList = getCountryCodeList()
        var indexSelection = 0


        val countryList = getCountryList()

        val genderList = getGenderList()
        mBinding.apply {
            cdCountryCode.data = countryCodeList.getSafe()
            cdCountry.data = countryList as ArrayList<String>
            cdGender.data = genderList
        }
        if (userDetail.countryCode.isEmpty()) {
            mBinding.cdCountryCode.selectionIndex = indexSelection
            userDetail.countryCode =
                countryCodeList?.get(mBinding.cdCountryCode.selectionIndex).getSafe()
            setPhoneLength(mBinding.cdCountryCode.selectionIndex)
        } else {
            val countryCodeIndex =
                metaData?.countries?.indexOfFirst { it.shortName + '(' + it.phoneCode + ')' == userDetail.countryCode }
            indexSelection = countryCodeIndex.getSafe()
            mBinding.cdCountryCode.selectionIndex = countryCodeIndex.getSafe()
            setPhoneLength(countryCodeIndex.getSafe())
        }
        mBinding.cdCountry.selectionIndex = indexSelection
        userDetail.country = countryList?.get(indexSelection).getSafe()
        userDetail.countryId = metaData?.countries?.get(indexSelection)?.id.getSafe()
        setCityList(userDetail.countryId)
        mBinding.cdCountry.onItemSelectedListener = { _, position: Int ->
            userDetail.countryId = metaData?.countries?.get(position)?.id.getSafe()
            setCityList(userDetail.countryId)
        }
        mBinding.cdGender.onItemSelectedListener = { _, position: Int ->
            mBinding.cdGender.errorText = null
            userDetail.genderId = metaData?.genders?.get(position)?.genericItemId.getSafe()
        }
    }


    private fun setPhoneLength(index: Int) {
        phoneLength = metaData?.countries?.get(index = index)?.phoneNoLimit.getSafe()
        mBinding.etMobileNumber.maxLength = phoneLength
    }

    private fun setCityList(countryId: Int) {
        citylist = getCityList(countryId)

        val cityList = citylist?.map { it.name } as ArrayList<String>
        if (cityList.size.getSafe() > 0) {
            mBinding.apply {
                cdCity.data = cityList
                cdCity.onItemSelectedListener = { _, position: Int ->
                    userDetail.cityId = citylist?.get(position)?.id.getSafe().toString()
                    mBinding.cdCity.errorText = null
                }
            }
        } else
            mBinding.cdCity.data = arrayListOf<String>()
    }


    private fun signupForm() {
        val name = userDetail.fullName
        val dob = userDetail.dateOfBirth
        val password = userDetail.password
        val confirmPassword = userDetail.passwordConfirm
        val verificationCode = userDetail.otp
         if (isValidName(name).not()) {
            mBinding.apply {
                etName.errorText =langData?.fieldValidationStrings?.nameValidation
                etName.requestFocus()
                svContainer.fullScroll(ScrollView.FOCUS_UP)
            }
            return
        }
        if (isValid(dob).not()) {
            mBinding.dob.errorText = mBinding.langData?.fieldValidationStrings?.dateOfBirthValidation
            mBinding.dob.requestFocus()
            mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
            return
        }
        if (userDetail.gender.isEmpty()) {
            mBinding.cdGender.errorText =mBinding.langData?.fieldValidationStrings?.genderValidation
            mBinding.cdGender.requestFocus()
            return
        }
        if (isValid(userDetail.cityId).not()) {
            mBinding.cdCity.errorText = mBinding.langData?.fieldValidationStrings?.selectCityValidation
            return
        }
        if (isValid(password).not()) {
            mBinding.etPassword.errorText = mBinding.langData?.fieldValidationStrings?.passwordValidation
            mBinding.etPassword.requestFocus()
            return
        }
        if (password.let { isValidPasswordLength(it).not() }) {
            mBinding.etPassword.errorText = mBinding.langData?.fieldValidationStrings?.passwordValidation
            mBinding.etPassword.requestFocus()
            return
        }
        if (isValid(confirmPassword).not()) {
            mBinding.etConfirmPassword.errorText =
               mBinding.langData?.fieldValidationStrings?.passwordConfirmValidation
            mBinding.etConfirmPassword.requestFocus()
            return
        }
        if (confirmPassword.let { isValidPasswordLength(it).not() }) {
            mBinding.etConfirmPassword.errorText =mBinding. langData?.fieldValidationStrings?.passwordConfirmValidation
            mBinding.etConfirmPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            mBinding.apply {
                etPassword.errorText = langData?.fieldValidationStrings?.passwordConfirmValidation
                etConfirmPassword.errorText = langData?.fieldValidationStrings?.passwordConfirmValidation
                etPassword.requestFocus()
            }
            return
        }

        if (password.startsWith(" ").getSafe()) {
            mBinding.etPassword.errorText = mBinding.langData?.fieldValidationStrings?.passwordCorrectValidation.getSafe()
            mBinding.etPassword.requestFocus()
            mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
            return
        }

        if (confirmPassword.startsWith(" ").getSafe()) {
            mBinding.etConfirmPassword.errorText = mBinding.langData?.fieldValidationStrings?.confirmPasswordCorrectValidation.getSafe()
            mBinding.etConfirmPassword.requestFocus()
            mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
            return
        }

        if (isValid(verificationCode).not()) {
            mBinding.etVerificationCode.errorText = mBinding.langData?.fieldValidationStrings?.verificationCodeValidation
            return
        }

        if (verificationCode.let { isValidCodeLength(it).not() }) {
            mBinding.etVerificationCode.errorText =mBinding.langData?.fieldValidationStrings?.verificationCodeCorrentValidation
            return
        }

        if (!mBinding.cbTerms.isChecked) {
            showToast(mBinding.langData?.fieldValidationStrings?.agreeTerms.getSafe())
            return
        }
        callRegisterApi()
    }

    private fun sendCodeFunctionality() {
        val lang = ApplicationClass.mGlobalData
        val resendCode=mBinding.langData?.globalString?.resendCode
        val verificationCodeSend=mBinding.langData?.userAuthScreen?.verificationCodeSent
        val sec="(${metaData?.otpRespondTime} ${mBinding.langData?.globalString?.sec}"
        mBinding.etVerificationCode.apply {
            this.verifHelperText =  verificationCodeSend
            this.buttonText = sec
            mBinding.send.apply {
                this.text =sec
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.btn_disable))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                isEnabled = false
                timer = viewModel.startTimer(mBinding.send, second = lang?.globalString?.sec.getSafe(), metaData?.otpRespondTime) {
                    this.isEnabled = true
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    this.text =  resendCode
                }
            }
        }
        callOTPApi(getOtpRequest())
        startSMSRetrieverClient()
    }

    private fun callOTPApi(request: OtpRequest) {
        if (isOnline(requireActivity())) {
            viewModel.sendCodeApiCall(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<OtpResponse>
                        otpCode = response.data?.otp ?: ""
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
                                message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {
                        hideLoader()
                    }
                }
            }
        } else {
            DialogUtils(requireActivity() )
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getTwilioChatTokenCall(participantId:Int) {
        val request = TwilioTokenRequest(
            participantId = participantId,
            deviceType = getString(R.string.device)
        )
        viewModel.getTwilioChatToken(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {

                    val response = it.data as ResponseGeneral<TwilioTokenResponse>
                    tinydb.putString(Enums.TinyDBKeys.CHATTOKEN.key,response.data?.token.getSafe())

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
                            message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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


    private fun callRegisterApi() {
        val otp = userDetail.otp.replace("-", "")
        val dob = getDateInFormat(userDetail.dateOfBirth, "dd/MM/yyyy", "yyyy-MM-dd")
        val countryCode = getCountryCode(userDetail.countryCode)
        var healthCareCheck = 0
        if (mBinding.cbProfCheck.isChecked)
            healthCareCheck = 1
        val userRequest = SignupRequest(
            userDetail.fullName,
            countryCode,
            userDetail.phoneNumber,
            userDetail.gender,
            dob,
            userDetail.countryId,
            userDetail.cityId,
            userDetail.password,
            userDetail.passwordConfirm,
            otp,
            genderId = userDetail.genderId,
            healthCareCheck,
            deviceToken = getAndroidID(requireContext()),
            fcmToken = tinydb.getString(Enums.TinyDBKeys.FCM_TOKEN.key),
            deviceMeta = getDeviceMeta()
        )
        if (isOnline(activity)) {
            viewModel.registerApiCall(userRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<UserResponse>
                        val user = response.data
                        user?.let { userResponse ->
                            tinydb.putObject(Enums.TinyDBKeys.USER.key, userResponse)
                            tinydb.putString(
                                Enums.TinyDBKeys.TOKEN_USER.key,
                                userResponse.accessToken.getSafe()
                            )
                            if(userResponse.messageBookingCount!=0)
                            getTwilioChatTokenCall(userResponse.id.getSafe())
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
                                message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {
                        hideLoader()
                    }
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getOtpRequest(): OtpRequest =
        OtpRequest(getCountryCode(userDetail.countryCode), userDetail.phoneNumber)


    override fun onDetach() {
        super.onDetach()
        if (::timer.isInitialized)
            timer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            requireActivity().unregisterReceiver(smsBroadcastReceiver)
        }
        catch (e: Exception){e.printStackTrace()}
    }

    private fun startSMSRetrieverClient() {
        try {
            smsBroadcastReceiver = SMSBroadcastReceiver()
            smsBroadcastReceiver.initOTPListener(object : OTPReceiveListener {
                override fun onOTPReceived(otp: String?) {
                    if (otp != null) {
                        mBinding.etVerificationCode.text = otp
                    }
                }

                override fun onOTPTimeOut() {
                    Timber.e("otp error")
                }
            })

            val client = context?.let { SmsRetriever.getClient(it) }
            val task = client?.startSmsRetriever()
            task?.addOnSuccessListener {
                val intentFilter = IntentFilter()
                intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
                context?.registerReceiver(
                    smsBroadcastReceiver, intentFilter
                )
            }

            task?.addOnFailureListener {
                // Failed to start retriever, inspect Exception for more details
                it.printStackTrace()
            }
        }
        catch (e: Exception){e.printStackTrace()}
    }
}