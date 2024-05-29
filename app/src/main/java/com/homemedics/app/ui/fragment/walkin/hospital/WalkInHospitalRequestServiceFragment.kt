package com.homemedics.app.ui.fragment.walkin.hospital

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
import com.fatron.network_module.models.response.walkinpharmacy.WalkInInitialResponse
import com.fatron.network_module.models.response.walkinpharmacy.WalkInStoreResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.databinding.FragmentWalkinHospitalRequestServiceBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.adapter.CheckoutPaymentMethodsAdapter
import com.homemedics.app.ui.adapter.LinkedConnectionsAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.WalkInViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import java.io.IOException

class WalkInHospitalRequestServiceFragment : BaseFragment(), View.OnClickListener {

    private var connectionItem: ClaimConnection? = null
    private lateinit var mBinding: FragmentWalkinHospitalRequestServiceBinding
    private lateinit var dialogViewBinding: FragmentAddVoicenoteBinding
    private lateinit var paymentMethodAdapter: CheckoutPaymentMethodsAdapter
    private lateinit var fileUtils: FileUtils
    private lateinit var animBlink: Animation
    private lateinit var meter: Chronometer
    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private var attachmentsAdapter = AddMultipleViewAdapter()
    private var file: File? = null
    private var player: MediaPlayer? = null
    private var length = 0
    private var absolutePath: String = ""
    private var elapsedMillis = 0L
    private var recorder: MediaRecorder? = null
    private var dialogSaveButton: Button? = null
    private var locale: String? = null
    private lateinit var audio: CustomAudio
  /*  private var isPrescriptionSet = false
    private var isInvoiceSet = false*/
    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = walkInViewModel.walkInHospitalName.getSafe()
            iSelectedService.apply {
                tvTitle.text = walkInViewModel.serviceName.getSafe()
                ivIcon.loadImage(walkInViewModel.walkInService?.services?.icon_url.getSafe(), R.drawable.ic_emergency)
            }
            tvPatientDetail.text = lang?.labPharmacyScreen?.patientDetails.getSafe()
            cdPatients.hint = lang?.globalString?.patient.getSafe()
            etInstructions.hint = lang?.globalString?.specialInstructions.getSafe()
        }
    }


    override fun onPause() {
        super.onPause()
        audio.onPause()
    }


    override fun init() {
        audio = CustomAudio(requireContext())
        locale =
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        walkInViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)
        animBlink = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.blink
        )
        mBinding.rvAttachments.adapter = attachmentsAdapter
        mBinding.iSelectedService.ivArrow.gone()

        getWalkInHospitalConnections()
        callGetAttachments()
    }

    override fun onResume() {
        super.onResume()
        setDataInDropDownView(data = walkInViewModel.walkInInitialResponse)
    }

    override fun getFragmentLayout() = R.layout.fragment_walkin_hospital_request_service

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkinHospitalRequestServiceBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                openDefaultMap(
                    walkInViewModel.mapLatLng, requireActivity()
                )
            }
            bSendRequest.setOnClickListener(this@WalkInHospitalRequestServiceFragment)

            cdPatients.onItemSelectedListener = { _, index ->
                val selectedUser =
                    walkInViewModel.walkInInitialResponse.walkInHospital?.familyMembers?.get(index)
                walkInViewModel.familyMemberId = selectedUser?.familyMemberId.getSafe()
                cdPatients.selectionIndex = index
               // initialWalkInHospital(familyMemberId = walkInViewModel.familyMemberId)
            }

            attachmentsAdapter.onEditItemCall = {
                if (it.drawable == R.drawable.ic_play_arrow)
                    showVoiceNoteDialog(true)
            }
            attachmentsAdapter.onDeleteClick = { _, position ->
                mBinding.lang?.apply {
                    DialogUtils(requireActivity())
                        .showDoubleButtonsAlertDialog(
                            title = dialogsStrings?.confirmDelete.getSafe(),
                            message = dialogsStrings?.deleteDesc.getSafe(),
                            positiveButtonStringText = globalString?.yes.getSafe(),
                            negativeButtonStringText = globalString?.no.getSafe(),
                            buttonCallback = {
                                val attachmentId =
                                    walkInViewModel.walkInAttachments[position].id.getSafe()
                                deleteClaimAttachment(attachmentId)
                            },
                        )
                }
            }

            tvVoiceNote.setOnClickListener {
                var hasRecording = false
                val fileList = walkInViewModel.fileList
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
            tvUploadDoc.setOnClickListener {
                findNavController().safeNavigate(
                    R.id.action_walkInHospitalRequestServiceFragment_to_walkInUploadDocsFragment4,
                    bundleOf("fromHospital" to true)
                )
            }
            tvAddImage.setOnClickListener {
                findNavController().safeNavigate(
                    R.id.action_walkInHospitalRequestServiceFragment_to_walkInUploadDocsFragment4,
                    bundleOf("fromImage" to true, "fromHospital" to true)
                )
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bSendRequest -> {
                showRequestSubmitDialog()
            }
        }
    }

    private fun setDataInDropDownView(data: WalkInInitialResponse?) {
        mBinding.apply {
            data?.let {
                val patientNames = it.walkInHospital?.familyMembers?.map { p ->
                    if (p.id == DataCenter.getUser()?.id)
                        p.fullName = mBinding.lang?.globalString?.self.getSafe()
                    p.fullName
                }
                mBinding.cdPatients.apply {
                    this.data = patientNames as ArrayList<String>? ?: arrayListOf()
                 /*  if (patientNames.isNullOrEmpty().not()) {
                        selectionIndex = 0
                        /*if (walkInViewModel.familyMemberId != 0) {
                            val pos =
                                it.walkInHospital?.familyMembers?.indexOfFirst { pt -> pt.familyMemberId == walkInViewModel.familyMemberId }
                                    .getSafe()
                            selectionIndex = if (pos == -1) 0 else pos
                        }*/
                    }*/
                }
            }
        }
    }

    private fun setConnectionData(connection: ClaimConnectionsResponse? = null) {
        mBinding.apply {
            connection?.let { walkInConnection ->
                val connections = walkInConnection.walkInConnections.getSafe()
                val connectionsAdapter = LinkedConnectionsAdapter()
                connectionsAdapter.itemClickListener = { item, pos ->
                    if(item.onHold.getBoolean().not()){
                        walkInViewModel.selectedConnection = connections[pos]
                        connectionsAdapter.listItems.map {
                            it.isChecked = false
                            it
                        }
                        connectionsAdapter.listItems[pos].isChecked = true
                        connectionItem = connectionsAdapter.listItems[pos]
                        connectionsAdapter.notifyDataSetChanged()
                    /*    if(!(isPrescriptionSet && isInvoiceSet))
                        {
                            DialogUtils(requireActivity())
                                .showDoubleButtonsAlertDialog(
                                    message = "kindly attach your invoice and prescription.",
                                    buttonCallback = {
                                    },
                                    negativeButtonCallback = {},
                                    cancellable = false
                                )
                        }*/
                       // else {
                            bSendRequest.isEnabled =
                                connectionsAdapter.listItems[pos].isChecked.getSafe() /*&& isPrescriptionSet && isInvoiceSet*/
                        //}
                    }

                }
                connectionsAdapter.listItems = connections.getSafe()
                rvPaymentMode.adapter = connectionsAdapter
                tvNoData.setVisible(connectionsAdapter.listItems.isEmpty())
            }
        }
    }

    private fun getWalkInHospitalConnections() {
        val request = WalkInConnectionRequest(
            partnerServiceId = walkInViewModel.partnerServiceId,
            bookingId = walkInViewModel.bookingId,
            serviceId = walkInViewModel.walkInService?.services?.genericItemId.getSafe(),
            filter = 0 // is not filter
        )
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInHospitalConnections(request).observe(this) {
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

    private fun initialWalkInHospital(familyMemberId: Int? = null) {
        val request = WalkInInitialRequest(
            healthcareId = walkInViewModel.hospitalId,
            familyMemberId = familyMemberId
        )
        if (isOnline(requireActivity())) {
            walkInViewModel.initialWalkInHospital(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as WalkInInitialResponse).let { walkInitialResponse ->
                            walkInViewModel.walkInInitialResponse = walkInitialResponse
                            walkInViewModel.bookingId = walkInitialResponse.walkInHospital?.bookingId.getSafe()
                            walkInViewModel.documentTypes = walkInitialResponse.walkInHospital?.documentTypes
                            getWalkInHospitalConnections()
                            attachmentsAdapter.listItems.clear()
                            setAttachments(null)
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

    private fun showRequestSubmitDialog() {
        val defaultFamilyMember =
            walkInViewModel.walkInInitialResponse.walkInHospital?.familyMembers?.get(0)?.familyMemberId
        val comments = mBinding.etInstructions.text.toString()
        DialogUtils(requireActivity())
            .showDoubleButtonsAlertDialog(
                title = mBinding.lang?.dialogsStrings?.areYouSure.getSafe(),
                message = mBinding.lang?.dialogsStrings?.walkInServiceRequest.getSafe(),
                positiveButtonStringText = mBinding.lang?.globalString?.yes.getSafe(),
                negativeButtonStringText = mBinding.lang?.globalString?.cancel.getSafe(),
                buttonCallback = {
                    val request = WalkInStoreRequest(
                        connectionId = walkInViewModel.selectedConnection?.id,
                        familyMemberId = if (walkInViewModel.familyMemberId != 0) walkInViewModel.familyMemberId else defaultFamilyMember,
                        healthCareId = walkInViewModel.hospitalId,
                        walkInHospitalId = walkInViewModel.walkInInitialResponse.walkInHospital?.walkInHospitalId,
                        serviceId = walkInViewModel.walkInService?.services?.genericItemId.getSafe(),
                        cityId = if (walkInViewModel.cityId != 0) walkInViewModel.cityId else null,
                        comments = comments.ifEmpty { null }
                    )
                    storeWalkInHospital(request)
                },
                negativeButtonCallback = {
                     mBinding.bSendRequest.isEnabled=true
                },
                cancellable = false
            )
    }

    private fun showConfirmationDialog(walkInStoreResponse: WalkInStoreResponse) {
        val orderNumber =
            "${mBinding.lang?.globalString?.request.getSafe()} ${Constants.HASH} ${walkInStoreResponse.uniqueIdentificationNumber.getSafe()}"
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = orderNumber,
                message = mBinding.lang?.dialogsStrings?.walkInServiceRequestReceived.getSafe(),
                buttonCallback = {
//                    findNavController().popBackStack(R.id.walkin_services_navigation, true)
                    findNavController().safeNavigate(
                        R.id.action_walkInHospitalRequestServiceFragment_to_walkin_hospital_order_details_navigation,
                        bundleOf(
                            Constants.BOOKINGID to walkInStoreResponse.walkInHospitalId.getSafe(),
                            "fromBooking" to true,
                            "fromRequest" to true
                        )
                    )
                }
            )
    }

    private fun storeWalkInHospital(request: WalkInStoreRequest) {
        if (isOnline(requireActivity())) {
            walkInViewModel.storeWalkInHospital(request).observe(this) {
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

    private fun addAttachmentApiCall() {
        val mediaList = ArrayList<MultipartBody.Part>()
        val uri = file?.let { fileUtils.getUriFromFile(context = requireContext(), it) }
        val mimeTypeView = uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }

        val typeImage =
            if (mimeTypeView?.contains("image").getSafe()) mimeTypeView else ""
        val mimeType = when (mimeTypeView) {
            FileUtils.typeOther,
            FileUtils.typePDF -> {
                Enums.AttachmentType.DOC.key
            }
            typeImage -> {
                Enums.AttachmentType.IMAGE.key
            }
            else -> {
                Enums.AttachmentType.VOICE.key
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

        if (isOnline(requireActivity())) {
            val request = AddWalkInAttachmentRequest(
                walkInHospitalId = walkInViewModel.walkInInitialResponse.walkInHospital?.walkInHospitalId.getSafe(),
                healthcareId = walkInViewModel.hospitalId,
                attachmentType = mimeType,
                documentType = 4, //other for now
                attachments = mediaList
            )

            walkInViewModel.addWalkInHospitalAttachment(request).observe(this) {
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

    private fun deleteClaimAttachment(id: Int) {
        if (isOnline(requireActivity())) {
            val request = WalkInAttachmentRequest(walkInHospitalAttachmentId = id)
            walkInViewModel.deleteWalkInHospitalAttachment(request = request).observe(this) {
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

    private fun callGetAttachments() {
        if (isOnline(requireActivity())) {
            val request = WalkInAttachmentRequest(
                walkInHospitalId = walkInViewModel.walkInInitialResponse.walkInHospital?.walkInHospitalId.getSafe()
            )
            walkInViewModel.callGetWalkInHospitalAttachments(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<AttachmentResponse>
                        walkInViewModel.fileList = arrayListOf()
                        val data = response.data?.attachments.getSafe()
                        setAttachments(data)
                        walkInViewModel.walkInAttachments = data
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
                    else -> {}
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

    private fun setAttachments(attachments: List<Attachment>?) {
//       val attachments: List<Attachment>? = response.claimAttachments
        //check if both prescription and invoice are uploaded and are in list.
       /* isPrescriptionSet = (attachments?.filter { it.documentType == 23  }?.size ?: 0) > 0
        isInvoiceSet = (attachments?.filter { it.documentType == 22 }?.size ?: 0) > 0
        if (!(isPrescriptionSet && isInvoiceSet)) {
            mBinding.bSendRequest.isEnabled = false
            var errorMsg: String? = null
            errorMsg = "kindly attach your invoice and prescription"
        }*/
        if (attachments?.isEmpty().getSafe()) {
            mBinding.apply {
                rvAttachments.invisible()
                attachmentsAdapter.listItems.clear()
//                tvNoData.setVisible(response.bookingDetails?.bookingStatusId != Enums.WalkInHospitalStatus.ON_HOLD.id)
            }
        } else {
            mBinding.apply {
                rvAttachments.setVisible(true)
                tvNoData.setVisible(false)
                val mandatoryAttachments = walkInViewModel.documentTypes?.filter { it.required.getBoolean() }
                var errorMsg: String? = null
                run breaking@{
                    mandatoryAttachments?.forEach { mandatoryDoc ->
                        val result =
                            walkInViewModel.walkInResponse?.details?.claimAttachments?.find { it.documentType == mandatoryDoc.genericItemId }
                        if (result == null) {
                            errorMsg = lang?.messages?.mandatoryAttachmentsRequired.getSafe()
                            return@breaking
                        }
                    }
                }

               // bSendRequest.isEnabled = errorMsg == null && connectionItem?.isChecked.getSafe()
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

//                val docType = metaData?.walkInHospitalDocumentType?.find { docType ->
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
                        if (it.attachmentTypeId == Enums.AttachmentType.VOICE.key) itemCenterIcon =
                            R.drawable.ic_voice_group

                        isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                    }
                )

                walkInViewModel.fileList = list
            }
            attachmentsAdapter.listItems = walkInViewModel.fileList
            lifecycleScope.launch {
                delay(100)
                mBinding.tvVoiceNote.isClickable = true
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

    private fun showImageDialog(itemId: String?) {
        val displayRectangle = Rect()
        val window: Window = requireActivity().window
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

        }
//        val builder = AlertDialog.Builder(requireActivity()).apply {
//            dialogViewBinding = FragmentAddVoicenoteBinding.inflate(layoutInflater)
//            setView(dialogViewBinding.root)
//            setTitle(mBinding.lang?.globalString?.voiceNote.getSafe())
//            dialogViewBinding.IvPlay.tag = R.string.play
//            setNegativeButton(mBinding.lang?.globalString?.cancel.getSafe()) { _, _ ->
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

    private fun showVoiceNoteDialog(isShow: Boolean) {
        audio.apply {
            title = mBinding.lang?.dialogsStrings?.addVoiceNote.getSafe()
            isPlayOnly = isShow
            positiveButtonText = mBinding.lang?.globalString?.done.getSafe()
            negativeButtonText = mBinding.lang?.globalString?.cancel.getSafe()
            cancel = mBinding.lang?.globalString?.cancel.getSafe()
            voiceNote = mBinding.lang?.dialogsStrings?.msgRecordAudioWalkin.getSafe()
            onSaveFile = { mfile, time ->
                file = mfile
                elapsedMillis = time
                saveFile()
            }
            show()
            tvVoiceNote = mBinding.tvVoiceNote
        }
//        val builder = AlertDialog.Builder(requireActivity()).apply {
//            dialogViewBinding = FragmentAddVoicenoteBinding.inflate(layoutInflater)
//            setView(dialogViewBinding.root)
//            setTitle(mBinding.lang?.dialogsStrings?.addVoiceNote)
//            dialogViewBinding.IvPlay.tag = R.string.play
//            dialogViewBinding.tvVoiceNote.text =
//                mBinding.lang?.dialogsStrings?.msgRecordAudioWalkin
//            dialogViewBinding.tvCancel.text = mBinding.lang?.globalString?.cancel
//            if (isShow.not()) {
//                setPositiveButton(mBinding.lang?.globalString?.done) { _, _ -> }
//            }
//            setNegativeButton(mBinding.lang?.globalString?.cancel) { _, _ ->
//                absolutePath = ""
//                stopPlaying()
//                if (isShow.not()) {
//                    stopRecording()
//                    deleteAudioFile()
//                }
//            }
//
//            setCancelable(false)
//            dialogViewBinding.apply {
//
//                if (isShow) {
//                    rlPlayLayout.setVisible(true)
//                    tvVoiceNote.setVisible(false)
//                    IvPlayDelete.setVisible(false)
//                    IvVoiceImage.setVisible(false)
//                }
//                meter = tvTimer
//
//                IvVoiceImage.setOnClickListener {
//                    file = fileUtils.createVoiceFile(requireContext())
//                    meter.base = SystemClock.elapsedRealtime()
//                    absolutePath = file?.absolutePath.getSafe()
//                    startRecording()
//                    val timer = object : CountDownTimer(3 * 60 * 1000, 1000) {
//                        override fun onTick(millisUntilFinished: Long) {}
//
//                        override fun onFinish() {
//                            rlPlayLayout.setVisible(true)
//                            flPlayLayout.setVisible(false)
//                            stopRecording()
//                            validate()
//                        }
//                    }
//                    timer.start()
//                    flPlayLayout.setVisible(true)
//                    IvVoiceImage.setVisible(false)
//                    validate()
//                }
//                IvVoicePause.setOnClickListener {//flPlayLayout
//                    rlPlayLayout.setVisible(true)
//                    flPlayLayout.setVisible(false)
//                    stopRecording()
//                    validate()
//                }
//                tvCancel.setOnClickListener {//flPlayLayout
//                    IvVoiceImage.setVisible(true)
//                    stopRecording()
//                    deleteAudioFile()
//                    flPlayLayout.setVisible(false)
//                    validate()
//                }
//
//                IvPlayDelete.setOnClickListener { //rlPlayLayout
//                    IvVoiceImage.setVisible(true)
//                    rlPlayLayout.setVisible(false)
//                    stopPlaying()
//                    deleteAudioFile()
//                    validate()
//                }
//                IvPlay.setOnClickListener {//rlPlayLayout
//                    if (IvPlay.tag == R.string.play)
//                        startPlaying()
//                    else stopPlaying()
//                    if (isShow.not())
//                        validate()
//                }
//            }
//        }.create()
//        builder.setOnShowListener {
//            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
//            dialogSaveButton?.setOnClickListener {
//                if (dialogViewBinding.flPlayLayout.isVisible) {
//                    elapsedMillis = SystemClock.elapsedRealtime() - meter.base
//                    stopRecording()
//                    if (elapsedMillis == 0L) {
//                        deleteAudioFile()
//                        dialogViewBinding.IvVoiceImage.setVisible(true)
//                    } else
//                        dialogViewBinding.rlPlayLayout.setVisible(true)
//                    dialogViewBinding.flPlayLayout.setVisible(false)
//                } else {
//                    builder.dismiss()
//                    saveFile()
//                }
//            }
//            dialogSaveButton?.isEnabled = false
//        }
//        builder.show()
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
}