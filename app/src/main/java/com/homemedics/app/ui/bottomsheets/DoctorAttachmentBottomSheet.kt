package com.homemedics.app.ui.bottomsheets

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseBottomSheetFragment
import com.homemedics.app.databinding.DialogAttachmentDoctorCallBinding
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.ui.fragment.doctorconsultation.DocReviewsFragment
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CallViewModel
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import java.io.IOException

class DoctorAttachmentBottomSheet : BaseBottomSheetFragment() {

    val TAG = "DoctorAttachmentBottomSheet"

    private lateinit var mBinding: DialogAttachmentDoctorCallBinding

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()

    private val callViewModel: CallViewModel by activityViewModels()

    private var itemsAdapter = AddMultipleViewAdapter()

    override fun getTheme(): Int = R.style.base_bottom_sheet

    override fun setLanguageData() {
        mBinding.langData=ApplicationClass.mGlobalData
    }
//
//    override fun onPause() {
//        super.onPause()
//        CustomAudio(requireContext()).stopRecording()
//    }

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
                actionbar.title = "\u2066# ${ callViewModel.orderDetailsResponse2?.bookingDetails?.uniqueIdentificationNumber.getSafe()}  \u2069"
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
                    tvYearsExp.text = if (docExp.toInt() > 1) "(${docExp} ${mBinding.langData?.globalString?.doctorExperiences.getSafe()})" else "(${docExp} ${mBinding.langData?.globalString?.doctorExperience.getSafe()})"
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
                    setAttachments(callViewModel.orderDetailsResponse2?.bookingDetails?.attachments)
                }
            }
    }

    override fun getFragmentLayout() = R.layout.dialog_attachment_doctor_call

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding = binding as DialogAttachmentDoctorCallBinding
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
                        showVoiceNoteDialog(item.itemId)
                    }
                }
            }
            val list: ArrayList<MultipleViewItem> = arrayListOf()
            attachments?.forEach {
                val drawable = when(it.attachmentTypeId) {
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
                val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
                list.add(
                    MultipleViewItem(
                        title = listTitle,
                        itemId = it.attachments?.file,
                        drawable = drawable,
                    ).apply {
                        itemEndIcon = R.drawable.ic_expand_more
                        if(it.attachmentTypeId== Enums.AttachmentType.VOICE.key ) itemCenterIcon=R.drawable.ic_voice_group

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
        alertDialog.window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        ivBack.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showVoiceNoteDialog(itemId: String?) {
        val audio = CustomAudio(requireContext())
        audio.apply {
            title =  mBinding.langData?.globalString?.voiceNote.getSafe()
            negativeButtonText =  mBinding.langData?.globalString?.cancel.getSafe()
            voiceNote = mBinding.langData?.dialogsStrings?.voiceNoteDescription.getSafe()
            cancel = mBinding.langData?.globalString?.cancel.getSafe()
            fileName=itemId.getSafe()
            isPlayOnly=true
            show()
        }
    }

}