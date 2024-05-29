package com.homemedics.app.ui.fragment.claim

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.request.claim.AddClaimAttachmentRequest
import com.fatron.network_module.models.request.claim.ClaimAttachmentRequest
import com.fatron.network_module.models.request.claim.ClaimStatusRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.claim.Claim
import com.fatron.network_module.models.response.claim.ClaimResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentClaimOrderDetailsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.adapter.CheckoutSplitAmountAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ClaimViewModel
import com.homemedics.app.viewmodel.MyOrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File

class ClaimOrdersDetailFragment : BaseFragment(), View.OnClickListener {
   private lateinit var audio: CustomAudio
    private val claimViewModel: ClaimViewModel by activityViewModels()
    private val ordersViewModel: MyOrderViewModel by activityViewModels()

    private lateinit var mBinding: FragmentClaimOrderDetailsBinding
    private var locale = ""
    private var elapsedMillis = 0L
    private var file: File? = null
    private lateinit var fileUtils: FileUtils
    private lateinit var animBlink: Animation
    private var attachmentsAdapter = AddMultipleViewAdapter()

    private var claimId = 0

    val langData = ApplicationClass.mGlobalData

    override fun setLanguageData() {
        mBinding.langData = langData
        mBinding.apply {
            actionbar.title = langData?.claimScreen?.claim.getSafe()
        }
    }

    override fun onPause() {
        super.onPause()
        audio.onPause()
    }

    override fun init() {
        locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        audio  = CustomAudio(requireContext())
        addVisibilityAttchment(true)
        val fromNoti=arguments?.getBoolean("fromNoti")
        if (fromNoti.getSafe())
            claimId=arguments?.getInt("bookingId").getSafe()
        else
            claimId = ordersViewModel.selectedOrder?.claim?.claimId.getSafe()
        claimViewModel.fromDetails = true
        ordersViewModel.page = 1
        ordersViewModel.listItems= arrayListOf()
        claimViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)
        animBlink = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.blink
        )

        mBinding.rvAttachments.adapter = attachmentsAdapter
