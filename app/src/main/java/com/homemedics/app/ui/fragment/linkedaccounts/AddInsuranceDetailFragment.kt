package com.homemedics.app.ui.fragment.linkedaccounts

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.linkaccount.LinkInsuranceRequest
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddInsuranceDetailsBinding
import com.homemedics.app.utils.*
import com.homemedics.app.utils.DataCenter.cnicMaskListener
import com.homemedics.app.viewmodel.ProfileViewModel

class AddInsuranceDetailFragment : BaseFragment(), View.OnClickListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentAddInsuranceDetailsBinding
    private var requiredFields = ArrayList<String>()
    private val langData = ApplicationClass.mGlobalData
    override fun setLanguageData() {
        mBinding.apply {
            bLink.text = langData?.linkedAccountScreen?.buttonLink
            actionbar.title = langData?.linkedAccountScreen?.addInsuranceCompany.getSafe()
            etName.hint = langData?.globalString?.name.getSafe()
            etCnicNumber.hint = langData?.globalString?.cnicNumber.getSafe()
            etCertificateId.hint = langData?.linkedAccountScreen?.certificateId.getSafe()
            etPolicyNumber.hint = langData?.linkedAccountScreen?.policyNumber.getSafe()
        }
    }

    override fun init() {
        setRelativeFields()


        mBinding.apply {
            etCnicNumber.mBinding.editText.addTextChangedListener(cnicMaskListener)
            multipleViewItem = profileViewModel.selectedInsurance
            iCompany.rbCompany.setVisible(false)
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_add_insurance_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddInsuranceDetailsBinding
    }

    private fun setRelativeFields() {
        val result =
            profileViewModel.insuranceDataFields.find { it.insuranceId == profileViewModel.selectedInsurance?.itemId?.toInt() }

        result?.let {
            requiredFields = it.fields as ArrayList<String>
            it.fields?.forEach { tag ->
                mBinding.root.findViewWithTag<View>(tag)?.setVisible(true)
            }
        }
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            bLink.setOnClickListener {
                if (isValid(etPolicyNumber.text).not() && requiredFields.contains(etPolicyNumber.tag)) {
                    etPolicyNumber.errorText =  langData?.fieldValidationStrings?.errorPolicyNumber
                    etPolicyNumber.requestFocus()
                    return@setOnClickListener
                }
                if (isValid(etCertificateId.text).not() && requiredFields.contains(etCertificateId.tag)) {
                    etCertificateId.errorText = langData?.fieldValidationStrings?.errorCertificateId
                    etCertificateId.requestFocus()
                    return@setOnClickListener
                }
                if (isValid(etName.text).not() && requiredFields.contains(etName.tag)) {
                    etName.errorText =  langData?.fieldValidationStrings?.nameValidation
                    etName.requestFocus()
                    return@setOnClickListener
                }
                if (isValidCnicLength(etCnicNumber.text.getSafe()).not() && requiredFields.contains(
                        etCnicNumber.tag
                    )
                ) {
                    etCnicNumber.errorText =  langData?.fieldValidationStrings?.errorCNIC
                    etCnicNumber.requestFocus()
                    return@setOnClickListener
                }

                val request = LinkInsuranceRequest(
                    insuranceId = profileViewModel.selectedInsurance?.itemId,
                    policy = etPolicyNumber.text,
                    certId = etCertificateId.text,
                    patientName = etName.text
                )
                linkApi(request)
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun linkApi(request: LinkInsuranceRequest) {
        if (isOnline(requireActivity())) {
            profileViewModel.linkInsuranceComp(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        findNavController().popBackStack(R.id.selectInsuranceCompanyFragment, true)
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
                                title =  langData?.globalString?.information.getSafe(),
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
                    title =  langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}