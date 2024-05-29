package com.homemedics.app.ui.fragment.doctorconsultation

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.bdc.BDCFilterRequest
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentDoctorFilterBinding
import com.homemedics.app.utils.DataCenter
import com.homemedics.app.utils.getBoolean
import com.homemedics.app.utils.getInt
import com.homemedics.app.utils.getSafe
import com.homemedics.app.viewmodel.DoctorConsultationViewModel

class DoctorFilterFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentDoctorFilterBinding
    private val doctorConsultationViewModel: DoctorConsultationViewModel by activityViewModels()
    private var bdcFilterRequest = BDCFilterRequest()

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            cdHospital.hint = langData?.bookingScreen?.clinicHospital.getSafe()
            cdGender.hint = langData?.globalString?.gender.getSafe()
            cdSpeciality.hint = langData?.globalString?.speciality.getSafe()
            cdCity.hint = langData?.globalString?.city.getSafe()
            cdCountry.hint = langData?.globalString?.country.getSafe()
            actionbar.title = langData?.globalString?.filter.getSafe()
        }
    }

    override fun init() {
        bdcFilterRequest = Gson().fromJson(
            Gson().toJson(doctorConsultationViewModel.bdcFilterRequest),
            BDCFilterRequest::class.java
        )
        observe()
        initDropDowns()
        setDisabledServices()
        setSelectedService()

        mBinding.cbAvailableForVideo.isChecked = bdcFilterRequest.availableForVideoCall.getBoolean()
    }

    private fun initDropDowns() {
        mBinding.apply {

            //country
            var countryIndex = -1
            if (bdcFilterRequest.countryId != null) {
                countryIndex =
                    metaData?.countries?.indexOfFirst { it.id == bdcFilterRequest.countryId.getSafe() }
                        .getSafe()
            }

            val indexSelection = countryIndex
            val countryList = getCountryList()
            cdCountry.data = countryList as ArrayList<String>

            if (countryIndex > -1) {
                cdCountry.selectionIndex = indexSelection
                val country = metaData?.countries?.get(indexSelection)
                bdcFilterRequest.countryId = country?.id.getSafe()
                bdcFilterRequest.countryName = country?.name.getSafe()
            }

            cdCountry.onItemSelectedListener = { _, position: Int ->
                val country = metaData?.countries?.get(position)
                bdcFilterRequest.countryId = country?.id.getSafe()
                bdcFilterRequest.countryName = country?.name.getSafe()
                setCityList(country?.id.getSafe())
            }
            setCityList(metaData?.countries?.get(cdCountry.selectionIndex)?.id.getSafe())

            //gender
            var genderIndex = -1
            if (bdcFilterRequest.genderId != null) {
                genderIndex =
                    metaData?.genders?.indexOfFirst { it.genericItemId == bdcFilterRequest.genderId.getSafe() }
                        .getSafe()
            }
            var selectionIndex = genderIndex
            val genderList = getGenderList()
            cdGender.data = genderList

            if (selectionIndex > -1) {
                cdGender.selectionIndex = selectionIndex
                val gender = metaData?.genders?.get(selectionIndex)
                bdcFilterRequest.genderId = gender?.genericItemId.getSafe()
                bdcFilterRequest.genderName = gender?.genericItemName.getSafe()
            }

            cdGender.onItemSelectedListener = { _, position: Int ->
                val gender = metaData?.genders?.get(position)
                bdcFilterRequest.genderId = gender?.genericItemId.getSafe()
                bdcFilterRequest.genderName = gender?.genericItemName.getSafe()
            }


            //speciality
            val specialities = DataCenter.getDoctorSpecialities()

            var specialityIndex = -1
            if (bdcFilterRequest.specialityId != null) {
                specialityIndex =
                    specialities.indexOfFirst { it.genericItemId == bdcFilterRequest.specialityId.getSafe() }
                        .getSafe()
            }

            selectionIndex = specialityIndex
            val specialitiesNames = specialities.map { it.genericItemName }
            cdSpeciality.data = specialitiesNames as ArrayList<String>

            if (selectionIndex > -1) {
                cdSpeciality.selectionIndex = selectionIndex
                val speciality = specialities[selectionIndex]
                bdcFilterRequest.specialityId = speciality.genericItemId.getSafe()
                bdcFilterRequest.specialityName = speciality.genericItemName.getSafe()
            }

            cdSpeciality.onItemSelectedListener = { _, position: Int ->
                val speciality = specialities[position]
                bdcFilterRequest.specialityId = speciality.genericItemId.getSafe()
                bdcFilterRequest.specialityName = speciality.genericItemName.getSafe()
            }
        }
    }

    private fun setCityList(countryId: Int) {
        var cityIndex = -1
        val cityList = getCityList(countryId)
        val cityNameList = cityList?.map { it.name } as ArrayList<String>

        if (bdcFilterRequest.cityId != null) {
            cityIndex =
                cityList.indexOfFirst { it.id == bdcFilterRequest.cityId.getSafe() }.getSafe()
        }

        var selectionIndex = cityIndex
        if (cityNameList.size.getSafe() > 0) {

            mBinding.apply {
                cdCity.data = cityNameList

                if (selectionIndex == -1) selectionIndex = 0
                val city = cityList.get(selectionIndex)
                bdcFilterRequest.cityId = city.id.getSafe()
                bdcFilterRequest.cityName = city.name.getSafe()
                if (cityNameList.size > selectionIndex)
                    cdCity.selectionIndex = selectionIndex

                cdCity.onItemSelectedListener = { _, position: Int ->
                    val city = cityList.get(position)
                    bdcFilterRequest.cityId = city.id.getSafe()
                    bdcFilterRequest.cityName = city.name.getSafe()
                }
            }
        } else
            mBinding.cdCity.data = arrayListOf<String>()
    }

    private fun setSelectedService() {
        mBinding.apply {
            val id = bdcFilterRequest.serviceId.getSafe()
            serviceTypeView.setServiceType(id)
        }

    }

    private fun setDisabledServices(){
        val excludedServiceIds = metaData?.doctorServices?.filter { it.offerService.getBoolean().not() }?.map {
            it.genericItemId
        }

        mBinding.serviceTypeView.setDisabledServices(excludedServiceIds as ArrayList<Int>?)
    }

    override fun getFragmentLayout() = R.layout.fragment_doctor_filter

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentDoctorFilterBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            bSave.setOnClickListener {
                doctorConsultationViewModel.bdcFilterRequest = bdcFilterRequest
                findNavController().popBackStack()
            }
            serviceTypeView.onItemSelected = { item ->
                bdcFilterRequest.serviceId = item.id
                bdcFilterRequest.serviceName = metaData?.partnerServiceType?.find { it.id==item.id }?.shortName.getSafe()
            }
            cbAvailableForVideo.setOnClickListener {
                bdcFilterRequest.availableForVideoCall = cbAvailableForVideo.isChecked.getInt()
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun observe() {

    }
}