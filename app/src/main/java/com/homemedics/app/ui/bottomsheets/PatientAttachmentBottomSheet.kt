package com.homemedics.app.ui.bottomsheets

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseBottomSheetFragment
import com.homemedics.app.databinding.DialogAttachmentCallBinding
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CallViewModel
import java.text.SimpleDateFormat
import java.util.*


class PatientAttachmentBottomSheet : BaseBottomSheetFragment() {

    val TAG = "PatientAttachmentBottomSheet"

    private lateinit var mBinding: DialogAttachmentCallBinding

    private val callViewModel: CallViewModel by activityViewModels()

    private var itemsAdapter = AddMultipleViewAdapter()
    override fun getTheme(): Int = R.style.base_bottom_sheet

    override fun setLanguageData() {
        mBinding.langData=ApplicationClass.mGlobalData
    }

    @SuppressLint("SetTextI18n")
    override fun init() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding.dataManager = callViewModel.appointmentResponse

        val roundedRadius = resources.getDimensionPixelSize(R.dimen.dp16)
            mBinding.actionbar.mBinding.clContainer.background = MaterialShapeDrawable(
                ShapeAppearanceModel.Builder()
                    .setTopRightCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                    .setTopLeftCorner(CornerFamily.ROUNDED, roundedRadius.toFloat())
                    .setTopLeftCorner(RoundedCornerTreatment())
                    .setTopRightCorner(RoundedCornerTreatment())
                    .build()
            ).apply { fillColor = ColorStateList.valueOf(resources.getColor(R.color.primary, requireContext().theme)) }

        val uniqueId = TinyDB.instance.getString("unique_id")
        mBinding.actionbar.title = "\u2066# $uniqueId \u2069 "
        mBinding.actionbar.desc = callViewModel.appointmentResponse?.bookingStatus.getSafe().firstCap()

        if (callViewModel.appointmentResponse?.patientDetails?.profilePicture != null) {
            mBinding.iDoctorHeader.ivThumbnail.loadImage(callViewModel.appointmentResponse?.patientDetails?.profilePicture, getGenderIcon(callViewModel.appointmentResponse?.patientDetails?.genderId.toString()))
        }

        var formattedStartDate = ""
        var formattedEndDate = ""
        val currentFormat = getString(R.string.timeFormat24)
        val timeFormat =
            if (DateFormat.is24HourFormat(requireContext())) getString(R.string.timeFormat24) else getString(
                R.string.timeFormat12
            )
        if(callViewModel.appointmentResponse?.startTime.isNullOrEmpty().not() && callViewModel.appointmentResponse?.endTime.isNullOrEmpty().not()){
            formattedStartDate = getDateInFormat(
                StringBuilder(callViewModel.appointmentResponse?.startTime.getSafe()).insert(2, ":").toString(), currentFormat,
                timeFormat
            )

            formattedEndDate = getDateInFormat(
                StringBuilder(callViewModel.appointmentResponse?.endTime.getSafe()).insert(2, ":").toString(),
                currentFormat,
                timeFormat
            )
        }
        val fee = if (callViewModel.appointmentResponse?.fee?.contains("Free", true).getSafe()) mBinding.langData?.globalString?.free else callViewModel.appointmentResponse?.fee
        val currencyData = metaData.currencies?.find { it.genericItemId == callViewModel.appointmentResponse?.currencyId }
        mBinding.currency = currencyData?.genericItemName.getSafe()
        val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        mBinding.iDoctorHeader.tvDetail.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${Constants.START}${callViewModel.appointmentResponse?.bookingDate}${Constants.END} ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END} ${mBinding.langData?.globalString?.to} ${Constants.START}$formattedEndDate${Constants.END} ${Constants.PIPE} $fee ${mBinding.currency}" else "${Constants.START}${callViewModel.appointmentResponse?.bookingDate}${Constants.END} ${Constants.PIPE} ${Constants.START}$formattedStartDate${Constants.END} ${mBinding.langData?.globalString?.to} ${Constants.START}$formattedEndDate${Constants.END} ${Constants.PIPE} ${mBinding.currency} $fee"
        callViewModel.appointmentResponse?.patientDetails?.dateOfBirth.getSafe()

        val cal: Calendar = Calendar.getInstance(Locale.US)
        val format = SimpleDateFormat("yyyy-MM-dd")
        format.parse(callViewModel.appointmentResponse?.patientDetails?.dateOfBirth.getSafe())?.let { cal.setTime(it) }
        val today = Calendar.getInstance(Locale.US)

        val time: Long = (today.time.time / 1000) - (cal.time.time / 1000)

        val years: Long = Math.round(time.toDouble()) / 31536000
        val months: Long = Math.round(time.toDouble() - years * 31536000) / 2628000

        val monthsToShow =
            if(months > 0) ", $months ${mBinding.langData?.globalString?.monthhs}"
            else ""

        if(years.toInt() == 0 && months.toInt() == 0){
            mBinding.iDoctorHeader.tvDescription.text =
                "\u2066${callViewModel.appointmentResponse?.patientDetails?.gender} | ${mBinding.langData?.globalString?.lessThanYear}\u2069"
        }
        else if (years > 0) {
            val year= if( years.toInt()>1) ApplicationClass.mGlobalData?.globalString?.years else ApplicationClass.mGlobalData?.globalString?.year
            mBinding.iDoctorHeader.tvDescription.text =
                "\u2066${callViewModel.appointmentResponse?.patientDetails?.gender} \u2069\u2069 | $years $year $monthsToShow \u2066"
        } else {
            mBinding.iDoctorHeader.tvDescription.text =
                "\u2066${callViewModel.appointmentResponse?.patientDetails?.gender}\u2069\u2069 | $months ${
                    mBinding.langData?.globalString?.monthhs
                } \u2066"
        }

        setAttachments(callViewModel.appointmentAttachments)

        mBinding.executePendingBindings()
    }

    override fun getFragmentLayout() = R.layout.dialog_attachment_call

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding = binding as DialogAttachmentCallBinding
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
            }
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
            cancel = mBinding.langData?.globalString?.cancel.getSafe()
            fileName=itemId.getSafe()
            isPlayOnly=true
            show()
        }
    }
}