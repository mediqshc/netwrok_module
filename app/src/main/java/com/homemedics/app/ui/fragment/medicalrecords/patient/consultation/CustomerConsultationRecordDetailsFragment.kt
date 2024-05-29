package com.homemedics.app.ui.fragment.medicalrecords.patient.consultation

import android.app.AlertDialog
import android.content.Intent
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
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.emr.EMRDownloadRequest
import com.fatron.network_module.models.request.emr.type.EMRDetailsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.emr.type.EmrDetails
import com.fatron.network_module.models.response.emr.type.EmrVital
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.databinding.FragmentCustomerConsultationRecordDetailsBinding
import com.homemedics.app.databinding.ItemSharedUserViewBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.activity.EMRActivity
import com.homemedics.app.ui.activity.HomeActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.adapter.EMRVitalItemsAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import okhttp3.MultipartBody
import java.io.File
import java.io.IOException

class CustomerConsultationRecordDetailsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentCustomerConsultationRecordDetailsBinding
    private lateinit var fileUtils: FileUtils
    private lateinit var dialogViewBinding: FragmentAddVoicenoteBinding
    private val emrViewModel: EMRViewModel by activityViewModels()
    private lateinit var vitalsAdapter: EMRVitalItemsAdapter
    private var attachmentsAdapter = AddMultipleViewAdapter()
    private var labTestList: ArrayList<MultipleViewItem>? = null
    private var player: MediaPlayer? = null
    private var length = 0
    private var emrDetailsResponse: EmrDetails? = null
    private var isSharedPerson = false
    private val colon = "\u003A"
    private lateinit var audio: CustomAudio

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            caSymptoms.title = lang?.emrScreens?.symptoms.getSafe()
            caDiagnosis.title = lang?.emrScreens?.diagnosis.getSafe()
            caMedicalProcedures.title = lang?.emrScreens?.medicalHealth.getSafe()
            caMedications.title = lang?.emrScreens?.medications.getSafe()
            caLabDiagnostic.title = lang?.emrScreens?.labAndDiagnostics.getSafe()
        }
    }

    override fun init() {
        audio = CustomAudio(requireContext())

        setupViews()
        fileUtils = FileUtils()
        fileUtils.init(this)

        if (emrViewModel.emrID != 0) //syncing tempEmrID and emrID
            emrViewModel.tempEmrID = emrViewModel.emrID

        if (emrViewModel.tempEmrID != 0)
            emrViewModel.emrID = emrViewModel.tempEmrID

        vitalsAdapter = EMRVitalItemsAdapter()

        mBinding.apply {
            rvVitals.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            rvVitals.adapter = vitalsAdapter
        }

        val request = EMRDetailsRequest(emrId = emrViewModel.tempEmrID)
        getRecordsDetailsApi(request)
    }

    override fun getFragmentLayout() = R.layout.fragment_customer_consultation_record_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCustomerConsultationRecordDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                if (requireActivity() is CallActivity) {
                    (requireActivity() as CallActivity).removeEMRNavigation()
                } else
                    findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                findNavController().safeNavigate(R.id.action_customerConsultationRecordDetailsFragment_to_shareRecordWithFragment)
            }
            actionbar.onAction3Click = {

                downloadReportApi()
            }
            caLabDiagnostic.onEditItemCall = { item ->
                if (item.itemEndIcon == R.drawable.ic_upload_black)
                    getDocument(item.itemId)
                else {
                    val attachemntId =
                        emrDetailsResponse?.emrCustomerAttachments?.find { it.emrTypeId == item.itemId?.toInt() }?.id
                    downloadReportApi(attachemntId)
                }
            }
        }
    }

    private fun getDocument(itemId: String?) {
        fileUtils.requestFilePermissions(requireActivity()) { result ->
            val file = result?.uri.let { uri ->
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
                uploadReportApi(result, itemId)

            }
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

    private fun uploadReportApi(result: FileUtils.FileData?, itemId: String?) {
        val mediaList = ArrayList<MultipartBody.Part>()

        var type =
            fileUtils.getMimeType(requireContext(), Uri.parse(result?.path.toString())).getSafe()
        if (type == "")
            type = result?.uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }.toString()
        val multipartFile = fileUtils.convertFileToMultiPart(
            File(result?.path.toString()),
            type,
            "attachments"
        )
        val typeImage =
            if (type.contains("image").getSafe()) type else ""

        val mimeType = when (type) {
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

        mediaList.add(multipartFile)
        emrViewModel.addCustomerEMRAttachment(
            emrViewModel.tempEmrID,
            Enums.EMRType.CONSULTATION.key,
            itemId?.toInt(),
            mimeType.toString(),
            mediaList
        ).observe(this) {
            if (isOnline(activity)) {
                when (it) {
                    is ResponseResult.Success -> {
                        hideLoader()

                        val response = it.data as ResponseGeneral<AttachmentResponse>
                        response.data?.attachments?.let {
                            emrDetailsResponse?.emrCustomerAttachments = it
                            mBinding.caLabDiagnostic.listItems = arrayListOf()
                            mBinding.caLabDiagnostic.listItems = getLabList(emrDetailsResponse)
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
    }

    override fun onPause() {
        super.onPause()

        audio.onPause()

        if (emrViewModel.tempEmrID == 0)
            emrViewModel.tempEmrID = emrViewModel.emrID.getSafe()
        emrViewModel.emrID = 0
    }

    override fun onClick(v: View?) {

    }

    private fun setupViews() {
        mBinding.apply {
            if (emrViewModel.isPatient) {
                actionbar.apply {
                    action2Res = 0
                    action3Res = 0
                }
                tvAttachments.text = lang?.globalString?.otherAttachments.getSafe()
            }
        }
    }

    private fun setDataInViews(response: EmrDetails?) {
        val timeFormat = if (DateFormat.is24HourFormat(binding.root.context))
            "HH:mm"
        else "hh:mm aa"

        response?.let { details ->
            val hash = "\u0023"
            mBinding.apply {
                actionbar.title =
                    "${lang?.emrScreens?.record} $hash ${Constants.START}${details.emrNumber} ${Constants.END}"

                isSharedPerson = details.customerUser?.id != DataCenter.getUser()?.id
                if (isSharedPerson)
                    mBinding.actionbar.action2Res = 0
                val dateStamp: String = getDateInFormat(
                    details.date.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy"
                )
                iDoctor.apply {
                    val isPatient = details.speciality.isNullOrEmpty()
                    val isLabReport = details.isLabReport.getBoolean()

                    if (isLabReport) {
                        tvTitle.text = details.partnerName.toString()
                        tvDesc.text = dateStamp
                        ivThumbnail.loadImage(
                            details.labIconUrl
                        )
                    } else if (isPatient) {
                        tvTitle.text = details.customerUser?.fullName.getSafe()
                        tvDesc.text = getDateInFormat(
                            details.date.getSafe(),
                            "yyyy-MM-dd hh:mm:ss",
                            "dd MMMM, yy || $timeFormat"
                        )
                        ivThumbnail.loadImage(
                            details.customerUser?.userProfilePicture?.file,
                            getGenderIcon(details.customerUser?.genderId.toString())
                        )
                    } else {
                        tvTitle.text = details.partnerName.toString()
                        tvDesc.text =
                            "${details.speciality?.get(0)?.genericItemName} ${Constants.PIPE} $dateStamp"
                        val icon = CustomServiceTypeView.ServiceType.getServiceById(
                            details.partnerServiceId.getSafe().toInt()
                        )?.icon.getSafe()
                        ivIcon.setImageResource(icon)
                        ivThumbnail.invisible()
                    }
                }

                if (details.emrVitals.isNullOrEmpty().not()) {
                    vitalsAdapter.listItems = details.emrVitals?.sortedBy { it.sortBy }
                        ?.toMutableList() as ArrayList<EmrVital>
                }
                setAttachments(response.emrAttachments)

                caSymptoms.listItems = (details.emrSymptoms as ArrayList<MultipleViewItem>?)?.map {
                    it.drawable = R.drawable.ic_bloodtype
                    it.itemEndIcon = 0
                    it
                } as ArrayList<MultipleViewItem>? ?: arrayListOf()

                caDiagnosis.listItems =
                    (details.emrDiagnosis as ArrayList<MultipleViewItem>?)?.map {
                        it.drawable = R.drawable.ic_bloodtype
                        it.itemEndIcon = 0
                        it
                    } as ArrayList<MultipleViewItem>? ?: arrayListOf()

                caMedicalProcedures.listItems =
                    (details.emrMedicalHealthcares as ArrayList<MultipleViewItem>?)?.map {
                        it.drawable = R.drawable.ic_emergency
                        it.itemEndIcon = 0
                        it
                    } as ArrayList<MultipleViewItem>? ?: arrayListOf()

                caMedications.listItems = ((details.emrProducts)?.map {
                    it.drawable = R.drawable.ic_medical_briefcase
                    it.itemEndIcon = 0

                    var desc = ""
                    var med = ""

                    val hourlyDosage =
                        DataCenter.getMeta()?.dosageQuantity?.find { hr -> hr.genericItemId == it.dosage?.hourly?.toInt() }?.genericItemName
                    val dosage = if (it.dosage?.hourly == null) {
                        val morning =
                            if (it.dosage?.morning.isNullOrEmpty()) "0" else it.dosage?.morning
                        val afternoon =
                            if (it.dosage?.afternoon.isNullOrEmpty()) "0" else it.dosage?.afternoon
                        val evening =
                            if (it.dosage?.evening.isNullOrEmpty()) "0" else it.dosage?.evening

                        "[$morning + $afternoon + $evening]"
                    } else {
                        "[${hourlyDosage.getSafe()}]"
                    }
                    med += "\n▪ ${it.name} $dosage ${Constants.START} ${Constants.MULTIPLY}${Constants.START} ${it.noOfDays} ${lang?.globalString?.days.getSafe()} ${Constants.END}${Constants.END}"
                    if (it.dosage?.hourly != null)
                        med += " || ${lang?.globalString?.quantity} $colon ${it.dosageQuantity}"
                    desc = "$desc $med"
                    it.desc = "${it.description} $desc"
                    it.descMaxLines = 9
                    it
                }) as ArrayList<MultipleViewItem>? ?: arrayListOf()
                labTestList = details.emrLabTests as ArrayList<MultipleViewItem>?
                caLabDiagnostic.listItems = getLabList(details)

                emrViewModel.selectedFamilyForShare =
                    details.shared as ArrayList<FamilyConnection>? ?: arrayListOf()
                setSharedFamily()
                hideViews(response)
            }
        }
    }

    private fun getLabList(response: EmrDetails?): ArrayList<MultipleViewItem> {

        return labTestList?.map {
            var drawableId = R.drawable.ic_upload_black
            if (response?.emrCustomerAttachments?.find { attachment -> attachment.emrTypeId == it.itemId?.toInt() } != null) {
                drawableId = R.drawable.ic_download_black
            }

            val bookedUserId = response?.bookedForUser?.id
            if (bookedUserId != DataCenter.getUser()?.id)
                drawableId = 0

            it.drawable = R.drawable.ic_bloodtype
            it.itemEndIcon = drawableId
            it
        } as ArrayList<MultipleViewItem>? ?: arrayListOf()
    }

    private fun hideViews(response: EmrDetails?) {
        mBinding.apply {
            if (response?.emrVitals?.isEmpty().getSafe()) {
                tvVitalsSymptoms.gone()
                rvVitals.gone()
            }
            if (response?.emrSymptoms?.isEmpty().getSafe()) {
                caSymptoms.gone()
            }
            if (response?.emrDiagnosis?.isEmpty().getSafe()) {
                caDiagnosis.gone()
                vDiagnosis.gone()
            }
            if (response?.emrProducts?.isEmpty().getSafe()) {
                caMedications.gone()
            }
            if (response?.emrLabTests?.isEmpty().getSafe()) {
                caLabDiagnostic.gone()
            }
            if (response?.emrMedicalHealthcares?.isEmpty().getSafe()) {
                caMedicalProcedures.gone()
            }
            if (response?.emrAttachments?.isEmpty().getSafe()) {
                tvAttachments.gone()
                rvAttachments.gone()
                tvNoData.gone()
                vAttachments.gone()
            }
            if (response?.emrVitals?.isEmpty().getSafe() && response?.emrSymptoms?.isEmpty()
                    .getSafe()
            ) {
                tvVitalsSymptoms.gone()
                rvVitals.gone()
                caSymptoms.gone()
                vSymtom.gone()
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
            mBinding.rvAttachments.adapter = attachmentsAdapter

            attachmentsAdapter.onEditItemCall = { item ->
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
                val listTitle =
                    if (index == -1) it.attachments?.file else it.attachments?.file?.substring(
                        index.getSafe() + 1,
                        it.attachments?.file?.length.getSafe()
                    )

                val locale =
                    TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
                list.add(
                    MultipleViewItem(
                        title = listTitle,
                        itemId = it.attachments?.file,
                        drawable = drawable,
                    ).apply {
                        itemEndIcon = R.drawable.ic_arrow_fw_black
                        if (it.attachmentTypeId == Enums.AttachmentType.VOICE.key) itemCenterIcon =
                            R.drawable.ic_voice_group

                        isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                    }
                )
            }

            attachmentsAdapter.listItems = list
        }
    }

    private fun setSharedFamily() {
        mBinding.apply {
            llSharedWithMain.setVisible(emrViewModel.selectedFamilyForShare.isNotEmpty() && isSharedPerson.not() && activity is EMRActivity /* show shared bar to only customer not to doc */)
            vShared.setVisible(emrViewModel.selectedFamilyForShare.isNotEmpty() && isSharedPerson.not() && activity is EMRActivity /* show shared bar to only customer not to doc */)
            val showLimit = 3

            if (emrViewModel.selectedFamilyForShare.isNotEmpty()) {
                val remainingCount = emrViewModel.selectedFamilyForShare.size - showLimit

                run breaking@{
                    emrViewModel.selectedFamilyForShare.forEachIndexed { index, familyConnection ->
                        val itemBinding = ItemSharedUserViewBinding.inflate(layoutInflater)
                        val params: LinearLayout.LayoutParams =
                            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)

                        if (index != 0)
                            params.marginStart = resources.getDimensionPixelSize(R.dimen.dp8)

                        if (index < showLimit) {
                            itemBinding.tvTitle.text = familyConnection.fullName
                            itemBinding.ivThumbnail.loadImage(
                                familyConnection.userProfilePicture?.file,
                                R.drawable.ic_profile_placeholder
                            )

                            params.weight = 1.3f

                            itemBinding.root.layoutParams = params

                            itemBinding.llParent.setOnClickListener {
                                findNavController().safeNavigate(R.id.action_customerConsultationRecordDetailsFragment_to_shareRecordWithFragment)
                            }

                            llSharedWith.addView(itemBinding.root)
                        } else {
                            itemBinding.tvTitle.text = "$remainingCount+"
//                            itemBinding.tvTitle.text = "10+"
                            itemBinding.tvTitle.setPadding(
                                resources.getDimensionPixelSize(R.dimen.dp8),
                                0,
                                resources.getDimensionPixelSize(R.dimen.dp8),
                                0
                            )
                            itemBinding.ivThumbnail.gone()
                            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
                            itemBinding.root.layoutParams = params

                            itemBinding.llParent.setOnClickListener {
                                findNavController().safeNavigate(R.id.action_customerConsultationRecordDetailsFragment_to_shareRecordWithFragment)
                            }



                            llSharedWith.addView(itemBinding.root)
                            return@breaking
                        }
                    }
                }
            }
        }
    }

    private fun getRecordsDetailsApi(request: EMRDetailsRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.getCustomerConsultationRecordDetails(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<EmrDetails>
                        emrDetailsResponse = response.data
                        setDataInViews(response.data)
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
                        mBinding.root.gone()
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
                        mBinding.root.visible()
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

    private fun showVoiceNoteDialog(itemId: String?) {
        audio.apply {
            title = mBinding.lang?.globalString?.voiceNote.getSafe()
            negativeButtonText = mBinding.lang?.globalString?.cancel.getSafe()
            voiceNote = mBinding.lang?.dialogsStrings?.voiceNoteDescription.getSafe()
            fileName = itemId.getSafe()
            isPlayOnly = true
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
//
//                }
//            }
//        }.create()
//
//        builder.show()
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

    private fun downloadReportApi(itemId: Int? = null) {
        val request = EMRDownloadRequest(
            emrId = emrViewModel.tempEmrID,
            type = if (itemId == null) Enums.EMRType.CONSULTATION.key else Enums.EMRType.REPORTS.key, //only reports can be downloaded separately
            emrAttachmentId = itemId ?: 0
        )
        if (isOnline(requireActivity())) {
            val fileUtils =
                if (requireActivity() is EMRActivity) (requireActivity() as EMRActivity).fileUtils
                else if (requireActivity() is HomeActivity) (requireActivity() as HomeActivity).fileUtils
                else if (requireActivity() is CallActivity) (requireActivity() as CallActivity).fileUtils
                else null

            fileUtils?.let {
                emrViewModel.downloadEMR(request, fileUtils).observe(this) {
                    when (it) {
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