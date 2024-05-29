package com.homemedics.app.ui.fragment.linkedaccounts

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentTermsnconditionBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.viewmodel.ProfileViewModel

class DetailTermsFragment:  BaseFragment() {

    private lateinit var mBinding: FragmentTermsnconditionBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.actionbar.title = ApplicationClass.mGlobalData?.linkedAccountScreen?.termsCondition.getSafe()
    }

    override fun init() {
        val terms = profileViewModel.companyTermsAndCondition?.termsAndConditions?.termsAndConditions
        mBinding.apply {
            tvDetails.text = terms?.getSafe() ?: ApplicationClass.mGlobalData?.globalString?.noResultFound.getSafe()
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_termsncondition

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentTermsnconditionBinding
    }


    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

        }
    }

}