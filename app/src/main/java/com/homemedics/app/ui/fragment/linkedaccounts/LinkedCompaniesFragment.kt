package com.homemedics.app.ui.fragment.linkedaccounts

import android.app.AlertDialog
import android.view.View
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.email.EmailSendRequest
import com.fatron.network_module.models.request.email.EmailVerifyRequest
import com.fatron.network_module.models.request.linkaccount.LinkCompanyRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.linkaccount.CompanyListResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogVerifyEmailBinding
import com.homemedics.app.databinding.FragmentLinkedCompanyBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.adapter.AddMyCompanyAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlin.properties.Delegates

class LinkedCompaniesFragment : BaseFragment(), View.OnClickListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentLinkedCompanyBinding
    private var lang: RemoteConfigLanguage? = null
    private val companyAdapter = AddMyCompanyAdapter()
    private var preSelected = -1

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            lang = langData
            actionbar.title = lang?.linkedAccountScreen?.linkYourCompany.getSafe()
            etSearch.hint = lang?.globalString?.search.getSafe()
            tvNoData.text = lang?.globalString?.noResultFound.getSafe()
            etEnterEmail.hint = lang?.linkedAccountScreen?.enterCompanyEmail.getSafe()
        }
    }

    override fun init() {
        instantiateMyCompaniesList()
        getCompaniesListApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_linked_company

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentLinkedCompanyBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            etSearch.addTextChangedListener {
                companyAdapter.filter.filter(it)
            }
            companyAdapter.apply {
                var selectedPosition by Delegates.observable(-1) { _, oldPos, newPos ->
                    val temperedOldPos = if(oldPos == -1) preSelected else oldPos
                    if (temperedOldPos != newPos) {
                        if (newPos in listItems.indices) {
                            listItems[newPos].isSelected = true
                            if (temperedOldPos != -1) {
                                listItems[temperedOldPos].isSelected = false
                                notifyItemChanged(temperedOldPos)
                            }
                            notifyItemChanged(newPos)
                        }
                    }
                }
                onItemSelected = { _, pos->
                    selectedPosition = pos
                    bLinkNew.isEnabled = getSelectedItem() != null
                }
            }
            etEnterEmail.doOnTextChanged { text, _, _, _ ->
                bVerifyEmail.isEnabled = isEmailValid(text.toString())
            }
            bLinkNew.setOnClickListener(this@LinkedCompaniesFragment)
            bVerifyEmail.setOnClickListener(this@LinkedCompaniesFragment)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bLinkNew -> {
                val companyId = (companyAdapter.getSelectedItem())
                val request = LinkCompanyRequest(companyId?.genericItemId.toString())
                linkApi(request)
            }
            R.id.bVerifyEmail -> {
                val email = mBinding.etEnterEmail.text.toString()
                val request = EmailSendRequest(email = email)
                verifyCompaniesEmail(request)
            }
        }
    }

    private fun instantiateMyCompaniesList() {
        mBinding.rvMyCompany.adapter = companyAdapter
        companyAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkEmpty()
            }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmpty()
            }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmpty()
            }
            fun checkEmpty() {
                mBinding.tvNoData.setVisible((companyAdapter.itemCount == 0))
                mBinding.rvMyCompany.setVisible((companyAdapter.itemCount != 0))
            }
        })
    }

    private lateinit var dialogVerifyEmailBinding: DialogVerifyEmailBinding
    private lateinit var dialogSaveButton: Button
    private lateinit var builder: AlertDialog

    private fun showVerifyEmailDialog() {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogVerifyEmailBinding = DialogVerifyEmailBinding.inflate(layoutInflater).apply {
                language = mBinding.langData
                tvEmail.text = mBinding.etEnterEmail.text
                etVerificationEmail.apply {
                    hint = language?.linkedAccountScreen?.enterVerificationCode.getSafe()
                    mBinding.editText.doAfterTextChanged {
                        dialogSaveButton.isEnabled = isValid(etVerificationEmail.text)
                    }
                }
            }
            setView(dialogVerifyEmailBinding.root)
            setTitle(mBinding.langData?.linkedAccountScreen?.verifyYourAction.getSafe())
            setPositiveButton(mBinding.langData?.globalString?.verify.getSafe()) { _, _ ->

            }
            setNegativeButton(mBinding.langData?.globalString?.cancel.getSafe(), null)
        }.create()

        builder.setOnShowListener {
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val email = dialogVerifyEmailBinding.tvEmail.text.toString()
                val code = dialogVerifyEmailBinding.etVerificationEmail.text
                val request = EmailVerifyRequest(
                    email = email,
                    code = code
                )
                companiesVerificationCode(request)
                builder.dismiss()
            }
            dialogSaveButton.isEnabled = false
        }
        builder.show()
    }

    private fun getCompaniesListApi() {
        if (isOnline(requireActivity())) {
            profileViewModel.getCooperateCompList().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<CompanyListResponse>
                        val list = (response.data?.companies as ArrayList<GenericItem>?)
                        list?.let { listItems ->
                            companyAdapter.listItems = listItems
                            companyAdapter.originalList = listItems
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = lang?.globalString?.information.getSafe(),
                                message =it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        mBinding.tvNoData.setText(getErrorMessage(it.generalResponse.message.getSafe()).getSafe())
                        mBinding.tvNoData.setVisible(true)
                        mBinding.rvMyCompany.setVisible(false)
//                        DialogUtils(requireActivity())
//                            .showSingleButtonAlertDialog(
//                                title = lang?.globalString?.information.getSafe(),
//                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
//                                cancellable = false,
//                                buttonCallback = {
//                                    findNavController().popBackStack()
//                                },
//                            )
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
                    title = lang?.errorMessages?.internetError.getSafe(),
                    message =lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun linkApi(request: LinkCompanyRequest){
        if (isOnline(requireActivity())) {
            profileViewModel.linkCompany(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        findNavController().popBackStack()
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
                    else -> { hideLoader() }
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

    private fun verifyCompaniesEmail(request: EmailSendRequest) {
        if (isOnline(requireActivity())) {
            profileViewModel.verifyCompaniesEmail(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        showVerifyEmailDialog()
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
                    else -> { hideLoader() }
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

    private fun companiesVerificationCode(request: EmailVerifyRequest) {
        if (isOnline(requireActivity())) {
            profileViewModel.companiesVerificationCode(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
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
                    else -> { hideLoader() }
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