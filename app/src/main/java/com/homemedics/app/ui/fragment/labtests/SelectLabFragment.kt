package com.homemedics.app.ui.fragment.labtests

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.labtest.LabBranchListRequest
import com.fatron.network_module.models.request.labtest.LabTestCartRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.labtest.LabBranchListResponse
import com.fatron.network_module.models.response.labtest.LabResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectLabBinding
import com.homemedics.app.ui.adapter.SelectLabAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.LabTestViewModel

class SelectLabFragment : BaseFragment(), View.OnClickListener {

    private val labTestViewModel: LabTestViewModel by activityViewModels()
    private lateinit var mBinding: FragmentSelectLabBinding
    private val adapter = SelectLabAdapter()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.selectLabBranch.getSafe()
            etSearch.hint = lang?.labPharmacyScreen?.searchLabByName.getSafe()
        }
    }

    override fun init() {
        mBinding.rvMyCompany.adapter = adapter
        getListApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_select_lab

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectLabBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            etSearch.addTextChangedListener {
                adapter.filter.filter(it)
            }

            adapter.apply {
                itemClickListener = { _, pos ->
//                    val index = listItems.indexOfFirst { it.isSelected == true }
//                    if (index != -1) {
//                        listItems[index].isSelected = false
//                        notifyItemChanged(index)
//                    }
////                    listItems.map { it.isSelected = false }
//                    listItems[pos].isSelected = true
//
//                    notifyItemChanged(pos)

                    labTestViewModel.selectedLabBranch = getSelectedItem()
                    bSelect.isEnabled = getSelectedItem() != null
                }
            }

            bSelect.setOnClickListener {
//                labTestViewModel.selectedLabBranch = (adapter.getSelectedItem())
//                labTestViewModel.bookLabTestRequest.labId = labTestViewModel.selectedLabBranch?.id
//                findNavController().popBackStack()


                labTestViewModel.bookConsultationRequest.preferredLaboratory =
                    labTestViewModel.selectedLabBranch?.id

                if (labTestViewModel.fromBLT) {
                    findNavController().popBackStack(R.id.selectMainLabFragment, true)
                } else {
                    val request = LabTestCartRequest(
                        bookingId = labTestViewModel.bookingIdResponse.bookingId,
                        labTestId = labTestViewModel.selectedLabTest?.id,
                        labId = labTestViewModel.selectedMainLab?.id,
                        branchId = labTestViewModel.selectedLabBranch?.id
                    )

                    addToCardApi(request)
                }
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun getListApi() {
        val request = LabBranchListRequest(
            labId = labTestViewModel.selectedMainLab?.id,
            cityId = labTestViewModel.labTestFilterRequest.cityId
        )

        if (isOnline(requireActivity())) {
            labTestViewModel.getLabBranchList(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<LabBranchListResponse>

                        val list = (response.data?.labBranches as ArrayList<LabResponse>?)
//                        list?.get(0)?.isSelected = true
                        list?.let { listItems ->
                            adapter.listItems = listItems
                            adapter.originalList = listItems
                        }

                        mBinding.bSelect.isEnabled =
                            adapter.listItems.find { it.isSelected.getSafe() } != null
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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

    private fun addToCardApi(request: LabTestCartRequest) {
        if (isOnline(requireActivity())) {
            labTestViewModel.labTestAddToCart(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                            mBinding.lang?.globalString?.information.getSafe(),
                            mBinding.lang?.dialogsStrings?.testAddedSuccessfully.getSafe(),
                            negativeButtonStringText = mBinding.lang?.globalString?.viewCart.getSafe(),
                            positiveButtonStringText = mBinding.lang?.globalString?.addMoreTests.getSafe(),
                            negativeButtonCallback = {
                                findNavController().safeNavigate(SelectLabFragmentDirections.actionSelectLabFragmentToLabTestCartDetailsFragment())
                            },
                            buttonCallback = {
                                findNavController().popBackStack(R.id.selectMainLabFragment, true)
                            }
                        )
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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