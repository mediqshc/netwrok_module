package com.homemedics.app.ui.fragment.medicalrecords

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.emr.type.SymptomsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.type.EMRDetailsResponse
import com.fatron.network_module.models.response.emr.type.EmrCreateResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentMedicalRecordsBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class MedicalRecordsFragment : BaseFragment() {

    private val emrViewModel: EMRViewModel by activityViewModels()

    private lateinit var mBinding: FragmentMedicalRecordsBinding

    private var lang: RemoteConfigLanguage? = null

    override fun setLanguageData() {
        lang = ApplicationClass.mGlobalData
        mBinding.apply {
            actionbar.title = lang?.emrScreens?.emrNav.getSafe()
        }
    }

    override fun init() {
        if (emrViewModel.isDraft) {
            val request = SymptomsRequest(customerId = emrViewModel.customerId, partnerServiceId = emrViewModel.partnerServiceId)
            getEMRDrafts(request)
        }
        initTabAndViewpager()
    }

    override fun getFragmentLayout() = R.layout.fragment_medical_records

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentMedicalRecordsBinding
    }

    override fun setListeners() {
        handleBackPress()
        mBinding.apply {
            actionbar.onAction1Click = {
                closeKeypad()
                showCancelChangesDialog()
            }
        }
    }

    private fun initTabAndViewpager() {

        mBinding.apply {
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(ObservationFragment())

                if (DataCenter.getUser().isMedicalStaff().not())
                    add(DiagnosisFragment())

                add(PrescriptionFragment())
            }
            viewPager.adapter = TabsPagerAdapter(childFragmentManager, lifecycle)
            viewPager.isUserInputEnabled = false
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                if (DataCenter.getUser().isMedicalStaff()) {
                    tab.text = when (position) {
                        0 -> lang?.emrScreens?.observation.getSafe()
                        1 -> lang?.emrScreens?.prescription.getSafe()
                        else -> {
                            ""
                        }
                    }
                } else {
                    tab.text = when (position) {
                        0 -> lang?.emrScreens?.observation.getSafe()
                        1 -> lang?.emrScreens?.diagnosis.getSafe()
                        2 -> lang?.emrScreens?.prescription.getSafe()
                        else -> {
                            ""
                        }
                    }
                }
            }.attach()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        emrViewModel.apply {
            emrId.value = null
            emrID = 0
            emrChat = false
        }
    }

    private fun handleBackPress(){
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showCancelChangesDialog()
                }
            }
        )
    }

    private fun showCancelChangesDialog() {
        DialogUtils(requireActivity())
            .showDoubleButtonsAlertDialog(
                title = lang?.emrScreens?.cancelChanges.getSafe(),
                message = lang?.emrScreens?.exitDescription.getSafe(),
                positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                negativeButtonStringText = lang?.globalString?.no.getSafe(),
                buttonCallback = {
                    if(requireActivity() is CallActivity){
                        (requireActivity() as CallActivity).removeEMRNavigation()
                    }
                    else
                        findNavController().popBackStack()

                    emrViewModel.emrID = 0
                    emrViewModel.bookingId = 0
                    emrViewModel.isDraft = false
                    emrViewModel.emrChat = false
                },
            )
    }

    private fun createEMR(symptomsRequest: SymptomsRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.emrCreate(symptomsRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<EmrCreateResponse>
                        response.data?.let { emrResponse ->
                            emrViewModel.emrID = emrResponse.emrId.getSafe()
                            emrViewModel.emrId.postValue(emrResponse.emrId)
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
                        hideLoader()
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
                    title = lang?.errorMessages?.internetError.getSafe(),
                    message = lang?.errorMessages?.internetError.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getEMRDrafts(symptomsRequest: SymptomsRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.emrDrafts(symptomsRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<EMRDetailsResponse>
                        response.data?.let { emrResponse ->
                            if (emrResponse.emrDraft == null) {
                                val request = SymptomsRequest(customerId = emrViewModel.customerId)
                                createEMR(request)
                            } else {
                                emrViewModel.emrID = emrResponse.emrDraft?.id.getSafe()
                                emrViewModel.emrId.postValue(emrResponse.emrDraft?.id)
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
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.Pending -> {
                        hideLoader()
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
                    title = lang?.errorMessages?.internetError.getSafe(),
                    message = lang?.errorMessages?.internetError.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}