package com.homemedics.app.ui.fragment.medicalrecords

import android.view.View
import androidx.core.widget.addTextChangedListener
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
import com.homemedics.app.databinding.FragmentAddEmrGenericBinding
import com.homemedics.app.ui.adapter.EMRGenericItemAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class AddMedicinesFragment : BaseFragment(), View.OnClickListener {

    private val emrViewModel: EMRViewModel by activityViewModels()

    private lateinit var mBinding: FragmentAddEmrGenericBinding

    private lateinit var emrAdapter: EMRGenericItemAdapter

    private var delay: Long = 1000 // 1 seconds after user stops typing
    private var taskJob: Job? = null
    private var lastTextEdit: Long = 0
    private var fromFilter = false

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.addMedicine.getSafe()
            etSearch.hint = lang?.emrScreens?.searchByName.getSafe()
        }
    }

    override fun init() {
        populateSymptomsList()

        val request = PageRequest(page = 1, emrId = emrViewModel.emrID)
        getMedicines(request)
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
            etSearch.doAfterTextChanged { s ->
                taskJob?.cancel()
                taskJob = lifecycleScope.launch {
                    if (s?.length.getSafe() > 0) {
                        lastTextEdit = System.currentTimeMillis()
                        delay(delay)
                        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
                            emrAdapter.listItems.clear()
                            fromFilter = true
                            val request = PageRequest(page = 0, emrId = emrViewModel.emrID)
                            val queryText = mBinding.etSearch.text.toString()
                            if(queryText.isNotEmpty())
                                request.displayName = queryText
                            else {
                                request.displayName = null
                            }
                            getMedicines(request)
                        }
                    } else {
                        lastTextEdit = 0
                        emrAdapter.listItems.clear()
                        emrAdapter.listItems = arrayListOf()
                        fromFilter = false
                        getMedicines(PageRequest(page = 1, emrId = emrViewModel.emrID))
                    }
                }
            }
            emrAdapter.itemClickListener = { item, _ ->
                emrViewModel.medicine = item
                emrViewModel.isEdit = false
                findNavController().safeNavigate(
                    AddMedicinesFragmentDirections.actionAddMedicinesFragmentToEnterDetailsFragment()
                )
            }
            emrAdapter.onDataFilter = { items ->
                rvEMRGeneric.setVisible(items.isNotEmpty())
                tvNoData.setVisible(items.isEmpty())
            }
            bCreate.setOnClickListener(this@AddMedicinesFragment)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bCreate -> {
                emrViewModel.isEdit = false
                emrViewModel.medicine = GenericItem()
                findNavController().safeNavigate(
                    AddMedicinesFragmentDirections.actionAddMedicinesFragmentToEnterDetailsFragment()
                )
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
                                    if (fromFilter.not()) {
                                        getMedicines(PageRequest(page = emrViewModel.emrFilterRequest.page, emrId = emrViewModel.emrID))
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

    override fun onDetach() {
        super.onDetach()
        fromFilter = false
    }

    private fun getMedicines(pageRequest: PageRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.getMedicineList(pageRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<GenericEMRListResponse>
                        response.data?.let { res ->
                            isLastPage = pageRequest.page == res.products?.lastPage
                            currentPage = res.products?.currentPage

                            val tempList = emrAdapter.listItems
                            tempList.addAll(res.products?.data as ArrayList<GenericItem>?  ?: arrayListOf())
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
}