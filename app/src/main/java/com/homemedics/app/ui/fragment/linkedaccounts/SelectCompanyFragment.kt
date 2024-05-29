package com.homemedics.app.ui.fragment.linkedaccounts

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.linkaccount.LinkCompanyRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.linkaccount.CompanyListResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectCompanyBinding
import com.homemedics.app.ui.adapter.AddMyCompanyAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlin.properties.Delegates

class SelectCompanyFragment : BaseFragment(), View.OnClickListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentSelectCompanyBinding
    private val companyAdapter = AddMyCompanyAdapter()
    private var preSelected = -1
    private val langData=ApplicationClass.mGlobalData

    override fun setLanguageData() {
mBinding.apply {
    actionbar.title=langData?.linkedAccountScreen?.selectCompany.getSafe()
    etSearch.hint=langData?.globalString?.search.getSafe()
    tvNoData.text=langData?.globalString?.noResultFound.getSafe()
    bLink.text=langData?.linkedAccountScreen?.buttonLink.getSafe()

}
    }

    override fun init() {
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
        getListApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_select_company

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectCompanyBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            etSearch.addTextChangedListener {
                companyAdapter.filter.filter(it)
            }
            companyAdapter.apply {
                var selectedPosition by Delegates.observable(-1) { property, oldPos, newPos ->
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
                    bLink.isEnabled = getSelectedItem() != null
                }
            }
            bLink.setOnClickListener {
                val companyId = (companyAdapter.getSelectedItem())
                val request = LinkCompanyRequest(companyId?.genericItemId.toString())
                linkApi(request)
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun linkApi(request: LinkCompanyRequest){
        if (isOnline(requireActivity())) {
            profileViewModel.linkCompany(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
//                    val response = it.data as ResponseGeneral<CompanyListResponse>
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
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message =langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getListApi(){
        if (isOnline(requireActivity())) {
            profileViewModel.getCooperateCompList().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<CompanyListResponse>

                        val list = (response.data?.companies as ArrayList<GenericItem>?)
//                        list?.get(0)?.isSelected = true
                        list?.let { listItems ->
                            companyAdapter.listItems = listItems
                            companyAdapter.originalList = listItems
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message =it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
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
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message =langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}