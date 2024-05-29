package com.homemedics.app.ui.fragment.planning

import android.app.AlertDialog
import android.graphics.Paint
import android.text.format.DateFormat.is24HourFormat
import android.view.View
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.planschedule.AddSlotRequest
import com.fatron.network_module.models.response.DeleteSlotsResponse
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.planschedule.*
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogTimeSlotBinding
import com.homemedics.app.databinding.FragmentWorkPlannerSlotsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.WeeklyTimeSlotsAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.PlanScheduleViewModel

class WorkPlannerSlotsFragment : BaseFragment(), View.OnClickListener {

    private val planScheduleViewModel: PlanScheduleViewModel by activityViewModels()
    private lateinit var mBinding: FragmentWorkPlannerSlotsBinding
    private var weekDayId = 0
    private lateinit var weeklySlotsAdapter: WeeklyTimeSlotsAdapter
    private lateinit var builder: AlertDialog
    override fun setLanguageData() {
        mBinding.langData = ApplicationClass.mGlobalData
    }

    override fun init() {
        observe()
        mBinding.apply {
            weeklySlotsAdapter = WeeklyTimeSlotsAdapter()
            val spacing = resources.getDimensionPixelSize(R.dimen.dp16)
            rvWeeklySlots.addItemDecoration(
                RecyclerViewItemDecorator(
                    spacing,
                    RecyclerViewItemDecorator.VERTICAL
                )
            )
            rvWeeklySlots.adapter = weeklySlotsAdapter
        }
        getSlotsApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_work_planner_slots

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWorkPlannerSlotsBinding

    }

