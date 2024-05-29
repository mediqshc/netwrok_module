package com.homemedics.app.ui.fragment.partner_profile

import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentPartnerProfileBinding
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.utils.getSafe

class PartnerProfileFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentPartnerProfileBinding

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.partnerProfileScreen?.partnerProfile.getSafe()
        }
    }

    override fun init() {
        val profile = ApplicationClass.mGlobalData?.globalString?.profile.getSafe()
        val service = ApplicationClass.mGlobalData?.globalString?.services.getSafe()
        mBinding.apply {
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(ProfileFragment())
                add(ServiceFragment())
            }
            viewPager.adapter = TabsPagerAdapter(childFragmentManager, lifecycle)
            viewPager.isUserInputEnabled = false
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> profile
                    1 -> service
                    else -> {
                        ""
                    }
                }
            }.attach()
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_partner_profile

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPartnerProfileBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                closeKeypad()
                findNavController().popBackStack()
            }
        }
    }

    override fun onClick(v: View?) {

    }
}