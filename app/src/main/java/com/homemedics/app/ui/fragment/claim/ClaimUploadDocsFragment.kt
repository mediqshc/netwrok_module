package com.homemedics.app.ui.fragment.claim

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.claim.AddClaimAttachmentRequest
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentClaimUploadDocsBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ClaimViewModel
import okhttp3.MultipartBody
import java.io.File

class ClaimUploadDocsFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentClaimUploadDocsBinding
    private val claimViewModel: ClaimViewModel by activityViewModels()
    private var optionsAdapter = AddMultipleViewAdapter()
    private var fromImage: Boolean = false
    private var file: File? = null
    private lateinit var fileUtils: FileUtils
    private var langData: RemoteConfigLanguage? = null

    private var selectedDocTypeId = 0
    var fromRequest = false

    override fun init() {
        langData = ApplicationClass.mGlobalData
        claimViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)
        fromRequest = arguments?.getBoolean("fromRequest").getSafe()
        fromImage = arguments?.getBoolean("fromImage").getSafe()
        if (fromImage)
            mBinding.actionbar.title = langData?.globalString?.uploadImage.getSafe()

        mBinding.rvDocType.adapter = optionsAdapter

        val list = if( claimViewModel.claimResponse?.details?.bookingDetails?.settlementDocuments.isNullOrEmpty()){
            claimViewModel.documentTypes
        }

        else claimViewModel.claimResponse?.details?.bookingDetails?.settlementDocuments

        optionsAdapter.listItems = list?.map {
            it.title = it.genericItemName
            if (it.required.getBoolean())
                it.title += "*"
            it.itemEndIcon = R.drawable.ic_arrow_fw_black
            it.drawable = R.drawable.ic_image
            it
        }.getSafe()

        setObserver()
    }

    override fun setLanguageData() {
        mBinding.apply {
            actionbar.title = ApplicationClass.mGlobalData?.claimScreen?.uploadDocument.getSafe()
            tvNoData.text =
                ApplicationClass.mGlobalData?.bookingScreen?.noHomeServiceFound.getSafe()
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_claim_upload_docs

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentClaimUploadDocsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            optionsAdapter.onItemClick = { item, _ ->
                //upload doc
                selectedDocTypeId = item.itemId?.toInt().getSafe()
                if (fromImage) {

                    fileUtils.requestPermissions(
                        requireActivity(),


                        ) { result ->
                        takeAction(result)
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = "application/pdf"
                        startActivityForResult(intent, 1)
                    } else {
                        fileUtils.requestFilePermissions(requireActivity(), false,true) { result ->
                            takeAction(result)
                        }
                    }
                }
            }
        }
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

    private fun takeAction(result: FileUtils.FileData?) {
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

    private fun showFileSizeDialog() {
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = langData?.globalString?.information.getSafe(),
                message = langData?.dialogsStrings?.fileSize.getSafe(),
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
            positiveButtonStringText = langData?.dialogsStrings?.permitManual.getSafe(),
            negativeButtonStringText = langData?.globalString?.close.getSafe(),
            buttonCallback = {
                context?.let { gotoAppSettings(it) }
            },
            cancellable = false
        )
    }

    private fun showRequestSubmitDialog() {
        DialogUtils(requireActivity())
            .showDoubleButtonsAlertDialog(
                title = langData?.labPharmacyScreen?.requestSubmitted.getSafe(),
                message = langData?.labPharmacyScreen?.submittedMsg.getSafe(),
                positiveButtonStringText = langData?.bookingScreen?.viewDetails.getSafe(),
                negativeButtonStringText = langData?.bookingScreen?.newRequest.getSafe(),
                buttonCallback = {
                    findNavController().safeNavigate(
                        R.id.action_labTestBookingWithOrderDetailsFragment_to_ordersDetailFragment,
                        bundleOf(Constants.BOOKINGID to claimViewModel.claimRequest.claimId)
                    )
                },
                negativeButtonCallback = {
                    tinydb.remove(com.fatron.network_module.utils.Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
                    findNavController().navigate(R.id.action_labTestBookingFragment_to_labTestFragment)
                },
                cancellable = false
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
            FileUtils.typePDF,FileUtils.typeDOC,FileUtils.typeDOCX -> {
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

        //either from submit claim or claim details
        val categoryId = if(claimViewModel.fromDetails) claimViewModel.claimResponse?.details?.claimCategoryId else claimViewModel.claimRequest.claimCategoryId

        if (isOnline(requireActivity())) {
            val request = AddClaimAttachmentRequest(
                claimId = claimViewModel.claimRequest.claimId,
                claimCategoryId = categoryId,
                attachmentType = mimeType,
                documentType = selectedDocTypeId,
                attachments = mediaList
            )

            claimViewModel.addClaimAttachment(request).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            claimViewModel.isAttachment = true
                            findNavController().popBackStack()
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
                            title = langData?.errorMessages?.internetError.getSafe(),
                            message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                            buttonCallback = {},
                        )
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun setObserver() {

    }

    override fun onClick(v: View?) {

    }

}