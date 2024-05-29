package com.homemedics.app.ui.fragment.pharmacy

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.pharmacy.PharmacyCategoriesRequest
import com.fatron.network_module.models.request.pharmacy.PharmacyProductRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.pharmacy.PharmacyCategoriesResponse
import com.fatron.network_module.models.response.pharmacy.PharmacyProduct
import com.fatron.network_module.models.response.pharmacy.PharmacyProductsListResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentMedicinesFilterBinding
import com.homemedics.app.utils.DialogUtils
import com.homemedics.app.utils.getErrorMessage
import com.homemedics.app.utils.getSafe
import com.homemedics.app.viewmodel.PharmacyViewModel


class PharmacyMedicinesFilterFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentMedicinesFilterBinding
    private val pharmacyViewModel: PharmacyViewModel by activityViewModels()
    private var overTheCounter = 0

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
        observe()
        getPharmacyCategoriesApi()
        initDropDowns()
    }

    override fun getFragmentLayout() = R.layout.fragment_medicines_filter

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentMedicinesFilterBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            cbOverCounter.setOnCheckedChangeListener { _, isChecked ->
                overTheCounter = if (isChecked) 1 else 0
            }
            bApplyFilter.setOnClickListener(this@PharmacyMedicinesFilterFragment)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bApplyFilter -> applyFilterList()
        }
    }

    private fun observe() {

    }

    private fun initDropDowns(){
        mBinding.apply {
            //country
            var countryIndex = -1
            if(pharmacyViewModel.countryId != 0){
                countryIndex = metaData?.countries?.indexOfFirst { it.id == pharmacyViewModel.countryId.getSafe() }.getSafe()
            }

            val indexSelection = countryIndex
            val countryList = getCountryList()
            cdCountry.data = countryList as ArrayList<String>

            if(countryIndex > -1){
                cdCountry.selectionIndex = indexSelection
                val country = metaData?.countries?.get(indexSelection)
                pharmacyViewModel.countryId = country?.id.getSafe()
                pharmacyViewModel.country = country?.name.getSafe()
            }

            cdCountry.onItemSelectedListener = { _, position: Int ->
                val country = metaData?.countries?.get(position)
                pharmacyViewModel.countryId = country?.id.getSafe()
                pharmacyViewModel.country = country?.name.getSafe()
                setCityList(country?.id.getSafe())
            }
            setCityList(metaData?.countries?.get(cdCountry.selectionIndex)?.id.getSafe())

            cdCategory.onItemSelectedListener = { _, position: Int ->
                val category = pharmacyViewModel.categories[position]
                pharmacyViewModel.categoryId = category.genericItemId.getSafe()
                pharmacyViewModel.categoryName = category.genericItemName.getSafe()
                setCategory(position)
            }
        }
    }

    private fun setCityList(countryId: Int) {
        var cityIndex = -1

        val cities = metaData?.cities?.filter { it.countryId == countryId }

        if(pharmacyViewModel.cityId != null){
            cityIndex = cities?.indexOfFirst { it.id == pharmacyViewModel.cityId.getSafe() }.getSafe()
        }

        val selectionIndex = cityIndex
        val cityList = getCityList(countryId)
        val cityNameList = cityList?.map { it.name } as ArrayList<String>
        if (cityNameList.size.getSafe() > 0) {

            mBinding.apply {
                cdCity.data = cityNameList

                if(selectionIndex > -1){
                    val city = cities?.get(selectionIndex)
                    pharmacyViewModel.cityId = city?.id.getSafe()
                    pharmacyViewModel.city = city?.name.getSafe()
                    if(cityNameList.size>selectionIndex)
                        cdCity.selectionIndex = selectionIndex
                }else {
                    mBinding.cdCity.selectionIndex = 0 //  select 0 index city
                }

                cdCity.onItemSelectedListener = { item, _: Int ->
                    val city = cities?.find { it.name == item }
                    pharmacyViewModel.cityId = city?.id.getSafe()
                    pharmacyViewModel.city = city?.name.getSafe()
                }
            }
        } else {
            mBinding.cdCity.data = arrayListOf()
        }
    }

    private fun setCategory(selectionIndex: Int) {
        if(selectionIndex > -1){
            val categories = pharmacyViewModel.categories[selectionIndex]
            pharmacyViewModel.categoryId = categories.genericItemId.getSafe()
            pharmacyViewModel.categoryName = categories.genericItemName.getSafe()
            if(pharmacyViewModel.categories.size > selectionIndex)
                mBinding.cdCategory.selectionIndex = selectionIndex
        }
    }

    private fun getPharmacyCategoriesApi() {
        val request = PharmacyCategoriesRequest(page = 0)
        pharmacyViewModel.getPharmacyCategories(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<*>
                    (response.data as PharmacyCategoriesResponse).let { categoriesList ->
                        pharmacyViewModel.categories = categoriesList.data as ArrayList<GenericItem>
                        val categories = categoriesList.data?.map { cat -> cat.genericItemName } as ArrayList<String>
                        mBinding.cdCategory.data = categories
                        val index = categoriesList.data?.indexOfFirst { it.genericItemId == pharmacyViewModel.categoryId }?:-1
                        if(index!=-1)
                            setCategory(index)
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
                            message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
    }

    private fun applyFilterList() {
        val request = PharmacyProductRequest(categoryId = pharmacyViewModel.categoryId, overTheCounter = overTheCounter, page = 1)
        pharmacyViewModel.getPharmacyProductsList(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<*>
                    (response.data as PharmacyProductsListResponse).let { pharmacyProducts ->
                        pharmacyViewModel.pharmacyProducts.postValue(
                            pharmacyProducts.products as ArrayList<PharmacyProduct>
                        )
                        findNavController().popBackStack()
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
                            message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
    }
}