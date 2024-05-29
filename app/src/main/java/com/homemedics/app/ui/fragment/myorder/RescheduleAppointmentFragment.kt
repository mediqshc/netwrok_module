package com.homemedics.app.ui.fragment.myorder

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.request.orders.RescheduleRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.bdc.PartnerSlotsResponse
import com.fatron.network_module.models.response.partnerprofile.DateSlotResponse
import com.fatron.network_module.models.response.partnerprofile.TimeSlot
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentRescheduleAppointmentBinding
import com.homemedics.app.ui.adapter.BDCTimeSlotsAdapter
import com.homemedics.app.ui.adapter.DateSlotsAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.MyOrderViewModel

class RescheduleAppointmentFragment : BaseFragment() {

    private lateinit var mBinding: FragmentRescheduleAppointmentBinding
    private lateinit var dateSlotsAdapter: DateSlotsAdapter
    private val ordersViewModel: MyOrderViewModel by activityViewModels()
    lateinit var timeSlotsAdapter: BDCTimeSlotsAdapter

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.globalString?.rescheduleAppointment.getSafe()
        }
    }

    override fun init() {
        if (isOnline(requireContext())) {
            getSlots()
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
        setupDateSlotsList()
        val spacing = resources.getDimensionPixelSize(R.dimen.dp6)
        mBinding.apply {

            rvSlots.layoutManager = GridLayoutManager(requireContext(), 4)
            rvSlots.addItemDecoration(GridItemDecoration(spacing, 4, false))
        }



    }

    override fun getFragmentLayout() = R.layout.fragment_reschedule_appointment

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentRescheduleAppointmentBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            bReschedule.setOnClickListener {
                rescheduleAppointment()
            }
        }
    }

    private fun setupDateSlotsList() {
        mBinding.apply {
            dateSlotsAdapter = DateSlotsAdapter()
            rvDateSlots.adapter = dateSlotsAdapter

            dateSlotsAdapter.onItemSelected = { item, pos ->
                ordersViewModel.bookConsultationRequest.bookingDate = item.timestamp
                updateTimeSlots(pos)
            }
        }
    }

    private fun getSlots() {
        val request = PartnerDetailsRequest(
            serviceId = ordersViewModel.serviceId,
            partnerUserId = ordersViewModel.partnerUserId,
            cityId = ordersViewModel.orderDetailsResponse?.bookingDetails?.cityId
        )

        ordersViewModel.getSlots(request = request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    hideLoader()
                    val response = it.data as ResponseGeneral<PartnerSlotsResponse>
                    response.data?.let { partnerSlotResponse ->
                        ordersViewModel.partnerSlotsResponse = partnerSlotResponse
                    }
                    setSlotsDataInViews(response.data)
                }
                is ResponseResult.Pending -> {
                    showLoader()
                }
                is ResponseResult.Failure -> {
                    hideLoader()
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = it.error.message.getSafe(),
                            buttonCallback = {},
                        )
                }
                is ResponseResult.ApiError -> {
                    hideLoader()
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                            buttonCallback = {},
                        )
                }
                is ResponseResult.Complete -> {
                    hideLoader()
                }
                else -> {
                    hideLoader()
                }
            }
        }
    }

    private fun setSlotsDataInViews(data: PartnerSlotsResponse?) {
        data?.let {
            ordersViewModel.bookConsultationRequest.apply {
                fee = it.fee
            }

            if (it.dateSlots.isNullOrEmpty().not()) {
                val filter = it.dateSlots?.filter { slots -> slots.isActive == true }
                val bookingDate = ordersViewModel.bookConsultationRequest.bookingDate

                if(filter?.isNotEmpty().getSafe()) {
                    val selectedDate = it.dateSlots?.first { slots -> slots.isActive == true }?.timestamp.getSafe()
                    ordersViewModel.bookConsultationRequest.bookingDate = selectedDate

                    it.dateSlots?.map {
                        it.isChecked = it.timestamp == ordersViewModel.bookConsultationRequest.bookingDate
                        it
                    }

                    dateSlotsAdapter.listItems = it.dateSlots as ArrayList<DateSlotResponse>
                    val index = dateSlotsAdapter.listItems .indexOfFirst {  it.timestamp == ordersViewModel.bookConsultationRequest.bookingDate}

                    if(index != -1) {
                        updateTimeSlots(index)
                        mBinding.rvDateSlots.smoothScrollToPosition(index)
                    }
                }
            }
        }
    }

    private fun updateTimeSlots(selectedDateIndex: Int) {
         timeSlotsAdapter = BDCTimeSlotsAdapter(CustomServiceTypeView.ServiceType.getServiceById(ordersViewModel.serviceId.getSafe()))
        mBinding.rvSlots.adapter = timeSlotsAdapter
        ordersViewModel.apply {
            mBinding.bReschedule.isEnabled=false
            timeSlotsAdapter.onTimeSlotSelected = { item, position ->
                mBinding.bReschedule.isEnabled=true
                bookConsultationRequest.startTime = item.start
                bookConsultationRequest.endTime = item.end
                bookConsultationRequest.shiftId=item.shiftId
            }

            val combinedList = ArrayList<TimeSlot>()
            try {
                partnerSlotsResponse.dateSlots
                    ?.get(selectedDateIndex)?.slots?.forEach {
                        it?.forEach { timeSlot ->
                            combinedList.add(timeSlot)
                        }
                    }
                mBinding.tvNoData.setVisible(combinedList.size == 0)

                val selectedIndex = combinedList.indexOfFirst {
                    (it.start == bookConsultationRequest.startTime && it.end == bookConsultationRequest.endTime)
                            || it.isChecked
                }

                combinedList.map { //avoiding multiple checks because of multiple arrays
                    it.isChecked = false
                    it
                }

                if(selectedIndex != -1){
                    val item = combinedList[selectedIndex]
                    bookConsultationRequest.startTime = item.start
                    bookConsultationRequest.endTime = item.end
                    combinedList[selectedIndex].isChecked = true
                    mBinding.bReschedule.isEnabled=true
                }
                else {
                    bookConsultationRequest.startTime = null
                    bookConsultationRequest.endTime = null
                }

                timeSlotsAdapter.listItems = combinedList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun rescheduleAppointment() {
        val request = RescheduleRequest(
            bookingId = ordersViewModel.bookingId.toInt(),
            date = ordersViewModel.bookConsultationRequest.bookingDate,
            startTime = ordersViewModel.bookConsultationRequest.startTime,
            endTime = ordersViewModel.bookConsultationRequest.endTime,
            shiftId =   ordersViewModel. bookConsultationRequest.shiftId
        )
        ordersViewModel.rescheduleOrder(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    findNavController().popBackStack()
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
                else -> { hideLoader() }
            }
        }
    }
}