//        if (claimViewModel.isAttachment) {
//            callGetAttachments()
//            setDataInViews(claimViewModel.claimResponse?.details)
//        } else {
            getOrderDetails()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        claimViewModel.isAttachment = false
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
                findNavController().popBackStack()
            }
            bCancelRequest.setOnClickListener(this@ClaimOrdersDetailFragment)
            bCancelRequestLong.setOnClickListener(this@ClaimOrdersDetailFragment)
            bResubmit.setOnClickListener(this@ClaimOrdersDetailFragment)

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
                                val attachmentId = claimViewModel.claimResponse?.details?.claimAttachments?.get(position)?.id.getSafe()
                                deleteClaimAttachment(attachmentId)
                            },
                        )
                }
            }

            tvVoiceNote.setOnClickListener {
                var hasRecording = false
                val fileList = claimViewModel.fileList
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
                findNavController().safeNavigate(ClaimOrdersDetailFragmentDirections.actionClaimOrdersDetailFragmentToClaimUploadDocsFragment())
            }

            tvAddImage.setOnClickListener {
                findNavController().safeNavigate(R.id.action_claimOrdersDetailFragment_to_claimUploadDocsFragment, bundleOf("fromImage" to true))
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bResubmit -> {
                var errorMsg: String? = null
                val mandatoryAttachments = claimViewModel.claimResponse?.details?.settlementDocuments?.filter { it.required.getBoolean() }
                claimViewModel.documentTypes?.filter { it.required.getBoolean() }
                    ?.let { (mandatoryAttachments.getSafe()).addAll(it) }

                run breaking@{
                    mandatoryAttachments?.forEach { mandatoryDoc ->
                        val result = claimViewModel.claimResponse?.details?.claimAttachments?.find { it.documentType == mandatoryDoc.genericItemId }
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
            R.id.bCancelRequest, R.id.bCancelRequestLong -> {
                DialogUtils(requireActivity())
                    .showDoubleButtonsAlertDialog(
                        title = langData?.dialogsStrings?.areYouSure.getSafe(),
                        message = langData?.dialogsStrings?.cancelClaimMsg.getSafe(),
                        buttonCallback = { updateStatus(
                            statusId = Enums.ClaimWalkInStatus.CANCELLED.id
                        ) },
                        positiveButtonStringText = langData?.globalString?.yes.getSafe(),
                        negativeButtonStringText = langData?.globalString?.goBack.getSafe(),
                        negativeButtonCallback = { }
                    )
            }
        }
    }

    private fun setOrderButtons(response: Claim){
        mBinding.apply {
            groupButtons.gone()

            when(response.bookingDetails?.bookingStatusId){
                Enums.ClaimWalkInStatus.UNDER_REVIEW.id -> {
                    bCancelRequestLong.visible()
                    val currency = metaData?.currencies?.find { it.genericItemId == response.bookingDetails?.currencyId }?.genericItemName.getSafe()
                    tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${response.amount.getSafe()} $currency" else "${Constants.START}$currency ${response.amount.getSafe()}${Constants.END}"
                }
                Enums.ClaimWalkInStatus.CANCELLED.id -> {

                }
                Enums.ClaimWalkInStatus.ON_HOLD.id -> {
                    llButtons.visible()
                    bResubmit.isEnabled = true
                }
                Enums.ClaimWalkInStatus.SETTLEMENT_IN_PROGRESS.id -> {
                    val currency = metaData?.currencies?.find { it.genericItemId == response.bookingDetails?.currencyId }?.genericItemName.getSafe()
                    tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${response.amount.getSafe()} $currency" else "${Constants.START}$currency ${response.amount.getSafe()}${Constants.END}"
                }
                Enums.ClaimWalkInStatus.REJECTED.id -> {

                }
                Enums.ClaimWalkInStatus.SETTLEMENT_ON_HOLD.id -> {

                }
                Enums.ClaimWalkInStatus.SETTLED.id -> {
                    val currency = metaData?.currencies?.find { it.genericItemId == response.bookingDetails?.currencyId }?.genericItemName.getSafe()
                    tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${response.amount.getSafe()} $currency" else "${Constants.START}$currency ${response.amount.getSafe()}${Constants.END}"
                }
                Enums.ClaimWalkInStatus.PACKAGE_SELECTION_PENDING.id -> {

                }
            }
        }
    }

    private fun setBottomMsg(response: Claim) {
        mBinding.apply {
            var msg = ""
            tvMsg.setTextColor(ContextCompat.getColor(requireContext(), R.color.black90))

            when (response.bookingDetails?.bookingStatusId) {
                Enums.ClaimWalkInStatus.UNDER_REVIEW.id -> {
                    msg = langData?.claimScreen?.msgClaimSubmitted.getSafe()
                }
                Enums.ClaimWalkInStatus.CANCELLED.id -> {
                    msg = langData?.claimScreen?.msgClaimCancelled.getSafe()
                }
                Enums.ClaimWalkInStatus.ON_HOLD.id -> {
                    tvMsg.setTextColor(ContextCompat.getColor(requireContext(), R.color.call_red))
                    msg = langData?.claimScreen?.msgClaimOnHold.getSafe()
                }
                Enums.ClaimWalkInStatus.SETTLEMENT_IN_PROGRESS.id -> {
                    msg = langData?.claimScreen?.msgClaimSettlementInProgress.getSafe()
                }
                Enums.ClaimWalkInStatus.REJECTED.id -> {
                    tvMsg.setTextColor(ContextCompat.getColor(requireContext(), R.color.call_red))
                    msg = langData?.claimScreen?.msgClaimRejected.getSafe()
                }
                Enums.ClaimWalkInStatus.SETTLEMENT_ON_HOLD.id -> {
                    tvMsg.setTextColor(ContextCompat.getColor(requireContext(), R.color.call_red))
                    msg = langData?.claimScreen?.msgClaimSettlementOnHold.getSafe()
                }
                Enums.ClaimWalkInStatus.SETTLED.id -> {
                    msg = langData?.claimScreen?.msgClaimSettled.getSafe()
                }
                Enums.ClaimWalkInStatus.PACKAGE_SELECTION_PENDING.id -> {

                }
            }
            val claimReason = claimViewModel.reason.toString()
            claimViewModel.comment

            if (response.bookingDetails?.bookingStatusId == 3 || response.bookingDetails?.bookingStatusId== 14 || response.bookingDetails?.bookingStatusId == 7) {


                if (claimReason.equals("null")) {
                    tvMsg.text = msg //{"$msg\n\n  Reason:N/A\n Comment:N/A"}.toString()
                } else {

                    if (claimViewModel.comment == "" || claimViewModel.comment.isNullOrEmpty()) {
                        val spannableString =
                            SpannableString("$msg\n\n   Reason: $claimReason\n  Comment: N/A")

                        // Set color for the reason part (starting from index msg.length + 2 to the end)
                        val start = msg.length + 2 // adding 2 for ": "
                        val end = spannableString.length
                        spannableString.setSpan(
                            ForegroundColorSpan(
                                ContextCompat.getColor(
                                    requireContext(),
                                    android.R.color.black
                                )
                            ),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        tvMsg.text = spannableString
                    } else {
                        val spannableString =
                            SpannableString("$msg\n\n   Reason: $claimReason\n  Comment: ${claimViewModel.comment} ")
                        // Set color for the reason part (starting from index msg.length + 2 to the end)
                        val start = msg.length + 2 // adding 2 for ": "
                        val end = spannableString.length
                        spannableString.setSpan(
                            ForegroundColorSpan(
                                ContextCompat.getColor(
                                    requireContext(),
                                    android.R.color.black
                                )
                            ),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        tvMsg.text = spannableString
                    }


                }
            } else {
                tvMsg.text = msg
            }
        }
    }

    private fun setOrderDetails(response: Claim) {

        val currency = metaData?.currencies?.find { it.genericItemId == response.bookingDetails?.currencyId }?.genericItemName

        val isStatusHold = response.bookingDetails?.bookingStatusId == Enums.ClaimWalkInStatus.ON_HOLD.id
        mBinding.apply {
            iProvider.apply {
                tvTitle.text = response.serviceProvider
                tvDesc.text = "${langData?.claimScreen?.claim} ${Constants.HASH} ${response.bookingDetails?.uniqueIdentificationNumber} ${Constants.PIPE} ${Constants.START}${getDateInFormat(response.createdAt.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")}${Constants.END}"
                ivIconSmall.loadImage(R.drawable.ic_healthcare_black)
                ivIconSmall.visible()
                ivIcon.gone()
                tvRightDesc.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${response.amount.getSafe()} $currency" else "${Constants.START}$currency ${response.amount}${Constants.END}"
            }

            actionbar.desc = metaData?.claimWalkInStatuses?.find { it.genericItemId == response.bookingDetails?.bookingStatusId }?.label.getSafe()

            tvBookBy.text = "${mBinding.langData?.globalString?.bookedBy}: ${response.bookingDetails?.customerUser?.fullName} | ${
                mBinding.langData?.globalString?.bookedFor
            }: ${if(response.bookingDetails?.bookedForUser?.id == DataCenter.getUser()?.id) langData?.globalString?.self else response.bookingDetails?.bookedForUser?.fullName}"

            val comment = if(response.bookingDetails?.instructions.isNullOrEmpty()) langData?.globalString?.noInstructions else response.bookingDetails?.instructions
            tvSpecialInstructionDesc.text = comment
            tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${response.bookingDetails?.paymentBreakdown?.total} $currency" else "${Constants.START}$currency ${response.bookingDetails?.paymentBreakdown?.total}${Constants.END}"
            tvServiceProvider.text = response.connection?.claimPackage?.name
            llAddAttachments.setVisible(isStatusHold)
            if (response.settlements != null) {
                tvPayAmount.apply {
                    text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${response.settlements?.actualAmount.getSafe()} $currency" else "${Constants.START}$currency ${response.settlements?.actualAmount.getSafe()}${Constants.END}"
                    visible()
                }
            }


            val service = "${metaData?.claimCategories?.find {
                it.genericItemId == response.claimCategoryId
            }?.genericItemName}"

            var orderDetails = arrayListOf<SplitAmount>()
            if (
                response.bookingDetails?.bookingStatusId == Enums.ClaimWalkInStatus.UNDER_REVIEW.id ||
                        response.bookingDetails?.bookingStatusId == Enums.ClaimWalkInStatus.SETTLED.id ||
                        response.bookingDetails?.bookingStatusId == Enums.ClaimWalkInStatus.SETTLEMENT_IN_PROGRESS.id
            ) {
                if (response.bookingDetails?.paymentBreakdown?.items.isNullOrEmpty()) {
                    val serviceCharges = "$service ${langData?.claimScreen?.claim.getSafe().lowercase()}"
                    orderDetails = arrayListOf(
                        SplitAmount(specialityName = serviceCharges, fee = response.amount),
                    )
                }
            }
            response.bookingDetails?.paymentBreakdown?.items?.forEach { items ->
                orderDetails.add(
                    SplitAmount(
                        specialityName = items.genericItemName.getSafe(),
                        fee = items.amount?.toString()
                    )
                )
            }

            val splitAmountAdapter = CheckoutSplitAmountAdapter()
            splitAmountAdapter.listItems = orderDetails
            rvSplitAmount.adapter = splitAmountAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDataInViews(response: Claim?) {
        response?.let {
//            response.bookingDetails?.bookingStatusId = Enums.ClaimStatus.SETTLEMENT_IN_PROGRESS.id
            setOrderDetails(response)
            setOrderButtons(response)
            setBottomMsg(response)
            setAttachments(response)
        }
    }

    private fun setAttachments(response: Claim) {
        val attachments: List<Attachment>? = response.claimAttachments
        if (attachments?.isEmpty().getSafe()) {
            mBinding.apply {
                rvAttachments.setVisible(false)
                tvNoData.setVisible(response.bookingDetails?.bookingStatusId != Enums.ClaimWalkInStatus.ON_HOLD.id)
            }
        } else {
            mBinding.apply {
                rvAttachments.setVisible(true)
                tvNoData.setVisible(false)

                val mandatoryAttachments = claimViewModel.documentTypes?.filter { it.required.getBoolean() }
                var errorMsg: String? = null
                run breaking@{
                    mandatoryAttachments?.forEach { mandatoryDoc ->
//                        val attachedCount = attachments?.filter { it.documentType == mandatoryDoc.id }?.size
                        val result = attachments?.find { it.documentType == mandatoryDoc.genericItemId }
//                        val count = response.bookingDetails?.settlementDocumentsRequired?.find { it.id == mandatoryDoc.id }?.count
                        if(result == null){ //|| attachedCount != count
                            errorMsg = langData?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }

                    response.bookingDetails?.settlementDocuments?.forEach { mandatoryDoc ->
//                        val attachedCount = attachments?.filter { it.documentType == mandatoryDoc.id }?.size
                        val result = attachments?.find { it.documentType == mandatoryDoc.genericItemId }
//                        val count = response.bookingDetails?.settlementDocumentsRequired?.find { it.id == mandatoryDoc.id }?.count
                        if(result == null){
                            errorMsg = langData?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }
                }

                bResubmit.isEnabled = errorMsg == null || claimViewModel.claimResponse?.details?.bookingDetails?.settlementDocumentsRequired.isNullOrEmpty()
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

//                val docType = claimViewModel.documentTypes?.find { docType ->
//                    docType.genericItemId == it.documentType }?.genericItemName

//                if(docType.isNullOrEmpty().not())
                    listTitle = "(${it.requestDocuments?.genericItemName}) $listTitle"

                val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
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

                claimViewModel.fileList = list
            }

            attachmentsAdapter.listItems = claimViewModel.fileList
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

        audio.apply {
            title =  mBinding.langData?.globalString?.voiceNote.getSafe()
            negativeButtonText =  mBinding.langData?.globalString?.cancel.getSafe()
            voiceNote = mBinding.langData?.dialogsStrings?.voiceNoteDescription.getSafe()
            cancel = mBinding.langData?.globalString?.cancel.getSafe()
            fileName=itemId.getSafe()
            isPlayOnly=true
            show()
            tvVoiceNote = mBinding.tvVoiceNote
        }
    }

    private fun showVoiceNoteDialog(isShow: Boolean) {
        audio.apply {
            title =  mBinding.langData?.dialogsStrings?.addVoiceNote.getSafe()
            isPlayOnly=isShow
            positiveButtonText =  mBinding.langData?.globalString?.done.getSafe()
            negativeButtonText =  mBinding.langData?.globalString?.cancel.getSafe()
            voiceNote = mBinding.langData?.dialogsStrings?.voiceNoteDescription.getSafe()
            cancel = mBinding.langData?.globalString?.cancel.getSafe()
            onSaveFile={mfile,time->
                file=mfile
                elapsedMillis=time
                saveFile()
            }
            show()
            tvVoiceNote = mBinding.tvVoiceNote
        }
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

    private fun addVisibilityAttchment(attachment_visibility:Boolean){
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

    private fun showFileSizeDialog() {
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = mBinding.langData?.globalString?.information.getSafe(),
                message = mBinding.langData?.dialogsStrings?.fileSize.getSafe(),
                buttonCallback = {},
            )
    }


    private fun getOrderDetails() {
        claimViewModel.claimRequest.claimId = claimId
        claimViewModel.claimDetails().observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<ClaimResponse>
                    response.data?.let { claimDetailResponse ->
                      claimViewModel.reason=  claimDetailResponse.details?.bookingDetails?.bookingReason?.reason?.reason
                        claimViewModel.comment=claimDetailResponse.details?.bookingDetails?.bookingReason?.comment.toString()
                        claimViewModel.claimBookingId=claimDetailResponse.details?.bookingId?.toInt()
                        claimViewModel.claimRequest.claimId = claimDetailResponse.details?.id
                        claimViewModel.documentTypes = claimDetailResponse.details?.documentTypes
                        setDataInViews(claimDetailResponse.details)
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
            claimId = claimId,
            bookingId =claimViewModel.claimBookingId, //claimViewModel.claimResponse?.details?.bookingDetails?.bookingReason?.booking_id,
            statusId = statusId
        )
        claimViewModel.cancelClaim(request).observe(this) {
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

        val request = AddClaimAttachmentRequest(
            claimId = claimId,
            claimCategoryId = claimViewModel.claimResponse?.details?.claimCategoryId,
            attachmentType = mimeType,
            documentType = 4, //other for now
            attachments = mediaList
        )

        if (isOnline(requireActivity())) {
            claimViewModel.addClaimAttachment(request).observe(this) {
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
        val request =
            DeleteAttachmentRequest(claimAttachmentId = id)

        if (isOnline(requireActivity())) {
            claimViewModel.deleteClaimAttachment(request = request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<*>
                            try {
                                showToast(getErrorMessage(response.message.getSafe()))
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
            claimViewModel.callGetAttachments(
                ClaimAttachmentRequest(
                    claimId = claimViewModel.claimRequest.claimId.getSafe()
                )
            )
                .observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<AttachmentResponse>
                            claimViewModel.fileList = arrayListOf()
                            val data = response.data?.attachments.getSafe()
                            claimViewModel.claimResponse?.details?.let {
                                it.claimAttachments = data
                                setAttachments(it)
                            }

                        }
                        is ResponseResult.Failure -> {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message = it.error.message.getSafe(),
                                    buttonCallback = { },
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
                    buttonCallback = {

                    },
                )
        }
    }
}