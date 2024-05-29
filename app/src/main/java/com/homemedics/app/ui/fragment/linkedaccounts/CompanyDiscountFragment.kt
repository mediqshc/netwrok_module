package com.homemedics.app.ui.fragment.linkedaccounts

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCompanyDiscountBinding
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.Constants
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.setVisible
import com.homemedics.app.viewmodel.ProfileViewModel

class CompanyDiscountFragment : BaseFragment() {

    private lateinit var mBinding: FragmentCompanyDiscountBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            tvMaxCap.text = "${langData?.linkedAccountScreen?.maxCap.getSafe()} ${Constants.COLON} "
            tvMinInvoice.text = "${langData?.linkedAccountScreen?.minInvoice.getSafe()} ${Constants.COLON} "
            tvNumberOfTransactions.text = "${langData?.linkedAccountScreen?.numberOfTransactions} ${Constants.COLON} "
        }
    }

    override fun init() {
        setDataInViews()
        initTabsAndViewPager()
    }

    override fun getFragmentLayout() = R.layout.fragment_company_discount

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding =  binding as FragmentCompanyDiscountBinding
    }

    override fun setListeners() {
        mBinding.actionbar.onAction1Click = {
            findNavController().popBackStack()
        }
    }

    private fun initTabsAndViewPager() {
        mBinding.apply {
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(CategoriesFragment())
                add(ExcludedProductsFragment())
            }
            viewPager.apply {
                adapter = TabsPagerAdapter(childFragmentManager, lifecycle)
            }
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> if (
                        CustomServiceTypeView.ServiceType.VideoCall.id == profileViewModel.companyDiscount?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.Message.id == profileViewModel.companyDiscount?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.HomeVisit.id == profileViewModel.companyDiscount?.partnerServiceId
                    )
                        langData?.globalString?.specialities.getSafe()
                    else
                        langData?.linkedAccountScreen?.categories.getSafe()
                    1 -> if (
                        CustomServiceTypeView.ServiceType.VideoCall.id == profileViewModel.companyDiscount?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.Message.id == profileViewModel.companyDiscount?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.HomeVisit.id == profileViewModel.companyDiscount?.partnerServiceId
                    )
                        langData?.globalString?.doctors.getSafe()
                    else
                        langData?.linkedAccountScreen?.excludedProducts.getSafe()
                    else -> ""
                }
            }.attach()
        }
    }

    private fun setDataInViews() {
        val discount = profileViewModel.companyDiscount
        val currency = metaData?.currencies?.find { it.genericItemId == profileViewModel.companyDiscount?.currencyId }?.genericItemName
        mBinding.apply {
            val none = langData?.labPharmacyScreen?.na.getSafe()
            actionbar.title = "${discount?.name.getSafe()} ${langData?.globalString?.discount.getSafe()}"
            tvDescription.text = if (discount?.discountType?.contains("Free", true).getSafe())
                    langData?.linkedAccountScreen?.discountDescription?.replace("[0]", discount?.discountType.getSafe())
                else if (discount?.discountType?.contains("Percentage", true).getSafe())
                    langData?.linkedAccountScreen?.discountDescription?.replace("[0]", "${discount?.value?.toString().getSafe()}%")
                else langData?.linkedAccountScreen?.discountDescription?.replace("[0]", "$currency ${discount?.value?.toString().getSafe()}")
            tvPromoCode.apply {
                setVisible(discount?.promoCode != null)
                text = "${langData?.globalString?.promoCode.getSafe()} ${Constants.COLON} ${discount?.promoCode.getSafe()}"
            }
            tvMaxCapValue.text = if (discount?.maxCap != null && discount.maxCap != 0) "$currency ${discount.maxCap.getSafe()}" else none
            tvMinInvoiceValue.text = if (discount?.minInvoice != null && discount.minInvoice != 0) "$currency ${discount.minInvoice.getSafe()}" else none
            tvTransactionValue.text = if (discount?.numberOfTransactions != null) "${discount.numberOfTransactions?.toString()}" else none
            tvResetValue.text = if (discount?.resetAfter != null) "${discount.resetAfter?.toString()} ${langData?.globalString?.days.getSafe()}" else none
        }
    }
}