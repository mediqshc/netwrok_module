package com.homemedics.app.ui.fragment.medicalrecords.patient.reports

import android.app.AlertDialog
import android.net.Uri
import android.view.View
import android.widget.Button
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.emr.type.EMRTypeEditResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogEnterDateBinding
import com.homemedics.app.databinding.FragmentAddReportRecordDocumentBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import okhttp3.MultipartBody
import java.io.File

class AddReportRecordDocumentsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentAddReportRecordDocumentBinding
    private lateinit var fileUtils: FileUtils
    private val emrViewModel: EMRViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.uploadReport.getSafe()
            caRecord.apply {
                title = lang?.emrScreens?.reportDocument.getSafe()
                custDesc = lang?.emrScreens?.addDocumentImage.getSafe()
            }
        }
    }

    override fun init() {
        fileUtils = FileUtils()
        emrViewModel.attachmentsToUpload.clear()
        fileUtils.init(this)
        setDataInViews()
    }

    override fun getFragmentLayout() = R.layout.fragment_add_report_record_document

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddReportRecordDocumentBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }

            bSave.setOnClickListener {
                emrViewModel.storeMedicineRequest.emrId = emrViewModel.emrID
                emrViewModel.storeMedicineRequest.emrTypeId = emrViewModel.selectedRecord?.genericItemId
                emrViewModel.storeMedicineRequest.type = Enums.EMRType.REPORTS.key
                addRecordApi()
            }

            caRecord.onAddItemClick = {
                fileUtils.requestFilePermissions(requireActivity()) { result ->
                    val file  = result?.uri.let { uri ->
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
                    if (file?.let { metaData?.maxFileSize?.let { it1 ->
                            fileUtils.photoUploadValidation(it,
                                it1
                            )
                        } }.getSafe()){
                        showFileSizeDialog()

                    }else{
                        emrViewModel.attachmentsToUpload.add(MultipleViewItem(title = result?.uri?.let {
                            fileUtils.getFileNameFromUri(
                                requireContext(),
                                it
                            )
                        }, itemId = result?.path.toString(), drawable = R.drawable.ic_document).apply {
                            type = result?.uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }
                        })

                        caRecord.listItems = emrViewModel.attachmentsToUpload
                        mBinding.bSave.isEnabled = caRecord.listItems.isNotEmpty()
                        mBinding.caRecord.addButtonEnabled = caRecord.listItems.isEmpty()
                        showEnterDateDialog()
                    }
                }
            }
            caRecord.onDeleteClick = { item, position ->
                caRecord.listItems.removeAt(position)
                caRecord.mBinding.rvItems.adapter?.notifyDataSetChanged()
                emrViewModel.attachmentsToUpload.removeAt(position)
                mBinding.caRecord.addButtonEnabled = caRecord.listItems.isEmpty()
                mBinding.bSave.isEnabled = caRecord.listItems.isNotEmpty()
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun setDataInViews(){
        mBinding.apply {
            iDoctor.tvTitle.text = emrViewModel.selectedRecord?.title
            iDoctor.tvDesc.text = emrViewModel.selectedRecord?.description
            iDoctor.ivIcon.setImageResource(R.drawable.ic_bloodtype)
            iDoctor.ivThumbnail.invisible()
        }
    }

    private fun showFileSizeDialog(){
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = mBinding.lang?.globalString?.information.getSafe(),
                message = mBinding.lang?.dialogsStrings?.fileSize.getSafe(),
                buttonCallback = {},
            )
    }

    private lateinit var dialogEnterDateBinding: DialogEnterDateBinding
    private lateinit var builder: AlertDialog
    private lateinit var dialogSaveButton: Button
    private fun showEnterDateDialog(){
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogEnterDateBinding = DialogEnterDateBinding.inflate(layoutInflater).apply {
                etDate.hint = mBinding.lang?.globalString?.date.getSafe()
            }
            setView(dialogEnterDateBinding.root)
            setTitle(mBinding.lang?.emrScreens?.enterDate.getSafe())
            setPositiveButton(mBinding.lang?.globalString?.add.getSafe()) { _, _ ->
                builder.dismiss()
            }
            setNegativeButton(mBinding.lang?.globalString?.cancel.getSafe(), null)

            dialogEnterDateBinding.apply {
                etDate.textColorCheck = true
                etDate.clickCallback = {
                    openCalender(dialogEnterDateBinding.etDate.mBinding.editText)
                }
                etDate.mBinding.editText.doAfterTextChanged {
                    dialogSaveButton.isEnabled = isValid(etDate.text)
                }
            }
        }.create()

        builder.setOnShowListener{
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                emrViewModel.storeMedicineRequest.date = getDateInFormat(dialogEnterDateBinding.etDate.text, "dd/MM/yyyy", "yyyy-MM-dd")
                builder.dismiss()
            }
            dialogSaveButton.isEnabled = false

        }
        builder.show()
    }

    private fun setAttachments(data: List<Attachment>?) {
        mBinding.bSave.isEnabled = data.isNullOrEmpty().not()
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
            emrViewModel.fileList.add(
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

        mBinding.caRecord.listItems = emrViewModel.fileList
    }

    private fun addAttachmentApiCall(emrTypeId: Int) {
        val mediaList = ArrayList<MultipartBody.Part>()
        var mimeType = 0

        emrViewModel.attachmentsToUpload.forEachIndexed { index, item ->
            var type=fileUtils.getMimeType(requireContext(), Uri.parse(item.itemId)).getSafe()
            if(type == "")
                type= item.type.getSafe()
            val multipartFile = fileUtils.convertFileToMultiPart(
                File(item.itemId.getSafe()),
                type  ,
                "attachments"
            )

            val typeImage =
                if (type.contains("image").getSafe()) type else ""
            mimeType = when (type) {
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
        }

        if (isOnline(requireActivity())) {
            emrViewModel.addCustomerEMRAttachment(
                emrViewModel.emrID,
                Enums.EMRType.REPORTS.key,
                emrTypeId,
                mimeType.toString(),
                mediaList
            ).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
//                            showEnterDateDialog()
//                            showToast(getString(R.string.m_record_added))
                            emrViewModel.attachmentsToUpload.clear()
                            findNavController().popBackStack(R.id.selectLabTestFragment, true)

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
            DeleteAttachmentRequest(item.itemId?.toInt())

        if (isOnline(requireActivity())) {
            emrViewModel.deleteCustomerEMRAttachment(request = request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<AttachmentResponse>
                            try {
                                showToast(response.message.getSafe())
                                emrViewModel.fileList.remove(item)
                                setAttachments(response.data?.attachments)
//                                callGetAttachments()
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

    private fun addRecordApi() {
        if (isOnline(requireActivity())) {
            emrViewModel.addCustomerMedicineRecord().observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<EMRTypeEditResponse>
//                        showToast(getString(R.string.m_record_added))

//                        emrViewModel.attachmentsToUpload.clear()

                        addAttachmentApiCall(response.data?.emrTypeId?.toInt().getSafe())

//                        findNavController().popBackStack(R.id.selectLabTestFragment, true)
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
                    else -> {}
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