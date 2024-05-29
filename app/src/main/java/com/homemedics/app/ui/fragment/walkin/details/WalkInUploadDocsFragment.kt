package com.homemedics.app.ui.fragment.walkin.details

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.walkin.AddWalkInAttachmentRequest
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentClaimUploadDocsBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.MyOrderViewModel
import com.homemedics.app.viewmodel.WalkInViewModel
import okhttp3.MultipartBody
import java.io.File

class WalkInUploadDocsFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentClaimUploadDocsBinding
    private val walkinViewModel: WalkInViewModel by activityViewModels()
    private val ordersViewModel: MyOrderViewModel by activityViewModels()
    private var optionsAdapter = AddMultipleViewAdapter()
    private var walkInPharmacyDetail = false
    private var fromImage: Boolean = false
    private var fromWalkInHospitalBooking: Boolean = false
    private var file: File? = null
    private lateinit var fileUtils: FileUtils
    private var langData: RemoteConfigLanguage? = null
    private var from_lab = false
    private var from_hospital = false

    private var selectedDocTypeId = 0
    private var partnerServiceId = 0
    private var walkInId = 0

    override fun init() {
        langData = ApplicationClass.mGlobalData
        partnerServiceId =
            if (ordersViewModel.selectedOrder?.partnerServiceId != null) ordersViewModel.selectedOrder?.partnerServiceId.getSafe()
            else walkinViewModel.partnerServiceId
        walkInId =
            when (partnerServiceId) {
                CustomServiceTypeView.ServiceType.WalkInPharmacy.id -> walkinViewModel.walkInResponse?.details?.id.getSafe()
                CustomServiceTypeView.ServiceType.WalkInLaboratory.id -> walkinViewModel.walkInResponse?.details?.id.getSafe()
                else -> walkinViewModel.walkInResponse?.details?.id.getSafe()
              // CustomServiceTypeView.ServiceType.WalkInPharmacy.id -> ordersViewModel.selectedOrder?.walkInPharmacy?.walkInPharmacyId.getSafe()
                //CustomServiceTypeView.ServiceType.WalkInLaboratory.id -> ordersViewModel.selectedOrder?.walkInLaboratory?.walkInLaboratoryId.getSafe()
                //else -> ordersViewModel.selectedOrder?.walkInHospital?.walkInHospitalId.getSafe()
            }

        walkinViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)
        val fromNoti = arguments?.getBoolean("fromNoti").getSafe()
        fromImage = arguments?.getBoolean("fromImage").getSafe()
        from_lab = arguments?.getBoolean("from_lab").getSafe()
        from_hospital = arguments?.getBoolean("fromHospital").getSafe()
        walkInPharmacyDetail = arguments?.getBoolean("walkInPharmacyDetails").getSafe()
        fromWalkInHospitalBooking = arguments?.getBoolean("fromHospital").getSafe()
        if (fromNoti) {
            walkInId = arguments?.getInt("noti_booking_id").getSafe()
        }
        if (fromImage) {
            mBinding.apply {
                actionbar.title =langData?.globalString?.uploadImage.getSafe()

            }
        }
        mBinding.rvDocType.adapter = optionsAdapter

        val list =
            if (walkinViewModel.walkInResponse?.details?.bookingDetails?.settlementDocuments.isNullOrEmpty()) {
//           when (partnerServiceId) {
//               CustomServiceTypeView.ServiceType.WalkInPharmacy.id -> metaData?.walkInPharmacyDocumentType
//               CustomServiceTypeView.ServiceType.WalkInLaboratory.id -> metaData?.walkInLaboratoryDocumentType
//               else -> metaData?.walkInHospitalDocumentType
//           }
                walkinViewModel.documentTypes.getSafe()
            } else {
                walkinViewModel.walkInResponse?.details?.bookingDetails?.settlementDocuments
            }

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
                walkinViewModel.isAttachment = true
                //   findNavController().popBackStack()

                if (fromImage) {
                    mBinding.apply {
                        actionbar.title = langData?.globalString?.uploadImage.getSafe()
                    }

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
                        fileUtils.requestFilePermissions(requireActivity(), false, true) { result ->
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

    private fun addAttachmentApiCall() {
        val mediaList = ArrayList<MultipartBody.Part>()
        val uri = file?.let { fileUtils.getUriFromFile(context = requireContext(), it) }
        val mimeTypeView = uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }

        var mimeType: Int = 0
        val typeImage =
            if (mimeTypeView?.contains("image").getSafe()) mimeTypeView else ""
        when (mimeTypeView) {
            FileUtils.typeOther,
            FileUtils.typePDF, FileUtils.typeDOC, FileUtils.typeDOCX -> {
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
            var api: LiveData<ResponseResult<*>>? = null

            if (partnerServiceId == CustomServiceTypeView.ServiceType.WalkInPharmacy.id) {
                val request = AddWalkInAttachmentRequest(
                    walkInPharmacyId = walkInId,
                    pharmacyId = walkinViewModel.walkInResponse?.details?.pharmacyId,
                    attachmentType = mimeType,
                    documentType = selectedDocTypeId,
                    attachments = mediaList
                )

                api = walkinViewModel.addWalkInAttachment(request)
            } else if (partnerServiceId == CustomServiceTypeView.ServiceType.WalkInLaboratory.id) {
                val request = AddWalkInAttachmentRequest(
                    walkInLaboratoryId = walkInId,
                    labId = walkinViewModel.walkInResponse?.details?.laboratoryId,
                    attachmentType = mimeType,
                    documentType = selectedDocTypeId,
                    attachments = mediaList
                )

                api = walkinViewModel.addWalkInLabAttachment(request)
            } else {
                val request = if (fromWalkInHospitalBooking) AddWalkInAttachmentRequest(
                    walkInHospitalId = walkinViewModel.walkInInitialResponse.walkInHospital?.walkInHospitalId.getSafe(),
                    healthcareId = walkinViewModel.hospitalId,
                    attachmentType = mimeType,
                    documentType = selectedDocTypeId, //other for now
                    attachments = mediaList
                ) else AddWalkInAttachmentRequest(
                    walkInHospitalId = walkInId,
                    healthcareId = walkinViewModel.walkInResponse?.details?.healthcareId,
                    attachmentType = mimeType,
                    documentType = selectedDocTypeId,
                    attachments = mediaList
                )

                api = walkinViewModel.addWalkInHospitalAttachment(request)
            }

            api.observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            walkinViewModel.isAttachment = true
                            if (walkInPharmacyDetail) {
                                /*  findNavController().navigate(R.id.walkInPharmacyDetailsFragment,
                                  bundleOf("amountEnable" to true))
                                  walkInPharmacyDetail=false*/
                                findNavController().popBackStack()

                            } else if (from_lab) {
                                /*findNavController().navigate(R.id.walkInLabOrdersDetailFragment,
                                bundleOf("submitreview_lab" to true)) */
                                findNavController().popBackStack()
                            } else if (from_hospital) {
                                findNavController().popBackStack()
                            } else {
                            /*    findNavController().navigate(R.id.action_walkInUploadDocsFragment_to_walkInOrdersDetailFragment)
                                walkinViewModel.isSubmitReviewAttachment=true
                                bundleOf(
                                    "submitreview" to true,
                                    "attachment_visibility2" to true

                                )*/
                                findNavController().popBackStack()
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