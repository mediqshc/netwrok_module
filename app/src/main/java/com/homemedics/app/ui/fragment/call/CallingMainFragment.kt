package com.homemedics.app.ui.fragment.call

import android.view.View
import androidx.fragment.app.activityViewModels
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCallingMainBinding
import com.homemedics.app.utils.gone
import com.homemedics.app.utils.visible
import com.homemedics.app.viewmodel.CallViewModel


class CallingMainFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentCallingMainBinding

    private val callViewModel: CallViewModel by activityViewModels()

    override fun setLanguageData() {

    }

    override fun getFragmentLayout(): Int = R.layout.fragment_calling_main

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCallingMainBinding
    }

    override fun setListeners() {
        mBinding.apply {

        }
    }

    override fun init() {
        mBinding.apply {
            gCallAccepted.gone()
            gOutgoingCall.gone()
            gIncomingCall.gone()
            gCallMulti.visible()
        }
    }

    override fun onClick(view: View?) {

    }
}