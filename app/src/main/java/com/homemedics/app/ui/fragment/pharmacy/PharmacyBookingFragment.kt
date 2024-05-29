package com.homemedics.app.ui.fragment.pharmacy

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
import com.homemedics.app.databinding.FragmentPharmacyBookingBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.model.AddressModel
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.PharmacyViewModel
import okhttp3.MultipartBody
import java.io.File

class PharmacyBookingFragment : BaseFragment(), View.OnClickListener {

    private val pharmacyViewModel: PharmacyViewModel by activityViewModels()

    private lateinit var mBinding: FragmentPharmacyBookingBinding

    private lateinit var fileUtils: FileUtils

    private var file: File? = null

    private var patientID: Int = 0

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.pharmacyTitle.getSafe()
            caPrescription.apply {
                title = lang?.labPharmacyScreen?.doYouHaveAPrescription.getSafe()
                custDesc = lang?.labPharmacyScreen?.uploadPrescriptionDesc.getSafe()
            }
            cdPatients.hint = lang?.globalString?.patient.getSafe()
            etInstructions.hint = lang?.globalString?.specialInstructions.getSafe()
            caDeliveryAddress.apply {
                title = lang?.labPharmacyScreen?.deliveryAddress.getSafe()
                custDesc = lang?.labPharmacyScreen?.deliveryDesc.getSafe()
            }
        }
        // default city and country if user not logged in.
        if (isUserLoggedIn().not())
            defaultCityAndCountry()
    }

    override fun init() {
        pharmacyViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)

        observe()
        setDataInViews(null)
        setActionbarCity()
        createBookingId()
        setAddress()
    }

    override fun onResume() {
        super.onResume()
        callGetAttachments()
    }

    override fun getFragmentLayout() = R.layout.fragment_pharmacy_booking

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPharmacyBookingBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            bSendRequest.setOnClickListener(this@PharmacyBookingFragment)
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
            caDeliveryAddress.onAddItemClick = {
                findNavController().safeNavigate(PharmacyBookingFragmentDirections.actionPharmacyBookingFragmentToSelectAddressNavigation())
            }
            caDeliveryAddress.onEditItemCall = {
                findNavController().safeNavigate(PharmacyBookingFragmentDirections.actionPharmacyBookingFragmentToSelectAddressNavigation())
            }
            cdPatients.onItemSelectedListener = { _, position: Int ->
                pharmacyViewModel.patientIdSelected = pharmacyViewModel.patients?.get(position)?.id.getSafe()
                cdPatients.selectionIndex = position
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bSendRequest -> {
                val patientID = pharmacyViewModel.patients?.get(mBinding.cdPatients.selectionIndex)?.familyMemberId.getSafe()
                pharmacyViewModel.bookConsultationRequest.apply {
                    serviceId = CustomServiceTypeView.ServiceType.PharmacyService.id
                    bookingId = pharmacyViewModel.prescriptionBookingId
                    patientId = if (patientID != 0) patientID else pharmacyViewModel.patientIdSelected
                    instructions = mBinding.etInstructions.text.toString()
                    bookingDate = (System.currentTimeMillis() / 1000).toString()
                    prescriptionFlow = 1

                    var errorMsg: String? = null

                    if (mBinding.caDeliveryAddress.mBinding.switchButton.isChecked
                        && userLocationId == null
                        || mBinding.caDeliveryAddress.listItems.isEmpty()
                    )
                        errorMsg = mBinding.lang?.labPharmacyScreen?.chooseAddressForPharmacy.getSafe()

                    if(mBinding.caPrescription.listItems.isNullOrEmpty())
                        errorMsg = mBinding.lang?.labPharmacyScreen?.uploadPrescriptionDesc.getSafe()

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
        pharmacyViewModel.selectedAddress.observe(this) {
            it?.let {
                mBinding.caDeliveryAddress.apply {
                    it.itemEndIcon = R.drawable.ic_edit

                    pharmacyViewModel.bookConsultationRequest.userLocationId =
                        (it as AddressModel?)?.id.getSafe()
                    listItems = arrayListOf(it)
                }
            }
        }
    }

    private fun defaultCityAndCountry() {
        val city = getCityList(metaData?.defaultCountryId.getSafe())?.find { city -> city.id == metaData?.defaultCityId.getSafe() }
        val country = metaData?.countries?.find { it.id == metaData?.defaultCountryId.getSafe() }
        pharmacyViewModel.country = country?.name.getSafe()
        pharmacyViewModel.city = city?.name.getSafe()
        mBinding.actionbar.desc = city?.name.getSafe()
    }

    private fun setActionbarCity() {
        if(pharmacyViewModel.city.getSafe().isEmpty()) {
            val user = DataCenter.getUser()
            user?.let {
                val city = getCityList(it.countryId.getSafe())?.find { city -> city.id == it.cityId }
                mBinding.actionbar.desc = city?.name.getSafe()
                pharmacyViewModel.cityId = city?.id.getSafe()
                pharmacyViewModel.city = city?.name.getSafe()

                val countryIndex = metaData?.countries?.indexOfFirst { country -> country.id == it.countryId.getSafe() }.getSafe()
                if (countryIndex != -1) {
                    val country = metaData?.countries?.get(countryIndex)
                    pharmacyViewModel.countryId = country?.id.getSafe()
                    pharmacyViewModel.country = country?.name.getSafe()
                }
            }
        } else {
            mBinding.actionbar.desc = pharmacyViewModel.city.getSafe()
        }
    }

    private fun setDataInViews(data: PartnerProfileResponse?){
        mBinding.apply {
            data?.let {
                pharmacyViewModel.patients = it.patients
                val patientNames = it.patients?.map { p ->
                    if (p.id == DataCenter.getUser()?.id)
                        p.fullName = mBinding.lang?.globalString?.self.getSafe()
                    p.fullName
                }
                mBinding.cdPatients.apply {
                    this.data = patientNames as ArrayList<String>? ?: arrayListOf()
                    if (patientNames.isNullOrEmpty().not()) {
                        selectionIndex = 0
                        if (pharmacyViewModel.bookConsultationRequest.patientId != null) {
                            val pos =
                                it.patients?.indexOfFirst { pt -> pt.familyMemberId == pharmacyViewModel.bookConsultationRequest.patientId }
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
            if (pharmacyViewModel.selectedAddress.value != null)
                index =
                    user?.userLocations?.indexOfFirst { it.address == pharmacyViewModel.selectedAddress.value?.desc?.getSafe() }
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
            pharmacyViewModel.selectedAddress.postValue(userLocation)
            mBinding.caDeliveryAddress.mBinding.tvAddNew.setVisible(false)
            mBinding.caDeliveryAddress.addButtonEnabled = false
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
                    findNavController().safeNavigate(R.id.action_pharmacyBookingFragment_to_ordersDetailFragment,
                        bundleOf(Constants.BOOKINGID to pharmacyViewModel.bookConsultationRequest.bookingId)
                    )
                },
                negativeButtonCallback = {
                    tinydb.remove(com.fatron.network_module.utils.Enums.TinyDBKeys.BOOKING_ID.key)
                    pharmacyViewModel.products?.clear()
                    findNavController().navigate(R.id.action_pharmacyBookingFragment_to_pharmacySearchFragment)
                },
                cancellable = false
            )
    }

    private fun setAttachments(data: List<Attachment>?) {
        pharmacyViewModel.fileList.clear()
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
            val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
            pharmacyViewModel.fileList.add(
                MultipleViewItem(
                    title = listTitle,
                    itemId =it.id.toString(),
                    drawable = drawable,
                ).apply {
                    type = it.attachments?.file
                    if(it.attachmentTypeId== Enums.AttachmentType.VOICE.key ) itemCenterIcon = R.drawable.ic_voice_group

                    isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                }
            )
        }
        mBinding.caPrescription.listItems = pharmacyViewModel.fileList
        mBinding.bSendRequest.isEnabled = pharmacyViewModel.fileList.isNotEmpty()
    }

    private fun addAttachmentApiCall() {
        val mediaList = ArrayList<MultipartBody.Part>()
        val uri = file?.let { fileUtils.getUriFromFile(context = requireContext(), it) }
        val mimeTypeView = uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }

        val typeImage = if (mimeTypeView?.contains("image").getSafe()) mimeTypeView else ""

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
            pharmacyViewModel.addConsultationAttachment(
                booking_id = pharmacyViewModel.bookingIdResponse.bookingId.getSafe(),
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

    private fun callGetAttachments() {
        if (isOnline(requireActivity())) {
            pharmacyViewModel.callGetAttachments(AppointmentDetailReq(bookingId = pharmacyViewModel.bookingIdResponse.bookingId.getSafe().toString()))
                .observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<AttachmentResponse>
                            pharmacyViewModel.fileList= arrayListOf()
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

    private fun deleteDocumentApiCall(item: MultipleViewItem) {
        val request =
            DeleteAttachmentRequest(item.itemId?.toInt())

        if (isOnline(requireActivity())) {
            pharmacyViewModel.deleteDocumentApiCall(request = request).observe(this) {
                if (isOnline(requireActivity())) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            try {
                                pharmacyViewModel.fileList.remove(item)
                                mBinding.caPrescription.listItems = pharmacyViewModel.fileList
                                mBinding.bSendRequest.isEnabled = pharmacyViewModel.fileList.isNotEmpty()
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

    private fun createBookingId() {
        val request = PartnerDetailsRequest(
            serviceId = CustomServiceTypeView.ServiceType.PharmacyService.id,
            partnerUserId = 0,
            bookingId = pharmacyViewModel.prescriptionBookingId
        )

        if (isOnline(requireActivity())) {
            pharmacyViewModel.createBookingId(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PartnerProfileResponse>
                        response.data?.let {
                            pharmacyViewModel.prescriptionBookingId = it.bookingId.getSafe()
                            pharmacyViewModel.bookingIdResponse = it
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

    private fun bookConsultation(request: BookConsultationRequest) {
        if (isOnline(requireActivity())) {
            pharmacyViewModel.bookConsultation(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        response.data?.let {
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
}