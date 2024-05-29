package com.homemedics.app.ui.fragment.medicalrecords

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Chronometer
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.emr.StoreEMRRequest
import com.fatron.network_module.models.request.emr.Vital
import com.fatron.network_module.models.request.emr.medicine.MedicineDeleteRequest
import com.fatron.network_module.models.request.emr.type.EMRDetailsRequest
import com.fatron.network_module.models.request.emr.type.EMRTypeDeleteRequest
import com.fatron.network_module.models.request.emr.type.StoreEMRTypeRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.emr.type.EMRDetailsResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogAddEmrBinding
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.databinding.FragmentPrescriptionBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import timber.log.Timber
import java.io.File
import java.io.IOException

class PrescriptionFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentPrescriptionBinding
    private val emrViewModel: EMRViewModel by activityViewModels()

    private var itemsAdapter = AddMultipleViewAdapter()

    private lateinit var audio: CustomAudio

    private lateinit var fileUtils: FileUtils
    private lateinit var animBlink: Animation

    private var file: File? = null
    private var elapsedMillis = 0L

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData

            etMedicalAdvise.apply {
                hint = lang?.emrScreens?.medicalAdvice.getSafe()
            }
            caMedicines.apply {
                title = lang?.emrScreens?.medicines.getSafe()
                custDesc = lang?.emrScreens?.medicinesDesc.getSafe()
            }
            caLabs.apply {
                title = lang?.emrScreens?.labDiagnosticTests.getSafe()
                custDesc = lang?.emrScreens?.labDiagnosticTestsDesc.getSafe()
            }
            caHealthcare.apply {
                title = lang?.emrScreens?.medicalHealth.getSafe()
                custDesc = lang?.emrScreens?.medicalHealthDesc.getSafe()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audio.onPause()
    }

    override fun init() {
        //initilize audio
        audio = CustomAudio(requireContext())

        if (DataCenter.getUser().isMedicalStaff()) {
            mBinding.apply {
                caMedicines.gone()
                caLabs.gone()
                vDividerMed.gone()
                vDividerLab.gone()
            }
        }

        emrViewModel.prescriptionAttachmentList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)
        animBlink = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.blink
        )
        getEmrDetails()
    }

    override fun onResume() {
        super.onResume()
        getEmrDetails()
    }

    override fun getFragmentLayout() = R.layout.fragment_prescription

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPrescriptionBinding
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

    override fun setListeners() {
        mBinding.apply {
            bSaveDraft.setOnClickListener(this@PrescriptionFragment)
            bSendToPatient.setOnClickListener(this@PrescriptionFragment)
            caMedicines.apply {
                onAddItemClick = {
                    findNavController().safeNavigate(
                        MedicalRecordsFragmentDirections.actionMedicalRecordsFragmentToAddMedicinesFragment()
                    )
                }
                onEditItemCall = { item ->
                    emrViewModel.apply {
                        isEdit = true
                        medicineProduct =
                            emrDetails?.emrProducts?.find { it.id == item.itemId?.toInt() }
                    }

                    findNavController().safeNavigate(
                        MedicalRecordsFragmentDirections.actionMedicalRecordsFragmentToEnterDetailsFragment()
                    )
                }
                onDeleteClick = { item, _ ->
                    val request = MedicineDeleteRequest(emrProductId = item.itemId?.toInt())
                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                        title = lang?.dialogsStrings?.confirmDelete.getSafe(),
                        message = lang?.dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                        negativeButtonStringText = lang?.globalString?.no.getSafe(),
                        buttonCallback = {
                            deleteMedicineApi(request)
                        }
                    )
                }
            }
            caLabs.apply {
                onAddItemClick = {
                    findNavController().safeNavigate(
                        MedicalRecordsFragmentDirections.actionMedicalRecordsFragmentToAddTestFragment()
                    )
                }
                onEditItemCall = {
                    showEditEmrDialog(it, Enums.EMRTypesMeta.LAB_TEST.key)
                }
                onDeleteClick = { item, _ ->
                    val type =
                        metaData?.emrTypes?.find { it.genericItemId == Enums.EMRTypesMeta.LAB_TEST.key }?.genericItemId
                    val request =
                        EMRTypeDeleteRequest(emrTypeId = item.itemId?.toInt(), type = type)
                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                        title = lang?.dialogsStrings?.confirmDelete.getSafe(),
                        message = lang?.dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                        negativeButtonStringText = lang?.globalString?.no.getSafe(),
                        buttonCallback = {
                            deleteEMRType(request)
                        }
                    )
                }
            }
            caHealthcare.apply {
                onAddItemClick = {
                    findNavController().safeNavigate(
                        MedicalRecordsFragmentDirections.actionMedicalRecordsFragmentToAddRecommendationsFragment()
                    )
                }
                onEditItemCall = {
                    showEditEmrDialog(it, Enums.EMRTypesMeta.MEDICAL_HEALTHCARE.key)
                }
                onDeleteClick = { item, _ ->
                    val type =
                        metaData?.emrTypes?.find { it.genericItemId == Enums.EMRTypesMeta.MEDICAL_HEALTHCARE.key }?.genericItemId
                    val request =
                        EMRTypeDeleteRequest(emrTypeId = item.itemId?.toInt(), type = type)
                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                        title = lang?.dialogsStrings?.confirmDelete.getSafe(),
                        message = lang?.dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                        negativeButtonStringText = lang?.globalString?.no.getSafe(),
                        buttonCallback = {
                            deleteEMRType(request)
                        }
                    )
                }
            }

            itemsAdapter.onEditItemCall = {
                if (it.drawable == R.drawable.ic_play_arrow)
                    showVoiceNoteDialog(true)
            }

            tvVoiceNote.setOnClickListener {
                var hasRecording = false
                val fileList = emrViewModel.prescriptionAttachmentList
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "application/pdf"
                    startActivityForResult(intent, 1)
                } else {
                    fileUtils.requestFilePermissions(requireActivity(), false) { result ->
                        if (result == null) {
                            displayNeverAskAgainDialog(lang?.dialogsStrings?.storagePermissions.getSafe())
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
                }
            }
            tvAddImage.setOnClickListener {
                fileUtils.requestPermissions(requireActivity()) { result ->
                    if (result == null) {
                        displayNeverAskAgainDialog(lang?.dialogsStrings?.storagePermissions.getSafe())
                    } else {
                        file = result.uri.let { uri ->
                            uri?.let {
                                fileUtils.copyUriToFile(
                                    requireContext(),
                                    it,
                                    fileUtils.getFileNameFromUri(
                                        requireContext(), uri
                                    ),
                                    Constants.MEDIA_TYPE_IMAGE
                                )
                            }
                        }
                        saveFile()
                    }
                }
            }

            itemsAdapter.onDeleteClick = { item, _ ->
                DialogUtils(requireActivity())
                    .showDoubleButtonsAlertDialog(
                        title = lang?.dialogsStrings?.confirmDelete.getSafe(),
                        message = lang?.dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                        negativeButtonStringText = lang?.globalString?.no.getSafe(),
                        buttonCallback = {
                            deleteDocumentApiCall(item)
                        },
                    )
            }
            mBinding.rvAttachments.adapter = itemsAdapter
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bSaveDraft -> {
                storeApiCall(1)
            }
            R.id.bSendToPatient -> {
                storeApiCall(0)
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

    private fun addFileList() {
        itemsAdapter.listItems = emrViewModel.prescriptionAttachmentList
        lifecycleScope.launch {
            delay(100)
            mBinding.tvVoiceNote.isClickable = true
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

    private fun setAttachments(data: List<Attachment>?) {
        data?.forEach {
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
            val listTitle =
                if (index == -1) it.attachments?.file else it.attachments?.file?.substring(
                    index.getSafe() + 1,
                    it.attachments?.file?.length.getSafe()
                )

            val locale =
                TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
            emrViewModel.prescriptionAttachmentList.add(
                MultipleViewItem(
                    title = listTitle,
                    itemId = it.id.toString(),
                    drawable = drawable
                ).apply {
                    type = it.attachments?.file
                    if (it.attachmentTypeId == Enums.AttachmentType.VOICE.key) itemCenterIcon =
                        R.drawable.ic_voice_group

                    isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                }
            )
        }
        addFileList()
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
                File(path.getSafe()),
                mimeTypeView.getSafe(),
                "attachments"
            )
        mediaList.add(multipartFile)

        if (isOnline(requireActivity())) {
            emrViewModel.addEMRAttachment(
                emr_id = emrViewModel.emrID,
                attachment_type = mimeType.toString(),
                emr_type = 2,
                document = mediaList
            ).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            getEmrDetails()
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

    private fun deleteDocumentApiCall(item: MultipleViewItem) {
        val request = DeleteAttachmentRequest(emrAttachmentId = item.itemId?.toInt())

        if (isOnline(requireActivity())) {
            emrViewModel.deleteEMRDocument(request = request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<*>
                            try {
                                emrViewModel.prescriptionAttachmentList.remove(item)
                                itemsAdapter.listItems = emrViewModel.prescriptionAttachmentList
                                getEmrDetails()
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

    private fun showVoiceNoteDialog(isShow: Boolean) {
        // audio = CustomAudio(requireContext())
        audio.apply {
            title = mBinding.lang?.dialogsStrings?.addVoiceNote.getSafe()
            isPlayOnly = isShow
            positiveButtonText = mBinding.lang?.globalString?.done.getSafe()
            negativeButtonText = mBinding.lang?.globalString?.cancel.getSafe()
            voiceNote = mBinding.lang?.dialogsStrings?.patientVoiceNote.getSafe()
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

    private fun getEmrDetails() {
        val emrDetailsRequest = EMRDetailsRequest(emrId = emrViewModel.emrID)
        if (isOnline(requireActivity())) {
            emrViewModel.emrDetails(emrDetailsRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<EMRDetailsResponse>
                        response.data?.let { emrDetailsResponse ->
                            emrViewModel.emrDetails = emrDetailsResponse.emrDetails
                            emrViewModel.prescriptionAttachmentList.clear()
                            var prescriptionAttachment: List<Attachment>? = arrayListOf()
                            emrDetailsResponse.emrDetails?.emrAttachments?.forEach { attachment ->
                                if (attachment.type == Enums.EMRAttachmentsType.PRESCRIPTION.key.toString()) {
                                    prescriptionAttachment =
                                        emrDetailsResponse.emrDetails?.emrAttachments?.filter { it.type == Enums.EMRAttachmentsType.PRESCRIPTION.key.toString() }
                                }
                            }
                            setAttachments(prescriptionAttachment)
                            mBinding.apply {
                                var desc = ""
                                val product =
                                    emrDetailsResponse.emrDetails?.emrProducts?.map { medicine ->
                                        val hourlyDosage =
                                            metaData?.dosageQuantity?.find { hr -> hr.genericItemId == medicine.dosage?.hourly?.toInt() }?.genericItemName
                                        desc =
                                            if (medicine.dosage?.hourly != null) "${medicine.description}\n${hourlyDosage} || ${medicine.noOfDays} ${mBinding.lang?.globalString?.days.getSafe()}"
                                            else "${medicine.description}\n${medicine.dosage?.morning}+${medicine.dosage?.afternoon}+${medicine.dosage?.evening} || ${medicine.noOfDays} ${mBinding.lang?.globalString?.days.getSafe()}"
                                        medicine.desc = medicine.description
                                        medicine.hasSecondAction = true
                                        medicine.descMaxLines = 4
                                        medicine.hasRoundLargeIcon = true
                                        medicine.drawable = R.drawable.ic_placeholder
                                        medicine.subDesc =
                                            if (medicine.dosage?.hourly != null) "$hourlyDosage || ${medicine.noOfDays} ${mBinding.lang?.globalString?.days.getSafe()}"
                                            else "${medicine.dosage?.morning}+${medicine.dosage?.afternoon}+${medicine.dosage?.evening} || ${medicine.noOfDays} ${mBinding.lang?.globalString?.days.getSafe()}"
                                        medicine
                                    }
                                caMedicines.listItems =
                                    product as ArrayList<MultipleViewItem>? ?: arrayListOf()
                                caHealthcare.listItems =
                                    ((emrDetailsResponse.emrDetails?.emrMedicalHealthcares as ArrayList<MultipleViewItem?>?)?.map {
                                        it?.hasRoundLargeIcon = false
                                        it?.hasSecondAction = true
                                        it?.descMaxLines = 4
                                        it
                                    } as ArrayList<MultipleViewItem>?) ?: arrayListOf()

                                caLabs.listItems =
                                    ((emrDetailsResponse.emrDetails?.emrLabTests as ArrayList<MultipleViewItem?>?)?.map {
                                        it?.hasRoundLargeIcon = false
                                        it?.hasSecondAction = true
                                        it?.descMaxLines = 4
                                        it
                                    } as ArrayList<MultipleViewItem>?) ?: arrayListOf()

                                bSendToPatient.isEnabled =
                                    emrDetailsResponse.emrDetails?.emrSymptoms?.isNotEmpty()
                                        .getSafe() ||
                                            emrDetailsResponse.emrDetails?.emrDiagnosis?.isNotEmpty()
                                                .getSafe() ||
                                            emrDetailsResponse.emrDetails?.emrProducts?.isNotEmpty()
                                                .getSafe() ||
                                            emrDetailsResponse.emrDetails?.emrMedicalHealthcares?.isNotEmpty()
                                                .getSafe() ||
                                            emrDetailsResponse.emrDetails?.emrLabTests?.isNotEmpty()
                                                .getSafe() ||
                                            prescriptionAttachment?.isNotEmpty().getSafe() ||
                                            emrViewModel.vitals?.isNotEmpty().getSafe()

                                bSaveDraft.isEnabled =
                                    emrDetailsResponse.emrDetails?.emrSymptoms?.isNotEmpty()
                                        .getSafe() ||
                                            emrDetailsResponse.emrDetails?.emrDiagnosis?.isNotEmpty()
                                                .getSafe() ||
                                            emrDetailsResponse.emrDetails?.emrProducts?.isNotEmpty()
                                                .getSafe() ||
                                            emrDetailsResponse.emrDetails?.emrMedicalHealthcares?.isNotEmpty()
                                                .getSafe() ||
                                            emrDetailsResponse.emrDetails?.emrLabTests?.isNotEmpty()
                                                .getSafe() ||
                                            prescriptionAttachment?.isNotEmpty().getSafe() ||
                                            emrViewModel.vitals?.isNotEmpty().getSafe()
                            }
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

    private fun editEMRType(storeEMRTypeRequest: StoreEMRTypeRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.editEMRType(storeEMRTypeRequest).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        getEmrDetails()
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

    private fun deleteEMRType(request: EMRTypeDeleteRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.deleteEMRType(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        getEmrDetails()
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

    private fun showEditEmrDialog(item: MultipleViewItem? = null, type: Int? = null) {
        val dialog: AlertDialog
        val emrDialogBuilder =
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme).apply {
                val addEmrBinding = DialogAddEmrBinding.inflate(layoutInflater)
                var emrType: Int? = 0
                if (item?.title != null || item?.desc != null) {
                    addEmrBinding.apply {
                        etTitle.apply {
                            text = item.title.getSafe()
                            mBinding.editText.apply {
                                isEnabled = false
                                isClickable = false
                                setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.grey
                                    )
                                )
                            }
                        }
                        etDescription.setText(item.desc.getSafe())
                    }
                }
                setView(addEmrBinding.root)
                setCancelable(false)
                when (type) {
                    Enums.EMRTypesMeta.LAB_TEST.key -> {
                        setTitle(mBinding.lang?.emrScreens?.editTest.getSafe())
                        emrType =
                            metaData?.emrTypes?.find { it.genericItemId == type }?.genericItemId
                    }
                    Enums.EMRTypesMeta.MEDICAL_HEALTHCARE.key -> {
                        setTitle(mBinding.lang?.emrScreens?.editRecommendation.getSafe())
                        emrType =
                            metaData?.emrTypes?.find { it.genericItemId == type }?.genericItemId
                    }
                }

                setPositiveButton(mBinding.lang?.globalString?.add.getSafe()) { _, _ ->
                    closeKeypad()
                    val name = addEmrBinding.etTitle.text
                    val description = addEmrBinding.etDescription.text

                    val storeEMRRequest = StoreEMRTypeRequest(
                        description = description.toString(),
                        name = name,
                        type = emrType.toString(),
                        emrTypeId = item?.itemId
                    )
                    editEMRType(storeEMRRequest)
                    this.create().dismiss()
                }
                setNegativeButton(mBinding.lang?.globalString?.cancel.getSafe(), null)
            }

        dialog = emrDialogBuilder.create()
        dialog.show()
    }

    private fun deleteMedicineApi(request: MedicineDeleteRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.deleteMedicine(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        getEmrDetails()
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

    private fun storeApiCall(isDrafted: Int) {
        var vitals: ArrayList<Vital>? = null
        if (emrViewModel.vitals?.isNotEmpty().getSafe()) {
//            if (emrViewModel.systolic.isEmpty()) {
//                DialogUtils(requireActivity())
//                    .showSingleButtonAlertDialog(
//                        title = getString(R.string.alert),
//                        message = getString(R.string.input_systolic),
//                    )
//                return
//            } else if (emrViewModel.diastolic.isEmpty()) {
//                DialogUtils(requireActivity())
//                    .showSingleButtonAlertDialog(
//                        title = getString(R.string.alert),
//                        message = getString(R.string.input_diastolic),
//                    )
//                return
//            }

            if ((emrViewModel.systolic.isEmpty() && emrViewModel.diastolic.isEmpty())
                || (emrViewModel.systolic.isNotEmpty() && emrViewModel.diastolic.isNotEmpty())
            ) {
                vitals = emrViewModel.vitals
            } else {
                DialogUtils(requireActivity())
                    .showSingleButtonAlertDialog(
                        title = mBinding.lang?.labPharmacyScreen?.alert.getSafe(),
                        message = if (emrViewModel.systolic.isEmpty()) mBinding.lang?.labPharmacyScreen?.systolic.getSafe() else mBinding.lang?.labPharmacyScreen?.diastolic.getSafe(),
                    )

                return
            }
        }
        val request = StoreEMRRequest(
            bookingId = emrViewModel.bookingId.toString(),
            customerId = emrViewModel.customerId,
            emrId = emrViewModel.emrID.toString(),
            isDraft = isDrafted,
            emrChat = if (emrViewModel.emrChat) 1 else 0,
            vitals = vitals,
            medicalAdvice = mBinding.etMedicalAdvise.text.toString()
        )
        storeEMR(request)
    }

    private fun storeEMR(request: StoreEMRRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.storeEMR(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        getEmrDetails()
                        if (requireActivity() is CallActivity) {
                            (requireActivity() as CallActivity).goBackDuringCall()
                        } else {
                            emrViewModel.docEmr.value = true
                            findNavController().popBackStack(R.id.medical_records_navigation, true)
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
}