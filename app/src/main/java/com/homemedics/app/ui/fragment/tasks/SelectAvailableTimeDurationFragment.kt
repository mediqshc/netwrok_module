package com.homemedics.app.ui.fragment.tasks

import android.view.View
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.partnerprofile.PartnerAvailabilityRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.partnerprofile.PartnerAvailabilityResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectAvailableTimeDurationBinding
import com.homemedics.app.ui.adapter.LanguageAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.TaskAppointmentsViewModel
import timber.log.Timber

class SelectAvailableTimeDurationFragment : BaseFragment(), View.OnClickListener {

    private val taskAppointmentsViewModel: TaskAppointmentsViewModel by activityViewModels()
    private lateinit var mBinding: FragmentSelectAvailableTimeDurationBinding

    private lateinit var adapter: LanguageAdapter
    val langData = ApplicationClass.mGlobalData

    override fun setLanguageData() {
        mBinding.apply {
            actionbar.title = langData?.taskScreens?.selectAvailableNowTitle.getSafe()
            bSelect.text = langData?.globalString?.select
        }
    }

    override fun init() {
        populateAddressList()
    }

    override fun getFragmentLayout() = R.layout.fragment_select_available_time_duration

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectAvailableTimeDurationBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    findNavController().popBackStack()
                }
            }

            adapter.onItemSelected = {
                bSelect.isEnabled = true
            }

            bSelect.setOnClickListener(this@SelectAvailableTimeDurationFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bSelect -> {
                val request = PartnerAvailabilityRequest(
                    partnerUserId = DataCenter.getUser()?.id,
                    availableTill = adapter.getSelectedItem()?.desc,
                    forceUpdate = 0,
                    availableNow = 1
                )
                setAvailableApi(request)
            }
        }
    }

    private fun populateAddressList() {
        val timesList = taskAppointmentsViewModel.partnerAvailabilityResponse?.availabilityOptions.getSafe()

        val itemsList = arrayListOf<MultipleViewItem>()

        timesList.forEachIndexed { index, item ->
            itemsList.add(
                MultipleViewItem(itemId = index.toString()).apply {
                    if(item == "0")
                        title = langData?.taskScreens?.wholeDay
                    else
                        title = getMinutesInHHmm(item)
                    desc = item //original time without unit
                }
            )
        }

        adapter = LanguageAdapter()
        adapter.listItems = itemsList
        mBinding.apply {
            rvLanguage.adapter = adapter
        }
    }

    private fun getMinutesInHHmm(mins: String): String{
        if (mins.isDigitsOnly()){
            val t = mins.toInt()
            val hours: Int = t / 60 // since both are ints, you get an int
            val minutes: Int = t % 60

            val hourUnit = if(hours > 1) langData?.globalString?.hours?.lowercase() else langData?.globalString?.hour?.lowercase()
            val minuteUnit = langData?.globalString?.minutes?.lowercase()

            val formatted =
                if(hours > 0 && minutes > 0)
                    "$hours $hourUnit $minutes $minuteUnit"
                else if(hours > 0 && minutes == 0)
                    "$hours $hourUnit"
                else if(hours == 0 && minutes > 0)
                    "$minutes $minuteUnit"
                 else
                     "0"

            return formatted
        }

        return "0"
    }

    private fun setAvailableApi(){
        val slots = "0"

        DialogUtils(requireActivity())
            .showDoubleButtonsAlertDialog(
                title = langData?.globalString?.warning.getSafe(),
                message = langData?.planningScreen?.warningPara?.replace(
                    "[0]",
                    slots
                ).getSafe()+ "\n\n${langData?.planningScreen?.warningPara2}",
                positiveButtonStringText =langData?.globalString?.btnContinue.getSafe(),
                negativeButtonStringText = langData?.globalString?.goBack.getSafe(),
                buttonCallback = {
                    findNavController().popBackStack()
                },
                negativeButtonCallback = { }
            )
    }

    private fun setAvailableApi(request: PartnerAvailabilityRequest){
        if (isOnline(requireActivity())) {
            taskAppointmentsViewModel.setPartnerAvailability(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PartnerAvailabilityResponse>
                        response.data?.let { res ->

                            if(request.forceUpdate == 1) {
                                findNavController().popBackStack()
                            }
                            else {
                                var next = ""
                                var duration = ""

                                if(adapter.getSelectedItem()?.desc == "0") { //whole day
                                    duration = langData?.taskScreens?.wholeDay?.lowercase().getSafe()
                                    next = ""
                                }
                                else {
                                    duration = adapter.getSelectedItem()?.title?.lowercase().getSafe()
                                    next = langData?.globalString?.next_.getSafe()
                                }

                                Timber.e(langData?.dialogsStrings?.partnerNoAvailableSlots)

                                val msg = if(res.availableSlots.getSafe() > 0){
                                    val rep0 = langData?.dialogsStrings?.partnerAvailableSlots?.replace("[0]", next)
                                    val rep1 = rep0?.replace("[1]", duration.getSafe())
                                    val rep2 = rep1?.replace("[2]", res.availableSlots.getSafe().toString())
                                    rep2
                                }
                                else {
                                    val rep0 = langData?.dialogsStrings?.partnerNoAvailableSlots?.replace("[0]", next)
                                    val rep1 = rep0?.replace("[1]", duration.getSafe())
                                    rep1
                                }

                                DialogUtils(requireActivity())
                                    .showDoubleButtonsAlertDialog(
                                        title = langData?.globalString?.confirm.getSafe(),
                                        message = msg.getSafe(),
                                        positiveButtonStringText =langData?.globalString?.confirm.getSafe(),
                                        negativeButtonStringText = langData?.globalString?.goBack.getSafe(),
                                        buttonCallback = {
                                            request.apply {
                                                this.forceUpdate = 1
                                            }
                                            setAvailableApi(request)
                                        },
                                        negativeButtonCallback = { }
                                    )
                            }
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
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}