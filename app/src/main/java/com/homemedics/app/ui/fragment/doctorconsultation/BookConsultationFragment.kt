package com.homemedics.app.ui.fragment.doctorconsultation

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentBookConsultationBinding
import com.homemedics.app.ui.adapter.DoctorConsultAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.DoctorConsultationViewModel

class BookConsultationFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentBookConsultationBinding

    private lateinit var doctorConsultAdapter: DoctorConsultAdapter

    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()
    val langData = ApplicationClass.mGlobalData

    override fun setLanguageData() {
        val bookingData = langData?.bookingScreen
        mBinding.apply {
            etSearch.hint = bookingData?.searchDoctor
            tvNoData.text = bookingData?.noSpecialityFound
            actionbar.title = bookingData?.bookConsult.getSafe()
            tvTitle.text = bookingData?.howToGetConsult
        }
    }

    override fun onDetach() {
        super.onDetach()
        doctorConsultationViewModel.flushData()
    }

    override fun init() {
        observe()
        setDisabledServices()
        setSelectedService()
        populateSpecialityList()
        doctorConsultationViewModel.bdcFilterRequest.apply {
            specialityId = null
            specialityName = ""
        }
        setActionbarCity()

    }

    private fun setActionbarCity() {
        //action bar city name
        if (doctorConsultationViewModel.bdcFilterRequest.cityName.isEmpty()) {
            val user = DataCenter.getUser()
            user?.let {
                val city =
                    getCityList(it.countryId.getSafe())?.find { city -> city.id == it.cityId }
                mBinding.actionbar.desc = city?.name.getSafe()
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
        } else mBinding.actionbar.desc = doctorConsultationViewModel.bdcFilterRequest.cityName
    }

    override fun getFragmentLayout() = R.layout.fragment_book_consultation

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentBookConsultationBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            etSearch.setOnClickListener {
                doctorConsultationViewModel.fromSearch = true
                doctorConsultationViewModel.bdcFilterRequest.partnerType =
                    Enums.Profession.DOCTOR.key
                findNavController().safeNavigate(BookConsultationFragmentDirections.actionBookConsultationToDoctorListingFragment())

            }
            serviceTypeView.onItemSelected = { item ->
                doctorConsultationViewModel.bdcFilterRequest.serviceId = item.id
                doctorConsultationViewModel.bdcFilterRequest.serviceName = metaData?.partnerServiceType?.find { it.id==item.id }?.shortName.getSafe()
            }

            doctorConsultAdapter.itemClickListener = { item, _ ->
                if (doctorConsultationViewModel.bdcFilterRequest.serviceId != null) {
                    doctorConsultationViewModel.bdcFilterRequest.apply {
                        partnerType = Enums.Profession.DOCTOR.key
                        specialityId = item.genericItemId
                        specialityName = item.genericItemName.getSafe()
                    }
                    doctorConsultationViewModel.fromSearch = false
                    findNavController().safeNavigate(BookConsultationFragmentDirections.actionBookConsultationToDoctorListingFragment())
                } else {
                    DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                        title = langData?.globalString?.information.getSafe(),
                        message = langData?.dialogsStrings?.selectService.getSafe()
                    )
                }
            }

        }
    }

    private fun setSelectedService() {
        mBinding.apply {
            val id = doctorConsultationViewModel.bdcFilterRequest.serviceId.getSafe()
            serviceTypeView.setServiceType(id.getSafe())
        }

    }

    private fun setDisabledServices(){
        val excludedServiceIds = metaData?.doctorServices?.filter { it.offerService.getBoolean().not() }?.map {
            it.genericItemId
        }

        mBinding.serviceTypeView.setDisabledServices(excludedServiceIds as ArrayList<Int>?)
    }

    override fun onClick(v: View?) {

    }

    private fun observe() {
        doctorConsultationViewModel.specialities.observe(this) {
            mBinding.apply {
                rvSpeciality.setVisible((it.isNullOrEmpty().not()))
                tvNoData.setVisible((it.isNullOrEmpty()))
            }
            doctorConsultAdapter.listItems = it
        }
    }

    private fun populateSpecialityList() {
        mBinding.apply {
            doctorConsultAdapter = DoctorConsultAdapter()
            rvSpeciality.adapter = doctorConsultAdapter
        }
    }
}