package com.homemedics.app.ui.fragment.profile

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.family.FamilyRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.repository.ResponseResult
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddConnectionBinding
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.AuthViewModel
import com.homemedics.app.viewmodel.ProfileViewModel


class AddConnectionFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentAddConnectionBinding
    private val request = FamilyRequest()
    private var phoneLength = 10
    private var startsWith = 0
    private val profileViewModel: ProfileViewModel by activityViewModels()

    val langData= ApplicationClass.mGlobalData
    override fun setLanguageData() {
mBinding.apply {
    bSave.text=langData?.globalString?.save
    customActionbar.title=langData?.personalprofileBasicScreen?.addConnection.getSafe()
    etMobileNumber.hint=langData?.globalString?.mobileNumber.getSafe()
    cdCountryCode.hint=langData?.globalString?.countryCode.getSafe()
    dob.hint=langData?.globalString?.dateOfBirth.getSafe()
    cdGender.hint=langData?.globalString?.gender.getSafe()
    cdRelation.hint=langData?.personalprofileBasicScreen?.relation.getSafe()
    etName.hint=langData?.globalString?.name.getSafe()
}
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_add_connection

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddConnectionBinding
        mBinding.familyRequest = request
    }

    override fun setListeners() {
        mBinding.apply {
            customActionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            etName.mBinding.editText.addTextChangedListener {
                bSave.isEnabled = etName.text.isNotEmpty() && cdRelation.mBinding.dropdownMenu.text.isNotEmpty() &&
                        cdGender.mBinding.dropdownMenu.text.isNotEmpty() && dob.text.isNotEmpty()
            }
            dob.mBinding.editText.addTextChangedListener {
                bSave.isEnabled = etName.text.isNotEmpty()   && cdRelation.mBinding.dropdownMenu.text.isNotEmpty() &&
                        cdGender.mBinding.dropdownMenu.text.isNotEmpty() && dob.text.isNotEmpty()
            }
            bSave.setOnClickListener {
                if(request.phoneNumber.startsWith("0"))
                    request.phoneNumber = request.phoneNumber.substring(1)

                val req = Gson().fromJson(Gson().toJson(request), FamilyRequest::class.java)
                req.dateOfBirth = getDateInFormat(request.dateOfBirth.getSafe(), "dd/MM/yyyy", "yyyy-MM-dd")
                req.countryCode = getCountryCode(request.countryCode.getSafe())
                if(isValidName(req.fullName).not()){
                    mBinding.etName.errorText = langData?.fieldValidationStrings?.nameValidation
                    mBinding.dob.requestFocus()
                    return@setOnClickListener
                }

                if (request.phoneNumber.isNotEmpty() && isValidPhoneLength(request.phoneNumber, phoneLength).not()) {
                    mBinding.etMobileNumber.errorText = langData?.fieldValidationStrings?.mobileNumberValidation
                    mBinding.etMobileNumber.requestFocus()
                    return@setOnClickListener
                }

                if(isValid(req.dateOfBirth).not()){
                    mBinding.dob.errorText = langData?.fieldValidationStrings?.dateOfBirthValidation
                    mBinding.dob.requestFocus()
                    return@setOnClickListener
                }

                val numberInitials = ""
                val numberLimit = 0
                val mobileNumber = request.phoneNumber
                mBinding.etMobileNumber.errorText = if(mobileNumber.startsWith("0") && mobileNumber.length == 1 && mobileNumber.length <= numberLimit) //accept 0 if only one digit and have valid limit
                    null
                else if(mobileNumber.startsWith(numberInitials) && mobileNumber.length <= numberLimit) //accept if start with valid initial and have valid limit
                    null
                else if(mobileNumber.startsWith("0$numberInitials") && mobileNumber.length <= numberLimit+1 /* to accept with zero */)
                    null
                else if (mobileNumber.startsWith(startsWith.toString()) && mobileNumber.length == phoneLength)
                    null
                else {
                    langData?.fieldValidationStrings?.mobileNumberValidation
                    mBinding.etMobileNumber.requestFocus()
                    return@setOnClickListener
                }
                createFamilyConnection(req)
            }

            dob.clickCallback = {
               dob.textColorCheck=true
                openCalender( mBinding.dob.mBinding.editText, canSelectToday = true)
            }
        }
    }

    override fun init() {
        mBinding.lifecycleOwner = this

        val genderList = metaData?.genders?.map { it.genericItemName }  as ArrayList<String>
        mBinding.cdGender.data = genderList
        mBinding.cdGender.onItemSelectedListener = { _, position: Int ->
            request.genderId = metaData?.genders?.get(position)?.genericItemId.getSafe().toString()

            mBinding.apply {
                bSave.isEnabled = etName.text.isNotEmpty() && cdRelation.mBinding.dropdownMenu.text.isNotEmpty() &&
                        cdGender.mBinding.dropdownMenu.text.isNotEmpty() && dob.text.isNotEmpty()
            }
        }
//        mBinding.cdGender.selectionIndex = 0
        request.genderId = metaData?.genders?.get(0)?.genericItemId.getSafe().toString()

        val countryCodeList = getCountryCodeList()
        mBinding.cdCountryCode.data = countryCodeList.getSafe()

        var selectedIndex = 0
        val index = metaData?.countries?.indexOfFirst { it.isDefault == 1 }
        if (index != -1)
            selectedIndex = index.getSafe()
        mBinding.cdCountryCode.selectionIndex = selectedIndex

        request.countryCode = countryCodeList?.get(mBinding.cdCountryCode.selectionIndex)
        setPhoneLength(mBinding.cdCountryCode.selectionIndex)
        mBinding.cdCountryCode.onItemSelectedListener = { _, position: Int ->
            setPhoneLength(position)
        }

        val familyRelationsList =
            metaData?.familyMemberRelations?.map { it.genericItemName } as ArrayList<String>
        mBinding.cdRelation.data = familyRelationsList
        mBinding.cdRelation.onItemSelectedListener = { _, position: Int ->
            request.relationId =
                metaData?.familyMemberRelations?.get(position)?.genericItemId.getSafe().toString()

            mBinding.apply {
                bSave.isEnabled = etName.text.isNotEmpty() && cdRelation.mBinding.dropdownMenu.text.isNotEmpty() &&
                        cdGender.mBinding.dropdownMenu.text.isNotEmpty() && dob.text.isNotEmpty()
            }
        }
//        mBinding.cdRelation.selectionIndex = 0
        request.relationId =
            metaData?.familyMemberRelations?.get(0)?.genericItemId.getSafe().toString()
    }

    private fun setPhoneLength(index: Int) {
        mBinding.etMobileNumber.errorText = null
        phoneLength = metaData?.countries?.get(index = index)?.phoneNoLimit.getSafe()
        startsWith = metaData?.countries?.get(index = index)?.phoneNoInitial.getSafe()
        mBinding.etMobileNumber.numberLimit = phoneLength
    }

    override fun onClick(view: View?) {

    }

    private fun createFamilyConnection(request: FamilyRequest) {
        if (isOnline(requireActivity())) {
            profileViewModel.createFamilyConnectionApiCall(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<FamilyConnection>
                        val familyConnection = response.data
                        profileViewModel.moveFamilyTabsToIndex = if(request.phoneNumber.isEmpty()) 0 else 2
                        findNavController().popBackStack()
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
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
                    title =  langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}