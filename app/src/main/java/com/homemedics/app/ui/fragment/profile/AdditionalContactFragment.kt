package com.homemedics.app.ui.fragment.profile

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAdditionalContactBinding
import com.homemedics.app.model.ContactItem
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.isValid
import com.homemedics.app.utils.isValidPhoneLength
import com.homemedics.app.utils.setVisible
import com.homemedics.app.viewmodel.ProfileViewModel

class AdditionalContactFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentAdditionalContactBinding
    private val viewModel: ProfileViewModel by activityViewModels()
    private val contactDetail = ContactItem()
    private var phoneLength = 10
    private var startsWith = 0
    val langData=ApplicationClass.mGlobalData
    override fun setLanguageData() {
        mBinding.apply {
            bAdd.text =langData?.globalString?.add
            customActionbar.title =langData?.personalprofileBasicScreen?.additionalContact.getSafe()
            cdCategorys.hint =langData?.globalString?.category.getSafe()
            etOther.hint =langData?.globalString?.other.getSafe()
            cdCountryCode.hint =langData?.globalString?.countryCode.getSafe()
            etMobileNumber.hint =langData?.globalString?.mobileNumber.getSafe()
        }

    }
    override fun getFragmentLayout(): Int = R.layout.fragment_additional_contact

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAdditionalContactBinding
        mBinding.lifecycleOwner=this
        mBinding.contactItem = contactDetail
    }

    override fun setListeners() {
        mBinding.apply {
            customActionbar.onAction2Click = {
                requireActivity().onBackPressed()
            }
            bAdd.setOnClickListener {
                addContact()
            }
            etMobileNumber.mBinding.editText.addTextChangedListener {
                bAdd.isEnabled = cdCategorys.mBinding.dropdownMenu.text.isNotEmpty() && etMobileNumber.text.isNotEmpty()
            }
        }
    }

    private fun addContact(){
        if(contactDetail.mobileNumber.startsWith("0"))
            contactDetail.mobileNumber = contactDetail.mobileNumber.substring(1)

        val mobileNumber = contactDetail.mobileNumber

        if(isValid(mobileNumber).not()){
            mBinding.etMobileNumber.errorText = langData?.fieldValidationStrings?.mobileNumberValidation
            mBinding.etMobileNumber.requestFocus()
            return
        }

        if (isValidPhoneLength(mobileNumber, phoneLength).not()) {
            mBinding.etMobileNumber.errorText = langData?.fieldValidationStrings?.mobileNumberValidation
            mBinding.etMobileNumber.requestFocus()
            return
        }

        if ( ( contactDetail.category.contains(langData?.globalString?.other.getSafe())||contactDetail.category.uppercase().contains("Other".uppercase()) ) &&  contactDetail.other.isNullOrEmpty()) {
            mBinding.etOther.errorText =langData?.fieldValidationStrings?.otherEmpty
            mBinding.etOther.requestFocus()
            return
        }

        val numberInitials = ""
        val numberLimit = 0
        mBinding.etMobileNumber.errorText = if(mobileNumber.startsWith("0") && mobileNumber.length == 1 && mobileNumber.length <= numberLimit)
            null
        else if(mobileNumber.startsWith(numberInitials) && mobileNumber.length <= numberLimit)
            null
        else if(mobileNumber.startsWith("0$numberInitials") && mobileNumber.length <= numberLimit+1)
            null
        else if (mobileNumber.startsWith(startsWith.toString()) && mobileNumber.length == phoneLength)
            null
        else {
            langData?.fieldValidationStrings?.mobileNumberValidation
            mBinding.etMobileNumber.requestFocus()
            return
        }

        if(mBinding.etMobileNumber.text.isNotEmpty()){
            var category=contactDetail.category
            if (category.uppercase().getSafe().contains("other".uppercase().getSafe()))
                category=contactDetail.other
            contactDetail.title = category
            contactDetail.desc =  getCountryCode(contactDetail.countryCode ) + contactDetail.mobileNumber
            contactDetail.drawable = R.drawable.ic_call_black
            contactDetail.countryCode= getCountryCode(contactDetail.countryCode )
            viewModel.contacts.add(contactDetail)
            findNavController().popBackStack()
        }
    }

    private fun setPhoneLength(index:Int){
        mBinding.etMobileNumber.errorText = null
        phoneLength=metaData?.countries?.get(index = index)?.phoneNoLimit.getSafe()
        startsWith = metaData?.countries?.get(index = index)?.phoneNoInitial.getSafe()
        mBinding.etMobileNumber.numberLimit = phoneLength
    }

    override fun init() {
        mBinding.etMobileNumber.numberInitials = "3"
        val categoryList = metaData?.phoneCategories?.map { it.genericItemName } as ArrayList<String>
        mBinding.cdCategorys.data = categoryList
//        mBinding.cdCategorys.selectionIndex = 0
        contactDetail.category = categoryList[0]
        contactDetail.categoryId =metaData?.phoneCategories?.get(0)?.genericItemId.getSafe()
        val countryCodeList = getCountryCodeList()
        mBinding.cdCountryCode.data = countryCodeList.getSafe()
        var selectedIndex = 0
        val index = metaData?.countries?.indexOfFirst { it.isDefault == 1 }
        if (index != -1)
            selectedIndex = index.getSafe()
        mBinding.cdCountryCode.selectionIndex = selectedIndex

        setPhoneLength(mBinding.cdCountryCode.selectionIndex)
        mBinding.cdCountryCode.onItemSelectedListener = { _, position: Int ->
            contactDetail.countryCode = countryCodeList?.get(position).getSafe()
            setPhoneLength(position)
        }
        contactDetail.countryCode = countryCodeList?.get(mBinding.cdCountryCode.selectionIndex).getSafe()
        mBinding.cdCategorys.onItemSelectedListener = { category: String, position: Int ->
            mBinding.etOther.setVisible(category .contains(getString(R.string.other)) ||category.contains(langData?.globalString?.other.getSafe()) )
            contactDetail.categoryId= metaData?.phoneCategories?.get(position)?.genericItemId.getSafe()

            mBinding.bAdd.isEnabled = mBinding.cdCategorys.mBinding.dropdownMenu.text.isNotEmpty() && mBinding.etMobileNumber.text.isNotEmpty()
        }
    }

    override fun onClick(view: View?) {

    }
}