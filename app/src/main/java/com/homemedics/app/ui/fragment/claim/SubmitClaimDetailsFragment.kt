package com.homemedics.app.ui.fragment.claim

import android.content.Intent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.claim.AddClaimAttachmentRequest
import com.fatron.network_module.models.request.claim.ClaimAttachmentRequest
import com.fatron.network_module.models.request.claim.ClaimRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.claim.ClaimResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSubmitClaimDetailsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ClaimViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File


class SubmitClaimDetailsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentSubmitClaimDetailsBinding
    private val claimViewModel: ClaimViewModel by activityViewModels()

    private var itemsAdapter = AddMultipleViewAdapter()
    private var attachmentsResponse: AttachmentResponse? = null


    private var file: File? = null
    private lateinit var fileUtils: FileUtils
    private var elapsedMillis = 0L

    private lateinit var animBlink: Animation
    private var locale: String? = null

    private lateinit var audio: CustomAudio
    private var isInvoiceSet = false
    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.claimScreen?.submitClaim.getSafe()
            etInvoiceAmount.hint = langData?.claimScreen?.enterInvoiceAmount.getSafe()
            etServer.hint = langData?.claimScreen?.enterLabPharmaHospital.getSafe()
            cdPatients.hint = langData?.globalString?.patient.getSafe()
            etInstructions.hint = langData?.globalString?.specialInstructions.getSafe()
            tvDesc.text = langData?.claimScreen?.msgEnterInvoiceAmount?.replace("[0]", claimViewModel.category.getSafe())
        }
    }

    override fun init() {
        audio = CustomAudio(requireContext())
        locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        claimViewModel.fileList = arrayListOf()
        observe()
        fileUtils = FileUtils()
        fileUtils.init(this)
        animBlink = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.blink
        )

        mBinding.rvAttachments.adapter = itemsAdapter

        setActionbarCity()
        createClaimId()
    }

    private fun setActionbarCity() {
        //action bar city name
        if (claimViewModel.claimRequest.cityName.isNullOrEmpty()) {
            val user = DataCenter.getUser()
            user?.let {
                val city =
                    getCityList(user.countryId.getSafe())?.find { city -> city.id == user.cityId }
                mBinding.actionbar.desc = city?.name.getSafe()
            }
        } else mBinding.actionbar.desc = claimViewModel.claimRequest.cityName.getSafe()
    }

    override fun getFragmentLayout() = R.layout.fragment_submit_claim_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSubmitClaimDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            itemsAdapter.onEditItemCall = {
                if (it.drawable == R.drawable.ic_play_arrow)
                    showVoiceNoteDialog(true)
            }
            itemsAdapter.onDeleteClick = { item, position ->
                mBinding.langData?.apply {
                    DialogUtils(requireActivity())
                        .showDoubleButtonsAlertDialog(
                            title = dialogsStrings?.confirmDelete.getSafe(),
                            message = dialogsStrings?.deleteDesc.getSafe(),
                            positiveButtonStringText = globalString?.yes.getSafe(),
                            negativeButtonStringText = globalString?.no.getSafe(),
                            buttonCallback = {

                                deleteClaimAttachment(item)

                            },
                        )

                }



            }

            actionbar.onAction1Click = {
                findNavController().popBackStack()
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
//                findNavController().safeNavigate(SubmitClaimDetailsFragmentDirections.actionSubmitClaimDetailsFragmentToClaimUploadDocsFragment())
                findNavController().safeNavigate(R.id.action_submitClaimDetailsFragment_to_claimUploadDocsFragment ,
                    bundleOf("fromRequest" to true))
            }

            tvAddImage.setOnClickListener {
                findNavController().safeNavigate(R.id.action_submitClaimDetailsFragment_to_claimUploadDocsFragment ,
                    bundleOf("fromImage" to true, "fromRequest" to true))
            }

            cdPatients.onItemSelectedListener = { item, pos ->
                if(claimViewModel.claimResponse?.claim?.familyMembers?.isEmpty()?.not().getSafe()){
                    claimViewModel.claimRequest.familyMemberId =
                        claimViewModel.claimResponse?.claim?.familyMembers?.get(pos)?.familyMemberId
                }
            }

            bCheckout.setOnClickListener {
                claimViewModel.claimRequest.apply {
                    if (claimViewModel.claimResponse?.claim?.familyMembers?.isEmpty()?.not().getSafe()) {
//                        familyMemberId =
//                            claimViewModel.claimResponse?.claim?.familyMembers?.get(cdPatients.selectionIndex)?.familyMemberId



                        var errorMsg: String? = null

                        if(etInvoiceAmount.text.isEmpty())
                            errorMsg = langData?.fieldValidationStrings?.invoiceAmountEmpty.getSafe()

                        if(etInvoiceAmount.text.isNotEmpty() && etInvoiceAmount.text.toDouble() == 0.0)
                            errorMsg = langData?.fieldValidationStrings?.invalidInvoiceAmount.getSafe()

                        if(etServer.text.isEmpty())
                            errorMsg = langData?.fieldValidationStrings?.labPharmaHospitalEmpty.getSafe()

                        val mandatoryAttachments = claimViewModel.documentTypes?.filter { it.required.getBoolean() }

                        run breaking@{
                            mandatoryAttachments?.forEach { mandatoryDoc ->
                                val result = attachmentsResponse?.attachments?.find { it.documentType == mandatoryDoc.genericItemId}
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
                            return@setOnClickListener
                        }

                        claimViewModel.claimRequest.apply {
                            amount = etInvoiceAmount.text
                            serviceProvider = etServer.text
                            comments = etInstructions.text.toString()
                            cityId = DataCenter.getUser()?.cityId
                            countryId = DataCenter.getUser()?.countryId
                        }

                        storeClaim()
                    } else {
                        startActivity(Intent(requireActivity(), AuthActivity::class.java))
                    }
                }
            }
        }
    }

    private fun navigateToCheckout() {
        findNavController().safeNavigate(SubmitClaimDetailsFragmentDirections.actionSubmitClaimDetailsFragmentToClaimCheckoutFragment())
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

    private fun addFileList() {
        itemsAdapter.listItems = claimViewModel.fileList
        lifecycleScope.launch {
            delay(100)
            mBinding.tvVoiceNote.isClickable = true
        }
    }

    override fun onClick(v: View?) {

    }

    private fun showFileSizeDialog() {
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = mBinding.langData?.globalString?.information.getSafe(),
                message = mBinding.langData?.dialogsStrings?.fileSize.getSafe(),
                buttonCallback = {},
            )
    }

    private fun observe() {

    }

    private fun setDataInViews(data: ClaimResponse) {
        data.let {
            claimViewModel.claimRequest.apply {
                claimId = it.claim?.claimId
            }

            val patientNames = it.claim?.familyMembers?.map { p ->
                if (p.id == DataCenter.getUser()?.id)
                    p.fullName = mBinding.langData?.globalString?.self
                p.fullName
            }
            mBinding.cdPatients.apply {
                this.data = patientNames as ArrayList<String>? ?: arrayListOf()
                if (patientNames.isNullOrEmpty().not()) {
                    selectionIndex = 0
                    if (claimViewModel.claimRequest.familyMemberId != null) {
                        val pos =
                            it.claim?.familyMembers?.indexOfFirst { pt -> pt.familyMemberId == claimViewModel.claimRequest.familyMemberId }
                                .getSafe()
                        selectionIndex = if (pos == -1) 0 else pos
                    }
                    claimViewModel.claimRequest.familyMemberId =
                        claimViewModel.claimResponse?.claim?.familyMembers?.get(selectionIndex)?.familyMemberId
                }
            }
        }
    }

    private fun createClaimId() {
        if (isOnline(requireActivity())) {

//            claimViewModel.claimRequest.claimId = 20 //change later

            claimViewModel.createClaimId().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<ClaimResponse>
                        response.data?.let {
                            claimViewModel.claimResponse = it
                            claimViewModel.bookingId = it.claim?.bookingId.getSafe()
                            claimViewModel.documentTypes = it.claim?.documentTypes
                            setDataInViews(it)
                            callGetAttachments()
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    createClaimId()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {
                                    createClaimId()
                                },
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

    private fun storeClaim() {
        if (isOnline(requireActivity())) {
            claimViewModel.storeClaimRequest().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        navigateToCheckout()
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
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
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

    override fun onDestroy() {
        super.onDestroy()
        claimViewModel.claimRequest = ClaimRequest()
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
                            attachmentsResponse = response.data
                            claimViewModel.fileList = arrayListOf()
                            val data = response.data?.attachments.getSafe()
                            setAttachments(data)

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

    private fun setAttachments(data: List<Attachment>?) {
        isInvoiceSet = (data?.filter { it.documentType == 13 }?.size ?: 0) > 0

        mBinding.bCheckout.isEnabled = isInvoiceSet



        data?.forEach {
            var drawable: Int = 0

            when (it.attachmentTypeId) {
                Enums.AttachmentType.DOC.key -> {
                    drawable = R.drawable.ic_upload_file

                }
                Enums.AttachmentType.IMAGE.key -> {
                    drawable = R.drawable.ic_image

                }
                else -> {
                    drawable = R.drawable.ic_play_arrow

                }
            }
            val index = it.attachments?.file?.lastIndexOf('/')
            var listTitle =
                if (index == -1) it.attachments?.file else it.attachments?.file?.substring(
                    index.getSafe() + 1,
                    it.attachments?.file?.length.getSafe()
                )

//            val docType = metaData?.claimDocumentType?.find { docType ->
//                docType.genericItemId == it.documentType }?.genericItemName
//
//            if(docType.isNullOrEmpty().not())
                listTitle = "(${it.requestDocuments?.genericItemName.getSafe()}) $listTitle"

            claimViewModel.fileList.add(
                MultipleViewItem(
                    title = listTitle,
                    itemId = it.id.toString(),
                    drawable = drawable,
                ).apply {

                    type = it.attachments?.file
                    if (it.attachmentTypeId == Enums.AttachmentType.VOICE.key) itemCenterIcon =
                        R.drawable.ic_voice_group

                    isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                }
            )
        }
        addFileList()

        mBinding.attachmentDivider.setVisible(claimViewModel.fileList.size > 0)

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
            claimId = claimViewModel.claimResponse?.claim?.claimId,
            claimCategoryId = claimViewModel.claimRequest.claimCategoryId,
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

    private fun deleteClaimAttachment(item: MultipleViewItem) {
        val request =
            DeleteAttachmentRequest(claimAttachmentId = item.itemId?.toInt())

        if (isOnline(requireActivity())) {
            claimViewModel.deleteClaimAttachment(request = request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<*>
                            try {
                                showToast(getErrorMessage(response.message.getSafe()))
                                claimViewModel.fileList.remove(item)
                                itemsAdapter.listItems = claimViewModel.fileList
                                mBinding.attachmentDivider.setVisible(claimViewModel.fileList.size > 0)



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

    private fun showVoiceNoteDialog(isShow: Boolean) {

        audio.apply {
            title = mBinding.langData?.dialogsStrings?.addVoiceNote.getSafe()
            isPlayOnly = isShow
            positiveButtonText = mBinding.langData?.globalString?.done.getSafe()
            negativeButtonText = mBinding.langData?.globalString?.cancel.getSafe()
            voiceNote =  mBinding.langData?.dialogsStrings?.msgRecordAudio.getSafe()
            cancel = mBinding.langData?.globalString?.cancel.getSafe()
            onSaveFile = { mfile, time ->
                file = mfile
                elapsedMillis = time
                saveFile()
            }
            show()
            tvVoiceNote = mBinding.tvVoiceNote
        }

    }


}