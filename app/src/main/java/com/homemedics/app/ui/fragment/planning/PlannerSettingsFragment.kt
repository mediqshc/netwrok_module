package com.homemedics.app.ui.fragment.planning

import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.activedays.ActiveDaysRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.activedays.ActiveDaysResponse
import com.fatron.network_module.models.response.activedays.AddActiveDaysResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentPlannerSettingsBinding
import com.homemedics.app.ui.adapter.WeekDaysAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.PlanScheduleViewModel

class PlannerSettingsFragment : BaseFragment(), View.OnClickListener {

    private val planScheduleViewModel: PlanScheduleViewModel by activityViewModels()
    private lateinit var mBinding: FragmentPlannerSettingsBinding
    private lateinit var weekDaysAdapter: WeekDaysAdapter
    private val user: UserResponse? by lazy {
        DataCenter.getUser()
    }

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.planningScreen?.navTitle.getSafe()
            cdTimeSlot.hint = langData?.globalString?.minutes.getSafe()
        }
    }

    override fun init() {
        fetchSlotIntervals()
        weekDaysAdapter = WeekDaysAdapter()
        mBinding.apply {
            if (user.isMedicalStaff()) {
                clMinutes.setVisible(false)
                divider1.setVisible(false)
            }

            weekDaysAdapter.listItems = metaData?.days?.map {
                val hasItem =
                    planScheduleViewModel.selectedDays.find { item -> it.title == item.title }
                if (hasItem != null) {
//                    bSave.isEnabled = true
                    it.isSelected = true
                }
                it
            } as ArrayList<GenericItem>
            rvWeekDays.adapter = weekDaysAdapter
        }
        getActiveDays()
    }

    override fun getFragmentLayout() = R.layout.fragment_planner_settings

    override fun getViewModel() {

    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBack()
                }
            }
        )
    }

    private fun handleBack() {
        if (planScheduleViewModel.timeSlots.value?.isEmpty()
                .getSafe() && planScheduleViewModel.selectedDays.isEmpty().getSafe()
        ) {
            findNavController().popBackStack(R.id.workPlannerFragment, true)
        } else
            findNavController().popBackStack()
    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPlannerSettingsBinding
    }

    override fun setListeners() {
        handleBackPress()
        mBinding.apply {
            actionbar.onAction1Click = {
                handleBack()
            }
            bSave.setOnClickListener {
                saveDays()
            }
            weekDaysAdapter.onCheckChanged = {
                bSave.isEnabled = weekDaysAdapter.getSelectedItems().size > 0
            }
        }
    }

    override fun onClick(v: View?) {

    }

    override fun onDetach() {
        super.onDetach()
        planScheduleViewModel.flushData()
    }

    private fun fetchSlotIntervals() {
        val meta = DataCenter.getMeta()
        val slotIntervals = meta?.slotIntervals?.map { it.name }

        mBinding.apply {
            cdTimeSlot.data = slotIntervals as ArrayList<String>
            if (slotIntervals.isNotEmpty()) {
//                cdTimeSlot.selectionIndex = 0 // client feedback show by default empty
                planScheduleViewModel.intervalId=meta.slotIntervals?.get(cdTimeSlot.selectionIndex)?.id.getSafe()
            }
            cdTimeSlot.onItemSelectedListener = { _, position ->
                planScheduleViewModel.intervalId = meta.slotIntervals?.get(position)?.id.getSafe()
            }
        }
    }

    private fun saveDays() {
        planScheduleViewModel.selectedDays = weekDaysAdapter.getSelectedItems()
        planScheduleViewModel.selectedDaysId.clear()
        planScheduleViewModel.selectedDays.forEachIndexed { _, genericItem ->
            planScheduleViewModel.selectedDaysId.add(genericItem.itemId?.toInt().getSafe())
        }
        val request = ActiveDaysRequest(
            activeDays = planScheduleViewModel.selectedDaysId,
            intervalId = planScheduleViewModel.intervalId
        )
        addActiveDays(request)
    }

    private fun addActiveDays(request: ActiveDaysRequest) {
        if (isOnline(requireActivity())) {
            planScheduleViewModel.addActiveDay(activeDaysRequest = request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<AddActiveDaysResponse>
                        if (response.data?.slotExists.getSafe() > 0) {
                            DialogUtils(requireActivity())
                                .showDoubleButtonsAlertDialog(
                                    title = mBinding.langData?.globalString?.warning.getSafe(),
                                    message = mBinding.langData?.planningScreen?.warningPara?.replace(
                                        "[0]",
                                        response.data?.slotExists.toString()
                                    ).getSafe(),
                                    positiveButtonStringText =mBinding.langData?.globalString?.btnContinue.getSafe(),
                                    negativeButtonStringText =  mBinding.langData?.globalString?.goBack.getSafe(),
                                    buttonCallback = {
                                        request.forceDelete = 1
                                        addActiveDays(
                                            request
                                        )
                                    },
                                    negativeButtonCallback = { }
                                )
                        } else {

                            if (request.activeDays?.isNotEmpty().getSafe())
                                findNavController().popBackStack()
                        }
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
                    message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getActiveDays() {
        if (isOnline(requireActivity())) {
            planScheduleViewModel.getActiveDays().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<ActiveDaysResponse>
                        val activeDays: List<Int>? = response.data?.activeDays
                        activeDays?.forEachIndexed { _, i ->
                            weekDaysAdapter.listItems[i - 1].isSelected = true
                            weekDaysAdapter.notifyItemChanged(i - 1)
                        }
                        mBinding.bSave.isEnabled = activeDays?.size.getSafe() > 0
                        val intervalId = response.data?.intervalId
                        val intervalIndex = metaData?.slotIntervals?.indexOfFirst { it.id == intervalId }.getSafe()
                        if (intervalIndex != -1) {
                            // client feedback show by default empty
//                            intervalIndex = 0
                            mBinding.cdTimeSlot.selectionIndex = intervalIndex
                        }
                        planScheduleViewModel.intervalId = intervalId.getSafe()
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
                    message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

}