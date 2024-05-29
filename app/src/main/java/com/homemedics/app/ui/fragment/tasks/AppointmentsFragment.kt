package com.homemedics.app.ui.fragment.tasks

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.fatron.network_module.models.request.partnerprofile.PartnerAvailabilityRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.AppointmentListResponse
import com.fatron.network_module.models.response.appointments.AppointmentServicesResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerAvailabilityResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAppointmentsBinding
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.TaskAppointmentsViewModel

class AppointmentsFragment : BaseFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private lateinit var mBinding: FragmentAppointmentsBinding

    private val appointmentViewModel: TaskAppointmentsViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            langData=ApplicationClass.mGlobalData
            actionbar.title=langData?.taskScreens?.appointments.getSafe()

        }
    }

    override fun init() {
        if(arguments?.getString(Constants.BOOKING_ID).getSafe().isNotEmpty()){
            appointmentViewModel.bookingId=arguments?.getString(Constants.BOOKING_ID).getSafe()
            appointmentViewModel.partnerServiceId =arguments?.getInt("partnerServiceId").getSafe()
            appointmentViewModel.dutyId =arguments?.getInt("dutyId").getSafe()
            findNavController().safeNavigate(R.id.action_notificationFragment_to_appointmentsDetailFragment)
            arguments?.clear()
        }

        mBinding.llAvailableNow.setVisible(DataCenter.getUser().isDoctor())

        doctorMedicalTask()
        initTabsPager()
        setSelectedServices(true)
        getAvailableApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_appointments

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAppointmentsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            sbAvailable.setOnClickListener {
                if(sbAvailable.isChecked)
                    findNavController().safeNavigate(AppointmentsFragmentDirections.actionAppointmentsFragmentToSelectAvailableTimeDurationFragment())
                else {
                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                        title = langData?.dialogsStrings?.areYouSure.getSafe(),
                        message = langData?.dialogsStrings?.markUnavailableWarning.getSafe(),
                        negativeButtonStringText = langData?.globalString?.cancel.getSafe(),
                        positiveButtonStringText = langData?.globalString?.yes.getSafe(),
                        negativeButtonCallback = {
                            sbAvailable.isChecked = true
                        },
                        buttonCallback = {
                            val request = PartnerAvailabilityRequest(
                                partnerUserId = DataCenter.getUser()?.id,
                                forceUpdate = 1,
                                availableNow = 0,
                                availableTill = "0"
                            )
                            setAvailableApi(request)
                        }
                    )
                }
            }

            sbVideoCall.setOnCheckedChangeListener(this@AppointmentsFragment)
            sbMessage.setOnCheckedChangeListener(this@AppointmentsFragment)
            sbHome.setOnCheckedChangeListener(this@AppointmentsFragment)
            sbClinic.setOnCheckedChangeListener(this@AppointmentsFragment)
        }
    }

    override fun onClick(v: View?) {

    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        appointmentViewModel.listItems= arrayListOf()
        appointmentViewModel.fromSelect= true
        setSelectedServices(true)
        getAppointments()
    }

    private fun setSelectedServices(isCleared: Boolean=false) {
        val selectedServices = arrayListOf(0)
        appointmentViewModel.appointmentListRequest.apply {
            mBinding.let {
                if(it.sbVideoCall.isChecked) selectedServices.add(CustomServiceTypeView.ServiceType.VideoCall.id)
                if(it.sbMessage.isChecked) selectedServices.add(CustomServiceTypeView.ServiceType.Message.id)
                if(it.sbHome.isChecked) selectedServices.add(CustomServiceTypeView.ServiceType.HomeVisit.id)
                if(it.sbClinic.isChecked) selectedServices.add(CustomServiceTypeView.ServiceType.Clinic.id)
            }

            services = selectedServices
        }


    }

    private fun initTabsPager() {
        mBinding.apply {
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(UpcomingFragment().apply {
                    val bundle = Bundle()
                    arguments = bundle.apply { putSerializable("type", Enums.AppointmentType.UPCOMING) } }
                )
                add(UpcomingFragment().apply {
                    val bundle = Bundle()
                    arguments = bundle.apply { putSerializable("type", Enums.AppointmentType.UNREAD) } }
                )
                add(UpcomingFragment().apply {
                    val bundle = Bundle()
                    arguments = bundle.apply { putSerializable("type", Enums.AppointmentType.HISTORY) } }
                )
            }
            viewPager.adapter = TabsPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> mBinding.langData?.globalString?.upcoming
                    1 -> mBinding.langData?.globalString?.unread
                    2 ->  mBinding.langData?.globalString?.history
                    else -> {
                        ""
                    }
                }
            }.attach()
            viewPager.isUserInputEnabled = false
            viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    var type = ""


                    when (position) {
                        0 -> {
                            appointmentViewModel.listItems= arrayListOf()
                             type = getString(R.string.upcoming)
                            appointmentViewModel.selectedTab.postValue(Enums.AppointmentType.UPCOMING)
                        }
                        1 -> {
                            appointmentViewModel.listItems= arrayListOf()
                            type = getString(R.string.unread)
                            appointmentViewModel.selectedTab.postValue(Enums.AppointmentType.UNREAD)
                        }
                        2 -> {
                            appointmentViewModel.listItems= arrayListOf()
                            type = getString(R.string.history)
                            appointmentViewModel.selectedTab.postValue(Enums.AppointmentType.HISTORY)
                        }
                        else -> {
                            appointmentViewModel.listItems= arrayListOf()
                            type = ""
                            appointmentViewModel.selectedTab.postValue(Enums.AppointmentType.UPCOMING)
                        }
                    }
                    appointmentViewModel.appointmentListRequest.appointmentType = type.lowercase()
                    appointmentViewModel.fromSelect= false
                    getAppointments()
                }
            })
        }

    }

    private fun doctorMedicalTask() {
        mBinding.clServices.setVisible(DataCenter.getUser().isDoctor())
    }

    private fun getAppointments(isCleared: Boolean=false) {
        if (isOnline(requireActivity())) {
            appointmentViewModel.appointmentListResponse.value=  null
             appointmentViewModel.getAppointments().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<AppointmentListResponse>
                        response.data?.let { appointmentList ->
                            appointmentViewModel.isClearedRequired = isCleared
                            appointmentViewModel.appointmentListResponse.postValue(appointmentList)
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

    private fun getAppointmentsServices() {
        if (isOnline(requireActivity())) {
            appointmentViewModel.getAppointmentsServices().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<AppointmentServicesResponse>
                        response.data?.let { appointmentServiceList ->

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

    private fun setAvailableApi(request: PartnerAvailabilityRequest){
        if (isOnline(requireActivity())) {
            appointmentViewModel.setPartnerAvailability(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
//                        val response = it.data as ResponseGeneral<AppointmentServicesResponse>
//                        response.data?.let { appointmentServiceList ->
//
//                        }

                        mBinding.tvAvailableTime.setVisible(request.availableNow.getBoolean())
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

    private fun getAvailableApi(){
        val request = PartnerAvailabilityRequest(
            partnerUserId = DataCenter.getUser()?.id,
        )

        if (isOnline(requireActivity())) {
            appointmentViewModel.getPartnerAvailability(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PartnerAvailabilityResponse>
                        response.data?.let { res ->
                            appointmentViewModel.partnerAvailabilityResponse = res

                            res.availableTill?.let {
                                val timeFormat =
                                    if (DateFormat.is24HourFormat(binding.root.context))
                                        "HH:mm"
                                    else "hh:mm aa"

                                val time = getDateInFormat(res.availableTill.getSafe(), getString(R.string.apiDateTimeFormat), timeFormat)

                                mBinding.apply {
                                    tvAvailableTime.text = langData?.taskScreens?.availableNowEnabledTime?.replace("[0]", time)
                                    tvAvailableTime.visible()
                                }
                            } ?: kotlin.run {
                                mBinding.tvAvailableTime.gone()
                            }

                            mBinding.apply {
                                sbAvailable.isChecked = res.availableTill != null
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
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}