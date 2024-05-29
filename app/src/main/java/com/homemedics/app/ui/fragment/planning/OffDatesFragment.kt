package com.homemedics.app.ui.fragment.planning

import android.app.AlertDialog
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.fatron.network_module.models.request.offdates.DeleteOffDatesRequest
import com.fatron.network_module.models.request.offdates.OffDatesRequest
import com.fatron.network_module.models.response.DeleteSlotsResponse
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.offdates.OffDate
import com.fatron.network_module.models.response.offdates.OffDatesResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogOffDatesBinding
import com.homemedics.app.databinding.FragmentOffDatesBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.OffDatesSlotAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.PlanScheduleViewModel
import timber.log.Timber


class OffDatesFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentOffDatesBinding

    private lateinit var dialogViewBinding: DialogOffDatesBinding

    private lateinit var builder: AlertDialog

    private lateinit var dialogSaveButton: Button

    private val planScheduleViewModel: PlanScheduleViewModel by activityViewModels()

    private lateinit var offDatesSlotAdapter: OffDatesSlotAdapter

    override fun setLanguageData() {
        mBinding.langData = ApplicationClass.mGlobalData
    }

    override fun init() {
        observe()
        populateOffDateList()
    }

    override fun getFragmentLayout() = R.layout.fragment_off_dates

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentOffDatesBinding
    }

    override fun setListeners() {
        mBinding.apply {
            tvAddNew.setOnClickListener(this@OffDatesFragment)
            offDatesSlotAdapter.itemDeleteListener = { offDate, position ->
                DialogUtils(requireActivity())
                    .showDoubleButtonsAlertDialog(
                        message =mBinding.langData?.dialogsStrings?.deleteDesc.getSafe(),
                        buttonCallback = { delete(offDate, position) },
                        negativeButtonCallback = {}
                    )
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvAddNew -> showOffDatesDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        getOffDates()
    }

    private fun observe() {
        planScheduleViewModel.offDates.observe(this) {
            mBinding.rvOffDates.setVisible((it.isNullOrEmpty().not()))
            offDatesSlotAdapter.listItems = it
        }
    }

    private fun populateOffDateList() {
        mBinding.apply {
            offDatesSlotAdapter = OffDatesSlotAdapter()
            rvOffDates.adapter = offDatesSlotAdapter
        }
    }

    private fun showOffDatesDialog() {
        val use24HourClock: Boolean = DateFormat.is24HourFormat(requireContext())
        val mergedTimeFormat12 = "dd MMMM yyyy ${Constants.START}hh:mm a${Constants.END}"
        val mergedTimeFormat24 = "dd MMMM yyyy ${Constants.START}HH:mm${Constants.END}"
        val timeFormat12 = getString(R.string.timeFormat12)
        val timeFormat24 = getString(R.string.timeFormat24)
        val dateFormat = getString(R.string.dateFormat)
        val apiFormat = getString(R.string.apiDateTimeFormat)
        val dialogTimeFormat = if (use24HourClock) timeFormat24 else timeFormat12
        val mergedTimeFormat = if (use24HourClock) mergedTimeFormat24 else mergedTimeFormat12

        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogViewBinding = DialogOffDatesBinding.inflate(layoutInflater)
            dialogViewBinding.apply {
                tvVideoCall.text=mBinding.langData?.globalString?.videoCalling
                tvHomeVisit.text=mBinding.langData?.globalString?.homeVisit
                tvMsgConsultation.text=mBinding.langData?.chatScreen?.messageConsultation
                etStartTimeDate.hint=mBinding.langData?.planningScreen?.startTimingDate.getSafe()
                etEndTimeDate.hint=mBinding.langData?.planningScreen?.endTimingDate.getSafe()
            }
            if (DataCenter.getUser().isMedicalStaff()) {
                dialogViewBinding.doctorGroup.setVisible(false) //medical staff can only have home visit service type
            }

            setView(dialogViewBinding.root)
            mBinding.langData?.apply {
            setTitle(this.planningScreen?.addOffDates)
            setPositiveButton(this.globalString?.save) { _, _ -> }
            setNegativeButton(this.globalString?.cancel, null)
            }
            setCancelable(false)
            dialogViewBinding.apply {
                sbVideoCall.setOnClickListener {
                    validate()
                }
                sbHomeVisit.setOnClickListener {
                    validate()
                }
                sbMsgConsultation.setOnClickListener {
                    validate()
                }
                etStartTimeDate.clickCallback = {
                    openCalender(
                        etStartTimeDate.mBinding.editText,
                        format = dateFormat,
                        restrictFutureDates = false,
                        restrictPastDates = true,
                        valueOnlyReturn = true,
                        onDismiss = { dateSelected ->
                            if (dateSelected.isNotEmpty()) {
                                openTimeDialog(
                                    etStartTimeDate.mBinding.editText,
                                    dialogTimeFormat,
                                    parentFragmentManager,
                                    valueOnlyReturn = true,
                                    onDismiss = { timeSelected ->
                                        if (timeSelected.isNotEmpty())
                                            etStartTimeDate.text = "$dateSelected ${Constants.START}$timeSelected${Constants.END}"
                                    }
                                )
                            }
                        }
                    )

                }
                etEndTimeDate.clickCallback = {
                    openCalender(
                        etEndTimeDate.mBinding.editText,
                        format = dateFormat,
                        restrictFutureDates = false,
                        restrictPastDates = true,
                        valueOnlyReturn = true,
                        onDismiss = { dateSelected ->
                            if (dateSelected.isNotEmpty()) {
                                openTimeDialog(
                                    etEndTimeDate.mBinding.editText,
                                    dialogTimeFormat,
                                    parentFragmentManager,
                                    valueOnlyReturn = true,
                                    onDismiss = { timeSelected ->
                                        if (timeSelected.isNotEmpty())
                                            etEndTimeDate.text = "$dateSelected ${Constants.START}$timeSelected${Constants.END}"
                                    }
                                )
                            }
                        }
                    )
                }
                etStartTimeDate.mBinding.editText.doAfterTextChanged {
                    validate()
                }
                etEndTimeDate.mBinding.editText.doAfterTextChanged {
                    validate()
                }
            }
        }.create()
        builder.setOnShowListener {
            (planScheduleViewModel.offDatesRequest.servicesIds) = arrayListOf()
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                (planScheduleViewModel.offDatesRequest.servicesIds as ArrayList<Int>).clear()
                if (dialogViewBinding.sbVideoCall.isChecked) {
                    (planScheduleViewModel.offDatesRequest.servicesIds as ArrayList<Int>).add(
                        CustomServiceTypeView.ServiceType.VideoCall.id
                    )
                }
                if (dialogViewBinding.sbHomeVisit.isChecked) {
                    (planScheduleViewModel.offDatesRequest.servicesIds as ArrayList<Int>).add(
                        CustomServiceTypeView.ServiceType.HomeVisit.id
                    )
                }
                if (dialogViewBinding.sbMsgConsultation.isChecked) {
                    (planScheduleViewModel.offDatesRequest.servicesIds as ArrayList<Int>).add(
                        CustomServiceTypeView.ServiceType.Message.id
                    )
                }

                if (DataCenter.getUser().isMedicalStaff()) {
                    planScheduleViewModel.offDatesRequest.servicesIds =
                        arrayListOf(CustomServiceTypeView.ServiceType.HomeVisit.id)
                }

                Timber.e(mergedTimeFormat)
                planScheduleViewModel.offDatesRequest.startDateTime = getDateInFormat(
                    dialogViewBinding.etStartTimeDate.mBinding.editText.text.toString(),
                    mergedTimeFormat,
                    apiFormat
                )
                planScheduleViewModel.offDatesRequest.endDateTime = getDateInFormat(
                    dialogViewBinding.etEndTimeDate.mBinding.editText.text.toString(),
                    mergedTimeFormat,
                    apiFormat
                )

                addOffDates(planScheduleViewModel.offDatesRequest.apply {
                    forceDelete = 0
                })
            }
            dialogSaveButton.isEnabled = false
        }
        builder.show()
    }

    private fun validate() {
        dialogViewBinding.apply {
            dialogSaveButton.isEnabled =
                isValid(etStartTimeDate.text) && isValid(etEndTimeDate.text)
                        && ((DataCenter.getUser()
                    .isDoctor() && (isChecked(sbVideoCall.isChecked) || isChecked(sbHomeVisit.isChecked) || isChecked(
                    sbMsgConsultation.isChecked
                )))
                        || DataCenter.getUser().isMedicalStaff())
        }
    }

    private fun getOffDates() {
        if (isOnline(requireActivity())) {
            planScheduleViewModel.getOffDates().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<OffDatesResponse>
                        planScheduleViewModel.offDates.postValue(response.data?.offDates as ArrayList<OffDate>?)
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

    private fun addOffDates(addOffDatesRequest: OffDatesRequest) {
        if (isOnline(requireActivity())) {
            planScheduleViewModel.addOffDates(addOffDatesRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<DeleteSlotsResponse>
                        if (response.data?.slotExists.getSafe() > 0) {
//                            addOffDatesRequest.forceDelete=1

                            DialogUtils(requireActivity())
                                .showDoubleButtonsAlertDialog(
                                    title = mBinding.langData?.globalString?.warning.getSafe(),
                                    message = mBinding.langData?.planningScreen?.warningPara?.replace(
                                        "[0]",
                                        response.data?.slotExists.toString()
                                    ).getSafe()+ "\n\n${mBinding.langData?.planningScreen?.warningPara2}",
                                    positiveButtonStringText =mBinding.langData?.globalString?.btnContinue.getSafe(),
                                    negativeButtonStringText =  mBinding.langData?.globalString?.goBack.getSafe(),
                                    buttonCallback = {
                                        addOffDates(
                                            addOffDatesRequest.apply {
                                                forceDelete = 1
                                            }
                                        )
                                    },
                                    negativeButtonCallback = { }
                                )
                        } else {
                            showToast(mBinding.langData?.messages?.off_date_added.getSafe())
                            builder.dismiss()
                            getOffDates()
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

    private fun delete(offDate: OffDate, position: Int) {
        val request = DeleteOffDatesRequest(offDateId = offDate.id)
        deleteOffDates(deleteOffDatesRequest = request, position = position)
    }

    private fun deleteOffDates(deleteOffDatesRequest: DeleteOffDatesRequest, position: Int) {
        if (isOnline(requireActivity())) {
            planScheduleViewModel.deleteOffDate(deleteOffDatesRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        offDatesSlotAdapter.apply {
//                            listItems.removeAt(position)
                            planScheduleViewModel.offDates.value?.removeAt(position)
                            notifyDataSetChanged()
                        }
                        getOffDates()
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