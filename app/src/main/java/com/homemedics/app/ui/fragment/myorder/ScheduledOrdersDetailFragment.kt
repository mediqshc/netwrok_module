package com.homemedics.app.ui.fragment.myorder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Paint
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.orders.MyOrdersRequest
import com.fatron.network_module.models.request.video.TokenRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogAddReviewBinding
import com.homemedics.app.databinding.FragmentScheduledOrderDetailsBinding
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import com.homemedics.app.viewmodel.EMRViewModel
import com.homemedics.app.viewmodel.MyOrderViewModel

class ScheduledOrdersDetailFragment : BaseFragment(), View.OnClickListener {

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()

    private val ordersViewModel: MyOrderViewModel by activityViewModels()

    private val emrViewModel: EMRViewModel by activityViewModels()

    private lateinit var mBinding: FragmentScheduledOrderDetailsBinding
    private var recordsAdapter = AddMultipleViewAdapter()
    private var bookingStatus: Enums.DutyStatusType? = null
    private lateinit var builder: AlertDialog
    private lateinit var addReviewBinding: DialogAddReviewBinding
    private lateinit var dialogSaveButton: Button
    private var rating: String? = null
    private var review: String? = null
    private val hash = "\u0023"
    private val colon = "\u003A"
    private val pipe = "\u007C"

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
        }
    }

    override fun onDetach() {
        super.onDetach()
        ordersViewModel.dutyId = null
    }

    override fun init() {
        val bookingId = arguments?.getInt(Constants.BOOKINGID).getSafe()
        if(bookingId != 0)
            ordersViewModel.bookingId = bookingId.toString()

        if (isOnline(requireActivity())) {
            getOrderDetails()
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_scheduled_order_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentScheduledOrderDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            recordsAdapter.itemClickListener = { item, _ ->
                emrViewModel.apply {
                    emrID = item.itemId?.toInt().getSafe()
                    isPatient = true
                }
                findNavController().safeNavigate(R.id.action_scheduledOrdersDetailFragment_to_customerConsultationRecordDetailsFragment3)
            }
            iDoctor.root.setOnClickListener {
                findNavController().safeNavigate(R.id.action_scheduledOrdersDetailFragment_to_docReviewsFragment2)
            }
            tvDesc.setOnClickListener(this@ScheduledOrdersDetailFragment)
            tvQualification.setOnClickListener(this@ScheduledOrdersDetailFragment)
            bAddReview.setOnClickListener(this@ScheduledOrdersDetailFragment)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bAddReview -> {
                showAddReviewDialog(ordersViewModel.orderDetailsResponse?.partnerDetails?.fullName)
            }
            R.id.tvDesc -> {
                doctorConsultationViewModel.bdcFilterRequest.serviceId =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerServiceId
                doctorConsultationViewModel.partnerProfileResponse.partnerUserId =
                    ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerUserId
                findNavController().safeNavigate(R.id.action_scheduledOrdersDetailFragment_to_aboutDoctorFragment2)
            }
            R.id.tvQualification -> {
                findNavController().safeNavigate(R.id.action_scheduledOrdersDetailFragment_to_docEducationFragment2)
            }
        }
    }

    private fun getMedicalRecords(data: OrderResponse) {
        val list = arrayListOf<MultipleViewItem>()
        list.clear()
        data.medicalRecord?.forEach { medicalRecords ->

            list.apply {
                add(
                    MultipleViewItem(
                        title = medicalRecords.emrName,
                        itemId = medicalRecords.emrId.toString(),
                        desc = "${medicalRecords.date} | ${mBinding.lang?.emrScreens?.record} $hash ${medicalRecords.emrNumber}",
                        drawable = R.drawable.ic_medical_rec,
                    ).apply {
                        itemEndIcon = R.drawable.ic_expand_more
                    }
                )
            }
        }
        mBinding.rvMedicalRecords.adapter = recordsAdapter
        recordsAdapter.listItems = list
        mBinding.tvNoDataMedicalRecords.setVisible(recordsAdapter.listItems.isEmpty())
    }

    private fun setAddReviewViews(isShow: Boolean) {
        mBinding.apply {
            actionbar.desc = Enums.AppointmentStatusType.COMPLETE.label.getSafe()
            tvMsg.apply {
                visible()
                text = lang?.myOrdersScreens?.completed.getSafe()
            }
            bAddReview.setVisible(isShow.not())
            llReviews.setVisible(isShow)
            tvReviewDesc.setVisible(isShow)
            viewBottom.setVisible(isShow)
            tvCompleted.setVisible(isShow)
        }
    }

    private fun setReviewViews(isShow: Boolean, review: String, stars: String) {
        mBinding.apply {
            tvMsg.setVisible(isShow.not())
            bAddReview.setVisible(isShow.not())
            llReviews.setVisible(isShow)
            tvReviewDesc.apply {
                setVisible(isShow)
                text = review
            }
            if (stars > "0.0") {
                tvStars.apply {
                    visible()
                    text = if (stars == "1") "$stars ${lang?.myOrdersScreens?.star}" else "$stars ${lang?.myOrdersScreens?.stars}"
                }
            } else {
                tvStars.gone()
            }
            viewBottom.setVisible(isShow)
            tvCompleted.setVisible(isShow)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getBookingStatus() {
        val services = metaData?.partnerServiceType?.find { it.id ==  ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerServiceId }
        bookingStatus = Enums.DutyStatusType.values().find { it.key == ordersViewModel.orderDetailsResponse?.bookingDetails?.dutyStatusId }
//        when(bookingStatus?.key) {
//            Enums.DutyStatusType.COMPLETED.key -> {
//                setAddReviewViews(false)
//            }
//        }

        if (bookingStatus?.key == Enums.DutyStatusType.COMPLETED.key && ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews != null) {
            setReviewViews(
                true, ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews?.review.getSafe(),
                ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews?.rating.toString()
            )
        }

        // Home health care
        if (services?.id == CustomServiceTypeView.ServiceType.HealthCare.id) {
            val visitType = metaData?.homeServiceVisitTypes?.find { it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.visitType }
            val currentFormat = getString(R.string.timeFormat24)
            val timeFormat = if (DateFormat.is24HourFormat(getAppContext())) getString(
                R.string.timeFormat24
            ) else getString(R.string.timeFormat12)
            val deviceLocaleStartTime = getDateInFormat(
                StringBuilder(ordersViewModel.scheduledDuty.startTime.getSafe()).toString(),
                currentFormat,
                timeFormat
            )
            val deviceLocaleEndTime = getDateInFormat(
                StringBuilder(ordersViewModel.scheduledDuty.endTime.getSafe()).toString(),
                currentFormat,
                timeFormat
            )
            mBinding.apply {
                tvServiceDesc.text = "${lang?.myOrdersScreens?.order.getSafe()} $hash ${ordersViewModel.orderDetailsResponse?.bookingDetails?.uniqueIdentificationNumber}\n" +
                        "${visitType?.genericItemName} $pipe " +
                        "${getLabelFromId(ordersViewModel.orderDetailsResponse?.bookingDetails?.specialityId.toString(), metaData?.specialties?.medicalStaffSpecialties)} " +
                        "$pipe ${ordersViewModel.orderDetailsResponse?.partnerDetails?.fullName} $pipe " +
                        "\n${Constants.START}${Constants.START}$deviceLocaleStartTime${Constants.END} ${Constants.MINUS} ${Constants.START}$deviceLocaleEndTime${Constants.END}${Constants.END}"
            }

            val nextSession = ordersViewModel.orderDetailsResponse?.bookingDetails?.nextSession
            val dutyStatus = Enums.DutyStatusType.values()
                .find { it.key == ordersViewModel.orderDetailsResponse?.bookingDetails?.dutyStatusId }
            when(dutyStatus?.key) {
                Enums.DutyStatusType.PENDING.key -> {
                    if (nextSession != null)
                        mBinding.tvMsg.text = mBinding.lang?.myOrdersScreens?.nextSession?.replace("[0]", getDateInFormat(
                            ordersViewModel.orderDetailsResponse?.bookingDetails?.nextSession.getSafe(),
                            "yyyy-MM-dd", "dd MMMM yyyy"))
                    else
                        mBinding.tvMsg.text = mBinding.lang?.myOrdersScreens?.bookStaffContact.getSafe()
                }
                Enums.DutyStatusType.STARTED.key -> {
                    if (nextSession != null)
                        mBinding.tvMsg.text = mBinding.lang?.myOrdersScreens?.nextSession?.replace("[0]", getDateInFormat(
                            ordersViewModel.orderDetailsResponse?.bookingDetails?.nextSession.getSafe(),
                            "yyyy-MM-dd", "dd MMMM yyyy"))
                    else
                        mBinding.tvMsg.text = mBinding.lang?.myOrdersScreens?.bookStaffContact.getSafe()
                }

                Enums.DutyStatusType.COMPLETED.key -> {
                    mBinding.tvCompleted.gone()
                    mBinding.tvMsg.visible()
                    if (ordersViewModel.orderDetailsResponse?.bookingDetails?.reviews != null) {
                        mBinding.tvMsg.text = mBinding.lang?.myOrdersScreens?.homeVisitComplete.getSafe()
                    } else {
                        mBinding.apply {
                            bAddReview.visible()
                            tvMsg.text = mBinding.lang?.myOrdersScreens?.afterReviewHome.getSafe()
                        }
                    }
                }
            }
        }
    }

    private fun setPartnerDetails(orderDetailResponse: OrderResponse?) {
        doctorConsultationViewModel.bdcFilterRequest.serviceId = orderDetailResponse?.bookingDetails?.partnerServiceId
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
            addReviewBinding.etInstructions.hint = mBinding.lang?.globalString?.feedbackHere.getSafe()
            addReviewBinding.tvDesc.text = if (partnerName != null)
                mBinding.lang?.dialogsStrings?.callDialogReviewDesc?.replace("[0]", partnerName.getSafe())
            else mBinding.lang?.myOrdersScreens?.callThankYou.getSafe()
            addReviewBinding.etInstructions.addTextChangedListener {
                val length = it?.length.getSafe()
                addReviewBinding.tvLength.text = getString(R.string.review_length_, length)
            }
            setView(addReviewBinding.root)
            setTitle(mBinding.lang?.myOrdersScreens?.addAReview.getSafe())
            setPositiveButton(mBinding.lang?.globalString?.save.getSafe()) { _, _ ->
                builder.dismiss()
            }
            setNegativeButton(mBinding.lang?.globalString?.cancel.getSafe(), null)
        }.create()

        builder.setOnShowListener{
            dialogSaveButton = builder.getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val review = addReviewBinding.etInstructions.text.toString()
                val rating = addReviewBinding.ratingBar.rating.toDouble()
                if(isValidRating(rating ).not()){
                    showToast(mBinding.lang?.fieldValidationStrings?.ratingValidation.getSafe())
                    return@setOnClickListener
                }
                this.review = review
                this.rating = rating.toString()
                val request = MyOrdersRequest(
                    bookingId = ordersViewModel.bookingId.toInt(),
                    rating = rating,
                    review = review,
                    dutyId = ordersViewModel.dutyId
                )
                addReview(request)
                builder.dismiss()
            }
        }
        builder.show()
    }

    @SuppressLint("SetTextI18n")
    private fun setData(orderDetailResponse: OrderResponse) {
        bookingStatus = Enums.DutyStatusType.values().find { it.key == orderDetailResponse.bookingDetails?.dutyStatusId }
        val dutyStatus = metaData?.dutyStatuses?.find { it.genericItemId == ordersViewModel.orderDetailsResponse?.bookingDetails?.dutyStatusId }?.label
        mBinding.apply {
            actionbar.apply {
                title = getDateInFormat(ordersViewModel.visitDate.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")
                desc = dutyStatus.getSafe()
            }
        }

        mBinding.apply {
            iDoctor.apply {
                ivOnlineStatus.gone()
                ivDrImage.loadImage(orderDetailResponse.partnerDetails?.profilePicture, getGenderIcon(orderDetailResponse.bookingDetails?.customerUser?.genderId.toString()))
                tvName.text = orderDetailResponse.partnerDetails?.fullName
                setSpecialities(tvExperience, orderDetailResponse)
                tvNoRating.text = lang?.globalString?.noRating.getSafe()
                tvReview.apply {
                    setVisible(orderDetailResponse.partnerDetails?.totalNoOfReviews != null)

                    val numOfReviews = if(orderDetailResponse.partnerDetails?.totalNoOfReviews.getSafe() == 0) 0 else String.format("%02d", orderDetailResponse.partnerDetails?.totalNoOfReviews.getSafe())

                    text =
                        "${lang?.globalString?.reviews}($numOfReviews)"
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
                setVisible(orderDetailResponse.partnerDetails?.overview.isNullOrEmpty().not())
                text = orderDetailResponse.partnerDetails?.overview.getSafe()
            }

            setEduction(tvQualification, orderDetailResponse)

            setServiceDescription(tvServiceDesc, orderDetailResponse)

            tvSpecialInstructionDesc.text = if (orderDetailResponse.bookingDetails?.instructions.isNullOrEmpty().not())
                orderDetailResponse.bookingDetails?.instructions
            else
                lang?.globalString?.noInstructions.getSafe()

            val isOrderOwner = (DataCenter.getUser()?.id == ordersViewModel.orderDetailsResponse?.bookingDetails?.bookedForUser?.id)
            tvBookBy.text =
                "${lang?.globalString?.bookedBy.getSafe()} $colon ${orderDetailResponse.bookingDetails?.customerUser?.fullName} $pipe ${lang?.globalString?.bookedFor.getSafe()} $colon ${if (isOrderOwner) lang?.globalString?.self else orderDetailResponse.bookingDetails?.bookedForUser?.fullName}"

            getBookingStatus()
        }
    }

    private fun setSpecialities(view: TextView, partner: OrderResponse?){
        partner?.let {
            val sb = StringBuilder()
            partner.partnerDetails?.specialities?.forEachIndexed { index, item ->
                sb.append(item.genericItemName)
                if(index != partner.partnerDetails?.specialities?.size?.minus(1))
                    sb.append(", ")
            }
            view.text = sb.toString()
        }
    }

    private fun setEduction(view: TextView, partner: OrderResponse?){
        partner?.let {
            val sb = StringBuilder()
            partner.partnerDetails?.educations?.forEachIndexed { index, edu ->
                sb.append(edu.degree)
                if(index != partner.partnerDetails?.educations?.size?.minus(1))
                    sb.append(", ")
            }
            view.text = sb.toString()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setServiceDescription(view: TextView, orderDetailResponse: OrderResponse) {
        val services = metaData?.partnerServiceType?.find { it.id ==  ordersViewModel.orderDetailsResponse?.bookingDetails?.partnerServiceId }
        var formattedStartDate = ""
        var formattedEndDate = ""
        val currentFormat = getString(R.string.timeFormat24)
        val timeFormat = if (DateFormat.is24HourFormat(requireContext())) getString(R.string.timeFormat24) else getString(
                R.string.timeFormat12
            )

        if(orderDetailResponse.bookingDetails?.startTime.isNullOrEmpty().not() && orderDetailResponse.bookingDetails?.endTime.isNullOrEmpty().not()){
            formattedStartDate = getDateInFormat(
                StringBuilder(orderDetailResponse.bookingDetails?.startTime.getSafe()).insert(2, ":").toString(), currentFormat,
                timeFormat
            )
        }

        if(orderDetailResponse.bookingDetails?.startTime.isNullOrEmpty().not() && orderDetailResponse.bookingDetails?.endTime.isNullOrEmpty().not()){
            formattedEndDate = getDateInFormat(
                StringBuilder(orderDetailResponse.bookingDetails?.endTime.getSafe()).insert(2, ":").toString(), currentFormat,
                timeFormat
            )
        }

        view.text = "${mBinding.lang?.myOrdersScreens?.order.getSafe()} $hash ${orderDetailResponse.bookingDetails?.uniqueIdentificationNumber}" +
                "\n${services?.name} $pipe ${orderDetailResponse.partnerDetails?.fullName}" +
                "\n$formattedStartDate - $formattedEndDate"
    }

    private fun addReview(request: MyOrdersRequest) {
        ordersViewModel.addReview(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                        setReviewViews(
                            true,
                            review.getSafe(),
                            rating.toString()
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
                else -> { hideLoader() }
            }
        }
    }

    private fun getOrderDetails() {
        val request = TokenRequest(bookingId = ordersViewModel.bookingId.toInt(), dutyId = ordersViewModel.dutyId)
        ordersViewModel.orderDetails(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<OrderResponse>
                    response.data?.let { orderDetailResponse ->
                        ordersViewModel.orderDetailsResponse = orderDetailResponse
                        setData(orderDetailResponse)
                        setPartnerDetails(orderDetailResponse)
                        getMedicalRecords(orderDetailResponse)
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
                else -> { hideLoader() }
            }
        }
    }
}