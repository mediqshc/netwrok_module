package com.homemedics.app.ui.fragment.tasks

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.contains
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.AppointmentStatusRequest
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.request.video.TokenRequest
import com.fatron.network_module.models.response.AppointmentStatusResponse
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.AppointmentResponse
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.video.VideoTokenResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.*
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.activity.ChatActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.adapter.CheckoutSplitAmountAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.utils.TimeAgo.timeLeft
import com.homemedics.app.viewmodel.CallViewModel
import com.homemedics.app.viewmodel.EMRViewModel
import com.homemedics.app.viewmodel.TaskAppointmentsViewModel
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


private const val DATE_FORMAT = "dd MMMM, yyyy"

class AppointmentsDetailFragment : BaseFragment(), View.OnClickListener {
    private val taskAppointmentsViewModel: TaskAppointmentsViewModel by activityViewModels()
    private val callViewModel: CallViewModel by activityViewModels()
    private val emrViewModel: EMRViewModel by activityViewModels()
    private var itemsAdapter = AddMultipleViewAdapter()
    private var medItemsAdapter = AddMultipleViewAdapter()
    private var  isCurrencyEmpty = true
    private var dateTimeSelected = false
    var runonly = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val startIntentSender =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_CANCELED) {

            } else if (it.resultCode == Activity.RESULT_OK) {

                requestPermissions()
            }
        }
    private var calculatedDist: Float? = null
    private val permissionsResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { success ->
            if (success.isEmpty().not()) {
                if (success.containsValue(false)) {
                    displayNeverAskAgainDialog()
                } else {
                    fusedLocationClient =
                        LocationServices.getFusedLocationProviderClient(requireActivity())
                    requestLocation()
                }
            }
        }
    private var length = 0
    private var player: MediaPlayer? = null
    private lateinit var dialogViewBinding: FragmentAddVoicenoteBinding
    private lateinit var mBinding: FragmentAppointmentsDetailBinding
    private lateinit var dialogCompleteAppointmentBinding: DialogCompleteAppointmentBinding
    private lateinit var dialogAcceptAppointmentBinding: DialogAcceptAppointmentBinding
    private lateinit var dialogRejectAppointmentBinding: DialogRejectAppointmentBinding
    private lateinit var dialogStartAppointmentBinding: DialogStartJourneyBinding
    private lateinit var dialogRescheduleAppointmentBinding: DialogAcceptAppointmentBinding
    private lateinit var builder: AlertDialog
    private lateinit var dialogSaveButton: Button
    private var isComplete = false
    private var doctorLatLng: Location? = null
    private var patientLatLng: Location? = null
    private var locale: String? = null

    private var isCallAppointment = false
    private lateinit var permissionUtils: PermissionUtils

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
        }
    }

    override fun init() {
        locale =
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        permissionUtils = PermissionUtils(this)
        emrViewModel.vitals = arrayListOf() // flush vitals for create emr
        mBinding.bStart.apply {
            isEnabled = false
            isClickable = false
        }

        taskAppointmentsViewModel.fromDetail = true

        if (taskAppointmentsViewModel.partnerServiceId == CustomServiceTypeView.ServiceType.HomeVisit.id ||
            taskAppointmentsViewModel.partnerServiceId == CustomServiceTypeView.ServiceType.HealthCare.id
        ) {
            checkPermission()
            enableLocationSettings()
//            requestLocation()
        }
        callGetApptDetail()
        callGetAttachments()
        doctorMedicalTask()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("booking_id", taskAppointmentsViewModel.bookingId)
        outState.putInt("partner_service_id", taskAppointmentsViewModel.partnerServiceId)
        outState.putInt("duty_id", taskAppointmentsViewModel.dutyId.getSafe())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            taskAppointmentsViewModel.bookingId = savedInstanceState.getString("booking_id", "0")
            taskAppointmentsViewModel.partnerServiceId =
                savedInstanceState.getInt("partner_service_id", 0)
            taskAppointmentsViewModel.dutyId = savedInstanceState.getInt("duty_id", 0)
            callGetApptDetail()
            callGetAttachments()
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_appointments_detail

    override fun getViewModel() {
    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAppointmentsDetailBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = { findNavController().popBackStack() }
            bAccept.setOnClickListener(this@AppointmentsDetailFragment)
            bReject.setOnClickListener(this@AppointmentsDetailFragment)
            gpLocation.referencedIds.forEach { id ->
                gpLocation.rootView.findViewById<View>(id).setOnClickListener {
                    if (mBinding.dataManager?.patientLocation != null) {
                        val gmmIntentUri =
                            Uri.parse("geo:${mBinding.dataManager?.patientLocation?.lat},${mBinding.dataManager?.patientLocation?.long}?q=${mBinding.dataManager?.patientLocation?.lat},${mBinding.dataManager?.patientLocation?.long}")
//                    val gmmIntentUri =
//                        "http://maps.google.com/maps?q=" + mBinding.dataManager?.patientLocation?.lat + "," + mBinding.dataManager?.patientLocation?.long + "(" + mBinding.dataManager?.patientLocation?.street + ")&iwloc=A&hl=es"
//                    val mapIntent = Intent(Intent.ACTION_VIEW,Uri.parse( gmmIntentUri))
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        mapIntent.resolveActivity(requireActivity().packageManager)?.let {
                            startActivity(mapIntent)
                        }
                    }
                }
            }
            medItemsAdapter.onItemClick = { item, _ ->
                emrViewModel.apply {
                    emrID = item.itemId?.toInt().getSafe()
                    isPatient = true
                }
                findNavController().safeNavigate(
                    AppointmentsDetailFragmentDirections.actionAppointmentsDetailFragmentToCustomerConsultationRecordDetailsFragment2()
                )
            }
            tvAdd.setOnClickListener(this@AppointmentsDetailFragment)
            bStart.setOnClickListener(this@AppointmentsDetailFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bStart -> {
                mBinding.apply {
                    if (bStart.isClickable && bStart.isEnabled) {
                        if (mBinding.dataManager?.serviceTypeId?.toInt()
                                .getSafe() == CustomServiceTypeView.ServiceType.HomeVisit.id || DataCenter.getUser()
                                .isMedicalStaff()
                        ) {
                            showStartAppointmentDialog()
                        } else if (
                            mBinding.dataManager?.serviceTypeId?.toInt()
                                .getSafe() == CustomServiceTypeView.ServiceType.Message.id
                        ) {
                            if (taskAppointmentsViewModel.appointmentResponse?.bookingStatusId != Enums.AppointmentStatusType.COMPLETE.key && taskAppointmentsViewModel.appointmentResponse?.bookingStatusId != Enums.AppointmentStatusType.CANCEL.key) {
                                val data = findOrderStatus(Enums.AppointmentStatusType.START.value)
                                changeStatusApiCall(
                                    AppointmentStatusRequest(
                                        bookingId = taskAppointmentsViewModel.bookingId,
                                        action = data?.genericItemName,
                                        dutyId = if (DataCenter.getUser()
                                                .isDoctor()
                                        ) null else taskAppointmentsViewModel.dutyId
                                    )
                                )
                            }
                            if (taskAppointmentsViewModel.appointmentResponse?.bookingStatusId != Enums.AppointmentStatusType.CANCEL.key) {
                                val messageIntent =
                                    Intent(requireActivity(), ChatActivity::class.java)
                                messageIntent.putExtra(
                                    "bookingId",
                                    taskAppointmentsViewModel.bookingId
                                )
                                messageIntent.putExtra(
                                    Constants.SID,
                                    taskAppointmentsViewModel.appointmentResponse?.chatProperties?.twilioChannelServiceId
                                )
                                startActivity(messageIntent)
                            }
                        } else {
                            val data = findOrderStatus(Enums.AppointmentStatusType.START.value)
                            changeStatusApiCall(
                                AppointmentStatusRequest(
                                    bookingId = taskAppointmentsViewModel.bookingId,
                                    action = data?.genericItemName,
                                    dutyId = if (DataCenter.getUser()
                                            .isDoctor()
                                    ) null else taskAppointmentsViewModel.dutyId
                                )
                            )
                        }
                    }
                }
            }
            R.id.bAccept -> {
                mBinding.apply {
                    if (isComplete )
                        showCompleteAppointmentDialog()
                    else
                        showAcceptAppointmentDialog()
                }
            }
            R.id.bReject -> rejectAndRescheduledToggle()
            R.id.tvAdd -> {
                emrViewModel.bookingId = taskAppointmentsViewModel.bookingId.toInt()
                emrViewModel.isDraft = true
                emrViewModel.partnerServiceId =
                    taskAppointmentsViewModel.partnerServiceId.toString()
                findNavController().safeNavigate(R.id.action_appointmentsDetailFragment_to_medical_records_navigation)
            }
        }
    }

    private fun getMedicalRecords(data: AppointmentResponse) {
        val list = arrayListOf<MultipleViewItem>()
        list.clear()
        data.medicalRecord?.forEach { medicalRecords ->
            val dateStamp: String = getDateInFormat(
                medicalRecords.date.getSafe(), "yyyy-MM-dd", "dd MMM yyyy"
            )
            list.apply {
                add(
                    MultipleViewItem(
                        title = medicalRecords.emrName,
                        itemId = medicalRecords.emrId.toString(),
                        desc = "${Constants.END}$dateStamp ${Constants.START}${Constants.END} ${Constants.PIPE} ${mBinding.langData?.emrScreens?.record.getSafe()} ${Constants.HASH} ${Constants.START}${medicalRecords.emrNumber}${Constants.END} ${Constants.START}",
                        drawable = R.drawable.ic_medical_rec,
                    ).apply {
                        itemEndIcon = R.drawable.ic_expand_more
                    }
                )
            }
        }
        mBinding.rvMedicalRecords.adapter = medItemsAdapter
        medItemsAdapter.listItems = list
        mBinding.tvNoDataMedicalRecords.setVisible(medItemsAdapter.listItems.isEmpty())
    }

    private fun doctorMedicalTask() {
        if (DataCenter.getUser().isMedicalStaff()) {
            mBinding.apply {
                ivStart.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_duo
                    )
                )
                bottomActionGroup.setVisible(false)
                medicalActionGroup.setVisible(true)
            }
        } else {
            mBinding.apply {
                ivStart.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_map
                    )
                )
                medicalActionGroup.setVisible(false)
                bottomActionGroup.setVisible(true)
            }
        }
    }

    private fun rejectAndRescheduledToggle() {
        if (mBinding.bAccept.isEnabled && mBinding.dataManager?.serviceTypeId?.toInt()
                .getSafe() == CustomServiceTypeView.ServiceType.HomeVisit.id && mBinding.bReject.text.toString() == mBinding.langData?.taskScreens?.reject.getSafe()
        )
            showRejectAppointmentDialog()
        else
            showRescheduleAppointmentDialog()
    }

    private fun startButtonToggle(isShow: Boolean) {
        mBinding.apply {
            bStart.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isShow) R.color.orange else R.color.disabled_orange
                    )
                )
                isClickable = isShow
                isEnabled = isShow
            }
        }
    }

    private fun showHideBottomViews(isShow: Boolean) {
        mBinding.apply {
            serviceActionGroup.setVisible(isShow)
            grpActions.setVisible(isShow)
            medicalActionGroup.setVisible(isShow)
            tvBottomMsg.setVisible(isShow.not())
        }
    }

    private fun showCompleteAppointmentDialog() {
        var completeReasonId = "0"
        var checked = 0

        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogCompleteAppointmentBinding =
                DialogCompleteAppointmentBinding.inflate(layoutInflater)
            setView(dialogCompleteAppointmentBinding.root)
            setTitle(mBinding.langData?.taskScreens?.consultation.getSafe())
            setPositiveButton(mBinding.langData?.globalString?.save.getSafe()) { _, _ ->

                builder.dismiss()
            }

            setCancelable(false)

            setNegativeButton(mBinding.langData?.globalString?.cancel.getSafe(), null)
            val languageData = mBinding.langData
            dialogCompleteAppointmentBinding.apply {


                cdStatus.hint = languageData?.globalString?.status.getSafe()
                etFollowUpDate.hint = languageData?.globalString?.followupDate.getSafe()
                tvDesc.text = languageData?.taskScreens?.consultationDesc.getSafe()

                if (mBinding.dataManager?.serviceTypeId?.toInt()
                        .getSafe() == CustomServiceTypeView.ServiceType.HomeVisit.id || DataCenter.getUser()
                        .isMedicalStaff()
                ) {

                    if (mBinding.currency.isNullOrEmpty()) {
                        sbAmountCollected.gone()
                        isCurrencyEmpty=true


                    }

                    else
                    {
                        sbAmountCollected.visible()
                        isCurrencyEmpty=false



                    }



                    tvAmountCollected.text =
                        if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${mBinding.dataManager?.paymentBreakdown?.total.getSafe()} ${mBinding.langData?.taskScreens?.amount} ${mBinding.currency}"
                        else "${mBinding.currency} ${mBinding.dataManager?.paymentBreakdown?.total.getSafe()} ${mBinding.langData?.taskScreens?.amount}"

                    sbAmountCollected.setOnCheckedChangeListener { _, isChecked ->
                             var isChecke=isChecked
                        if (isChecke && etFollowUpDate.text.isNotEmpty() && etFollowUpDate.text!=" ")
                        {

                            checked = 1
                            dialogSaveButton.isEnabled = true
                            isCurrencyEmpty=false
                            cdStatus.selectionIndex = 0
                            dateTimeSelected=false




                        }
                         else {
                            dialogSaveButton.isEnabled = false
                            etFollowUpDate.text = " "
                            isCurrencyEmpty=false
                            cdStatus.selectionIndex=0
                            cdStatus.hint=languageData?.globalString?.status.getSafe()
                            dateTimeSelected=false
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message ="kindly enter followup date",

                                    buttonCallback = {},
                                )
                            if (etFollowUpDate.isEmpty()){
                                sbAmountCollected.isChecked=false
                                dialogSaveButton.isEnabled=false
                            }

                        }





                    }


                } else {
                    llPayment.setVisible(false)

                }
                val list = arrayListOf<String>()
                metaData?.bookingCompletedStatuses?.map {
                    list.add(it.genericItemName.getSafe())
                }


                etFollowUpDate.apply {
                    clickCallback = {
                        textColorCheck = true
                        openCalender(mBinding.editText, format = DATE_FORMAT, false, true)
                    }
                    mBinding.editText.doAfterTextChanged {

                        if(!isCurrencyEmpty)
                        {
                                dialogSaveButton.isEnabled = false
                                sbAmountCollected.isChecked = false
                                dateTimeSelected=true




                        }
                        else
                        {
                            dialogSaveButton.isEnabled=true
                            cdStatus.selectionIndex=0
                        }


                    }
                }

                cdStatus.apply {
                    data = list
                    onItemSelectedListener = { _, position ->
                        completeReasonId =
                            metaData?.bookingCompletedStatuses?.get(position)?.genericItemId.toString()
                            sbAmountCollected.isChecked = false

                    }
                }

            }
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {

                val data = metaData?.orderStatuses?.find {
                    it.genericItemName?.lowercase()
                        ?.contains(Enums.AppointmentStatusType.COMPLETE.value).getSafe()
                }
                var followupDate: String? = null
                followupDate = getLongDateFromString(
                    dialogCompleteAppointmentBinding.etFollowUpDate.text,
                    DATE_FORMAT,
                    "5"
                ).toString()
                if (followupDate == "0")
                    followupDate = null
                changeStatusApiCall(
                    AppointmentStatusRequest(
                        bookingId = taskAppointmentsViewModel.bookingId,
                        action = data?.genericItemName,
                        followupDate = followupDate,
                        feeCollected = checked,
                        bookingCompletedReasonsId = completeReasonId,
                        dutyId = if (DataCenter.getUser()
                                .isDoctor()
                        ) null else taskAppointmentsViewModel.dutyId
                    )
                )

            }
            dialogSaveButton.isEnabled = false
        }
        builder.show()
    }

    private fun isValidCompleteData(): Boolean {
        return isValid(dialogCompleteAppointmentBinding.cdStatus.mBinding.dropdownMenu.text.toString())
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        val cancel = CancellationTokenSource().token

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancel
        ).addOnSuccessListener(requireActivity()) { location ->
            Timber.e("success")
            if (location != null) {
                doctorLatLng = Location("")
                doctorLatLng?.latitude = location.latitude
                doctorLatLng?.longitude = location.longitude
                calculateDist()
            }
        }.addOnCompleteListener {

        }
    }

    private fun calculateDist() {
        patientLatLng?.let {
            calculatedDist = (doctorLatLng?.distanceTo(it))?.div(1000)
        }
    }

    private fun showStartAppointmentDialog() {
        var selectedTimeId = "0"

        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogStartAppointmentBinding = DialogStartJourneyBinding.inflate(layoutInflater)
            setView(dialogStartAppointmentBinding.root)

            setTitle(mBinding.langData?.userAuthScreen?.startJourney.getSafe())
            setPositiveButton(mBinding.langData?.globalString?.btnContinue.getSafe()) { _, _ ->
                builder.dismiss()
            }
            var descText = mBinding.langData?.taskScreens?.timeIntervalDesc
            if (calculatedDist != null && calculatedDist != 0f) {
                descText = mBinding.langData?.taskScreens?.journeyDES?.replace(
                    "[0]",
                    String.format("%.2f", calculatedDist)
                )
            } else
                calculateDist()
            setCancelable(false)
            setNegativeButton(mBinding.langData?.globalString?.cancel.getSafe(), null)
            val minutes = mBinding.langData?.globalString?.minutes.getSafe()
            dialogStartAppointmentBinding.cdTime.hint =
                mBinding.langData?.globalString?.time.getSafe()
            dialogStartAppointmentBinding.apply {


                tvDesc.text = descText
                cdTime.apply {
                    val list = arrayListOf<String>()
                    metaData?.appointmentStartTime?.map {
                        list.add(it.genericItemName.getSafe() + ' ' + minutes)
                    }
                    data = list

                    if (list.isNotEmpty()) {
                        selectionIndex = 0
                        selectedTimeId =
                            metaData?.appointmentStartTime?.get(selectionIndex)?.genericItemId.toString()
                                .getSafe()
                    }

                    onItemSelectedListener = { _, pos ->
                        selectedTimeId =
                            metaData?.appointmentStartTime?.get(pos)?.genericItemId.toString()
                                .getSafe()
                        dialogSaveButton.isEnabled
                        isValid(cdTime.mBinding.dropdownMenu.text.toString())
                    }
                }
            }
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val data = findOrderStatus(Enums.AppointmentStatusType.START.value)
                changeStatusApiCall(
                    AppointmentStatusRequest(
                        bookingId = taskAppointmentsViewModel.bookingId,
                        action = data?.genericItemName,
                        journeyTimeId = selectedTimeId,
                        dutyId = if (DataCenter.getUser()
                                .isDoctor()
                        ) null else taskAppointmentsViewModel.dutyId
                    )
                )

            }
            dialogSaveButton.isEnabled = selectedTimeId != "0"
        }
        builder.show()
    }

    private fun findOrderStatus(value: String) = metaData?.orderStatuses?.find {
        it.genericItemName?.lowercase()
            ?.contains(value).getSafe()
    }

    private fun showAcceptAppointmentDialog() {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogAcceptAppointmentBinding =
                DialogAcceptAppointmentBinding.inflate(layoutInflater).apply {
                    tvDesc.text = mBinding.langData?.dialogsStrings?.acceptHomeRequest.getSafe()
                }
            setView(dialogAcceptAppointmentBinding.root)
            setTitle(mBinding.langData?.taskScreens?.acceptAppointment.getSafe())
            setPositiveButton(mBinding.langData?.globalString?.yes.getSafe()) { _, _ ->
                builder.dismiss()
            }
            setNegativeButton(mBinding.langData?.globalString?.cancel.getSafe(), null)
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val data = metaData?.orderStatuses?.find {
                    it.genericItemName?.lowercase() == Enums.AppointmentStatusType.CONFIRM.value
                }
                changeStatusApiCall(
                    AppointmentStatusRequest(
                        bookingId = taskAppointmentsViewModel.bookingId,
                        action = data?.genericItemName,
                        dutyId = if (DataCenter.getUser()
                                .isDoctor()
                        ) null else taskAppointmentsViewModel.dutyId
                    )
                )
            }
        }
        builder.show()
    }

    private fun showRejectAppointmentDialog() {
        var reasonId = "0"
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogRejectAppointmentBinding = DialogRejectAppointmentBinding.inflate(layoutInflater)
            setView(dialogRejectAppointmentBinding.root)
            setTitle(mBinding.langData?.taskScreens?.rejectAppointment.getSafe())
            setPositiveButton(mBinding.langData?.globalString?.yes.getSafe()) { _, _ ->
                builder.dismiss()
            }
            setNegativeButton(mBinding.langData?.globalString?.cancel.getSafe(), null)
            setCancelable(false)
            val langData = mBinding.langData
            dialogRejectAppointmentBinding.apply {
                tvDesc.text = langData?.taskScreens?.rejectDesc
                cdReason.hint = langData?.taskScreens?.reason.getSafe()
                val list = arrayListOf<String>()
                metaData?.bookingRejectedStatuses?.map {
                    list.add(it.genericItemName.getSafe())
                }
                cdReason.data = list
                cdReason.onItemSelectedListener = { _, position ->
                    reasonId =
                        metaData?.bookingRejectedStatuses?.get(position)?.genericItemId.toString()
                    dialogSaveButton.isEnabled = cdReason.mBinding.dropdownMenu.text.isNotEmpty()
                }
            }
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val data = metaData?.orderStatuses?.find {
                    it.genericItemName?.lowercase()
                        ?.contains(Enums.AppointmentStatusType.REJECT.value).getSafe()
                }
                changeStatusApiCall(
                    AppointmentStatusRequest(
                        bookingId = taskAppointmentsViewModel.bookingId,
                        action = data?.genericItemName,
                        bookingCompletedReasonsId = reasonId,
                        dutyId = if (DataCenter.getUser()
                                .isDoctor()
                        ) null else taskAppointmentsViewModel.dutyId
                    )
                )

            }
            dialogSaveButton.isEnabled = false
        }
        builder.show()
    }

    private fun showRescheduleAppointmentDialog() {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogRescheduleAppointmentBinding =
                DialogAcceptAppointmentBinding.inflate(layoutInflater).apply {
                    tvDesc.text = mBinding.langData?.dialogsStrings?.rescheduleRequest.getSafe()
                }
            setView(dialogRescheduleAppointmentBinding.root)
            setTitle(mBinding.langData?.taskScreens?.rescheduleAppointment.getSafe())
            setPositiveButton(mBinding.langData?.globalString?.yes.getSafe()) { _, _ ->
                builder.dismiss()
            }
            setNegativeButton(mBinding.langData?.globalString?.cancel.getSafe(), null)
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val data = metaData?.orderStatuses?.find {
                    it.genericItemName?.lowercase()
                        ?.contains(Enums.AppointmentStatusType.RESCHEDULING.value).getSafe()
                }
                changeStatusApiCall(
                    AppointmentStatusRequest(
                        bookingId = taskAppointmentsViewModel.bookingId,
                        action = data?.genericItemName,
                        dutyId = if (DataCenter.getUser()
                                .isDoctor()
                        ) null else taskAppointmentsViewModel.dutyId
                    )
                )
            }
        }
        builder.show()
    }

    private fun callGetApptDetail() {
        if (isOnline(requireActivity())) {
            taskAppointmentsViewModel.callGetApptDetail(
                AppointmentDetailReq(
                    bookingId = taskAppointmentsViewModel.bookingId,
                    partnerServiceId = taskAppointmentsViewModel.partnerServiceId,
                    dutyId = if (DataCenter.getUser()
                            .isDoctor()
                    ) null else taskAppointmentsViewModel.dutyId
                )
            ).observe(viewLifecycleOwner) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<AppointmentResponse>
                        val data = response.data as AppointmentResponse

                        setData(data)
                        getMedicalRecords(data)

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
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun callGetAttachments() {
        if (isOnline(requireActivity())) {
            taskAppointmentsViewModel.callGetAttachments(
                AppointmentDetailReq(
                    bookingId = taskAppointmentsViewModel.bookingId,
                    dutyId = taskAppointmentsViewModel.dutyId
                )
            )
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<AttachmentResponse>
                            val data = response.data?.attachments
                            setAttachments(data)
                            taskAppointmentsViewModel.appointmentAttachments =
                                data as ArrayList<Attachment>?
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
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun changeStatusApiCall(request: AppointmentStatusRequest) {
        if (isOnline(requireActivity())) {
            taskAppointmentsViewModel.changeStatusApiCall(request)
                .observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<AppointmentStatusResponse>
                            if (response.data?.appointmentStatusChanged.getSafe()) {

                                callGetApptDetail()
//                                changeValues(request.action,  "${mBinding.currency} ${request.feeCollected}" )
                                if (::builder.isInitialized)
                                    builder.dismiss()

                                val serviceId =
                                    taskAppointmentsViewModel.appointmentResponse?.serviceTypeId
                                if (CustomServiceTypeView.ServiceType.getServiceById(
                                        serviceId?.toInt().getSafe()
                                    ) == CustomServiceTypeView.ServiceType.VideoCall
                                    && request.action == Enums.AppointmentStatusType.START.value
                                ) {
                                    val tokenRequest =
                                        TokenRequest(bookingId = taskAppointmentsViewModel.bookingId.toInt())

                                    if (permissionUtils.hasPermissions(
                                            CallActivity.callPermissionArray
                                        )
                                    ) {
                                        getVideoCallToken(tokenRequest)
                                    } else {
                                        permissionUtils.requestPermissions(
                                            CallActivity.callPermissionArray
                                        )
                                            .setMessageOnPermanentDenial(mBinding.langData?.globalString?.cameraPermissions.getSafe())
                                            .onPermissionResult = object : PermissionUtils.
                                        OnPermissionResult {
                                            override fun onPermissionGranted() {
                                                getVideoCallToken(tokenRequest)
                                            }

                                            override fun onPermissionDenied() {}
                                        }
                                    }
                                }

                                if (CustomServiceTypeView.ServiceType.getServiceById(
                                        serviceId?.toInt().getSafe()
                                    ) == CustomServiceTypeView.ServiceType.HealthCare
                                    && request.action == Enums.AppointmentStatusType.START.value
                                ) {
                                    mBinding.apply {
                                        bAccept.apply {
                                            text =
                                                mBinding.langData?.taskScreens?.markComplete.getSafe()
                                            isEnabled = true
                                        }
                                    }
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
                    }
                }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getVideoCallToken(request: TokenRequest) {
        callViewModel.getTwilioVideoCallToken(request)
            .observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<VideoTokenResponse>
                        response.data?.let { videoCall ->
                            val intent = Intent(requireContext(), CallActivity::class.java).apply {
                                putExtra("token", videoCall.token)
                                putExtra("room_name", videoCall.roomName)
                                putExtra("sid", videoCall.sid)
                                putExtra(
                                    "task_details",
                                    taskAppointmentsViewModel.appointmentResponse
                                )
                                putExtra(
                                    "attachments",
                                    taskAppointmentsViewModel.appointmentAttachments
                                )
                                putExtra("booking_id", taskAppointmentsViewModel.bookingId)
                                putExtra("order_id", videoCall.order?.id)
                            }
                            startActivity(intent)
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
    }

    private fun changeValues(request: Int?, amount: String? = null) {
        when {
            request == Enums.AppointmentStatusType.CONFIRM.key && mBinding.dataManager?.serviceTypeId?.toInt()
                .getSafe() == CustomServiceTypeView.ServiceType.HomeVisit.id -> {
                mBinding.apply {
                    startButtonToggle(true)
                    bAccept.apply {
                        text = mBinding.langData?.taskScreens?.markComplete.getSafe()
                        isEnabled = false
                    }
                    bReject.apply {
                        text = mBinding.langData?.taskScreens?.reshedule.getSafe()
                    }

                }
            }
            request == Enums.AppointmentStatusType.START.key.getSafe() -> {
                startFlowButtons(true)
                startButtonToggle(isCallAppointment)

            }
            request == Enums.AppointmentStatusType.RESCHEDULING.key.getSafe() -> {
                startButtonToggle(false)
                showHideBottomViews(false)
                mBinding.tvBottomMsg.text =
                    amount?.let {
                        mBinding.langData?.taskScreens?.rescheduleEnd.getSafe().replace("[0]", it)
                    }

            }
            request == Enums.AppointmentStatusType.REJECT.key.getSafe() -> {
                showHideBottomViews(false)
                mBinding.tvBottomMsg.text = mBinding.langData?.taskScreens?.rejectedEnd.getSafe()

            }
            request == Enums.AppointmentStatusType.CANCEL.key.getSafe() -> {
                showHideBottomViews(false)
                mBinding.tvBottomMsg.text = mBinding.langData?.taskScreens?.cancelMsg

            }
            request == Enums.AppointmentStatusType.COMPLETE.key.getSafe() -> {
                startButtonToggle(false)
                showHideBottomViews(false)

                var msg = mBinding.langData?.taskScreens?.completedEnd.getSafe()
                if (mBinding.dataManager?.serviceType?.lowercase()
                        ?.contains(
                            mBinding.langData?.globalString?.homeVisit.getSafe().lowercase()
                        ).getSafe()
                )
                    msg = if (mBinding.dataManager?.feeCollected.getBoolean()) amount?.let {
                        mBinding.langData?.taskScreens?.completeMsg.getSafe().replace("[0]", it)
                    }.getSafe() else mBinding.langData?.taskScreens?.completedEnd.getSafe()
                mBinding.tvBottomMsg.text = msg


            }
        }

    }

    private fun startFlowButtons(isEnable: Boolean) {
        mBinding.apply {
            bAccept.isEnabled = isEnable
            bAccept.apply {
                text = mBinding.langData?.taskScreens?.markComplete.getSafe()
            }
            if (DataCenter.getUser().isMedicalStaff())
                bReject.gone()

            bReject.apply {
                text = mBinding.langData?.taskScreens?.reshedule.getSafe()
            }
        }
        isComplete = true

    }

    private fun setData(data: AppointmentResponse) {

        isCallAppointment =
            data.serviceTypeId?.toInt() == CustomServiceTypeView.ServiceType.VideoCall.id
        val currencyData = metaData?.currencies?.find { it.genericItemId == data.currencyId }

        val isDoctor = DataCenter.getUser().isDoctor()
        mBinding.dataManager = data
        taskAppointmentsViewModel.appointmentResponse = mBinding.dataManager
        mBinding.actionbar.title = "\u2066# ${data.uniqueIdentificationNumber}\u2069"
        mBinding.currency = currencyData?.genericItemName.getSafe()
        mBinding.ivThumbnail.loadImage(
            data.patientDetails?.profilePicture,
            getGenderIcon(data.patientDetails?.genderId.toString())
        )
        calculateAge(data)
        changeValues(data.bookingStatusId, "${mBinding.currency} ${data.fee}")
        emrViewModel.customerId = data.bookedForUser?.id.toString()

        val bookingStatus =
            getLabelsFromId(data.bookingStatusId.toString(), metaData?.orderStatuses)
        mBinding.actionbar.desc = bookingStatus.getSafe().firstCap()
        if (isDoctor) {
            mBinding.tvAdd.setVisible(
                data.bookingStatusId == Enums.AppointmentStatusType.START.key ||
                        data.bookingStatusId == Enums.AppointmentStatusType.COMPLETE.key
            )

            mBinding.tvService.text =
                metaData?.partnerServiceType?.find { it.id == data.partnerServiceId }?.name

            if (data.serviceTypeId?.toInt()
                    .getSafe() == CustomServiceTypeView.ServiceType.VideoCall.id
            ) {
//            if (data.serviceType?.lowercase()?.contains("video").getSafe()) {
                val currentFormat = getString(R.string.timeFormat24)
                val timeFormat =
                    if (DateFormat.is24HourFormat(requireContext())) getString(R.string.timeFormat24) else getString(
                        R.string.timeFormat12
                    )

                var formattedStartDate = ""
                var formattedEndDate = ""
                if (data.startTime.isNullOrEmpty().not() && data.endTime.isNullOrEmpty().not()) {
                    formattedStartDate = getDateInFormat(
                        StringBuilder(data.startTime).insert(2, ":").toString(), currentFormat,
                        timeFormat
                    )

                    formattedEndDate = getDateInFormat(
                        StringBuilder(data.endTime).insert(2, ":").toString(),
                        currentFormat,
                        timeFormat
                    )
                }

                val param = mBinding.bStart.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(0, 0, 0, 0)
                mBinding.apply {
                    bStart.layoutParams = param
                    gpLocation.setVisible(false)
                    val fee = if (data.fee?.contains("Free", true)
                            .getSafe()
                    ) langData?.globalString?.free.getSafe() else data.fee.getSafe()
                    val date = getDateInFormat(
                        data.bookingDateOriginal.getSafe(),
                        getString(R.string.apiDateTimeFormat),
                        getString(
                            R.string.dateFormat2
                        )
                    )
                    tvDetail.text =
                        if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "$date ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END} ${mBinding.langData?.globalString?.to.getSafe()} ${Constants.START}$formattedEndDate${Constants.END} ${Constants.PIPE} $fee ${currency.getSafe()}" else "$date ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END} ${mBinding.langData?.globalString?.to.getSafe()} ${Constants.START}$formattedEndDate${Constants.END} ${Constants.PIPE}  ${currency.getSafe()} $fee"

                    ivStart.setImageResource(R.drawable.ic_duo)
                    val service = metaData?.doctorServices?.find {
                        it.itemId?.toInt() == data.serviceTypeId?.toInt().getSafe()
                    }
                    tvAmountCollected.text = if (service?.genericItemName?.contains("Free", true)
                            .getSafe()
                    ) langData?.globalString?.free else service?.genericItemName
                    tvTotalAmount.text = getAmount(
                        data.paymentBreakdown?.paymentCollected.getSafe(),
                        mBinding.currency
                    )
                }

                val isRescheduled =
                    data.bookingStatusId == Enums.AppointmentStatusType.RESCHEDULING.key
                val isCanceled = data.bookingStatusId == Enums.AppointmentStatusType.CANCEL.key
                val isCompleted = data.bookingStatusId == Enums.AppointmentStatusType.COMPLETE.key
                val isStarted = data.bookingStatusId == Enums.AppointmentStatusType.START.key

                startFlowButtons(isStarted)
                startButtonToggle(isRescheduled.not() && isCanceled.not() && isCompleted.not())
            } else if (mBinding.dataManager?.serviceTypeId?.toInt()
                    .getSafe() == CustomServiceTypeView.ServiceType.HomeVisit.id
            ) { //home

                calculateShowAddress(data)

                mBinding.tvDetail.text =
                    "${Constants.START}${data.bookingDate}${Constants.END} ${Constants.PIPE} ${data.shift?.shift} ${Constants.PIPE} ${
                        getAmount(
                            data.fee,
                            mBinding.currency
                        )
                    }"
                mBinding.tvTotalAmount.text =
                    getAmount(data.paymentBreakdown?.total.getSafe(), mBinding.currency)
            } else if (
                data.serviceTypeId?.toInt()
                    .getSafe() == CustomServiceTypeView.ServiceType.Message.id
            ) {
                val isRescheduled =
                    data.bookingStatusId == Enums.AppointmentStatusType.RESCHEDULING.key
                val isCanceled = data.bookingStatusId == Enums.AppointmentStatusType.CANCEL.key
                val isConfirm =
                    data.bookingStatusId == Enums.AppointmentStatusType.CONFIRM.key || data.bookingStatusId == Enums.AppointmentStatusType.START.key
                val isCompleted = data.bookingStatusId == Enums.AppointmentStatusType.COMPLETE.key

                val param = mBinding.bStart.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(0, 0, 0, 0)

                mBinding.apply {
                    bStart.layoutParams = param
                    startButtonToggle((isRescheduled.not() && isCanceled.not()) || isCompleted || isConfirm)
                    val time = timeLeft(
                        data.timeLeft,
                        mBinding.langData?.chatScreen
                    )

                    tvDetail.text =
                        "${Constants.START}${data.bookingDate}${Constants.END} ${Constants.PIPE} ${Constants.START} $time ${Constants.END} ${Constants.PIPE} ${
                            getAmount(
                                data.fee,
                                currency
                            )
                        }${Constants.END}"
                    ivStart.setImageResource(R.drawable.ic_chat_white)
                    val service = metaData?.doctorServices?.find {
                        it.itemId?.toInt() == data.serviceTypeId?.toInt().getSafe()
                    }
                    tvAmountCollected.text = service?.genericItemName

                    tvTotalAmount.text =
                        getAmount(data.paymentBreakdown?.paymentCollected.getSafe(), currency)
//                    val msgStatus =
//                        metaData?.consultationMessagesStatuses?.find { it.genericItemId!=2 && it.genericItemId == data.chatSessionStatus?.toInt()}?.label
                    val msgStatus =
                        if (data.chatSessionStatus?.toInt() == Enums.SessionStatuses.INITIATED.key) mBinding.langData?.globalString?.start.getSafe() else mBinding.langData?.taskScreens?.sendMessage.getSafe()
                    tvStart.text = msgStatus
                    // hide address
                    gpLocation.gone()
                    serviceActionGroup.gone()
                    grpActions.gone()
                    medicalActionGroup.gone()
                    tvOrderDetails.visible()
                    bottomActionGroup.visible()
                }
            }
        } else { //for medical staff
            val currentFormat = getString(R.string.timeFormat24)
            val timeFormat =
                if (DateFormat.is24HourFormat(requireContext())) getString(R.string.timeFormat24) else getString(
                    R.string.timeFormat12
                )

            var formattedStartDate = ""
            var formattedEndDate = ""
            if (data.startTime.isNullOrEmpty().not() && data.endTime.isNullOrEmpty().not()) {
                formattedStartDate = getDateInFormat(
                    StringBuilder(data.startTime).insert(2, ":").toString(), currentFormat,
                    timeFormat
                )

                formattedEndDate = getDateInFormat(
                    StringBuilder(data.endTime).insert(2, ":").toString(),
                    currentFormat,
                    timeFormat
                )
            }

            val dutyStatus =
                metaData?.dutyStatuses?.find { it.genericItemId == data.bookingStatusId }
            val speciality =
                metaData?.specialties?.medicalStaffSpecialties?.find { it.genericItemId == data.specialityId?.toInt() }?.genericItemName

            mBinding.apply {
                tvAdd.setVisible(
                    data.bookingStatusId == Enums.DutyStatusType.STARTED.key ||
                            data.bookingStatusId == Enums.DutyStatusType.COMPLETED.key
                )
            }

            mBinding.actionbar.desc = dutyStatus?.label.getSafe()
            calculateShowAddress(data)
            mBinding.tvService.text = speciality.getSafe()
            mBinding.tvDetail.text =
                "${Constants.START}${data.bookingDate}${Constants.END} ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END} ${mBinding.langData?.globalString?.to.getSafe()} ${Constants.START}$formattedEndDate${Constants.END} ${Constants.PIPE} ${
                    getAmount(
                        data.fee,
                        mBinding.currency
                    )
                }"

            mBinding.ivStart.setImageResource(R.drawable.ic_map)
            setPaymentData(data)
            startButtonToggle(true)
            mBinding.apply {
                val serviceId =
                    taskAppointmentsViewModel.appointmentResponse?.serviceTypeId
                bAccept.apply {
                    text = mBinding.langData?.taskScreens?.markComplete
                    isEnabled = CustomServiceTypeView.ServiceType.getServiceById(
                        serviceId?.toInt().getSafe()
                    ) == CustomServiceTypeView.ServiceType.HealthCare
                }
                bReject.gone()
                isComplete = true
            }

            when (dutyStatus?.genericItemId) {
                Enums.DutyStatusType.PENDING.key -> {
                    mBinding.apply {
                        startButtonToggle(true)
                        bReject.gone()
                        bAccept.apply {
                            visible()
                            isEnabled = false
                        }
                    }
                }
                Enums.DutyStatusType.STARTED.key -> {
                    mBinding.apply {
                        startButtonToggle(false)
                        showHideBottomViews(true)
                        tvAmountCollected.gone()
                        tvTotalAmount.gone()
                        bReject.gone()
                        bAccept.apply {
                            visible()
                            isEnabled = true
                        }
                    }
                }
                Enums.DutyStatusType.COMPLETED.key -> {
                    mBinding.apply {
                        startButtonToggle(false)
                        bAccept.gone()
                        bReject.gone()
                        tvBottomMsg.apply {
                            visible()
                            text =
                                if (data.feeCollected.getBoolean()) mBinding.langData?.taskScreens?.medicalStaffCompleteMsg?.replace(
                                    "[0]",
                                    data.fee.toString()
                                ) else mBinding.langData?.taskScreens?.completedEndMedicalStaff
                        }
                    }
                }
                Enums.DutyStatusType.CANCELLED.key -> {
                    mBinding.apply {
                        startButtonToggle(false)
                        bAccept.gone()
                        bReject.gone()
                        tvBottomMsg.apply {
                            visible()
                            text = mBinding.langData?.taskScreens?.canceledEnd
                        }
                    }
                }
            }

        }
        mBinding.executePendingBindings()

    }

    private fun getAmount(fee: String?, currency: String?): CharSequence {
        return if (fee?.contains("Free", true).getSafe())
            mBinding.langData?.globalString?.free.getSafe()
        else if (fee?.contains("Amount unavailable", true).getSafe())
            mBinding.langData?.globalString?.amountUnavailable.getSafe()
        else if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)
            "$fee $currency"
        else
            "${Constants.START}$currency $fee${Constants.END}"
    }

    private fun setPaymentData(data: AppointmentResponse) {

        val splitAmountAdapter = CheckoutSplitAmountAdapter().apply {
            currency = mBinding.currency
        }
        val breakDown = data.paymentBreakdown
        val list = arrayListOf<SplitAmount>()
        breakDown?.duties?.let { list.addAll(it) }

        list.apply {

            if (breakDown?.corporateDiscount != null)
                add(
                    SplitAmount(
                        specialityName = mBinding.langData?.globalString?.corporateDiscount,
                        fee = breakDown.corporateDiscount.toString()
                    )
                )

            if (breakDown?.promoDiscount != null)
                add(
                    SplitAmount(
                        specialityName = mBinding.langData?.globalString?.promoDiscount,
                        fee = breakDown.promoDiscount.toString()
                    )
                )

            if (breakDown?.companyCredit != null)
                add(
                    SplitAmount(
                        specialityName = mBinding.langData?.globalString?.companyDiscount,
                        fee = breakDown.companyCredit.toString()
                    )
                )
        }

        mBinding.apply {
            tvPayableAmount.text = getAmount(breakDown?.total.getSafe(), mBinding.currency)
            tvVisit.text = mBinding.langData?.taskScreens?.totalAmount.getSafe()
            tvAmount.text = if (breakDown?.paidInAdvance != null) getAmount(
                breakDown.paidInAdvance.getSafe(),
                mBinding.currency
            ) else "0 ${mBinding.currency}"
            tvVisitAmount.text = getAmount(breakDown?.paymentCollected.getSafe(), mBinding.currency)
        }

        splitAmountAdapter.listItems = list

        mBinding.apply {
            rvSplitAmount.apply {
                if (data.bookingStatusId == Enums.AppointmentStatusType.RESCHEDULING.key || data.bookingStatusId == Enums.AppointmentStatusType.COMPLETE.key)
                    gone()
                else
                    visible()
                adapter = splitAmountAdapter
            }
            tvPaymentMethod.text = mBinding.langData?.globalString?.total.getSafe()
        }
    }

    private fun calculateShowAddress(data: AppointmentResponse) {
        if (data.patientLocation != null)
            data.patientLocation?.apply {
                patientLatLng = Location("")
                patientLatLng?.latitude = this.lat?.toDouble() ?: 0.0
                patientLatLng?.longitude = this.long?.toDouble() ?: 0.0
            }
        calculateDist()
        if (data.bookingStatusId == Enums.AppointmentStatusType.CONFIRM.key) {
            startFlowButtons(data.start.getSafe().not())
            startButtonToggle(data.start.getSafe())
        }
        mBinding.tvAmountCollected.text =
            mBinding.langData?.taskScreens?.totalAmount
    }

    private fun calculateAge(data: AppointmentResponse) {
        val cal: Calendar = Calendar.getInstance(Locale.US)
        val format = SimpleDateFormat("yyyy-MM-dd")
        if (data.patientDetails?.dateOfBirth?.getSafe()?.isNotEmpty().getSafe())
            format.parse(data.patientDetails?.dateOfBirth.getSafe())?.let { cal.setTime(it) }
        val today = Calendar.getInstance(Locale.US)

        val time: Long = (today.time.time / 1000) - (cal.time.time / 1000)

        val years: Long = Math.round(time.toDouble()) / 31536000
        val months: Long = Math.round(time.toDouble() - years * 31536000) / 2628000

        val monthsToShow =
            if (months > 0) ", $months ${mBinding.langData?.globalString?.monthhs}"
            else ""
        val gender = metaData?.genders?.find { it.itemId?.toInt() == data.patientDetails?.genderId }
            ?: metaData?.genders?.get(0)

        if (years.toInt() == 0 && months.toInt() == 0) {
            mBinding.tvDescription.text =
                "\u2069${gender?.genericItemName} | ${mBinding.langData?.taskScreens?.lessThanOneYear}\u2066"
        } else if (years > 0) {
            var year = mBinding.langData?.globalString?.year
            if (years.toInt() > 1)
                year = mBinding.langData?.globalString?.years
            mBinding.tvDescription.text =
                "\u2066${gender?.genericItemName}\u2069\u2069 | $years $year $monthsToShow\u2066"
        } else {
            mBinding.tvDescription.text =
                "\u2066${gender?.genericItemName} \u2069\u2069 | $months ${
                    mBinding.langData?.globalString?.monthhs
                }\u2066"
        }
    }

    private fun setAttachments(attachments: List<Attachment>?) {
        if (attachments?.isEmpty().getSafe()) {
            mBinding.apply {
                tvAttachments.setVisible(false)
                rvAttachments.setVisible(false)
                vShadow4.setVisible(false)
            }
        } else {
            mBinding.apply {
                tvAttachments.setVisible(true)
                rvAttachments.setVisible(true)
                vShadow4.setVisible(true)
            }
            mBinding.rvAttachments.adapter = itemsAdapter

            itemsAdapter.onEditItemCall = { item ->
                when (item.drawable) {
                    R.drawable.ic_upload_file -> {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.itemId))
                        startActivity(browserIntent)
                    }
                    R.drawable.ic_image -> {
                        showImageDialog(item.itemId)
                    }

                    else -> {
                        /*if (Patterns.WEB_URL.matcher(item.itemId).matches()) {
                            showVoiceNoteDialog(item.itemId,true)
                        } else {*/
                            showVoiceNoteDialog(item.itemId,false)
                       // }
                    }
                }
            }
            val list: ArrayList<MultipleViewItem> = arrayListOf()
            attachments?.forEach {
                var drawable: Int = 0

                when (it.attachmentTypeId) {
                    Enums.AttachmentType.DOC.key -> {
                        drawable = R.drawable.ic_upload_file

                    }
                    Enums.AttachmentType.IMAGE.key -> {
                        drawable = R.drawable.ic_image

                    }
                    else -> {
                        drawable = R.drawable.ic_play_arrow

                    }
                }
                val index = it.attachments?.file?.lastIndexOf('/')
                val listTitle =
                    if (index == -1) it.attachments?.file else it.attachments?.file?.substring(
                        index.getSafe() + 1,
                        it.attachments?.file?.length.getSafe()
                    )

                val locale =
                    TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
                list.add(
                    MultipleViewItem(
                        title = listTitle,
                        itemId = it.attachments?.file,
                        drawable = drawable,
                    ).apply {
                        itemEndIcon = R.drawable.ic_expand_more
                        if (it.attachmentTypeId == Enums.AttachmentType.VOICE.key) itemCenterIcon =
                            R.drawable.ic_voice_group

                        isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                    }
                )


            }

            itemsAdapter.listItems = list
        }
    }



    private fun showImageDialog(itemId: String?) {
        val displayRectangle = Rect()
        val window: Window = requireActivity().getWindow()
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        val builder =
            AlertDialog.Builder(requireContext())
        val viewGroup: ViewGroup = requireActivity().findViewById(android.R.id.content)
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_view_image, viewGroup, false)

        builder.setView(dialogView)


        val ivBack = dialogView.findViewById<ImageView>(R.id.ivBack)

        val ivApptImage = dialogView.findViewById<ImageView>(R.id.ivApptImage)
        ivApptImage.layoutParams.width = (resources.displayMetrics.widthPixels * 0.80).toInt()
        ivApptImage.layoutParams.height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        Glide.with(requireContext())
            .load(itemId)
            .fitCenter()
            .into(ivApptImage)

        val alertDialog = builder.create()
        alertDialog.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        ivBack.setOnClickListener {
            alertDialog.dismiss()
        }
