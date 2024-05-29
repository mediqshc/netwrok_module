package com.homemedics.app.ui.fragment.linkedaccounts

import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.linkaccount.CompanyRequest
import com.fatron.network_module.models.request.linkaccount.DeleteLinkRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.linkaccount.CompanyResponse
import com.fatron.network_module.models.response.linkaccount.LinkedAccountsResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentLinkedAccountsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LinkedAccountsFragment : BaseFragment(), View.OnClickListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentLinkedAccountsBinding

    override fun onDetach() {
        super.onDetach()
        profileViewModel.flushLinkedAccountsLists()
    }

    override fun onDestroy() {
        super.onDestroy()
        profileViewModel.linkAccountItem = null
        profileViewModel.isTermsConditionsClick = false
        profileViewModel.isDiscountClick = false
    }

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.linkedAccountScreen?.linkedAccounts.getSafe()
        }
    }

    override fun init() {
        observe()

//        mBinding.apply {
//            val user = DataCenter.getUser()
//            if ((user.isDoctor() || user.isMedicalStaff()) && user?.applicationStatusId == Enums.ApplicationStatus.APPROVED.key)
//                caMyHospitals.setVisible(true)
//        }
        partnerMode()
        getLinkedAccounts()
    }

    override fun getFragmentLayout() = R.layout.fragment_linked_accounts

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentLinkedAccountsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            caMyCompanies.apply {
                onAddItemClick = {
                    findNavController().safeNavigate(
                        LinkedAccountsFragmentDirections.actionLinkedAccountsFragmentToLinkedCompaniesFragment()
                    )
                }
                onItemClick = { item, _ ->
                    profileViewModel.linkAccountItem = item
                    lifecycleScope.launch {
                        delay(200)
                        findNavController().safeNavigate(
                            R.id.action_linkedAccountsFragment_to_linkedAccountDetailFragment,
                            bundleOf("isFrom" to 1)
                        )
                    }
//                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
//                        title = langData?.dialogsStrings?.confirmDelete.getSafe(),
//                        message = langData?.dialogsStrings?.deleteDesc.getSafe(),
//                        positiveButtonStringText =langData?.globalString?.yes.getSafe(),
//                        negativeButtonStringText = langData?.globalString?.no.getSafe(),
//                        buttonCallback = { //1 for companies
//                            val realItem = profileViewModel.companiesList.value?.find { item.itemId == it.id.toString() }
//                            realItem?.let {
//                                deleteCompanyLinkApi(realItem)
//                            }
//                        }
//                    )
                }
            }
            caMyInsurance.apply {
                    onAddItemClick = {
                        findNavController().safeNavigate(
                            LinkedAccountsFragmentDirections.actionLinkedAccountsFragmentToLinkedInsurancesFragment()
                        )
                    }
                onItemClick = { item, _ ->
                    profileViewModel.linkAccountItem = item
                    lifecycleScope.launch {
                        delay(200)
                        findNavController().safeNavigate(
                            R.id.action_linkedAccountsFragment_to_linkedAccountDetailFragment,
                            bundleOf("isFrom" to 2)
                        )
                    }
//                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
//                        title = langData?.dialogsStrings?.confirmDelete.getSafe(),
//                        message = langData?.dialogsStrings?.deleteDesc.getSafe(),
//                        positiveButtonStringText =langData?.globalString?.yes.getSafe(),
//                        negativeButtonStringText = langData?.globalString?.no.getSafe(),
//                        buttonCallback = {  //2 for insurance
////                            val realItem = profileViewModel.insuranceCompanyList.value?.find { item.itemId == it.id.toString() }
////                            realItem?.let {
////                                deleteInsuranceCompLinkApi(realItem)
////                            }
//                        }
//                    )
                }
            }
            caMyHospitals.apply {
                onAddItemClick = {
                    findNavController().safeNavigate(
                        LinkedAccountsFragmentDirections.actionLinkedAccountsFragmentToAddHospitalFragment()
                    )
                }
                onDeleteClick = { item, _ ->
                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                        title = langData?.dialogsStrings?.confirmDelete.getSafe(),
                        message = langData?.dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText =langData?.globalString?.yes.getSafe(),
                        negativeButtonStringText = langData?.globalString?.no.getSafe(),
                        buttonCallback = {
                            val realItem = profileViewModel.hospitalsList.value?.find { item.itemId == it.id.toString() }
                            realItem?.let {
                                deleteHospitalLinkApi(realItem)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun observe() {
        profileViewModel.companiesList.observe(this){
            it?.let {
                mBinding.caMyCompanies.listItems = (it as ArrayList<MultipleViewItem>).map {
                    it.itemEndIcon = R.drawable.ic_expand_more
                    it.hasRoundLargeIcon = true
                    it
                } as ArrayList<MultipleViewItem>
            }
        }
        profileViewModel.insuranceCompanyList.observe(this){
            it?.let {
                mBinding.caMyInsurance.listItems = (it as ArrayList<MultipleViewItem>).map {
//                    val item = MultipleViewItem(it.itemId, it.title, it.desc, it.imageUrl)
                    it.itemEndIcon = R.drawable.ic_expand_more
                    it.hasRoundLargeIcon = true
                    it
                } as ArrayList<MultipleViewItem>
            }
        }
        profileViewModel.hospitalsList.observe(this){
            it?.let {
                mBinding.caMyHospitals.listItems = (it as ArrayList<MultipleViewItem>).map {
                    val item = MultipleViewItem(it.itemId, it.title, it.desc, it.imageUrl)
                    item.hasRoundLargeIcon = true
                    item
                } as ArrayList<MultipleViewItem>
            }
        }
    }

    private fun partnerMode() {
        mBinding.apply {
            if (arguments != null) {
                val isPartnerMode = arguments?.getBoolean(Enums.PlannerMode.PLANNER_MODE.key)
                if (isPartnerMode == true) {
                    mBinding.apply {
                        caMyInsurance.setVisible(false)
                        caMyCompanies.setVisible(false)
                        caMyHospitals.setVisible(true)
                    }
                }
            } else {
                caMyHospitals.setVisible(false)
                caMyInsurance.setVisible(true)
                caMyCompanies.setVisible(true)
            }
        }
    }

    private fun deleteHospitalLinkApi(item: CompanyResponse){
        val request = DeleteLinkRequest(healthcareId = item.healthcareId.toString())

        if (isOnline(requireActivity())) {
            profileViewModel.deleteHospital(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        profileViewModel.hospitalsList.value?.removeIf { it.healthcareId == item.healthcareId }
                        profileViewModel.hospitalsList.postValue(profileViewModel.hospitalsList.value)
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
                    title =  mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getLinkedAccounts(){
        val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        val request = CompanyRequest(homePage = 0)
        if (isOnline(requireActivity())) {
            profileViewModel.getLinkedAccounts(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<LinkedAccountsResponse>
                        profileViewModel.companiesList.postValue(
                            (response.data?.companies as ArrayList<CompanyResponse>?)?.map { item ->
                                val currency = metaData?.currencies?.find { it.genericItemId == item.currencyId }?.genericItemName
                                item.pakgeId = item.packageId
                                item.charges = item.amount?.getSafe()?.toInt()
                                item.desc = "${mBinding.langData?.linkedAccountScreen?.availableCredits} "
                                item.redText = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.amount?.getSafe().round(2)} $currency" else "$currency ${item.amount?.getSafe().round(2)}"
                                item.isRed = true
                                item
                            } as ArrayList<CompanyResponse>?
                        )
                        profileViewModel.insuranceCompanyList.postValue(
                            (response.data?.insurances as ArrayList<CompanyResponse>?)?.map { item ->
                                val currency = metaData?.currencies?.find { it.genericItemId == item.currencyId }?.genericItemName
                                item.pakgeId = item.packageId
                                item.charges = item.amount?.getSafe()?.toInt()
                                item.desc = "${mBinding.langData?.linkedAccountScreen?.availableCredits} "
                                item.redText = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.amount?.getSafe().round(2)} $currency" else "$currency ${item.amount?.getSafe().round(2)}"
                                item.isRed = true
                                item
                            } as ArrayList<CompanyResponse>?
                        )
                        profileViewModel.hospitalsList.postValue(
                            (response.data?.healthcares as ArrayList<CompanyResponse>?)
                        )
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {

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
                    title =  mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}