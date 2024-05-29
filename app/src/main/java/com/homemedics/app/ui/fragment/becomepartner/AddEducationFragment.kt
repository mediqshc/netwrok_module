package com.homemedics.app.ui.fragment.becomepartner

import android.net.Uri
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.partnerprofile.EducationRequest
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentEducationBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import okhttp3.MultipartBody
import java.io.File


class AddEducationFragment : BaseFragment(), View.OnClickListener {

    private val educationRequest = EducationRequest()
    private lateinit var mBinding: FragmentEducationBinding
    private lateinit var fileUtils: FileUtils
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.partnerProfileScreen?.addEducation.getSafe()
            etDegree.hint = langData?.partnerProfileScreen?.degree.getSafe()
            etInstitute.hint = langData?.partnerProfileScreen?.institute.getSafe()
            etCountry.hint = langData?.globalString?.country.getSafe()
            etYear.hint = langData?.globalString?.year.getSafe()
            caDocuments.title = langData?.globalString?.documents.getSafe()
        }
    }

    override fun init() { profileViewModel. isFromMenu=0
        mBinding.educationModel = educationRequest
        dropdownList()
        mBinding.  caDocuments.addNewRes=R.drawable.ic_upload_file_primary
        fileUtils = FileUtils()
        fileUtils.init(this)
    }

    override fun getFragmentLayout() = R.layout.fragment_education

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentEducationBinding
    }

    override fun setListeners() {
        val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            caDocuments.onAddItemClick = {
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
                        profileViewModel.educationDocument.add(MultipleViewItem(title = result?.uri?.let {
                            fileUtils.getFileNameFromUri(
                                requireContext(),
                                it
                            )
                        }, itemId = result?.path.toString(), drawable = R.drawable.ic_document).apply {
                            type = result?.uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }
                            isRTL = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
                        })
                        addEducationList()
                    }
                }
            }
            etYear.clickCallback = {
                etYear.textColorCheck = true
                DialogUtils(requireActivity()).showYearAlertDialog { year ->
                    etYear.text = year
                    etYear.mBinding.editText.setTextColor(
                        resources.getColor(
                            R.color.black,
                            null
                        )
                    )
                }
            }

            bSave.setOnClickListener(this@AddEducationFragment)
            etDegree.mBinding.editText.addTextChangedListener { enableButton() }
            etInstitute.mBinding.editText.addTextChangedListener { enableButton() }
            etYear.mBinding.editText.addTextChangedListener { enableButton() }
        }
    }

    private fun enableButton() {
        mBinding.apply {
            bSave.isEnabled = etDegree.text.isNotEmpty() && etInstitute.text.isNotEmpty() &&
                    etCountry.mBinding.dropdownMenu.text.isNotEmpty() && etYear.text.isNotEmpty()
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

    private fun uploadMultiParts() {
        val mediaList = ArrayList<MultipartBody.Part>()
        profileViewModel.educationDocument.forEachIndexed { index, item ->
var type=fileUtils.getMimeType(requireContext(), Uri.parse(item.itemId)).getSafe()
    if(type == "")
           type= item.type.getSafe()
            val multipartFile = fileUtils.convertFileToMultiPart(
                File(item.itemId.getSafe()),
                type  ,
                "education_documents[]"
            )

            mediaList.add(multipartFile)
        }

                profileViewModel.storeEducation(
                    educationDocument = mediaList,
                    educationRequest = educationRequest
                ).observe(this) {
                    if (isOnline(activity)) {
                        when (it) {
                            is ResponseResult.Success -> {
                                hideLoader()
                                showToast(mBinding.langData?.messages?.education_created.getSafe())
                                try {
                                    findNavController().popBackStack()
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
                                        message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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


    }
    private fun addEducationList() {
        mBinding.caDocuments.listItems = profileViewModel.educationDocument
        mBinding.caDocuments.onDeleteClick = { item, pos ->

            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    message = mBinding.langData?.messages?.doc_delete_msg.getSafe(),
                    buttonCallback = {
                        profileViewModel.educationDocument.remove(item)
                        mBinding.caDocuments.listItems = profileViewModel.educationDocument
                    },
                )
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bSave -> addEducation()
        }
    }

    private fun dropdownList() {
        val countryList =metaData?.allCountries?.map { it.name } as ArrayList<String>?

        mBinding.etCountry.data = countryList as ArrayList<String>
        educationRequest.countryId = metaData?.allCountries?.get(0)?.id.getSafe()

        mBinding.etCountry.onItemSelectedListener = { _, position: Int ->
            educationRequest.countryId = metaData?.allCountries?.get(position)?.id.getSafe()
        }

    }

    private fun addEducation() {
        val degree = educationRequest.degree
        val institute = educationRequest.school
        val year = educationRequest.year
        mBinding.apply {
            if (isValid(degree).not()) {
                etDegree.errorText = mBinding.langData?.fieldValidationStrings?.degreeEmpty.getSafe()
                etDegree.requestFocus()
                return
            }
            if (isValid(institute).not()) {
                etInstitute.errorText = mBinding.langData?.fieldValidationStrings?.instituteEmpty.getSafe()
                etInstitute.requestFocus()
                return
            }
            if (isValid(year).not()) {
                etYear.errorText = mBinding.langData?.fieldValidationStrings?.yearEmpty.getSafe()
                etYear.requestFocus()
                return
            }
            if (profileViewModel.educationDocument.isEmpty()) {
                showToast(mBinding.langData?.fieldValidationStrings?.uploadDocument.getSafe())
                return
            }

        }
        uploadMultiParts()
    }
}