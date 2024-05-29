package com.homemedics.app.ui.fragment.linkedaccounts

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCompanyCreditsDetailsBinding
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.Constants
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.gone
import com.homemedics.app.utils.visible
import com.homemedics.app.viewmodel.ProfileViewModel
import java.lang.StringBuilder

class CompanyCreditsDetailsFragment : BaseFragment() {

    private lateinit var mBinding: FragmentCompanyCreditsDetailsBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            tvCategoriesHeading.text = "${langData?.globalString?.exclusionCategories.getSafe()} ${Constants.COLON}"
        }
    }

    override fun init() {
        setDataInViews()
        if (
            CustomServiceTypeView.ServiceType.Claim.id == profileViewModel.companyServices?.partnerServiceId ||
            CustomServiceTypeView.ServiceType.OtherWalkIn.id == profileViewModel.companyServices?.partnerServiceId
        ) {
            mBinding.apply {
                tvCategoriesHeading.visible()
                tvCategoriesValues.visible()
                svView.visible()
                viewPager.gone()
                vShadow.gone()
                tabLayout.gone()
                val sb = StringBuilder()
                if (profileViewModel.companyServices?.exclusions?.categories?.isNotEmpty().getSafe()) {
                    profileViewModel.companyServices?.exclusions?.categories?.forEachIndexed { _, list ->
                        if (list[1] != null)
                            sb.append("${list[0]} : ${list[1]} \n")
                        else
                            sb.append("${list[0]} : ${mBinding.langData?.labPharmacyScreen?.na.getSafe()} \n")
                    }
                    tvCategoriesValues.text = sb.toString()
                } else {
                    tvCategoriesValues.text = langData?.labPharmacyScreen?.na.getSafe()
                }
            }
        } else {
            mBinding.apply {
                tvCategoriesHeading.gone()
                tvCategoriesValues.gone()
                svView.gone()
                viewPager.visible()
                vShadow.visible()
                tabLayout.visible()
            }
            initTabsAndViewPager()
        }

    }

    override fun getFragmentLayout() = R.layout.fragment_company_credits_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCompanyCreditsDetailsBinding
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
                        CustomServiceTypeView.ServiceType.VideoCall.id == profileViewModel.companyServices?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.Message.id == profileViewModel.companyServices?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.HomeVisit.id == profileViewModel.companyServices?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.Clinic.id == profileViewModel.companyServices?.partnerServiceId
                    )
                        langData?.globalString?.specialities.getSafe()
                    else
                        langData?.linkedAccountScreen?.categories.getSafe()
                    1 -> if (
                        CustomServiceTypeView.ServiceType.VideoCall.id == profileViewModel.companyServices?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.Message.id == profileViewModel.companyServices?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.HomeVisit.id == profileViewModel.companyServices?.partnerServiceId ||
                        CustomServiceTypeView.ServiceType.Clinic.id == profileViewModel.companyServices?.partnerServiceId
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
        val serviceCredits = profileViewModel.companyServices
        mBinding.apply {
            val none = langData?.labPharmacyScreen?.na.getSafe()
            actionbar.title = serviceCredits?.name.getSafe()
            tvSubLimit.text = if (serviceCredits?.subLimit != null && serviceCredits.subLimit != 0.0) "${Constants.START} ${langData?.globalString?.sublimit.getSafe()} ${Constants.COLON} ${serviceCredits.subLimit.getSafe()}% ${Constants.END}" else "${langData?.globalString?.sublimit.getSafe()} $none"
        }
    }
}