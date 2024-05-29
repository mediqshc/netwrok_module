package com.homemedics.app.ui.fragment.walkin.hospital

import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.homeservice.HomeServiceListRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.homeservice.HomeServiceListResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentWalkinHospitalUploadDocsBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.adapter.HomeServiceAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import com.homemedics.app.viewmodel.WalkInViewModel
import okhttp3.MultipartBody
import java.io.File

class WalkInHospitalUploadDocsFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentWalkinHospitalUploadDocsBinding
    private lateinit var homeServiceAdapter: HomeServiceAdapter
    private val homeServiceViewModel: DoctorConsultationViewModel by activityViewModels()
    private val walkInViewModel: WalkInViewModel by activityViewModels()

    private var file: File? = null
    private lateinit var fileUtils: FileUtils
    private var langData: RemoteConfigLanguage? = null

    override fun init() {
        langData = ApplicationClass.mGlobalData
        walkInViewModel.fileList = arrayListOf()
        fileUtils = FileUtils()
        fileUtils.init(this)
        homeServiceAdapter= HomeServiceAdapter()
        mBinding.rvDocType.adapter=homeServiceAdapter
        var list= arrayListOf<GenericItem>()
        list.add(GenericItem(genericItemName = "Prescription"))
        list.add(GenericItem(genericItemName = "Other"))

        homeServiceAdapter.listItems=list
        getServiceListCall()
        setObserver()
    }

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title =  langData?.globalString?.uploadDoc.getSafe()
            tvNoData.text =  langData?.bookingScreen?.noHomeServiceFound.getSafe()
        }
     }

    override fun getFragmentLayout() = R.layout.fragment_walkin_hospital_upload_docs

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkinHospitalUploadDocsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            homeServiceAdapter.itemClickListener = { item, _ ->
                //upload doc
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
                    }
                }
            }
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
            findNavController().popBackStack()
//            addAttachmentApiCall()
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
                    findNavController().safeNavigate(R.id.action_labTestBookingWithOrderDetailsFragment_to_ordersDetailFragment,
                        bundleOf(Constants.BOOKINGID to walkInViewModel.bookConsultationRequest.bookingId)
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
            walkInViewModel.addConsultationAttachment(
                booking_id = walkInViewModel.bookingIdResponse.bookingId.getSafe(),
                mimeType.toString(),
                mediaList
            ).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
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

    private fun getServiceListCall() {
        if (isOnline(requireActivity())) {
            homeServiceViewModel.getHomeServiceList(HomeServiceListRequest(countryId = homeServiceViewModel.countryId,cityId = homeServiceViewModel.cityId ))
                .observe(this) { it ->
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<HomeServiceListResponse>
                            homeServiceViewModel.medSpecialities.value= response.data?.services.getSafe()
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
                                    message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun setObserver() {

    }

    override fun onClick(v: View?) {

    }

}