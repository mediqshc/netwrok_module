package com.homemedics.app.ui.fragment.becomepartner

import android.Manifest
import android.os.Build
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.becomepartner.PartnerCnicResponse
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentUploadCnicPictureBinding
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class UploadCnicPictureFragment : BaseFragment(), View.OnClickListener  {

    private lateinit var mBinding: FragmentUploadCnicPictureBinding

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private lateinit var fileUtils: FileUtils

    private var frontCnic: File? = null
    private var backCnic: File? = null

    private var  buttonText:String =""

    private var user: UserResponse? = null

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.partnerProfileScreen?.uploadPicCnic.getSafe()
        }
    }

    override fun init() {
        profileViewModel. isFromMenu=0
        fileUtils = FileUtils()
        fileUtils.init(this)
        user = DataCenter.getUser()
        setupView()
    }

    override fun getFragmentLayout() = R.layout.fragment_upload_cnic_picture

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentUploadCnicPictureBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            caCnicFront.onAddItemClick = {
                if( caCnicFront.mBinding.tvAddNew.text== mBinding.langData?.globalString?.remove.getSafe()){
                    caCnicFront.tvAddButton = mBinding.langData?.globalString?.upload.getSafe()

                    caCnicFront.addNewRes = R.drawable.ic_upload_file_primary
                    mBinding.caCnicFront.cnicUri= null

                    frontCnic=null
                    buttonEnableCheck()

                }else {

                    caCnicFront.isEnabled = false
                    lifecycleScope.launch {
                        delay(2000)
                        caCnicFront.isEnabled = true
                    }
                    fileUtils.requestPermissions(requireActivity(),true) { result ->
                        if(fileUtils.hasPermissions(
                            requireContext(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    )
                                } else {
                                    arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.CAMERA,
                                    )
                                }
                        ).not()){
                            showAllowPermissionsDialog()
                            return@requestPermissions
                        }

                        if(result?.path.isNullOrEmpty().not()){caCnicFront.addNewRes = R.drawable.ic_delete_file
                        frontCnic  = result?.path?.toUri().let { uri ->
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
                        if (frontCnic?.let { metaData?.maxFileSize?.let { it1 ->
                                fileUtils.photoUploadValidation(it,
                                    it1
                                )
                            } }.getSafe()){
                            showFileSizeDialog()
                        }else {
                            caCnicFront.tvAddButton = mBinding.langData?.globalString?.remove.getSafe()
                                mBinding.caCnicFront.cnicUri = result?.path?.toUri()
                                buttonEnableCheck()
                            }
                        }
                   }
                    }

            }
            caCnicBack.onAddItemClick = {
                if( caCnicBack. mBinding.tvAddNew.text== mBinding.langData?.globalString?.remove.getSafe()){
                    caCnicBack.tvAddButton = mBinding.langData?.globalString?.upload.getSafe()
                    caCnicBack.addNewRes = R.drawable.ic_upload_file_primary
                    mBinding.caCnicBack.cnicUri= null
                    backCnic=null
                    buttonEnableCheck()
                }else {

                    caCnicBack.isEnabled = false
                    lifecycleScope.launch {
                        delay(2000)
                        caCnicBack.isEnabled = true
                    }
                    fileUtils.requestPermissions(requireActivity(),true) { result ->
                        if(fileUtils.hasPermissions(
                                requireContext(),
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    )
                                } else {
                                    arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.CAMERA,
                                    )
                                }
                            ).not()){
                            showAllowPermissionsDialog()
                            return@requestPermissions
                        }

                        if(result?.path.isNullOrEmpty().not()){                        backCnic = result?.path?.toUri().let { uri ->
                            caCnicBack.addNewRes = R.drawable.ic_delete_file
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
                        if (backCnic?.let { metaData?.maxFileSize?.let { it1 ->
                                fileUtils.photoUploadValidation(it,
                                    it1
                                )
                            } }.getSafe()) {
                            showFileSizeDialog()
                        }else {
                            mBinding.caCnicBack.cnicUri = result?.path?.toUri()
                            caCnicBack.tvAddButton = mBinding.langData?.globalString?.remove.getSafe()
                                buttonEnableCheck()
                            }
                        }
                    }
                }
            }
            bSubmitRequest.setOnClickListener(this@UploadCnicPictureFragment)
        }
    }

    private fun showFileSizeDialog(){
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = mBinding.langData?.globalString?.information.getSafe(),
                message = mBinding.langData?.dialogsStrings?.fileSize.getSafe(),
                buttonCallback = {},
            )
    }

    private fun buttonEnableCheck(){
        mBinding.bSubmitRequest.isEnabled =  mBinding.caCnicFront.cnicUri!= null &&  mBinding.caCnicBack.cnicUri != null
        if(mBinding.bSubmitRequest.isEnabled)
            mBinding.bSubmitRequest.text=mBinding.langData?.globalString?.save.getSafe()
        else
            mBinding.bSubmitRequest.text=mBinding.langData?.globalString?.back.getSafe()
    }

    private fun showAllowPermissionsDialog(){
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            title = mBinding.langData?.globalString?.warning.getSafe(),
            message = mBinding.langData?.dialogsStrings?.storagePermissions.getSafe(),
            negativeButtonStringText = mBinding.langData?.globalString?.cancel.getSafe(),
            positiveButtonStringText = mBinding.langData?.dialogsStrings?.permitManual.getSafe(),
            buttonCallback = {
                gotoAppSettings(requireActivity())
            }
        )
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bSubmitRequest ->{
                if(buttonText.isNotEmpty())
                    findNavController().popBackStack()
                else
                    uploadMultiParts()
            }
        }
    }

    private fun setupView() {
        buttonText = arguments?.getString("buttonText").getSafe()
        val  titleText = arguments?.getString("screenTitle") ?: mBinding.langData?.partnerProfileScreen?.uploadPicCnic.getSafe()
        mBinding.apply {
            actionbar.title=titleText
            caCnicFront.addNewRes = R.drawable.ic_upload_file_primary
            caCnicBack.addNewRes = R.drawable.ic_upload_file_primary

            if (user?.cnicFront != null && user?.cnicBack != null) {
                caCnicFront.tvAddButton = mBinding.langData?.globalString?.remove.getSafe()
                caCnicBack.tvAddButton = mBinding.langData?.globalString?.remove.getSafe()
                caCnicFront.addNewRes = R.drawable.ic_delete_file
                caCnicBack.addNewRes = R.drawable.ic_delete_file
                caCnicFront.cnicUri = user?.cnicFront?.toUri()
                caCnicBack.cnicUri = user?.cnicBack?.toUri()
            } else {
                caCnicFront.tvAddButton = mBinding.langData?.globalString?.upload.getSafe()
                caCnicBack.tvAddButton = mBinding.langData?.globalString?.upload.getSafe()
                caCnicFront.addNewRes = R.drawable.ic_upload_file_primary
                caCnicBack.addNewRes = R.drawable.ic_upload_file_primary

                val uri = requireContext().getUriFromDrawable(R.drawable.ic_placeholder)
                caCnicFront.cnicUri = uri
                caCnicBack.cnicUri = uri
            }

        }
        if (buttonText.getSafe().isNotEmpty()) {
            mBinding.bSubmitRequest.isEnabled=true
            mBinding.bSubmitRequest.text = buttonText

            mBinding.caCnicBack.mBinding.tvAddNew.setVisible(false)
            mBinding.caCnicFront.mBinding.tvAddNew.setVisible(false)
        }
    }

    private fun uploadMultiParts() {
        if ( (mBinding.caCnicBack.cnicUri!=null && backCnic != null)  || (mBinding.caCnicFront.cnicUri!=null && frontCnic != null )) {
            val frontCnic= frontCnic?.let {
                fileUtils.convertFileToMultiPart(
                    it,
                    Constants.MEDIA_TYPE_IMAGE,
                    Enums.PartnerCnic.FRONT.key
                )
            }
            val backCnic= backCnic?.let {
                fileUtils.convertFileToMultiPart(
                    it,
                    Constants.MEDIA_TYPE_IMAGE,
                    Enums.PartnerCnic.BACK.key
                )
            }
            var update ="false"
            if(backCnic == null ||  frontCnic == null)
                update="true"
            profileViewModel.storePartnerCnic(cnic_front = frontCnic, cnic_back = backCnic,update).observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            try {
                                val response = it.data as ResponseGeneral<PartnerCnicResponse>
                                val partnerCnicResponse = response.data

                                DialogUtils(requireActivity())
                                    .showSingleButtonAlertDialog(
                                        message = mBinding.langData?.partnerProfileScreen?.cnicSucces.getSafe(),
                                        buttonCallback = { findNavController().popBackStack() },
                                    )

                                val user = DataCenter.getUser()
                                user?.cnicFront = partnerCnicResponse?.cnicFront
                                user?.cnicBack = partnerCnicResponse?.cnicBack
                                if (user != null) {
                                    tinydb.putObject(com.fatron.network_module.utils.Enums.TinyDBKeys.USER.key, user)
                                }
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
        }else{
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.langData?.globalString?.error.getSafe(),
                    message = mBinding.langData?.partnerProfileScreen?.cnicFrontBackRequired.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}