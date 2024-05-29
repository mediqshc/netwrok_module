package com.homemedics.app.ui.bottomsheets

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseBottomSheetFragment
import com.homemedics.app.databinding.DialogRecordsDoctorCallBinding
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.fragment.doctorconsultation.DocReviewsFragment
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CallViewModel
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import com.homemedics.app.viewmodel.EMRViewModel

class DoctorRecordsBottomSheet : BaseBottomSheetFragment()  {

    val TAG = "DoctorRecordsBottomSheet"

    private lateinit var mBinding: DialogRecordsDoctorCallBinding

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()

    private val emrViewModel: EMRViewModel by activityViewModels()

    private val callViewModel: CallViewModel by activityViewModels()

    private var itemsAdapter = AddMultipleViewAdapter()

    override fun getTheme(): Int = R.style.base_bottom_sheet

    override fun setLanguageData() {
        mBinding.langData=ApplicationClass.mGlobalData
    }

    @SuppressLint("SetTextI18n")
    override fun init() {
            mBinding.apply {
                val roundedRadius = resources.getDimensionPixelSize(R.dimen.dp16)
                mBinding.actionbar.mBinding.clContainer.background = MaterialShapeDrawable(
                    ShapeAppearanceModel.Builder()
                        .setTopRightCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                        .setTopLeftCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                        .setTopLeftCorner(RoundedCornerTreatment())
                        .setTopRightCorner(RoundedCornerTreatment())
                        .build()
                ).apply { fillColor = ColorStateList.valueOf(resources.getColor(R.color.primary, requireContext().theme)) }

                val label = metaData.orderStatuses?.find { it.genericItemId == callViewModel.orderDetailsResponse2?.bookingDetails?.bookingStatusId }?.label
                actionbar.title ="\u2066# ${callViewModel.orderDetailsResponse2?.bookingDetails?.uniqueIdentificationNumber.getSafe()} \u2069 "
                actionbar.desc = label.getSafe()
                vShadow.visible()
                iDoctor.apply {
                    root.visible()
                    ivOnlineStatus.gone()
                    tvName.apply {
                        text = callViewModel.orderDetailsResponse2?.partnerDetails?.fullName
                        setTextColor(ContextCompat.getColor(requireActivity(), R.color.primary))
                    }
                    ivDrImage.loadImage(callViewModel.orderDetailsResponse2?.partnerDetails?.profilePicture, R.drawable.ic_male)
                    setPartnerBio(tvExperience, callViewModel.orderDetailsResponse2)
                    val docExp = callViewModel.orderDetailsResponse2?.partnerDetails?.experience.getSafe()
                    tvYearsExp.text = if (docExp.toInt() > 1) "(${docExp} ${mBinding.langData?.globalString?.doctorExperiences})" else "(${docExp} ${mBinding.langData?.globalString?.doctorExperience})"
                    tvReview.apply {
                        visible()

                        val numOfReviews = if(callViewModel.orderDetailsResponse2?.partnerDetails?.totalNoOfReviews.getSafe() == 0) 0 else String.format("%02d", callViewModel.orderDetailsResponse2?.partnerDetails?.totalNoOfReviews.getSafe())

                        text = "${mBinding.langData?.globalString?.reviews}($numOfReviews)"
                        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                    }
                    val avgRating = callViewModel.orderDetailsResponse2?.partnerDetails?.average_reviews_rating
                    if (avgRating.getSafe() > 0.0) {
                        tvNoRating.gone()
                        ratingBar.apply {
                            visible()
                            rating = callViewModel.orderDetailsResponse2?.partnerDetails?.average_reviews_rating?.toFloat().getSafe()
                        }
                    } else {
                        ratingBar.gone()
                        tvNoRating.visible()
                    }
                }
            }

        getMedicalRecords(callViewModel.orderDetailsResponse2)
    }

    override fun getFragmentLayout() = R.layout.dialog_records_doctor_call

    override fun getViewModel() {

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.behavior.addBottomSheetCallback(object :
            BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {

                    val roundedRadius = resources.getDimensionPixelSize(R.dimen.dp16)
                    mBinding.actionbar.mBinding.clContainer.background = MaterialShapeDrawable(
                        ShapeAppearanceModel.Builder()
                            .setTopRightCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                            .setTopLeftCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                            .setTopLeftCorner(RoundedCornerTreatment())
                            .setTopRightCorner(RoundedCornerTreatment())
                            .build()
                    ).apply { fillColor = ColorStateList.valueOf(resources.getColor(R.color.primary, requireContext().theme)) }

                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val roundedRadius = resources.getDimensionPixelSize(R.dimen.dp16)
                mBinding.actionbar.mBinding.clContainer.background = MaterialShapeDrawable(
                    ShapeAppearanceModel.Builder()
                        .setTopRightCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                        .setTopLeftCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                        .setTopLeftCorner(RoundedCornerTreatment())
                        .setTopRightCorner(RoundedCornerTreatment())
                        .build()
                ).apply { fillColor = ColorStateList.valueOf(resources.getColor(R.color.primary, requireContext().theme)) }
            }
        })

        return bottomSheetDialog
    }

    override fun getViewBinding() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding =  binding as DialogRecordsDoctorCallBinding
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
            }
            itemsAdapter.onItemClick = { item, _ ->
                emrViewModel.apply {
                    emrID = item.itemId?.toInt().getSafe()
                    isPatient = true
                }
                (requireActivity() as CallActivity).setRecordNavigation()
                dismiss()
            }
        }
    }

    private fun getMedicalRecords(data: OrderResponse?) {
        val list = arrayListOf<MultipleViewItem>()
        list.clear()
        data?.medicalRecord?.forEach { medicalRecords ->

            list.apply {
                add(
                    MultipleViewItem(
                        title = medicalRecords.emrName,
                        itemId = medicalRecords.emrId.toString(),
                        desc = "${medicalRecords.date} | ${mBinding.langData?.globalString?.recordNum} ${medicalRecords.emrNumber}",
                        drawable = R.drawable.ic_medical_rec,
                    ).apply {
                        itemEndIcon = R.drawable.ic_expand_more
                    }
                )
            }
        }
        mBinding.rvMedicalRecords.adapter = itemsAdapter
        itemsAdapter.listItems = list
        mBinding.tvNoData.apply {
            text = mBinding.langData?.callScreen?.noMedicalRecordsFound
            setVisible(list.isEmpty())
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

    private fun setPartnerBio(view: TextView, partner: OrderResponse?){
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
}