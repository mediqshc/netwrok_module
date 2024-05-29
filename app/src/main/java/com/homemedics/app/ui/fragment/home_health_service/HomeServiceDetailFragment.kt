package com.homemedics.app.ui.fragment.home_health_service

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Chronometer
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.emr.AttachEMRtoBDCRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.homeservice.HomeServiceDetailResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.databinding.FragmentHomeServDetailsBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.model.AddressModel
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import com.homemedics.app.viewmodel.EMRViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import timber.log.Timber
import java.io.File
import java.io.IOException

class HomeServiceDetailFragment : BaseFragment(), View.OnClickListener {
    private var langData: RemoteConfigLanguage? = null
    private var itemsAdapter = AddMultipleViewAdapter()
    private lateinit var mBinding: FragmentHomeServDetailsBinding
    private val homeServiceViewModel: DoctorConsultationViewModel by activityViewModels()
    private val emrViewModel: EMRViewModel by activityViewModels()

    private lateinit var audio: CustomAudio

    private var absolutePath: String = ""
    var patientList= arrayListOf<FamilyConnection>()
    //audio data
    private lateinit var dialogViewBinding: FragmentAddVoicenoteBinding

    private var dialogSaveButton: Button? = null
    private var file: File? = null
    private lateinit var meter: Chronometer
    private lateinit var fileUtils: FileUtils
    private var elapsedMillis = 0L
    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null
    private var length = 0
    private lateinit var animBlink: Animation

    override fun init() {
        audio = CustomAudio(requireContext())
        getDetailCall()
        setActionbarCity()
        setView()
        if (homeServiceViewModel.homeConsultationRequest.bookingId != null)
            callGetAttachments()
        observe()

    }


    override fun setLanguageData() {
        langData = ApplicationClass.mGlobalData
    }

