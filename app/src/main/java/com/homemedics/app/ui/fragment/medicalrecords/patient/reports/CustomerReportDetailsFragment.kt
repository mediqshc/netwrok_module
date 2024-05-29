package com.homemedics.app.ui.fragment.medicalrecords.patient.reports

import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.emr.EMRDownloadRequest
import com.fatron.network_module.models.request.emr.type.EMRDetailsRequest
import com.fatron.network_module.models.request.emr.type.EMRTypeDeleteRequest
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
import com.homemedics.app.databinding.FragmentCustomerReportDetailsBinding
import com.homemedics.app.databinding.ItemSharedUserViewBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.EMRActivity
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class CustomerReportDetailsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentCustomerReportDetailsBinding
    private val emrViewModel: EMRViewModel by activityViewModels()
    private var attachmentsAdapter = AddMultipleViewAdapter()
    private var listItems: ArrayList<MultipleViewItem>? = null
    private var customerRecordResponse: CustomerRecordResponse? = null
    private var isSharedPerson = false

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            caLabDiagnostic.title = lang?.emrScreens?.labAndDiagnostics.getSafe()
        }
    }

    override fun init() {

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

    override fun getFragmentLayout() = R.layout.fragment_customer_report_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCustomerReportDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                findNavController().safeNavigate(CustomerReportDetailsFragmentDirections.actionCustomerReportDetailsFragmentToShareRecordWithFragment())
            }
            actionbar.onAction3Click = {
                val request = EMRDownloadRequest(
                    emrId = emrViewModel.emrID,
                    type = emrViewModel.selectedEMRType?.key
                )
                downloadReportApi(request)
            }
            attachmentsAdapter.onDeleteClick = { item, _ ->
                deleteRecordAttachmentItemApi(item.extraInt.getSafe())
            }
            caLabDiagnostic.onEditItemCall = { item ->
                try {
                    val request = EMRDownloadRequest(
                        emrId = emrViewModel.emrID,
                        type = emrViewModel.selectedEMRType?.key,
                        emrAttachmentId = customerRecordResponse?.attachments?.get(
                            customerRecordResponse?.labTests?.indexOfFirst { it.id == item.itemId?.toInt() }.getSafe()
                        )?.id
                    )
                    downloadReportApi(request)
                }
                catch (e:Exception){
                    e.printStackTrace()
                    showToast(getString(R.string.something_went_wrong))
                }
            }
            bModify.setOnClickListener {
                findNavController().safeNavigate(CustomerReportDetailsFragmentDirections.actionCustomerReportDetailsFragmentToAddNewReportRecordFragment())
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

    private fun setDataInViews(response: CustomerRecordResponse?){
        response?.let { details ->
            val hash = "\u0023"
            mBinding.apply {
                bModify.setVisible(details.partnerName?.contains("self", true).getSafe())
                actionbar.title = "${lang?.emrScreens?.record} $hash ${details.emrNumber}"

                isSharedPerson = details.customerUser?.id != DataCenter.getUser()?.id
                if(isSharedPerson)
                    mBinding.actionbar.action2Res = 0

                iDoctor.apply {
                    tvTitle.text = details.partnerName.toString()

                    if(details.speciality.isNullOrEmpty()){
                        tvDesc.text = getDateInFormat(details.date.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")
                    }
                    else{
                        tvDesc.text = "${details.speciality?.get(0)?.genericItemName} ${Constants.PIPE} ${getDateInFormat(details.date.getSafe(), "yyyy-MM-dd", "dd MMM yyyy")}"
                        bModify.gone()
                    }

//                    ivThumbnail.invisible()
//
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
                        if (details.labIconUrl != null)
                            ivIcon.loadImage(details.labIconUrl)
                        else
                            ivIcon.setImageResource(
                                CustomServiceTypeView.ServiceType.getServiceById(
                                    details.serviceTypeId.getSafe().toInt())?.icon.getSafe()
                            )

                        ivThumbnail.invisible()
                    }
                }
                tvAttachments.setVisible(false)
                rvAttachments.setVisible(false)
//                setAttachments(response.attachments)

                listItems = ((details.labTests?.map {
                    it.drawable = R.drawable.ic_bloodtype
                    it.itemEndIcon = R.drawable.ic_download_black
                    it
                }) as ArrayList<MultipleViewItem>?) ?: arrayListOf()
                caLabDiagnostic.listItems = listItems as ArrayList<MultipleViewItem>


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
                        extraInt=it.id
                        itemEndIcon = 0
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
                        customerRecordResponse = response.data?.emrDetails
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

    private fun deleteRecordApi(){
        if (isOnline(requireActivity())) {
            emrViewModel.deleteCustomerEMRRecord().observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())
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

    private fun deleteRecordItemApi(emrTypeId: Int){
        val request = EMRTypeDeleteRequest(
            type = emrViewModel.selectedEMRType?.key,
            emrTypeId = emrTypeId
        )
        if (isOnline(requireActivity())) {
            emrViewModel.deleteCustomerEMRRecordType(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())

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

    private fun deleteRecordAttachmentItemApi(itemId: Int){
        val request = DeleteAttachmentRequest(emrAttachmentId =  itemId )
        if (isOnline(requireActivity())) {
            emrViewModel.deleteEMRDocument(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())

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

    private fun downloadReportApi(request: EMRDownloadRequest){
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