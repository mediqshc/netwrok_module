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
import com.homemedics.app.databinding.FragmentAddHospitalBinding
import com.homemedics.app.ui.adapter.AddMyHospitalsAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel
import kotlin.properties.Delegates

class AddHospitalFragment : BaseFragment(), View.OnClickListener {

    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var mBinding: FragmentAddHospitalBinding
    private val hospitalAdapter = AddMyHospitalsAdapter()
    private var preSelected = -1

    private val langData = ApplicationClass.mGlobalData
    override fun setLanguageData() {
        mBinding.apply {
            etSearch.hint = langData?.globalString?.search
            actionbar.title = langData?.linkedAccountScreen?.addHospitalAndClinic.getSafe()
            tvNoData.text = langData?.globalString?.noResultFound.getSafe()
            bLink.text = langData?.linkedAccountScreen?.buttonLink.getSafe()
        }
    }

    override fun init() {
        mBinding.rvHospital.adapter = hospitalAdapter
        hospitalAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
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
                mBinding.tvNoData.setVisible((hospitalAdapter.itemCount == 0))
                mBinding.rvHospital.setVisible((hospitalAdapter.itemCount != 0))
            }
        })
        getListApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_add_hospital

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddHospitalBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            etSearch.addTextChangedListener {
                hospitalAdapter.filter.filter(it)
            }
            hospitalAdapter.apply {
                var selectedPosition by Delegates.observable(-1) { property, oldPos, newPos ->
                    val temperedOldPos = if (oldPos == -1) preSelected else oldPos

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

                onItemSelected = { _, pos ->
                    selectedPosition = pos
                    bLink.isEnabled = getSelectedItem() != null
                }
            }
            bLink.setOnClickListener {
                val companyId = (hospitalAdapter.getSelectedItem())
                val request = LinkCompanyRequest(healthcareId = companyId?.genericItemId.toString())
                linkApi(request)
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun linkApi(request: LinkCompanyRequest) {
        if (isOnline(requireActivity())) {
            profileViewModel.linkHospital(request).observe(this) {
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

    private fun getListApi() {
        if (isOnline(requireActivity())) {
            profileViewModel.getHospitalList().observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<CompanyListResponse>
                        val list = response.data?.healthcare as ArrayList<GenericItem>?
//                        list?.get(0)?.isSelected = true
                        list?.let { listItems ->
                            hospitalAdapter.listItems = listItems
                            hospitalAdapter.originalList = listItems
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = langData?.globalString?.information.getSafe(),
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title =langData?.globalString?.information.getSafe(),
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
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}