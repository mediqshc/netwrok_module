package com.homemedics.app.ui.fragment.doctorconsultation

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.request.emr.AttachEMRtoBDCRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.bdc.PartnerSlotsResponse
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.partnerprofile.DateSlotResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.partnerprofile.PatientResponse
import com.fatron.network_module.models.response.partnerprofile.TimeSlot
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentBookConsultationDetailsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.model.AddressModel
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.ui.activity.CheckoutActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.adapter.BDCTimeSlotsAdapter
import com.homemedics.app.ui.adapter.DateSlotsAdapter
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import com.homemedics.app.viewmodel.EMRViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class BookConsultationDetailsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentBookConsultationDetailsBinding
    private var itemsAdapter = AddMultipleViewAdapter()
    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()
    private val emrViewModel: EMRViewModel by activityViewModels()


    private lateinit var dateSlotsAdapter: DateSlotsAdapter
    private lateinit var audio: CustomAudio

   lateinit var absolutepath:String
    private var file: File? = null
    private lateinit var fileUtils: FileUtils
    private var elapsedMillis = 0L
    private lateinit var animBlink: Animation
    private var locale: String? = null
    private var isAvailableNowFlow = false

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.bookingScreen?.bookConsult.getSafe()
            cdPatients.hint = langData?.globalString?.patient.getSafe()
            etInstructions.hint = langData?.globalString?.specialInstructions.getSafe()
            caAddresses.title = langData?.globalString?.visitAddress.getSafe()
            caMedicalRecords.title = langData?.globalString?.medicalRecords.getSafe()
        }
    }

    override fun onPause() {
        super.onPause()
        saveData()

        audio.onPause()
    }


    private fun saveData() {
        doctorConsultationViewModel.bookConsultationRequest.apply {
            bookingDate = dateSlotsAdapter.getSelectedItem()?.timestamp
//            startTime = fsdf
//            endTime = fdasfas
            userLocationId =
                (doctorConsultationViewModel.selectedAddress.value as AddressModel?)?.id?.toInt()
            instructions = mBinding.etInstructions.text.toString()

            if (mBinding.cdPatients.selectionIndex != -1 && mBinding.partner?.patients?.isEmpty()
                    ?.not().getSafe()
            )
                patientId =
                    mBinding.partner?.patients?.get(mBinding.cdPatients.selectionIndex)?.familyMemberId
        }
    }

    private fun restoreData() {
        doctorConsultationViewModel.bookConsultationRequest.apply {
            mBinding.etInstructions.setText(this.instructions.getSafe())

            //select patient
            //set slot
        }
    }

    override fun init() {
        audio = CustomAudio(requireContext())
        locale =
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        doctorConsultationViewModel.fileList = arrayListOf()
        observe()
        fileUtils = FileUtils()
        fileUtils.init(this)
        animBlink = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.blink
        )


        setAddress()
        itemsAdapter.onDeleteClick = { item, position ->
            mBinding.langData?.apply {
                DialogUtils(requireActivity())
                    .showDoubleButtonsAlertDialog(
                        title = dialogsStrings?.confirmDelete.getSafe(),
                        message = dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = globalString?.yes.getSafe(),
                        negativeButtonStringText = globalString?.no.getSafe(),
                        buttonCallback = {
                            deleteDocumentApiCall(item)

                        },
                    )
            }
        }
        mBinding.rvAttachments.adapter = itemsAdapter
        mBinding.serviceTypeView.setServiceType(doctorConsultationViewModel.bdcFilterRequest.serviceId.getSafe())
        setupDateSlotsList()
        //for time slots items spacing
        val spacing = resources.getDimensionPixelSize(R.dimen.dp6)
        mBinding.rvSlots.addItemDecoration(GridItemDecorationForDiffSpanCount(spacing, 4, false))

        setActionbarCity()
        getPartnerDetails()
    }

    private fun setAddress() {
        val user = DataCenter.getUser()
        if (user?.userLocations?.isNotEmpty().getSafe()) {
            var index = 0
            if (doctorConsultationViewModel.selectedAddress.value != null)
                index =
                    user?.userLocations?.indexOfFirst { it.address == doctorConsultationViewModel.selectedAddress.value?.desc?.getSafe() }
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
                drawable = R.drawable.ic_location_pin_black

            }
            doctorConsultationViewModel.selectedAddress.postValue(userLocation)
            mBinding.caAddresses.mBinding.tvAddNew.invisible() //should not be gone, UI issue will appear
        }
    }

    private fun setActionbarCity() {
        //action bar city name
        if (doctorConsultationViewModel.bdcFilterRequest.cityName.isEmpty()) {
            val user = DataCenter.getUser()
            user?.let {
                val city =
                    getCityList(user.countryId.getSafe())?.find { city -> city.id == user.cityId }
                mBinding.actionbar.desc = city?.name.getSafe()
            }
        } else mBinding.actionbar.desc = doctorConsultationViewModel.bdcFilterRequest.cityName
    }

    override fun getFragmentLayout() = R.layout.fragment_book_consultation_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentBookConsultationDetailsBinding
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
            itemsAdapter.onEditItemCall
            itemsAdapter.onEditItemCall = {
                if (it.drawable == R.drawable.ic_play_arrow)
                    showVoiceNoteDialog(true)
            }
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            tvVoiceNote.setOnClickListener {
                var hasRecording = false
                val fileList = doctorConsultationViewModel.fileList
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
                    } }
                }
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
            tvDesc.setOnClickListener {
                findNavController().safeNavigate(BookConsultationDetailsFragmentDirections.actionBookConsultationDetailsFragmentToAboutDoctorFragment())
            }
            tvQualification.setOnClickListener {
                findNavController().safeNavigate(BookConsultationDetailsFragmentDirections.actionBookConsultationDetailsFragmentToDocEducationFragment())
            }
            iDoctor.tvReview.setOnClickListener {
                findNavController().safeNavigate(BookConsultationDetailsFragmentDirections.actionBookConsultationDetailsFragmentToDocReviewsFragment())
            }
            caMedicalRecords.onAddItemClick = {
                val patient = partner?.patients?.get(cdPatients.selectionIndex)
                emrViewModel.consultationFilterRequest.customerId = patient?.familyMemberId
                emrViewModel.selectedFamily = FamilyConnection(
                    id = patient?.familyMemberId,
                    userId = patient?.familyMemberId,
                    familyMemberId = patient?.familyMemberId,
                    fullName = patient?.fullName,
                )
                findNavController().safeNavigate(
                    BookConsultationDetailsFragmentDirections.actionBookConsultationDetailsFragmentToPatientEmrNavigation(
                        bookingId = doctorConsultationViewModel.partnerProfileResponse.bookingId.getSafe()
                    )
                )
            }

            serviceTypeView.onItemSelected = { item ->
                caAddresses.setVisible(item == CustomServiceTypeView.ServiceType.HomeVisit)
                divider1.setVisible(item == CustomServiceTypeView.ServiceType.HomeVisit)
                doctorConsultationViewModel.bdcFilterRequest.serviceId = item.id
                doctorConsultationViewModel.bookConsultationRequest.serviceId = item.id

                tvServiceDesc.text =
                    metaData?.doctorServices?.find { it.genericItemId == item.id }?.description
                bBookConsultation.text =
                    if (item == CustomServiceTypeView.ServiceType.HomeVisit)
                        langData?.bookingScreen?.requestAppointment.getSafe()
                    else
                        langData?.globalString?.bookNow.getSafe()

                if (item == CustomServiceTypeView.ServiceType.Message) {
                    setMessageConsultationDataInViews()
                } else {
                    hideMessageIrrelevantViews(false)
                }

                getSlots()
            }

            bBookConsultation.setOnClickListener {
                val specialId = doctorConsultationViewModel.bdcFilterRequest.specialityId
                doctorConsultationViewModel.bookConsultationRequest.apply {
                    if (partner?.patients?.isEmpty()?.not().getSafe()) {
                        patientId =
                            partner?.patients?.get(cdPatients.selectionIndex)?.familyMemberId
                        instructions = etInstructions.text.toString()
                        fee = fee.getCommaRemoved()
                        specialityId = specialId

                        var errorMsg: String? = null
                        if (bookingDate.isNullOrEmpty() && serviceId != CustomServiceTypeView.ServiceType.Message.id)
                            errorMsg = langData?.dialogsStrings?.selectBookingDate
                        else if (startTime.isNullOrEmpty() && serviceId != CustomServiceTypeView.ServiceType.Message.id)
                            errorMsg = langData?.dialogsStrings?.selectSlot

                        if (doctorConsultationViewModel.bdcFilterRequest.serviceId == CustomServiceTypeView.ServiceType.HomeVisit.id
                            && userLocationId == null
                        )
                            errorMsg = langData?.dialogsStrings?.selectOneAddress

                        if (errorMsg != null) {
                            DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = errorMsg
                            )
                            return@setOnClickListener
                        }

                        bookConsultation(this)
                    } else {
                        startActivity(Intent(requireActivity(), AuthActivity::class.java))
                    }
                }
            }
            cdPatients.onItemSelectedListener = { item, index ->
                val prevUser =
                    mBinding.partner?.patients?.find { it.familyMemberId == doctorConsultationViewModel.bookConsultationRequest.patientId }
                val selectedUser = mBinding.partner?.patients?.get(index)


                if (prevUser != null && (prevUser.familyMemberId != selectedUser?.familyMemberId.getSafe()) && caMedicalRecords.listItems.isNotEmpty()) {
                    emrWillDeleteDialog(prevUser, selectedUser)
                } else {
                    doctorConsultationViewModel.bookConsultationRequest.patientId =
                        selectedUser?.familyMemberId.getSafe()

                    cdPatients.selectionIndex = index
                }

            }
            caAddresses.onEditItemCall = {
                findNavController().safeNavigate(
                    R.id.action_bookConsultationDetailsFragment_to_selectAddressFragment,
                    bundleOf("fromBDC" to true)
                )
            }
            caAddresses.onAddItemClick = {
                findNavController().safeNavigate(BookConsultationDetailsFragmentDirections.actionBookConsultationDetailsFragmentToSelectAddressFragment())
            }
            caMedicalRecords.onDeleteClick = { item, pos ->
                detachEMRtoBDC(item, pos)
            }
        }

        restoreData()
    }

    private fun hideMessageIrrelevantViews(isMessageService: Boolean) {
        mBinding.apply {
            textView.setVisible(isMessageService.not())
            rvDateSlots.setVisible(isMessageService.not())
            tvTimingText.setVisible(isMessageService.not())
            rvSlots.setVisible(isMessageService.not())
            tvNoData.setVisible(isMessageService.not())
            divider2.setVisible(isMessageService.not())
        }
    }

    private fun navigateToCheckout() {
        val checkoutIntent = Intent(requireActivity(), CheckoutActivity::class.java)
        checkoutIntent.apply {
            putExtra(
                Enums.BundleKeys.partnerProfileResponse.key,
                Gson().toJson(doctorConsultationViewModel.partnerProfileResponse)
            )
            putExtra(
                Enums.BundleKeys.partnerSlotsResponse.key,
                Gson().toJson(doctorConsultationViewModel.partnerSlotsResponse)
            )
            putExtra(
                Enums.BundleKeys.bookConsultationRequest.key,
                Gson().toJson(doctorConsultationViewModel.bookConsultationRequest)
            )
            putExtra("fromBDC", true)
        }

        startActivity(checkoutIntent)
//        lifecycleScope.launch {
//            delay(200) //for smooth UI purpose
//            findNavController().popBackStack(R.id.bookConsultation, true)
//        }
    }

    private fun emrWillDeleteDialog(prevUser: PatientResponse?, newUser: PatientResponse?) {
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            title = mBinding.langData?.globalString?.warning.getSafe(),
            message = mBinding.langData?.dialogsStrings?.medicalRecordDelete?.replace(
                "[0]",
                prevUser?.fullName.getSafe()
            ).getSafe(),
            positiveButtonStringText = mBinding.langData?.globalString?.btnContinue.getSafe(),
            negativeButtonStringText = mBinding.langData?.globalString?.goBack.getSafe(),
            buttonCallback = {
                mBinding.caMedicalRecords.listItems.forEachIndexed { index, item ->
                    detachEMRtoBDC(item, index)
                }

                doctorConsultationViewModel.bookConsultationRequest.patientId =
                    newUser?.familyMemberId.getSafe()

                val newIndex =
                    mBinding.partner?.patients?.indexOfFirst { it.familyMemberId == newUser?.familyMemberId }
                        .getSafe()
                mBinding.cdPatients.selectionIndex = newIndex

            },
            negativeButtonCallback = {
                doctorConsultationViewModel.bookConsultationRequest.patientId =
                    prevUser?.familyMemberId.getSafe()

                val prevIndex =
                    mBinding.partner?.patients?.indexOfFirst { it.familyMemberId == prevUser?.familyMemberId }
                        .getSafe()
                mBinding.cdPatients.selectionIndex = prevIndex
            }
        )

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
        itemsAdapter.listItems = doctorConsultationViewModel.fileList
        lifecycleScope.launch {
            delay(100)
            mBinding.tvVoiceNote.isClickable = true
        }
    }

    private fun updateTimeSlots(selectedDateIndex: Int) {
        val isCallService =
            doctorConsultationViewModel.bdcFilterRequest.serviceId == CustomServiceTypeView.ServiceType.VideoCall.id
        isAvailableNowFlow =
            doctorConsultationViewModel.partnerProfileResponse.isOnline.getBoolean() && isCallService
        var isTodaySelected = false
        if (doctorConsultationViewModel.bookConsultationRequest.bookingDate.isNullOrEmpty().not()) {
            val longTime = doctorConsultationViewModel.bookConsultationRequest.bookingDate?.toLong()
                ?.times(1000)

            val today = Calendar.getInstance()
            val stringToday = SimpleDateFormat("yyyy-MM-dd").format(today.time)
            val bookingDay = SimpleDateFormat("yyyy-MM-dd").format(longTime)

            isTodaySelected = stringToday == bookingDay
        }

        val timeSlotsAdapter = BDCTimeSlotsAdapter(
            CustomServiceTypeView.ServiceType.getServiceById(doctorConsultationViewModel.bdcFilterRequest.serviceId.getSafe())
        )

        mBinding.apply {
            rvSlots.adapter = timeSlotsAdapter
            val gridLayoutManager = GridLayoutManager(requireContext(), 4)
            gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0 && isAvailableNowFlow && isTodaySelected)
                        2
                    else
                        1
                }
            }
            rvSlots.layoutManager = gridLayoutManager
        }

        doctorConsultationViewModel.apply {
            timeSlotsAdapter.onTimeSlotSelected = { item, position ->
                if (item.isAvailableNowSlot) { //available now
                    //no need for time in available now slot
                    bookConsultationRequest.bookAvailableNow = 1
                    bookConsultationRequest.startTime = item.start
                    bookConsultationRequest.endTime = item.start
                    bookConsultationRequest.shiftId = item.shiftId
                } else {
                    bookConsultationRequest.bookAvailableNow = 0
                    bookConsultationRequest.startTime = item.start
                    bookConsultationRequest.endTime = item.end
                    bookConsultationRequest.shiftId = item.shiftId
                }
            }

            val combinedList = ArrayList<TimeSlot>()

            //available now slot adding
            if (isAvailableNowFlow && isTodaySelected) {
                val availableNowSlot =
                    TimeSlot(start = mBinding.langData?.bookingScreen?.availableNow).apply {
                        isAvailableNowSlot = true
                    }
                combinedList.add(0, availableNowSlot)
            }

            try {
                partnerSlotsResponse.dateSlots
                    ?.get(selectedDateIndex)?.slots?.forEach {
                        it?.forEach { timeSlot ->
                            combinedList.add(timeSlot)
                        }
                    }
                mBinding.tvNoData.setVisible(combinedList.size == 0)

                val selectedIndex = combinedList.indexOfFirst {
                    (it.start == bookConsultationRequest.startTime && it.end == bookConsultationRequest.endTime)
                            || it.isChecked
                }

                combinedList.map { //avoiding multiple checks because of multiple arrays
                    it.isChecked = false
                    it
                }

                if (selectedIndex != -1) {
                    val item = combinedList[selectedIndex]
                    bookConsultationRequest.startTime = item.start
                    bookConsultationRequest.endTime = item.end
                    combinedList[selectedIndex].isChecked = true
                } else {
                    bookConsultationRequest.startTime = null
                    bookConsultationRequest.endTime = null
                }

                timeSlotsAdapter.listItems = combinedList
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
//        doctorConsultationViewModel.dateSlots.observe(this) {
//            if (it.isNullOrEmpty()) return@observe
//            dateSlotsAdapter.listItems = it
//        }
        doctorConsultationViewModel.selectedAddress.observe(this) {
            it?.let {
                mBinding.caAddresses.apply {
                    it.itemEndIcon = R.drawable.ic_edit

                    doctorConsultationViewModel.bookConsultationRequest.userLocationId =
                        (it as AddressModel?)?.id.getSafe()
                    listItems = arrayListOf(it)
                }
            }
        }
    }

    private fun setupDateSlotsList() {
        mBinding.apply {
            dateSlotsAdapter = DateSlotsAdapter()
            rvDateSlots.adapter = dateSlotsAdapter

            dateSlotsAdapter.onItemSelected = { item, pos ->
                doctorConsultationViewModel.bookConsultationRequest.bookingDate = item.timestamp
                updateTimeSlots(pos)
            }
        }
    }

    private fun setMessageConsultationDataInViews() {
        if (doctorConsultationViewModel.bookConsultationRequest.serviceId != CustomServiceTypeView.ServiceType.Message.id)
            return

        mBinding.apply {
            hideMessageIrrelevantViews(true)

            val fee = doctorConsultationViewModel.partnerProfileResponse.fee

            val currency =
                DataCenter.getMeta()?.currencies?.find { it.itemId == doctorConsultationViewModel.partnerProfileResponse.currencyId.toString() }
            if (currency?.genericItemName != null && fee != null) {
                tvPrice.text =
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "$fee ${currency.genericItemName}" else "${currency.genericItemName} $fee"
            } else {
                tvPrice.text = fee
            }

            doctorConsultationViewModel.bookConsultationRequest.fee = fee
        }
    }

    private fun setDataInViews(data: PartnerProfileResponse?) {
        data?.let {
            mBinding.partner = it
            doctorConsultationViewModel.bookConsultationRequest.apply {
                bookingId = it.bookingId
            }

            val patientNames = it.patients?.map { p ->
                if (p.familyMemberId == DataCenter.getUser()?.id)
                    p.fullName = mBinding.langData?.globalString?.self
                p.fullName
            }
            var yearExp = ApplicationClass.mGlobalData?.globalString?.yearExp?.replace(
                "[0]",
                it.experience.getSafe()
            )
            if (it.experience?.isNotEmpty().getSafe()) {
                try {
                    if (it.experience?.toFloat().getSafe() < 1F)
                        yearExp =
                            ApplicationClass.mGlobalData?.globalString?.lessThanOneYearExp?.replace(
                                "[0]",
                                it.experience.getSafe()
                            )
                    else if (it.experience?.toFloat().getSafe() > 1F)
                        yearExp = ApplicationClass.mGlobalData?.globalString?.yearsExp?.replace(
                            "[0]",
                            it.experience.getSafe()
                        )
                } catch (e: Exception) {
                    e.printStackTrace()
                    yearExp = ""
                }

            } else {
                yearExp = ""
            }
            mBinding.iDoctor.tvYearsExp.text = yearExp
            mBinding.cdPatients.apply {
                this.data = patientNames as ArrayList<String>? ?: arrayListOf()
                if (patientNames.isNullOrEmpty().not()) {
                    selectionIndex = 0
                    if (doctorConsultationViewModel.bookConsultationRequest.patientId != null) {
                        val pos =
                            it.patients?.indexOfFirst { pt -> pt.familyMemberId == doctorConsultationViewModel.bookConsultationRequest.patientId }
                                .getSafe()
                        selectionIndex = if (pos == -1) 0 else pos
                    }
                }
            }

            val list = arrayListOf<MultipleViewItem>()
            data.medicalRecord?.forEach { medicalRecords ->
                list.apply {
                    add(
                        MultipleViewItem(
                            title = medicalRecords.emrName,
                            itemId = medicalRecords.emrId.toString(),
                            desc = "${Constants.END}${
                                getDateInFormat(
                                    medicalRecords.date.getSafe(),
                                    "yyyy-MM-dd",
                                    com.homemedics.app.utils.getString(R.string.dateFormat)
                                )
                            }${Constants.START}${Constants.END} ${Constants.PIPE} ${mBinding.langData?.emrScreens?.record.getSafe()} ${Constants.HASH} ${Constants.START}${medicalRecords.emrNumber}${Constants.END} ${Constants.START}",
                            drawable = R.drawable.ic_medical_rec,
                        ).apply {
                            extraInt = medicalRecords.emrType
                        }
                    )
                }
            }
            mBinding.caMedicalRecords.listItems = list


            val excludedServiceIds = data.services?.filter { it.offerService.not() }?.map {
                it.id
            }

            if (data.isAvailableInCity.getBoolean().not())
                (excludedServiceIds as ArrayList).add(CustomServiceTypeView.ServiceType.HomeVisit.id)

            mBinding.serviceTypeView.setDisabledServices(excludedServiceIds as ArrayList<Int>?)
        }
    }

    private fun setSlotsDataInViews(data: PartnerSlotsResponse?) {
        data?.let {
            doctorConsultationViewModel.partnerProfileResponse.fee = data.fee
            mBinding.slotsResponse = it
            doctorConsultationViewModel.bookConsultationRequest.apply {
                fee = it.fee
            }

            if (it.dateSlots.isNullOrEmpty().not()) {
                val filter = it.dateSlots?.filter { slots -> slots.isActive == true }
                val bookingDate = doctorConsultationViewModel.bookConsultationRequest.bookingDate

                if (filter?.isNotEmpty().getSafe()) {
                    var selectedDate = ""

                    if (bookingDate.isNullOrEmpty().not()) {
                        selectedDate =
                            it.dateSlots?.firstOrNull { slots -> slots.timestamp == bookingDate && slots.isActive == true }?.timestamp.getSafe()
                    } else {
                        selectedDate =
                            it.dateSlots?.first { slots -> slots.isActive == true }?.timestamp.getSafe()
                    }

                    doctorConsultationViewModel.bookConsultationRequest.bookingDate =
                        selectedDate

                    it.dateSlots?.map {
                        it.isChecked =
                            it.timestamp == doctorConsultationViewModel.bookConsultationRequest.bookingDate
                        it
                    }

                    dateSlotsAdapter.listItems = it.dateSlots as ArrayList<DateSlotResponse>
                    val index =
                        dateSlotsAdapter.listItems.indexOfFirst { it.timestamp == doctorConsultationViewModel.bookConsultationRequest.bookingDate }

                    if (index != -1) {
                        updateTimeSlots(index)
                        mBinding.rvDateSlots.smoothScrollToPosition(index)
                    }
                } else {
                    mBinding.apply {
                        tvTimingText.setVisible(false)
                        tvNoData.setVisible(true)
                    }
                }

            }
        }

        setMessageConsultationDataInViews()
    }

    private fun setPartnerBio(view: TextView, partner: PartnerProfileResponse?) {
        partner?.let {
            val sb = StringBuilder()
            partner.specialities?.forEachIndexed { index, item ->
                sb.append(item.genericItemName)
                if (index != partner.specialities?.size?.minus(1))
                    sb.append(", ")
            }
            view.text = sb.toString()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("service_id", doctorConsultationViewModel.bdcFilterRequest.serviceId.getSafe())
        outState.putInt("partner_user_id", doctorConsultationViewModel.partnerProfileResponse.partnerUserId.getSafe())
        outState.putInt("city_id", doctorConsultationViewModel.bdcFilterRequest.cityId.getSafe())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            doctorConsultationViewModel.bdcFilterRequest.serviceId = savedInstanceState.getInt("service_id", 0)
            doctorConsultationViewModel.partnerProfileResponse.partnerUserId = savedInstanceState.getInt("partner_user_id", 0)
            doctorConsultationViewModel.bdcFilterRequest.cityId = savedInstanceState.getInt("city_id", 0)
            getPartnerDetails()
        }
    }

    private fun getPartnerDetails() {
        val request = PartnerDetailsRequest(
            serviceId = doctorConsultationViewModel.bdcFilterRequest.serviceId,
            partnerUserId = doctorConsultationViewModel.partnerProfileResponse.partnerUserId,
            bookingId = doctorConsultationViewModel.partnerProfileResponse.bookingId,
            cityId = doctorConsultationViewModel.bdcFilterRequest.cityId
        )

        if (isOnline(requireActivity())) {
            doctorConsultationViewModel.getPartnerDetails(request).observe(viewLifecycleOwner) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<PartnerProfileResponse>
                        response.data?.let {
                            doctorConsultationViewModel.partnerProfileResponse = it
                        }

                        setDataInViews(response.data)
                        val reviews = response.data?.totalNoOfReviews
                        val desc = response.data?.overview
                        mBinding.apply {
                            tvDesc.setVisible(desc != null && desc.isNotEmpty())
                            iDoctor.apply {
                                tvAmount.setVisible(false)
                                tvReview.apply {
                                    setVisible(reviews != null)

                                    val noOfReview = if (reviews == 0) reviews else String.format(
                                        "%02d",
                                        reviews
                                    )
                                    text = "${langData?.globalString?.reviews}($noOfReview)"
                                    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                                }
                                lifecycleScope.launch {
                                    delay(200) // delay for to avoid data binding
                                    setPartnerBio(tvExperience, response.data)

                                    if (response.data?.average_reviews_rating.getSafe() > 0.0) {
                                        tvNoRating.gone()
                                        ratingBar.apply {
                                            visible()
                                            rating =
                                                response.data?.average_reviews_rating?.toFloat()
                                                    .getSafe()
                                        }
                                    } else {
                                        ratingBar.gone()
                                        tvNoRating.text =
                                            ApplicationClass.mGlobalData?.globalString?.noRating
                                        tvNoRating.visible()
                                    }
                                }
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

    private fun getSlots() {
        val request = PartnerDetailsRequest(
            serviceId = doctorConsultationViewModel.bdcFilterRequest.serviceId,
            partnerUserId = doctorConsultationViewModel.partnerProfileResponse.partnerUserId,
            cityId = doctorConsultationViewModel.bdcFilterRequest.cityId
        )

        if (isOnline(requireActivity())) {
            doctorConsultationViewModel.getSlots(request = request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        hideLoader()
                        val response = it.data as ResponseGeneral<PartnerSlotsResponse>
                        response.data?.let {
                            doctorConsultationViewModel.partnerSlotsResponse = it
                        }

                        setSlotsDataInViews(response.data)
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

    private fun bookConsultation(request: BookConsultationRequest) {
        if (isOnline(requireActivity())) {
            if (doctorConsultationViewModel.bookConsultationRequest.fee?.equals("Free", true)
                    .getSafe()
            )
                doctorConsultationViewModel.bookConsultationRequest.fee = "0"

            doctorConsultationViewModel.bookConsultation(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        if (request.serviceId == CustomServiceTypeView.ServiceType.Message.id && TinyDB.instance.getString(
                                com.fatron.network_module.utils.Enums.TinyDBKeys.CHATTOKEN.key
                            )
                                .isNotEmpty() && ApplicationClass.twilioChatManager?.conversationClients == null
                        ) {
                            ApplicationClass.twilioChatManager?.initializeWithAccessToken(
                                requireContext(),
                                TinyDB.instance.getString(
                                    com.fatron.network_module.utils.Enums.TinyDBKeys.CHATTOKEN.key
                                ).getSafe(),
                                TinyDB.instance.getString(
                                    com.fatron.network_module.utils.Enums.TinyDBKeys.FCM_TOKEN.key
                                )
                            )

                        }

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
                        val message = "active_session"
                        if (message == it.generalResponse.message.toString())
                             {
                                DialogUtils(requireActivity())
                                    .showSingleButtonAlertDialog(
                                        message = "The message consultation is already booked for current doctor",
                                        buttonCallback = {},
                                    )
                            } else {
                                DialogUtils(requireActivity())
                                    .showSingleButtonAlertDialog(
                                        message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                        buttonCallback = {},
                                    )
                            }
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

    private fun callGetAttachments() {
        if (isOnline(requireActivity())) {
            doctorConsultationViewModel.callGetAttachments(
                AppointmentDetailReq(
                    bookingId = doctorConsultationViewModel.partnerProfileResponse.bookingId.getSafe()
                        .toString()
                )
            )
                .observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<AttachmentResponse>
                            doctorConsultationViewModel.fileList = arrayListOf()
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
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
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

            doctorConsultationViewModel.fileList.add(
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

        mBinding.attachmentDivider.setVisible(doctorConsultationViewModel.fileList.size > 0)
    }

    fun String.toUnicode(): String {


        var unicodeString = this.map { String.format("\\u%04x", it.toInt()) }.joinToString("")
        Timber.e(unicodeString)
        unicodeString =
            unicodeString.replace("\\u", "").chunked(4) { it.toString().toInt(16).toChar() }
                .joinToString("")

        return unicodeString
    }

    private fun addAttachmentApiCall() {
        val mediaList = ArrayList<MultipartBody.Part>()
        val absolutepath=file?.absolutePath
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
            doctorConsultationViewModel.addConsultationAttachment(
                booking_id = doctorConsultationViewModel.partnerProfileResponse.bookingId.getSafe(),
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

    private fun deleteDocumentApiCall(item: MultipleViewItem) {
        val request =
            DeleteAttachmentRequest(item.itemId?.toInt())

        if (isOnline(requireActivity())) {
            doctorConsultationViewModel.deleteDocumentApiCall(request = request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<*>
                            try {
                                showToast(response.message.getSafe())
                                doctorConsultationViewModel.fileList.remove(item)
                                itemsAdapter.listItems = doctorConsultationViewModel.fileList
                                mBinding.attachmentDivider.setVisible(doctorConsultationViewModel.fileList.size > 0)
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

    private fun detachEMRtoBDC(item: MultipleViewItem, pos: Int) {
        val request = AttachEMRtoBDCRequest(
            bookingId = doctorConsultationViewModel.partnerProfileResponse.bookingId,
            emrId = item.itemId?.toInt(),
            emrCustomerType = item.extraInt
        )

        if (isOnline(requireActivity())) {
            emrViewModel.detachEMRtoBDC(request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val type = object : TypeToken<ArrayList<MultipleViewItem>>() {}.type
                            val tempList: ArrayList<MultipleViewItem> = Gson().fromJson(
                                Gson().toJson(mBinding.caMedicalRecords.listItems),
                                type
                            )
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
            voiceNote = mBinding.langData?.dialogsStrings?.voiceNoteDescription.getSafe()
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