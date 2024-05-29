package com.homemedics.app.ui.fragment.walkin.laboratory

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.walkin.*
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.claim.ClaimConnection
import com.fatron.network_module.models.response.claim.ClaimConnectionsResponse
import com.fatron.network_module.models.response.walkin.WalkIn
import com.fatron.network_module.models.response.walkin.WalkInResponse
import com.fatron.network_module.models.response.walkinpharmacy.WalkInInitialResponse
import com.fatron.network_module.models.response.walkinpharmacy.WalkInStoreResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.databinding.FragmentWalkinPharmacyDetailsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.adapter.LinkedConnectionsAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.WalkInViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import timber.log.Timber
import java.io.File
import java.io.IOException

class WalkinLabDetailFragment: BaseFragment(), View.OnClickListener {

    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private val walkinViewModel: WalkInViewModel by activityViewModels()
    private lateinit var mBinding: FragmentWalkinPharmacyDetailsBinding
    private lateinit var dialogViewBinding: FragmentAddVoicenoteBinding
    private var absolutePath: String = ""
    private var file: File? = null
    private lateinit var meter: Chronometer
    private var length = 0
    private lateinit var fileUtils: FileUtils
    private var locale: String? = null
    private var dialogSaveButton: Button? = null
    private var elapsedMillis = 0L
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private lateinit var animBlink: Animation
    private var walkInId = 0
    private lateinit var audio: CustomAudio
    private var isPrescriptionSet = false
    private var isInvoiceSet = false
    private var attachmentsAdapter = AddMultipleViewAdapter()
    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = walkInViewModel.walkInLabName.getSafe()
            tvPatientDetail.text = lang?.labPharmacyScreen?.patientDetails.getSafe()
            cdPatients.hint = lang?.globalString?.patient.getSafe()
            etInvoice.hint = lang?.walkInScreens?.enterInvoiceAmount.getSafe()

        }
    }

    override fun init() {
        audio = CustomAudio(requireContext())
        fileUtils = FileUtils()
        fileUtils.init(this)
        audio = CustomAudio(requireContext())
        mBinding.rvAttachments.adapter = attachmentsAdapter
//        getWalkInLaboratoryConnections()
     //   amountEnabled()
        if (!walkinViewModel.isAttachment) {
            initialWalkInLaboratory()
            // callGetAttachments()
            setDataInViews(walkinViewModel.walkInResponse?.details) }
        else {
            getOrderDetails()
            getWalkInLaboratoryConnections()
            setDataInDropDownView(walkinViewModel.walkInInitialResponse)
            // getOrderDetails()
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_walkin_pharmacy_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkinPharmacyDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    findNavController().popBackStack()
                }
                onAction2Click = {
                    openDefaultMap(
                        walkInViewModel.mapLatLng, requireActivity()
                    )
                }
            }
            bSendRequest.setOnClickListener(this@WalkinLabDetailFragment)
            cdPatients.onItemSelectedListener = { _, index ->
                val selectedUser = walkInViewModel.walkInInitialResponse.walkInLaboratory?.familyMembers?.get(index)

                walkInViewModel.familyMemberId = selectedUser?.familyMemberId.getSafe()

                cdPatients.selectionIndex = index

              //  initialWalkInLaboratory(familyMemberId = walkInViewModel.familyMemberId)
            }
            etInvoice.mBinding.editText.doAfterTextChanged { amount ->
                if (amount?.isNotEmpty().getSafe()) {
                    try {
                        bSendRequest.isEnabled = walkInViewModel.selectedConnection?.isChecked.getSafe() &&
                                amount?.toString()?.toDouble().getSafe() <= walkInViewModel.selectedConnection?.claimPackage?.credit?.amount.getSafe() &&
                                amount?.toString()?.toDouble().getSafe() > 0.0 && isPrescriptionSet && isInvoiceSet
                    } catch (e: Exception) {
                        Timber.e("$e")
                    }
                    if (mBinding.etInvoice.mBinding.editText.text?.isNotEmpty() == true){
                        if(!(isPrescriptionSet && isInvoiceSet))
                        {
                            DialogUtils(requireActivity())
                                .showDoubleButtonsAlertDialog(
                                    message = "kindly attach  your invoice and prescription.",
                                    buttonCallback = {
                                    },
                                    negativeButtonCallback = {},
                                    cancellable = false
                                )
                        }
                    }
                } else {
                    bSendRequest.isEnabled = false
                }
            }

            attachmentsAdapter.onDeleteClick = { item, position ->
                mBinding.lang?.apply {
                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                        title = dialogsStrings?.confirmDelete.getSafe(),
                        message = dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = globalString?.yes.getSafe(),
                        negativeButtonStringText = globalString?.no.getSafe(),
                        buttonCallback = {
                            val attachmentId =
                                walkinViewModel.walkInResponse?.details?.claimAttachments?.get(
                                    position
                                )?.id.getSafe()
                            deleteClaimAttachment(attachmentId)
                        },
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
                    title = lang?.globalString?.information.getSafe(),
                    message = lang?.dialogsStrings?.recordingAlreadyAdded.getSafe()
                )

                return@setOnClickListener
            }

            fileUtils.requestAudioPermission(requireActivity()) { result ->
                if (result)
                    showVoiceNoteDialog(false)
                else

                    displayNeverAskAgainDialog(lang?.dialogsStrings?.recordPermissions.getSafe())

            }
            tvVoiceNote.isClickable = false
        }





        tvAddImage.setOnClickListener {
            findNavController().safeNavigate(R.id.action_walkinLabDetailFragment_to_walkInUploadDocsFragment7,
                bundleOf("fromImage" to true,
                "from_lab" to true
            ))


           /* forImage=true
            if (forImage) {
                mBinding.apply {
                    actionbar.title = lang?.globalString?.uploadImage.getSafe()
                }
                fileUtils.requestPermissions(
                    requireActivity(),


                    ) { result ->
                    takeAction(result)
                    amountEnable=true
                    forImage=false
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "application/pdf"
                    startActivityForResult(intent, 1)
                } else {
                    fileUtils.requestFilePermissions(requireActivity(), false,true) { result ->
                        takeAction(result)
                        //amountEnableSecond=true
                    }
                }
            }*/
        }






        tvUploadDoc.setOnClickListener {
            findNavController().safeNavigate(R.id.action_walkinLabDetailFragment_to_walkInUploadDocsFragment7,
                bundleOf("fromRequest" to true,
                "from_lab" to true))

           /* if (forImage) {
                mBinding.apply {
                    actionbar.title = lang?.globalString?.uploadImage.getSafe()
                }
                fileUtils.requestPermissions(
                    requireActivity(),


                    ) { result ->
                    takeAction(result)
                }
             } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "application/pdf"
                    startActivityForResult(intent, 1)
                } else {
                    fileUtils.requestFilePermissions(requireActivity(), false,true) { result ->
                        takeAction(result)
                    }
                }
                amountEnableSecond=true
                amountEnabled()
            }*/
        }
            if (walkinViewModel.isFamilyMemberSelected){
                etInvoice.mBinding.editText.setText("")
            }

       }
    }

    override fun onDestroy() {
        super.onDestroy()
        walkInViewModel.selectedConnection?.isChecked = false
        walkInViewModel.familyMemberId = 0
        walkinViewModel.isAttachment=false
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bSendRequest -> {
                val amount = mBinding.etInvoice.text
                if (isValid(amount).not()) {
                    mBinding.etInvoice.errorText = mBinding.lang?.fieldValidationStrings?.invoiceAmountEmpty.getSafe()
                    mBinding.etInvoice.requestFocus()
                    mBinding.nestedScrollView.fullScroll(ScrollView.FOCUS_UP)
                    return
                }
                showRequestSubmitDialog()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 1 && resultCode === Activity.RESULT_OK) {
            val selectedFileUri: Uri? = data?.data
            file = selectedFileUri.let { uri ->
                uri?.let {
                    fileUtils.getMimeType(requireContext(), uri = uri)?.let { it1 ->
                        fileUtils.copyUriToFile(
                            requireContext(),
                            it,
                            fileUtils.getFileNameFromUri(
                                requireContext(), uri
                            ),
                            it1
                        )
                    }
                }
            }
            saveFile()
        }
    }
    private fun takeAction(result: FileUtils.FileData?) {
        if (result == null) {
            //displayNeverAskAgainDialog(langData?.dialogsStrings?.storagePermissions.getSafe())
        } else {
            file = result.uri.let { uri ->
                uri?.let {
                    fileUtils.getMimeType(requireContext(), uri = uri)?.let { it1 ->
                        fileUtils.copyUriToFile(
                            requireContext(),
                            it,
                            fileUtils.getFileNameFromUri(
                                requireContext(), uri
                            ),
                            it1
                        )
                    }
                }
            }
            saveFile()
        }
    }

    private fun amountEnabled(){

            mBinding.etInvoice.mBinding.editText.isEnabled = true


            mBinding.etInvoice.mBinding.editText.doAfterTextChanged { amount ->
                if (amount?.isNotEmpty().getSafe()) {
                    try {
                        mBinding.bSendRequest.isEnabled = walkInViewModel.selectedConnection?.isChecked.getSafe() &&
                                amount?.toString()?.toDouble().getSafe() <= walkInViewModel.selectedConnection?.claimPackage?.credit?.amount.getSafe() &&
                                amount?.toString()?.toDouble().getSafe() > 0.0
                    } catch (e: Exception) {
                        Timber.e("$e")
                    }
                } else {
                    mBinding.bSendRequest.isEnabled = false
                }
            }

    }
    @SuppressLint("SetTextI18n")
    private fun setDataInViews(response: WalkIn?) {
        response?.let {
//            response.bookingDetails?.bookingStatusId = Enums.ClaimWalkInStatus.ON_HOLD.id

            setAttachments(response)
        }
    }

    private fun showRequestSubmitDialog() {
        val defaultFamilyMember = walkInViewModel.walkInInitialResponse.walkInLaboratory?.familyMembers?.get(0)?.familyMemberId
        DialogUtils(requireActivity())
            .showDoubleButtonsAlertDialog(
                title = mBinding.lang?.dialogsStrings?.areYouSure.getSafe(),
                message = mBinding.lang?.dialogsStrings?.walkInServiceRequest.getSafe(),
                positiveButtonStringText = mBinding.lang?.globalString?.yes.getSafe(),
                negativeButtonStringText = mBinding.lang?.globalString?.cancel.getSafe(),
                buttonCallback = {
                    val amount = mBinding.etInvoice.text
                    val request = WalkInStoreRequest(
                        amount = amount.toDouble(),
                        connectionId = walkInViewModel.selectedConnection?.id,
                        familyMemberId = if (walkInViewModel.familyMemberId != 0) walkInViewModel.familyMemberId else defaultFamilyMember,
                        laboratoryId = walkInViewModel.labId,
                        walkInLaboratoryId = walkInViewModel.walkInInitialResponse.walkInLaboratory?.walkInLaboratoryId,
                        cityId = if (walkInViewModel.cityId != 0) walkInViewModel.cityId else null
                    )
                    storeWalkInLaboratory(request)
                },
                negativeButtonCallback = {
                     mBinding.bSendRequest.isEnabled=true
                },
                cancellable = false
            )
    }

    private fun showConfirmationDialog(walkInStoreResponse: WalkInStoreResponse) {
        val orderNumber = "${mBinding.lang?.globalString?.order.getSafe()} ${Constants.HASH} ${walkInStoreResponse.uniqueIdentificationNumber.getSafe()}"
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = orderNumber,
                message = mBinding.lang?.dialogsStrings?.transactionSuccessMsgLab.getSafe(),
                buttonCallback = {
//                    findNavController().navigate(R.id.action_walkinLabDetailFragment_to_labServiceFragment)
                    findNavController().safeNavigate(
                        //R.id.action_walkInHospitalRequestServiceFragment_to_walkin_lab_order_details_navigation,
                        R.id.action_walkinLabDetailFragment_to_walkin_lab_order_details_navigation,
                        bundleOf(
                            Constants.BOOKINGID to walkInStoreResponse.walkInLaboratoryId.getSafe(),
                            "fromBooking" to true,
                            "fromRequest" to true
                        )
                    )
                }
            )
    }

    private fun setDataInDropDownView(data: WalkInInitialResponse?){
        mBinding.apply {
            data?.let {
                val patientNames = it.walkInLaboratory?.familyMembers?.map { p ->
                    if (p.id == DataCenter.getUser()?.id)
                        p.fullName = mBinding.lang?.globalString?.self.getSafe()
                    p.fullName
                }
                mBinding.cdPatients.apply {
                    this.data = patientNames as ArrayList<String>? ?: arrayListOf()
                  /*  if (patientNames.isNullOrEmpty().not()) {
                        selectionIndex = 0
                       /* if (walkInViewModel.familyMemberId != 0) {
                            val pos =
                                it.walkInLaboratory?.familyMembers?.indexOfFirst { pt -> pt.familyMemberId == walkInViewModel.familyMemberId }
                                    .getSafe()
                            selectionIndex = if (pos == -1) 0 else pos
                        }*/
                    }*/
                }
            }
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
            tvVoiceNote = mBinding.tvVoiceNote
        }

    }


    private fun showVoiceNoteDialog(isShow: Boolean) {
        audio.apply {
            title = mBinding.lang?.dialogsStrings?.addVoiceNote.getSafe()
            isPlayOnly = isShow
            positiveButtonText = mBinding.lang?.globalString?.done.getSafe()
            negativeButtonText = mBinding.lang?.globalString?.cancel.getSafe()
            voiceNote = mBinding.lang?.dialogsStrings?.msgRecordAudioWalkin.getSafe()
            cancel = mBinding.lang?.globalString?.cancel.getSafe()
            onSaveFile = { mfile, time ->
                file = mfile
                elapsedMillis = time
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
                title = mBinding.lang?.globalString?.information.getSafe(),
                message = mBinding.lang?.dialogsStrings?.fileSize.getSafe(),
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






    private fun displayNeverAskAgainDialog(message: String) {
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            message = message,
            positiveButtonStringText = mBinding.lang?.dialogsStrings?.permitManual.getSafe(),
            negativeButtonStringText = mBinding.lang?.globalString?.close.getSafe(),
            buttonCallback = {
                context?.let { gotoAppSettings(it) }
            },
            cancellable = false
        )

    }
    private fun setAttachments(response: WalkIn) {
        val attachments: List<Attachment>? = response.claimAttachments
        isPrescriptionSet = (attachments?.filter { it.documentType == 20 }?.size ?: 0) > 0
        isInvoiceSet = (attachments?.filter { it.documentType == 19 }?.size ?: 0) > 0

        if (!(isPrescriptionSet && isInvoiceSet)) {
            mBinding.bSendRequest.isEnabled = false
        }
        if (attachments?.isEmpty().getSafe()) {
            mBinding.apply {
                rvAttachments.setVisible(false)
               // tvNoData.setVisible(response.bookingDetails?.bookingStatusId != Enums.ClaimWalkInStatus.ON_HOLD.id)
            }
        } else {
            mBinding.apply {
                rvAttachments.setVisible(true)
                tvNoData.setVisible(false)


                val mandatoryAttachments = walkinViewModel.documentTypes?.filter { it.required.getBoolean() }
                var errorMsg: String? = null
                run breaking@{
                    mandatoryAttachments?.forEach { mandatoryDoc ->
                        val attachedCount = attachments?.filter { it.documentType == mandatoryDoc.id }?.size
                        val count = response.bookingDetails?.settlementDocumentsRequired?.find { it.id == mandatoryDoc.id }?.count
                        val result = walkinViewModel.walkInResponse?.details?.claimAttachments?.find { it.documentType == mandatoryDoc.genericItemId }
                        if(result == null){
                            errorMsg = lang?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }

                    response.bookingDetails?.settlementDocuments?.forEach { mandatoryDoc ->
                        val attachedCount = attachments?.filter { it.documentType == mandatoryDoc.id }?.size
                        val count = response.bookingDetails?.settlementDocumentsRequired?.find { it.id == mandatoryDoc.id }?.count
                        val result = walkinViewModel.walkInResponse?.details?.claimAttachments?.find { it.documentType == mandatoryDoc.genericItemId }
                        if(result == null){
                            errorMsg = lang?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }
                }

               //  bSendRequest.isEnabled = errorMsg == null
              //  bSendRequest.isEnabled=true

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
                        //change in fi statement in != not equal
                        if(response.bookingDetails?.bookingStatusId == Enums.ClaimWalkInStatus.ON_HOLD.id)
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


    private fun setConnectionData(connection: ClaimConnectionsResponse? = null) {
        mBinding.apply {
            connection?.let { walkInConnection ->
                val connections = walkInConnection.walkInConnections as ArrayList<ClaimConnection>
                val connectionsAdapter = LinkedConnectionsAdapter()
                connectionsAdapter.itemClickListener = { item, pos ->
                    if(item.onHold.getBoolean().not()){
                        walkInViewModel.selectedConnection = connections[pos]
                        connectionsAdapter.listItems.map {
                            it.isChecked = false
                            it
                        }
                        connectionsAdapter.listItems[pos].isChecked = true
                        connectionsAdapter.notifyDataSetChanged()

                        val invoiceAmount = etInvoice.text
                        if (invoiceAmount.isNotEmpty()) {
                            try {
                                if(!(isPrescriptionSet && isInvoiceSet))
                                {
                                    DialogUtils(requireActivity())
                                        .showDoubleButtonsAlertDialog(
                                            message = "kindly attach your invoice and prescription.",
                                            buttonCallback = {
                                            },
                                            negativeButtonCallback = {},
                                            cancellable = false
                                        )
                                }
                                bSendRequest.isEnabled = invoiceAmount.toDouble() <= connections[pos].claimPackage?.credit?.amount.getSafe() &&
                                        invoiceAmount.toDouble().getSafe() > 0.0 && isPrescriptionSet || isInvoiceSet

                                if (invoiceAmount.toDouble() > connections[pos].claimPackage?.credit?.amount.getSafe())
                                    showToast(lang?.fieldValidationStrings?.invoiceAmountGreater.getSafe())
                            } catch (e: Exception) {
                                Timber.e("$e")
                            }
                        }
                    }
                }
                connectionsAdapter.listItems = connections.getSafe()
                rvPaymentMode.adapter = connectionsAdapter
                tvNoData.setVisible(connectionsAdapter.listItems.isEmpty())
            }
        }
    }


    private fun getOrderDetails() {
        walkinViewModel.walkInRequest.walkInLaboratoryId = walkinViewModel.walkInInitialResponse.walkInLaboratory?.walkInLaboratoryId.getSafe()
        walkinViewModel.walkInRequest.walkInHospitalId = null
        walkinViewModel.walkInRequest.walkInPharmacyId = null

        walkinViewModel.walkInLabDetails(walkinViewModel.walkInRequest).observe(this) {
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

    private fun getWalkInLaboratoryConnections() {
        val request = WalkInConnectionRequest(
            bookingId = walkInViewModel.bookingId
        )
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInLaboratoryConnections(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        setConnectionData(connection = (response.data as ClaimConnectionsResponse))
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
                                message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun initialWalkInLaboratory(familyMemberId: Int? = null) {
        val request = WalkInInitialRequest(
            labId = walkInViewModel.labId,
            familyMemberId = familyMemberId
        )
        if (isOnline(requireActivity())) {
            walkInViewModel.initialWalkInLaboratory(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as WalkInInitialResponse).let { walkInitialResponse ->
                            walkInViewModel.walkInInitialResponse = walkInitialResponse
                            walkInViewModel.bookingId = walkInitialResponse.walkInLaboratory?.bookingId.getSafe()
                            walkInId=walkInitialResponse.walkInLaboratory?.walkInLaboratoryId.getSafe()
                            setDataInDropDownView(data = walkInitialResponse)
                            getWalkInLaboratoryConnections()
                            getOrderDetails()
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
                                message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun storeWalkInLaboratory(request: WalkInStoreRequest) {
        if (isOnline(requireActivity())) {
            walkInViewModel.storeWalkInLaboratory(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showConfirmationDialog((response.data as WalkInStoreResponse))
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
                                message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
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
                            title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                            message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                            buttonCallback = {},
                        )
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
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
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
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
                            title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                            message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                            buttonCallback = {},
                        )
                }
            }
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