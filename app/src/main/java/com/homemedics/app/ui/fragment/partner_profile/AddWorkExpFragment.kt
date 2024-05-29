package com.homemedics.app.ui.fragment.partner_profile

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.partnerprofile.WorkExperience
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddWorkExpBinding
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddWorkExpFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentAddWorkExpBinding
    private val viewModel: ProfileViewModel by activityViewModels()
    private val workExp = WorkExperience()

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            customActionbar.title = langData?.partnerProfileScreen?.workTitle.getSafe()
            etCompany.hint = langData?.globalString?.company.getSafe()
            etDesignation.hint = langData?.partnerProfileScreen?.designation.getSafe()
            startDate.hint = langData?.globalString?.startDate.getSafe()
            endDate.hint = langData?.globalString?.endDate.getSafe()
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_add_work_exp

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddWorkExpBinding
        mBinding.lifecycleOwner = this
        mBinding.workItem = workExp
    }

    override fun setListeners() {
        mBinding.apply {
            customActionbar.onAction2Click = {
                requireActivity().onBackPressed()
            }
            bSave.setOnClickListener {
                addContact()
            }
            startDate.clickCallback = {
                openCalender(mBinding.startDate.mBinding.editText)
            }

            endDate.clickCallback = {
                openCalender(mBinding.endDate.mBinding.editText)
            }
            etCompany.mBinding.editText.doAfterTextChanged {
                bSave.isEnabled = etCompany.text.isNotEmpty() && etDesignation.text.isNotEmpty()
            }
            etDesignation.mBinding.editText.doAfterTextChanged {
                bSave.isEnabled = etDesignation.text.isNotEmpty() && etCompany.text.isNotEmpty()
            }
        }
    }

    private fun addContact() {
        mBinding.apply {
            if (workExp.company.isEmpty()) {
                etCompany.errorText =  langData?.fieldValidationStrings?.companyEmpty
                etCompany.requestFocus()
                return
            }
            if (workExp.designation.isNullOrEmpty()) {
                etDesignation.errorText =langData?.fieldValidationStrings?.designEmpty

                etDesignation.requestFocus()
                return
            }
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            if (workExp.endDate.getSafe().isNotEmpty() && workExp.startDate.getSafe()
                    .isNotEmpty()
            ) {
                val endDate: Date = sdf.parse(workExp.endDate)
                val startDate: Date = sdf.parse(workExp.startDate)
                if (endDate.after(startDate).not()) {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = langData?.fieldValidationStrings?.dateSmaller.getSafe(),
                            buttonCallback = {

                            },
                        )
                    return

                }
            }
            if(workExp.endDate.getSafe().isEmpty() && workExp.startDate.getSafe().isEmpty().not() || workExp.endDate.getSafe().isEmpty().not() && workExp.startDate.getSafe().isEmpty())
                    {
                DialogUtils(requireActivity())
                    .showSingleButtonAlertDialog(
                        message = mBinding.langData?.fieldValidationStrings?.dateFilledError.getSafe(),
                        buttonCallback = {

                        },
                    )
                return
            }
         }

        addWorkExpApi()


    }
    private fun addWorkExpApi(){
        val workExps = WorkExperience()

     val startDate = getDateInFormat(workExp.startDate.getSafe(), "dd/MM/yyyy", "yyyy-MM-dd")
     val endDate = getDateInFormat(workExp.endDate.getSafe(), "dd/MM/yyyy", "yyyy-MM-dd")

        workExp.apply {
            workExps.company = company
            workExps.designation = designation
            workExps.yearsOfExperience = null
            workExps.endDate = endDate
            workExps.startDate = startDate
            workExps.isSelected=null
            workExps. itemId = null
            workExps. title = null
            workExps. desc = null
            workExps. imageUrl = null
            workExps. drawable  = null
            workExps. type = null
            workExps. isSelected = null
            workExps. hasRoundLargeIcon = null
        }

        if (isOnline(requireActivity())) {
            viewModel.callAddWorkExpApi(workExps).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        showToast(mBinding.langData?.messages?.work_exp_added.getSafe())
                        findNavController().popBackStack()


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
                    message =mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    override fun init() {

    }

    override fun onClick(view: View?) {

    }
}