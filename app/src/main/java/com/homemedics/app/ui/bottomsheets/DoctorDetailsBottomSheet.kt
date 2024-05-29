package com.homemedics.app.ui.bottomsheets

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.text.format.DateFormat
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseBottomSheetFragment
import com.homemedics.app.databinding.DialogDoctorDetailsCallBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.fragment.doctorconsultation.AboutDoctorFragment
import com.homemedics.app.ui.fragment.doctorconsultation.DocEducationFragment
import com.homemedics.app.ui.fragment.doctorconsultation.DocReviewsFragment
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CallViewModel
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DoctorDetailsBottomSheet : BaseBottomSheetFragment() {

    val TAG = "DoctorDetailsBottomSheet"

    private lateinit var mBinding: DialogDoctorDetailsCallBinding

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()

    private val callViewModel: CallViewModel by activityViewModels()

    private var locale: String? = null

    override fun getTheme(): Int = R.style.base_bottom_sheet

    override fun setLanguageData() {
        mBinding.langData = ApplicationClass.mGlobalData
    }

    @SuppressLint("SetTextI18n")
    override fun init() {
        locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        lifecycleScope.launch {
            delay(200) // to override default databinding view values

            mBinding.apply {
                val roundedRadius = resources.getDimensionPixelSize(R.dimen.dp16)
                mBinding.actionbar.mBinding.clContainer.background = MaterialShapeDrawable(
                    ShapeAppearanceModel.Builder()
                        .setTopRightCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                        .setTopLeftCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                        .setTopLeftCorner(RoundedCornerTreatment())
                        .setTopRightCorner(RoundedCornerTreatment())
                        .build()
                ).apply {
                    fillColor = ColorStateList.valueOf(
                        resources.getColor(
                            R.color.primary,
                            requireContext().theme
                        )
                    )
                }

                val label = metaData.orderStatuses?.find { it.genericItemId == callViewModel.orderDetailsResponse2?.bookingDetails?.bookingStatusId }?.label
                actionbar.title =
                    "\u2066# ${callViewModel.orderDetailsResponse2?.bookingDetails?.uniqueIdentificationNumber.getSafe()}  \u2069"
                actionbar.desc = label.getSafe()

                iDoctor.apply {
                    root.visible()
                    ivOnlineStatus.gone()
                    tvName.apply {
                        text = callViewModel.orderDetailsResponse2?.partnerDetails?.fullName
                        setTextColor(ContextCompat.getColor(requireActivity(), R.color.primary))
                    }
                    ivDrImage.loadImage(
                        callViewModel.orderDetailsResponse2?.partnerDetails?.profilePicture,
                        R.drawable.ic_male
                    )
                    setPartnerBio(tvExperience, callViewModel.orderDetailsResponse2)
                    val docExp =
                        callViewModel.orderDetailsResponse2?.partnerDetails?.experience.getSafe()
                    tvYearsExp.text =
                        if (docExp.toInt() > 1) "(${docExp} ${mBinding.langData?.globalString?.doctorExperiences})" else "(${docExp} ${
                            mBinding.langData?.globalString?.doctorExperience
                        })"
                    tvReview.apply {
                        visible()

                        val numOfReviews =
                            if (callViewModel.orderDetailsResponse2?.partnerDetails?.totalNoOfReviews.getSafe() == 0) 0 else String.format(
                                "%02d",
                                callViewModel.orderDetailsResponse2?.partnerDetails?.totalNoOfReviews.getSafe()
                            )

                        text = "${mBinding.langData?.globalString?.reviews}($numOfReviews)"
                        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    }
                    val avgRating =
                        callViewModel.orderDetailsResponse2?.partnerDetails?.average_reviews_rating
                    if (avgRating.getSafe() > 0.0) {
                        tvNoRating.gone()
                        ratingBar.apply {
                            visible()
                            rating =
                                callViewModel.orderDetailsResponse2?.partnerDetails?.average_reviews_rating?.toFloat()
                                    .getSafe()
                        }
                    } else {
                        ratingBar.gone()
                        tvNoRating.visible()
                    }
                }
            }
        }
        mBinding.apply {
            if (callViewModel.orderDetailsResponse2?.partnerDetails?.overview != null)
                tvDesc.text = callViewModel.orderDetailsResponse2?.partnerDetails?.overview
            else
                tvDesc.gone()

            setEducation(tvQualification, callViewModel.orderDetailsResponse2)
            var formattedStartDate = ""
            val currentFormat = getString(R.string.timeFormat24)
            val timeFormat =
                if (DateFormat.is24HourFormat(requireContext())) getString(R.string.timeFormat24) else getString(
                    R.string.timeFormat12
                )

            if (callViewModel.orderDetailsResponse2?.bookingDetails?.startTime.isNullOrEmpty()
                    .not() && callViewModel.orderDetailsResponse2?.bookingDetails?.endTime.isNullOrEmpty()
                    .not()
            ) {
                formattedStartDate = getDateInFormat(
                    StringBuilder(callViewModel.orderDetailsResponse2?.bookingDetails?.startTime.getSafe()).insert(
                        2,
                        ":"
                    ).toString(), currentFormat,
                    timeFormat
                )
            }

            tvServiceDesc.text =
                "${mBinding.langData?.callScreen?.order.getSafe()} ${Constants.HASH} ${callViewModel.orderDetailsResponse2?.bookingDetails?.uniqueIdentificationNumber}\n${
                    callViewModel.orderDetailsResponse2?.bookingDetails?.bookingDate?.let { bookingDate ->
                        getDateInFormat(
                            bookingDate, "yyyy-MM-dd hh:mm:ss", "dd MMM yyyy"
                        )
                    }
                } ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END}"
            val currency =
                DataCenter.getMeta()?.currencies?.find { it.itemId == callViewModel.orderDetailsResponse2?.partnerDetails?.currencyId.toString() }
            if (currency?.genericItemName != null && callViewModel.orderDetailsResponse2?.partnerDetails?.fee != null) {
                tvPrice.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${callViewModel.orderDetailsResponse2?.partnerDetails?.fee.getSafe()} ${currency.genericItemName}" else "${currency.genericItemName} ${callViewModel.orderDetailsResponse2?.partnerDetails?.fee.getSafe()}"
            } else {
                tvPrice.text = if (callViewModel.orderDetailsResponse2?.partnerDetails?.fee?.contains("Free", true).getSafe()) langData?.globalString?.free.getSafe() else "${callViewModel.orderDetailsResponse2?.partnerDetails?.fee}"
            }

            tvSpecialInstructionDesc.text =
                if (callViewModel.orderDetailsResponse2?.bookingDetails?.instructions.isNullOrEmpty()
                        .not()
                )
                    callViewModel.orderDetailsResponse2?.bookingDetails?.instructions
                else
                    mBinding.langData?.globalString?.noInstructions

            tvBookBy.text =
                "${mBinding.langData?.globalString?.bookedBy}: ${callViewModel.orderDetailsResponse2?.bookingDetails?.customerUser?.fullName} | ${
                    mBinding.langData?.globalString?.bookedFor
                }: ${callViewModel.orderDetailsResponse2?.bookingDetails?.bookedForUser?.fullName}"

            if (currency?.genericItemName != null && callViewModel.orderDetailsResponse2?.partnerDetails?.fee != null) {
                tvAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${callViewModel.orderDetailsResponse2?.partnerDetails?.fee} ${currency.genericItemName}" else "${currency.genericItemName} ${callViewModel.orderDetailsResponse2?.partnerDetails?.fee}"
            } else {
                tvAmount.text = if (callViewModel.orderDetailsResponse2?.partnerDetails?.fee?.contains("Free", true).getSafe()) langData?.globalString?.free.getSafe() else "${callViewModel.orderDetailsResponse2?.partnerDetails?.fee}"
            }

            if (currency?.genericItemName != null && callViewModel.orderDetailsResponse2?.partnerDetails?.fee != null) {
                tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${callViewModel.orderDetailsResponse2?.partnerDetails?.fee} ${currency.genericItemName}" else "${currency.genericItemName} ${callViewModel.orderDetailsResponse2?.partnerDetails?.fee}"
            } else {
                tvPayableAmount.text = if (callViewModel.orderDetailsResponse2?.partnerDetails?.fee?.contains("Free", true).getSafe()) langData?.globalString?.free.getSafe() else "${callViewModel.orderDetailsResponse2?.partnerDetails?.fee}"
            }

            if (currency?.genericItemName != null && callViewModel.orderDetailsResponse2?.partnerDetails?.fee != null) {
                tvPayAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${callViewModel.orderDetailsResponse2?.bookingDetails?.paymentBreakdown?.payableAmount} ${currency.genericItemName}" else "${currency.genericItemName} ${callViewModel.orderDetailsResponse2?.bookingDetails?.paymentBreakdown?.payableAmount}"
            } else { // free case
                tvPayAmount.text = "${callViewModel.orderDetailsResponse2?.partnerDetails?.fee}"
            }

            tvPaymentMethod.text =
                "${mBinding.langData?.callScreen?.paymentMode} ${callViewModel.orderDetailsResponse2?.bookingDetails?.paymentBreakdown?.paymentMethod}"
        }
    }

    override fun getFragmentLayout() = R.layout.dialog_doctor_details_call

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding = binding as DialogDoctorDetailsCallBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                setOnClickListener {
                    dismiss()
                }
                onAction1Click = {
                    dismiss()
                }
                onAction2Click = {
                    dismiss()
                }
                iDoctor.tvReview.setOnClickListener {
                    setPartnerDetails(callViewModel.orderDetailsResponse2)
                    (activity as CallActivity).setFragment(DocReviewsFragment())
                    dialog?.dismiss()
                }
                tvDesc.setOnClickListener {
                    setPartnerDetails(callViewModel.orderDetailsResponse2)
                    (activity as CallActivity).setFragment(AboutDoctorFragment())
                    dialog?.dismiss()
                }
                tvQualification.setOnClickListener {
                    setPartnerDetails(callViewModel.orderDetailsResponse2)
                    (activity as CallActivity).setFragment(DocEducationFragment())
                    dialog?.dismiss()
                }
            }
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

    private fun setPartnerBio(view: TextView, partner: OrderResponse?) {
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

    private fun setEducation(view: TextView, partner: OrderResponse?) {
        partner?.let {
            val sb = StringBuilder()
            partner.partnerDetails?.educations?.forEachIndexed { index, item ->
                sb.append(item.degree)
                if (index != partner.partnerDetails?.educations?.size?.minus(1))
                    sb.append(", ")
            }
            view.text = sb.toString()
        }
    }
}