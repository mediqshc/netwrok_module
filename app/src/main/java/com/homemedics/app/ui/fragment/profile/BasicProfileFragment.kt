package com.homemedics.app.ui.fragment.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.auth.DeleteAccountRequest
import com.fatron.network_module.models.request.email.EmailSendRequest
import com.fatron.network_module.models.request.email.EmailVerifyRequest
import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.request.user.UserPhoneNumber
import com.fatron.network_module.models.request.user.UserRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.email.VerifyEmailResponse
import com.fatron.network_module.models.response.meta.City
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.textfield.TextInputEditText
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogVerifyCodeBinding
import com.homemedics.app.databinding.FragmentBasicProfileBinding
import com.homemedics.app.model.AddressModel
import com.homemedics.app.model.ContactItem
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.utils.*
import com.homemedics.app.utils.DataCenter.cnicMaskListener
import com.homemedics.app.viewmodel.AuthViewModel
import com.homemedics.app.viewmodel.ProfileViewModel


class BasicProfileFragment : BaseFragment(), View.OnClickListener {

    private var timer: CountDownTimer? = null
    private lateinit var emailVerifyDialogViewBinding: DialogVerifyCodeBinding
    private lateinit var mBinding: FragmentBasicProfileBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var phoneLength = 10

    private var user: UserResponse? = null

    private var citylist: List<City>? = null
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

    private var otpCode = ""
    private val isPhoneVerified = MutableLiveData(false)
    private val isEmailVerified = MutableLiveData(false)

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            cdCountryCode.hint = langData?.globalString?.countryCode.getSafe()
            cdGender.hint = langData?.globalString?.gender.getSafe()
            cdCountry.hint = langData?.globalString?.country.getSafe()
            cdCity.hint = langData?.globalString?.city.getSafe()
            etCnic.hint = langData?.globalString?.cnic.getSafe()
            etEmail.hint = langData?.personalprofileBasicScreen?.email.getSafe()
            etName.hint = langData?.globalString?.name.getSafe()
            etMobileNumber.hint = langData?.globalString?.mobileNumber.getSafe()
            dob.hint = langData?.globalString?.dateOfBirth.getSafe()
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_basic_profile

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentBasicProfileBinding

        user = DataCenter.getUser()
        if (user?.email?.isNotEmpty() == true) {
            isEmailVerified.postValue(true)
//            setDrawableEnd(mBinding.tvVerifyEmail)
            mBinding.ivDone.setVisible(true)
            mBinding.tvVerifyEmail.visibility = View.INVISIBLE
            mBinding.bSave.isEnabled = true
        }

