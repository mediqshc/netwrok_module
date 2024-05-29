package com.homemedics.app.ui.fragment.pharmacy

import android.app.AlertDialog
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.pharmacy.PharmacyCartRequest
import com.fatron.network_module.models.request.pharmacy.PharmacyOrderRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.pharmacy.OrderDetailsResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogWarningInfoBinding
import com.homemedics.app.databinding.FragmentPharmacyCartCheckoutBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.model.AddressModel
import com.homemedics.app.ui.activity.CheckoutActivity
import com.homemedics.app.ui.adapter.PharmacyProductsListAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.PharmacyViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File

class PharmacyCartCheckoutFragment : BaseFragment(), View.OnClickListener {

    private val pharmacyViewModel: PharmacyViewModel by activityViewModels()

    private lateinit var mBinding: FragmentPharmacyCartCheckoutBinding

    private lateinit var dialogWarningInfoBinding: DialogWarningInfoBinding

    private lateinit var adapter: PharmacyProductsListAdapter

    private lateinit var orderDetailsResponse: OrderDetailsResponse

    private lateinit var builder: AlertDialog

    private lateinit var dialogSaveButton: Button

    private lateinit var fileUtils: FileUtils

    private var file: File? = null

    private var locale: String? = null

    private var bookingId: Int = 0

    private var quantity = 1

    private var price: Double = 0.0

    var handler: Handler = Handler(Looper.getMainLooper())

    var delay: Long = 1000 // 1 seconds after user stops typing

    var lastTextEdit: Long = 0

