package com.homemedics.app.ui.fragment.medicalrecords.patient.reports

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.type.GenericEMRListResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectLabTestBinding
import com.homemedics.app.ui.adapter.LabTestsAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SelectLabTestFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentSelectLabTestBinding
    private lateinit var adapter: LabTestsAdapter
    private val emrViewModel: EMRViewModel by activityViewModels()
    private var delay: Long = 1000 // 1 seconds after user stops typing
    private var taskJob: Job? = null
    private var lastTextEdit: Long = 0
    private var fromFilter = false

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.selectLabTest.getSafe()
            etSearch.hint = lang?.emrScreens?.searchByLabDiagnostic.getSafe()
        }
    }

    override fun init() {
        populateList()
        val request = PageRequest(page = 1)
        getLabTests(request)
    }

    override fun getFragmentLayout() = R.layout.fragment_select_lab_test

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectLabTestBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }

            adapter.onCheckChange = {
                bSelect.isEnabled = adapter.getSelectedItem() != null
                adapter.getSelectedItem()?.let {
                    emrViewModel.selectedRecord = adapter.getSelectedItem()
                }
            }

            etSearch.doAfterTextChanged { s ->
                taskJob?.cancel()
                taskJob = lifecycleScope.launch {
                    if (s?.length.getSafe() > 0) {
                        lastTextEdit = System.currentTimeMillis()
                        delay(delay)
                        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
                            adapter.listItems.clear()
                            fromFilter = true
                            val request = PageRequest(page = 0)
                            val queryText = mBinding.etSearch.text.toString()
                            if(queryText.isNotEmpty())
                                request.displayName = queryText
                            else {
                                request.displayName = null
                            }
                            getLabTests(request)
                        }
                    } else {
                        lastTextEdit = 0
                        adapter.listItems.clear()
                        adapter.listItems = arrayListOf()
//                        tvNoData.visible()
                        fromFilter = false
                        getLabTests(PageRequest(page = 1))
                    }

                }
            }

            bSelect.setOnClickListener {
                findNavController().safeNavigate(SelectLabTestFragmentDirections.actionSelectLabTestFragmentToAddReportRecordDocumentsFragment())
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private var loading = false
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var isLastPage = false
    var currentPage: Int? = 1

    private fun populateList(){
        mBinding.apply {
            adapter = LabTestsAdapter()
            mBinding.rvList.adapter = adapter
            val layoutManager = rvList.layoutManager as LinearLayoutManager

            rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) { //check for scroll down
                        visibleItemCount = layoutManager.childCount
                        totalItemCount = layoutManager.itemCount
                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                        if (loading.not()) {
                            if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                                emrViewModel.emrFilterRequest.page = currentPage?.plus(1)
                                if(isLastPage.not()){
                                    if (fromFilter.not()) {
                                        getLabTests(PageRequest(page = emrViewModel.emrFilterRequest.page))
                                        loading = true
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun getLabTests(pageRequest: PageRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.getLabTests(pageRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<GenericEMRListResponse>
                        response.data?.let { res ->
                            isLastPage = pageRequest.page == res.lastPage
                            currentPage = res.currentPage

                            val tempList = adapter.listItems
                            tempList.addAll(res.labTests?.data as ArrayList<GenericItem>?  ?: arrayListOf())
                            adapter.listItems = tempList.distinctBy {
                                it.genericItemId
                            } as ArrayList<GenericItem>
                            adapter.originalList = adapter.listItems
                            adapter.notifyDataSetChanged()
                            mBinding.tvNoData.setVisible(adapter.listItems.isEmpty())
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

    override fun onDetach() {
        super.onDetach()
        fromFilter = false
    }
}