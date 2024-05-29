package com.homemedics.app.ui.fragment.medicalrecords.patient

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.family.FamilyResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectFamilyBinding
import com.homemedics.app.ui.adapter.SelectFamilyConnectionsAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class SelectFamilyFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentSelectFamilyBinding
    private lateinit var listAdapter: SelectFamilyConnectionsAdapter
    private val emrViewModel: EMRViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.selectFamily.getSafe()
        }
    }

    override fun init() {
        populateList()
        getFamilyConnections()
    }

    override fun getFragmentLayout() = R.layout.fragment_select_family

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectFamilyBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().navigateUp()
            }

            listAdapter.itemClickListener = {
                item, _ ->
                emrViewModel.selectedFamily = item

                when (emrViewModel.selectedEMRType){
                    Enums.EMRType.CONSULTATION -> {
                        findNavController().safeNavigate(SelectFamilyFragmentDirections.actionCustomerSelectFamilyFragmentToCustomerEmrConsultationNavigation())
                    }
                    Enums.EMRType.REPORTS -> {
                        findNavController().safeNavigate(SelectFamilyFragmentDirections.actionCustomerSelectFamilyFragmentToCustomerEmrReportsNavigation())
                    }
                    Enums.EMRType.MEDICATION -> {
                        findNavController().safeNavigate(SelectFamilyFragmentDirections.actionCustomerSelectFamilyFragmentToCustomerEmrMedicationNavigation())
                    }
                    Enums.EMRType.VITALS -> {
                        findNavController().safeNavigate(SelectFamilyFragmentDirections.actionCustomerSelectFamilyFragmentToCustomerEmrVitalsNavigation())
                    }
                    else ->
                        findNavController().navigateUp() //go back to main listing
                }
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun populateList() {
        mBinding.apply {
            listAdapter = SelectFamilyConnectionsAdapter()
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
                            val user = DataCenter.getUser()
                            val self = mBinding.lang?.globalString?.self.getSafe()
                            listAdapter.listItems = (it.connected as ArrayList<FamilyConnection>).apply {
                                add(0, FamilyConnection(
                                    id = user?.id,
                                    userId = user?.id,
                                    familyMemberId = user?.id,
                                    profilePicture = user?.profilePicture,
                                    relation = self,
                                    fullName = user?.fullName.getSafe(),
                                    genderId = user?.genderId.getSafe().toString(),
                                    age = getAgeFromDate(user?.dateOfBirth, "dd/MM/yyyy"),
                                    addresses = user?.userLocations
                                ))
                            }
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
                                message =getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Pending -> {
                        showLoader()
                    }
                    is ResponseResult.Complete -> {
                        hideLoader()
                    }
                    else -> {
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