        mBinding.apply {

            etCnic.mBinding.editText.addTextChangedListener(cnicMaskListener)
            dob.mBinding.editText.setTextColor(resources.getColor(R.color.black, null))
        }
    }

    override fun setListeners() {
        mBinding.apply {
            caContacts.onAddItemClick = {
                findNavController().safeNavigate(PersonalProfileFragmentDirections.actionPersonalProfileFragmentToAdditionalContactFragment())
            }

            tvVerifyEmail.setOnClickListener {
                if (user?.email?.equals(mBinding.etEmail.text, true)
                        ?.not() == true || user?.email?.isEmpty().getSafe()
                ) {
                    closeKeypad()
                    validateEmail()
                }
            }
            caAddresses.onAddItemClick = {
                profileViewModel._address.postValue(null)
                findNavController().safeNavigate(PersonalProfileFragmentDirections.actionPersonalProfileFragmentToAddAddressFragment())

            }
            dob.clickCallback = {
                openCalender(mBinding.dob.mBinding.editText, canSelectToday = true, currentDob = profileViewModel.userProfile.value?.dateOfBirth.getSafe() )
            }
            bSave.setOnClickListener(this@BasicProfileFragment)
            bDeleteAccount.setOnClickListener(this@BasicProfileFragment)
            onChangedText(etEmail.mBinding.editText)

            etName.mBinding.editText.doAfterTextChanged {
                val textEntered = it.toString()
                if (textEntered.isNotEmpty() && textEntered.contains("  ")) {
                    etName.mBinding.editText.setText(
                        etName.mBinding.editText.text.toString().replace("  ", "")
                    );
                    etName.mBinding.editText.text?.length?.let { it1 ->
                        etName.mBinding.editText.setSelection(
                            it1
                        )
                    }
                    etName.errorText = langData?.fieldValidationStrings?.nameCorrectValidation.getSafe()
                    etName.requestFocus()
                }
            }
        }
    }

    override fun init() {
        mBinding.lifecycleOwner = this
        initDialog()
        mBinding.etName.mBinding.editText.filters = arrayOf(filter)
        mBinding.userProfile = profileViewModel.userProfile.value
        observe()
        if (profileViewModel.userProfile.value?.userPhoneNumbers?.isNotEmpty() == true) {

            profileViewModel.userProfile.value?.userPhoneNumbers?.map {
                val contactItem = ContactItem()
                val phoneCategory = metaData?.phoneCategories?.find { phone ->
                    phone.genericItemId == it.category?.toInt().getSafe()
                }
                var categoryPhone = ""
                if (phoneCategory != null)
                    categoryPhone = phoneCategory.genericItemName.getSafe()
                if (categoryPhone.uppercase().getSafe().contains("other".uppercase().getSafe()))
                    categoryPhone = it.other.getSafe()
                contactItem.apply {
                    countryCode = it.countryCode.getSafe()
                    category = categoryPhone
                    categoryId = it.category?.toInt().getSafe()
                    mobileNumber = it.phoneNumber.getSafe()
                    other = it.other.getSafe()
                    title = categoryPhone
                    desc = it.countryCode + it.phoneNumber.getSafe()
                    drawable = R.drawable.ic_call
                }

                if (isUniqueItem(
                        contactItem,
                        profileViewModel.contacts as ArrayList<MultipleViewItem>
                    )
                )
                    profileViewModel.contacts.add(contactItem)
            }
        }
        if (profileViewModel.userProfile.value?.userLocations?.isNotEmpty() == true) {

            profileViewModel.userProfile.value?.userLocations?.map {
                val categoryLoc = profileViewModel.getLocationCategory(
                    metaData?.locationCategories,
                    it.category,
                    it.other
                )

                val userLocation = AddressModel()
                userLocation.apply {
                    streetAddress = it.street.getSafe()
                    category = categoryLoc
                    categoryId = it.category?.toInt().getSafe()
                    floor = it.floorUnit.getSafe()
                    subLocality = it.sublocality.getSafe()
                    region = it.category.getSafe()
                    latitude = it.lat?.toDouble()
                    longitude = it.long?.toDouble()
                    region = it.region.getSafe()
                    other = it.other.getSafe()
                    title = categoryLoc
                    desc = it.address.getSafe()
                    address = it.address.getSafe()
                    drawable = R.drawable.ic_location_trans
                }

                if (isUniqueItem(
                        userLocation,
                        profileViewModel.addresses as ArrayList<MultipleViewItem>
                    )
                )
                    profileViewModel.addresses.add(userLocation)
            }
        }

        mBinding.apply {
            caContacts.listItems = profileViewModel.contacts as ArrayList<MultipleViewItem>
            caAddresses.listItems = profileViewModel.addresses as ArrayList<MultipleViewItem>

            caAddresses.onDeleteClick = { item, _ ->
                DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                    title = langData?.dialogsStrings?.confirmDelete.getSafe(),
                    message =  langData?.dialogsStrings?.deleteDesc.getSafe(),
                    positiveButtonStringText = langData?.globalString?.yes.getSafe(),
                    negativeButtonStringText = langData?.globalString?.no.getSafe(),
                    buttonCallback = {
                        (profileViewModel.userProfile.value?.userLocations as ArrayList<MultipleViewItem>).removeIf { it.desc == item.desc }

                        profileViewModel.addresses.remove(item)
                        caAddresses.listItems =
                            profileViewModel.addresses as ArrayList<MultipleViewItem>
                    }
                )
            }
            caContacts.onDeleteClick = { item, _ ->
                DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                    title = langData?.dialogsStrings?.confirmDelete.getSafe(),
                    message = langData?.dialogsStrings?.deleteDesc.getSafe(),
                    positiveButtonStringText = langData?.globalString?.yes.getSafe(),
                    negativeButtonStringText = langData?.globalString?.no.getSafe(),
                    buttonCallback = {
                        (profileViewModel.userProfile.value?.userPhoneNumbers as ArrayList<MultipleViewItem>).removeIf { it.desc == item.desc }
                        profileViewModel.contacts.remove(item)
                        caContacts.listItems =
                            profileViewModel.contacts as ArrayList<MultipleViewItem>
                    }
                )
            }
        }
    }

    private fun isUniqueItem(item: MultipleViewItem, list: ArrayList<MultipleViewItem>): Boolean {
        return list.find { it.title == item.title && it.desc == item.desc } == null
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.bSave -> {
                closeKeypad()
                sendPersonalProfile()
            }
            R.id.bDeleteAccount -> {
                deleteAccountFunctionality()
            }
        }
    }

    private fun observe() {
        isEmailVerified.observe(this) {
            it?.let {
                if (it) {
                    mBinding.tvVerifyEmail.text = mBinding.langData?.personalprofileBasicScreen?.verified
                } else {
                    val spannableText = SpannableString(mBinding.langData?.personalprofileBasicScreen?.verifyEmail)
                    mBinding.langData?.personalprofileBasicScreen?.verifyEmail?.length?.let { it1 ->
                        spannableText.setSpan(
                            UnderlineSpan(),
                            0, // start
                            it1, // end
                            0 // flags
                        )
                    }
                    mBinding.tvVerifyEmail.text = spannableText
                }
            }
        }
    }

    private fun dropdownList() {
        //country code
        val countryCodeList = getCountryCodeList()
        mBinding.cdCountryCode.data = countryCodeList.getSafe()
        var indexSelection = 0

        if (profileViewModel.userProfile.value?.countryCode.getSafe().isEmpty()) {
            mBinding.cdCountryCode.selectionIndex = indexSelection
            profileViewModel.userProfile.value?.countryCode =
                countryCodeList?.get(mBinding.cdCountryCode.selectionIndex).getSafe()

        } else {
            val countryCodeIndex = metaData?.countries?.indexOfFirst {
                it.phoneCode == profileViewModel.userProfile.value?.countryCode ||
                        "${it.shortName}(${it.phoneCode})" == profileViewModel.userProfile.value?.countryCode
            }
            indexSelection = countryCodeIndex.getSafe()
            if (countryCodeIndex == -1)
                indexSelection = 0
        }
        mBinding.cdCountryCode.selectionIndex = indexSelection.getSafe()
        profileViewModel._userProfile.value?.countryCode =
            countryCodeList?.get(indexSelection.getSafe())
        phoneLength = metaData?.countries?.get(indexSelection.getSafe())?.phoneNoLimit.getSafe()
        mBinding.etMobileNumber.maxLength = phoneLength
        //country list
        val countryList = getCountryList()
        mBinding.cdCountry.data = countryList as ArrayList<String>
        if (profileViewModel.userProfile.value?.countryId != 0) {
            val index =
                metaData?.countries?.indexOfFirst { it.id == profileViewModel.userProfile.value?.countryId }
            if (index != -1)
                mBinding.cdCountry.selectionIndex = index.getSafe()
        } else
            profileViewModel._userProfile.value?.countryId =
                metaData?.countries?.get(indexSelection)?.id.getSafe()


        mBinding.cdCountry.onItemSelectedListener = { _, position: Int ->
            mBinding.cdCity.data = arrayListOf()
            profileViewModel._userProfile.value?.countryId =
                metaData?.countries?.get(position)?.id.getSafe()
            setCityList(profileViewModel.userProfile.value?.countryId.getSafe())
            mBinding.cdCity.mBinding.dropdownMenu.setText("")
            mBinding.cdCity.hint = mBinding.langData?.globalString?.city.getSafe()
        }

        setCityList(profileViewModel.userProfile.value?.countryId.getSafe())

        val genderList = metaData?.genders?.map { it.genericItemName } as ArrayList<String>

        mBinding.cdGender.data = genderList
        var genderIndex =
            metaData?.genders?.indexOfFirst { it.genericItemId == profileViewModel.userProfile.value?.genderId }.getSafe()

        if (genderIndex == -1)
            genderIndex = indexSelection

        if (genderIndex >= 0 && genderIndex < genderList.size) { // to avoid for index out of bound exception
            mBinding.cdGender.selectionIndex = genderIndex.getSafe()
        }
        mBinding.cdGender.onItemSelectedListener = { _, position: Int ->
            profileViewModel._userProfile.value?.genderId =
                metaData?.genders?.get(position)?.genericItemId.getSafe()
            profileViewModel.genderId.postValue(metaData?.genders?.get(position)?.genericItemId)
        }
    }

    private fun setCityList(countryId: Int) {
        citylist = getCityList(countryId = countryId)
        val cityList = citylist?.map { it.name }
        if (cityList?.size.getSafe() > 0) {
            mBinding.cdCity.data = cityList as ArrayList<String>
            if (profileViewModel.userProfile.value?.cityId != 0) {
                val index =
                    citylist?.indexOfFirst { it.id == profileViewModel.userProfile.value?.cityId }
                if (index != -1)
                    mBinding.cdCity.selectionIndex = index.getSafe()
//                else
//                    mBinding.cdCity.selectionIndex = 0 //  select 0 index city
            } else {
                profileViewModel._userProfile.value?.cityId = metaData?.cities?.get(0)?.id.getSafe()
            }

            mBinding.cdCity.onItemSelectedListener = { _, position: Int ->
                profileViewModel._userProfile.value?.cityId = citylist?.get(position)?.id.getSafe()
                mBinding.cdCity.clearFocus()

            }
        } else {
            profileViewModel.userProfile.value?.cityId = null
            profileViewModel.userProfile.value?.city = null
            mBinding.cdCity.data = arrayListOf<String>()
            mBinding.cdCity.mBinding.dropdownMenu.setText("", true)
        }
    }

    private fun validateEmail() {
        val email = mBinding.etEmail.text
        if (isValid(email).not()) {
            mBinding.etEmail.errorText =mBinding.langData?.fieldValidationStrings?.emailValidation

            return
        }
        if (isEmailValid(email).not()) {
            mBinding.etEmail.errorText =mBinding.langData?.fieldValidationStrings?.emailValidation
            return
        }
        if (timer != null) {
            showEmailVerifyDialog()
            return
        }

        val sendEmailRequest = EmailSendRequest(email = email)
        sendEmail(sendEmailRequest)
    }

    override fun onDetach() {
        super.onDetach()
        timer?.cancel()
    }

    private fun setDrawableEnd(textView: TextView, @DrawableRes icon: Int = R.drawable.ic_done) {
        val img = ContextCompat.getDrawable(requireContext(), icon)
        img?.setBounds(0, 0, 30, 30)
        textView.setCompoundDrawables(null, null, img, null)
    }

    private fun sendPersonalProfile() {
        val phoneNumber = profileViewModel.userProfile.value?.phoneNumber
        val name = profileViewModel.userProfile.value?.fullName?.trim()
        val email = profileViewModel.userProfile.value?.email
        val cnic = profileViewModel.userProfile.value?.cnic
        val dob = profileViewModel.userProfile.value?.dateOfBirth
        val city = mBinding.cdCity.mBinding.dropdownMenu.text.toString()

        if (isValidPhone(phoneNumber).not()) {
            mBinding.etMobileNumber.errorText = mBinding.langData?.fieldValidationStrings?.mobileNumEmpty
            mBinding.etMobileNumber.requestFocus()
            mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
            return
        }
        if (isValidPhoneLength(phoneNumber.getSafe(), phoneLength = phoneLength).not()) {
            mBinding.etMobileNumber.errorText = mBinding.langData?.fieldValidationStrings?.mobileNumberValidation
            mBinding.etMobileNumber.requestFocus()
            mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
            return
        }
        if (isValidName(name).not()) {
            mBinding.etName.errorText = mBinding.langData?.fieldValidationStrings?.nameCorrectValidation
            mBinding.etName.requestFocus()
            mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
            return
        }

        if (name?.startsWith(" ").getSafe()) {
            mBinding.etName.errorText = mBinding.langData?.fieldValidationStrings?.nameCorrectValidation
            mBinding.etName.requestFocus()
            mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
            return
        }

        if (email?.isNotEmpty() == true) {
            if (isEmailValid(email.getSafe()).not()) {
                mBinding.etEmail.errorText = mBinding.langData?.fieldValidationStrings?.emailValidation
                mBinding.etEmail.requestFocus()
                mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
                return
            }
        }

        if (cnic?.isNotEmpty() == true) {
            if (isValidCnicLength(cnic.getSafe()).not()) {
                mBinding.etCnic.errorText = mBinding.langData?.fieldValidationStrings?.errorCNIC
                mBinding.etCnic.requestFocus()
                return
            }
        }

        if (isValid(dob).not()) {
            mBinding.dob.errorText =mBinding.langData?.fieldValidationStrings?.dateOfBirthValidation
            mBinding.dob.requestFocus()
            mBinding.dob.requestFocus()
            return
        }
        if (isValid(profileViewModel.userProfile.value?.cityId.toString()).not()) {

            showToast(mBinding.langData?.fieldValidationStrings?.selectCityValidation.getSafe())
            return
        }
        if (isValid(city).not()) {
            mBinding.cdCity.errorText = mBinding.langData?.fieldValidationStrings?.selectCityValidation
            mBinding.cdCity.hint = mBinding.langData?.globalString?.city.getSafe()
            mBinding.cdCity.requestFocus()
            return
        }
        callCreateProfileApi()

    }

    private fun callCreateProfileApi() {
        val userLocation = mBinding.caAddresses.listItems as List<AddressModel>
        val userLoc = userLocation.map {
            UserLocation(
                it.address,
                it.categoryId.toString(),
                it.floor,
                it.latitude.toString(),
                it.longitude.toString(),
                it.other,
                it.streetAddress,
                region = it.region,
                sublocality = it.subLocality
            )
        }
        val contactList = mBinding.caContacts.listItems as List<ContactItem>
        val userPhoneNumbers =
            contactList.map {
                UserPhoneNumber(
                    it.countryCode,
                    it.mobileNumber,
                    it.categoryId.toString(),
                    it.other
                )
            }

        val dob = getDateInFormat(
            profileViewModel.userProfile.value?.dateOfBirth.getSafe(),
            "dd/MM/yyyy",
            "yyyy-MM-dd"
        )
        var email: String? = null
        if (profileViewModel.userProfile.value?.email != "")
            email = profileViewModel.userProfile.value?.email
        var cnic: String? = null
        if (profileViewModel.userProfile.value?.cnic != "")
            cnic = profileViewModel.userProfile.value?.cnic
        val userRequest =
            UserRequest(
                cnic = cnic,
                phoneNumber = profileViewModel.userProfile.value?.phoneNumber,
                countryCode = getCountryCode(profileViewModel.userProfile.value?.countryCode.getSafe()),
                dateOfBirth = dob,
                email = email,
                fullName = profileViewModel.userProfile.value?.fullName.getSafe(),
                gender = profileViewModel.userProfile.value?.gender.getSafe(),
                genderId = profileViewModel.userProfile.value?.genderId.getSafe(),
                cityId = profileViewModel.userProfile.value?.cityId,
                countryId = profileViewModel.userProfile.value?.countryId,
                userLocations = userLoc,
                userPhoneNumbers = userPhoneNumbers
            )

        if (isOnline(requireActivity())) {
            profileViewModel.createProfileApiCall(userRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<UserResponse>
                        response.data?.let { it1 ->
                            tinydb.putObject(
                                Enums.TinyDBKeys.USER.key,
                                it1
                            )
                        }
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =mBinding.langData?.messages?.profile_update_success.getSafe(),
                                buttonCallback = { mBinding.svContainer.requestFocus() },
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

    private lateinit var emailVerifyDialog: AlertDialog

    private fun initDialog() {
        emailVerifyDialogViewBinding = DialogVerifyCodeBinding.inflate(layoutInflater)
        emailVerifyDialogViewBinding.apply {
            langData=ApplicationClass.mGlobalData
            etVerificationCode.hint=langData?.globalString?.verificationCode.getSafe()
        }
        val dialogBuilder = AlertDialog.Builder(requireActivity()).apply {
            setView(emailVerifyDialogViewBinding.root)
            setCancelable(false)
            setPositiveButton(mBinding.langData?.globalString?.verify, null)
            setNegativeButton(mBinding.langData?.globalString?.cancel) { _, _ -> }
            create().apply {
                emailVerifyDialog = this
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (isValid(emailVerifyDialogViewBinding.etVerificationCode.text).not()) {
                            emailVerifyDialogViewBinding.etVerificationCode.errorText = mBinding.langData?.fieldValidationStrings?.verificationCodeValidation
                            return@setOnClickListener
                        }

                        if (isValidCodeLength(emailVerifyDialogViewBinding.etVerificationCode.text).not()) {
                            emailVerifyDialogViewBinding.etVerificationCode.errorText =mBinding.langData?.fieldValidationStrings?.verificationCodeCorrentValidation

                            return@setOnClickListener
                        }
                        closeKeypad()
                        val emailVerifyRequest = EmailVerifyRequest(
                            mBinding.etEmail.text,
                            emailVerifyDialogViewBinding.etVerificationCode.text
                        )
                        verifyEmail(emailVerifyRequest)

                        dismiss()
                    }
                }
                val spannableText = SpannableString(mBinding.langData?.globalString?.resendCode)
                mBinding.langData?.globalString?.resendCode?.length?.let {
                    spannableText.setSpan(
                        UnderlineSpan(),
                        0, // start
                        it, // end
                        0 // flags
                    )
                }
                emailVerifyDialogViewBinding.tvVerifyCode.text = spannableText
                emailVerifyDialogViewBinding.tvVerifyCode.apply {
                    setOnClickListener {  //resend code
                        if (timer == null) {
                            isEnabled = false
                            setCompoundDrawables(null, null, null, null)
                            val sendEmailRequest = EmailSendRequest(email = mBinding.etEmail.text)
                            sendEmail(sendEmailRequest)

                            timer =
                                viewModel.startTimer(
                                    emailVerifyDialogViewBinding.tvVerifyCode,mBinding.langData?.globalString?.sec.getSafe()
                                    , metaData?.otpRespondTime
                                ) {
                                    isEnabled = true
                                    setDrawableEnd(this, R.drawable.ic_arrow_fw_primary)

                                    text = spannableText
                                    timer = null
                                }
                        }
                    }
                }
            }
        }
    }

    private fun showEmailVerifyDialog() {
        try {
            if (emailVerifyDialog.isShowing.not())
                emailVerifyDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendEmail(sendEmailRequest: EmailSendRequest) {
        if (isOnline(requireActivity())) {
            profileViewModel.sendEmail(sendEmailRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        if (response.status in 200..299) {
                            showEmailVerifyDialog()
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
                        if (
                            it.generalResponse.status == 422 &&
                            it.generalResponse.errors?.get(0).equals(
                                getString(R.string.email_check_verify), false).getSafe()
                        ) {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message = it.generalResponse.errors?.get(0).getSafe(),
                                    buttonCallback = {},
                                )
                        } else {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe()  ,
                                    buttonCallback = {},
                                )
                        }
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

    private fun verifyEmail(verifyRequest: EmailVerifyRequest) {
        if (isOnline(requireActivity())) {
            profileViewModel.verifyEmail(verifyRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<VerifyEmailResponse>
                        if (response.status in 200..299) {
                            isEmailVerified.postValue(true)
//                            setDrawableEnd(mBinding.tvVerifyEmail)
                            mBinding.ivDone.setVisible(true)
                            mBinding.tvVerifyEmail.visibility = View.INVISIBLE
                            user?.email = response.data?.email
                            user?.let { usr -> tinydb.putObject(Enums.TinyDBKeys.USER.key, usr) }
                            mBinding.bSave.isEnabled = true
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

    private fun onChangedText(editText: TextInputEditText) {
        editText.addTextChangedListener {
            mBinding.apply {
                val spannableText = SpannableString(mBinding.langData?.personalprofileBasicScreen?.verifyEmail)
                mBinding.langData?.personalprofileBasicScreen?.verifyEmail?.let { it1 ->
                    spannableText.setSpan(
                        UnderlineSpan(),
                        0, // start
                        it1.length, // end
                        0 // flags
                    )
                }
                if (user?.email.isNullOrEmpty().not() && user?.email?.equals(etEmail.text, true)
                        .getSafe()
                ) {
                    ivDone.visible()
                    tvVerifyEmail.invisible()
                    bSave.isEnabled = true

                } else if (mBinding.etEmail.text.isEmpty()) {
                    tvVerifyEmail.apply {
                        isClickable = false
                        isEnabled = false
                    }

                    ivDone.gone()
                    tvVerifyEmail.visible()
                    bSave.isEnabled = true

                } else {
                    tvVerifyEmail.apply {
                        text = spannableText
                        isClickable = true
                        isEnabled = true
                    }

                    ivDone.gone()
                    tvVerifyEmail.visible()
                    bSave.isEnabled = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dropdownList()
    }

    private fun deleteAccountApi() {
        val userId = DataCenter.getUser()?.id
        val request = DeleteAccountRequest(userAccountId = userId)
        if (isOnline(requireActivity())) {
            viewModel.deleteAccount(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        if (response.status in 200..299) {
                            ApplicationClass.twilioChatManager?.apply {
//                                unregisterFcmToken(TinyDB.instance.getString(Enums.TinyDBKeys.FCM_TOKEN.key)) // for PN
                                conversationClients?.shutdown()
                                conversationClients?.removeAllListeners()
                                conversationClients = null
                                conversationClientCheck = false
                                conversation?.removeAllListeners()
                                conversation = null
                                conversationSid = ""
                                _messages.value = null
                            }
                            TinyDB.instance.remove(Enums.TinyDBKeys.USER.key)
                            TinyDB.instance.remove(Enums.TinyDBKeys.CHATTOKEN.key)
                            TinyDB.instance.remove(Enums.TinyDBKeys.TOKEN_USER.key)
                            TinyDB.instance.remove(Enums.TinyDBKeys.BOOKING_ID.key)
                            TinyDB.instance.remove(Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
                            TinyDB.instance.remove(com.homemedics.app.utils.Enums.FirstTimeUnique.FIRST_TIME_UNIQUE.key)
                            // navigate to Home Screen
                            requireActivity().startActivity(
                                Intent(requireActivity(), AuthActivity::class.java)
                            )
                            requireActivity().finish()
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
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun deleteAccountFunctionality() {
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            title = mBinding.langData?.globalString?.deleteMyAccount.getSafe(),
            message = mBinding.langData?.dialogsStrings?.deleteAccountRequest.getSafe(),
            positiveButtonStringText = mBinding.langData?.globalString?.yes.getSafe(),
            negativeButtonStringText = mBinding.langData?.globalString?.no.getSafe(),
            buttonCallback = {
                deleteAccountApi()
            }
        )
    }
}