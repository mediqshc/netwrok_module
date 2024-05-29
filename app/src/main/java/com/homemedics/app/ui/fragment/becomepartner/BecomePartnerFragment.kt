package com.homemedics.app.ui.fragment.becomepartner

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.ScrollView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.partnerprofile.PartnerDetailsRequest
import com.fatron.network_module.models.request.partnerprofile.deletePartnerEducation
import com.fatron.network_module.models.request.partnerprofile.deletePartnerSpecialityReq
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.partnerprofile.Services
import com.fatron.network_module.models.response.user.RejectionReason
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentBecomePartnerBinding
import com.homemedics.app.utils.*
import com.homemedics.app.utils.DataCenter.cnicMaskListener
import com.homemedics.app.viewmodel.ProfileViewModel


class BecomePartnerFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentBecomePartnerBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var partnerDetailsResponse = PartnerProfileResponse()

    override fun onPause() {
        super.onPause()
        closeKeypad()
    }

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.tabString?.becomePartner.getSafe()
            langData?.let { lang ->
                etLicense.hint = lang.partnerProfileScreen?.licenseCode.getSafe()
                etYearsExperience.hint = lang.partnerProfileScreen?.yearsOfExperience.getSafe()
                etCnicNumber.hint = lang.globalString?.cnicNumber.getSafe()
                etProfessionalOverview.hint =
                    lang.partnerProfileScreen?.professionalDetailPlaceHolder.getSafe()
                caSpecialties.apply {
                    title = lang.partnerProfileScreen?.specialties.getSafe()
                }
                caEducation.apply {
                    title = lang.partnerProfileScreen?.education.getSafe()
                }
            }
        }
    }

    override fun init() {
        profileViewModel.isFromMenu=1
    }

    override fun getFragmentLayout() = R.layout.fragment_become_partner

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentBecomePartnerBinding
        mBinding.etCnicNumber.mBinding.editText.addTextChangedListener(cnicMaskListener)

        mBinding.lifecycleOwner = this
        mBinding.partnerModel = profileViewModel
        setPartnerType()
        mBinding.etYearsExperience.mBinding.textInputLayout.suffixText = ApplicationClass.mGlobalData?.globalString?.year

    }
    private fun setPartnerType(){
        val font = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular)

        var radioButtonType: RadioButton
        val param = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        metaData?.partnerTypes?.forEachIndexed { index, it ->

            radioButtonType = RadioButton(requireContext())
            radioButtonType.typeface = font
            radioButtonType.layoutParams = param
            val dp = resources.getDimensionPixelSize(R.dimen.dp16)
            val dpRight = resources.getDimensionPixelSize(R.dimen.dp8)
            radioButtonType.setPadding(0, dp, dpRight, dp)

            radioButtonType.text = it.genericItemName
            radioButtonType.id = it.genericItemId.getSafe()
            if (index == 0 && profileViewModel.specialityType==0)
                radioButtonType.isChecked = true

            radioButtonType.textSize = 13F
            mBinding.rgProfession.addView(radioButtonType)
        }

        mBinding.rgProfession.setOnCheckedChangeListener { _, checkedId ->
             if(checkedId!=profileViewModel.specialityType){
                profileViewModel.specialties= arrayListOf()
                if (checkedId== Enums.Profession.DOCTOR.key) {

                    profileViewModel.specialties =metaData?.specialties?.doctorSpecialties  as ArrayList<GenericItem>

                } else {
                    profileViewModel.specialties =  metaData?.specialties?.medicalStaffSpecialties  as ArrayList<GenericItem>
                }
                profileViewModel.selectedSpecialties= arrayListOf()
                mBinding.caSpecialties.listItems =
                    profileViewModel.selectedSpecialties
            }
          profileViewModel.specialityType   = checkedId

        }

    }

    override fun setListeners() {
       mBinding. actionbar.onAction1Click = {
            closeKeypad()
            findNavController().popBackStack()
        }
//        mBinding.etYearsExperience.mBinding.editText.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                mBinding.etYearsExperience.inputType = InputType.TYPE_CLASS_NUMBER
//            } else {
//                mBinding.etYearsExperience.inputType = InputType.TYPE_CLASS_TEXT
//                setYearSuffix()
//
//            }
//        }
        mBinding.apply {
            caSpecialties.onAddItemClick = {

                findNavController().safeNavigate(
                    BecomePartnerFragmentDirections.actionBecomePartnerFragmentToSpecialitiesFragment()
                )
            }
            caEducation.onAddItemClick = {
                profileViewModel.educationDocument = arrayListOf()
                findNavController().safeNavigate(
                    BecomePartnerFragmentDirections.actionBecomePartnerFragmentToAddEducationFragment()
                )
            }
            tvUploadCnic.setOnClickListener(this@BecomePartnerFragment)
            bSubmitRequest.setOnClickListener(this@BecomePartnerFragment)
        }
    }

    private fun setYearSuffix() {
        if (profileViewModel.yearsOfExperience.value.getSafe().isNotEmpty()) {
            var exp = profileViewModel.yearsOfExperience.value?.toInt().getSafe()
            if (exp == 0)
                exp = 1
           val year= if(exp>1)ApplicationClass.mGlobalData?.globalString?.years else ApplicationClass.mGlobalData?.globalString?.year
            mBinding.etYearsExperience.mBinding.textInputLayout.suffixText =year
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvUploadCnic -> {
                findNavController().safeNavigate(
                    BecomePartnerFragmentDirections.actionBecomePartnerFragmentToUploadCnicPictureFragment()
                )
            }
            R.id.bSubmitRequest -> submitRequest()
        }
    }

    override fun onResume() {
        super.onResume()
        getPartnerDetails()
    }

    private fun submitRequest() {
        val licenseCodes = mBinding.etLicense.text
        var yearsExperiences = profileViewModel.yearsOfExperience.value
        if (yearsExperiences.getSafe().isEmpty())
            yearsExperiences = null
        val cnicNumber = mBinding.etCnicNumber.text
        val overviews = mBinding.etProfessionalOverview.text

        if (mBinding.rgProfession.checkedRadioButtonId == -1) {
            showToast(mBinding.langData?.fieldValidationStrings?.errorProfession.getSafe())
            return
        }
        if (isValid(cnicNumber).not()) {
            mBinding.etCnicNumber.errorText = mBinding.langData?.fieldValidationStrings?.cnicNumber.getSafe()
            mBinding.etCnicNumber.requestFocus()
            mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
            return
        }
        if (isValidCnicLength(cnicNumber).not()) {
            mBinding.etCnicNumber.errorText = mBinding.langData?.fieldValidationStrings?.errorCNIC.getSafe()
            mBinding.etCnicNumber.requestFocus()
            mBinding.svContainer.fullScroll(ScrollView.FOCUS_UP)
            return
        }
        if (profileViewModel.selectedSpecialties.isEmpty()) {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.globalString?.error.getSafe(),
                    message = mBinding.langData?.fieldValidationStrings?.specialityError.getSafe(),
                    buttonCallback = { },
                )
            return
        }


        val partnerDetailsRequest = PartnerDetailsRequest()
        partnerDetailsRequest.apply {
            cnic = cnicNumber
            licenseCode = licenseCodes
            overview = overviews.toString()
            type = profileViewModel.specialityType
            yearsOfExperience = yearsExperiences
        }
        storePartnerDetails(partnerDetailsRequest)
    }

    private fun applicationReviewDialog(reason: RejectionReason? = null) {
        var message = mBinding.langData?.partnerProfileScreen?.partnerDetailAdded.getSafe()
        if (partnerDetailsResponse.applicationStatusId == Enums.ApplicationStatus.REJECTED.key)
            message = reason?.reason?.getSafe() ?: mBinding.langData?.dialogsStrings?.partnerRejected.getSafe()
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = reason?.title?.getSafe() ?: mBinding.langData?.globalString?.information.getSafe(),
                message = message,
                buttonCallback = {   },
            )
    }

    private fun storePartnerDetails(partnerDetailsRequest: PartnerDetailsRequest) {
        profileViewModel.storePartnerDetails(partnerDetailsRequest = partnerDetailsRequest)
            .observe(this) {
                if (isOnline(activity)) {
                when (it) {
                    is ResponseResult.Success -> {
                        hideLoader()
                        try {
                            val response = it.data as ResponseGeneral<PartnerProfileResponse>
                            val partnerResponse = response.data
                            if (partnerResponse != null) {
                                setData(partnerResponse)
                                mBinding.bSubmitRequest.text=mBinding.langData?.globalString?.requestSubmitted.getSafe()
                                mBinding.flLayout.requestFocus()
                            }
                            applicationReviewDialog(partnerResponse?.rejectionReason)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Failure -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {
                        hideLoader()
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
    }

    private fun getList(list: List<GenericItem>?): List<GenericItem> {
        return list?.map { genItem ->
            val specialityObj =
                partnerDetailsResponse.specialities?.find { spec -> spec.genericItemId == genItem.genericItemId }
            if (specialityObj != null)
                genItem.isSelected = true
            genItem
        } as ArrayList<GenericItem>
    }

    private fun getPartnerDetails() {
        profileViewModel.getPartnerDetails().observe(this) {
            if (isOnline(activity)) {
                when (it) {
                    is ResponseResult.Success -> {
                        hideLoader()
                        try {
                            val response = it.data as ResponseGeneral<PartnerProfileResponse>
                            val partnerResponse = response.data
                            if (partnerResponse != null) {
                                setData(partnerResponse)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Failure -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {
                        hideLoader()
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
    }

    private fun setData(partnerResponse: PartnerProfileResponse) {

        partnerDetailsResponse = partnerResponse
        profileViewModel.apply {

            if (cnic.value.isNullOrEmpty()) _cnic.postValue(partnerResponse.cnic)
            if (licenseCode.value.isNullOrEmpty()) _licenseCode.postValue(
                partnerResponse.licenseCode
            )
            if (yearsOfExperience.value.isNullOrEmpty())
                _yearsOfExperience.postValue(partnerResponse.yearsOfExperience)
            if (overview.value.isNullOrEmpty()) _overview.postValue(
                partnerResponse.overview
            )
        }

        profileViewModel.partnerType =
            partnerDetailsResponse.type.getSafe()
        var memberId = 0
        if (partnerDetailsResponse.memberTypeId.getSafe().isNotEmpty())
            memberId =
                partnerDetailsResponse.memberTypeId?.toInt().getSafe()

        if (partnerDetailsResponse.memberTypeId.getSafe()
                .isNotEmpty() || mBinding.rgProfession.checkedRadioButtonId == memberId
        ) {
            profileViewModel.specialityType =
                partnerDetailsResponse.memberTypeId?.toInt().getSafe()
            mBinding.rgProfession.check(profileViewModel.specialityType)
            profileViewModel.selectedSpecialties =
                partnerResponse.specialities?.map { sp ->
                    MultipleViewItem(
                        itemId = sp.genericItemId.toString(),
                        title = sp.genericItemName,
                        imageUrl = sp.icon_url,

                        ).apply { isSelected = true }

                } as ArrayList<MultipleViewItem>
        } else {
            var checkid = mBinding.rgProfession.checkedRadioButtonId
            if (checkid == -1) {
                checkid = 1
                profileViewModel.selectedSpecialties = arrayListOf()
            }
            profileViewModel.specialityType = checkid

            mBinding.rgProfession.check(profileViewModel.specialityType)

        }

        if (partnerDetailsResponse.cnicFront.getSafe()
                .isNotEmpty() || partnerDetailsResponse.cnicBack.getSafe().isNotEmpty()
        ) {
            val uploaded = mBinding.langData?.partnerProfileScreen?.cnicUloaded.getSafe()
            val text = SpannableString(uploaded)
            text.setSpan(
                UnderlineSpan(),
                0, // start
                uploaded.length, // end
                0 // flags
            )
            mBinding.tvUploadCnic.text = text


            mBinding.tvUploadCnic.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_edit_primary,
                0
            )
        }
        if (profileViewModel.specialityType == Enums.Profession.DOCTOR.key) {

            profileViewModel.specialties =
                getList(metaData?.specialties?.doctorSpecialties) as ArrayList<GenericItem>

            profileViewModel.serviceList =
                partnerDetailsResponse.doctorServices as ArrayList<Services>
        } else {
            profileViewModel.specialties =
                getList(metaData?.specialties?.medicalStaffSpecialties) as ArrayList<GenericItem>

            profileViewModel.serviceList =
                partnerDetailsResponse.staffServices as ArrayList<Services>
        }



        if (partnerResponse.specialities.isNullOrEmpty()
                .not() && partnerResponse.educations.isNullOrEmpty()
                .not() && partnerResponse.cnicBack.isNullOrEmpty()
                .not() && partnerResponse.cnicFront.isNullOrEmpty().not()
        )
            mBinding.bSubmitRequest.isEnabled = true
        val gender = getLabelFromId(
            partnerDetailsResponse.genderId.toString().getSafe(),
            metaData?.genders
        )
        mBinding.apply {
            val phoneNumber = "\u200E" + "${partnerDetailsResponse.countryCode}${partnerDetailsResponse.phoneNumber}" // unicode implementation for plus sign
            tvName.text = partnerDetailsResponse.fullName
            tvInfo.text =
                "$gender | $phoneNumber | ${
                    mBinding.langData?.globalString?.dob.getSafe()
                } ${
                    getDateInFormat(
                        partnerDetailsResponse.dateOfBirth.getSafe(),
                        "dd/MM/yyyy",
                        "dd/MM/yyyy"
                    )
                }"
            if (partnerDetailsResponse.profilePicture.getSafe().isEmpty())
                ivThumbnail.setImageResource(
                    getGenderIcon(
                        partnerDetailsResponse.genderId.toString()
                    )
                )
            else
                ivThumbnail.loadImage(
                    partnerDetailsResponse.profilePicture,
                    getGenderIcon(partnerDetailsResponse.genderId.toString())
                )
        }
        setYearSuffix()
        profileViewModel.educationList =
            partnerResponse.educations?.map { edu ->
                val index =
                    metaData?.allCountries?.indexOfFirst { it.id == edu.countryId }
                MultipleViewItem(
                    itemId = edu.id,
                    title = edu.school,
                    desc = edu.degree + " | " + metaData?.allCountries?.get(
                        index.getSafe()
                    )?.name + " | " + edu.year,
                    drawable = R.drawable.ic_school
                )
            } as ArrayList<MultipleViewItem>
        profileViewModel.selectedWorkExperience =
            partnerResponse.works?.map {
                MultipleViewItem(
                    itemId = it.id,
                    title = it.company,
                    desc = it.designation,
                    drawable = R.drawable.ic_company
                )
            } as ArrayList<MultipleViewItem>
        addSpecialities()
        addEducation()

        if (partnerDetailsResponse.applicationStatusId == Enums.ApplicationStatus.UNDER_REVIEW.key || partnerDetailsResponse.applicationStatusId == Enums.ApplicationStatus.REJECTED.key) {
            mBinding.apply {
                cnLayout.requestFocus()
                etLicense.clearFocus()
                etYearsExperience.clearFocus()
                etCnicNumber.clearFocus()
                etProfessionalOverview.clearFocus()

            if (partnerDetailsResponse.applicationStatusId == Enums.ApplicationStatus.UNDER_REVIEW.key) {

                bSubmitRequest.isClickable = false
                bSubmitRequest.isEnabled = false
               bSubmitRequest.text=mBinding.langData?.globalString?.requestSubmitted.getSafe()

                 flLayout.setVisible(true)
            } else {
                bSubmitRequest.isClickable = true
                bSubmitRequest.isEnabled = true
                flLayout.setVisible(false)
            }
            }
            val text = SpannableString(partnerResponse.applicationStatus.getSafe())
            text.setSpan(
                UnderlineSpan(),
                0, // start
                partnerResponse.applicationStatus.getSafe().length, // end
                0 // flags
            )
            mBinding.actionbar.mBinding.tvDesc.text = text

            mBinding.actionbar.action2Res = R.drawable.ic_info
            mBinding.actionbar.onAction2Click = {
                applicationReviewDialog(partnerResponse.rejectionReason)
            }

        }


    }

    private fun addSpecialities() {
        if(profileViewModel.selectedSpecialties.isEmpty() && profileViewModel.isFromMenu==0) {
            profileViewModel.selectedSpecialties = partnerDetailsResponse.specialities.getSafe()
        }

        mBinding.caSpecialties.listItems =
            profileViewModel.selectedSpecialties
        mBinding.caSpecialties.onDeleteClick = { item, pos ->
            if(pos < mBinding.caSpecialties.listItems.size){
                DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                    title = mBinding.langData?.dialogsStrings?.confirmDelete.getSafe(),
                    message = mBinding.langData?.dialogsStrings?.deleteDesc.getSafe(),
                    positiveButtonStringText = mBinding.langData?.globalString?.yes.getSafe(),
                    negativeButtonStringText = mBinding.langData?.globalString?.no.getSafe(),
                    buttonCallback = {
                        calldeleteSpecialityApi(item, pos)
                    }
                )
            }
        }
    }

    private fun addEducation() {
        mBinding.caEducation.listItems = profileViewModel.educationList
        mBinding.caEducation.onDeleteClick = { item, pos ->
            DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                title = mBinding.langData?.dialogsStrings?.confirmDelete.getSafe(),
                message = mBinding.langData?.dialogsStrings?.deleteEducationDesc.getSafe(),
                positiveButtonStringText = mBinding.langData?.globalString?.yes.getSafe(),
                negativeButtonStringText = mBinding.langData?.globalString?.no.getSafe(),
                buttonCallback = {
                    calldeleteEducApi(item)
                }
            )

        }
    }

    private fun calldeleteEducApi(item: MultipleViewItem) {
        val deletePartnerEducation =
            deletePartnerEducation(educationId = item.itemId?.toInt().getSafe())
        profileViewModel.calldeleteEducation(deletePartnerEducation).observe(this) {
            if (isOnline(activity)) {
                when (it) {
                    is ResponseResult.Success -> {
                        hideLoader()
                        try {

                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message = mBinding.langData?.messages?.edu_delete_msg.getSafe(),
                                    buttonCallback = {
                                        profileViewModel.educationList.remove(item)
                                        mBinding.caEducation.listItems =
                                            profileViewModel.educationList

                                    },
                                )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Failure -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {
                        hideLoader()
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
    }

    private fun calldeleteSpecialityApi(item: MultipleViewItem, pos: Int) {
        val deletePartnerSpecialityReq =
            deletePartnerSpecialityReq(item.itemId?.toInt().getSafe())
        profileViewModel.calldeleteSpeciality(deletePartnerSpecialityReq).observe(this) {
            if (isOnline(activity)) {
                when (it) {
                    is ResponseResult.Success -> {
                        hideLoader()
                        try {
                            profileViewModel.specialties.find {
                                it.itemId == item.itemId.getSafe()
                            }?.apply { isSelected = false }
                            profileViewModel.selectedSpecialties.removeIf {it.itemId == item.itemId.getSafe()  }
                            mBinding.caSpecialties.listItems = arrayListOf()
                            mBinding.caSpecialties.listItems =
                                profileViewModel.selectedSpecialties

                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message = mBinding.langData?.messages?.speciality_delete_msg.getSafe(),
                                    buttonCallback = {


                                    },
                                )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Failure -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        hideLoader()
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {
                        hideLoader()
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
    }

}