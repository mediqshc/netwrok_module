package com.homemedics.app.ui.fragment.packages.package_details

import android.graphics.Color
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.auth.EconOtpRequest
import com.fatron.network_module.models.request.auth.EconSubscribeRequest
import com.fatron.network_module.models.request.linkaccount.CompanyRequest
import com.fatron.network_module.models.response.EconResponse
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.linkaccount.*
import com.fatron.network_module.models.response.packages.Package
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentPackageDetailsBinding
import com.homemedics.app.ui.adapter.CityCountryAdapter
import com.homemedics.app.ui.adapter.CreditAdapter
import com.homemedics.app.ui.adapter.CreditDiscountAdapter
import com.homemedics.app.ui.fragment.packages.PackagesViewModel
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PackageDetailsFragment : BaseFragment() {

    companion object {
        fun newInstance() = PackageDetailsFragment()
    }

    private val viewModel: PackageDetailsViewModel by viewModels()
    private val packagesViewModel: PackagesViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var creditAdapter: CreditAdapter
    private lateinit var discountAdapter: CreditDiscountAdapter
    private lateinit var cityCountryAdapter: CityCountryAdapter

    private lateinit var mBinding: FragmentPackageDetailsBinding

    private val langData = ApplicationClass.mGlobalData
    private var isFromHome = false

    override fun getViewBinding() {
        mBinding = binding as FragmentPackageDetailsBinding
    }


    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            mBinding.actionbar.title = "Package Details"
        }

    }

    override fun init() {
        isFromHome = arguments?.getBoolean("isFromHome").getSafe()
        creditAdapter = CreditAdapter()
        discountAdapter = CreditDiscountAdapter()
        cityCountryAdapter = CityCountryAdapter()
        mBinding.apply {
            iCreditFund.rvDetailItem.adapter = creditAdapter
            iDiscounts.rvDetailItem.adapter = discountAdapter
            rvCountryCity.adapter = cityCountryAdapter
        }
        showBanner()
        setObserver()
        creditVisible()
        //getCompaniesServices()
        showDiscounts()

        if (mBinding.clTerms.isVisible)
            profileViewModel.companyTermsAndCondition?.let { setTermsAndConditionData(it) }
    }

    private fun showDiscounts() {

        mBinding.apply {
            tvCreditFund.gone()
            iCreditFund.llDetail.gone()
        }


        mBinding.apply {
            if (mBinding.iDiscounts.llDetail.isVisible.not()) {
                getCompaniesDiscounts()
            }
            //iCreditFund.llDetail.gone()
            iDiscounts.apply {
                llDetail.visible()
                tvDetail.text = mBinding.langData?.linkedAccountScreen?.discountDesc
                tvAmount.gone()
            }

            clTerms.gone()
            // tvCreditFund.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            tvTermsCondition.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            tvDiscounts.setBackgroundColor(Color.parseColor("#F4D0D0"))
            tvDetailConditionText.gone()
        }

    }

    private fun showBanner() {
        mBinding.apply {
            packagesViewModel.selectedPackage.value?.let { item ->
                item?.promotion_media?.file?.let { url ->
                    banner.ivBanner.loadImage(url)
                }
                item?.valid_days?.let { days ->
                    banner.tvDays.text = days.getSafe().toString()
                }
                banner.tvName.text = item.name.getSafe()
                banner.tvDescription.text = item.description.getSafe()
                banner.tvPrice.text = "PKR " + item.amount.getSafe().toString()
                //set subscribe buttom text
                bSubscribe.text = "Subscribe Now for Rs." + item.amount.getSafe().toString()
            }

        }

    }

    override fun getFragmentLayout(): Int = R.layout.fragment_package_details

    override fun getViewModel() {
    }

    private fun creditVisible() {
        mBinding.iCreditFund.apply {
            llDetail.visible()
            tvAmount.apply {
                visible()
                text =
                    "${profileViewModel.linkAccountItem?.redText.getSafe()} ${mBinding.langData?.linkedAccountScreen?.availableCredit.getSafe()}"
            }
            tvDetail.text = mBinding.langData?.linkedAccountScreen?.creditDesc.getSafe()

        }
    }

    private fun setObserver() {
        profileViewModel.companiesServicesListLiveData.observe(this) { companyServices ->
//            companyServices?.let { services ->
//                creditAdapter.listItems = services
//                if (companyServices.isEmpty()) {
//                    mBinding.apply {
//                        tvCreditFund.gone()
//                        iCreditFund.llDetail.gone()
//                    }
//                }
//            }
            //TODO hide for the time being
            mBinding.apply {
                tvCreditFund.gone()
                iCreditFund.llDetail.gone()
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

        mBinding.tvDetailConditionText.setVisible(
            profileViewModel.companyTermsAndCondition?.termsAndConditions?.termsAndConditions.isNullOrEmpty()
                .not()
        )
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


    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            tvDetailConditionText.setOnClickListener {
                profileViewModel.isTermsConditionsClick = true
                profileViewModel.isDiscountClick = false
                //   findNavController().safeNavigate(LinkedAccountDetailFragmentDirections.actionLinkedAccountDetailFragmentToDetailTermsFragment())
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
                tvDetailConditionText.setVisible(
                    profileViewModel.companyTermsAndCondition?.termsAndConditions?.termsAndConditions.isNullOrEmpty()
                        .not()
                )

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
//                    findNavController().safeNavigate(
//                        LinkedAccountDetailFragmentDirections.actionLinkedAccountDetailFragmentToCompanyCreditsDetailsFragment()
//                    )
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
//                    findNavController().safeNavigate(LinkedAccountDetailFragmentDirections.actionLinkedAccountDetailFragmentToCompanyDiscountFragment())
                }
            }

            bSubscribe.setOnClickListener {
                sendEconOtpApiCall()
            }

            bVerifyOtp.setOnClickListener {
                subscribeEconPackage(etOtp.text.toString())
            }
        }
    }

    private fun subscribeEconPackage(otp: String) {
        val request = EconSubscribeRequest(
            network = packagesViewModel.network.value.getSafe(),
            phone = packagesViewModel.phone.value.getSafe(),
            otp = otp,
            packageId = packagesViewModel.selectedPackage.value?.id.getSafe().toString()
        )

        if (isOnline(requireActivity())) {
            packagesViewModel.subscribeEconPackageApiCall(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<EconResponse>
                        if (response.data?.status == 0) {
                            //subscription failed
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = "successful",
                                    message = response.data?.result.getSafe(),
                                    buttonCallback = {
                                        // Clear the back stack up to the loginFragment destination
                                        if (isFromHome) {
                                            findNavController().popBackStack(
                                                R.id.homeFragment,
                                                false
                                            )
                                        } else {
                                            findNavController().popBackStack(
                                                R.id.loginFragment,
                                                false
                                            )
                                        }

                                        //findNavController().navigate(R.id.action_packageDetailsFragment_to_loginFragment)
                                    },
                                )

                        } else {
                            //subscription failed
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = langData?.globalString?.information.getSafe(),
                                    message = response.data?.result.getSafe(),
                                    buttonCallback = {
                                        findNavController().popBackStack()
                                    },
                                )

                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
        }
    }

    private fun sendEconOtpApiCall() {
        val request = EconOtpRequest(
            network = packagesViewModel.network.value.getSafe(),
            phone = packagesViewModel.phone.value.getSafe()
        )

        if (isOnline(requireActivity())) {
            packagesViewModel.sendOtpApiCall(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<EconResponse>
                        if (response.data?.status == 0) {
                            mBinding.apply {
                                llOtpView.visible()
                                bSubscribe.gone()
                            }
                        } else {
                            //otp sending failed
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = langData?.globalString?.information.getSafe(),
                                    message = response.data?.result.getSafe(),
                                    buttonCallback = {
                                        findNavController().popBackStack()
                                    },
                                )

                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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
            val countryItem =
                metaData?.countries?.find { it.id == country.countryId }?.name.getSafe()
            val apiCities = cities?.filter { it.city?.countryId == country.countryId }
            val finalCities =
                metaData?.cities?.filter {
                    apiCities?.map { it.cityId }?.contains(it.id).getSafe()
                }
            val sb = StringBuilder()
            finalCities?.forEachIndexed { pos, item ->
                sb.append(item.name)
                if (pos == finalCities.size - 2)
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
            tvCompanyValidity.text =
                langData?.linkedAccountScreen?.companyValidity?.replace("[#]", endDate)
            tvTermsText.setVisible(cityCountryList.isNotEmpty())
            view13.setVisible(cityCountryList.isNotEmpty())
        }

        val dependentList = arrayListOf<DependentResponse>()
        dependentList.clear()
        termsAndCondition.termsAndConditions?.promotionAges?.forEach { age ->
            val ageLimits = if (age.minAge == 0 && age.maxAge == 0)
                mBinding.langData?.globalString?.noAgeLimit.getSafe()
            else if (age.minAge == 0 && age.maxAge.getSafe() > 0)
                "${mBinding.langData?.globalString?.age.getSafe()} ${age.maxAge} ${mBinding.langData?.globalString?.below.getSafe()}"
            else if (age.minAge.getSafe() > 0 && age.maxAge.getSafe() == 0)
                "${mBinding.langData?.globalString?.age.getSafe()} ${age.minAge} ${mBinding.langData?.globalString?.above.getSafe()}"
            else "${mBinding.langData?.globalString?.age.getSafe()} ${age.minAge} ${mBinding.langData?.globalString?.to.getSafe()} ${age.maxAge}"

            var gender: String? = null
            if (age.genderId.getSafe() > 0) gender =
                metaData?.genders?.find { it.genericItemId == age.genderId }?.genericItemName

            var name =
                metaData?.familyMemberRelations?.find { it.genericItemId == age.familyMemberRelationId }?.genericItemName.getSafe()
            if (name.isEmpty()) name = mBinding.langData?.globalString?.self.getSafe()

            val relation = if (gender != null) "$gender $name" else name

            dependentList.add(
                DependentResponse(age.id, relation, ageLimits)
            )
        }
        //  dependentAdapter.listItems = dependentList
    }

    private fun getCompaniesServices() {
        //val request = CompanyRequest(packageId = profileViewModel.linkAccountItem?.pakgeId)
        val request = CompanyRequest(packageId = packagesViewModel.selectedPackage.value?.id)
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
                    else -> {
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

    private fun getCompaniesDiscounts() {
        //  val request = CompanyRequest(packageId = profileViewModel.linkAccountItem?.pakgeId)
        val request = CompanyRequest(packageId = packagesViewModel.selectedPackage.value?.id)
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
                    else -> {
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

    private fun getCompaniesTermsAndConditions() {
        //  val request = CompanyRequest(packageId = profileViewModel.linkAccountItem?.pakgeId)
        val request = CompanyRequest(packageId = packagesViewModel.selectedPackage.value?.id)
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
                    else -> {
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
}