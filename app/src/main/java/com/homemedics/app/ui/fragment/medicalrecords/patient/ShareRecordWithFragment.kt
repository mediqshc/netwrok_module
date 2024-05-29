package com.homemedics.app.ui.fragment.medicalrecords.patient

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.emr.EMRShareWithRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.family.FamilyResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentShareRecordWithBinding
import com.homemedics.app.ui.adapter.ShareRecordWithAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class ShareRecordWithFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentShareRecordWithBinding
    private lateinit var listAdapter: ShareRecordWithAdapter
    private val emrViewModel: EMRViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.shareRecordWith.getSafe()
            etSearch.hint = lang?.globalString?.searchByName.getSafe()
        }
    }

    override fun init() {
        populateList()
        getFamilyConnections()
    }

    override fun getFragmentLayout() = R.layout.fragment_share_record_with

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentShareRecordWithBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().navigateUp()
            }

            bSave.setOnClickListener {
                emrViewModel.selectedFamilyForShare = listAdapter.getSelectedItems()
                val request = EMRShareWithRequest(
                    emrId = emrViewModel.tempEmrID,
                    shareWith = listAdapter.getSelectedItems().map { it.familyMemberId.getSafe() }
                )
                shareRecordApi(request)
            }

            etSearch.addTextChangedListener {
                listAdapter.filter.filter(it)
            }

            listAdapter.itemClickListener = {
                item, _ ->

            }
            listAdapter.onDataFilter = { items ->
                rvList.setVisible(items.isNotEmpty())
                tvNoData.setVisible(items.isEmpty())
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun populateList() {
        mBinding.apply {
            listAdapter = ShareRecordWithAdapter()
            listAdapter.onCheckChange = {
//                bSave.isEnabled = listAdapter.getSelectedItems().isNotEmpty()
            }
            rvList.adapter = listAdapter
            listAdapter.lang = ApplicationClass.mGlobalData
        }
    }

    private fun getFamilyConnections(){
        if (isOnline(requireActivity())) {
            emrViewModel.getFamilyConnectionsApiCall(true).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<FamilyResponse>
                        response.data?.let {
                            val toShowEMR = (it.connected as ArrayList<FamilyConnection>).filter { it.toShowInEMRShare == 1 }
                            listAdapter.listItems =
                                toShowEMR.map { item ->
                                    item.isSelected = emrViewModel.selectedFamilyForShare.find { it.id ==  item.familyMemberId} != null
                                    item
                                } as ArrayList<FamilyConnection>
                            listAdapter.originalList = listAdapter.listItems

                            mBinding.rvList.setVisible(listAdapter.listItems.isEmpty().not().getSafe())
                            mBinding.tvNoData.setVisible(listAdapter.listItems.isEmpty().getSafe() )

//                            mBinding.bSave.isEnabled = listAdapter.getSelectedItems().isNotEmpty()
                        }
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

    private fun shareRecordApi(request: EMRShareWithRequest){
        if (isOnline(requireActivity())) {
            emrViewModel.customerEMRRecordShare(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        findNavController().navigateUp()
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