package com.homemedics.app.ui.fragment.walkin.pharmacy

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
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

class WalkInPharmacyDetailsFragment : BaseFragment(), View.OnClickListener {

    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private val walkinViewModel: WalkInViewModel by activityViewModels()
    private lateinit var mBinding: FragmentWalkinPharmacyDetailsBinding
    private var locale: String? = null
    private var file: File? = null
    private var walkInId = 0
    var api: LiveData<ResponseResult<*>>? = null
    private var forImage = false
    private var elapsedMillis = 0L
    private lateinit var audio: CustomAudio
    private lateinit var fileUtils: FileUtils
    private var attachmentsAdapter = AddMultipleViewAdapter()

  //  private var isPrescriptionSet = false
    private var isInvoiceSet = false
    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = walkInViewModel.walkInPharmacyName.getSafe()
            tvPatientDetail.text = lang?.labPharmacyScreen?.patientDetails.getSafe()
            cdPatients.hint = lang?.globalString?.patient.getSafe()
            etInvoice.hint = lang?.walkInScreens?.enterInvoiceAmount.getSafe()


        }
    }

    override fun init() {

        // getOrderDetails()
        audio = CustomAudio(requireContext())
        walkinViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)
//        getWalkInPharmacyConnections()
        mBinding.rvAttachments.adapter = attachmentsAdapter


        if (!walkinViewModel.isAttachment) {
            initialWalkInPharmacy()
            //  callGetAttachments()
            setDataInViews(walkinViewModel.walkInResponse?.details)
        } else {
            getOrderDetails()
            getWalkInPharmacyConnections()
            setPatientDataDropDownData(walkinViewModel.walkInInitialResponse)

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
            bSendRequest.setOnClickListener(this@WalkInPharmacyDetailsFragment)
            cdPatients.onItemSelectedListener = { _, index ->
                val selectedUser =
                    walkInViewModel.walkInInitialResponse.walkInPharmacy?.familyMembers?.get(index)

                walkInViewModel.familyMemberId =
                    selectedUser?.familyMemberId.getSafe()

                cdPatients.selectionIndex = index


                // initialWalkInPharmacy(familyMemberId = walkInViewModel.familyMemberId)


            }
            etInvoice.mBinding.editText.doAfterTextChanged { amount ->
                if (amount?.isNotEmpty().getSafe()) {
                    try {
                        bSendRequest.isEnabled = walkInViewModel.selectedConnection?.isChecked.getSafe() &&
                                amount?.toString()?.toDouble().getSafe() <= walkInViewModel.selectedConnection?.claimPackage?.credit?.amount.getSafe() &&
                                amount?.toString()?.toDouble().getSafe() > 0.0  && /* isPrescriptionSet && */ isInvoiceSet
                    } catch (e: Exception) {
                        Timber.e("$e")
                    }

                    if (mBinding.etInvoice.mBinding.editText.text?.isNotEmpty() == true && isInvoiceSet){
                        if(!( /* isPrescriptionSet  &&  */ isInvoiceSet))
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



        }
        attachmentsAdapter.onEditItemCall = {
            if (it.drawable == R.drawable.ic_play_arrow) showVoiceNoteDialog(true)
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


        mBinding.tvVoiceNote.setOnClickListener {
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
                    title = mBinding.lang?.globalString?.information.getSafe(),
                    message = mBinding.lang?.dialogsStrings?.recordingAlreadyAdded.getSafe()
                )

                return@setOnClickListener
            }

            fileUtils.requestAudioPermission(requireActivity()) { result ->
                if (result) showVoiceNoteDialog(false)
                else displayNeverAskAgainDialog(mBinding.lang?.dialogsStrings?.recordPermissions.getSafe())
            }

        }
        mBinding.tvAddImage.setOnClickListener {

            findNavController().safeNavigate(
                R.id.action_walkInPharmacyDetailsFragment_to_walkInUploadDocsFragment52,
                bundleOf(
                    "fromImage" to true,
                    "walkInPharmacyDetails" to true
                )

            )

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
                       //  takeAction(result)
                         //amountEnableSecond=true
                     }
                 }
             }*/
        }



        mBinding.tvUploadDoc.setOnClickListener {
            findNavController().safeNavigate(
                R.id.action_walkInPharmacyDetailsFragment_to_walkInUploadDocsFragment52,
                bundleOf(
                    "fromRequest" to true,
                    "walkInPharmacyDetails" to true
                )

            )

            /* if (forImage) {
                 mBinding.apply {
                     actionbar.title = lang?.globalString?.uploadImage.getSafe()
                 }
                 fileUtils.requestPermissions(
                     requireActivity(),


                     ) { result ->
                   //  takeAction(result)
                 }
             } else {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                     val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                     intent.addCategory(Intent.CATEGORY_OPENABLE)
                     intent.type = "application/pdf"
                     startActivityForResult(intent, 1)
                 } else {
                     fileUtils.requestFilePermissions(requireActivity(), false,true) { result ->
                       //  takeAction(result)
                     }
                 }
                 amountEnableSecond=true
                 amountEnabled()
             } */
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        walkInViewModel.selectedConnection?.isChecked = false
        walkInViewModel.familyMemberId = 0
        walkInViewModel.isAttachment = false
        walkinViewModel.partnerServiceId = 0

        //  findNavController().popBackStack(R.id.walkInPharmacyServicesFragment ,true)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bSendRequest -> {
                val amount = mBinding.etInvoice.text
                mBinding.bSendRequest.isEnabled=false
                if (isValid(amount).not()) {
                    mBinding.etInvoice.errorText =
                        mBinding.lang?.fieldValidationStrings?.invoiceAmountEmpty.getSafe()
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




    @SuppressLint("SetTextI18n")
    private fun setDataInViews(response: WalkIn?) {
        response?.let {
            response.bookingDetails?.bookingStatusId = Enums.ClaimWalkInStatus.ON_HOLD.id

            setAttachments(response)
        }
    }

    private fun setAttachments(response: WalkIn) {
        val attachments: List<Attachment>? = response.claimAttachments

        //check if both prescription and invoice are uploaded and are in list.
       // isPrescriptionSet = (attachments?.filter { it.documentType == 17 }?.size ?: 0) > 0
        isInvoiceSet = (attachments?.filter { it.documentType == 16 }?.size ?: 0) > 0

        if (!(/* isPrescriptionSet &&  */ isInvoiceSet)) {
            mBinding.bSendRequest.isEnabled = false
        }

        if (attachments?.isEmpty().getSafe()) {
            mBinding.apply {
                rvAttachments.setVisible(false)
                // tvNoData.setVisible(response.bookingDetails?.bookingStatusId != Enums.ClaimWalkInStatus.ON_HOLD.id)
            }
        } else {
            mBinding.apply {
                tvNoData.setVisible(false)
                rvAttachments.setVisible(true)


                val mandatoryAttachments =
                    walkinViewModel.documentTypes?.filter { it.required.getBoolean() }
                var errorMsg: String? = null
                run breaking@{
                    mandatoryAttachments?.forEach { mandatoryDoc ->
                        val result =
                            walkinViewModel.walkInResponse?.details?.claimAttachments?.find { it.documentType == mandatoryDoc.genericItemId }
                        if (result == null) {
                            errorMsg = lang?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }

                    response.bookingDetails?.settlementDocuments?.forEach { mandatoryDoc ->
                        val attachedCount =
                            attachments?.filter { it.documentType == mandatoryDoc.id }?.size
                        val count =
                            response.bookingDetails?.settlementDocumentsRequired?.find { it.id == mandatoryDoc.id }?.count
                        val result =
                            walkinViewModel.walkInResponse?.details?.claimAttachments?.find { it.documentType == mandatoryDoc.genericItemId }
                        if (result == null) {
                            errorMsg = lang?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }
                }


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
                        /*  if (Patterns.WEB_URL.matcher(item.itemId).matches()) {
                              showVoiceNoteDialog(item.itemId,true)
                          } else {
                              showVoiceNoteDialog(item.itemId,false)
                          }*/
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
                        index.getSafe() + 1, it.attachments?.file?.length.getSafe()
                    )

//                val docType = metaData?.walkInPharmacyDocumentType?.find { docType ->
//                    docType.genericItemId == it.documentType
//                }?.genericItemName

//                if (docType.isNullOrEmpty().not())
                listTitle = "(${it.requestDocuments?.genericItemName.getSafe()}) $listTitle"


                list.add(
                    MultipleViewItem(
                        title = listTitle,
                        itemId = it.attachments?.file,
                        drawable = drawable,
                    ).apply {

                        //  ther is != in condition in if statement
                        if (response.bookingDetails?.bookingStatusId !== Enums.ClaimWalkInStatus.ON_HOLD.id) itemEndIcon =
                            R.drawable.ic_arrow_fw_angular_black

                        if (it.attachmentTypeId == Enums.AttachmentType.VOICE.key) itemCenterIcon =
                            R.drawable.ic_voice_group

                        isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                    })

                walkinViewModel.fileList = list
            }

            attachmentsAdapter.listItems = walkinViewModel.fileList
            lifecycleScope.launch {
                delay(100)
                mBinding.tvVoiceNote.isClickable = true
            }
        }
    }

    private fun showVoiceNoteDialog(itemId: String?, isUrl: Boolean) {

        val audio = CustomAudio(requireContext())
        if (isUrl) {
            audio.apply {
                title = mBinding.lang?.globalString?.voiceNote.getSafe()
                isPlayOnly = true
                url = itemId.getSafe()
                positiveButtonText = mBinding.lang?.globalString?.done.getSafe()
                negativeButtonText = mBinding.lang?.globalString?.cancel.getSafe()
                cancel = mBinding.lang?.globalString?.cancel.getSafe()
                show()

            }
        } else {
            audio.apply {
                title = mBinding.lang?.globalString?.voiceNote.getSafe()
                isPlayOnly = true
                fileName = itemId.getSafe()
                positiveButtonText = mBinding.lang?.globalString?.done.getSafe()
                negativeButtonText = mBinding.lang?.globalString?.cancel.getSafe()
                cancel = mBinding.lang?.globalString?.cancel.getSafe()
                show()

            }
        }


    }

    private fun showImageDialog(itemId: String?) {
        val displayRectangle = Rect()
        val window: Window = requireActivity().getWindow()
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        val builder = AlertDialog.Builder(requireContext())
        val viewGroup: ViewGroup = requireActivity().findViewById(android.R.id.content)
        val dialogView: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_view_image, viewGroup, false)

        builder.setView(dialogView)

        val ivBack = dialogView.findViewById<ImageView>(R.id.ivBack)

        val ivApptImage = dialogView.findViewById<ImageView>(R.id.ivApptImage)
        ivApptImage.layoutParams.width = (resources.displayMetrics.widthPixels * 0.80).toInt()
        ivApptImage.layoutParams.height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        Glide.with(requireContext()).load(itemId).fitCenter().into(ivApptImage)

        val alertDialog = builder.create()
        alertDialog.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT
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

    private fun showRequestSubmitDialog() {
        val defaultFamilyMember =
            walkInViewModel.walkInInitialResponse.walkInPharmacy?.familyMembers?.get(0)?.familyMemberId

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
                        pharmacyId = walkInViewModel.pharmacyId,
                        walkInPharmacyId = walkInViewModel.walkInInitialResponse.walkInPharmacy?.walkInPharmacyId,
                        cityId = if (walkInViewModel.cityId != 0) walkInViewModel.cityId else null
                    )

                    storeWalkInPharmacy(request)


                },
                negativeButtonCallback = {
                    mBinding.bSendRequest.isEnabled=true

                },
                cancellable = false
            )
    }

    private fun showConfirmationDialog(walkInStoreResponse: WalkInStoreResponse) {
        val orderNumber =
            "${mBinding.lang?.globalString?.order.getSafe()} ${Constants.HASH} ${walkInStoreResponse.uniqueIdentificationNumber.getSafe()}"

        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = orderNumber,
                message = mBinding.lang?.dialogsStrings?.transactionSuccessMsg.getSafe(),
                buttonCallback = {
//                    findNavController().navigate(R.id.action_walkInPharmacyDetailsFragment_to_pharmacyServiceFragment)
                    findNavController().safeNavigate(
                        R.id.action_walkInPharmacyDetailsFragment_to_walkin_order_details_navigation3,
                        bundleOf(
                            Constants.BOOKINGID to walkInStoreResponse.walkInPharmacyId.getSafe(),
                            "fromBooking" to true,
                            "fromRequest" to true,
                            "submitreview" to true,
                        )
                    )
                }
            )
    }

    private fun setPatientDataDropDownData(data: WalkInInitialResponse) {
        data.let {
            val patientNames = it.walkInPharmacy?.familyMembers?.map { p ->
                if (p.id == DataCenter.getUser()?.id)
                    p.fullName = mBinding.lang?.globalString?.self.getSafe()
                p.fullName
            }
            mBinding.cdPatients.apply {
                this.data = patientNames as ArrayList<String>? ?: arrayListOf()

                  /* if (patientNames.isNullOrEmpty().not()) {
                        selectionIndex = 0
                        if (walkInViewModel.familyMemberId != 0) {
                            val pos =
                                it.walkInPharmacy?.familyMembers?.indexOfFirst { pt -> pt.familyMemberId == walkInViewModel.familyMemberId }
                                    .getSafe()
                            selectionIndex = if (pos == -1) 0 else pos
                        }
                    }*/

            }
        }
    }
    //--------   Voice Dialog--------------

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
                        it, it1
                    )
                }
            }.getSafe()) {
            showFileSizeDialog()

        } else {
            addAttachmentApiCall()
        }
    }

    private fun showFileSizeDialog() {
        DialogUtils(requireActivity()).showSingleButtonAlertDialog(
            title = mBinding.lang?.globalString?.information.getSafe(),
            message = mBinding.lang?.dialogsStrings?.fileSize.getSafe(),
            buttonCallback = {},
        )
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

    private fun addAttachmentApiCall() {
        val mediaList = ArrayList<MultipartBody.Part>()
        val uri = file?.let { fileUtils.getUriFromFile(context = requireContext(), it) }
        val mimeTypeView = uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }

        var mimeType: Int = 0
        val typeImage = if (mimeTypeView?.contains("image").getSafe()) mimeTypeView else ""
        when (mimeTypeView) {
            FileUtils.typeOther, FileUtils.typePDF -> {
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
        val multipartFile = fileUtils.convertFileToMultiPart(
            File(path), mimeTypeView.getSafe(), "attachments"
        )
        mediaList.add(multipartFile)

        val docType =
            walkinViewModel.walkInResponse?.details?.documentTypes?.find { it.genericItemName == "Other" }?.id.getSafe()

        if (isOnline(requireActivity())) {
            val request = AddWalkInAttachmentRequest(
                walkInPharmacyId = walkInId,
                pharmacyId = walkinViewModel.walkInResponse?.details?.pharmacyId,
                attachmentType = mimeType,
                documentType = docType, //other for now
                attachments = mediaList
            )


            walkinViewModel.addWalkInAttachment(request).observe(this) {
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
                            DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                        }
                        is ResponseResult.ApiError -> {
                            hideLoader()
                            DialogUtils(requireActivity()).showSingleButtonAlertDialog(
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
                    DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                        title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                        message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                        buttonCallback = {},
                    )
                }
            }
        } else {
            DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                buttonCallback = {},
            )
        }
    }

    private fun setConnectionData(connection: ClaimConnectionsResponse? = null) {
        mBinding.apply {
            connection?.let { walkInConnection ->
                val connections = walkInConnection.walkInConnections as ArrayList<ClaimConnection>
                val connectionsAdapter = LinkedConnectionsAdapter()
                connectionsAdapter.itemClickListener = { item, pos ->
                    if (item.onHold.getBoolean().not()) {
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
                                if(!( /* isPrescriptionSet  && */ isInvoiceSet))
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

                                bSendRequest.isEnabled =
                                    invoiceAmount.toDouble() <= connections[pos].claimPackage?.credit?.amount.getSafe() &&
                                            invoiceAmount.toDouble()
                                                .getSafe() > 0.0 && /* isPrescriptionSet && */ isInvoiceSet

                                if (invoiceAmount.toDouble() > connections[pos].claimPackage?.credit?.amount.getSafe())
                                {
                                    DialogUtils(requireActivity())
                                        .showDoubleButtonsAlertDialog(
                                            message = "Enter Amount is greater then Remaning amount.",
                                            buttonCallback = {
                                            },
                                            negativeButtonCallback = {},
                                            cancellable = false
                                        )
                                }
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

    private fun getWalkInPharmacyConnections() {
        val request = WalkInConnectionRequest(
            bookingId = walkInViewModel.bookingId
        )
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInPharmacyConnections(request).observe(this) {
                when (it) {
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
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun initialWalkInPharmacy(familyMemberId: Int? = null) {
        val request = WalkInInitialRequest(
            pharmacyId = walkInViewModel.pharmacyId,
            familyMemberId = familyMemberId
        )
        if (isOnline(requireActivity())) {
            walkInViewModel.initialWalkInPharmacy(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as WalkInInitialResponse).let { walkInitialResponse ->
                            walkInViewModel.walkInInitialResponse = walkInitialResponse
                            walkInViewModel.bookingId =
                                walkInitialResponse.walkInPharmacy?.bookingId.getSafe()
                            walkInId =
                                walkInitialResponse.walkInPharmacy?.walkInPharmacyId.getSafe()
                            setPatientDataDropDownData(data = walkInitialResponse)
                            getWalkInPharmacyConnections()
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
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun storeWalkInPharmacy(request: WalkInStoreRequest) {
        if (isOnline(requireActivity())) {
            walkInViewModel.storeWalkInPharmacy(request).observe(this) {
                when (it) {
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
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getOrderDetails() {
        walkinViewModel.walkInRequest.walkInPharmacyId =
            walkinViewModel.walkInInitialResponse.walkInPharmacy?.walkInPharmacyId.getSafe()
        walkinViewModel.walkInRequest.walkInLaboratoryId = null
        walkinViewModel.walkInRequest.walkInHospitalId = null

        walkinViewModel.walkInDetails(walkinViewModel.walkInRequest).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<WalkInResponse>
                    response.data?.let { walkInDetailResponse ->
                        walkinViewModel.walkInResponse = walkInDetailResponse
                        walkinViewModel.walkInRequest.walkInPharmacyId =
                            walkInDetailResponse.details?.id
                        walkinViewModel.documentTypes = walkInDetailResponse.details?.documentTypes
                        walkinViewModel.bookingId =
                            walkInDetailResponse.details?.bookingId.getSafe()
                        walkinViewModel.partnerServiceId =
                            walkInDetailResponse.details?.bookingDetails?.partnerServiceId.getSafe()
                        setDataInViews(walkInDetailResponse.details)
                    }
                }
                is ResponseResult.Failure -> {
                    DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                        message = it.error.message.getSafe(),
                        buttonCallback = {
                            findNavController().popBackStack()
                        },
                    )
                }
                is ResponseResult.ApiError -> {
                    DialogUtils(requireActivity()).showSingleButtonAlertDialog(
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

    private fun deleteClaimAttachment(id: Int) {


        if (isOnline(requireActivity())) {
            val request = WalkInAttachmentRequest(walkInPharmacyAttachmentId = id)

            walkinViewModel.deleteWalkInAttachment(request = request).observe(this) {
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
                            DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                        }
                        is ResponseResult.ApiError -> {
                            hideLoader()
                            DialogUtils(requireActivity()).showSingleButtonAlertDialog(
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
                    DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                        title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                        message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                        buttonCallback = {},
                    )
                }
            }
        } else {
            DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                buttonCallback = {},
            )
        }
    }

    private fun callGetAttachments() {
        if (isOnline(requireActivity())) {
            val request = WalkInAttachmentRequest(
                walkInPharmacyId = walkinViewModel.walkInRequest.walkInPharmacyId.getSafe()
            )

            walkinViewModel.callWalkInGetAttachments(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<AttachmentResponse>
                        walkinViewModel.fileList = arrayListOf()
                        val data = response.data?.attachments.getSafe()
                        data.forEach {
                            if (it.documentType == 16 || it.documentType == 17)
                                walkInViewModel.documentTypeid = it.documentType.getSafe()
                        }
                        walkinViewModel.walkInResponse?.details?.let {
                            it.claimAttachments = data
                            setAttachments(it)
                        }

                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                            message = it.error.message.getSafe(),
                            buttonCallback = {},
                        )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity()).showSingleButtonAlertDialog(
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
            DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                buttonCallback = {},
            )
        }
    }


}