//        alertDialog.window?.setLayout(width, height)

    }

    private fun showVoiceNoteDialog(itemId: String?, isUrl: Boolean ) {

        val audio = CustomAudio(requireContext())
       /* if (isUrl) {
            audio.apply {
                title = mBinding.langData?.globalString?.voiceNote.getSafe()
                isPlayOnly = true
                url = itemId.getSafe()
                positiveButtonText = mBinding.langData?.globalString?.done.getSafe()
                negativeButtonText = mBinding.langData?.globalString?.cancel.getSafe()
                cancel = mBinding.langData?.globalString?.cancel.getSafe()
                show()

            }
        } else {*/
            audio.apply {
                title = mBinding.langData?.globalString?.voiceNote.getSafe()
                isPlayOnly = true
                fileName = itemId.getSafe()
                positiveButtonText = mBinding.langData?.globalString?.done.getSafe()
                negativeButtonText = mBinding.langData?.globalString?.cancel.getSafe()
                cancel = mBinding.langData?.globalString?.cancel.getSafe()
                show()

          //  }
        }


    }

    private fun checkPermission() {

        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            requestLocation()
        } else {
            requestPermissions()
        }
    }


    private fun requestPermissions() {
        permissionsResultLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun enableLocationSettings() {
        val locationRequest = LocationRequest.create()
            .setInterval(500)
            .setFastestInterval(100)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        LocationServices
            .getSettingsClient(requireContext())
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(requireActivity()) { response: LocationSettingsResponse? -> }
            .addOnFailureListener(requireActivity()) { ex ->
                if (ex is ResolvableApiException) {
                    // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(ex.resolution).build()
                        startIntentSender.launch(intentSenderRequest)

                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }

    private fun displayNeverAskAgainDialog() {
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            message = mBinding.langData?.globalString?.locationPermissions.getSafe(),
            positiveButtonStringText = mBinding.langData?.dialogsStrings?.permitManual.getSafe(),
            negativeButtonStringText = mBinding.langData?.globalString?.close.getSafe(),
            buttonCallback = {
                context?.let { gotoAppSettings(it) }
            },
            negativeButtonCallback = {

            },
            cancellable = false
        )

    }
}