package com.homemedics.app.ui.fragment.labtests

import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentLabTestBookingBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.model.AddressModel
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.LabTestViewModel
import okhttp3.MultipartBody
import java.io.File

class LabTestBookingFragment : BaseFragment(), View.OnClickListener {

    private val labTestViewModel: LabTestViewModel by activityViewModels()
    private lateinit var mBinding: FragmentLabTestBookingBinding

    private var file: File? = null
    private lateinit var fileUtils: FileUtils
    private var itemsAdapter = AddMultipleViewAdapter()
    private var isToggleOn = false

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.labTest.getSafe()
            caPrescription.apply {
                title = lang?.labPharmacyScreen?.doYouHaveAPrescription.getSafe()
                custDesc = lang?.labPharmacyScreen?.prescriptionDescLab.getSafe()
            }
            tvPatientDetail.text = lang?.labPharmacyScreen?.patientDetails.getSafe()
            cdPatients.hint = lang?.globalString?.patient.getSafe()
            etInstructions.hint = lang?.globalString?.specialInstructions.getSafe()
            caDeliveryAddress.apply {
                title = lang?.labPharmacyScreen?.isSampleCollectionRequired.getSafe()
                custDesc = lang?.labPharmacyScreen?.chooseSampleAddress.getSafe()
            }
            caPreferredLab.apply {
                title = lang?.labPharmacyScreen?.preferLab.getSafe()
                custDesc = lang?.labPharmacyScreen?.preferLabDesc.getSafe()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        labTestViewModel.flushData()
    }

    override fun init() {
        labTestViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)

        val savedBookingId = tinydb.getInt(com.fatron.network_module.utils.Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
        if(savedBookingId != 0){
            labTestViewModel.bookingIdResponse.bookingId = savedBookingId
        }

        observe()
        setDataInViews(null)
        createBookingId()
        setAddress()
    }

    override fun getFragmentLayout() = R.layout.fragment_lab_test_booking

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentLabTestBookingBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            bSendRequest.setOnClickListener(this@LabTestBookingFragment)

