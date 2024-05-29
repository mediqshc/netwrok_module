package com.homemedics.app.ui.fragment.linkedaccounts

import android.graphics.Color
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.linkaccount.CompanyRequest
import com.fatron.network_module.models.request.linkaccount.DeleteLinkRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.linkaccount.*
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.ConnectionDetailsFragmentBinding
import com.homemedics.app.ui.adapter.CityCountryAdapter
import com.homemedics.app.ui.adapter.CreditAdapter
import com.homemedics.app.ui.adapter.CreditDiscountAdapter
import com.homemedics.app.ui.adapter.DependentAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LinkedAccountDetailFragment : BaseFragment() {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: ConnectionDetailsFragmentBinding
    private lateinit var creditAdapter: CreditAdapter
    private lateinit var dependentAdapter: DependentAdapter
    private lateinit var discountAdapter: CreditDiscountAdapter
    private lateinit var cityCountryAdapter: CityCountryAdapter
    private var isFrom: Int = 0

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = profileViewModel.linkAccountItem?.title.getSafe()
        }
    }

    override fun init() {
        isFrom = arguments?.getInt("isFrom").getSafe()
        creditAdapter = CreditAdapter()
        dependentAdapter = DependentAdapter()
        discountAdapter = CreditDiscountAdapter()
        cityCountryAdapter = CityCountryAdapter()
        mBinding.apply {
            iCreditFund.rvDetailItem.adapter = creditAdapter
            iDiscounts.rvDetailItem.adapter = discountAdapter
            rvTermDetailItem.adapter = dependentAdapter
            rvCountryCity.adapter = cityCountryAdapter
        }

        setObserver()
        creditVisible()
        if (profileViewModel.isService.not() && profileViewModel.isDiscountClick.not() && profileViewModel.isTermsConditionsClick.not())
            getCompaniesServices()

        if (mBinding.clTerms.isVisible)
            profileViewModel.companyTermsAndCondition?.let { setTermsAndConditionData(it) }
    }

    override fun onResume() {
        super.onResume()
        if (profileViewModel.isDiscountClick) {
            mBinding.apply {
                iCreditFund.llDetail.gone()
                iDiscounts.apply {
                    llDetail.visible()
                    tvDetail.text = mBinding.langData?.linkedAccountScreen?.discountDesc
                    tvAmount.gone()
                }
                clTerms.gone()
                tvCreditFund.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                tvTermsCondition.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                tvDiscounts.setBackgroundColor(Color.parseColor("#F4D0D0"))
                tvDetailConditionText.gone()
            }
        }
        if (profileViewModel.isTermsConditionsClick) {
            mBinding.apply {
                clTerms.visible()
                iDiscounts.llDetail.gone()
                iCreditFund.llDetail.gone()
                tvTermsCondition.setBackgroundColor(Color.parseColor("#F4D0D0"))
                tvCreditFund.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                tvDiscounts.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
//                tvDetailConditionText.setVisible(profileViewModel.companyTermsAndCondition?.termsAndConditions?.termsAndConditions.isNullOrEmpty().not())
            }
        }

        mBinding.tvDetailConditionText.setVisible(profileViewModel.companyTermsAndCondition?.termsAndConditions?.termsAndConditions.isNullOrEmpty().not())
    }

    override fun onDestroy() {
        super.onDestroy()
        profileViewModel.linkAccountItem = null
        profileViewModel.isTermsConditionsClick = false
        profileViewModel.isDiscountClick = false
        profileViewModel.isService = false
        profileViewModel.isDiscountClick = false
        profileViewModel.isTermsConditionsClick = false
    }

    override fun getFragmentLayout() = R.layout.connection_details_fragment

    override fun getViewModel() {

    }

    private fun setObserver() {
        profileViewModel.companiesServicesListLiveData.observe(this) { companyServices ->
            companyServices?.let { services ->
                creditAdapter.listItems = services
                if (companyServices.isEmpty()) {
                    mBinding.apply {
                        tvCreditFund.gone()
                        iCreditFund.llDetail.gone()
                    }
                }
            }
        }

        profileViewModel.companiesDiscountsListLiveData.observe(this) { companyDiscounts ->
            companyDiscounts?.let { discounts ->
                discountAdapter.listItems = discounts
                if (discounts.isEmpty()) {
                    mBinding.apply {
                        tvDiscounts.gone()
                        iDiscounts.llDetail.gone()
                    }
                }
            }
        }
    }

    private fun creditVisible() {
        mBinding.iCreditFund.apply {
            llDetail.visible()
            tvAmount.apply {
//                setVisible(profileViewModel.linkAccountItem?.redText?.contains("-")?.not().getSafe())
                visible()
                text = "${profileViewModel.linkAccountItem?.redText.getSafe()} ${mBinding.langData?.linkedAccountScreen?.availableCredit.getSafe()}"
            }
            tvDetail.text = mBinding.langData?.linkedAccountScreen?.creditDesc.getSafe()
//            tvAmount.visible()
        }
    }

    override fun getViewBinding() {
        mBinding = binding as ConnectionDetailsFragmentBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                    title= if (isFrom == 1) langData?.dialogsStrings?.unlinkCompanyTitle.getSafe() else langData?.dialogsStrings?.unlinkInsuranceTitle.getSafe(),
                    message = langData?.dialogsStrings?.unlinkCompanyDesc?.replace("[0]", profileViewModel.linkAccountItem?.title.getSafe()).getSafe(),
                    positiveButtonStringText = langData?.globalString?.ok.getSafe(),
                    negativeButtonStringText = langData?.globalString?.cancel.getSafe(),
                    negativeButtonCallback = {},
                    buttonCallback = {
                        //unlink
                        if (isFrom == 1) { //companies
                            val realItem =
                                profileViewModel.companiesList.value?.find { profileViewModel.linkAccountItem?.itemId == it.id.toString() }
                            realItem?.let {
                                deleteCompanyLinkApi(realItem)
                            }
                        } else { //insurance
                            val realItem =
                                profileViewModel.insuranceCompanyList.value?.find { profileViewModel.linkAccountItem?.itemId == it.id.toString() }
                            realItem?.let {
                                deleteInsuranceCompLinkApi(realItem)
                            }
                        }
                    }
                )
            }
            tvDetailConditionText.setOnClickListener {
                profileViewModel.isTermsConditionsClick = true
                profileViewModel.isDiscountClick = false
                findNavController().safeNavigate(LinkedAccountDetailFragmentDirections.actionLinkedAccountDetailFragmentToDetailTermsFragment())
            }
            tvCreditFund.setOnClickListener {
                if (mBinding.iCreditFund.llDetail.isVisible.not())
                    getCompaniesServices()
                creditVisible()
                iDiscounts.llDetail.gone()
                tvCreditFund.setBackgroundColor(Color.parseColor("#F4D0D0"))
                tvDiscounts.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                tvTermsCondition.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                clTerms.gone()
                tvDetailConditionText.gone()

            }
            tvDiscounts.setOnClickListener {
                if (mBinding.iDiscounts.llDetail.isVisible.not()) {
                    getCompaniesDiscounts()
                }
                iCreditFund.llDetail.gone()
                iDiscounts.apply {
                    llDetail.visible()
                    tvDetail.text = mBinding.langData?.linkedAccountScreen?.discountDesc
                    tvAmount.gone()
                }

                clTerms.gone()
                tvCreditFund.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                tvTermsCondition.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                tvDiscounts.setBackgroundColor(Color.parseColor("#F4D0D0"))
                tvDetailConditionText.gone()

            }
            tvTermsCondition.setOnClickListener {
                if (clTerms.isVisible.not()) {
                    getCompaniesTermsAndConditions()
                }
                clTerms.visible()
                iDiscounts.llDetail.gone()
                iCreditFund.llDetail.gone()
                tvTermsCondition.setBackgroundColor(Color.parseColor("#F4D0D0"))
                tvCreditFund.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                tvDiscounts.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                tvDetailConditionText.setVisible(profileViewModel.companyTermsAndCondition?.termsAndConditions?.termsAndConditions.isNullOrEmpty().not())

            }

            creditAdapter.itemClickListener = { item, _ ->
                profileViewModel.apply {
                    isService = true
                    isDiscountClick = false
                    isTermsConditionsClick = false
                    companyServices = item
                    specialitiesExclusion = item.exclusions?.specialities.getSafe()
                    doctors = item.exclusions?.doctors.getSafe()
                    excludedItems = item.exclusions?.excludedItems.getSafe()
                    excludedCategories = item.exclusions?.excludedCategories.getSafe()
                }
                lifecycleScope.launch {
                    delay(200)
                    findNavController().safeNavigate(
                        LinkedAccountDetailFragmentDirections.actionLinkedAccountDetailFragmentToCompanyCreditsDetailsFragment()
                    )
                }
            }

            discountAdapter.itemClickListener = { item, _ ->
                profileViewModel.apply {
                    isDiscountClick = true
                    isTermsConditionsClick = false
                    isService = false
                    companyDiscount = item
                    specialitiesExclusion = item.exclusions?.specialities.getSafe()
                    doctors = item.exclusions?.doctors.getSafe()
                    excludedItems = item.exclusions?.excludedItems.getSafe()
                    excludedCategories = item.exclusions?.excludedCategories.getSafe()
                }
                lifecycleScope.launch {
                    delay(200)
                    findNavController().safeNavigate(
                        LinkedAccountDetailFragmentDirections.actionLinkedAccountDetailFragmentToCompanyDiscountFragment()
                    )
                }
            }
        }
    }

    private fun setTermsAndConditionData(termsAndCondition: CompanyTermsAndConditionsResponse) {
        val endDate = getDateInFormat(
            termsAndCondition.termsAndConditions?.endDate.getSafe(),
            "yyyy-MM-dd",
            "dd/MM/yyyy"
        )
        val cityCountryList = arrayListOf<CityCountry>()
        val countries = termsAndCondition.termsAndConditions?.promotionCountry?.getSafe()
        val cities = termsAndCondition.termsAndConditions?.promotionCity?.getSafe()
        cityCountryList.clear()
        countries?.forEach { country ->
            val countryItem = metaData?.countries?.find { it.id == country.countryId }?.name.getSafe()
            val apiCities = cities?.filter { it.city?.countryId == country.countryId }
            val finalCities = metaData?.cities?.filter { apiCities?.map { it.cityId }?.contains(it.id).getSafe() }
            val sb = StringBuilder()
            finalCities?.forEachIndexed { pos, item ->
                sb.append(item.name)
                if(pos == finalCities.size -2)
                    sb.append(" ${mBinding.langData?.globalString?.and.getSafe()} ")
                else if (item == finalCities.last())
                    sb.append(".")
                else
                    sb.append(", ")
            }
            cityCountryList.add(
                CityCountry(country = countryItem, city = sb.toString())
            )
        }

        cityCountryAdapter.listItems = cityCountryList

        mBinding.apply {
            tvCompanyValidity.text = langData?.linkedAccountScreen?.companyValidity?.replace("[#]", endDate)
            tvTermsText.setVisible(cityCountryList.isNotEmpty())
            view13.setVisible(cityCountryList.isNotEmpty())
        }

        val dependentList = arrayListOf<DependentResponse>()
        dependentList.clear()
        termsAndCondition.termsAndConditions?.promotionAges?.forEach { age ->
            val ageLimits = if (age.minAge == 0 && age.maxAge == 0)
                mBinding.langData?.globalString?.noAgeLimit.getSafe()
            else if(age.minAge == 0 && age.maxAge.getSafe() > 0)
                "${mBinding.langData?.globalString?.age.getSafe()} ${age.maxAge} ${mBinding.langData?.globalString?.below.getSafe()}"
            else if(age.minAge.getSafe() > 0 && age.maxAge.getSafe() == 0)
                "${mBinding.langData?.globalString?.age.getSafe()} ${age.minAge} ${mBinding.langData?.globalString?.above.getSafe()}"
            else "${mBinding.langData?.globalString?.age.getSafe()} ${age.minAge} ${mBinding.langData?.globalString?.to.getSafe()} ${age.maxAge}"

            var gender: String? = null
            if (age.genderId.getSafe() > 0) gender = metaData?.genders?.find { it.genericItemId == age.genderId }?.genericItemName

            var name = metaData?.familyMemberRelations?.find { it.genericItemId == age.familyMemberRelationId }?.genericItemName.getSafe()
            if (name.isEmpty()) name = mBinding.langData?.globalString?.self.getSafe()

            val relation = if (gender != null) "$gender $name" else name

            dependentList.add(
                DependentResponse(age.id, relation, ageLimits)
            )
        }
        dependentAdapter.listItems = dependentList
    }

    private fun deleteCompanyLinkApi(item: CompanyResponse){
        val request = DeleteLinkRequest(companyId = item.companyId.toString())

        if (isOnline(requireActivity())) {
            profileViewModel.deleteCompanyLink(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        profileViewModel.companiesList.value?.removeIf { it.companyId == item.companyId }
                        profileViewModel.companiesList.postValue(profileViewModel.companiesList.value)
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
                                message =  getErrorMessage( it.generalResponse.message.getSafe()).getSafe()  ,
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
                    title =  mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun deleteInsuranceCompLinkApi(item: CompanyResponse){
        val request = DeleteLinkRequest(insuranceId = item.companyId.toString())

        if (isOnline(requireActivity())) {
            profileViewModel.deleteInsuranceComp(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        profileViewModel.insuranceCompanyList.value?.removeIf { it.companyId == item.companyId }
                        profileViewModel.insuranceCompanyList.postValue(profileViewModel.insuranceCompanyList.value)
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

    private fun getCompaniesServices() {
        val request = CompanyRequest(packageId = profileViewModel.linkAccountItem?.pakgeId)
        if (isOnline(requireActivity())) {
            profileViewModel.getCompaniesServices(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val data = it.data as ResponseGeneral<*>
                        val response = data.data as CompanyServicesResponse
                        response.let { companyServicesList ->
                            profileViewModel.companiesServicesListLiveData.postValue(
                                companyServicesList.services.getSafe()
                            )
                        }
//                        getCompaniesDiscounts()
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

    private fun getCompaniesDiscounts() {
        val request = CompanyRequest(packageId = profileViewModel.linkAccountItem?.pakgeId)
        if (isOnline(requireActivity())) {
            profileViewModel.getCompaniesDiscounts(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val data = it.data as ResponseGeneral<*>
                        val response = data.data as CompanyDiscountsResponse
                        response.let { companyServicesList ->
                            profileViewModel.companiesDiscountsListLiveData.postValue(
                                companyServicesList.discounts.getSafe()
                            )
                        }
//                        getCompaniesTermsAndConditions()
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

    private fun getCompaniesTermsAndConditions() {
        val request = CompanyRequest(packageId = profileViewModel.linkAccountItem?.pakgeId)
        if (isOnline(requireActivity())) {
            profileViewModel.getCompaniesTermsAndConditions(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val data = it.data as ResponseGeneral<*>
                        val response = data.data as CompanyTermsAndConditionsResponse
                        profileViewModel.companyTermsAndCondition = response
                        setTermsAndConditionData(response)
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
}