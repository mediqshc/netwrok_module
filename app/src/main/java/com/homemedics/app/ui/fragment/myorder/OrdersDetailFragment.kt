package com.homemedics.app.ui.fragment.myorder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.request.orders.MyOrdersRequest
import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.request.video.TokenRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.checkout.CheckoutDetailResponse
import com.fatron.network_module.models.response.checkout.Promotions
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.meta.PartnerService
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.models.response.pharmacy.Product
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogAddReviewBinding
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.databinding.FragmentOrderDetailsBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.ChatActivity
import com.homemedics.app.ui.activity.CheckoutActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.adapter.CheckoutSplitAmountAdapter
import com.homemedics.app.ui.adapter.PromotionsAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.utils.Constants.ORDER_DETAIL_BOOKING_ID
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import com.homemedics.app.viewmodel.EMRViewModel
import com.homemedics.app.viewmodel.MyOrderViewModel
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class OrdersDetailFragment : BaseFragment(), View.OnClickListener {

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()

    private val ordersViewModel: MyOrderViewModel by activityViewModels()

    private val emrViewModel: EMRViewModel by activityViewModels()

    private lateinit var mBinding: FragmentOrderDetailsBinding

    private lateinit var addReviewBinding: DialogAddReviewBinding

    private lateinit var builder: AlertDialog

    private lateinit var dialogSaveButton: Button

    private lateinit var audio: CustomAudio

    private lateinit var dialogViewBinding: FragmentAddVoicenoteBinding

    private var services: PartnerService? = null

    private var itemsAdapter = AddMultipleViewAdapter()

    private var recordsAdapter = AddMultipleViewAdapter()

    private var player: MediaPlayer? = null

    private var length = 0

    private var bookingStatus: Enums.AppointmentStatusType? = null

    private var rating: String? = null

    private var review: String? = null

    private var currencyData: GenericItem? = null

    private var uniqueId: String? = null

    private var uniqueList = arrayListOf<String>()

    private var language: RemoteConfigLanguage? = null

    private var locale: String? = null

    private lateinit var promoAdapter: PromotionsAdapter

    private var promoPackageList: ArrayList<Promotions>? = null

    val start = "\u2066"
    val end = "\u2069"
    val hash = "\u0023"
    val colon = "\u003A"
    val pipe = "\u007C"

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            language = lang
            caAddresses.title = language?.globalString?.visitAddress.getSafe()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ORDER_DETAIL_BOOKING_ID, ordersViewModel.bookingId.toInt())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            ordersViewModel.bookingId =
                savedInstanceState.getInt(ORDER_DETAIL_BOOKING_ID, 0).toString()
            if (isOnline(requireActivity())) {
                getOrders()
            } else {
                DialogUtils(requireActivity())
                    .showSingleButtonAlertDialog(
                        title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                        message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                        buttonCallback = {},
                    )
            }
        }
    }

    override fun init() {
        audio = CustomAudio(requireContext())
        locale =
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        uniqueList.addAll(tinydb.getListString(Enums.FirstTimeUnique.FIRST_TIME_UNIQUE.key))
        ordersViewModel.page = 1
        ordersViewModel.listItems = arrayListOf()
        val bookingId = arguments?.getInt(Constants.BOOKINGID).getSafe()
        if (bookingId != 0)
            ordersViewModel.bookingId = bookingId.toString()

        if (isOnline(requireActivity())) {
            getOrders()
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }


    }

    override fun getFragmentLayout() = R.layout.fragment_order_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentOrderDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            iDoctor.root.setOnClickListener {
                if (services?.id != CustomServiceTypeView.ServiceType.LaboratoryService.id)
                    findNavController().safeNavigate(R.id.action_ordersDetailFragment_to_docReviewsFragment2)
            }
            recordsAdapter.onItemClick = { item, _ ->
                emrViewModel.apply {
                    emrID = item.itemId?.toInt().getSafe()
                    isPatient = true
                }
                findNavController().safeNavigate(R.id.action_ordersDetailFragment_to_customerConsultationRecordDetailsFragment3)
            }
            caAddresses.onItemClick = { _, _ ->
                navigateToGoogleMap()
            }
            caAddresses.onEditItemCall = {
                navigateToGoogleMap()
            }
            bAddReview.setOnClickListener(this@OrdersDetailFragment)
            bCancelRequest.setOnClickListener(this@OrdersDetailFragment)
            bCancelOrder.setOnClickListener(this@OrdersDetailFragment)
            bReschedule.setOnClickListener(this@OrdersDetailFragment)
            bCancel.setOnClickListener(this@OrdersDetailFragment)
            bPayConfirm.setOnClickListener(this@OrdersDetailFragment)
            tvDesc.setOnClickListener(this@OrdersDetailFragment)
            tvQualification.setOnClickListener(this@OrdersDetailFragment)
            bAddReviewMsg.setOnClickListener(this@OrdersDetailFragment)
            bMessages.setOnClickListener(this@OrdersDetailFragment)
            bMessagesLong.setOnClickListener(this@OrdersDetailFragment)
            bVisitSchedule.setOnClickListener(this@OrdersDetailFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bReschedule -> {
                ordersViewModel.serviceId =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerServiceId.getSafe()
                ordersViewModel.partnerUserId =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerUserId.getSafe()
                findNavController().safeNavigate(
                    OrdersDetailFragmentDirections.actionOrdersDetailFragmentToRescheduleAppointmentFragment()
                )
            }
            R.id.bAddReviewMsg, R.id.bAddReview -> {
                if (services?.id == CustomServiceTypeView.ServiceType.LaboratoryService.id) {
                    showAddReviewDialog(ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.branch?.labs?.name)
                } else {
                    showAddReviewDialog(ordersViewModel.orderDetailsResponse?.partnerDetails?.fullName)
                }
            }
            R.id.bCancelOrder -> {
                DialogUtils(requireActivity())
                    .showDoubleButtonsAlertDialog(
                        title = if (services?.id == CustomServiceTypeView.ServiceType.PharmacyService.id || services?.id == CustomServiceTypeView.ServiceType.LaboratoryService.id)
                            language?.myOrdersScreens?.cancelOrder.getSafe()
                        else
                            language?.myOrdersScreens?.cancelAppointment.getSafe(),
                        message = if (services?.id == CustomServiceTypeView.ServiceType.PharmacyService.id || services?.id == CustomServiceTypeView.ServiceType.LaboratoryService.id)
                            language?.dialogsStrings?.orderDeleteMsg.getSafe()
                        else
                            language?.myOrdersScreens?.cancelationMsg.getSafe(),
                        buttonCallback = { cancelOrder() },
                        positiveButtonStringText = language?.globalString?.yes.getSafe(),
                        negativeButtonStringText = language?.globalString?.goBack.getSafe(),
                        negativeButtonCallback = { }
                    )
            }
            R.id.bCancelRequest, R.id.bCancel -> {
                DialogUtils(requireActivity())
                    .showDoubleButtonsAlertDialog(
                        title = if (services?.id == CustomServiceTypeView.ServiceType.PharmacyService.id || services?.id == CustomServiceTypeView.ServiceType.LaboratoryService.id)
                            language?.myOrdersScreens?.cancelOrder.getSafe()
                        else
                            language?.myOrdersScreens?.cancelAppointment.getSafe(),
                        message = if (services?.id == CustomServiceTypeView.ServiceType.PharmacyService.id || services?.id == CustomServiceTypeView.ServiceType.LaboratoryService.id)
                            language?.dialogsStrings?.orderDeleteMsg.getSafe()
                        else
                            language?.myOrdersScreens?.cancelationMsg.getSafe(),
                        buttonCallback = { cancelOrder() },
                        positiveButtonStringText = language?.globalString?.yes.getSafe(),
                        negativeButtonStringText = language?.globalString?.goBack.getSafe(),
                        negativeButtonCallback = { }
                    )
            }
            R.id.bPayConfirm -> {
                doctorConsultationViewModel.bookConsultationRequest.bookingId =
                    ordersViewModel.bookingId.toInt()
                doctorConsultationViewModel.partnerProfileResponse.fee =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.fee
                doctorConsultationViewModel.partnerProfileResponse.partnerServiceId =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerServiceId

                navigateToCheckout()
            }
            R.id.tvDesc -> {
                doctorConsultationViewModel.bdcFilterRequest.serviceId =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerServiceId
                doctorConsultationViewModel.partnerProfileResponse.partnerUserId =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerUserId

                findNavController().safeNavigate(R.id.action_ordersDetailFragment_to_aboutDoctorFragment2)
            }
            R.id.tvQualification -> {
                findNavController().safeNavigate(R.id.action_ordersDetailFragment_to_docEducationFragment2)
            }
            R.id.bMessages -> {
                val messageIntent = Intent(requireActivity(), ChatActivity::class.java)
                messageIntent.putExtra("fromOrder", true)
                messageIntent.putExtra(
                    Constants.SID,
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.chatProperties?.twilioChannelServiceId
                )
                startActivity(messageIntent)
            }
            R.id.bMessagesLong -> {
                val messageIntent = Intent(requireActivity(), ChatActivity::class.java)
                messageIntent.putExtra("bookingId", ordersViewModel.bookingId)
                messageIntent.putExtra("fromOrder", true)
                messageIntent.putExtra(
                    Constants.SID,
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.chatProperties?.twilioChannelServiceId
                )

                startActivity(messageIntent)
            }
            R.id.bVisitSchedule -> {
                findNavController().safeNavigate(R.id.action_ordersDetailFragment_to_visitSchedulesFragment)
            }
        }
    }

    private fun navigateToGoogleMap() {
        val homeCollection =
            ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.homeCollection.getBoolean()
        val lat = ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.branch?.lat
        val long =
            ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.branch?.long
        if (homeCollection.not()) {
            val gmmIntentUri = Uri.parse("geo:${lat},${long}?q=${lat},${long}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            mapIntent.resolveActivity(requireActivity().packageManager)?.let {
                startActivity(mapIntent)
            }
        }
    }

    private fun getMedicalRecords(data: OrderResponse) {
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
                        desc = "$end${dateStamp}$start$end $pipe ${language?.emrScreens?.record} $hash $start${medicalRecords.emrNumber}$end $start",
                        drawable = R.drawable.ic_medical_rec,
                    ).apply {
                        itemEndIcon = R.drawable.ic_expand_more
                    }
                )
            }
        }
        mBinding.rvMedicalRecords.adapter = recordsAdapter
        recordsAdapter.listItems = list

        if (services?.id != CustomServiceTypeView.ServiceType.PharmacyService.id)
            mBinding.tvNoDataMedicalRecords.setVisible(recordsAdapter.listItems.isEmpty())

    }

    private fun navigateToCheckout() {
        val checkoutIntent = Intent(requireActivity(), CheckoutActivity::class.java)
        checkoutIntent.apply {
            putExtra(
                Enums.BundleKeys.partnerProfileResponse.key,
                Gson().toJson(doctorConsultationViewModel.partnerProfileResponse)
            )
            putExtra(
                Enums.BundleKeys.partnerSlotsResponse.key,
                Gson().toJson(doctorConsultationViewModel.partnerSlotsResponse)
            )
            putExtra(
                Enums.BundleKeys.bookConsultationRequest.key,
                Gson().toJson(doctorConsultationViewModel.bookConsultationRequest)
            )
        }
        startActivity(checkoutIntent)
    }

    private fun setReschedulingViews(isShow: Boolean) {
        val label =
            metaData?.orderStatuses?.find { it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.bookingStatusId }?.label.getSafe()
        mBinding.apply {
            actionbar.desc = label
            tvMsg.text = language?.myOrdersScreens?.rescheduleMsg.getSafe()
            llButtons.setVisible(isShow)
            bAddReview.setVisible(isShow.not())
            bCancelRequest.setVisible(isShow.not())
            llReviews.setVisible(isShow.not())
            tvReviewDesc.setVisible(isShow.not())
            viewBottom.setVisible(isShow.not())
            tvCompleted.setVisible(isShow.not())
            llAdminButtons.setVisible(isShow.not())

            val isLabOrder =
                ordersViewModel.orderDetailsResponse?.bookingDetails?.bookingStatusId == Enums.AppointmentStatusType.RESCHEDULED.key
            if (isLabOrder) { //will change later after client discussion
                llButtons.weightSum = 1f
                bReschedule.gone()
            }
        }
    }

    private fun setCancelOrderViews(isShow: Boolean, isRejected: Boolean = false) {
        val label =
            metaData?.orderStatuses?.find { it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.bookingStatusId }?.label.getSafe()
        mBinding.apply {
            actionbar.desc = label
            tvMsg.apply {
                visible()
                text = if (services?.id == CustomServiceTypeView.ServiceType.PharmacyService.id) {
                    language?.myOrdersScreens?.canceledPharLabOrder.getSafe()
                } else {
                    if (isRejected) language?.myOrdersScreens?.appointmentRejected.getSafe() else language?.myOrdersScreens?.canceled.getSafe()
                }
            }
            llButtons.setVisible(isShow)
            bAddReview.setVisible(isShow)
            bCancelRequest.setVisible(isShow)
            llReviews.setVisible(isShow)
            tvReviewDesc.setVisible(isShow)
            viewBottom.setVisible(isShow)
            tvCompleted.setVisible(isShow)
            llAdminButtons.setVisible(isShow)
        }
    }

    private fun setAddReviewViews(isShow: Boolean) {
        val label =
            metaData?.orderStatuses?.find { it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.bookingStatusId }?.label.getSafe()
        mBinding.apply {
            actionbar.desc = label
            tvMsg.apply {
                visible()
                text = language?.myOrdersScreens?.completed.getSafe()
            }
            llButtons.setVisible(isShow)
            bAddReview.setVisible(isShow.not())
            bCancelRequest.setVisible(isShow)
            llReviews.setVisible(isShow)
            tvReviewDesc.setVisible(isShow)
            viewBottom.setVisible(isShow)
            tvCompleted.setVisible(isShow)
            llAdminButtons.setVisible(isShow)
        }
    }

    private fun setReviewViews(isShow: Boolean, review: String, stars: String) {
        mBinding.apply {
            tvMsg.setVisible(isShow.not())
            llButtons.setVisible(isShow.not())
            bAddReview.setVisible(isShow.not())
            bCancelRequest.setVisible(isShow.not())
            llReviews.setVisible(isShow)
            tvReviewDesc.apply {
                setVisible(isShow)
                text = review
            }
            if (stars > "0.0") {
                tvStars.apply {
                    visible()
                    text =
                        if (stars == "1") "$stars ${language?.myOrdersScreens?.star}" else "$stars ${
                            language?.myOrdersScreens?.stars
                        }"
                }
            } else {
                tvStars.gone()
            }
            viewBottom.setVisible(isShow)
            tvCompleted.setVisible(isShow)
            llAdminButtons.setVisible(isShow.not())
//            if (services?.id == CustomServiceTypeView.ServiceType.Message.id) {
//                bMessagesLong.visible()
//            }
        }
    }

    private fun approvalPending(isShow: Boolean) {
        mBinding.apply {
            tvMsg.apply {
                visible()
                text = language?.myOrdersScreens?.unconfirmedHomeMessage.getSafe()
            }
            bAddReview.setVisible(isShow.not())
            bCancelRequest.setVisible(isShow)
            llButtons.setVisible(isShow.not())
            llReviews.setVisible(isShow.not())
            tvReviewDesc.setVisible(isShow.not())
            viewBottom.setVisible(isShow.not())
            tvCompleted.setVisible(isShow.not())
            llAdminButtons.setVisible(isShow.not())
        }
    }

    private fun confirmationPending(isShow: Boolean) {
        mBinding.apply {
            tvMsg.apply {
                visible()
                text = language?.myOrdersScreens?.orderPendingDesc.getSafe()
            }
            bAddReview.setVisible(isShow)
            bCancelRequest.setVisible(isShow)
            llButtons.setVisible(isShow)
            llReviews.setVisible(isShow)
            tvReviewDesc.setVisible(isShow)
            viewBottom.setVisible(isShow)
            tvCompleted.setVisible(isShow)
            llAdminButtons.setVisible(isShow.not())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getBookingStatus(orderDetailResponse: OrderResponse?) {
        services =
            metaData?.partnerServiceType?.find { it.id == ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerServiceId }
        bookingStatus = Enums.AppointmentStatusType.values()
            .find { it.key == ordersViewModel.orderDetailsResponse?.bookingDetails?.bookingStatusId }
        when (bookingStatus?.key) {
            Enums.AppointmentStatusType.COMPLETE.key -> {
                setAddReviewViews(false)
            }
            Enums.AppointmentStatusType.RESCHEDULED.key -> {
                setReschedulingViews(true)
            }
            Enums.AppointmentStatusType.RESCHEDULING.key -> {
                setReschedulingViews(true)
            }
            Enums.AppointmentStatusType.CANCEL.key -> {
                setCancelOrderViews(false)
            }
            Enums.AppointmentStatusType.REJECT.key -> {
                setCancelOrderViews(false, true)
            }
            Enums.AppointmentStatusType.APPROVALPENDING.key -> {
                approvalPending(true)
            }
        }

        if (bookingStatus?.key == Enums.AppointmentStatusType.COMPLETE.key && ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews != null) {
            setReviewViews(
                true,
                ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews?.review.getSafe(),
                ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews?.rating.toString()
            )
        }

        // home visit
        if (services?.id == CustomServiceTypeView.ServiceType.HomeVisit.id) {
            mBinding.tvFee.text = language?.myOrdersScreens?.homeVisitFee.getSafe()
            when (bookingStatus?.key) {
                Enums.AppointmentStatusType.APPROVALPENDING.key -> {
                    approvalPending(true)
                }
                Enums.AppointmentStatusType.CANCEL.key -> {
                    setCancelOrderViews(false)
                }
                Enums.AppointmentStatusType.CONFIRMATIONPENDING.key -> {
                    confirmationPending(false)
                }
                Enums.AppointmentStatusType.COMPLETE.key -> {
                    mBinding.tvMsg.gone()
                    mBinding.tvCompleted.visible()

                    if (ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews != null) {
                        mBinding.apply {
                            tvCompleted.text =
                                language?.myOrdersScreens?.homeVisitComplete.getSafe()
                        }
                    } else {
                        mBinding.apply {
                            bAddReview.visible()
                            tvCompleted.text = language?.myOrdersScreens?.afterReviewHome.getSafe()
                        }
                        singleTimeShowReviewPopupLocally(ordersViewModel.orderDetailsResponse?.partnerDetails?.fullName)
                    }
                }
            }
            val time =
                metaData?.appointmentStartTime?.find { it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.journeyTimeId }?.genericItemName
            if (bookingStatus?.key == Enums.AppointmentStatusType.START.key && ordersViewModel.orderDetailsResponse?.bookingDetails?.journeyTimeId != null)
                mBinding.tvMsg.text =
                    language?.myOrdersScreens?.doctorOnWay?.replace("[0]", time.getSafe())
                        .getSafe() //getString(R.string.journey_time, time)


            mBinding.apply {
                val dateStamp: String = getDateInFormat(
                    orderDetailResponse?.bookingDetails?.bookingDate.getSafe(),
                    "yyyy-MM-dd hh:mm:ss",
                    "dd MMM yyyy"
                )
                tvServiceDesc.text =
                    "${language?.myOrdersScreens?.order.getSafe()} $hash ${ordersViewModel.orderDetailsResponse?.bookingDetails?.uniqueIdentificationNumber}\n${dateStamp} $pipe ${orderDetailResponse?.bookingDetails?.shift.toString()}"

                val breakDown =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.paymentBreakdown
                val paymentMethod = if (breakDown?.total.getSafe()
                        .isEmpty() || breakDown?.total.getSafe() == "0"
                )
                    "${language?.globalString?.paymentMode} $colon ${language?.globalString?.none}"
                else
                    "${language?.globalString?.paymentMode} $colon ${breakDown?.paymentMethod.toString()}"

                tvPayableAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                        breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                    } ${currencyData?.genericItemName.getSafe()}"
                    else "${currencyData?.genericItemName.getSafe()} ${
                        breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                    }"
                tvPaymentMethod.text = paymentMethod
                tvPayAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                        breakDown?.total?.getCommaRemoved().parseDouble().round(2)
                    } ${currencyData?.genericItemName.getSafe()}" else "${currencyData?.genericItemName.getSafe()} ${
                        breakDown?.total?.getCommaRemoved().parseDouble().round(2)
                    }"
            }
        }

        val splitAmountAdapter = CheckoutSplitAmountAdapter().apply {
            currency = currencyData?.genericItemName.getSafe()
        }
        val breakDown = ordersViewModel.orderDetailsResponse?.bookingDetails?.paymentBreakdown
        val list = arrayListOf<SplitAmount>()

        if (services?.id == CustomServiceTypeView.ServiceType.HealthCare.id) {
            val visitType =
                metaData?.homeServiceVisitTypes?.find { it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.visitType }?.genericItemId

            breakDown?.duties?.map { splitAmount ->
                if (visitType == Enums.HomeHealthcareVisitType.MULTIPLE_VISIT.key) {
                    splitAmount.specialityName =
                        "${Constants.START}${splitAmount.specialityName}${Constants.END} ${Constants.MULTIPLY} ${Constants.START}${splitAmount.daysQuantity} ${language?.globalString?.days}${Constants.END}\n" +
                                "${splitAmount.startDate} ${Constants.MINUS} ${splitAmount.endDate}\n" +
                                "${
                                    splitAmount.days.toString().replace("[", "").replace("]", "")
                                } ${Constants.PIPE} ${splitAmount.startTime} ${Constants.MINUS} ${splitAmount.endTime}"
                }

                splitAmount
            }
            breakDown?.duties?.let { list.addAll(it) }
        }

        list.apply {
            if (breakDown?.corporateDiscount != null)
                add(
                    SplitAmount(
                        specialityName = getString(R.string.corporate_discount),
                        fee = breakDown.corporateDiscount
                    )
                )

            if (breakDown?.promoDiscount != null)
                add(
                    SplitAmount(
                        specialityName = getString(R.string.promo_discount),
                        fee = breakDown.promoDiscount
                    )
                )

            if (breakDown?.companyCredit != null)
                add(
                    SplitAmount(
                        specialityName = getString(R.string.company_credit),
                        fee = breakDown.companyCredit.toString()
                    )
                )
        }

        mBinding.apply {
            val paymentMethod = if (breakDown?.total.getSafe()
                    .isEmpty() || breakDown?.total.getSafe() == "0"
            )
                "${language?.globalString?.paymentMode} $colon ${language?.globalString?.none.getSafe()}"
            else
                "${language?.globalString?.paymentMode} $colon ${breakDown?.paymentMethod.toString()}"

            tvPayableAmount.text =
                if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                    breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                } ${currencyData?.genericItemName.getSafe()}"
                else "${currencyData?.genericItemName.getSafe()} ${
                    breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                }"
            tvPaymentMethod.text = paymentMethod
            tvPayAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                breakDown?.total?.getCommaRemoved().parseDouble().round(2)
            } ${currencyData?.genericItemName.getSafe()}" else "${currencyData?.genericItemName.getSafe()} ${
                breakDown?.total?.getCommaRemoved().parseDouble().round(2)
            }"
        }

        splitAmountAdapter.listItems = list

        mBinding.apply {
            rvSplitAmount.apply {
                visible()
                adapter = splitAmountAdapter
            }
        }

        if (breakDown?.duties?.isEmpty().getSafe()) {
            mBinding.apply {
                rvSplitAmount.gone()
                tvPayableAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN && orderDetailResponse?.bookingDetails?.paymentBreakdown?.subTotal?.contains("Amount unavailable", true).getSafe().not()) {
                        "${
                            orderDetailResponse?.bookingDetails?.paymentBreakdown?.subTotal?.getCommaRemoved()
                                .parseDouble().round(2)
                        } ${currencyData?.genericItemName.getSafe()}"
                    } else if (orderDetailResponse?.bookingDetails?.paymentBreakdown?.subTotal?.contains(
                            "Amount unavailable",
                            true
                        ).getSafe()
                    ) {
                        orderDetailResponse?.bookingDetails?.paymentBreakdown?.subTotal?.getSafe()
                    } else {
                        "${currencyData?.genericItemName.getSafe()} ${
                            orderDetailResponse?.bookingDetails?.paymentBreakdown?.subTotal?.getCommaRemoved()
                                .parseDouble().round(2)
                        }"
                    }
                tvPayAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                        breakDown?.total?.getCommaRemoved().parseDouble().round(2)
                    } ${currencyData?.genericItemName.getSafe()}"
                    else "${currencyData?.genericItemName.getSafe()} ${
                        breakDown?.total?.getCommaRemoved().parseDouble().round(2)
                    }"
            }
        }

        // Home health care
        if (services?.id == CustomServiceTypeView.ServiceType.HealthCare.id) {
            val visitType =
                metaData?.homeServiceVisitTypes?.find { it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.visitType }

            var date = ""
            if (visitType?.genericItemId == Enums.HomeHealthcareVisitType.SINGLE_VISIT.key)
                if (breakDown?.duties.isNullOrEmpty().not())
                    date = "${breakDown?.duties?.get(0)?.startDate} $pipe "

            mBinding.apply {
                iDoctor.root.gone()
                llFees.gone()
                tvDesc.gone()
                view.gone()
                tvQualification.gone()
                vShadow.gone()
                tvServiceDesc.text =
                    "${language?.myOrdersScreens?.order} $hash ${ordersViewModel.orderDetailsResponse?.bookingDetails?.uniqueIdentificationNumber}\n$date${visitType?.genericItemName} $pipe ${
                        getLabelFromId(
                            ordersViewModel.orderDetailsResponse?.bookingDetails?.specialityId.toString(),
                            metaData?.specialties?.medicalStaffSpecialties
                        )
                    }"
            }
            when (bookingStatus?.key) {
                Enums.AppointmentStatusType.CANCEL.key -> {
                    setCancelOrderViews(false)
                    mBinding.apply {
                        rvSplitAmount.visible()
                        llTotalStrip.visible()
                        llAmountStrip.visible()
                        llFees.visible()
                        tvNoDataPayment.gone()
                    }
                }
                Enums.AppointmentStatusType.CONFIRM.key -> {
                    val dutyStatus = Enums.DutyStatusType.values()
                        .find { it.key == ordersViewModel.orderDetailsResponse?.bookingDetails?.dutyStatusId }
                    val bookingDate =
                        ordersViewModel.orderDetailsResponse?.bookingDetails?.bookingDate
                    val nextSession =
                        ordersViewModel.orderDetailsResponse?.bookingDetails?.nextSession
                    val lastSession =
                        ordersViewModel.orderDetailsResponse?.bookingDetails?.lastSession
                    val journeyTimeId = orderDetailResponse?.bookingDetails?.journeyTimeId

                    if (visitType?.genericItemId == Enums.HomeHealthcareVisitType.MULTIPLE_VISIT.key) {
                        if (lastSession.isNullOrEmpty().not() && nextSession.isNullOrEmpty()
                                .not()
                        ) {

                            val sessionDateFormatted =
                                getDateInFormat(nextSession.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")

                            val lastSessionFormatted =
                                getDateInFormat(lastSession.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")

                            val rep1 = language?.myOrdersScreens?.visitCompleteSession?.replace(
                                "[0]",
                                lastSessionFormatted.getSafe()
                            )
                            val rep2 = rep1?.replace("[1]", sessionDateFormatted.getSafe())
                            mBinding.tvMsg.text = rep2
                            //getString(R.string.home_health_complete_desc_visit_with_prev_date, lastSessionFormatted, sessionDateFormatted)
                        } else if (dutyStatus?.key == Enums.DutyStatusType.STARTED.key && journeyTimeId != null) {
                            val time = metaData?.appointmentStartTime?.find {
                                it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.journeyTimeId
                            }?.genericItemName.getSafe()
                            mBinding.tvMsg.text =
                                language?.myOrdersScreens?.medicalStaffOnWay?.replace("[0]", time)
                        } else if (dutyStatus?.key == Enums.DutyStatusType.PENDING.key && nextSession.isNullOrEmpty()
                                .not()
                        ) {

                            val sessionDateFormatted =
                                getDateInFormat(nextSession.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")

                            mBinding.tvMsg.text = language?.myOrdersScreens?.nextSession?.replace(
                                "[0]",
                                sessionDateFormatted
                            )
                        } else {
                            mBinding.tvMsg.text =
                                language?.myOrdersScreens?.confirmedMsgHomecare.getSafe()
                        }
                    } else
                        mBinding.tvMsg.text =
                            language?.myOrdersScreens?.confirmedMsgHomecare.getSafe()

                    mBinding.bVisitSchedule.setVisible(visitType?.genericItemId == Enums.HomeHealthcareVisitType.MULTIPLE_VISIT.key)
                }

                Enums.AppointmentStatusType.COMPLETE.key -> {
                    mBinding.bVisitSchedule.setVisible(visitType?.genericItemId == Enums.HomeHealthcareVisitType.MULTIPLE_VISIT.key)
                    mBinding.tvMsg.gone()
                    mBinding.tvCompleted.visible()

                    if (ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews != null) {
                        mBinding.apply {
                            tvCompleted.text =
                                if (visitType?.genericItemId == Enums.HomeHealthcareVisitType.SINGLE_VISIT.key) language?.myOrdersScreens?.homeVisitComplete.getSafe() else language?.myOrdersScreens?.multiVisitCompletion.getSafe()
                        }
                    } else {
                        mBinding.apply {
                            if (visitType?.genericItemId == Enums.HomeHealthcareVisitType.SINGLE_VISIT.key)
                                bAddReview.visible()

                            tvCompleted.text =
                                if (visitType?.genericItemId == Enums.HomeHealthcareVisitType.SINGLE_VISIT.key) language?.myOrdersScreens?.homeVisitComplete.getSafe() else language?.myOrdersScreens?.multiVisitCompletion.getSafe()
                        }
                        singleTimeShowReviewPopupLocally(ordersViewModel.orderDetailsResponse?.partnerDetails?.fullName)
                    }
                }

                Enums.AppointmentStatusType.START.key -> {
                    var time = "15"
                    if (ordersViewModel.orderDetailsResponse?.bookingDetails?.journeyTimeId != null)
                        time = metaData?.appointmentStartTime?.find {
                            it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.journeyTimeId
                        }?.genericItemName.getSafe()
                    mBinding.tvMsg.text =
                        language?.myOrdersScreens?.medicalStaffOnWay?.replace("[0]", time)
                    mBinding.bVisitSchedule.setVisible(visitType?.genericItemId == Enums.HomeHealthcareVisitType.MULTIPLE_VISIT.key)
                }
                Enums.AppointmentStatusType.CONFIRMATIONPENDING.key -> {
                    confirmationPending(false)
                    mBinding.apply {
                        rvSplitAmount.visible()
                        llTotalStrip.visible()
                        llAmountStrip.visible()
                        llFees.gone()
                        tvPaymentMethod.text = language?.globalString?.payableAmount.getSafe()
                        tvNoDataPayment.gone()
                        tvMsg.text = language?.myOrdersScreens?.approvalPending.getSafe()
                    }
                }
                Enums.AppointmentStatusType.REVIEWPENDING.key -> {
                    approvalPending(true)
                    mBinding.apply {
                        tvMsg.text = language?.myOrdersScreens?.requestPendingHomecare.getSafe()
                        rvSplitAmount.gone()
                        llTotalStrip.gone()
                        llAmountStrip.gone()
                        llFees.gone()
                        tvNoDataPayment.visible()
                    }
                }
            }
        }

        // Labs test
        if (services?.id == CustomServiceTypeView.ServiceType.LaboratoryService.id) {
            val labList = arrayListOf<LabTestResponse>()
            breakDown?.labs?.map { lab ->
                lab.labTest?.genericItemName = lab.labTest?.genericItemName.getSafe()
                lab
            }
            breakDown?.labs?.let { labList.addAll(it) }

            val splitAmount = arrayListOf<SplitAmount>()
            labList.forEach { lab ->
                splitAmount.add(
                    SplitAmount(
                        specialityName = lab.labTest?.genericItemName,
                        fee = lab.totalFee?.getCommaRemoved().parseDouble()?.round(2)
                    )
                )
            }

            splitAmount.apply {
                if (breakDown?.corporateDiscount != null)
                    add(
                        SplitAmount(
                            specialityName = getString(R.string.corporate_discount),
                            fee = "${currencyData?.genericItemName.getSafe()} ${breakDown.corporateDiscount.toString()}"
                        )
                    )

                if (breakDown?.promoDiscount != null)
                    add(
                        SplitAmount(
                            specialityName = getString(R.string.promo_discount),
                            fee = "${currencyData?.genericItemName.getSafe()} ${breakDown.promoDiscount.toString()}"
                        )
                    )

                if (breakDown?.companyCredit != null)
                    add(
                        SplitAmount(
                            specialityName = getString(R.string.company_credit),
                            fee = "${currencyData?.genericItemName.getSafe()} ${breakDown.companyCredit.toString()}"
                        )
                    )
            }

            mBinding.apply {
                tvSpecialInstructionDesc.text =
                    if (orderDetailResponse?.bookingDetails?.instructions.isNullOrEmpty().not())
                        orderDetailResponse?.bookingDetails?.instructions
                    else
                        language?.globalString?.noInstructions.getSafe()

                val paymentMethod = if (breakDown?.total.getSafe()
                        .isEmpty() || breakDown?.total.getSafe() == "0"
                )
                    "${language?.globalString?.paymentMode.getSafe()} $colon ${language?.globalString?.none.getSafe()}"
                else
                    "${language?.globalString?.paymentMode.getSafe()} $colon ${breakDown?.paymentMethod.toString()}"

                tvPayableAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                        breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                    } ${currencyData?.genericItemName.getSafe()}"
                    else "${currencyData?.genericItemName.getSafe()} ${
                        breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                    }"
                tvPaymentMethod.text = paymentMethod
                tvPayAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                        breakDown?.total?.getCommaRemoved().parseDouble().round(2)
                    } ${currencyData?.genericItemName.getSafe()}" else "${currencyData?.genericItemName.getSafe()} ${
                        breakDown?.total?.getCommaRemoved().parseDouble().round(2)
                    }"
                iDoctor.tvYearsExp.gone()
            }

            splitAmountAdapter.listItems = splitAmount

            val homeCollection =
                ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.homeCollection
            mBinding.apply {
                iDoctor.root.gone()
                tvDesc.gone()
                view.gone()
                tvQualification.gone()
                vShadow.gone()
                tvNoDataMedicalRecords.gone()
                vAttachments.setVisible(
                    ordersViewModel.orderDetailsResponse?.medicalRecord.isNullOrEmpty().not()
                )
                tvMedicalRecords.setVisible(
                    ordersViewModel.orderDetailsResponse?.medicalRecord.isNullOrEmpty().not()
                )
                rvMedicalRecords.setVisible(
                    ordersViewModel.orderDetailsResponse?.medicalRecord.isNullOrEmpty().not()
                )
                llGeneric.gone()
                llFees.gone()
                llPharmacy.root.gone()
                caAddresses.visible()
                if (!homeCollection.getBoolean() && homeCollection != null) {
                    caAddresses.title = language?.myOrdersScreens?.labAddress.getSafe()
                }
                val prescriptionLab =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.branch
                if (prescriptionLab != null) {
                    iDoctor.apply {
                        root.visible()
                        tvAmount.gone()
                        tvNoRating.gone()
                        ratingBar.gone()
                        ivDrImage.invisible()
                        tvReview.visible()
                        ivLab.visible()
                        ivLab.loadImage(
                            ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.branch?.labs?.iconUrl,
                            R.drawable.ic_launcher_foreground
                        )
                        tvName.text =
                            ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.branch?.labs?.name
                        val homeSample =
                            if (homeCollection.getBoolean() && homeCollection != null) language?.myOrdersScreens?.homeCollection.getSafe() else ""
                        tvExperience.maxLines = 10 //using single line elsewhere
                        var expText =
                            "${language?.myOrdersScreens?.order.getSafe()} $hash ${ordersViewModel.orderDetailsResponse?.bookingDetails?.uniqueIdentificationNumber} $pipe $start${
                                orderDetailResponse?.bookingDetails?.bookingDate?.let { bookingDate ->
                                    getDateInFormat(
                                        bookingDate, "yyyy-MM-dd hh:mm:ss", "dd MMM yyyy"
                                    )
                                }
                            } $end"
                        if (homeSample.isEmpty().not())
                            expText += " $pipe $homeSample"
                        tvExperience.text = expText

                        tvReview.text =
                            "${currencyData?.genericItemName.getSafe()} ${breakDown?.total}"
                        tvReview.paintFlags = 0
                    }
                } else {
                    llPharmacy.root.visible()
                    llPharmacy.ivIcon.loadImage(R.drawable.ic_lab_test)
                    val timeFormat = if (DateFormat.is24HourFormat(getAppContext()))
                        getString(R.string.timeFormat24) else getString(R.string.timeFormat12)
                    val dateStamp: String = getDateInFormat(
                        orderDetailResponse?.bookingDetails?.bookingDate.getSafe(),
                        "yyyy-MM-dd hh:mm:ss",
                        "dd MMM yyyy"
                    )
                    llPharmacy.tvServiceDescription.text =
                        "${language?.myOrdersScreens?.order.getSafe()} $hash ${ordersViewModel.orderDetailsResponse?.bookingDetails?.uniqueIdentificationNumber}\n${
                            dateStamp
                        }"
