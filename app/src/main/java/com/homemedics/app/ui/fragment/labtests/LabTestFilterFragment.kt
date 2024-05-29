package com.homemedics.app.ui.fragment.labtests

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.labtest.LabTestFilterRequest
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentLabTestFilterBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.viewmodel.LabTestViewModel

class LabTestFilterFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentLabTestFilterBinding
    private val labTestViewModel: LabTestViewModel by activityViewModels()
    private var labTestFilterRequest = LabTestFilterRequest()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.globalString?.filter.getSafe()
            cdCountry.hint = lang?.globalString?.country.getSafe()
            cdCity.hint = lang?.globalString?.city.getSafe()
            cdCategory.hint = lang?.globalString?.category.getSafe()
        }
    }

    override fun init() {
        labTestFilterRequest = Gson().fromJson(
            Gson().toJson(labTestViewModel.labTestFilterRequest),
            LabTestFilterRequest::class.java
        )
        observe()
        initDropDowns()
    }

    private fun initDropDowns(){
        mBinding.apply {

            //country
            var countryIndex = -1
            if(labTestFilterRequest.countryId != null){
                countryIndex = metaData?.countries?.indexOfFirst { it.id == labTestFilterRequest.countryId.getSafe() }.getSafe()
            }

            val indexSelection = countryIndex
            val countryList = getCountryList()
            cdCountry.data = countryList as ArrayList<String>

            if(countryIndex > -1){
                cdCountry.selectionIndex = indexSelection
                val country = metaData?.countries?.get(indexSelection)
                labTestFilterRequest.countryId = country?.id.getSafe()
                labTestFilterRequest.countryName = country?.name.getSafe()
            } else {
                mBinding.cdCity.selectionIndex = 0 //  select 0 index city
            }

            cdCountry.onItemSelectedListener = { _, position: Int ->
                val country = metaData?.countries?.get(position)
                labTestFilterRequest.countryId = country?.id.getSafe()
                labTestFilterRequest.countryName = country?.name.getSafe()
                setCityList(country?.id.getSafe())
            }
            setCityList(metaData?.countries?.get(cdCountry.selectionIndex)?.id.getSafe())

            //category
            val categories = labTestViewModel.labTestCategories.value.getSafe()
            var categoryIndex = -1
            if(labTestFilterRequest.categoryId != null){
                categoryIndex = categories.indexOfFirst { it.genericItemId == labTestFilterRequest.categoryId.getSafe() }.getSafe()
            }

            val categoryIndexSelection = categoryIndex
            val categoryList = categories.map { it.genericItemName }
            cdCategory.data = categoryList as ArrayList<String>

            if(categoryIndex > -1){
                cdCategory.selectionIndex = categoryIndexSelection
                val category = categories[categoryIndexSelection]
                labTestFilterRequest.categoryId = category.genericItemId.getSafe()
                labTestFilterRequest.categoryName = category.genericItemName.getSafe()
            }

            cdCategory.onItemSelectedListener = { _, position: Int ->
                val category = categories[position]
                labTestFilterRequest.categoryId = category.genericItemId.getSafe()
                labTestFilterRequest.categoryName = category.genericItemName.getSafe()
            }
        }
    }

    private fun setCityList(countryId: Int) {
        var cityIndex = -1

        val cities = metaData?.cities?.filter { it.countryId == countryId }

        if(labTestFilterRequest.cityId != null){
            cityIndex = cities?.indexOfFirst { it.id == labTestFilterRequest.cityId.getSafe()}.getSafe()
        }

        val selectionIndex = cityIndex
        val cityList = getCityList(countryId)
        val cityNameList = cityList?.map { it.name } as ArrayList<String>
        if (cityNameList.size.getSafe() > 0) {

            mBinding.apply {
                cdCity.data = cityNameList

                if(selectionIndex > -1){
                    val city = cities?.get(selectionIndex)
                    labTestFilterRequest.cityId = city?.id.getSafe()
                    labTestFilterRequest.cityName = city?.name.getSafe()
                    if(cityNameList.size>selectionIndex)
                    cdCity.selectionIndex = selectionIndex
                }

                cdCity.onItemSelectedListener = { item, position: Int ->
                    val city = cities?.find { it.name == item }
                    labTestFilterRequest.cityId = city?.id.getSafe()
                    labTestFilterRequest.cityName = city?.name.getSafe()
                }
            }
        }else
            mBinding.cdCity.data = arrayListOf<String>()
    }

    override fun getFragmentLayout() = R.layout.fragment_lab_test_filter

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentLabTestFilterBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            bSave.setOnClickListener {
                labTestViewModel.labTestFilterRequest = labTestFilterRequest
                findNavController().popBackStack()
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun observe() {

    }
}