    private val inputFinishChecker = Runnable {
        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
            lifecycleScope.launch {
                pharmacyQuantityUpdate(quantity, price)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(inputFinishChecker)
    }

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.pharmacyTitle.getSafe()
            cdPatients.hint = lang?.globalString?.patient.getSafe()
            etInstructions.hint = lang?.globalString?.specialInstructions.getSafe()
            caDeliveryAddress.apply {
                title = lang?.labPharmacyScreen?.deliveryAddress.getSafe()
                custDesc = lang?.labPharmacyScreen?.deliveryDesc.getSafe()
            }
            caPrescription.apply {
                title = lang?.labPharmacyScreen?.doYouHaveAPrescription.getSafe()
                custDesc = lang?.labPharmacyScreen?.prescriptionDetails.getSafe()
            }
        }
    }

    override fun init() {
        locale = tinydb.getString(Enums.TinyDBKeys.LOCALE.key)
        bookingId = tinydb.getInt(Enums.TinyDBKeys.BOOKING_ID.key)
        pharmacyViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)

        mBinding.apply {
            val spacing = resources.getDimensionPixelSize(R.dimen.dp16)
            adapter = PharmacyProductsListAdapter().apply {
                langData = ApplicationClass.mGlobalData
            }
            rvMedicineProducts.adapter = adapter
            rvMedicineProducts.addItemDecoration(RecyclerViewItemDecorator(spacing, RecyclerViewItemDecorator.VERTICAL))
        }

        observe()
        setDataInViews(null)
        pharmacyOrderDetailsApi()
        setAddress()
    }

    override fun getFragmentLayout() = R.layout.fragment_pharmacy_cart_checkout

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPharmacyCartCheckoutBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    findNavController().popBackStack()
                }
                onAction2Click = {
                    findNavController().popBackStack()
                }
                onAction3Click = {
                    DialogUtils(requireActivity())
                        .showDoubleButtonsAlertDialog(
                            title = lang?.globalString?.warning.getSafe(),
                            message = lang?.dialogsStrings?.clearCartMsg.getSafe(),
                            positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                            negativeButtonStringText = lang?.globalString?.no.getSafe(),
                            buttonCallback = {
                                pharmaClearCartApi()
                            }
                        )
                }
            }
            adapter.apply {
                onActionPLusClick = { item, pos ->
                    quantity = item.quantity.getSafe()
                    pharmacyViewModel.pharmaProductId = item.productId.getSafe()
                    quantity = quantity.plus(1)
                    adapter.listItems[pos].quantity = quantity
                    price = item.price?.toDouble().getSafe()
                    if (quantity > 0) {
                        lastTextEdit = System.currentTimeMillis()
                        handler.postDelayed(inputFinishChecker, delay)
                    } else {
                        lastTextEdit = 0
                    }
                    actionbar.dotText = quantity.toString()
                    notifyDataSetChanged()
                }
                onActionMinusClick = { item, pos ->
                    quantity = item.quantity.getSafe()
                    pharmacyViewModel.pharmaProductId = item.productId.getSafe()
                    quantity = quantity.minus(1)
                    if(quantity <= 0)
                        quantity = 0

                    adapter.listItems[pos].quantity = quantity
                    price = item.price?.toDouble().getSafe()
                    lastTextEdit = System.currentTimeMillis()
                    handler.postDelayed(inputFinishChecker, delay)
                    actionbar.dotText = quantity.toString()
                    notifyDataSetChanged()
                }
                onWarningClick = {
                    showWarningDialog()
                }
            }
            caPrescription.onAddItemClick = {
                fileUtils.requestPermissions(requireActivity(), mIsChooser = true) { result ->
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
            caPrescription.onDeleteClick = { item, _->
                deleteDocumentApiCall(item)
            }
            caDeliveryAddress.onAddItemClick = {
                findNavController().safeNavigate(PharmacyCartCheckoutFragmentDirections.actionPharmacyCartCheckoutFragmentToSelectAddressNavigation())
            }
            caDeliveryAddress.onEditItemCall = {
                findNavController().safeNavigate(
                    R.id.action_pharmacyCartCheckoutFragment_to_select_address_navigation,
                    bundleOf("fromPharmacy" to true)
                )
            }
            cdPatients.onItemSelectedListener = { _, position: Int ->
                pharmacyViewModel.patientIdSelected = pharmacyViewModel.familyMembers?.get(position)?.id.getSafe()
                cdPatients.selectionIndex = position
            }
            bOrderNow.setOnClickListener(this@PharmacyCartCheckoutFragment)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bOrderNow -> orderNow()
        }
    }

    override fun onDetach() {
        super.onDetach()
        pharmacyViewModel.isShowPrescription.postValue(false)
    }

    private fun showWarningDialog() {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogWarningInfoBinding = DialogWarningInfoBinding.inflate(layoutInflater)
            dialogWarningInfoBinding.lang = mBinding.lang
            setView(dialogWarningInfoBinding.root)
            setPositiveButton(mBinding.lang?.globalString?.ok.getSafe()) { _, _ -> }
        }.create()

        builder.setOnShowListener{
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                pharmacyViewModel.isShowPrescription.postValue(true)
                builder.dismiss()
            }
        }
        builder.show()
    }

    private fun navigateToCheckout() {
        val checkoutIntent = Intent(requireActivity(), CheckoutActivity::class.java)
        checkoutIntent.apply {
            putExtra(
                Constants.BOOKING_ID,
                bookingId
            )
            putExtra(
                com.homemedics.app.utils.Enums.BundleKeys.bookConsultationRequest.key,
                Gson().toJson(pharmacyViewModel.bookConsultationRequest)
            )
        }
        startActivity(checkoutIntent)
//        lifecycleScope.launch{
//            delay(200) //for smooth UI purpose
//            pharmacyViewModel.products?.clear()
//            findNavController().popBackStack(R.id.pharmacySearchFragment, true)
//        }
    }

    private fun orderNow() {
        val prescriptionRequired = orderDetailsResponse.products?.find {
                product -> product.product?.prescriptionRequired.getBoolean()
        }?.product?.prescriptionRequired.getBoolean()

        if (prescriptionRequired && mBinding.caPrescription.listItems.isEmpty()) {
            showWarningDialog()
        } else {
            val bookingID = tinydb.getInt(Enums.TinyDBKeys.BOOKING_ID.key)
            val patientID = if(orderDetailsResponse.familyMembers.isNullOrEmpty().not()) //on shafiq request
                    orderDetailsResponse.familyMembers?.get(mBinding.cdPatients.selectionIndex)?.familyMemberId.getSafe()
                else DataCenter.getUser()?.id
            pharmacyViewModel.bookConsultationRequest.apply {
                serviceId = CustomServiceTypeView.ServiceType.PharmacyService.id
                bookingId = bookingID.getSafe()
                patientId = if (patientID != 0) patientID else pharmacyViewModel.patientIdSelected
                instructions = mBinding.etInstructions.text.toString()
                bookingDate = (System.currentTimeMillis() / 1000).toString()
                prescriptionFlow = 0

                var errorMsg: String? = null

                if (mBinding.caDeliveryAddress.mBinding.switchButton.isChecked
                    || userLocationId == null
                )
                    errorMsg = mBinding.lang?.labPharmacyScreen?.chooseAddressForPharmacy?.getSafe()

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

    private fun setDataInViews(data: OrderDetailsResponse?){
        mBinding.apply {
            data?.let {
                pharmacyViewModel.familyMembers = it.familyMembers
                val patientNames = it.familyMembers?.map { p ->
                    if (p.id == DataCenter.getUser()?.id)
                        p.fullName = lang?.globalString?.self.getSafe()
                    p.fullName
                }
                mBinding.cdPatients.apply {
                    this.data = patientNames as ArrayList<String>? ?: arrayListOf()
                    if (patientNames.isNullOrEmpty().not()) {
                        selectionIndex = 0
                        if (pharmacyViewModel.bookConsultationRequest.patientId != null) {
                            val pos =
                                it.familyMembers?.indexOfFirst { pt -> pt.familyMemberId == pharmacyViewModel.bookConsultationRequest.patientId }
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
            mBinding.caDeliveryAddress.mBinding.tvAddNew.invisible()
            mBinding.caDeliveryAddress.addButtonEnabled = false
        }
    }

    private fun pharmacyOrderDetailsApi() {
        val cityId = if (pharmacyViewModel.cityId != 0) pharmacyViewModel.cityId else null
        val countryId = if (pharmacyViewModel.countryId != 0) pharmacyViewModel.countryId else null
        val bookingId = tinydb.getInt(Enums.TinyDBKeys.BOOKING_ID.key)
        val request = PharmacyOrderRequest(
            bookingId = bookingId,
            countryId = countryId,
            cityId = cityId
        )
        pharmacyViewModel.pharmacyOrderDetails(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<*>
                    (response.data as OrderDetailsResponse).let { pharmacyOrderDetailsResponse ->
                        orderDetailsResponse = pharmacyOrderDetailsResponse
                        setDataInViews(pharmacyOrderDetailsResponse)
                        adapter.listItems = pharmacyOrderDetailsResponse.products.getSafe()
                        mBinding.apply {
                            actionbar.dotText = pharmacyOrderDetailsResponse.productsCount.toString()
                            val currency = metaData?.currencies?.find { it.genericItemId == pharmacyOrderDetailsResponse.booking?.currencyId }?.genericItemName.getSafe()
                            tvTotalAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${pharmacyOrderDetailsResponse.paymentBreakdown?.total.getSafe()} $currency" else "$currency ${pharmacyOrderDetailsResponse.paymentBreakdown?.total.getSafe()}"
                            tvPayAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${pharmacyOrderDetailsResponse.paymentBreakdown?.payableAmount.getSafe()} $currency" else "$currency ${pharmacyOrderDetailsResponse.paymentBreakdown?.payableAmount.getSafe()}"
                        }
                        callGetAttachments()
                        if (adapter.listItems.isEmpty())
                            findNavController().safeNavigate(PharmacyCartCheckoutFragmentDirections.actionPharmacyCartCheckoutFragmentToPharmacyProductDetailsFragment())
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
                else -> { hideLoader() }
            }
        }
    }

    private fun pharmaClearCartApi() {
        val bookingId = tinydb.getInt(Enums.TinyDBKeys.BOOKING_ID.key)
        val request = PharmacyOrderRequest(bookingId = bookingId)
        pharmacyViewModel.pharmaClearCart(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    pharmacyViewModel.products?.clear()
                    tinydb.remove(Enums.TinyDBKeys.BOOKING_ID.key)
                    findNavController().navigate(R.id.action_pharmacyCartCheckoutFragment_to_pharmacySearchFragment)
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
                else -> { hideLoader() }
            }
        }
    }

    private fun pharmacyQuantityUpdate(quantity: Int = 1, price: Double) {
        val bookingId = tinydb.getInt(Enums.TinyDBKeys.BOOKING_ID.key)
        val request = PharmacyCartRequest(
            productId = pharmacyViewModel.pharmaProductId,
            bookingId = if (bookingId != 0) bookingId else null,
            price = price.toString(),
            quantity = quantity
        )
        pharmacyViewModel.pharmacyQuantityUpdate(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    pharmacyOrderDetailsApi()
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
                    hideLoader()
                }
                is ResponseResult.Complete -> {
                    hideLoader()
                }
                else -> { hideLoader() }
            }
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
                title = mBinding.lang?.globalString?.information.getSafe(),
                message = mBinding.lang?.dialogsStrings?.fileSize.getSafe(),
                buttonCallback = {},
            )
    }

    private fun setAttachments(data: List<Attachment>?) {
        pharmacyViewModel.fileList.clear()
        data?.forEach {
            val drawable = when (it.attachmentTypeId) {
                com.homemedics.app.utils.Enums.AttachmentType.DOC.key -> {
                    R.drawable.ic_upload_file
                }
                com.homemedics.app.utils.Enums.AttachmentType.IMAGE.key -> {
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
                    if(it.attachmentTypeId== com.homemedics.app.utils.Enums.AttachmentType.VOICE.key ) itemCenterIcon = R.drawable.ic_voice_group

                    isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                }
            )
        }
        mBinding.caPrescription.listItems = pharmacyViewModel.fileList
    }

    private fun addAttachmentApiCall() {
        val mediaList = ArrayList<MultipartBody.Part>()
        val uri = file?.let { fileUtils.getUriFromFile(context = requireContext(), it) }
        val mimeTypeView = uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }

        val typeImage = if (mimeTypeView?.contains("image").getSafe()) mimeTypeView else ""

        val mimeType = when (mimeTypeView) {
            FileUtils.typeOther,
            FileUtils.typePDF -> {
                com.homemedics.app.utils.Enums.AttachmentType.DOC.key
            }
            typeImage -> {
                com.homemedics.app.utils.Enums.AttachmentType.IMAGE.key
            }
            else -> {
                com.homemedics.app.utils.Enums.AttachmentType.VOICE.key
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
                booking_id = bookingId.getSafe(),
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
            pharmacyViewModel.callGetAttachments(AppointmentDetailReq(bookingId = bookingId.toString()))
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

    private fun bookConsultation(request: BookConsultationRequest) {
        if (isOnline(requireActivity())) {
            pharmacyViewModel.bookConsultation(request).observe(this) {
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
        }
        else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}