package com.homemedics.app.ui.fragment.labtests

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.walkinpharmacy.ServiceTypes
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentPharmacyServicesBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.adapter.PharmacyServiceAdapter
import com.homemedics.app.ui.fragment.pharmacy.PharmacyServiceFragmentDirections
import com.homemedics.app.utils.DataCenter
import com.homemedics.app.utils.DialogUtils
import com.homemedics.app.utils.getErrorMessage
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.isOnline
import com.homemedics.app.utils.safeNavigate
import com.homemedics.app.viewmodel.HomeViewModel
import com.homemedics.app.viewmodel.WalkInViewModel

class LabServiceFragment : BaseFragment() {

    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var mBinding: FragmentPharmacyServicesBinding
    private lateinit var pharmaAdapter: PharmacyServiceAdapter
    private var lang: RemoteConfigLanguage? = null
    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.walkInScreens?.laboratoryServices.getSafe()
        }
    }

    override fun init() {
        getLabServiceType()
        populateList()
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_pharmacy_services

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPharmacyServicesBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
        }
        pharmaAdapter.itemClickListener = { _, pos ->
            when (pos) {
                0 -> findNavController().safeNavigate(
                    LabServiceFragmentDirections.actionLabServiceFragmentToLabTestFragment()
                )
                1 -> {
                    if(isUserLoggedIn() && homeViewModel.linkedAccounts.isEmpty()){
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = lang?.globalString?.information.getSafe(),
                                message = lang?.dialogsStrings?.notLinkedToCorporate.getSafe()
                            )
                    } else {
                        walkInViewModel.apply {
                            isDiscountCenter = false
                            isLab = false
                        }
                        findNavController().safeNavigate(
                            LabServiceFragmentDirections.actionLabServiceFragmentToWalkinLabServiceFragment()
                        )
                    }
                }
                2 -> {
                    if(isUserLoggedIn() && homeViewModel.linkedAccounts.isEmpty()){
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = lang?.globalString?.information.getSafe(),
                                message = lang?.dialogsStrings?.notLinkedToCorporate.getSafe()
                            )
                    } else {
                        walkInViewModel.apply {
                            isDiscountCenter = true
                            isLab = true
                        }
                        findNavController().safeNavigate(
                            LabServiceFragmentDirections.actionLabServiceFragmentToWalkinLabServiceFragment()
                        )
                    }
                }
            }
        }
    }

    private fun populateList() {
        pharmaAdapter = PharmacyServiceAdapter().apply {
//            listItems = DataCenter.getLabServicesList(lang?.globalString)
        }
        mBinding.rvPharmacyServices.apply {
            adapter = pharmaAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun getLabServiceType() {
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInLabServiceTypes().observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as ServiceTypes).let { serviceTypes ->
                            pharmaAdapter.listItems =
                                DataCenter.getLabServicesList(lang?.globalString, serviceTypes)
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
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message = ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}