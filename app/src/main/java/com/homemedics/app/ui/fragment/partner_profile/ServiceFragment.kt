package com.homemedics.app.ui.fragment.partner_profile

import android.view.View
import androidx.fragment.app.activityViewModels
import com.fatron.network_module.models.request.partnerprofile.ServiceRequest
import com.fatron.network_module.models.response.partnerprofile.Services
import com.fatron.network_module.repository.ResponseResult
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentServiceBinding
import com.homemedics.app.ui.adapter.ServiceAdapter
import com.homemedics.app.utils.DialogUtils
import com.homemedics.app.utils.getErrorMessage
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.isOnline
import com.homemedics.app.viewmodel.ProfileViewModel

class ServiceFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentServiceBinding
    private val serviceAdapter = ServiceAdapter()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val serviceList = arrayListOf<Services>()
    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
        }
    }

    override fun init() {
        profileViewModel.serviceList.map {
            serviceList.add(Gson().fromJson(Gson().toJson(it), Services::class.java))
        }
        serviceAdapter.listItems = serviceList
        mBinding.rvServiceList.adapter = serviceAdapter

    }

    override fun getFragmentLayout(): Int = R.layout.fragment_service

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentServiceBinding
    }

    override fun setListeners() {
        serviceAdapter.onServiceCheckChange = { _, _ ->
            mBinding.bSubmitRequest.isEnabled = isSelectionChanged()
        }
        mBinding.bSubmitRequest.setOnClickListener(this)
    }

    private fun isSelectionChanged(): Boolean{
        if(profileViewModel.serviceList.size != serviceAdapter.listItems.size)
            return false

        val changedList = profileViewModel.serviceList.filterIndexed { i, value ->
            serviceAdapter.listItems[i].offerService != value.offerService
        }

        return changedList.isNotEmpty()
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.bSubmitRequest) {
            callStorePartnerServiceApi()
        }
    }
    private fun callStorePartnerServiceApi() {
        val selectedService = serviceAdapter.listItems.filter { it.offerService }
        val selectedServiceList = arrayListOf<Int>()
        if (selectedService.isNotEmpty())
            selectedService.map { selectedServiceList.add(it.id) }

        val serviceRequest = ServiceRequest(services = selectedServiceList)
        profileViewModel.storePartnerService(serviceRequest)
            .observe(this) {
                if (isOnline(activity)) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            try {
                                DialogUtils(requireActivity())
                                    .showSingleButtonAlertDialog(
                                        message = mBinding.langData?.partnerProfileScreen?.serviceUpdate.getSafe(),
                                        buttonCallback = {},
                                    )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        is ResponseResult.Pending -> {
                            showLoader()
                        }
                        is ResponseResult.Failure -> {
                            hideLoader()
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message = it.error.message.getSafe(),
                                    buttonCallback = {},
                                )
                        }
                        is ResponseResult.ApiError -> {
                            hideLoader()
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                    buttonCallback = {},
                                )
                        }
                        is ResponseResult.Complete -> {
                            hideLoader()
                        }
                        else -> {
                            hideLoader()
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
}