package com.homemedics.app.ui.fragment.walkin.details

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.request.claim.ClaimStatusRequest
import com.fatron.network_module.models.request.orders.MyOrdersRequest
import com.fatron.network_module.models.request.walkin.AddWalkInAttachmentRequest
import com.fatron.network_module.models.request.walkin.WalkInAttachmentRequest
import com.fatron.network_module.models.request.walkin.WalkInRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.walkin.WalkIn
import com.fatron.network_module.models.response.walkin.WalkInResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogAddReviewBinding
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.databinding.FragmentClaimOrderDetailsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.adapter.CheckoutSplitAmountAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.utils.Constants.LAB_ORDER_DETAIL_BOOKING_ID
import com.homemedics.app.viewmodel.MyOrderViewModel
import com.homemedics.app.viewmodel.WalkInViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import java.io.IOException

class WalkInLabOrdersDetailFragment : BaseFragment(), View.OnClickListener {

    private val walkinViewModel: WalkInViewModel by activityViewModels()
    private val ordersViewModel: MyOrderViewModel by activityViewModels()

    private lateinit var addReviewBinding: DialogAddReviewBinding
    private lateinit var addReviewDialogBuilder: AlertDialog
    private lateinit var mBinding: FragmentClaimOrderDetailsBinding

    private lateinit var dialogViewBinding: FragmentAddVoicenoteBinding
    var fromUploadForLab=false

    private var resendConfirmationTimer: CountDownTimer? = null
    var attachment_lab=false

    //audio data
    private var player: MediaPlayer? = null
    private var length = 0
    private var absolutePath: String = ""
    private var dialogSaveButton: Button? = null
    private var file: File? = null
    private lateinit var meter: Chronometer
    private lateinit var fileUtils: FileUtils
    private var elapsedMillis = 0L
    private var recorder: MediaRecorder? = null
    private lateinit var animBlink: Animation
    private var isPrescriptionSet = false
    private var isInvoiceSet = false

    private var locale: String? = null
    private var rating: String? = null
    var fromNoti:Boolean=false
    private var review: String? = null

    private var attachmentsAdapter = AddMultipleViewAdapter()

    private var walkInId = 0

    val langData = ApplicationClass.mGlobalData

    override fun setLanguageData() {
        mBinding.langData = langData

        mBinding.apply {
            actionbar.title = langData?.walkInScreens?.walkinLaboratory.getSafe()
        }
    }