            caPrescription.onAddItemClick = {
                fileUtils.requestPermissions(requireActivity(), mIsChooser = true) { result ->
                    if (result == null) {
                        displayNeverAskAgainDialog(mBinding.lang?.dialogsStrings?.storagePermissions.getSafe())
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
            caPrescription.onDeleteClick = { item, _->
                deleteDocumentApiCall(item)
            }
            caPreferredLab.onAddItemClick = {
                findNavController().safeNavigate(LabTestBookingFragmentDirections.actionLabTestBookingFragmentToSelectMainLabFragment())
            }
            caPreferredLab.onDeleteClick = { item, _->
                labTestViewModel.selectedMainLab = null
                labTestViewModel.selectedLabBranch = null
                caPreferredLab.listItems = arrayListOf()
                caPreferredLab.addButtonEnabled = caPreferredLab.listItems.isEmpty()
            }
            caDeliveryAddress.onEditItemCall = {
                findNavController().safeNavigate(LabTestBookingFragmentDirections.actionLabTestBookingFragmentToSelectAddressNavigation())
            }
            caDeliveryAddress.onAddItemClick = {
                findNavController().safeNavigate(LabTestBookingFragmentDirections.actionLabTestBookingFragmentToSelectAddressNavigation())
            }
            caDeliveryAddress.mBinding.switchButton.apply {
                setOnClickListener {
                    labTestViewModel.bookConsultationRequest.homeCollection = isChecked.getInt()
                    isToggleOn = isChecked
                    caDeliveryAddress.isEnabled = isChecked
                    if (caDeliveryAddress.listItems.isEmpty()) {
                        caDeliveryAddress.showAddButton = isChecked
                    } else if (caDeliveryAddress.listItems.isNotEmpty()) {
                        caDeliveryAddress.showAddButton = false
                        labTestViewModel.bookConsultationRequest.userLocationId =
                            (mBinding.caDeliveryAddress.listItems[0] as AddressModel?)?.id.getSafe()
                    }
                }
            }

            cdPatients.onItemSelectedListener = { item, index ->
                val selectedUser = labTestViewModel.bookingIdResponse.patients?.get(index)

                labTestViewModel.bookConsultationRequest.patientId =
                    selectedUser?.familyMemberId.getSafe()

                cdPatients.selectionIndex = index
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bSendRequest -> {
                labTestViewModel.bookConsultationRequest.apply {
                    homeCollection = if (isToggleOn.not()) 0 else 1
                    serviceId = CustomServiceTypeView.ServiceType.LaboratoryService.id

                    patientId = labTestViewModel.bookingIdResponse.patients?.get(mBinding.cdPatients.selectionIndex)?.familyMemberId
                    instructions = mBinding.etInstructions.text.toString()
                    bookingDate = (System.currentTimeMillis()/1000).toString()
                    preferredLaboratory = preferredLaboratory ?: 0
                    prescriptionFlow = 1
                    fee = "0"

                    var errorMsg: String? = null

                    if (mBinding.caDeliveryAddress.mBinding.switchButton.isChecked
                        && mBinding.caDeliveryAddress.listItems.isEmpty()
                    )
                        errorMsg = mBinding.lang?.labPharmacyScreen?.chooseAddressForLab.getSafe()

//                    if(preferredLaboratory == null)
//                        errorMsg = getString(R.string.select_preferred_lab)

                    if(mBinding.caPrescription.listItems.isNullOrEmpty())
                        errorMsg = mBinding.lang?.labPharmacyScreen?.uploadPrescriptionLabDesc.getSafe()

                    if (errorMsg != null) {
                        DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                            title = mBinding.lang?.globalString?.information.getSafe(),
                            message = errorMsg
                        )
                        return
                    }

                    bookConsultation(this)
                }
            }
        }
    }

    private fun observe() {
        labTestViewModel.selectedAddress.observe(this) {
            it?.let {
                mBinding.caDeliveryAddress.apply {
                    it.itemEndIcon = R.drawable.ic_edit

                    labTestViewModel.bookConsultationRequest.userLocationId =
                        (it as AddressModel?)?.id.getSafe()
                    listItems = arrayListOf(it)
                }
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

    private fun showRequestSubmitDialog() {
        DialogUtils(requireActivity())
            .showDoubleButtonsAlertDialog(
                title = mBinding.lang?.labPharmacyScreen?.requestSubmitted.getSafe(),
                message = mBinding.lang?.labPharmacyScreen?.submittedMsg.getSafe(),
                positiveButtonStringText = mBinding.lang?.bookingScreen?.viewDetails.getSafe(),
                negativeButtonStringText = mBinding.lang?.bookingScreen?.newRequest.getSafe(),
                buttonCallback = {
                    findNavController().safeNavigate(R.id.action_labTestBookingWithOrderDetailsFragment_to_ordersDetailFragment,
                        bundleOf(Constants.BOOKINGID to labTestViewModel.bookConsultationRequest.bookingId)
                    )
                },
                negativeButtonCallback = {
                    tinydb.remove(com.fatron.network_module.utils.Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
                    findNavController().navigate(R.id.action_labTestBookingFragment_to_labTestFragment)
                },
                cancellable = false
            )
    }

    private fun setDataInViews(data: PartnerProfileResponse?){
        labTestViewModel.bookConsultationRequest.preferredLaboratory = null
        mBinding.apply {
            labTestViewModel.let { vm ->
                vm.selectedLabBranch?.let{ item ->

                    caPreferredLab.listItems = arrayListOf(
                        (item as MultipleViewItem).apply {
                            imageUrl = item.labs?.iconUrl
                            hasLargeIcon = true
                        }
                    )
                    caPreferredLab.addButtonEnabled = caPreferredLab.listItems.isEmpty()

                    vm.bookConsultationRequest.preferredLaboratory = vm.selectedLabBranch?.id
                }

                caDeliveryAddress.mBinding.switchButton.isChecked = vm.bookConsultationRequest.homeCollection.getBoolean()
                caDeliveryAddress.isEnabled = vm.bookConsultationRequest.homeCollection.getBoolean()
            }

            data?.let {
                val patientNames = it.patients?.map { p ->
                    if (p.id == DataCenter.getUser()?.id)
                        p.fullName = mBinding.lang?.globalString?.self.getSafe()
                    p.fullName
                }
                mBinding.cdPatients.apply {
                    this.data = patientNames as ArrayList<String>? ?: arrayListOf()
                    if (patientNames.isNullOrEmpty().not()) {
                        selectionIndex = 0
                        if (labTestViewModel.bookConsultationRequest.patientId != null) {
                            val pos =
                                it.patients?.indexOfFirst { pt -> pt.familyMemberId == labTestViewModel.bookConsultationRequest.patientId }
                                    .getSafe()
                            selectionIndex = if (pos == -1) 0 else pos
                        }
                    }
                }
            }
        }
    }

    private fun setAddress() {
        val user = DataCenter.getUser()
        if (user?.userLocations?.isNotEmpty().getSafe()) {
            var index = 0
            if (labTestViewModel.selectedAddress.value != null)
                index =
                    user?.userLocations?.indexOfFirst { it.address == labTestViewModel.selectedAddress.value?.desc?.getSafe() }
                        .getSafe()

            if (index == -1) index = 0

            val location = user?.userLocations?.get(index)

            val locCategory = metaData?.locationCategories?.find { loc ->
                loc.genericItemId == location?.category?.toInt().getSafe()
            }
            var categoryLoc = ""
            if (locCategory != null)
                categoryLoc = locCategory.genericItemName.getSafe()

            if (categoryLoc.uppercase().getSafe().contains("other".uppercase().getSafe()))
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
            labTestViewModel.selectedAddress.postValue(userLocation)
            mBinding.caDeliveryAddress.mBinding.tvAddNew.setVisible(false)
            mBinding.caDeliveryAddress.addButtonEnabled = false
            mBinding.caDeliveryAddress.showSwitchButton = true
        } else {
            labTestViewModel.bookConsultationRequest.userLocationId = null
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
            labTestViewModel.fileList.add(
                MultipleViewItem(
                    title = listTitle,
                    itemId =it.id.toString(),
                    drawable = drawable,
                ).apply {

                    type= it.attachments?.file
                    if(it.attachmentTypeId== Enums.AttachmentType.VOICE.key ) itemCenterIcon=R.drawable.ic_voice_group

                    isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                }
            )
        }

//        itemsAdapter.listItems = labTestViewModel.fileList
        mBinding.caPrescription.listItems = labTestViewModel.fileList
        mBinding.bSendRequest.isEnabled = labTestViewModel.fileList.isNotEmpty()
    }

    private fun createBookingId() {
        val request = PartnerDetailsRequest(
            serviceId = CustomServiceTypeView.ServiceType.LaboratoryService.id,
            partnerUserId = 0,
            bookingId = labTestViewModel.bookingIdResponse.bookingId
        )

        if (isOnline(requireActivity())) {
            labTestViewModel.createBookingId(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PartnerProfileResponse>
                        response.data?.let {
                            if(labTestViewModel.bookingIdResponse.bookingId != it.bookingId)
                                labTestViewModel.flushData(true)

                            labTestViewModel.bookingIdResponse = it
                            labTestViewModel.bookingIdResponse.bookingId?.let { bookingId ->
                                tinydb.putInt(com.fatron.network_module.utils.Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key, bookingId)
                            }
                            labTestViewModel.bookConsultationRequest.bookingId = labTestViewModel.bookingIdResponse.bookingId
                            callGetAttachments()
                            setDataInViews(it)
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
            labTestViewModel.addConsultationAttachment(
                booking_id = labTestViewModel.bookingIdResponse.bookingId.getSafe(),
                mimeType.toString(),
                mediaList
            ).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            callGetAttachments()
                        }
                        is ResponseResult.Pending -> {
                            showLoader()
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
            DeleteAttachmentRequest(item.itemId?.toInt())

        if (isOnline(requireActivity())) {
            labTestViewModel.deleteDocumentApiCall(request = request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<*>
                            try {
                                showToast(response.message.getSafe())
                                labTestViewModel.fileList.remove(item)
//                                itemsAdapter.listItems = labTestViewModel.fileList
                                mBinding.caPrescription.listItems = labTestViewModel.fileList
                                mBinding.bSendRequest.isEnabled = labTestViewModel.fileList.isNotEmpty()
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
            labTestViewModel.callGetAttachments(AppointmentDetailReq(bookingId =  labTestViewModel.bookingIdResponse.bookingId.getSafe().toString()))
                .observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<AttachmentResponse>
                            labTestViewModel.fileList= arrayListOf()
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

    private fun bookConsultation(request: BookConsultationRequest) {
        if (isOnline(requireActivity())) {
            if(labTestViewModel.bookConsultationRequest.fee?.equals("Free", true).getSafe())
                labTestViewModel.bookConsultationRequest.fee = "0"

            labTestViewModel.bookConsultation(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        response.data?.let {
                            labTestViewModel.removeSavedBookingId()
                            showRequestSubmitDialog()
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
                                message =getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
}