//                            " $pipe $start$timeStamp$end"
                }
                tvAttachments.apply {
                    visible()
                    text = language?.globalString?.prescription.getSafe()
                }
                tvNoData.text = language?.globalString?.noAttachments.getSafe()
                rvSplitAmount.apply {
                    visible()
                    adapter = splitAmountAdapter
                }

                if (bookingStatus?.key != Enums.AppointmentStatusType.REVIEWPENDING.key ||
                    bookingStatus?.key != Enums.AppointmentStatusType.APPROVALPENDING.key
                ) {
                    if (breakDown?.sampleCharges.isNullOrEmpty()
                            .not() && breakDown?.sampleCharges.getCommaRemoved().toInt() > 0
                    ) {
                        mBinding.apply {
                            iExtraAmount.apply {
                                root.visible()
                                tvTitle.text =
                                    language?.checkoutScreen?.homeCollectionCharges.getSafe()
                                tvAmount.text =
                                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                                        breakDown?.sampleCharges.getCommaRemoved()?.parseDouble()
                                            ?.round(2).getSafe()
                                    } $currency" else "$currency ${
                                        breakDown?.sampleCharges.getCommaRemoved()?.parseDouble()
                                            ?.round(2).getSafe()
                                    }"
                            }
                        }
                    }
                }
            }

            when (bookingStatus?.key) {
                Enums.AppointmentStatusType.REVIEWPENDING.key, Enums.AppointmentStatusType.APPROVALPENDING.key -> {
                    approvalPending(true)
                    mBinding.apply {
                        iDoctor.tvAmount.gone()
                        iDoctor.tvReview.gone()
                        tvNoDataMedicalRecords.gone()
                        tvNoDataPayment.visible()
                        llPharmacy.tvPricePharma.gone()
                        llFees.gone()
                        caAddresses.gone()
                        vAddress.gone()
                        rvSplitAmount.gone()
                        llTotalStrip.gone()
                        llAmountStrip.gone()
                        tvMsg.text = language?.myOrdersScreens?.requestPendingHomecare.getSafe()
                    }
                }
                Enums.AppointmentStatusType.CONFIRM.key -> {
                    mBinding.iDoctor.root.visible()
                    mBinding.llPharmacy.tvPricePharma.visible()
                    mBinding.tvMsg.text =
                        if (homeCollection.getBoolean()) language?.myOrdersScreens?.labHomeConfirm.getSafe() else language?.myOrdersScreens?.labVisitConfirm.getSafe()
                }
                Enums.AppointmentStatusType.SAMPLE_COLLECTED.key -> {
                    mBinding.iDoctor.root.visible()
                    mBinding.llPharmacy.tvPricePharma.visible()
                    mBinding.tvMsg.text = language?.myOrdersScreens?.samplePending.getSafe()
                }
                Enums.AppointmentStatusType.CANCEL.key -> {
                    setCancelOrderViews(false)
                    mBinding.tvMsg.text = language?.myOrdersScreens?.canceledPharLabOrder.getSafe()
                    mBinding.apply {
                        val prescriptionLab =
                            ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.branch
                        if (prescriptionLab == null) {
                            iDoctor.root.gone()
                            caAddresses.gone()
                        }
                        llPharmacy.tvPricePharma.visible()
                        rvSplitAmount.visible()
                        llTotalStrip.visible()
                        llAmountStrip.visible()
                        llFees.gone()
                        tvNoDataPayment.gone()
                    }
                }
                Enums.AppointmentStatusType.CONFIRMATIONPENDING.key -> {
                    confirmationPending(false)
                    mBinding.apply {
                        iDoctor.root.visible()
                        llPharmacy.tvPricePharma.visible()
                        rvSplitAmount.visible()
                        llTotalStrip.visible()
                        llAmountStrip.visible()
                        llFees.gone()
                        tvPaymentMethod.text = language?.globalString?.payableAmount.getSafe()
                        tvNoDataPayment.gone()
                        tvMsg.text = language?.myOrdersScreens?.approvalPending.getSafe()
                    }
                }
                Enums.AppointmentStatusType.RESCHEDULED.key, Enums.AppointmentStatusType.RESCHEDULED.key -> {
                    mBinding.apply {
                        tvMsg.text = language?.myOrdersScreens?.labVisitConfirm?.getSafe()
                        bCancelOrder.gone()
                        bCancel.gone()
                        bCancelRequest.gone()
                    }
                }
                Enums.AppointmentStatusType.COMPLETE.key -> {
                    mBinding.apply {
                        tvNoDataMedicalRecords.setVisible(recordsAdapter.listItems.isEmpty())
                        iDoctor.root.visible()
                        vAttachments.visible()
                        tvMedicalRecords.visible()
                        rvMedicalRecords.visible()
                        llPharmacy.tvPricePharma.visible()
                        bVisitSchedule.visible()
                        llPharmacy.tvPricePharma.visible()
                        tvMsg.gone()
                        bVisitSchedule.gone()

                        if (ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews != null) {
                            mBinding.apply {
                                tvCompleted.apply {
                                    visible()
                                    text = language?.myOrdersScreens?.reviewDone.getSafe()
                                }
                            }
                        } else {
                            mBinding.apply {
                                bAddReview.visible()
                                tvCompleted.apply {
                                    visible()
                                    text = language?.myOrdersScreens?.labFeedback.getSafe()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pharmacy
        if (services?.id == CustomServiceTypeView.ServiceType.PharmacyService.id) {
            val multiply = "\u0078"
            val left = "\u0028"
            val right = "\u0029"
            val pharmacyList = arrayListOf<Product>()
            breakDown?.products?.map { product ->
                product.product?.displayName =
                    "$start${product.product?.displayName.getSafe()} $left${product.product?.packageType?.genericItemName.getSafe()}$right $multiply ${product.quantity.getSafe()}$end"
                product
            }
            breakDown?.products?.let { pharmacyList.addAll(it) }

            val splitAmount = arrayListOf<SplitAmount>()
            pharmacyList.forEach { prod ->
                splitAmount.add(
                    SplitAmount(
                        specialityName = prod.product?.displayName,
                        fee = prod.subtotal?.round(2),
                        isAvailable = prod.isAvailable,
                        isSubstitute = prod.isSubstitute
                    )
                )
            }

            splitAmount.apply {

                if (breakDown?.corporateDiscount != null)
                    add(
                        SplitAmount(
                            specialityName = getString(R.string.corporate_discount),
                            fee = "${currencyData?.genericItemName.getSafe()} ${breakDown.corporateDiscount.toString()}"
                        )
                    )

                if (breakDown?.promoDiscount != null)
                    add(
                        SplitAmount(
                            specialityName = getString(R.string.promo_discount),
                            fee = "${currencyData?.genericItemName.getSafe()} ${breakDown.promoDiscount.toString()}"
                        )
                    )

                if (breakDown?.companyCredit != null)
                    add(
                        SplitAmount(
                            specialityName = getString(R.string.company_credit),
                            fee = "${currencyData?.genericItemName.getSafe()} ${breakDown.companyCredit.toString()}"
                        )
                    )
            }

            mBinding.apply {
                caAddresses.title = language?.labPharmacyScreen?.deliveryAddress.getSafe()

                val paymentMethod = if (breakDown?.total.getSafe()
                        .isEmpty() || breakDown?.total.getSafe() == "0"
                )
                    "${language?.globalString?.paymentMode} $colon ${language?.globalString?.none}"
                else
                    "${language?.globalString?.paymentMode} $colon ${breakDown?.paymentMethod.toString()}"

                tvPayableAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                        breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                    } ${currencyData?.genericItemName.getSafe()}"
                    else "${currencyData?.genericItemName.getSafe()} ${
                        breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                    }"
                tvPaymentMethod.text = paymentMethod
                tvPayAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                        breakDown?.total?.getCommaRemoved().parseDouble().round(2)
                    } ${currencyData?.genericItemName.getSafe()}" else "${currencyData?.genericItemName.getSafe()} ${
                        breakDown?.total?.getCommaRemoved().parseDouble().round(2)
                    }"
            }

            splitAmountAdapter.listItems = splitAmount

            mBinding.apply {
                iDoctor.root.gone()
                tvDesc.gone()
                view.gone()
                tvQualification.gone()
                vShadow.gone()
                tvNoDataMedicalRecords.gone()
                vAttachments.gone()
                tvMedicalRecords.gone()
                rvMedicalRecords.gone()
                llGeneric.gone()
                llFees.gone()
                llPharmacy.root.visible()
                val timeFormat = if (DateFormat.is24HourFormat(getAppContext()))
                    getString(R.string.timeFormat24) else getString(R.string.timeFormat12)
                val dateStamp: String = getDateInFormat(
                    orderDetailResponse?.bookingDetails?.bookingDate.getSafe(),
                    "yyyy-MM-dd hh:mm:ss",
                    "dd MMM yyyy"
                )
                llPharmacy.tvServiceDescription.text =
                    "${language?.myOrdersScreens?.order.getSafe()} $hash ${ordersViewModel.orderDetailsResponse?.bookingDetails?.uniqueIdentificationNumber}\n${
                        dateStamp
                    }"
                tvAttachments.apply {
                    visible()
                    text = language?.globalString?.prescription.getSafe()
                }
                tvNoData.text = language?.globalString?.noAttachments.getSafe()
                rvSplitAmount.apply {
                    visible()
                    adapter = splitAmountAdapter
                }
            }

            when (bookingStatus?.key) {
                Enums.AppointmentStatusType.REVIEWPENDING.key -> {
                    approvalPending(true)
                    mBinding.apply {
                        tvNoDataPayment.visible()
                        llPharmacy.tvPricePharma.gone()
                        llFees.gone()
                        rvSplitAmount.gone()
                        llTotalStrip.gone()
                        llAmountStrip.gone()
                        tvMsg.text = language?.myOrdersScreens?.requestPendingHomecare.getSafe()
                    }
                }
                Enums.AppointmentStatusType.CONFIRM.key -> {
                    mBinding.llPharmacy.tvPricePharma.apply {
                        visible()
                        text =
                            if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${breakDown?.subTotal.getSafe()} ${currencyData?.genericItemName.getSafe()}" else "${currencyData?.genericItemName.getSafe()} ${breakDown?.subTotal.getSafe()}"
                    }
                    mBinding.tvMsg.text = language?.myOrdersScreens?.pharmacyConfirm.getSafe()

                    pharmacyList.forEach { prod ->
                        if (prod.isAvailable == 0 && prod.isSubstitute == 0)
                            mBinding.tvMsg.text = language?.myOrdersScreens?.deliveryAlert.getSafe()
                        else if (prod.isAvailable == 0 && prod.isSubstitute == 1)
                            mBinding.tvMsg.text =
                                language?.myOrdersScreens?.pharmacySubstitude.getSafe()
                    }
                }
                Enums.AppointmentStatusType.CANCEL.key -> {
                    mBinding.llPharmacy.tvPricePharma.apply {
                        visible()
                        text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                            breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                        } ${currencyData?.genericItemName.getSafe()}" else "${currencyData?.genericItemName.getSafe()} ${
                            breakDown?.subTotal?.getCommaRemoved().parseDouble().round(2)
                        }"
                    }
                    setCancelOrderViews(false)
                    mBinding.tvMsg.text = language?.myOrdersScreens?.canceledPharLabOrder.getSafe()
                    mBinding.apply {
                        llPharmacy.tvPricePharma.visible()
                        rvSplitAmount.visible()
                        llTotalStrip.visible()
                        llAmountStrip.visible()
                        llFees.gone()
                        tvNoDataPayment.gone()
                    }
                }
                Enums.AppointmentStatusType.CONFIRMATIONPENDING.key -> {
                    mBinding.llPharmacy.tvPricePharma.apply {
                        visible()
                        text =
                            if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${breakDown?.subTotal.getSafe()} ${currencyData?.genericItemName.getSafe()}" else "${currencyData?.genericItemName.getSafe()} ${breakDown?.subTotal.getSafe()}"
                    }
                    confirmationPending(false)
                    mBinding.apply {
                        llPharmacy.tvPricePharma.visible()
                        rvSplitAmount.visible()
                        llTotalStrip.visible()
                        llAmountStrip.visible()
                        llFees.gone()
                        tvPaymentMethod.text = language?.globalString?.payableAmount.getSafe()
                        tvNoDataPayment.gone()
                        tvMsg.text = language?.myOrdersScreens?.approvalPending.getSafe()
                    }
                }
                Enums.AppointmentStatusType.COMPLETE.key -> {
                    mBinding.llPharmacy.tvPricePharma.apply {
                        visible()
                        text =
                            if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${breakDown?.subTotal.getSafe()} ${currencyData?.genericItemName.getSafe()}" else "${currencyData?.genericItemName.getSafe()} ${breakDown?.subTotal.getSafe()}"
                    }
                    mBinding.tvMsg.gone()
                    if (ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews != null) {
                        mBinding.apply {
                            tvCompleted.apply {
                                visible()
                                text = language?.myOrdersScreens?.pharmacyCompleteFeedback.getSafe()
                            }
                        }
                    } else {
                        mBinding.apply {
                            bAddReview.visible()
                            tvCompleted.apply {
                                visible()
                                text =
                                    language?.myOrdersScreens?.pharmacyOrderDeliveredFeedback.getSafe()
                            }
                        }
                    }
                }
            }
        }

        //messages
        if (services?.id == CustomServiceTypeView.ServiceType.Message.id) {
            val bookingDetails = ordersViewModel.orderDetailsResponse?.bookingDetails
            val isOrderOwner = (DataCenter.getUser()?.id == bookingDetails?.bookedForUser?.id)

            val sessionStatus =
                ordersViewModel.orderDetailsResponse?.bookingDetails?.chatSessionStatus?.toInt()
            val msgStatus =
                metaData?.consultationMessagesStatuses?.find { it.genericItemId == sessionStatus }?.genericItemId
            mBinding.apply {
                if (bookingStatus?.key == Enums.AppointmentStatusType.COMPLETE.key || msgStatus == Enums.SessionStatuses.ENDED.key) {
                    llMessageButtons.gone()
                    bMessagesLong.gone()

                    if (bookingStatus?.key == Enums.AppointmentStatusType.COMPLETE.key && ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews == null) {
                        llMessageButtons.setVisible(isOrderOwner)
                        bAddReview.setVisible(isOrderOwner.not())

                        tvMsg.text = language?.myOrdersScreens?.messageAfterCompltion.getSafe()
                    } else if (ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews != null) {
                        setReviewViews(
                            true,
                            ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews?.review.getSafe(),
                            ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews?.rating.toString()
                        )

                        tvMsg.text = language?.myOrdersScreens?.messageFeedback.getSafe()
                        tvCompleted.text = language?.myOrdersScreens?.messageFeedback.getSafe()
                    } else {
                        tvMsg.text = language?.myOrdersScreens?.messageAfterCompltion.getSafe()
                    }

                } else if (bookingStatus?.key == Enums.AppointmentStatusType.CANCEL.key) {
                    bMessagesLong.gone()
                    tvMsg.text = language?.myOrdersScreens?.canceled.getSafe()
                } else {
                    bMessagesLong.setVisible(isOrderOwner)
                    tvMsg.text =
                        if (isOrderOwner)
                            language?.myOrdersScreens?.messageConsultationMsg.getSafe()
                        else
                            language?.myOrdersScreens?.messageConsultationMsgOther.getSafe()
                }

                if (ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews != null) {
                    setReviewViews(
                        true,
                        ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews?.review.getSafe(),
                        ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews?.rating.toString()
                    )
                }
            }
        }

//        val freeOrZero = orderDetailResponse?.bookingDetails?.paymentBreakdown?.total == "0" || orderDetailResponse?.bookingDetails?.paymentBreakdown?.payableAmount == "0" || orderDetailResponse?.bookingDetails?.paymentBreakdown?.payableAmount?.contains("free", true).getSafe()
//        mBinding.llAmountStrip.setVisible(freeOrZero.not())
    }

    private fun populatePromotions(orderResponse: OrderResponse?) {
        promoPackageList = arrayListOf<Promotions>().apply {
            orderResponse?.bookingDetails?.promotions?.let { promo ->
                add(promo)
            }
        }
        promoAdapter.listItems = promoPackageList.getSafe()
        mBinding.rvPromotions.apply {
            setVisible(promoPackageList?.isNotEmpty().getSafe())
            adapter = promoAdapter
        }
    }

    private fun populatePackages(orderResponse: OrderResponse?) {
        promoPackageList?.addAll(orderResponse?.bookingDetails?.packages.getSafe())
        promoAdapter.listItems = promoPackageList.getSafe()
        mBinding.rvPromotions.apply {
            setVisible(promoPackageList?.isNotEmpty().getSafe())
            adapter = promoAdapter
        }
    }

    private fun singleTimeShowReviewPopupLocally(partnerName: String? = null) {
        val match = uniqueList.find { it == uniqueId }
        if (match == null) {
            showAddReviewDialog(partnerName)
            uniqueList.add(uniqueId.getSafe())
            tinydb.putListString(Enums.FirstTimeUnique.FIRST_TIME_UNIQUE.key, uniqueList)
        }
    }

    private fun setPartnerDetails(orderDetailResponse: OrderResponse?) {
        doctorConsultationViewModel.bdcFilterRequest.serviceId =
            orderDetailResponse?.bookingDetails?.partnerServiceId
        doctorConsultationViewModel.partnerProfileResponse.apply {
            partnerUserId = orderDetailResponse?.bookingDetails?.partnerUserId
            profilePicture = orderDetailResponse?.partnerDetails?.profilePicture
            fullName = orderDetailResponse?.partnerDetails?.fullName
            experience = orderDetailResponse?.partnerDetails?.experience
            totalNoOfReviews = orderDetailResponse?.partnerDetails?.totalNoOfReviews
            average_reviews_rating = orderDetailResponse?.partnerDetails?.average_reviews_rating
            specialities = orderDetailResponse?.partnerDetails?.specialities
            educations = orderDetailResponse?.partnerDetails?.educations
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun showAddReviewDialog(partnerName: String?) {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            addReviewBinding = DialogAddReviewBinding.inflate(layoutInflater)
            addReviewBinding.etInstructions.hint = language?.globalString?.feedbackHere.getSafe()
            addReviewBinding.tvDesc.visible()
            addReviewBinding.tvDesc.text = if (partnerName != null)
                language?.dialogsStrings?.callDialogReviewDesc?.replace(
                    "[0]",
                    partnerName.getSafe()
                )
            else language?.myOrdersScreens?.callThankYou.getSafe()
            addReviewBinding.etInstructions.addTextChangedListener {
                val length = it?.length.getSafe()
                addReviewBinding.tvLength.text = getString(R.string.review_length_, length)
            }
            setView(addReviewBinding.root)
            setTitle(language?.myOrdersScreens?.addAReview.getSafe())
            setPositiveButton(language?.globalString?.save.getSafe()) { _, _ ->
                builder.dismiss()
            }
            setNegativeButton(language?.globalString?.cancel.getSafe(), null)
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = builder.getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val review = addReviewBinding.etInstructions.text.toString()
                val rating = addReviewBinding.ratingBar.rating.toDouble()
                if (isValidRating(rating).not()) {
                    showToast(language?.fieldValidationStrings?.ratingValidation.getSafe())
                    return@setOnClickListener
                }
                this.review = review
                this.rating = rating.toString()
                val request = MyOrdersRequest(
                    bookingId = ordersViewModel.bookingId.toInt(),
                    rating = rating,
                    review
                )
                addReview(request)
                builder.dismiss()
            }
        }
        builder.show()
    }

    private fun getOrders() {
        val request = TokenRequest(bookingId = ordersViewModel.bookingId.toInt())
        ordersViewModel.orderDetails(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<OrderResponse>
                    response.data?.let { orderDetailResponse ->
                        uniqueId = orderDetailResponse.bookingDetails?.uniqueIdentificationNumber
                        ordersViewModel.orderDetailsResponse = orderDetailResponse
                        doctorConsultationViewModel.bookConsultationRequest.bookingId =
                            orderDetailResponse.id.getSafe()
                        setData(orderDetailResponse)
                        setPartnerDetails(orderDetailResponse)
                        getMedicalRecords(orderDetailResponse)
                        getBookingStatus(orderDetailResponse)
                        populatePromotions(orderDetailResponse)
                        populatePackages(orderDetailResponse)
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

    @SuppressLint("SetTextI18n")
    private fun setData(orderDetailResponse: OrderResponse) {
        promoAdapter = PromotionsAdapter().apply {
            isConfirmation = true
            currency =
                metaData?.currencies?.find { it.genericItemId == orderDetailResponse.bookingDetails?.currencyId }?.genericItemName.getSafe()
        }
        val total = orderDetailResponse.bookingDetails?.paymentBreakdown?.total.getSafe()
        val label =
            metaData?.orderStatuses?.find { it.genericItemId == orderDetailResponse.bookingDetails?.bookingStatusId }?.label.getSafe()
        bookingStatus = Enums.AppointmentStatusType.values()
            .find { it.key == orderDetailResponse.bookingDetails?.bookingStatusId }
        val doctorService =
            metaData?.partnerServiceType?.find { it.id == orderDetailResponse.bookingDetails?.partnerServiceId }

        mBinding.apply {
            actionbar.apply {
                val partnerTypeId = orderDetailResponse.bookingDetails?.partnerTypeId

                title =
                    if (partnerTypeId == Enums.PartnerType.DOCTOR.key)
                        doctorService?.shortName.getSafe()
                    else
                        doctorService?.name.getSafe()
                desc = label.getSafe()
            }

            mBinding.tvFee.text = "${doctorService?.name} ${language?.globalString?.fee.getSafe()}"
        }

        when (doctorService?.id) {
            CustomServiceTypeView.ServiceType.HomeVisit.id,
            CustomServiceTypeView.ServiceType.HealthCare.id,
            CustomServiceTypeView.ServiceType.PharmacyService.id -> {
                mBinding.apply {
                    caAddresses.visible()
                    vAddress.visible()

                    val userLocation: UserLocation? =
                        ordersViewModel.orderDetailsResponse?.bookingDetails?.bookingAddress
                    val result: ArrayList<MultipleViewItem> = arrayListOf()
                    result.apply {
                        clear()
                        if (userLocation != null) {
                            add(userLocation)
                        }
                    }
                    if (result.isNotEmpty()) {
                        result[0].apply {
                            title = metaData?.locationCategories?.find {
                                it.genericItemId.getSafe()
                                    .toString() == userLocation?.category.getSafe()
                            }?.title
                            drawable = R.drawable.ic_location_pin_black
                            itemEndIcon = 0
                        }
                    }
                    caAddresses.listItems = result
                }
            }
            CustomServiceTypeView.ServiceType.LaboratoryService.id -> {
                mBinding.apply {
                    caAddresses.visible()
                    vAddress.visible()

                    val userLocation: UserLocation? =
                        ordersViewModel.orderDetailsResponse?.bookingDetails?.bookingAddress

                    val branchLocation =
                        ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.branch
                    val homeCollection =
                        ordersViewModel.orderDetailsResponse?.bookingDetails?.homeCollection?.homeCollection.getBoolean()

                    val result: ArrayList<MultipleViewItem> = arrayListOf()
                    result.apply {
                        clear()
                        if (userLocation != null && homeCollection) {
                            add(userLocation)
                        } else {
                            if (branchLocation != null) {
                                caAddresses.title = language?.myOrdersScreens?.labAddress.getSafe()
                                add(branchLocation)
                            }
                        }
                    }
                    if (result.isNotEmpty()) {
                        result[0].apply {
                            title =
                                if (homeCollection.not()) branchLocation?.name.getSafe() else metaData?.locationCategories?.find {
                                    it.genericItemId.getSafe()
                                        .toString() == userLocation?.category.getSafe()
                                }?.title
                            drawable = R.drawable.ic_location_pin_black
                            itemEndIcon =
                                if (homeCollection.not()) R.drawable.ic_arrow_fw_black else 0
                        }
                    }
                    caAddresses.listItems = result
                }
            }
        }

        currencyData =
            metaData?.currencies?.find { it.genericItemId == orderDetailResponse.bookingDetails?.currencyId }
        val desc = orderDetailResponse.partnerDetails?.overview

        mBinding.apply {
            currency = currencyData?.genericItemName.getSafe()

            iDoctor.apply {
                ivOnlineStatus.gone()
                ivDrImage.loadImage(
                    orderDetailResponse.partnerDetails?.profilePicture,
                    getGenderIcon(orderDetailResponse.bookingDetails?.customerUser?.genderId.toString())
                )
                tvName.text = orderDetailResponse.partnerDetails?.fullName
                tvNoRating.text = language?.globalString?.noRating.getSafe()
                setSpecialities(tvExperience, orderDetailResponse)
                setExperience(tvYearsExp, orderDetailResponse)
                tvReview.apply {
                    setVisible(orderDetailResponse.partnerDetails?.totalNoOfReviews != null)

                    val numOfReviews =
                        if (orderDetailResponse.partnerDetails?.totalNoOfReviews.getSafe() == 0) 0 else String.format(
                            "%02d",
                            orderDetailResponse.partnerDetails?.totalNoOfReviews.getSafe()
                        )

                    text =
                        "${language?.globalString?.reviews.getSafe()}($numOfReviews)"
                    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }
                if (orderDetailResponse.partnerDetails?.average_reviews_rating.getSafe() > 0.0) {
                    tvNoRating.gone()
                    ratingBar.apply {
                        visible()
                        rating =
                            orderDetailResponse.partnerDetails?.average_reviews_rating?.toFloat()
                                .getSafe()
                    }
                } else {
                    ratingBar.gone()
                    tvNoRating.visible()
                }
            }

            tvDesc.apply {
                setVisible(
                    orderDetailResponse.partnerDetails?.overview != null && desc?.isNotEmpty()
                        .getSafe()
                )
                text = orderDetailResponse.partnerDetails?.overview.getSafe()
            }

            setEduction(tvQualification, orderDetailResponse)

            setServiceDescription(tvServiceDesc, orderDetailResponse)

            val currency =
                DataCenter.getMeta()?.currencies?.find { it.itemId == orderDetailResponse.bookingDetails?.currencyId.toString() }
            if (currency?.genericItemName != null && orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal != null && orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal != "Free") {
                if (orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal?.contains("Amount unavailable", true).getSafe()) {
                    tvPrice.text = orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getSafe()
                } else {
                tvPrice.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                    orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getCommaRemoved().parseDouble()
                        .round(2)
                } ${currency.genericItemName}"
                else "${currency.genericItemName} ${
                    orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getCommaRemoved().parseDouble()
                        .round(2)
                }"}
            } else {
                tvPrice.text = if (orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal?.contains("Free", true)
                        .getSafe()
                ) lang?.globalString?.free.getSafe() else if (orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal?.contains(
                        "Amount unavailable",
                        true
                    ).getSafe()
                ) lang?.globalString?.amountUnavailable.getSafe() else "${
                    orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getCommaRemoved().parseDouble()
                        ?.round(2)
                }"
            }

            tvSpecialInstructionDesc.text =
                if (orderDetailResponse.bookingDetails?.instructions.isNullOrEmpty().not())
                    orderDetailResponse.bookingDetails?.instructions
                else
                    language?.globalString?.noInstructions.getSafe()

            val isOrderOwner =
                (DataCenter.getUser()?.id == ordersViewModel.orderDetailsResponse?.bookingDetails?.bookedForUser?.id)
            tvBookBy.text =
                "${language?.globalString?.bookedBy.getSafe()} $colon ${orderDetailResponse.bookingDetails?.customerUser?.fullName} $pipe ${language?.globalString?.bookedFor.getSafe()} $colon ${if (isOrderOwner) language?.globalString?.self else orderDetailResponse.bookingDetails?.bookedForUser?.fullName}"

            if (currency?.genericItemName != null && orderDetailResponse.bookingDetails?.paymentBreakdown?.total != null && orderDetailResponse.bookingDetails?.paymentBreakdown?.total != "Free") {
                if (orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal?.contains("Amount unavailable", true).getSafe()) {
                    tvAmount.text = orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getSafe()
                } else {
                    tvAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                    orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getCommaRemoved().parseDouble().round(2)
                } ${currency.genericItemName}"
                else "${currency.genericItemName} ${
                    orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getCommaRemoved().parseDouble().round(2)
                }"
                }
            } else {
                tvAmount.text =
                    if (orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal?.contains("Free", true).getSafe()) {
                        lang?.globalString?.free.getSafe()
                    } else if (orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal?.contains(
                            "Amount unavailable",
                            true
                        ).getSafe()
                    ) {
                        orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getSafe()
                    } else {
                        orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getCommaRemoved().parseDouble()
                            .round(2)
                    }
            }

            if (currency?.genericItemName != null && orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal != null && orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal != "Free") {
                if (orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal?.contains("Amount unavailable", true).getSafe()) {
                    tvPayableAmount.text = orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getSafe()
                } else {
                    tvPayableAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                        orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal?.getCommaRemoved().parseDouble()
                            .round(2)
                    } ${currency.genericItemName}"
                    else "${currency.genericItemName} ${
                        orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getCommaRemoved().parseDouble()
                            .round(2)
                    }"
                }
            } else {
                tvPayableAmount.text = orderDetailResponse.bookingDetails?.paymentBreakdown?.subTotal.getSafe()
            }

            if (currency?.genericItemName != null && total != null && total != "Free") {
                tvPayAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${
                        total.getCommaRemoved().parseDouble().round(2)
                    } ${currency.genericItemName}"
                    else "${currency.genericItemName} ${
                        total.getCommaRemoved().parseDouble().round(2)
                    }"
            } else {
                tvPayAmount.text = total
            }

            caAddresses.mBinding.tvAddNew.gone()
            setAttachments(orderDetailResponse.bookingDetails?.attachments)

        }
    }

    private fun setExperience(view: TextView, details: OrderResponse?) {
        var yearExp = ApplicationClass.mGlobalData?.globalString?.yearExp?.replace(
            "[0]",
            details?.partnerDetails?.experience.getSafe()
        )
        if (details?.partnerDetails?.experience?.toInt().getSafe() < 1)
            yearExp = ApplicationClass.mGlobalData?.globalString?.lessThanOneYearExp?.replace(
                "[0]",
                details?.partnerDetails?.experience.getSafe()
            )
        else if (details?.partnerDetails?.experience?.toInt().getSafe() > 1)
            yearExp = ApplicationClass.mGlobalData?.globalString?.yearsExp?.replace(
                "[0]",
                details?.partnerDetails?.experience.getSafe()
            )
        view.text = yearExp
    }

    private fun setSpecialities(view: TextView, partner: OrderResponse?) {
        partner?.let {
            val sb = StringBuilder()
            partner.partnerDetails?.specialities?.forEachIndexed { index, item ->
                sb.append(item.genericItemName)
                if (index != partner.partnerDetails?.specialities?.size?.minus(1))
                    sb.append(", ")
            }
            view.text = sb.toString()
        }
    }

    private fun setEduction(view: TextView, partner: OrderResponse?) {
        partner?.let {
            val sb = StringBuilder()
            partner.partnerDetails?.educations?.forEachIndexed { index, edu ->
                sb.append(edu.degree)
                if (index != partner.partnerDetails?.educations?.size?.minus(1))
                    sb.append(", ")
            }
            view.text = sb.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setServiceDescription(view: TextView, orderDetailResponse: OrderResponse) {
        val timeFormat =
            if (DateFormat.is24HourFormat(requireContext())) getString(R.string.timeFormat24) else getString(
                R.string.timeFormat12
            )
        val dateStamp: String = getDateInFormat(
            orderDetailResponse.bookingDetails?.bookingDate.getSafe(),
            "yyyy-MM-dd hh:mm:ss",
            "dd MMM yyyy"
        )
        val timeStamp: String = SimpleDateFormat(timeFormat).format(Calendar.getInstance().time)
        val services =
            metaData?.partnerServiceType?.find { it.id == ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerServiceId }
        var formattedStartDate = ""
        val currentFormat = getString(R.string.timeFormat24)

        if (orderDetailResponse.bookingDetails?.startTime.isNullOrEmpty()
                .not() && orderDetailResponse.bookingDetails?.endTime.isNullOrEmpty().not()
        ) {
            formattedStartDate = getDateInFormat(
                StringBuilder(orderDetailResponse.bookingDetails?.startTime.getSafe()).insert(
                    2,
                    colon
                ).toString(), currentFormat,
                timeFormat
            )
        }

        if (services?.id == CustomServiceTypeView.ServiceType.HomeVisit.id) {
            view.text =
                "${language?.myOrdersScreens?.order.getSafe()} $hash ${orderDetailResponse.bookingDetails?.uniqueIdentificationNumber}\n${
                    dateStamp
                } $start$timeStamp$end $pipe ${orderDetailResponse.bookingDetails?.shift.toString()}"
        } else if (services?.id == CustomServiceTypeView.ServiceType.Message.id) {
            val time = TimeAgo.timeLeft(
                orderDetailResponse.bookingDetails?.timeLeft,
                mBinding.lang?.chatScreen
            )
            view.text =
                "${language?.myOrdersScreens?.order.getSafe()} $hash ${orderDetailResponse.bookingDetails?.uniqueIdentificationNumber}\n${
                    dateStamp
                } $start$timeStamp$end $pipe ${language?.myOrdersScreens?.timeRemaining}$colon $time"
        } else {
            var descText =
                "${language?.myOrdersScreens?.order.getSafe()} $hash ${orderDetailResponse.bookingDetails?.uniqueIdentificationNumber}\n${
                    dateStamp
                }"
            if (formattedStartDate.isEmpty().not())
                descText += " $pipe $start$formattedStartDate$end"
            view.text = descText


        }
    }

    private fun setAttachments(attachments: List<Attachment>?) {
        if (attachments?.isEmpty().getSafe()) {
            mBinding.apply {
                tvAttachments.setVisible(true)
                rvAttachments.setVisible(false)
                tvNoData.setVisible(true)
            }
        } else {
            mBinding.apply {
                tvAttachments.setVisible(true)
                rvAttachments.setVisible(true)
                tvNoData.setVisible(false)
            }
            mBinding.rvAttachments.adapter = itemsAdapter
            itemsAdapter.onItemClick = { item, _ ->
                when (item.drawable) {
                    R.drawable.ic_upload_file -> {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.itemId))
                        startActivity(browserIntent)
                    }
                    R.drawable.ic_image -> {
                        showImageDialog(item.itemId)
                    }
                    else -> {
                        showVoiceNoteDialog(item.itemId)
                    }
                }
            }
            val list: ArrayList<MultipleViewItem> = arrayListOf()
            attachments?.forEach {
                val drawable = when (it.attachmentTypeId) {
                    Enums.AttachmentType.DOC.key -> {
                        R.drawable.ic_upload_file
                    }
                    Enums.AttachmentType.IMAGE.key -> {
                        R.drawable.ic_image
                    }
                    else -> {
                        R.drawable.ic_play_arrow
                    }
                }
                val index = it.attachments?.file?.lastIndexOf('/')
                val listTitle =
                    if (index == -1) it.attachments?.file else it.attachments?.file?.substring(
                        index.getSafe() + 1,
                        it.attachments?.file?.length.getSafe()
                    )


                list.add(
                    MultipleViewItem(
                        title = listTitle,
                        itemId = it.attachments?.file,
                        drawable = drawable,
                    ).apply {
                        services =
                            metaData?.partnerServiceType?.find { it.id == ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerServiceId }

                        itemEndIcon =
                            if (services?.id == CustomServiceTypeView.ServiceType.LaboratoryService.id || services?.id == CustomServiceTypeView.ServiceType.PharmacyService.id) 0 else R.drawable.ic_expand_more

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
                .inflate(R.layout.fragment_view_image, viewGroup, false).apply {
                    findViewById<TextView>(R.id.tvPreview).text =
                        language?.globalString?.imagePreview.getSafe()
                }

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
    }

    private fun showVoiceNoteDialog(itemId: String?) {

        audio.apply {
            title = mBinding.lang?.globalString?.voiceNote.getSafe()
            isPlayOnly = true
            fileName = itemId.getSafe()
            positiveButtonText = mBinding.lang?.globalString?.done.getSafe()
            negativeButtonText = mBinding.lang?.globalString?.cancel.getSafe()
            cancel = mBinding.lang?.globalString?.cancel.getSafe()
            show()

        }
//        val builder = AlertDialog.Builder(requireActivity()).apply {
//            dialogViewBinding = FragmentAddVoicenoteBinding.inflate(layoutInflater)
//            setView(dialogViewBinding.root)
//            setTitle(R.string.voice_note)
//            dialogViewBinding.IvPlay.tag = R.string.play
//            setNegativeButton(R.string.cancel) { _, _ ->
//                stopPlaying()
//            }
//            setCancelable(false)
//            dialogViewBinding.apply {
//                rlPlayLayout.setVisible(true)
//                tvVoiceNote.setVisible(false)
//                IvPlayDelete.setVisible(false)
//                IvVoiceImage.setVisible(false)
//
//                IvPlay.setOnClickListener {//rlPlayLayout
//                    if (IvPlay.tag == R.string.play)
//                        startPlaying(itemId)
//                    else stopPlaying()
//                }
//            }
//        }.create()
//
//        builder.show()
    }

    private fun startPlaying(itemId: String?) {
        player = MediaPlayer().apply {
            try {
                dialogViewBinding.IvPlay.tag = R.string.pause
                dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_pause)
                setDataSource(itemId)
                prepare()
                seekTo(length)
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        player?.setOnCompletionListener {
            dialogViewBinding.IvPlay.tag = R.string.play
            dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_play_arrow)
            length = 0
            player?.release()
            player = null
        }
    }

    private fun stopPlaying() {
        dialogViewBinding.IvPlay.tag = R.string.play
        dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_play_arrow)
        player?.pause()
        length = player?.currentPosition.getSafe()
    }

    private fun setMedicalRecords() {
        mBinding.rvMedicalRecords.adapter = recordsAdapter

        val list: ArrayList<MultipleViewItem> = arrayListOf()
        list.add(
            MultipleViewItem(
                title = "Video consultation",
                desc = "8th May, 22 | Record # AB-1449",
                drawable = R.drawable.ic_tv,
            ).apply {
                itemEndIcon = 0
            }
        )
        list.add(
            MultipleViewItem(
                title = "Reports",
                desc = "14th Aug, 22 | Record # PJ-1045",
                drawable = R.drawable.ic_tv,
            ).apply {
                itemEndIcon = 0
            }
        )
        list.add(
            MultipleViewItem(
                title = "Medication",
                desc = "01th Aug, 22 | Record # MM-1598",
                drawable = R.drawable.ic_tv,
            ).apply {
                itemEndIcon = 0
            }
        )
        recordsAdapter.listItems = list
    }

    private fun cancelOrder() {
        val request = TokenRequest(bookingId = ordersViewModel.bookingId.toInt())
        ordersViewModel.cancelOrder(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<*>
                    setCancelOrderViews(false)
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = getErrorMessage(response.message.toString()),
                            buttonCallback = {},
                        )
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

    private fun addReview(request: MyOrdersRequest) {
        ordersViewModel.addReview(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    setReviewViews(
                        true,
                        review.getSafe(),
                        rating.toString()
                    )

                    getOrders()
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

    override fun onPause() {
        super.onPause()
        audio.onPause()
    }

}