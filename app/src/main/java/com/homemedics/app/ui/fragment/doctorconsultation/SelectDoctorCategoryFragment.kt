package com.homemedics.app.ui.fragment.doctorconsultation

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentBookConsultationBinding
import com.homemedics.app.databinding.FragmentSelectDoctorCategoryBinding
import com.homemedics.app.ui.activity.AuthActivity
import com.homemedics.app.ui.adapter.DoctorListAdapter
import com.homemedics.app.ui.adapter.LoaderStateAdapter
import com.homemedics.app.ui.adapter.TopRatedDoctorListAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectDoctorCategoryFragment : BaseFragment() {

    private lateinit var mBinding: FragmentSelectDoctorCategoryBinding
    val langData = ApplicationClass.mGlobalData

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()
    private lateinit var doctorListingAdapter: TopRatedDoctorListAdapter
    lateinit var loaderStateAdapter: LoaderStateAdapter

    override fun setLanguageData() {
//        val bookingData = langData?.bookingScreen
//        mBinding.apply {
//            etSearch.hint = bookingData?.searchDoctor
//            tvNoData.text = bookingData?.noSpecialityFound
//            actionbar.title = bookingData?.bookConsult.getSafe()
//            tvTitle.text = bookingData?.howToGetConsult
//        }
    }

    override fun init() {
        setActionbarCity()
        populateDoctorList()

        doctorConsultationViewModel.bdcFilterRequest.apply {
            partnerType = Enums.Profession.DOCTOR.key
            specialityId = 142
            specialityName = "General practitioner"
            serviceId = 1
            serviceName = "Video call"
        }

        lifecycleScope.launch {
            doctorConsultationViewModel.items.collectLatest {
                setData(it)
            }
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_select_doctor_category

    override fun getViewModel() {
    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectDoctorCategoryBinding
    }

    override fun setListeners() {
        mBinding.cvGP.setOnClickListener {
            doctorConsultationViewModel.bdcFilterRequest.apply {
                partnerType = Enums.Profession.DOCTOR.key
                specialityId = 142
                specialityName = "General practitioner"
                serviceId = 1
                serviceName = "Video call"
            }
            doctorConsultationViewModel.fromSearch = false
            findNavController().safeNavigate(SelectDoctorCategoryFragmentDirections.actionSelectDoctorCategoryFragmentToDoctorListingFragment())
        }

        mBinding.cvSpecialist.setOnClickListener {
            findNavController().safeNavigate(SelectDoctorCategoryFragmentDirections.actionSelectDoctorCategoryFragmentToBookConsultation())

        }

        mBinding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        doctorListingAdapter.itemClickListener = { item, _ ->
            if (isUserLoggedIn()) {
                doctorConsultationViewModel.partnerProfileResponse = item
                item as PartnerProfileResponse
                item.specialities?.apply {
                    this.forEach {
                        val temp=it.genericItemId?.toInt()
                        doctorConsultationViewModel.bdcFilterRequest.specialityId=temp
                    }
                }
                findNavController().safeNavigate(SelectDoctorCategoryFragmentDirections.actionSelectDoctorCategoryFragmentToBookConsultationDetailsFragment())
            } else {
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun setActionbarCity() {
        //action bar city name
        if (doctorConsultationViewModel.bdcFilterRequest.cityName.isEmpty()) {
            val user = DataCenter.getUser()
            user?.let {
                val city =
                    getCityList(it.countryId.getSafe())?.find { city -> city.id == it.cityId }
                mBinding.tvDesc.text = city?.name.getSafe()
                doctorConsultationViewModel.bdcFilterRequest.cityId = city?.id.getSafe()
                doctorConsultationViewModel.bdcFilterRequest.cityName = city?.name.getSafe()

                val countryIndex =
                    metaData?.countries?.indexOfFirst { country -> country.id == it.countryId.getSafe() }
                        .getSafe()

                if (countryIndex != -1) {
                    val country = metaData?.countries?.get(countryIndex)
                    doctorConsultationViewModel.bdcFilterRequest.countryId = country?.id.getSafe()
                    doctorConsultationViewModel.bdcFilterRequest.countryName =
                        country?.name.getSafe()
                }
            }
        } else mBinding.tvDesc.text = doctorConsultationViewModel.bdcFilterRequest.cityName
    }


    private fun populateDoctorList() {
        mBinding.apply {
            doctorListingAdapter = TopRatedDoctorListAdapter()
            loaderStateAdapter = LoaderStateAdapter { doctorListingAdapter.retry() }
            rvDoctor.adapter = doctorListingAdapter.withLoadStateFooter(loaderStateAdapter)

        }
    }

    private suspend fun setData(pagingData: PagingData<PartnerProfileResponse>) {
        doctorListingAdapter.submitData(pagingData)

    }

}