    override fun onDetach() {
        super.onDetach()
        walkinViewModel.isSubmitReviewAttachment=false

        try {
            resendConfirmationTimer?.cancel()
        }
        catch (e: Exception){e.printStackTrace()}
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(LAB_ORDER_DETAIL_BOOKING_ID, walkinViewModel.walkInRequest.walkInLaboratoryId.getSafe())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            walkinViewModel.walkInRequest.walkInLaboratoryId = savedInstanceState.getInt(LAB_ORDER_DETAIL_BOOKING_ID, 0)
            getOrderDetails()
        }
    }

    override fun init() {
        locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
         fromNoti= arguments?.getBoolean("fromNoti") == true
        val fromBooking = arguments?.getBoolean("fromBooking").getSafe()
        if (fromNoti.getSafe() || fromBooking)
            walkInId=arguments?.getInt("bookingId").getSafe()
        else
        walkInId = ordersViewModel.selectedOrder?.walkInLaboratory?.walkInLaboratoryId.getSafe()
        attachment_lab=arguments?.getBoolean("attachment_visibility_lab").getSafe()
        fromUploadForLab= arguments?.getBoolean("submitreview_lab").getSafe()
        ordersViewModel.page = 1
        walkinViewModel.fromDetails = true
        ordersViewModel.listItems= arrayListOf()
        mBinding.tvSpecialInstruction.gone()
        mBinding.tvSpecialInstructionDesc.gone()
        mBinding.divider1.gone()
       // mBinding.bSubmitForReview.isEnabled=true

        walkinViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)
        animBlink = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.blink
        )
        mBinding.bResubmit.isEnabled=true

        mBinding.rvAttachments.adapter = attachmentsAdapter
        getOrderDetails()
        addVisibilityAttchment(attachment_lab  || fromNoti==true)
       if (walkinViewModel.isAttachment) {
            callGetAttachments()
           setDataInViews(walkinViewModel.walkInResponse?.details)

       }
       else {
         //   getOrderDetails()
          // addVisibilityAttchment(attachment_lab)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        walkinViewModel.isAttachment = false
        walkinViewModel.partnerServiceId = 0




    }

    override fun getFragmentLayout() = R.layout.fragment_claim_order_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentClaimOrderDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                walkinViewModel.isFamilyMemberSelected=true
                findNavController().popBackStack()
            }
            bCancelRequest.setOnClickListener(this@WalkInLabOrdersDetailFragment)
            bCancelRequestLong.setOnClickListener(this@WalkInLabOrdersDetailFragment)
            bResubmit.setOnClickListener(this@WalkInLabOrdersDetailFragment)
            bSubmitForReview.setOnClickListener(this@WalkInLabOrdersDetailFragment)
            bAddReviewLong.setOnClickListener(this@WalkInLabOrdersDetailFragment)
            bContactHelplineLong.setOnClickListener(this@WalkInLabOrdersDetailFragment)

            attachmentsAdapter.onEditItemCall = {
                if (it.drawable == R.drawable.ic_play_arrow)
                    showVoiceNoteDialog(true)
            }
            attachmentsAdapter.onDeleteClick = { item, position ->
                mBinding.langData?.apply {
                    DialogUtils(requireActivity())
                        .showDoubleButtonsAlertDialog(
                            title = dialogsStrings?.confirmDelete.getSafe(),
                            message = dialogsStrings?.deleteDesc.getSafe(),
                            positiveButtonStringText = globalString?.yes.getSafe(),
                            negativeButtonStringText = globalString?.no.getSafe(),
                            buttonCallback = {
                                val attachmentId = walkinViewModel.walkInResponse?.details?.claimAttachments?.get(position)?.id.getSafe()
                                deleteClaimAttachment(attachmentId)
                            },
                        )
                }
            }

            iProvider.tvSubDesc.setOnClickListener {
                resendConfirmationApi()

                if(resendConfirmationTimer == null){
                    resendConfirmationTimer =
                        walkinViewModel.startTimer(
                            iProvider.tvSubDesc,mBinding.langData?.globalString?.sec.getSafe()
                            , metaData?.otpRespondTime,
                            onTick = { time ->
                                iProvider.tvSubDesc.isClickable = false
                                iProvider.tvSubDesc.alpha = 0.7f
                                iProvider.tvSubDesc.text = "${Constants.START}${langData?.walkInScreens?.resendConfirmation} ${Constants.GREATER_THAN} ($time) ${mBinding.langData?.globalString?.sec.getSafe()} ${Constants.END}"
                            },
                            onFinish = {
                                resendConfirmationTimer = null
                                iProvider.tvSubDesc.isClickable = true
                                iProvider.tvSubDesc.alpha = 1f
                                iProvider.tvSubDesc.text = "${Constants.START}${langData?.walkInScreens?.resendConfirmation} ${Constants.GREATER_THAN}${Constants.END}"
                            }
                        )
                }
            }

            tvVoiceNote.setOnClickListener {
                var hasRecording = false
                val fileList = walkinViewModel.fileList
                run breaking@{
                    fileList.forEach {
                        if (it.drawable == R.drawable.ic_play_arrow) {
                            hasRecording = true
                            return@breaking
                        }
                    }
                }
                if (hasRecording) {
                    DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                        title = langData?.globalString?.information.getSafe(),
                        message = langData?.dialogsStrings?.recordingAlreadyAdded.getSafe()
                    )

                    return@setOnClickListener
                }

                fileUtils.requestAudioPermission(requireActivity()) { result ->
                    if (result)
                        showVoiceNoteDialog(false)
                    else
                        displayNeverAskAgainDialog(langData?.dialogsStrings?.recordPermissions.getSafe())
                }
                tvVoiceNote.isClickable = false
            }
            tvUploadDoc.setOnClickListener {
                findNavController().safeNavigate(R.id.action_walkInLabOrdersDetailFragment_to_walkInUploadDocsFragment2, bundleOf("fromRequest" to true,
                    "from_lab" to true
                ))
            }

            tvAddImage.setOnClickListener {
                findNavController().safeNavigate(R.id.action_walkInLabOrdersDetailFragment_to_walkInUploadDocsFragment2, bundleOf("fromImage" to true,
                    "from_lab" to true))
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bSubmitForReview -> {
                var errorMsg: String? = null
                val mandatoryAttachments = metaData?.walkInLaboratoryDocumentType?.filter { it.required.getBoolean() }
                metaData?.walkInLaboratoryDocumentType?.filter { it.required.getBoolean() }
                    ?.let { (mandatoryAttachments as ArrayList).addAll(it) }

                run breaking@{
                    mandatoryAttachments?.forEach { mandatoryDoc ->
                        val result = walkinViewModel.walkInResponse?.details?.claimAttachments?.find { it.documentType == mandatoryDoc.genericItemId }
                        if(result == null){
                            errorMsg = langData?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }
                }

                if (errorMsg != null) {
                    DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                        title = langData?.globalString?.information.getSafe(),
                        message = errorMsg.getSafe()
                    )
                    return
                }

                updateStatus(
                    statusId = Enums.ClaimWalkInStatus.UNDER_REVIEW.id
                )
            }
            R.id.bAddReviewLong -> {
                showAddReviewDialog(walkinViewModel.walkInResponse?.details?.bookingDetails?.uniqueIdentificationNumber)
            }
            R.id.bCancelRequestLong, R.id.bCancelRequest -> {
                updateStatus(
                    statusId = Enums.ClaimWalkInStatus.CANCELLED.id
                )
            }
            R.id.bContactHelplineLong -> {
                requireActivity().openPhoneDialer("tel:051-11137777") // number will be dynamic later as discuss with QA
            }
        }
    }

    private fun setOrderButtons(response: WalkIn){
        mBinding.apply {
            groupButtons.gone()
            when(response.bookingDetails?.bookingStatusId){
                Enums.ClaimWalkInStatus.UNDER_REVIEW.id -> {

                }
                Enums.ClaimWalkInStatus.ON_HOLD.id -> {
                        bSubmitForReview.apply {
                            visible()
                            isEnabled = true


                        }
                }
                Enums.ClaimWalkInStatus.COMPLETED.id -> {
                    bAddReviewLong.visible()
                    if (response.review != null) {
                        setReviewViews(
                            true,
                            response.review?.review.getSafe(),
                            response.review?.rating.toString()
                        )
                        bAddReviewLong.gone()
                    }
                }
                Enums.ClaimWalkInStatus.UNAUTHORISED.id -> {
                    bContactHelplineLong.visible()
                }
            }
        }
    }

    private fun setBottomMsg(response: WalkIn) {
        mBinding.apply {
            var msg = ""
            tvMsg.setTextColor(ContextCompat.getColor(requireContext(), R.color.black90))

            when(response.bookingDetails?.bookingStatusId){
                Enums.ClaimWalkInStatus.UNDER_REVIEW.id -> {
                    msg = langData?.walkInScreens?.msgStatusUnderReview.getSafe()
                }
                Enums.ClaimWalkInStatus.ON_HOLD.id -> {
                    tvMsg.setTextColor(ContextCompat.getColor(requireContext(), R.color.call_red))
                    msg = langData?.walkInScreens?.msgStatusHold.getSafe()
                }
                Enums.ClaimWalkInStatus.UNAUTHORISED.id -> {
                    tvMsg.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.call_red))
                        visible()
                    }
                    msg = langData?.walkInScreens?.msgStatusUnauthorised.getSafe()
                        .replace("[0]", response.connection?.claimPackage?.name.getSafe())
                }
                Enums.ClaimWalkInStatus.COMPLETED.id -> {
                    if (response.review != null) {
                        setReviewViews(
                            true,
                            response.review?.review.getSafe(),
                            response.review?.rating.toString()
                        )
                    }
                }
            }

            tvMsg.text = msg
        }
    }

    private fun setOrderDetails(response: WalkIn) {
        val currency = metaData?.currencies?.find { it.genericItemId == response.bookingDetails?.currencyId }?.genericItemName

        val isStatusHold = response.bookingDetails?.bookingStatusId == Enums.ClaimWalkInStatus.ON_HOLD.id
        mBinding.apply {
            iProvider.apply {
                tvTitle.text = response.branch?.name
                tvDesc.text = "${langData?.globalString?.order} ${Constants.HASH} ${response.bookingDetails?.uniqueIdentificationNumber} ${Constants.PIPE} ${Constants.START}${getDateInFormat(response.createdAt.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")}${Constants.END}"
                ivIcon.loadImage(response.branch?.labs?.icon_url, R.drawable.ic_placeholder)
                tvSubDesc.setVisible(isStatusHold)
                tvSubDesc.text = "${Constants.START}${langData?.walkInScreens?.resendConfirmation} ${Constants.GREATER_THAN}${Constants.END}"
                ivIcon.loadImage(response.branch?.labs?.icon_url, R.drawable.ic_placeholder)
                if (response.amount.isNullOrEmpty().not()) {
                    tvRightDesc.apply {
                        setVisible(response.amount.isNullOrEmpty().not())
                        text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)
                            "${response.amount?.parseDouble()?.round(2).getSafe()} $currency"
                        else "$currency ${response.amount?.parseDouble()?.round(2).getSafe()}"
                    }
                }
            }

            actionbar.desc = metaData?.claimWalkInStatuses?.find { it.genericItemId == response.bookingDetails?.bookingStatusId }?.label.getSafe()

            tvBookBy.text = "${mBinding.langData?.globalString?.bookedBy}: ${response.bookingDetails?.customerUser?.fullName} | ${
                mBinding.langData?.globalString?.bookedFor
            }: ${if(response.bookingDetails?.bookedForUser?.id == DataCenter.getUser()?.id) langData?.globalString?.self else response.bookingDetails?.bookedForUser?.fullName}"

            val instructions = if(response.bookingDetails?.instructions.isNullOrEmpty()) langData?.labPharmacyScreen?.na else response.bookingDetails?.instructions
            tvSpecialInstructionDesc.text = instructions
            tvServiceProvider.apply {
                text = if (response.connection?.claimPackage?.name.isNullOrEmpty().not()) "${langData?.walkInScreens?.payment_} ${response.connection?.claimPackage?.name}" else "${langData?.walkInScreens?.payment_} "
            }
            if (response.bookingDetails?.paymentBreakdown?.total.isNullOrEmpty().not()) {
                tvPayableAmount.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${response.bookingDetails?.paymentBreakdown?.total.parseDouble()
                        .round(2)} $currency" else "$currency ${response.bookingDetails?.paymentBreakdown?.total.parseDouble().round(2)}"
            }
            if (response.bookingDetails?.paymentBreakdown?.packageAmount.isNullOrEmpty().not()) {
                tvPayAmount.apply {
                    text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)
                        "${response.bookingDetails?.paymentBreakdown?.packageAmount?.parseDouble()?.round(2).getSafe()} $currency"
                    else "$currency ${response.bookingDetails?.paymentBreakdown?.packageAmount?.parseDouble()?.round(2).getSafe()}"
                    setVisible(response.bookingDetails?.paymentBreakdown?.packageAmount.isNullOrEmpty().not())
                }
            }

            llAddAttachments.setVisible(isStatusHold)

            val orderDetails = arrayListOf<SplitAmount>()

            if (response.bookingDetails?.paymentBreakdown?.walkInName?.isNullOrEmpty()?.not().getSafe()) {
                orderDetails.add(
                    SplitAmount(specialityName = response.bookingDetails?.paymentBreakdown?.walkInName, fee = response.amount?.parseDouble()?.round(2))
                )
            }

            response.bookingDetails?.paymentBreakdown?.items?.forEach { items ->
                orderDetails.add(
                    SplitAmount(
                        specialityName = items.genericItemName.getSafe(),
                        fee = items.amount?.round(2).toString()
                    )
                )
            }

            val splitAmountAdapter = CheckoutSplitAmountAdapter()
            splitAmountAdapter.listItems = orderDetails
            rvSplitAmount.adapter = splitAmountAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDataInViews(response: WalkIn?) {
        response?.let {
//            response.bookingDetails?.bookingStatusId = Enums.ClaimWalkInStatus.ON_HOLD.id
            setOrderDetails(response)
            setOrderButtons(response)
            setBottomMsg(response)
            setAttachments(response)
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun showAddReviewDialog(partnerName: String?) {
        addReviewDialogBuilder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            addReviewBinding = DialogAddReviewBinding.inflate(layoutInflater)
            addReviewBinding.etInstructions.hint = langData?.globalString?.feedbackHere.getSafe()
            addReviewBinding.tvDesc.visible()
            addReviewBinding.tvDesc.text = if (partnerName != null)
                langData?.dialogsStrings?.callDialogReviewDesc?.replace("[0]", "${langData.globalString?.order} ${Constants.HASH} ${partnerName.getSafe()}")
            else langData?.myOrdersScreens?.callThankYou.getSafe()
            addReviewBinding.etInstructions.addTextChangedListener {
                val length = it?.length.getSafe()
                addReviewBinding.tvLength.text = getString(R.string.review_length_, length)
            }
            setView(addReviewBinding.root)
            setTitle(langData?.myOrdersScreens?.addAReview.getSafe())
            setPositiveButton(langData?.globalString?.save.getSafe()) { _, _ ->
                addReviewDialogBuilder.dismiss()
            }
            setNegativeButton(langData?.globalString?.cancel.getSafe(), null)
        }.create()

        addReviewDialogBuilder.setOnShowListener {
            dialogSaveButton = addReviewDialogBuilder.getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton?.setOnClickListener {
                val review = addReviewBinding.etInstructions.text.toString()
                val rating = addReviewBinding.ratingBar.rating.toDouble()
                if (isValidRating(rating).not()) {
                    showToast(langData?.fieldValidationStrings?.ratingValidation.getSafe())
                    return@setOnClickListener
                }
                this.review = review
                this.rating = rating.toString()
                val request = MyOrdersRequest(
                    bookingId = walkinViewModel.walkInResponse?.details?.bookingId, //ordersViewModel.bookingId.toInt(),
                    rating = rating,
                    review
                )
                addReview(request)
                addReviewDialogBuilder.dismiss()
            }
        }
        addReviewDialogBuilder.show()
    }

    private fun setAttachments(response: WalkIn) {
        val attachments: List<Attachment>? = response.claimAttachments
        isPrescriptionSet = (attachments?.filter { it.documentType == 20 }?.size ?: 0) > 0
        isInvoiceSet = (attachments?.filter { it.documentType == 19 }?.size ?: 0) > 0

        if (!(isPrescriptionSet && isInvoiceSet)) {
            mBinding.bSubmitForReview.isEnabled = false
        }
        if (attachments?.isEmpty().getSafe()) {
            mBinding.apply {
                rvAttachments.setVisible(false)
                tvNoData.setVisible(response.bookingDetails?.bookingStatusId != Enums.ClaimWalkInStatus.ON_HOLD.id)
            }
        } else {
            mBinding.apply {
                rvAttachments.setVisible(true)
                tvNoData.setVisible(false)

                if (isInvoiceSet&&isPrescriptionSet) {
                    bSubmitForReview.isEnabled = true
                }

                val mandatoryAttachments = walkinViewModel.documentTypes?.filter { it.required.getBoolean() }
                var errorMsg: String? = null
                run breaking@{
                    mandatoryAttachments?.forEach { mandatoryDoc ->
                        val attachedCount = attachments?.filter { it.documentType == mandatoryDoc.id }?.size
                        val count = response.bookingDetails?.settlementDocumentsRequired?.find { it.id == mandatoryDoc.id }?.count
                        val result = walkinViewModel.walkInResponse?.details?.claimAttachments?.find { it.documentType == mandatoryDoc.genericItemId }
                        if(result == null){
                            errorMsg = langData?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }

                    response.bookingDetails?.settlementDocuments?.forEach { mandatoryDoc ->
                        val attachedCount = attachments?.filter { it.documentType == mandatoryDoc.id }?.size
                        val count = response.bookingDetails?.settlementDocumentsRequired?.find { it.id == mandatoryDoc.id }?.count
                        val result = walkinViewModel.walkInResponse?.details?.claimAttachments?.find { it.documentType == mandatoryDoc.genericItemId }
                        if(result == null){
                            errorMsg = langData?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }
                }

               // bSubmitForReview.isEnabled = errorMsg == null


            }

            attachmentsAdapter.onItemClick = { item, _ ->
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
                var listTitle =
                    if (index == -1) it.attachments?.file else it.attachments?.file?.substring(
                        index.getSafe() + 1,
                        it.attachments?.file?.length.getSafe()
                    )

//                val docType = walkinViewModel.documentTypes?.find { docType ->
//                    docType.genericItemId == it.documentType }?.genericItemName
//
//                if(docType.isNullOrEmpty().not())
                    listTitle = "(${it.requestDocuments?.genericItemName.getSafe()}) $listTitle"


                list.add(
                    MultipleViewItem(
                        title = listTitle,
                        itemId = it.attachments?.file,
                        drawable = drawable,
                    ).apply {
                        if(response.bookingDetails?.bookingStatusId != Enums.ClaimWalkInStatus.ON_HOLD.id)
                            itemEndIcon = R.drawable.ic_arrow_fw_angular_black

                        if (it.attachmentTypeId == Enums.AttachmentType.VOICE.key) itemCenterIcon =
                            R.drawable.ic_voice_group

                        isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                    }
                )

                walkinViewModel.fileList = list
            }

            attachmentsAdapter.listItems = walkinViewModel.fileList
            lifecycleScope.launch {
                delay(100)
                mBinding.tvVoiceNote.isClickable = true
            }
        }
    }

    private fun displayNeverAskAgainDialog(message: String) {
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            message = message,
            positiveButtonStringText = mBinding.langData?.dialogsStrings?.permitManual.getSafe(),
            negativeButtonStringText = mBinding.langData?.globalString?.close.getSafe(),
            buttonCallback = {
                context?.let { gotoAppSettings(it) }
            },
            cancellable = false
        )

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
    }

    private fun showVoiceNoteDialog(itemId: String?) {
        val builder = AlertDialog.Builder(requireActivity()).apply {
            dialogViewBinding = FragmentAddVoicenoteBinding.inflate(layoutInflater)
            setView(dialogViewBinding.root)
            setTitle(R.string.voice_note)
            dialogViewBinding.IvPlay.tag = R.string.play
            setNegativeButton(R.string.cancel) { _, _ ->
                stopPlaying()
            }
            setCancelable(false)
            dialogViewBinding.apply {
                rlPlayLayout.setVisible(true)
                tvVoiceNote.setVisible(false)
                IvPlayDelete.setVisible(false)
                IvVoiceImage.setVisible(false)

                IvPlay.setOnClickListener {//rlPlayLayout
                    if (IvPlay.tag == R.string.play)
                        startPlaying(itemId)
                    else stopPlaying()
                }
            }
        }.create()

        builder.show()
    }

    private fun showVoiceNoteDialog(isShow: Boolean) {
        val builder = AlertDialog.Builder(requireActivity()).apply {
            dialogViewBinding = FragmentAddVoicenoteBinding.inflate(layoutInflater)
            setView(dialogViewBinding.root)
            setTitle(mBinding.langData?.dialogsStrings?.addVoiceNote)
            dialogViewBinding.IvPlay.tag = R.string.play
            dialogViewBinding.tvVoiceNote.text =
                mBinding.langData?.dialogsStrings?.msgRecordAudioWalkin
            dialogViewBinding.tvCancel.text = mBinding.langData?.globalString?.cancel
            if (isShow.not()) {
                setPositiveButton(mBinding.langData?.globalString?.done) { _, _ -> }
            }
            setNegativeButton(mBinding.langData?.globalString?.cancel) { _, _ ->
                absolutePath = ""
                stopPlaying()
                if (isShow.not()) {
                    stopRecording()
                    deleteAudioFile()
                }
                mBinding.tvVoiceNote.isClickable = true
            }

            setCancelable(false)
            dialogViewBinding.apply {

                if (isShow) {
                    rlPlayLayout.setVisible(true)
                    tvVoiceNote.setVisible(false)
                    IvPlayDelete.setVisible(false)
                    IvVoiceImage.setVisible(false)
                }
                meter = tvTimer

                IvVoiceImage.setOnClickListener {
                    file = fileUtils.createVoiceFile(requireContext())
                    meter.base = SystemClock.elapsedRealtime()
                    absolutePath = file?.absolutePath.getSafe()
                    startRecording()
                    val timer = object : CountDownTimer(3 * 60 * 1000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {}

                        override fun onFinish() {
                            rlPlayLayout.setVisible(true)
                            flPlayLayout.setVisible(false)
                            stopRecording()
                            validate()
                        }
                    }
                    timer.start()
                    flPlayLayout.setVisible(true)
                    IvVoiceImage.setVisible(false)
                    validate()
                }
                IvVoicePause.setOnClickListener {//flPlayLayout
                    rlPlayLayout.setVisible(true)
                    flPlayLayout.setVisible(false)
                    stopRecording()
                    validate()
                }
                tvCancel.setOnClickListener {//flPlayLayout
                    IvVoiceImage.setVisible(true)
                    stopRecording()
                    deleteAudioFile()
                    flPlayLayout.setVisible(false)
                    validate()
                }

                IvPlayDelete.setOnClickListener { //rlPlayLayout
                    IvVoiceImage.setVisible(true)
                    rlPlayLayout.setVisible(false)
                    stopPlaying()
                    deleteAudioFile()
                    validate()
                }
                IvPlay.setOnClickListener {//rlPlayLayout
                    if (IvPlay.tag == R.string.play)
                        startPlaying()
                    else stopPlaying()
                    if (isShow.not())
                        validate()
                }
            }
        }.create()
        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton?.setOnClickListener {
                if (dialogViewBinding.flPlayLayout.isVisible) {
                    elapsedMillis = SystemClock.elapsedRealtime() - meter.base
                    stopRecording()
                    if (elapsedMillis == 0L) {
                        deleteAudioFile()
                        dialogViewBinding.IvVoiceImage.setVisible(true)
                    } else
                        dialogViewBinding.rlPlayLayout.setVisible(true)
                    dialogViewBinding.flPlayLayout.setVisible(false)
                } else {
                    builder.dismiss()
                    saveFile()
                }
            }
            dialogSaveButton?.isEnabled = false
        }
        builder.show()
    }

    private fun saveFile() {
        if (file?.let {
                metaData?.maxFileSize?.let { it1 ->
                    fileUtils.photoUploadValidation(
                        it,
                        it1
                    )
                }
            }.getSafe()) {
            showFileSizeDialog()

        } else {
            addAttachmentApiCall()
        }
    }

    private fun deleteAudioFile() {
        if (file?.exists().getSafe()) {
            file?.delete()
        }
    }

    private fun validate() {
        dialogViewBinding.apply {
            dialogSaveButton?.isEnabled = !this.IvVoiceImage.isVisible

        }
    }

    private fun showFileSizeDialog() {
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = mBinding.langData?.globalString?.information.getSafe(),
                message = mBinding.langData?.dialogsStrings?.fileSize.getSafe(),
                buttonCallback = {},
            )
    }

    private fun startRecording() {
        Log.e("MainActivity", "file name ${file?.absolutePath}")

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireContext())
        } else {
            MediaRecorder()
        }
        Log.e("uri", "uri ${Uri.fromFile(file)}")
        recorder.apply {

            this?.setAudioSource(MediaRecorder.AudioSource.MIC)
            this?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            this?.setOutputFile(absolutePath)
            this?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                this?.prepare()
                this?.start()
                lifecycleScope.launch {
                    delay(1000L)

                    meter.start()
                    meter.startAnimation(animBlink)
                    dialogViewBinding.IvVoicePause.startAnimation(animBlink)


                }

            } catch (e: IOException) {
                Log.e("MainActivity", "prepare() failed")
            }


        }
    }

    private fun stopRecording() {
        meter.stop()
        try {
            if (recorder != null) {
                recorder?.setOnErrorListener(null);
                recorder?.setOnInfoListener(null);
                recorder?.setPreviewDisplay(null);
                recorder?.stop()
                recorder?.release()
                recorder = null
            }
        } catch (e: IllegalStateException) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
        } catch (e: RuntimeException) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
        } catch (e: Exception) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
        }
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

    private fun startPlaying() {
        Log.e("filesBName", "${file?.absolutePath} ")

        player = MediaPlayer().apply {
            try {
                dialogViewBinding.IvPlay.tag = R.string.pause
                dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_pause)

                setDataSource(absolutePath)

                prepare()
                seekTo(length)
                start()
            } catch (e: IOException) {
                Log.e("MainActivity", "prepare() failed")
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

    private fun getOrderDetails() {
        walkinViewModel.walkInRequest.walkInLaboratoryId = walkInId
        walkinViewModel.walkInRequest.walkInHospitalId = null
        walkinViewModel.walkInRequest.walkInPharmacyId = null

        walkinViewModel.walkInLabDetails(walkinViewModel.walkInRequest).observe(viewLifecycleOwner) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<WalkInResponse>
                    response.data?.let { walkInDetailResponse ->
                        walkinViewModel.walkInResponse = walkInDetailResponse
                        walkinViewModel.walkInRequest.walkInLaboratoryId = walkInDetailResponse.details?.id
                        walkinViewModel.documentTypes = walkInDetailResponse.details?.documentTypes
                        walkinViewModel.bookingId = walkInDetailResponse.details?.bookingId.getSafe()
                        walkinViewModel.partnerServiceId = walkInDetailResponse.details?.bookingDetails?.partnerServiceId.getSafe()
                        setDataInViews(walkInDetailResponse.details)
                    }
                }
                is ResponseResult.Failure -> {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = it.error.message.getSafe(),
                            buttonCallback = {
                                 findNavController().popBackStack()
                            },
                        )
                }
                is ResponseResult.ApiError -> {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                            buttonCallback = {
                                findNavController().popBackStack()
                            },
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

    private fun updateStatus(statusId: Int) {
        val request = ClaimStatusRequest(
            walkInLaboratoryId = walkinViewModel.walkInRequest.walkInLaboratoryId,
            bookingId = walkinViewModel.walkInResponse?.details?.bookingId,
            statusId = statusId
        )

        walkinViewModel.walkInLabStatus(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<*>
//                    showToast(getErrorMessage(response.message.toString()))

                    getOrderDetails()
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

    private fun addAttachmentApiCall() {
        val mediaList = ArrayList<MultipartBody.Part>()
        val uri = file?.let { fileUtils.getUriFromFile(context = requireContext(), it) }
        val mimeTypeView = uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }

        var mimeType: Int = 0
        val typeImage =
            if (mimeTypeView?.contains("image").getSafe()) mimeTypeView else ""
        when (mimeTypeView) {
            FileUtils.typeOther,
            FileUtils.typePDF -> {
                mimeType = Enums.AttachmentType.DOC.key
            }
            typeImage -> {
                mimeType = Enums.AttachmentType.IMAGE.key
            }
            else -> {
                mimeType = Enums.AttachmentType.VOICE.key
            }
        }
        val path = file?.absolutePath
        val multipartFile =
            fileUtils.convertFileToMultiPart(
                File(path),
                mimeTypeView.getSafe(),
                "attachments"
            )
        mediaList.add(multipartFile)

        val docType = walkinViewModel.walkInResponse?.details?.documentTypes?.find { it.genericItemName == "Other" }?.id.getSafe()

        if (isOnline(requireActivity())) {
            val request = AddWalkInAttachmentRequest(
                walkInLaboratoryId = walkInId,
                labId = walkinViewModel.walkInResponse?.details?.laboratoryId,
                attachmentType = mimeType,
                documentType = docType, //other for now
                attachments = mediaList
            )

            walkinViewModel.addWalkInLabAttachment(request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            callGetAttachments()
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
                } else {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                            message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                            buttonCallback = {},
                        )
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

    private fun deleteClaimAttachment(id: Int) {


        if (isOnline(requireActivity())) {
            val request =
                WalkInAttachmentRequest(walkInLaboratoryAttachmentId = id)

            walkinViewModel.deleteWalkInLabAttachment(request = request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<*>
                            try {
//                                showToast(getErrorMessage(response.message.getSafe()))
                                callGetAttachments()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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
                } else {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                            message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                            buttonCallback = {},
                        )
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
            val request = WalkInAttachmentRequest(
                walkInLaboratoryId = walkinViewModel.walkInRequest.walkInLaboratoryId.getSafe()
            )

            walkinViewModel.callGetWalkInLabAttachments(request).observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<AttachmentResponse>
                            walkinViewModel.fileList = arrayListOf()
                            val data = response.data?.attachments.getSafe()
                            walkinViewModel.walkInResponse?.details?.let {
                                it.claimAttachments = data
                                setAttachments(it)
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
    private fun addVisibilityAttchment(attachment_visibility:Boolean) {
        if (attachment_visibility) {

            mBinding.attachmentView.visible()
            mBinding.headingM.visible()
            mBinding.llAddAttachments.visible()
            mBinding.tvVoiceNote.visible()
            mBinding.tvAddImage.visible()
            mBinding.tvUploadDoc.visible()
            mBinding.rvAttachments.visible()
            mBinding.tvNoData.visible()
        }
    }
    private fun resendConfirmationApi() {
        if (isOnline(requireActivity())) {
            val request = WalkInRequest(
                walkInLaboratoryId = walkInId,
                labId = walkinViewModel.walkInResponse?.details?.laboratoryId.getSafe(),
                amount = walkinViewModel.walkInResponse?.details?.amount.getSafe(),
                bookingId = walkinViewModel.bookingId.getSafe()
            )

            walkinViewModel.resendWalkInLabConfirmation(request).observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            showToast(langData?.dialogsStrings?.confirmationSend.getSafe())
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
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun setReviewViews(isShow: Boolean, review: String, stars: String) {
        mBinding.apply {
            tvMsg.setVisible(isShow.not())
            llButtons.setVisible(isShow.not())
            bAddReviewLong.setVisible(isShow.not())
            bCancelRequest.setVisible(isShow.not())
            llReviews.setVisible(isShow)
            tvShowReview.apply {
                setVisible(isShow)
                text = review
            }
            if (stars > "0.0") {
                tvStars.apply {
                    visible()
                    text = if (stars == "1") "$stars ${langData?.myOrdersScreens?.star}" else "$stars ${
                        langData?.myOrdersScreens?.stars
                    }"
                }
            } else {
                tvStars.gone()
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
                    getOrderDetails()
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
}