    override fun getFragmentLayout() = R.layout.fragment_home_serv_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentHomeServDetailsBinding
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
            bBookConsultation.setOnClickListener {
                when {
                    homeServiceViewModel.homeConsultationRequest.visitType == null -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.warning.getSafe(),
                                //                            buttonOneText=langData.globalString?.ok,
                                message = langData?.bookingScreen?.selectVisitType.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    homeServiceViewModel.homeConsultationRequest.userLocationId == null -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.warning.getSafe(),
                                //                            buttonOneText=langData.globalString?.ok,
                                message = langData?.bookingScreen?.selectAddress.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    else -> callApiBookService()
                }
            }
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }


            tvVoiceNote.setOnClickListener {
                var hasRecording = false
                val fileList = homeServiceViewModel.fileList
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
                        message =langData?.dialogsStrings?.recordingAlreadyAdded.getSafe()
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "application/pdf"
                    startActivityForResult(intent, 1)
                } else {
                fileUtils.requestFilePermissions(requireActivity(), false) { result ->
                    if (result == null) {
                        displayNeverAskAgainDialog(langData?.dialogsStrings?.storagePermissions.getSafe())
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
            caAddresses.onEditItemCall = {
                findNavController().safeNavigate(HomeServiceDetailFragmentDirections.actionHomeServiceDetailFragmentToSelectAddressFragment())
            }
            caAddresses.onAddItemClick = {
                findNavController().safeNavigate(HomeServiceDetailFragmentDirections.actionHomeServiceDetailFragmentToSelectAddressFragment())
            }

            caMedicalRecords.onAddItemClick = {
                findNavController().safeNavigate(
                        HomeServiceDetailFragmentDirections.actionHomeServiceDetailFragmentToPatientEmrNavigation(
                        bookingId = homeServiceViewModel.homeServiceRequest?.bookingId.getSafe()
                    )
                )
            }
            caMedicalRecords.onDeleteClick = { item, pos ->
                detachEMRtoBDC(item, pos)
            }

            tvAddImage.setOnClickListener {
                fileUtils.requestPermissions(requireActivity()) { result ->
                    if (result == null) {
                        displayNeverAskAgainDialog(langData?.dialogsStrings?.storagePermissions.getSafe())
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
    private fun displayNeverAskAgainDialog(message: String) {
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            message = message,
            positiveButtonStringText =langData?.dialogsStrings?.permitManual.getSafe(),
            negativeButtonStringText = langData?.globalString?.close.getSafe(),
            buttonCallback = {
                context?.let { gotoAppSettings(it) }
            },
            cancellable = false
        )

    }
    override fun onClick(v: View?) {

    }

    private fun observe() {
        homeServiceViewModel.selectedAddress.observe(this) {
            it?.let {
                mBinding.caAddresses.apply {
                    it.itemEndIcon = R.drawable.ic_edit

                    homeServiceViewModel.homeConsultationRequest.userLocationId =
                        (it as AddressModel?)?.id.getSafe()
                    listItems = arrayListOf(it)
                }
            }
        }
    }

    private fun callApiBookService() {
        homeServiceViewModel.homeConsultationRequest.apply {
            userLocationId =
                (homeServiceViewModel.selectedAddress.value as AddressModel?)?.id?.toInt()
            instructions = mBinding.etInstructions.text.toString()

            if (mBinding.cdPatients.selectionIndex != -1)
                patientId =
                    patientList[mBinding.cdPatients.selectionIndex].familyMemberId
        }
        if (isOnline(requireActivity())) {
            homeServiceViewModel.callApiBookService(homeServiceViewModel.homeConsultationRequest)
                .observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            DialogUtils(requireActivity())
                                .showDoubleButtonsAlertDialog(
                                    title = langData?.globalString?.requestSubmit.getSafe(),
                                    message = langData?.bookingScreen?.reqSubmitDesc.getSafe(),
                                    positiveButtonStringText = langData?.bookingScreen?.viewDetails.getSafe(),
                                    negativeButtonStringText = langData?.bookingScreen?.newRequest.getSafe(),
                                    buttonCallback = {
                                                     findNavController().safeNavigate(R.id.action_homeServiceDetailFragment_to_ordersDetailFragment,
                                                         bundleOf(Constants.BOOKINGID to homeServiceViewModel.homeConsultationRequest.bookingId))
                                    },
                                    negativeButtonCallback = {
                                        findNavController().safeNavigate(HomeServiceDetailFragmentDirections.actionHomeServiceDetailFragmentToHomeServiceListFragment())
                                    },
                                    cancellable = false
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
                                    message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getDetailCall() {
        if (isOnline(requireActivity())) {
            homeServiceViewModel.getHomeServiceDetails(homeServiceViewModel.homeServiceRequest)
                .observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<HomeServiceDetailResponse>
                            response.data?.let {
                                setData(it)
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
                    }
                }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun setView() {
        mBinding.langData = langData
        mBinding.cdPatients.hint=langData?.globalString?.patient.getSafe()
        mBinding.bookingRequest = homeServiceViewModel.homeConsultationRequest
        mBinding.actionbar.title = arguments?.getString(Constants.TITLE).getSafe()
        fileUtils = FileUtils()
        fileUtils.init(this)
        animBlink = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.blink

        )
        setAddress()

        itemsAdapter.onDeleteClick = { item, position ->
            langData?.apply {
                DialogUtils(requireActivity())
                    .showDoubleButtonsAlertDialog(
                        title =dialogsStrings?.confirmDelete.getSafe(),
                        message = dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = globalString?.yes.getSafe(),
                        negativeButtonStringText =  globalString?.no.getSafe(),
                        buttonCallback = {
                            deleteDocumentApiCall(item)

                        },
                    )
            }
        }
        mBinding.rvAttachments.adapter = itemsAdapter

        val font = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular)

        var radioButtonType: RadioButton
        val param = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        metaData?.homeServiceVisitTypes?.forEachIndexed { index, it ->

            radioButtonType = RadioButton(requireContext())
            radioButtonType.typeface = font
            radioButtonType.layoutParams = param
            val dp = resources.getDimensionPixelSize(R.dimen.dp16)
            val dpRight = resources.getDimensionPixelSize(R.dimen.dp8)
            radioButtonType.setPadding(0, dp, dpRight, dp)

            radioButtonType.text = it.genericItemName
            radioButtonType.id = it.genericItemId.getSafe()
            if (homeServiceViewModel.homeConsultationRequest.visitType != null && homeServiceViewModel.homeConsultationRequest.visitType == it.genericItemId.getSafe()) {
                radioButtonType.isChecked = true
            }
            radioButtonType.textSize = 13F
            mBinding.rgVisitType.addView(radioButtonType)

        }


        mBinding.rgVisitType.setOnCheckedChangeListener { _, checkedId ->
            homeServiceViewModel.homeConsultationRequest.visitType = checkedId
            mBinding.bBookConsultation.isEnabled=true

        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.bBookConsultation.isEnabled = mBinding.rgVisitType.checkedRadioButtonId != -1
        setPatientList(patientList)
    }

    private fun setAddress() {
        val user = DataCenter.getUser()
        if (user?.userLocations?.isNotEmpty().getSafe()) {
            var index = 0
            if (homeServiceViewModel.selectedAddress.value != null)
                index =
                    user?.userLocations?.indexOfFirst { it.address == homeServiceViewModel.selectedAddress.value?.desc?.getSafe() }
                        .getSafe()

            if (index == -1) index = 0

            val location = user?.userLocations?.get(index)

            val locCategory = metaData?.locationCategories?.find { loc ->
                loc.genericItemId == location?.category?.toInt().getSafe()
            }
            var categoryLoc = ""
            if (locCategory != null)
                categoryLoc = locCategory.genericItemName.getSafe()

            if (categoryLoc.uppercase().getSafe().contains("other".uppercase().getSafe())|| categoryLoc.uppercase().getSafe().contains(langData?.globalString?.other?.uppercase().getSafe()))
                categoryLoc = location?.other.getSafe()

            val userLocation = AddressModel()
            userLocation.apply {
                id = location?.id
                extraInt = location?.id
                streetAddress = location?.street.getSafe()
                category = categoryLoc
                categoryId = location?.category?.toInt().getSafe()
                floor = location?.floorUnit.getSafe()
                subLocality = location?.address.getSafe()
                region = location?.category.getSafe()
                latitude = location?.lat?.toDouble()
                longitude = location?.long?.toDouble()
                region = location?.region.getSafe()
                other = location?.other.getSafe()
                title = categoryLoc
                desc = location?.address.getSafe()
                itemId = locCategory?.genericItemId.getSafe().toString()
                drawable = R.drawable.ic_location_trans

            }
            homeServiceViewModel.selectedAddress.postValue(userLocation)
            mBinding.caAddresses.mBinding.tvAddNew.invisible() //should not be gone, UI issue will appear
        }
    }


    private fun setData(data: HomeServiceDetailResponse) {
        data.let {
            homeServiceViewModel.homeServiceRequest?.bookingId = it.bookingId
            homeServiceViewModel.homeConsultationRequest.apply {
                bookingId = it.bookingId
            }
            setPatientList(it.patients)

            val list = arrayListOf<MultipleViewItem>()
            data.medicalRecord?.forEach { medicalRecords ->
                list.apply {
                    add(
                        MultipleViewItem(
                            title = medicalRecords.emrName,
                            itemId = medicalRecords.emrId.toString(),
                            desc = "${medicalRecords.date} | ${langData?.globalString?.recordNum.getSafe()} ${medicalRecords.emrNumber}",
                            drawable = R.drawable.ic_medical_rec,
                        ).apply {
                            extraInt = medicalRecords.emrType
                        }
                    )
                }
            }
            mBinding.caMedicalRecords.listItems = list
        }
    }

    private fun setPatientList(patients: List<FamilyConnection>?) {
        mBinding.cdPatients.data = arrayListOf()
        patientList = patients as ArrayList<FamilyConnection>
        val patientNames = patients.map { p ->
            if (p.familyMemberId == DataCenter.getUser()?.id)
                p.fullName = langData?.globalString?.self
            p.fullName
        }
        mBinding.cdPatients.data = patientNames as ArrayList<String> ?: arrayListOf()
        mBinding.cdPatients.onItemSelectedListener = { item, index ->
            mBinding.cdPatients.selectionIndex = index
            homeServiceViewModel.homeConsultationRequest.patientId =
                patientList[index].familyMemberId.getSafe()

        }
        if (patientNames.isNullOrEmpty().not()) {
          var  selectionIndex = 0
            if (homeServiceViewModel.homeConsultationRequest.patientId != null) {
                val pos =
                    patientList.indexOfFirst { pt -> pt.familyMemberId == homeServiceViewModel.homeConsultationRequest.patientId }
                        .getSafe()
                selectionIndex = if (pos == -1) 0 else pos
            }
            mBinding.cdPatients.selectionIndex=selectionIndex
        }
}
    private fun setActionbarCity() {
        //action bar city name
        if (homeServiceViewModel.cityName.value?.isEmpty().getSafe()) {
            val user = DataCenter.getUser()
            user?.let {
                val city =
                    getCityList(user.countryId.getSafe())?.find { city -> city.id == user.cityId }
                mBinding.actionbar.desc = city?.name.getSafe()
            }
        } else mBinding.actionbar.desc = homeServiceViewModel.cityName.value.getSafe()
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

    private fun showFileSizeDialog() {
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = langData?.globalString?.information.getSafe(),
                message = langData?.dialogsStrings?.fileSize.getSafe(),
                buttonCallback = {},
            )
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

        if (isOnline(requireActivity())) {
            homeServiceViewModel.addConsultationAttachment(
                booking_id = homeServiceViewModel.homeConsultationRequest.bookingId,
                mimeType.toString(),
                mediaList
            ).observe(this) {
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
                            title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                            message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                            buttonCallback = {},
                        )
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun callGetAttachments() {
        if (isOnline(requireActivity())) {
            homeServiceViewModel.callGetAttachments(
                AppointmentDetailReq(
                    bookingId = homeServiceViewModel.homeConsultationRequest.bookingId
                        .toString()
                )
            )
                .observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<AttachmentResponse>
                            homeServiceViewModel.fileList = arrayListOf()
                            val data = response.data?.attachments
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
                                    message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun setAttachments(data: List<Attachment>?) {
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
            val listTitle =
                if (index == -1) it.attachments?.file else it.attachments?.file?.substring(
                    index.getSafe() + 1,
                    it.attachments?.file?.length.getSafe()
                )

            val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
            homeServiceViewModel.fileList.add(
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
        mBinding.rvAttachments.setVisible(true)
        addFileList()

        mBinding.attachmentDivider.setVisible(homeServiceViewModel.fileList.size > 0)
    }

    private fun detachEMRtoBDC(item: MultipleViewItem, pos: Int) {
        val request = AttachEMRtoBDCRequest(
            bookingId = homeServiceViewModel.homeServiceRequest?.bookingId,
            emrId = item.itemId?.toInt(),
            emrCustomerType = item.extraInt
        )

        if (isOnline(requireActivity())) {
            emrViewModel.detachEMRtoBDC(request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val type = object : TypeToken<ArrayList<MultipleViewItem>>(){}.type
                            val tempList: ArrayList<MultipleViewItem> = Gson().fromJson(Gson().toJson(mBinding.caMedicalRecords.listItems), type)
                            tempList.removeAt(pos)
                            mBinding.caMedicalRecords.listItems = arrayListOf()
                            mBinding.caMedicalRecords.listItems = tempList
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
                                    message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                            title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                            message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                            buttonCallback = {},
                        )
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun addFileList() {
        itemsAdapter.listItems = homeServiceViewModel.fileList
        lifecycleScope.launch {
            delay(100)
            mBinding.tvVoiceNote.isClickable = true
        }
    }

    private fun deleteDocumentApiCall(item: MultipleViewItem) {
        val request =
            DeleteAttachmentRequest(item.itemId?.toInt())

        if (isOnline(requireActivity())) {
            homeServiceViewModel.deleteDocumentApiCall(request = request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<*>
                            try {
                                showToast(response.message.getSafe())
                                homeServiceViewModel.fileList.remove(item)
                                itemsAdapter.listItems = homeServiceViewModel.fileList
                                mBinding.attachmentDivider.setVisible(homeServiceViewModel.fileList.size > 0)
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
                                    message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                            title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                            message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                            buttonCallback = {},
                        )
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
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
            voiceNote =  langData?.dialogsStrings?.voiceNoteDescMedicalstaff.getSafe()
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