    override fun setListeners() {
        mBinding.apply {
            tvAddNew.setOnClickListener {
                showAddSlotDialog()
            }

            weeklySlotsAdapter.onSlotChipDelete =
                { item: SlotByType, position: Int, weeklySlot: WeeklySlots ->

                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                        title = langData?.dialogsStrings?.confirmDelete.getSafe(),
                        message =langData?.dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = langData?.globalString?.yes.getSafe(),
                        negativeButtonStringText = langData?.globalString?.no.getSafe(),
                        negativeButtonCallback = {},
                        buttonCallback = {
                            deleteSlotApi(
                                deleteSlotRequest(
                                    item.slots?.get(
                                        position
                                    )?.id.getSafe(),
                                    0
                                )
                            )
                        }
                    )


                }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun observe() {
        planScheduleViewModel.timeSlots.observe(this) {
            mBinding.rvWeeklySlots.setVisible((it.isNullOrEmpty().not()))
            weeklySlotsAdapter.listItems = it
        }
    }

    private lateinit var dialogViewBinding: DialogTimeSlotBinding
    private lateinit var dialogSaveButton: Button

    private fun showAddSlotDialog() {
        val selectedDays = planScheduleViewModel.selectedDays.map { it.title } as ArrayList<String>
        var timeFormat = "hh:mm aa"
        if (is24HourFormat(requireContext())) {
            timeFormat = "HH:mm"
        }
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogViewBinding = DialogTimeSlotBinding.inflate(layoutInflater)
            dialogViewBinding.apply {
                langData=mBinding.langData
                etEndTime.hint=langData?.globalString?.endTiming.getSafe()
                etStartTime.hint=langData?.globalString?.startTiming.getSafe()
                cdWeekDay.hint=langData?.planningScreen?.weekday.getSafe()

                if(TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)== DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_UR) {
                    cclVideoCall.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.selector_rounded_right_primary_transparent,
                        null
                    )
                    cclHomeVisit.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.selector_rounded_left_primary_transparent,
                        null
                    )
                }else {
                    cclVideoCall.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.selector_rounded_left_primary_transparent,
                        null
                    )
                    cclHomeVisit.background = ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.selector_rounded_right_primary_transparent,
                        null
                    )
                }
            }
            initDialogTabs()

            setView(dialogViewBinding.root)
            setTitle(mBinding.langData?.planningScreen?.addTimeSlot)
            setPositiveButton(mBinding.langData?.globalString?.save) { _, _ ->

            }
            setNegativeButton(mBinding.langData?.globalString?.cancel, null)

            dialogViewBinding.apply {
                cdWeekDay.data = selectedDays
                if (selectedDays.size.getSafe() > 0) {
                    cdWeekDay.selectionIndex = 0
                    weekDayId =
                        planScheduleViewModel.selectedDays[cdWeekDay.selectionIndex].itemId.getSafe()
                            .toInt()
                }
                cdWeekDay.onItemSelectedListener = { _, position ->
                    weekDayId =
                        planScheduleViewModel.selectedDays[position].itemId.getSafe().toInt()
                    canEnableButton()
                }
                etStartTime.clickCallback = {
                    openTimeDialog(etStartTime.mBinding.editText, timeFormat, parentFragmentManager)
                }
                etEndTime.clickCallback = {
                    openTimeDialog(etEndTime.mBinding.editText, timeFormat, parentFragmentManager)
                }
                etStartTime.mBinding.editText.doAfterTextChanged {
                    canEnableButton()
                }
                etEndTime.mBinding.editText.doAfterTextChanged {
                    canEnableButton()
                }
            }
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val doctorService = metaData?.doctorServices
                var partnerServiceId = "1"

                if (dialogViewBinding.cclHomeVisit.isChecked) {

                    val obj =
                        doctorService?.find {
                            it.genericItemId == CustomServiceTypeView.ServiceType.HomeVisit.id
                        }
                    if (obj != null)
                        partnerServiceId = obj.itemId.getSafe()
                }
                val request = AddSlotRequest(
                    dayId = weekDayId.toString(),
                    startTime = getDateInFormat(
                        dialogViewBinding.etStartTime.mBinding.editText.text.toString(),
                        timeFormat,
                        "HHmm"
                    ),
                    endTime = getDateInFormat(
                        dialogViewBinding.etEndTime.mBinding.editText.text.toString(),
                        timeFormat,
                        "HHmm"
                    ),
                    intervalId = planScheduleViewModel.intervalId.toString(),
                    partnerServiceId = partnerServiceId
                )
                addSlotsApi(request)
            }
            dialogSaveButton.isEnabled = false

        }
        builder.show()
    }

    private fun canEnableButton() {
        dialogViewBinding.apply {
            dialogSaveButton.isEnabled = isValid(etStartTime.text)
                    && isValid(etEndTime.text)
                    && cdWeekDay.selectionIndex != -1
                    && (cclHomeVisit.isChecked || cclVideoCall.isChecked)
        }
    }

    private fun initDialogTabs() {
        dialogViewBinding.apply {
            cclVideoCall.isChecked = true
            cclHomeVisit.isChecked = false

            cclHomeVisit.setOnClickListener {
                cclHomeVisit.isChecked = true
                cclVideoCall.isChecked = false
                canEnableButton()
            }
            cclVideoCall.setOnClickListener {
                cclHomeVisit.isChecked = false
                cclVideoCall.isChecked = true
                canEnableButton()
            }

            if (DataCenter.getUser().isMedicalStaff()) {
                cclHomeVisit.isChecked = true
                cclVideoCall.isEnabled = false

                //disabled UI work
                dialogViewBinding.tvVideoCall.apply {
                    setTextColor(resources.getColor(R.color.grey, requireContext().theme))
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                cclVideoCall.setBackgroundResource(R.drawable.rounded_left_primary20)
                linearLayout.setBackgroundResource(0)
            }
        }
    }


    private fun getSlotsApi() {
        if (isOnline(requireActivity())) {
            planScheduleViewModel.getSlotsApiCall().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<SlotsDataResponse>
                        if (response.data?.activeDays?.isEmpty().getSafe())
                            findNavController().safeNavigate(WorkPlannerFragmentDirections.actionWorkPlannerFragmentToPlannerSettingsFragment())
                        planScheduleViewModel.selectedDays = metaData?.days?.filter { elem ->
                            response.data?.activeDays?.contains(elem.genericItemId).getSafe()
                        } as ArrayList<GenericItem>

                        val weeklySlots = arrayListOf<WeeklySlots>()

                        response.data?.activeDays?.forEach { weekDays ->
                            val weekObj =
                                metaData?.days?.find { day -> day.itemId?.toInt() == weekDays }
                            val weeklySlot =
                                response.data?.scheduledSlots?.filter { slots -> slots.dayId == weekObj?.itemId?.toInt() }
                            val weeklySlotsVideo =
                                weeklySlot?.filter { slot -> slot.partnerServiceId == 1 }

                            val slots = arrayListOf<SlotByType>()

                            val weeklySlotsHome =
                                weeklySlot?.filter { slot -> slot.partnerServiceId == 2 }

                            val currentFormat = getString(R.string.timeFormat24)
                            val timeFormat =
                                if (is24HourFormat(requireContext())) getString(R.string.timeFormat24) else "${Constants.START}${getString(
                                    R.string.timeFormat12
                                )}${Constants.END}"
                            if (weekObj != null) {
                                if (weeklySlotsVideo?.size.getSafe() > 0) {
                                    val timeSlot = arrayListOf<TimeSlots>()

                                    weeklySlotsVideo?.forEach { scheduleSlot ->
                                        timeSlot.add(
                                            TimeSlots(
                                                scheduleSlot.id,
                                                "${getDateInFormat(
                                                    StringBuilder(scheduleSlot.startTime).insert(
                                                        2,
                                                        ":"
                                                    ).toString(),
                                                    currentFormat,
                                                    timeFormat
                                                )} ${mBinding.langData?.globalString?.to} ${getDateInFormat(
                                                        StringBuilder(scheduleSlot.endTime).insert(
                                                            2,
                                                            ":"
                                                        ).toString(),
                                                        currentFormat,
                                                        timeFormat
                                                    )}"
                                                    //                                                StringBuilder(scheduleSlot.startTime).insert(2, ":").toString(),
                                                    //                                                StringBuilder(scheduleSlot.endTime).insert(2, ":").toString(),
                                                )

                                        )
                                    }

                                    slots.add(
                                        SlotByType(
                                            mBinding.langData?.globalString?.videoCalling.getSafe(),
                                            timeSlot,
                                            R.drawable.ic_movie
                                        )
                                    )

                                }
                                if (weeklySlotsHome?.size.getSafe() > 0) {
                                    val timeSlot = arrayListOf<TimeSlots>()

                                    weeklySlotsHome?.forEach { scheduleSlot ->
                                        timeSlot.add(
                                            TimeSlots(
                                                scheduleSlot.id,
                                               "  ${getDateInFormat(
                                                   StringBuilder(scheduleSlot.startTime).insert(
                                                       2,
                                                       ":"
                                                   ).toString(),
                                                   currentFormat,
                                                   timeFormat
                                               )} ${mBinding.langData?.globalString?.to} ${getDateInFormat(
                                                        StringBuilder(scheduleSlot.endTime).insert(
                                                            2,
                                                            ":"
                                                        ).toString(),
                                                        currentFormat,
                                                        timeFormat
                                                    )}"
                                                )
                                        )
                                    }
                                    slots.add(
                                        SlotByType(
                                            mBinding.langData?.globalString?.homeVisit.getSafe(),
                                            timeSlot,
                                            R.drawable.ic_home
                                        )
                                    )

                                }
                                if (slots.size.getSafe() > 0)
                                    weeklySlots.add(
                                        WeeklySlots(
                                            weekObj.title.getSafe(),
                                            slots = slots
                                        )
                                    )
                            }
                        }
                        planScheduleViewModel.timeSlots.postValue(weeklySlots)


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

    private fun addSlotsApi(request: AddSlotRequest) {
        if (isOnline(requireActivity())) {
            planScheduleViewModel.addSlotsApiCall(request = request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
//                    val response = it.data as ResponseGeneral< >
                        builder.dismiss()
                        showToast(mBinding.langData?.messages?.slotsAddMsg.getSafe())
                        getSlotsApi()

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

    private fun deleteSlotApi(request: deleteSlotRequest) {
        if (isOnline(requireActivity())) {
            planScheduleViewModel.deleteSlotsApiCall(request = request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<DeleteSlotsResponse>
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
                                        deleteSlotApi(
                                            deleteSlotRequest(
                                                request.slotId,
                                                1
                                            )
                                        )
                                    },
                                    negativeButtonCallback = { }
                                )

                        } else {
                            showToast(getErrorMessage(response.message.getSafe()))
                            getSlotsApi()
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