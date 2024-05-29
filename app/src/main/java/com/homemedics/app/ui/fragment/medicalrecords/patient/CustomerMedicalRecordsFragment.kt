package com.homemedics.app.ui.fragment.medicalrecords.patient

import android.content.Intent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCustomerMedicalRecordsBinding
import com.homemedics.app.ui.activity.HomeActivity
import com.homemedics.app.ui.adapter.MedicalRecordTypeAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class CustomerMedicalRecordsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentCustomerMedicalRecordsBinding
    private lateinit var listAdapter: MedicalRecordTypeAdapter
    private val emrViewModel: EMRViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.emrScreens?.medicalRecords.getSafe()
        }
    }

    override fun init() {
        setupViews()

        mBinding.actionbar.desc = if(emrViewModel.selectedFamily == null || emrViewModel.selectedFamily?.familyMemberId == DataCenter.getUser()?.id) mBinding.langData?.globalString?.self.getSafe() else emrViewModel.selectedFamily?.fullName.getSafe()
        populateList()
    }

    override fun getFragmentLayout() = R.layout.fragment_customer_medical_records

    override fun getViewModel() {
        emrViewModel.fromBDC = arguments?.containsKey(Enums.BundleKeys.fromBDC.key).getSafe()
        emrViewModel.fromChat = arguments?.containsKey(Enums.BundleKeys.fromChat.key).getSafe()
        emrViewModel.bookingId = arguments?.getInt(Enums.BundleKeys.bookingId.key).getSafe()
    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCustomerMedicalRecordsBinding
    }

    override fun setListeners() {
        handleBackPress()
        mBinding.apply {
            actionbar.onAction1Click = {
                requireActivity().onBackPressed()

                val emrIntent = Intent(requireActivity(), HomeActivity::class.java)
                requireActivity().apply {
                    startActivity(emrIntent)
                    finish()
                }
            }

            actionbar.onAction2Click = {
                findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerSelectFamilyFragment())
            }
            actionbar.mBinding.tvDesc.setOnClickListener {
                if(emrViewModel.fromBDC || emrViewModel.fromChat)
                    return@setOnClickListener
                findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerSelectFamilyFragment())
            }

            actionbar.onAction2Click = {
                findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerSelectFamilyFragment())
            }

            listAdapter.itemClickListener = {
                item, _ ->
                emrViewModel.selectedEMRType = Enums.EMRType.values().find { it.key == item.genericItemId }

                when(emrViewModel.selectedEMRType){
                    Enums.EMRType.CONSULTATION ->
                        findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerEmrConsultationNavigation())
                    Enums.EMRType.REPORTS ->
                        findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerEmrReportsNavigation())
                    Enums.EMRType.MEDICATION ->
                        findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerEmrMedicationNavigation())
                    Enums.EMRType.VITALS ->
                        findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerEmrVitalsNavigation())
                }
            }
        }

        handlePN()
    }

    private fun handleBackPress(){
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
               /* val chatIntent = Intent(requireActivity(), HomeActivity::class.java)
                requireActivity().apply {
                    startActivity(chatIntent)
                    finish()            } */

            }
        })
    }

    override fun onClick(v: View?) {

    }

    private fun handlePN(){
        if(requireActivity().intent.hasExtra(Enums.BundleKeys.fromPush.key).getSafe()){
            val emrType = requireActivity().intent.getIntExtra("emrType", 0)
            val emrId = requireActivity().intent.getIntExtra("emrId", 0)
            requireActivity().intent.removeExtra("emrId")
            requireActivity(). intent.removeExtra("emrType")
            requireActivity(). intent.removeExtra(Enums.BundleKeys.fromPush.key)
            emrViewModel.selectedEMRType = Enums.EMRType.values().find { it.key == emrType }
            emrViewModel.emrID = emrId
            emrViewModel.fromPush = true

            when(emrViewModel.selectedEMRType){
                Enums.EMRType.CONSULTATION ->
                    findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerEmrConsultationNavigation())
                Enums.EMRType.REPORTS ->
                    findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerEmrReportsNavigation())
                Enums.EMRType.MEDICATION ->
                    findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerEmrMedicationNavigation())
                Enums.EMRType.VITALS ->
                    findNavController().safeNavigate(CustomerMedicalRecordsFragmentDirections.actionCustomerMedicalRecordsFragmentToCustomerEmrVitalsNavigation())
            }

        }
    }


    private fun populateList() {
        mBinding.apply {
            listAdapter = MedicalRecordTypeAdapter()
            listAdapter.listItems = DataCenter.getEMRTypeList(mBinding.langData)
            rvList.adapter = listAdapter
        }
    }

    private fun setupViews(){
        mBinding.apply {
            if(emrViewModel.fromBDC || emrViewModel.fromChat){
                actionbar.mBinding.apply {
                    ivAction3.gone()
                    ivAction2.gone()
                }
            }
        }
    }


    fun onBackPressed() {
        if (fragmentManager!!.backStackEntryCount > 0) fragmentManager!!.popBackStack()
    }
}