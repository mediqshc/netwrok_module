package com.homemedics.app.ui.fragment.medicalrecords.patient.medications

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.format.DateFormat
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Chronometer
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.emr.type.EMRDetailsRequest
import com.fatron.network_module.models.request.emr.type.EMRTypeDeleteRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerEMRRecordResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddNewMedicineRecordBinding
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import timber.log.Timber
import java.io.File
import java.io.IOException

class AddNewMedicationRecordFragment : BaseFragment() {

    private lateinit var mBinding: FragmentAddNewMedicineRecordBinding
    private var isModify = false

    private val emrViewModel: EMRViewModel by activityViewModels()
    private var file: File? = null
    private lateinit var fileUtils: FileUtils
    private var elapsedMillis = 0L
    private lateinit var animBlink: Animation
    private var itemsAdapter = AddMultipleViewAdapter()
    private var customerRecordResponse: CustomerRecordResponse? = null
    private lateinit var audio: CustomAudio

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.newMedicalRecord.getSafe()
            etRecordDate.hint = lang?.emrScreens?.dateAndTime.getSafe()
            caMedicine.apply {
                title = lang?.emrScreens?.medications.getSafe()
                custDesc = lang?.emrScreens?.addMedicineDesc.getSafe()
            }
        }
    }

    override fun init() {
        audio = CustomAudio(requireContext())

        emrViewModel.fileList = arrayListOf()

        setupViews()
        setDataInViews(null)

        fileUtils = FileUtils()
        fileUtils.init(this)
        animBlink = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.blink
        )

        mBinding.rvAttachments.adapter = itemsAdapter

        getRecordsDetailsApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_add_new_medicine_record

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddNewMedicineRecordBinding
    }

    override fun onPause() {
        super.onPause()

        audio.onPause()
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

            actionbar.onAction1Click = {
                findNavController().navigateUp()
            }

            actionbar.onAction2Click = {
                DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                    title = lang?.globalString?.warning.getSafe(),
                    message = lang?.dialogsStrings?.deleteDesc.getSafe(),
                    negativeButtonStringText = lang?.globalString?.cancel.getSafe(),
                    positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                    buttonCallback = {
                        deleteRecordApi()
                    }
                )
            }

            itemsAdapter.onEditItemCall = {
                if (it.drawable == R.drawable.ic_play_arrow)
                    showVoiceNoteDialog(true)
            }
            itemsAdapter.onDeleteClick = { item, position ->
                DialogUtils(requireActivity())
                    .showDoubleButtonsAlertDialog(
                        title = mBinding.lang?.dialogsStrings?.confirmDelete.getSafe(),
                        message = mBinding.lang?.dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = mBinding.lang?.globalString?.yes.getSafe(),
                        negativeButtonStringText = mBinding.lang?.globalString?.no.getSafe(),
                        buttonCallback = {
                            deleteDocumentApiCall(item)
                        },
                    )
            }
            caMedicine.onDeleteClick = { item, position ->
                deleteRecordItemApi(item.itemId?.toInt().getSafe(), position)
            }
            caMedicine.onEditItemCall = { item ->
                emrViewModel.medicineToModify =
                    customerRecordResponse?.products?.find { it.id == item.itemId?.toInt() }
                findNavController().safeNavigate(
                    AddNewMedicationRecordFragmentDirections.actionAddNewMedicationRecordFragmentToAddMedicineDetailsFragment()
                )
            }
            bSaveRecord.setOnClickListener {
                if (etRecordDate.text.isEmpty()) {
                    etRecordDate.errorText = lang?.fieldValidationStrings?.recordDateEmpty.getSafe()

                    return@setOnClickListener
                }
                if (caMedicine.listItems.isEmpty()) {
                    etRecordDate.errorText =
                        lang?.fieldValidationStrings?.medicationsEmpty.getSafe()

                    return@setOnClickListener
                }

                saveRecordApi()
            }
            caMedicine.onAddItemClick = {
                findNavController().safeNavigate(
                    AddNewMedicationRecordFragmentDirections.actionAddNewMedicationRecordFragmentToSelectMedicineFragment()
                )
            }

            etRecordDate.clickCallback = {
                openCalender(
                    etRecordDate.mBinding.editText,
                    valueOnlyReturn = true,
                    onDismiss = { dateSelected ->
                        if (dateSelected.isNotEmpty()) {
                            openTimeDialog(
                                etRecordDate.mBinding.editText,
                                valueOnlyReturn = true,
                                onDismiss = { timeSelected ->
                                    if (timeSelected.isNotEmpty()) {
                                        etRecordDate.text =
                                            "$dateSelected ${Constants.PIPE}${Constants.PIPE} ${Constants.START}$timeSelected${Constants.END}"
                                    }
                                },
                                parentFragment = parentFragmentManager
                            )
                        }
                    }
                )
            }

            tvVoiceNote.setOnClickListener {
                var hasRecording = false
                val fileList = emrViewModel.fileList
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
                        message = lang?.emrScreens?.voiceRecordingMsg.getSafe()
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
        }
    }

    private fun setupViews() {
        mBinding.apply {
            if (arguments?.containsKey(getString(R.string.modify)).getSafe()) {
                isModify = true
                actionbar.action2Res = R.drawable.ic_delete_white
            }
        }
    }

    private fun setUserDetails() {
        mBinding.apply {
            emrViewModel.selectedFamily?.let {
                iDoctor.tvTitle.text = it.fullName
                iDoctor.tvDesc.text = getCurrentDateTime("dd MMMM, yy")
                iDoctor.ivThumbnail.loadImage(it.profilePicture, getGenderIcon(it.genderId))
            } ?: kotlin.run {
                val user = DataCenter.getUser()
                iDoctor.tvTitle.text = user?.fullName
                iDoctor.tvDesc.text = getCurrentDateTime("dd MMMM, yy")
                iDoctor.ivThumbnail.loadImage(
                    user?.profilePicture,
                    getGenderIcon(user?.genderId.toString())
                )
            }
        }
    }

    private fun setDataInViews(details: CustomerRecordResponse?) {
        mBinding.apply {
            setUserDetails()

            bSaveRecord.isEnabled = details?.products.isNullOrEmpty().not()
            details?.let {
                setAttachments(it.attachments)

                iDoctor.apply {

                    if (details.speciality.isNullOrEmpty()) {
                        tvDesc.text = "${details.date}"
                    } else {
                        tvDesc.text =
                            "${details.speciality?.get(0)?.genericItemName} | ${details.date}"
                    }

                    if (details.serviceTypeId.isNullOrEmpty() || details.serviceTypeId == "0") {
                        setUserDetails()
                    } else {
                        tvTitle.text = details.partnerName.toString()
                        ivIcon.setImageResource(
                            CustomServiceTypeView.ServiceType.getServiceById(
                                details.serviceTypeId.getSafe().toInt()
                            )?.icon.getSafe()
                        )
                        ivThumbnail.invisible()
                    }
                }

                caMedicine.listItems = ((details.products?.map {
                    val hourlyDosage =
                        DataCenter.getMeta()?.dosageQuantity?.find { hr -> hr.genericItemId == it.dosage?.hourly?.toInt() }?.genericItemName
                    var desc = ""
                    var dosage = ""
                    dosage = if (it.dosage?.hourly == null) {
                        val morning =
                            if (it.dosage?.morning.isNullOrEmpty()) "0" else it.dosage?.morning
                        val afternoon =
                            if (it.dosage?.afternoon.isNullOrEmpty()) "0" else it.dosage?.afternoon
                        val evening =
                            if (it.dosage?.evening.isNullOrEmpty()) "0" else it.dosage?.evening

                        "$morning + $afternoon + $evening"
                    } else {
                        val colon = "\u003A"
                        "${hourlyDosage.getSafe()} ${Constants.PIPE}${Constants.PIPE} ${lang?.globalString?.quantity.getSafe()} $colon ${it.dosageQuantity}"
                    }

                    desc =
                        "$dosage ${Constants.PIPE}${Constants.PIPE} ${it.noOfDays} ${lang?.globalString?.days.getSafe()}"

                    it.hasSecondAction = isModify.not()
                    it.drawable = R.drawable.ic_medical_briefcase
//                    it.itemEndIcon = R.drawable.ic_delete
                    it.desc = it.description
//                    it.hasRoundLargeIcon = true
                    it.descMaxLines = 1
                    it.subDesc = desc
                    it
                }) as ArrayList<MultipleViewItem>?) ?: arrayListOf()
                if (it.originalDate != null && isModify) {
                    var timeFormat = "hh:mm aa"
                    if (DateFormat.is24HourFormat(binding.root.context)) {
                        timeFormat = "HH:mm"
                    }
                    val dateFormat = "dd/MM/yyyy ${Constants.PIPE}${Constants.PIPE} $timeFormat"
                    etRecordDate.text =
                        getDateInFormat(
                            details.originalDate.getSafe(),
                            "yyyy-MM-dd HH:mm",
                            dateFormat
                        )
                }

                if (isModify) {
                    val hash = "\u0023"
                    actionbar.title =
                        "${lang?.emrScreens?.modifyText.getSafe()} ${lang?.globalString?.recordNum?.lowercase()} $hash ${it.emrNumber}"
                }
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
        itemsAdapter.listItems = emrViewModel.fileList
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

    private fun callGetAttachments() {
        val request = EMRDetailsRequest(
            emrId = emrViewModel.emrID,
            type = emrViewModel.selectedEMRType?.key
        )
        if (isOnline(requireActivity())) {
            emrViewModel.getCustomerEMRRecordsDetails(request)
                .observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<CustomerEMRRecordResponse>
                            emrViewModel.fileList = arrayListOf()
                            val data = response.data?.emrDetails?.attachments
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

    private fun setAttachments(data: List<Attachment>?) {
        emrViewModel.fileList.clear()
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
            emrViewModel.fileList.add(
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
            emrViewModel.addCustomerEMRAttachment(
                emrViewModel.emrID,
                Enums.EMRType.MEDICATION.key,
                emrViewModel.selectedRecord?.genericItemId,
                mimeType.toString(),
                mediaList
            ).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<AttachmentResponse>
                            try {
                                setAttachments(response.data?.attachments)
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

    private fun deleteDocumentApiCall(item: MultipleViewItem) {
        val request =
            DeleteAttachmentRequest(
                emrAttachmentId = item.itemId?.toInt()
            )

        if (isOnline(requireActivity())) {
            emrViewModel.deleteCustomerEMRAttachment(request = request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<*>
                            try {
                                showToast(response.message.getSafe())
                                emrViewModel.fileList.remove(item)
                                itemsAdapter.listItems = emrViewModel.fileList
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
        audio.apply {
            title = mBinding.lang?.dialogsStrings?.addVoiceNote.getSafe()
            isPlayOnly = isShow
            positiveButtonText = mBinding.lang?.globalString?.done.getSafe()
            negativeButtonText = mBinding.lang?.globalString?.cancel.getSafe()
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

    private fun getRecordsDetailsApi() {
        val request = EMRDetailsRequest(
            emrId = emrViewModel.emrID,
            type = emrViewModel.selectedEMRType?.key
        )

        if (isOnline(requireActivity())) {
            emrViewModel.getCustomerEMRRecordsDetails(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<CustomerEMRRecordResponse>
                        customerRecordResponse = response.data?.emrDetails
                        setDataInViews(response.data?.emrDetails)
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

    private fun saveRecordApi() {
        val splitDate = mBinding.etRecordDate.text.split("||")
        var date: String? = null
        if (splitDate.isNotEmpty() && splitDate.size > 1) {
            date = "${splitDate[0]}${splitDate[1]}"

            date = getDateInFormat(date, "dd/MM/yyyy hh:mm aa", "yyyy-MM-dd HH:mm")
        }
        val request = EMRDetailsRequest(
            emrId = emrViewModel.emrID,
            type = emrViewModel.selectedEMRType?.key,
            date = date,
            modify = if (isModify) 1 else null
        )

        if (isOnline(requireActivity())) {
            emrViewModel.saveCustomerEMRRecord(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())
                        findNavController().popBackStack()
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

    private fun deleteRecordApi() {
        if (isOnline(requireActivity())) {
            emrViewModel.deleteCustomerEMRRecord().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())
                        findNavController().popBackStack(
                            R.id.customerMedicationsDetailsFragment,
                            true
                        )
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

    private fun deleteRecordItemApi(emrTypeId: Int, pos: Int) {
        val request = EMRTypeDeleteRequest(
            type = emrViewModel.selectedEMRType?.key,
            emrTypeId = emrTypeId
        )
        if (isOnline(requireActivity())) {
            emrViewModel.deleteCustomerEMRRecordType(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())
                        mBinding.caMedicine.listItems.removeAt(pos)
                        mBinding.caMedicine.mBinding.rvItems.adapter?.notifyDataSetChanged()
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

    private fun deleteRecordAttachmentItemApi(itemId: Int) {
        val request = DeleteAttachmentRequest(emrAttachmentId = itemId)
        if (isOnline(requireActivity())) {
            emrViewModel.deleteEMRDocument(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())
                        callGetAttachments()
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
}