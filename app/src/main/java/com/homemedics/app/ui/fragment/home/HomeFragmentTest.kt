package com.homemedics.app.ui.fragment.home

import android.view.View
import androidx.fragment.app.activityViewModels
import com.fatron.network_module.models.generic.MultipleViewItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentHomeTestBinding
import com.homemedics.app.viewmodel.HomeViewModel

class HomeFragmentTest : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentHomeTestBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun setLanguageData() {

    }

    override fun init() {
        mBinding.g.apply {
            listItems = arrayListOf(
                MultipleViewItem(
                    title = "Testing 1",
                    desc = "this is testing description"
                ),
                MultipleViewItem(
                    title = "Testing 2",
                    desc = "this is testing description"
                )
            )
        }

        mBinding.h.apply {
            listItems = arrayListOf(
                MultipleViewItem(
                    title = "Testing 1",
                    desc = "this is testing description"
                ),
                MultipleViewItem(
                    title = "Testing 2",
                    desc = "this is testing description"
                )
            )
        }

        mBinding.i.apply {
            listItems = arrayListOf(
                MultipleViewItem(
                    title = "Testing 1",
                    desc = "this is testing description"
                ),
                MultipleViewItem(
                    title = "Testing 2",
                    desc = "this is testing description"
                )
            )
        }

        mBinding.k.apply {
            listItems = arrayListOf(
                MultipleViewItem(
                    title = "Testing 1",
                    desc = "this is testing description"
                ),
                MultipleViewItem(
                    title = "Testing 2",
                    desc = "this is testing description"
                )
            )
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_home_test

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentHomeTestBinding

    }

    override fun setListeners() {

    }

    override fun onClick(v: View?) {
        when(v?.id) {

        }
    }
}