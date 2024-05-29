package com.homemedics.app.ui.fragment.auth

import android.content.IntentFilter
import android.os.CountDownTimer
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.auth.ForgetPwdRequest
import com.fatron.network_module.models.request.auth.OtpRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.auth.ForgetPwdResponse
import com.fatron.network_module.models.response.auth.OtpResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentForgotPasswordBinding
import com.homemedics.app.receiver.OTPReceiveListener
import com.homemedics.app.receiver.SMSBroadcastReceiver
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.AuthViewModel
import com.santalu.maskara.Mask
import com.santalu.maskara.MaskChangedListener
import com.santalu.maskara.MaskStyle
import timber.log.Timber


class ForgotPasswordFragment : BaseFragment(), View.OnClickListener {
    private lateinit var smsBroadcastReceiver: SMSBroadcastReceiver
    private val forgetPwdModel = ForgetPwdRequest()
    private lateinit var timer: CountDownTimer

    private var phoneLength=10
    private lateinit var mBinding: FragmentForgotPasswordBinding
    private var otpCode: String = ""
    private val viewModel: AuthViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            AuthViewModel.ViewModelFactory()
        )[AuthViewModel::class.java]
    }

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            etMobileNumber.hint = langData?.globalString?.mobileNumber.getSafe()
            etPassword.hint = langData?.userAuthScreen?.newPassword.getSafe()
            etConfirmPassword.hint = langData?.userAuthScreen?.confirmPassword.getSafe()
            etVerificationCode.hint = langData?.globalString?.verificationCode.getSafe()
            cdCountryCode.hint = langData?.globalString?.countryCode.getSafe()
        }

    }

    override fun getFragmentLayout(): Int = R.layout.fragment_forgot_password

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentForgotPasswordBinding
    }

    override fun setListeners() {
        mBinding.apply {
            include.ivBack.setOnClickListener(this@ForgotPasswordFragment)
            bVerifyCode.setOnClickListener(this@ForgotPasswordFragment)
            bChangePassword.setOnClickListener(this@ForgotPasswordFragment)
            etVerificationCode.imeOptionCallback = {
                disableContactsField()
                showChangePasswordViews()
            }
            etConfirmPassword.imeOptionCallback = {
                changePassword()
            }
            etVerificationCode.mBinding.send.setOnClickListener(this@ForgotPasswordFragment)
            etMobileNumber.mBinding.editText.onChangedText(etMobileNumber.mBinding.editText, etVerificationCode.mBinding.editText, bVerifyCode)
            etVerificationCode.mBinding.editText.onChangedText(etMobileNumber.mBinding.editText, etVerificationCode.mBinding.editText, bVerifyCode)
            etPassword.mBinding.editText.onChangedText(etPassword.mBinding.editText, etConfirmPassword.mBinding.editText, bChangePassword)
            etConfirmPassword.mBinding.editText.onChangedText(etPassword.mBinding.editText, etConfirmPassword.mBinding.editText, bChangePassword)

            etPassword.mBinding.editText.doAfterTextChanged {
                val textEntered = it.toString()

                    if (textEntered.isNotEmpty() && textEntered.contains("  ")) {

                        etPassword.mBinding.editText.setText(
                            etPassword.mBinding.editText.text.toString().replace("  ", "")
                        );
                        etPassword.mBinding.editText.text?.length?.let { it1 ->
                            etPassword.mBinding.editText.setSelection(
                                it1
                            )
                        }
                        etPassword.errorText =
                            langData?.fieldValidationStrings?.passwordCorrectValidation.getSafe()
                        etPassword.requestFocus()
                    }

            }

           etConfirmPassword.mBinding.editText.doAfterTextChanged {
                val textEntered = it.toString()

                    if (textEntered.isNotEmpty() && textEntered.contains("  ")) {

                        etConfirmPassword.mBinding.editText.setText(
                            etConfirmPassword.mBinding.editText.text.toString().replace("  ", ""),

                            );
                        etConfirmPassword.mBinding.editText.text?.length?.let { it1 ->
                            etConfirmPassword.mBinding.editText.setSelection(
                                it1
                            )
                        }
                        etConfirmPassword.errorText =
                            langData?.fieldValidationStrings?.confirmPasswordCorrectValidation.getSafe()
                        etConfirmPassword.requestFocus()

                }
            }
        }
    }

    override fun init() {
        requireActivity().statusBarColor(R.color.white, false)

        startSMSRetrieverClient()

        mBinding.lifecycleOwner = this
        mBinding.forgetPwdModel=forgetPwdModel
        forgetPwdModel.countryCode=  arguments?.getString("code").getSafe()
        forgetPwdModel.phoneNumber=  arguments?.getString("phone").getSafe()
        disableContactsField()
        setupCountryCode()
        val otpMask = Mask(value = getString(R.string.otp_format), character = '_', MaskStyle.NORMAL)
        val otpMaskListener = MaskChangedListener(otpMask)
        mBinding.etVerificationCode.mBinding.editText.addTextChangedListener(otpMaskListener)

    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.ivBack -> findNavController().navigateUp()
            R.id.send -> sendCodeFunctionality()
            R.id.bVerifyCode -> {
                showChangePasswordViews()
            }
            R.id.bChangePassword -> changePassword()
        }
    }

    private fun disableContactsField() {
        mBinding.apply {
            etMobileNumber.inputType = InputType.TYPE_NULL
            cdCountryCode.isDropdownEnabled = false
        }
    }

      fun regexpattren(password:String):Boolean{

            val regexPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?!.*\\s).{8,}$".toRegex()
            return regexPattern.matches(password)

    }
    private fun setupCountryCode() {
        val countryCodeList = getCountryCodeList()
        mBinding.cdCountryCode.data = countryCodeList as ArrayList<String>
        if (forgetPwdModel.countryCode.isEmpty()) {
            mBinding.cdCountryCode.selectionIndex = 0
            forgetPwdModel.countryCode=countryCodeList[mBinding.cdCountryCode.selectionIndex]
            setPhoneLength(mBinding.cdCountryCode.selectionIndex)


        }else{
            val countryCodeIndex= metaData?.countries?.indexOfFirst { it.shortName+'('+it.phoneCode +')'==forgetPwdModel.countryCode }
            mBinding.cdCountryCode.selectionIndex= countryCodeIndex.getSafe()
            setPhoneLength(countryCodeIndex.getSafe())
        }

     }


    private fun setPhoneLength(index:Int){
        phoneLength=metaData?.countries?.get(index = index)?.phoneNoLimit.getSafe()
        mBinding.etMobileNumber.maxLength=phoneLength
    }

    private fun changePassword() {
        val password = forgetPwdModel.password
        val confirmPassword = forgetPwdModel.passwordConfirmation

        if (isValid(password).not()) {
            mBinding.etPassword.errorText =  mBinding.langData?.fieldValidationStrings?.passwordValidation
            mBinding.etPassword.requestFocus()
            return
        }
        if (isValidPasswordLength(password.getSafe()).not()) {
            mBinding.etPassword.errorText =  mBinding.langData?.fieldValidationStrings?.passwordValidation
            mBinding.etPassword.requestFocus()
            return
        }
        if (isValid(confirmPassword).not()) {
            mBinding.etConfirmPassword.errorText =  mBinding.langData?.fieldValidationStrings?.passwordConfirmValidation
            mBinding.etConfirmPassword.requestFocus()
            return
        }
     if (isValidPasswordLength(confirmPassword.getSafe()).not()) {
            mBinding.etConfirmPassword.errorText =  mBinding.langData?.fieldValidationStrings?.passwordConfirmValidation
            mBinding.etConfirmPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            mBinding.etPassword.errorText =   mBinding.langData?.fieldValidationStrings?.passwordConfirmValidation
            mBinding.etConfirmPassword.errorText =   mBinding.langData?.fieldValidationStrings?.passwordConfirmValidation
            mBinding.etConfirmPassword.requestFocus()
            return
        }

        if (isValidName(password).not()) {
            mBinding.etPassword.errorText = mBinding.langData?.fieldValidationStrings?.passwordCorrectValidation
            mBinding.etPassword.requestFocus()
            return
        }

        if (password?.startsWith(" ").getSafe()) {
            mBinding.etPassword.errorText = mBinding.langData?.fieldValidationStrings?.passwordCorrectValidation
            mBinding.etPassword.requestFocus()
            return
        }

        if (isValidName(confirmPassword).not()) {
            mBinding.etConfirmPassword.errorText = mBinding.langData?.fieldValidationStrings?.confirmPasswordCorrectValidation
            mBinding.etConfirmPassword.requestFocus()
            return
        }

        if (confirmPassword?.startsWith(" ").getSafe()) {
            mBinding.etConfirmPassword.errorText = mBinding.langData?.fieldValidationStrings?.confirmPasswordCorrectValidation
            mBinding.etConfirmPassword.requestFocus()
            return
        }


       // for changing of password with regex
       /* if (password?.let { regexpattren(it) } == false){
            Toast.makeText(context,"This is not right password",Toast.LENGTH_LONG).show()
            mBinding.etConfirmPassword.errorText =  mBinding.langData?.fieldValidationStrings?.passwordConfirmValidation
            mBinding.etConfirmPassword.requestFocus()
            return
        }
        if (confirmPassword?.let { regexpattren(it) } == false){
            Toast.makeText(context,"This is not right password of confirm",Toast.LENGTH_LONG).show()
            mBinding.etConfirmPassword.errorText =  mBinding.langData?.fieldValidationStrings?.passwordConfirmValidation
            mBinding.etConfirmPassword.requestFocus()
            return
        }*/

        changePwdApiCall()

    }

    private fun showChangePasswordViews() {
        mBinding.apply {
            if (etVerificationCode.isVisible) {

                val phoneNumber = forgetPwdModel?.phoneNumber
                val verificationCode = forgetPwdModel?.otp

                if(isValidPhone(phoneNumber).not()) {
                    mBinding.etMobileNumber.errorText =   mBinding.langData?.fieldValidationStrings?.mobileNumEmpty
                    mBinding.etMobileNumber.requestFocus()
                    return
                }
                if(isValidPhoneLength(phoneNumber.getSafe(),phoneLength).not()){
                    mBinding.etMobileNumber.errorText =   mBinding.langData?.fieldValidationStrings?.mobileNumberValidation
                    mBinding.etMobileNumber.requestFocus()
                    return
                }
                if (isValid(verificationCode).not()) {
                    mBinding.etVerificationCode.errorText =  mBinding.langData?.fieldValidationStrings?.verificationCodeValidation
                    mBinding.etVerificationCode.requestFocus()
                    return
                }
                if (isValidCodeLength(verificationCode.getSafe()).not()) {
                    mBinding.etVerificationCode.errorText =  mBinding.langData?.fieldValidationStrings?.verificationCodeCorrentValidation
                    mBinding.etVerificationCode.requestFocus()
                    return
                }

                verifyCodeApiCall()
            }
        }

    }

    private fun verifyCodeApiCall() {
      val forgetPwdRequest=ForgetPwdRequest()
        forgetPwdModel.apply {

            val otp= forgetPwdModel.otp?.replace("-", "")
            forgetPwdRequest.countryCode=  getCountryCode(this.countryCode)
            forgetPwdRequest.otp= otp
            forgetPwdRequest.phoneNumber= this.phoneNumber
        }

        if (isOnline(requireActivity())) {
            viewModel.verifyCodeApiCall(forgetPwdRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
//                    val response = it.data as ResponseGeneral<ForgetPwdRequest>
                        mBinding.apply {
                            etVerificationCode.setVisible(false)
                            etPassword.setVisible(true)
                            etConfirmPassword.setVisible(true)
                            bVerifyCode.setVisible(false)
                            bChangePassword.apply {
                                setVisible(true)
                            }
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
                    else -> { hideLoader() }
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

    private fun changePwdApiCall() {
        val req = Gson().fromJson(Gson().toJson(forgetPwdModel), ForgetPwdRequest::class.java)
        req.countryCode = getCountryCode(forgetPwdModel.countryCode)


        if (isOnline(requireActivity())) {
            viewModel.changePwdApiCall(req).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<ForgetPwdResponse>

                        mBinding.etMobileNumber.errorText = null
                        mBinding.etPassword.errorText = null
                        mBinding.etConfirmPassword.errorText = null
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =mBinding.langData?.dialogsStrings?.resetPasswordSuccess.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
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
                    message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }

    }

    private fun sendCodeFunctionality() {
        val lang = ApplicationClass.mGlobalData
        val resendCode=mBinding.langData?.globalString?.resendCode
        val verificationCodeSend=mBinding.langData?.userAuthScreen?.verificationCodeSent
        val sec="(${metaData?.otpRespondTime} ${mBinding.langData?.globalString?.sec}"
        mBinding.etVerificationCode.apply {
            this.verifHelperText= verificationCodeSend
            this.buttonText = sec
            mBinding.send.apply {
                this.text = sec
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.btn_disable))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                isEnabled = false
              timer=  viewModel.startTimer(mBinding.send, second = lang?.globalString?.sec.getSafe(),metaData?.otpRespondTime) {
                    this.isEnabled = true
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    this.text = resendCode
                }
            }
        }
        sendCodeApiCall()
        startSMSRetrieverClient()
    }

    private fun sendCodeApiCall() {
        if (isOnline(requireActivity())) {
            viewModel.sendCodeApiCall(getOtpRequest()).observe(this) {
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
                    else -> { hideLoader() }
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
        OtpRequest(getCountryCode(forgetPwdModel.countryCode), phoneNumber = forgetPwdModel.phoneNumber.getSafe())


    override fun onDetach() {
        super.onDetach()
        if (::timer.isInitialized)
            timer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(smsBroadcastReceiver)
    }

    private fun startSMSRetrieverClient() {
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
}