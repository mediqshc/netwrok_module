package com.homemedics.app.ui.fragment.medicalrecords.patient.medications

import android.text.format.DateFormat
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.emr.EMRDownloadRequest
import com.fatron.network_module.models.request.emr.type.EMRDetailsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.emr.customer.records.CustomerEMRRecordResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCustomerMedicationDetailsBinding
import com.homemedics.app.databinding.ItemSharedUserViewBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.EMRActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class CustomerMedicationsDetailsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentCustomerMedicationDetailsBinding

    private val emrViewModel: EMRViewModel by activityViewModels()

    private var attachmentsAdapter = AddMultipleViewAdapter()
    private var isSharedPerson = false

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            etRecordDate.hint = lang?.emrScreens?.dateAndTime.getSafe()
            caMedications.title = lang?.emrScreens?.medications.getSafe()
        }
    }

    override fun init() {
//        setSharedFamily()
        setupViews()

        if(emrViewModel.emrID != 0) //syncing tempEmrID and emrID
            emrViewModel.tempEmrID = emrViewModel.emrID

        if (emrViewModel.tempEmrID != 0)
            emrViewModel.emrID = emrViewModel.tempEmrID

        val request = EMRDetailsRequest(
            emrId = emrViewModel.emrID,
            type = emrViewModel.selectedEMRType?.key
        )
        getRecordsDetailsApi(request)
    }

    override fun getFragmentLayout() = R.layout.fragment_customer_medication_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCustomerMedicationDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                findNavController().safeNavigate(CustomerMedicationsDetailsFragmentDirections.actionCustomerMedicationsDetailsFragmentToShareRecordWithFragment())
            }
            actionbar.onAction3Click = {
                val request = EMRDownloadRequest(
                    emrId = emrViewModel.emrID,
                    type = emrViewModel.selectedEMRType?.key
                )
                downloadReportApi(request)
            }
            bModify.setOnClickListener {
                findNavController().safeNavigate(CustomerMedicationsDetailsFragmentDirections.actionCustomerMedicationsDetailsFragmentToAddNewMedicationRecordFragment())
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun setSharedFamily() {
        mBinding.apply {
            llSharedWithMain.setVisible(emrViewModel.selectedFamilyForShare.isNotEmpty() && isSharedPerson.not())
            sharedDivider.setVisible(emrViewModel.selectedFamilyForShare.isNotEmpty() && isSharedPerson.not())
            val showLimit = 3

            if(emrViewModel.selectedFamilyForShare.isNotEmpty()){
                val remainingCount = emrViewModel.selectedFamilyForShare.size - showLimit

                run breaking@{
                    emrViewModel.selectedFamilyForShare.forEachIndexed { index, familyConnection ->
                        val itemBinding = ItemSharedUserViewBinding.inflate(layoutInflater)
                        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)

                        if(index != 0)
                            params.marginStart = resources.getDimensionPixelSize(R.dimen.dp8)

                        if(index < showLimit){
                            itemBinding.tvTitle.text = familyConnection.fullName
                            itemBinding.ivThumbnail.loadImage(familyConnection.userProfilePicture?.file, R.drawable.ic_profile_placeholder)

                            params.weight = 1.3f

                            itemBinding.root.layoutParams = params
                            llSharedWith.addView(itemBinding.root)
                        }
                        else{
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
                            llSharedWith.addView(itemBinding.root)
                            return@breaking
                        }
                    }
                }
            }
        }
    }

    private fun setupViews(){
        mBinding.apply {
            llRecordDate.gone()
        }
    }

    private fun setDataInViews(response: CustomerRecordResponse?){
        response?.let { details ->
            val num = "\u0023"
            mBinding.apply {
                actionbar.title = "${lang?.emrScreens?.record} $num ${details.emrNumber}"

                isSharedPerson = details.customerUser?.id != DataCenter.getUser()?.id
                if(isSharedPerson)
                    mBinding.actionbar.action2Res = 0

                iDoctor.apply {
                    tvTitle.text = details.partnerName.toString()
                    val timeFormat = if (DateFormat.is24HourFormat(getAppContext()))
                        getString(R.string.timeFormat24) else getString(R.string.timeFormat12)

                    var date = ""
                    if(details.originalDate.isNullOrEmpty()){
                        date = details.date.getSafe()
                    }
                    else{
                        date = details.originalDate.getSafe()
                        date = getDateInFormat(date, "yyyy-MM-dd hh:mm:ss", "dd MMMM yyyy ${Constants.PIPE}${Constants.PIPE} ${Constants.START}$timeFormat${Constants.END}")
                    }

                    if(details.speciality.isNullOrEmpty()){
                        tvDesc.text = date
                    }
                    else{
                        tvDesc.text = "${details.speciality?.get(0)?.genericItemName} ${Constants.PIPE} $date"
                        bModify.gone()
                    }

//                    ivThumbnail.invisible()

//                    var icon = 0
//                    if(details.serviceTypeId.isNullOrEmpty() || details.serviceTypeId == "0"){
//                        icon = R.drawable.ic_face
//                    } else{
//                        icon = CustomServiceTypeView.ServiceType.getServiceById(details.serviceTypeId.getSafe().toInt())?.icon.getSafe()
//                        bModify.gone()
//                    }
//                    ivIcon.setImageResource(icon)

                    if(details.serviceTypeId.isNullOrEmpty() || details.serviceTypeId == "0"){
                        val user = DataCenter.getUser()
                        ivThumbnail.loadImage(user?.profilePicture, getGenderIcon(user?.genderId.toString()))
                        ivThumbnail.visible()
                        ivIcon.invisible()
                    }
                    else {
                        ivIcon.setImageResource(CustomServiceTypeView.ServiceType.getServiceById(details.serviceTypeId.getSafe().toInt())?.icon.getSafe())
                        ivThumbnail.invisible()
                    }

                }
                if(details.originalDate!=null) {
                    var timeFormat = "hh:mm aa"
                    if (DateFormat.is24HourFormat(binding.root.context)) {
                        timeFormat = "HH:mm"
                    }
                    val dateFormat = "dd/MM/yyyy ${Constants.PIPE}${Constants.PIPE} ${Constants.START}$timeFormat${Constants.END}"
                    etRecordDate.text =
                        getDateInFormat(details.originalDate.getSafe(), "yyyy-MM-dd HH:mm", dateFormat)
                }
                else {
                    etRecordDate.text = details.date.getSafe()
                }

                setAttachments(response.attachments)

                val product = details.products?.map { medicine ->
                    val hourlyDosage = metaData?.dosageQuantity?.find { hr -> hr.genericItemId == medicine.dosage?.hourly?.toInt() }?.genericItemName
                    medicine.desc = medicine.description
                    medicine.descMaxLines = 4
                    medicine.subDesc = if (medicine.dosage?.hourly != null) "$hourlyDosage ${Constants.MULTIPLY} ${Constants.START}${medicine.noOfDays} ${lang?.globalString?.days}${Constants.END} ${Constants.PIPE}${Constants.PIPE} ${lang?.globalString?.quantity} ${Constants.COLON} ${medicine.dosageQuantity}"
                    else "${medicine.dosage?.morning}${Constants.PLUS}${medicine.dosage?.afternoon}${Constants.PLUS}${medicine.dosage?.evening} ${Constants.MULTIPLY} ${Constants.START}${medicine.noOfDays} ${lang?.globalString?.days}${Constants.END}"
                    medicine.itemEndIcon = 0
                    medicine
                }
                caMedications.listItems = product as ArrayList<MultipleViewItem>? ?: arrayListOf()

                emrViewModel.selectedFamilyForShare = details.shared as ArrayList<FamilyConnection>? ?: arrayListOf()
                setSharedFamily()
            }
        }
    }

    private fun setAttachments(attachments: List<Attachment>?) {
        if (attachments.isNullOrEmpty()) {
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

            val list: ArrayList<MultipleViewItem> = arrayListOf()
            attachments.forEach {
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
                val listTitle = if (index == -1) it.attachments?.file else it.attachments?.file?.substring(
                    index.getSafe() + 1,
                    it.attachments?.file?.length.getSafe()
                )
                val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
                list.add(
                    MultipleViewItem(
                        title = listTitle,
                        itemId = it.attachments?.file,
                        drawable = drawable,
                    ).apply {
                        itemEndIcon = 0
                        extraInt = it.id
                        if (it.attachmentTypeId == Enums.AttachmentType.VOICE.key) itemCenterIcon =
                            R.drawable.ic_voice_group

                        isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                    }
                )
            }

            attachmentsAdapter.listItems = list
        }
    }

    private fun getRecordsDetailsApi(request: EMRDetailsRequest){
        if (isOnline(requireActivity())) {
            emrViewModel.getCustomerEMRRecordsDetails(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<CustomerEMRRecordResponse>
                        setDataInViews(response.data?.emrDetails)
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
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

    private fun downloadReportApi(request:  EMRDownloadRequest){
        if (isOnline(requireActivity())) {
            emrViewModel.downloadEMR(request, (requireActivity() as EMRActivity).fileUtils).observe(this){
                when (it) {
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
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