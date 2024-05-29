package com.homemedics.app.ui.fragment.walkin.hospital

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.walkin.WalkInInitialRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.walkin.services.WalkInService
import com.fatron.network_module.models.response.walkin.services.WalkInServices
import com.fatron.network_module.models.response.walkinpharmacy.WalkInInitialResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentWalkinHospitalSelectServiceBinding
import com.homemedics.app.ui.adapter.WalkInServiceAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.WalkInViewModel

class WalkInHospitalSelectServiceFragment : BaseFragment() {

    private lateinit var mBinding: FragmentWalkinHospitalSelectServiceBinding
    private lateinit var walkInServiceAdapter: WalkInServiceAdapter
    private val walkInViewModel: WalkInViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title =  walkInViewModel.walkInHospitalName.getSafe()
            tvNoData.text = lang?.bookingScreen?.noHomeServiceFound.getSafe()
        }
     }

    override fun init() {
        setObserver()
        setupAdapter()
        getWalkInHospitalServicesList()
        initialWalkInHospital()
    }

    override fun getFragmentLayout() = R.layout.fragment_walkin_hospital_select_service

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkinHospitalSelectServiceBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                openDefaultMap(
                    walkInViewModel.mapLatLng, requireActivity()
                )
            }
            walkInServiceAdapter.itemClickListener = { item, _ ->
                walkInViewModel.serviceName = item.services?.genericItemName.getSafe()
                walkInViewModel.walkInService = item
                walkInViewModel.partnerServiceId = item.services?.partnerServiceId.getSafe()
                findNavController().safeNavigate(WalkInHospitalSelectServiceFragmentDirections.actionWalkInHospitalSelectServiceFragmentToWalkInHospitalRequestServiceFragment())
            }
        }
    }

    private fun setObserver() {
        walkInViewModel.walkInHospitalServices.observe(this) { walkInHospitalServices ->
            walkInHospitalServices?.let { hospitalServices ->
                mBinding.apply {
                    rvServices.setVisible((hospitalServices.isEmpty().not()))
                    tvNoData.setVisible((hospitalServices.isEmpty()))
                }
                walkInServiceAdapter.listItems = hospitalServices
            }
        }
    }

    private fun setupAdapter() {
        walkInServiceAdapter = WalkInServiceAdapter()
        mBinding.rvServices.adapter = walkInServiceAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        walkInViewModel.walkInHospitalServices.value = null
    }

    private fun getWalkInHospitalServicesList() {
        val request = WalkInInitialRequest(healthcareId = walkInViewModel.hospitalId)
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInHospitalServicesList(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        val data = (response.data as WalkInServices)
                        walkInViewModel.walkInHospitalServices.postValue(data.walkInServices.getSafe())
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
                                message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun initialWalkInHospital() {
        val request = WalkInInitialRequest(healthcareId = walkInViewModel.hospitalId)
        if (isOnline(requireActivity())) {
            walkInViewModel.initialWalkInHospital(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as WalkInInitialResponse).let { walkInitialResponse ->
                            walkInViewModel.walkInInitialResponse = walkInitialResponse
                            walkInViewModel.bookingId = walkInitialResponse.walkInHospital?.bookingId.getSafe()
                            walkInViewModel.documentTypes = walkInitialResponse.walkInHospital?.documentTypes
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
                                message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}