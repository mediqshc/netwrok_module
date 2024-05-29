package com.homemedics.app.ui.fragment.partner_profile

import android.text.InputType
import android.view.View
import android.widget.ScrollView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.partnerprofile.PartnerDetailsRequest
import com.fatron.network_module.models.request.partnerprofile.deletePartnerEducation
import com.fatron.network_module.models.request.partnerprofile.deletePartnerSpecialityReq
import com.fatron.network_module.models.request.partnerprofile.deletePartnerWork
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.partnerprofile.Services
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentProfileBinding
import com.homemedics.app.utils.*
import com.homemedics.app.utils.DataCenter.cnicMaskListener
import com.homemedics.app.viewmodel.ProfileViewModel

class ProfileFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentProfileBinding
    private var partnerProfile = PartnerProfileResponse()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val pipe = "\u007C"
    private val start = "\u2066"
    private val end = "\u2069"

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            etLicense.hint = langData?.partnerProfileScreen?.licenseCode.getSafe()
            etYearsExperience.hint = langData?.partnerProfileScreen?.yearsOfExperience.getSafe()
            etProfessionalOverview.hint = langData?.partnerProfileScreen?.professionalDetailPlaceHolder.getSafe()
            etCnicNumber.hint = langData?.globalString?.cnicNumber.getSafe()
        }
    }

    override fun init() {
        mBinding.apply {
            etCnicNumber.mBinding.editText.addTextChangedListener(cnicMaskListener)
            detailLayout.ivDelete.setVisible(false)
            detailLayout.ivAccept.setVisible(false)
            val dp = resources.getDimensionPixelSize(R.dimen.dp16)
            detailLayout.connectionLayout.setPadding(0,dp , 0, 0)
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_profile

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentProfileBinding
        mBinding.etYearsExperience.mBinding.textInputLayout.suffixText =   ApplicationClass.mGlobalData?.globalString?.year


    }

    override fun setListeners() {
        mBinding.apply {
            bSubmitRequest.setOnClickListener(this@ProfileFragment)
            caSpecialties.onAddItemClick = {

                findNavController().safeNavigate(
                    PartnerProfileFragmentDirections.actionProfileFragmentToSpecialitiesFragment()
                )
            }
            etCnicNumber.mBinding.textInputLayout.setEndIconOnClickListener {
                findNavController().safeNavigate(
                    R.id.uploadCnicPictureFragment, bundleOf("buttonText" to mBinding.langData?.globalString?.back,"screenTitle" to  mBinding.langData?.partnerProfileScreen?.viewPicCnic)
                )
            }
            caEducation.onAddItemClick = {
                profileViewModel.educationDocument= arrayListOf()
                findNavController().safeNavigate(
                    PartnerProfileFragmentDirections.actionProfileFragmentToAddEducationFragment()
                )
            }
            caWorkExp.onAddItemClick = {
                findNavController().safeNavigate(
                    PartnerProfileFragmentDirections.actionPartnerProfileFragmentToAddWorkExpFragment()
                )
            }
            etYearsExperience.mBinding.editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    mBinding.etYearsExperience.inputType = InputType.TYPE_CLASS_NUMBER
                } else {
                    mBinding.etYearsExperience.inputType = InputType.TYPE_CLASS_TEXT
                    setYearSuffix()

                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bSubmitRequest -> submitRequest()
        }
    }

    private fun submitRequest() {
        val licenseCodes = partnerProfile.licenseCode
        val yearsExperiences = partnerProfile.yearsOfExperience
        val cnicNumber = partnerProfile.cnic
        val overviews = partnerProfile.overview
        mBinding.apply {


            if (isValid(cnicNumber).not()) {
                etCnicNumber.errorText = mBinding.langData?.fieldValidationStrings?.cnicNumber.getSafe()
                etCnicNumber.requestFocus()
                svContainer.fullScroll(ScrollView.FOCUS_UP)
                return
            }
            if (isValidCnicLength(cnicNumber.getSafe()).not()) {
                etCnicNumber.errorText = mBinding.langData?.fieldValidationStrings?.errorCNIC.getSafe()
                etCnicNumber.requestFocus()
                svContainer.fullScroll(ScrollView.FOCUS_UP)
                return
            }
        }

        val partnerDetailsRequest = PartnerDetailsRequest()
        partnerDetailsRequest.apply {
            cnic = cnicNumber
            licenseCode = licenseCodes
            overview = overviews.toString()
            type = partnerProfile.memberTypeId?.toInt()
            yearsOfExperience = yearsExperiences
        }
        storePartnerDetails(partnerDetailsRequest)
    }

    private fun storePartnerDetails(partnerDetailsRequest: PartnerDetailsRequest) {
        profileViewModel.storePartnerDetails(partnerDetailsRequest = partnerDetailsRequest)
            .observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            try {
                                DialogUtils(requireActivity())
                                    .showSingleButtonAlertDialog(
                                        message = mBinding.langData?.partnerProfileScreen?.partnerDetailUpdate.getSafe(),
                                        buttonCallback = {},
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

    private fun addSpecialities() {
        mBinding.caSpecialties.listItems =
            profileViewModel.selectedSpecialties
        mBinding.caSpecialties.onDeleteClick = { item, pos ->
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

    private fun addEducation() {
        mBinding.caEducation.listItems = profileViewModel.educationList
        mBinding.caEducation.onDeleteClick = { item, pos ->
            DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                title = mBinding.langData?.dialogsStrings?.confirmDelete.getSafe(),
                message = mBinding.langData?.dialogsStrings?.deleteDesc.getSafe(),
                positiveButtonStringText = mBinding.langData?.globalString?.yes.getSafe(),
                negativeButtonStringText = mBinding.langData?.globalString?.no.getSafe(),
                buttonCallback = {
                    calldeleteEducApi(item)
                }
            )
        }
    }

    private fun addWorkExp() {
        mBinding.caWorkExp.listItems = profileViewModel.selectedWorkExperience

        mBinding.caWorkExp.onDeleteClick = { item, pos ->
            DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                title = mBinding.langData?.dialogsStrings?.confirmDelete.getSafe(),
                message = mBinding.langData?.dialogsStrings?.deleteDesc.getSafe(),
                positiveButtonStringText = mBinding.langData?.globalString?.yes.getSafe(),
                negativeButtonStringText = mBinding.langData?.globalString?.no.getSafe(),
                buttonCallback = {
                    calldeletePartnerWorkApi(item)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (isOnline(activity)) {
            getPartnerDetails()
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }


    private fun getPartnerDetails() {
        profileViewModel.getPartnerDetails().observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    hideLoader()
                    try {
                        val response = it.data as ResponseGeneral<PartnerProfileResponse>
                        val partnerResponse = response.data
                        if (partnerResponse != null) {
                            partnerProfile = partnerResponse
                            mBinding.partnerModel = partnerResponse
                            profileViewModel.partnerType =
                                partnerProfile.type.getSafe()
                            if( partnerProfile.memberTypeId.getSafe().isNotEmpty())
                                profileViewModel.specialityType =
                                    partnerProfile.memberTypeId?.toInt().getSafe()
                            if (profileViewModel.specialityType == Enums.Profession.DOCTOR.key) {

                                profileViewModel.specialties =
                                    metaData?.specialties?.doctorSpecialties?.map { genItem ->
                                        val specialityObj =
                                            partnerProfile.specialities?.find { spec -> spec.genericItemId == genItem.genericItemId }
                                        if (specialityObj != null)
                                            genItem.isSelected = true
                                        genItem
                                    } as ArrayList<GenericItem>
                                profileViewModel.serviceList =
                                    partnerProfile.doctorServices as ArrayList<Services>
                            } else {

                                profileViewModel.specialties =
                                    metaData?.specialties?.medicalStaffSpecialties?.map { genItem ->
                                        val specialityObj =
                                            partnerProfile.specialities?.find { spec -> spec.genericItemId == genItem.genericItemId }
                                        if (specialityObj != null)
                                            genItem.isSelected = true
                                        genItem
                                    } as ArrayList<GenericItem>
                                profileViewModel.serviceList =
                                    partnerProfile.staffServices as ArrayList<Services>
                            }

                            val gender = getLabelFromId(partnerProfile.genderId.toString().getSafe(), metaData?.genders)
                            mBinding.detailLayout.apply {
                                val dob = getDateInFormat(
                                    partnerProfile.dateOfBirth.getSafe(),
                                    "dd/MM/yyyy",
                                    "dd/MM/yyyy"
                                )
                                tvTitle.text = partnerProfile.fullName
                                tvDesc.text =
                                    "${partnerProfile.type.firstCap()} $pipe $gender $pipe $start${partnerProfile.countryCode}${partnerProfile.phoneNumber}$end $pipe ${
                                        mBinding.langData?.globalString?.dob.getSafe()
                                    } ${
                                        dob.ifEmpty { mBinding.langData?.labPharmacyScreen?.na.getSafe() }
                                    }"
                                if (partnerProfile.profilePicture.getSafe().isEmpty())
                                    ivIcon.setImageResource(getGenderIcon(partnerProfile.genderId.toString()))
                                else
                                    ivIcon.loadImage(
                                        partnerProfile.profilePicture,
                                        getGenderIcon(partnerProfile.genderId.toString())
                                    )
                            }
                            profileViewModel.selectedSpecialties =
                                response.data?.specialities?.map { sp ->
                                    MultipleViewItem(
                                        itemId = sp.genericItemId.toString(),
                                        title = sp.genericItemName,
                                        imageUrl = sp.icon_url,

                                        ).apply {  isSelected = true}

                                } as ArrayList<MultipleViewItem>
                            profileViewModel.educationList =
                                response.data?.educations?.map { edu ->
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
                                response.data?.works?.map {
                                    var exp = 1
                                    if (it.yearsOfExperience.getSafe() != "0")
                                        exp = it.yearsOfExperience?.toInt().getSafe()
                                    var desc = "${it.designation} "
                                    if (it.yearsOfExperience.getSafe() != "") {
                                        val year= if(exp>1) ApplicationClass.mGlobalData?.globalString?.years else ApplicationClass.mGlobalData?.globalString?.year

                                        desc =
                                            "${it.designation} |${Constants.START} ${it.yearsOfExperience ?: 0}  $year  ${Constants.END}"
                                    }
                                    MultipleViewItem(
                                        itemId = it.id,
                                        title = it.company,
                                        desc = desc,
                                        drawable = R.drawable.ic_company
                                    )
                                } as ArrayList<MultipleViewItem>
                            addSpecialities()
                            addWorkExp()
                            setYearSuffix()
                            addEducation()
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
        }
    }

    private fun setYearSuffix() {
        if (partnerProfile.yearsOfExperience.getSafe().isNotEmpty()) {
            var exp = partnerProfile.yearsOfExperience?.toInt().getSafe()
            if (exp == 0)
                exp = 1
            val year= if(exp.toInt()>1) ApplicationClass.mGlobalData?.globalString?.years else ApplicationClass.mGlobalData?.globalString?.year

            mBinding.etYearsExperience.mBinding.textInputLayout.suffixText =
                year
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
                        message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
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
                                profileViewModel.selectedSpecialties as ArrayList<MultipleViewItem>
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message = mBinding.langData?.messages?.speciality_delete_msg.getSafe(),
                                    buttonCallback = { },
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
                        message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                        buttonCallback = {},
                    )
            }
        }
    }


    private fun calldeletePartnerWorkApi(item: MultipleViewItem) {
        val deletePartnerWork = deletePartnerWork(workId = item.itemId?.toInt().getSafe())
        profileViewModel.calldeletePartnerWork(deletePartnerWork).observe(this) {
            if (isOnline(activity)) {
                when (it) {
                    is ResponseResult.Success -> {
                        hideLoader()
                        try {

                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message = mBinding.langData?.messages?.work_delete_msg.getSafe(),
                                    buttonCallback = {
                                        profileViewModel.selectedWorkExperience.remove(item)
                                        mBinding.caWorkExp.listItems =
                                            profileViewModel.selectedWorkExperience
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
                        message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                        buttonCallback = {},
                    )
            }
        }
    }
}