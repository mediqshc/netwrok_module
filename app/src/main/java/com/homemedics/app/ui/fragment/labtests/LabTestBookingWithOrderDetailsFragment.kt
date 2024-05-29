package com.homemedics.app.ui.fragment.labtests

import android.content.Intent
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.request.labtest.LabTestCartRequest
import com.fatron.network_module.models.request.labtest.LabTestHomeCollectionRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.pharmacy.OrderDetailsResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentLabTestBookingWithOrderDetailsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.model.AddressModel
import com.homemedics.app.ui.activity.CheckoutActivity
import com.homemedics.app.ui.adapter.CheckoutSplitAmountAdapter
import com.homemedics.app.ui.adapter.LabTestCartListAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.LabTestViewModel
import okhttp3.MultipartBody
import java.io.File

class LabTestBookingWithOrderDetailsFragment : BaseFragment(), View.OnClickListener {

    private val labTestViewModel: LabTestViewModel by activityViewModels()
    private lateinit var mBinding: FragmentLabTestBookingWithOrderDetailsBinding

    private var file: File? = null
    private lateinit var fileUtils: FileUtils
    private lateinit var cartAdapter: LabTestCartListAdapter
    private var isToggleOn = false

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.labTest.getSafe()
            cdPatients.hint = lang?.globalString?.patient.getSafe()
            etInstructions.hint = lang?.globalString?.specialInstructions.getSafe()
            caDeliveryAddress.apply {
                title = lang?.labPharmacyScreen?.isSampleCollectionRequired.getSafe()
                custDesc = lang?.labPharmacyScreen?.chooseSampleAddress.getSafe()
            }
            caPrescription.apply {
                title = lang?.labPharmacyScreen?.doYouHaveAPrescription.getSafe()
                custDesc = lang?.labPharmacyScreen?.prescriptionDescLab.getSafe()
            }
        }
    }

    override fun init() {
        labTestViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)
        cartAdapter = LabTestCartListAdapter()

        // Added item decoration and remove from setCartItemViews method, to resolve spacing glitch
        val spacing = resources.getDimensionPixelSize(R.dimen.dp16)
        mBinding.rvMedicineProducts.addItemDecoration(RecyclerViewItemDecorator(spacing, RecyclerViewItemDecorator.VERTICAL))

        observe()
        setDataInViews(null)
        createBookingId()
        getCartDetailsApi()
        setAddress()
    }

    override fun getFragmentLayout() = R.layout.fragment_lab_test_booking_with_order_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentLabTestBookingWithOrderDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction3Click = {
                val ids = labTestViewModel.orderDetailsResponse.labCartItems?.map { it.id }
                ids?.let {
                    DialogUtils(requireActivity())
                        .showDoubleButtonsAlertDialog(
                            title = mBinding.lang?.globalString?.warning.getSafe(),
                            message = mBinding.lang?.dialogsStrings?.clearCartMsg.getSafe(),
                            positiveButtonStringText = mBinding.lang?.globalString?.yes.getSafe(),
                            negativeButtonStringText = mBinding.lang?.globalString?.no.getSafe(),
                            buttonCallback = {
                                cartItemDeleteApi(null)
                            }
                        )
                }
            }

            bBookNow.setOnClickListener(this@LabTestBookingWithOrderDetailsFragment)

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

            caDeliveryAddress.onEditItemCall = {
                findNavController().safeNavigate(
                    R.id.action_labTestBookingWithOrderDetailsFragment_to_select_address_navigation,
                    bundleOf("fromLab" to true)
                )
            }
            caDeliveryAddress.onAddItemClick = {
                findNavController().safeNavigate(LabTestBookingWithOrderDetailsFragmentDirections.actionLabTestBookingWithOrderDetailsFragmentToSelectAddressNavigation())
            }
            caDeliveryAddress.mBinding.switchButton.apply {
                setOnClickListener {
                    labTestViewModel.bookConsultationRequest.homeCollection = isChecked.getInt()
                    caDeliveryAddress.isEnabled = isChecked
                    isToggleOn = isChecked

                    if (caDeliveryAddress.listItems.isEmpty()) {
                        caDeliveryAddress.showAddButton = isChecked
                    } else if (caDeliveryAddress.listItems.isNotEmpty()) {
                        caDeliveryAddress.showAddButton = false
                    }

                    val branchId = labTestViewModel.orderDetailsResponse.labCartItems?.first()?.branch?.id.getSafe()

                    val request = LabTestHomeCollectionRequest(
                        bookingId = labTestViewModel.bookingIdResponse.bookingId,
                        homeCollection = isChecked.getInt(),
                        branchId = branchId
                    )
                    updateHomeCollection(request)
                }
            }

            cartAdapter.onDeleteClick = { item, pos ->

                val request = LabTestCartRequest(
                    item_id = arrayListOf(item.id.getSafe())
                )

                cartItemDeleteApi(request)
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
            R.id.bBookNow -> {
                labTestViewModel.bookConsultationRequest.apply {
                    homeCollection = if (isToggleOn.not()) 0 else 1
                    serviceId = CustomServiceTypeView.ServiceType.LaboratoryService.id

                    patientId = labTestViewModel.bookingIdResponse.patients?.get(mBinding.cdPatients.selectionIndex)?.familyMemberId
                    instructions = mBinding.etInstructions.text.toString()
                    bookingDate = (System.currentTimeMillis()/1000).toString()
                    prescriptionFlow = 0
                    fee = labTestViewModel.orderDetailsResponse.paymentBreakdown?.payableAmount

                    var errorMsg: String? = null

                    if (mBinding.caDeliveryAddress.mBinding.switchButton.isChecked
                        && userLocationId == null
                    )
                        errorMsg = mBinding.lang?.labPharmacyScreen?.chooseAddressForLab.getSafe()

                    if(preferredLaboratory == null)
                        errorMsg = mBinding.lang?.labPharmacyScreen?.selectPreferredLab.getSafe()

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
            positiveButtonText = R.string.permit_manual,
            negativeButtonText = R.string.close,
            buttonCallback = {
                context?.let { gotoAppSettings(it) }
            },
            cancellable = false
        )
    }

    private fun showRequestSubmitDialog() {
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = mBinding.lang?.globalString?.requestSubmitted.getSafe(),
                message = mBinding.lang?.labPharmacyScreen?.requestInfo.getSafe(),
                buttonCallback = {
                    findNavController().popBackStack(R.id.labTestFragment, true)
                },
            )
    }

    private fun setDataInViews(data: PartnerProfileResponse?){
        mBinding.apply {
            labTestViewModel.let { vm ->
                caDeliveryAddress.mBinding.switchButton.isChecked = vm.bookConsultationRequest.homeCollection.getBoolean()
                caDeliveryAddress.isEnabled = vm.bookConsultationRequest.homeCollection.getBoolean()
            }

            data?.let {
                val patientNames = it.patients?.map { p ->
                    if (p.id == DataCenter.getUser()?.id)
                        p.fullName = lang?.globalString?.self.getSafe()
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

    private fun setCartDataInViews(orderDetailsResponse: OrderDetailsResponse){
        mBinding.apply {
            actionbar.dotText = labTestViewModel.orderDetailsResponse.labCartItems?.size.getSafe().toString()

            iSelectedLab.apply {
                if(orderDetailsResponse.labCartItems.isNullOrEmpty().not()){
                    val item = orderDetailsResponse.labCartItems?.first()

                    labTestViewModel.bookConsultationRequest.preferredLaboratory = item?.branch?.id

                    tvTitle.text = item?.branch?.name
                    tvDesc.text = item?.branch?.streetAddress
                    ivIcon.loadImage(item?.lab?.iconUrl, R.drawable.ic_launcher_foreground)
                    ivThumbnail.gone()
                }
            }

            val paymentBreakdown = orderDetailsResponse.paymentBreakdown

            val orderDetails = arrayListOf(
                SplitAmount(specialityName = lang?.labPharmacyScreen?.labDiscount.getSafe(), fee = paymentBreakdown?.labDiscount.getSafe().toString()),
                SplitAmount(specialityName = lang?.globalString?.total.getSafe(), fee = paymentBreakdown?.totalFee.getSafe()),
                SplitAmount(specialityName = lang?.labPharmacyScreen?.sampleCollectionCharges.getSafe(), fee = paymentBreakdown?.sampleCharges.getSafe().toString()),
            )


            if(orderDetailsResponse.labCartItems.isNullOrEmpty()) {
                if(findNavController().popBackStack(R.id.selectMainLabFragment, true).not())
                    findNavController().popBackStack()
            }

            val currency = metaData?.currencies?.find { it.genericItemId == paymentBreakdown?.currencyId }?.genericItemName.getSafe()

            cartAdapter.currency = currency
            rvMedicineProducts.adapter = cartAdapter
            cartAdapter.listItems = orderDetailsResponse.labCartItems.getSafe()

            // Split amount recyclerview
            val splitAmountAdapter = CheckoutSplitAmountAdapter()
            splitAmountAdapter.currency = currency
            rvSplitAmount.adapter = splitAmountAdapter
            splitAmountAdapter.listItems = orderDetails
            // implement item decoration for top margin

            val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
            tvPayAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${orderDetailsResponse.paymentBreakdown?.payableAmount} ${splitAmountAdapter.currency}" else "${splitAmountAdapter.currency} ${orderDetailsResponse.paymentBreakdown?.payableAmount}"
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
    }

    private fun navigateToCheckout() {
        val checkoutIntent = Intent(requireActivity(), CheckoutActivity::class.java)
        checkoutIntent.apply {
            putExtra(
                Constants.BOOKING_ID,
                labTestViewModel.bookingIdResponse.bookingId
            )
            putExtra(
                com.homemedics.app.utils.Enums.BundleKeys.bookConsultationRequest.key,
                Gson().toJson(labTestViewModel.bookConsultationRequest)
            )
        }
        startActivity(checkoutIntent)
//        lifecycleScope.launch{
//            delay(200) //for smooth UI purpose
//            findNavController().popBackStack(R.id.labTestFragment, true)
//        }
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
                            labTestViewModel.bookingIdResponse = it
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
                                labTestViewModel.fileList.remove(item)
//                                itemsAdapter.listItems = labTestViewModel.fileList
                                mBinding.caPrescription.listItems = labTestViewModel.fileList
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
                            navigateToCheckout()
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
//                                    findNavController().popBackStack()
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

    private fun getCartDetailsApi(){
        val request = LabTestCartRequest(
            bookingId = labTestViewModel.bookingIdResponse.bookingId
        )

        if (isOnline(requireActivity())) {
            labTestViewModel.getCartDetails(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<OrderDetailsResponse>

                        response.data?.let { details ->
                            labTestViewModel.orderDetailsResponse = details
                            setCartDataInViews(details)
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message =it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
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

    private fun cartItemDeleteApi(request: LabTestCartRequest? = null){

        if (isOnline(requireActivity())) {
            labTestViewModel.labTestDeleteToCart(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        getCartDetailsApi()
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message =it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
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

    private fun updateHomeCollection(request: LabTestHomeCollectionRequest){

        if (isOnline(requireActivity())) {
            labTestViewModel.updateHomeCollection(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<OrderDetailsResponse>

                        response.data?.let { details ->
                            labTestViewModel.orderDetailsResponse = details
                            setCartDataInViews(details)
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message =it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
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