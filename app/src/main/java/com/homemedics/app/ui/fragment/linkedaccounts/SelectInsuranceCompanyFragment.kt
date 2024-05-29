package com.homemedics.app.ui.fragment.linkedaccounts

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.linkaccount.CompanyListResponse
import com.fatron.network_module.models.response.linkaccount.CompanyResponse
import com.fatron.network_module.models.response.linkaccount.InsuranceFields
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectInsuranceBinding
import com.homemedics.app.ui.adapter.InsuranceCompanyAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlin.properties.Delegates

class SelectInsuranceCompanyFragment : BaseFragment(), View.OnClickListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentSelectInsuranceBinding
    private val insuranceCompanyAdapter = InsuranceCompanyAdapter()
    private var preSelected = -1
    private val langData = ApplicationClass.mGlobalData

    override fun setLanguageData() {
        mBinding.apply {
            bNext.text=langData?.linkedAccountScreen?.buttonNext
            tvNoData.text=langData?.globalString?.noResultFound
            etSearch.hint=langData?.globalString?.search
            actionbar.title=langData?.linkedAccountScreen?.addInsuranceCompany.getSafe()

        }
    }

    override fun init() {
        mBinding.rvInsurance.adapter = insuranceCompanyAdapter
        insuranceCompanyAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
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
                mBinding.tvNoData.setVisible((insuranceCompanyAdapter.itemCount == 0))
                mBinding.rvInsurance.setVisible((insuranceCompanyAdapter.itemCount != 0))
            }
        })
        getListApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_select_insurance

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectInsuranceBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            etSearch.addTextChangedListener {
                insuranceCompanyAdapter.filter.filter(it)
            }
            insuranceCompanyAdapter.apply {
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
                    bNext.isEnabled = getSelectedItem() != null
                }
            }
            bNext.setOnClickListener {
                profileViewModel.selectedInsurance = insuranceCompanyAdapter.getSelectedItem()
                findNavController().safeNavigate(SelectInsuranceCompanyFragmentDirections.actionSelectInsuranceCompanyFragmentToInsuranceDetailFragment())
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun getListApi(){
        if (isOnline(requireActivity())) {
            profileViewModel.getInsuranceCompList().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<CompanyListResponse>

                        val list = (response.data?.insurances as ArrayList<CompanyResponse>?)
                        profileViewModel.insuranceDataFields = response.data?.insuranceFields as ArrayList<InsuranceFields>
//                        list?.get(0)?.isSelected = true
                        list?.let { listItems ->
                            insuranceCompanyAdapter.listItems = listItems
                            insuranceCompanyAdapter.originalList = listItems
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title =langData?.globalString?.information.getSafe(),
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