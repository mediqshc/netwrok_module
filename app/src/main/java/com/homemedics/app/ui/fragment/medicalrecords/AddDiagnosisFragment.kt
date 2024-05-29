package com.homemedics.app.ui.fragment.medicalrecords

import android.app.AlertDialog
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.request.emr.type.StoreEMRTypeRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.type.GenericEMRListResponse
import com.fatron.network_module.models.response.emr.type.StoreEMRTypeResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogAddEmrBinding
import com.homemedics.app.databinding.FragmentAddEmrGenericBinding
import com.homemedics.app.ui.adapter.EMRGenericItemAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import timber.log.Timber

class AddDiagnosisFragment : BaseFragment(), View.OnClickListener {

    private val emrViewModel: EMRViewModel by activityViewModels()

    private lateinit var mBinding: FragmentAddEmrGenericBinding

    private lateinit var emrAdapter: EMRGenericItemAdapter

    private lateinit var dialogAddEmrBinding: DialogAddEmrBinding
    private lateinit var builder: AlertDialog
    private lateinit var dialogSaveButton: Button

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.addDiagnosis.getSafe()
            etSearch.hint = lang?.emrScreens?.searchByName.getSafe()
        }
    }

    override fun init() {
        populateSymptomsList()

        val request = PageRequest(page = 1, emrId = emrViewModel.emrID)
        getDiagnosisList(request)
    }

    override fun getFragmentLayout() = R.layout.fragment_add_emr_generic

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddEmrGenericBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            etSearch.addTextChangedListener {
                emrAdapter.filter.filter(it)
            }
            emrAdapter.itemClickListener = { item, _ ->
                showAddEmrBindingDialog(item)
            }
            emrAdapter.onDataFilter = { items ->
                rvEMRGeneric.setVisible(items.isNotEmpty())
                tvNoData.setVisible(items.isEmpty())
            }
            bCreate.setOnClickListener(this@AddDiagnosisFragment)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bCreate -> {
                showAddEmrBindingDialog()
            }
        }
    }

    private var loading = false
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var isLastPage = false
    var currentPage: Int? = 1

    private fun populateSymptomsList() {
        emrAdapter = EMRGenericItemAdapter()
        mBinding.apply {
            rvEMRGeneric.adapter = emrAdapter
            val layoutManager = rvEMRGeneric.layoutManager as LinearLayoutManager

            rvEMRGeneric.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    if (dy > 0) { //check for scroll down
                        visibleItemCount = layoutManager.childCount
                        totalItemCount = layoutManager.itemCount
                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                        if (loading.not()) {
                            if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
//                                loading = false
                                Timber.e("Last Item Wow !")

                                emrViewModel.emrFilterRequest.page = currentPage?.plus(1)

                                if(isLastPage.not()){
                                    getDiagnosisList(PageRequest(page = emrViewModel.emrFilterRequest.page))
                                    loading = true
                                }
                            }
                        }
                    }
                }
            })
        }
    }


    private fun showAddEmrBindingDialog(item: GenericItem? = null) {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogAddEmrBinding = DialogAddEmrBinding.inflate(layoutInflater)
            if (item?.genericItemName != null || item?.description != null) {
                dialogAddEmrBinding.apply {
                    etTitle.apply {
                        text = item.genericItemName.getSafe()
                        mBinding.editText.apply {
                            isEnabled = false
                            isClickable = false
                            setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.grey
                                )
                            )
                        }
                    }
                    etDescription.setText(item.description.getSafe())
                }
            }
            dialogAddEmrBinding.apply {
                etTitle.hint=mBinding.lang?.emrScreens?.symptomTitle.getSafe()
                textInputLayout.hint=mBinding.lang?.emrScreens?.descriptions.getSafe()
                etTitle.mBinding.editText.doAfterTextChanged {
                    dialogSaveButton.isEnabled = isValid(etTitle.mBinding.editText.text.toString())
                }
            }
            setView(dialogAddEmrBinding.root)
            setTitle(mBinding.lang?.emrScreens?.addDiagnosis.getSafe())
            setPositiveButton(mBinding.lang?.globalString?.add.getSafe()) { _, _ ->

            }
            setNegativeButton(mBinding.lang?.globalString?.cancel.getSafe(), null)

        }.create()

        builder.setOnShowListener{
            dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val detail = dialogAddEmrBinding.etDescription.text
                val type = metaData?.emrTypes?.find { it.genericItemId == Enums.EMRTypesMeta.DIAGNOSIS.key }?.genericItemId
                val emrId = emrViewModel.emrID
                val typeId = item?.genericItemId
                val name = if (item?.genericItemName != null) item.genericItemName else dialogAddEmrBinding.etTitle.text


                val storeEMRRequest = StoreEMRTypeRequest(
                    description = detail.toString(),
                    emrId = emrId.toString(),
                    name = name,
                    type = type.toString(),
                    typeId = typeId?.toString()
                )
                storeEMR(storeEMRRequest)
            }
            dialogSaveButton.isEnabled = isValid(dialogAddEmrBinding.etTitle.text)

        }
        builder.show()
    }

    private fun getDiagnosisList(pageRequest: PageRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.getDiagnosisList(pageRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<GenericEMRListResponse>
                        response.data?.let { res ->
                            isLastPage = pageRequest.page == res.medicalHealthCares?.lastPage
                            currentPage = res.medicalHealthCares?.currentPage

                            val tempList = emrAdapter.listItems
                            tempList.addAll(res.diagnosis?.data as ArrayList<GenericItem>?  ?: arrayListOf())
                            emrAdapter.listItems = tempList.distinctBy {
                                it.genericItemId
                            } as ArrayList<GenericItem>
                            emrAdapter.originalList = emrAdapter.listItems
                            emrAdapter.notifyDataSetChanged()

                            mBinding.rvEMRGeneric.setVisible(emrAdapter.listItems.isNotEmpty())
                            mBinding.tvNoData.setVisible(emrAdapter.listItems.isEmpty())

                            loading = false
                        }
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

    private fun storeEMR(storeEMRTypeRequest: StoreEMRTypeRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.storeEMRType(storeEMRTypeRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<StoreEMRTypeResponse>
                        response.data?.let { storeEMRResponse ->
                            emrViewModel.storeEMR = storeEMRResponse
                            emrViewModel.isDraft = false
                            findNavController().popBackStack()
                            builder.dismiss()
                        }
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