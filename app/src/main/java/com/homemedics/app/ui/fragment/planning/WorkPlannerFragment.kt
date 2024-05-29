package com.homemedics.app.ui.fragment.planning

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentWorkPlannerBinding
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.safeNavigate
import com.homemedics.app.viewmodel.PlanScheduleViewModel

class WorkPlannerFragment : BaseFragment(), View.OnClickListener {

    private val planScheduleViewModel: PlanScheduleViewModel by activityViewModels()
    private lateinit var mBinding: FragmentWorkPlannerBinding

    override fun setLanguageData() {
        mBinding.actionbar.title=ApplicationClass.mGlobalData?.planningScreen?.workPlanner.getSafe()
    }

    override fun init() {
        mBinding.apply {
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(WorkPlannerSlotsFragment())
                add(OffDatesFragment())
            }
            viewPager.adapter = TabsPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> ApplicationClass.mGlobalData?.planningScreen?.weeklyPlanner
                    1 ->ApplicationClass.mGlobalData?.planningScreen?.offDates
                    else -> {
                        ""
                    }
                }
            }.attach()
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_work_planner

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWorkPlannerBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click={
                findNavController().safeNavigate(WorkPlannerFragmentDirections.actionWorkPlannerFragmentToPlannerSettingsFragment())

            }
        }
    }

    override fun onClick(v: View?) {

    }
}