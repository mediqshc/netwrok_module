package com.homemedics.app.ui.fragment.pharmacy

import android.app.AlertDialog
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.pharmacy.PharmacyCategoriesRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.pharmacy.PharmacyCategoriesResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogSelectCityPharmaBinding
import com.homemedics.app.databinding.FragmentPharmacySearchBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.adapter.HomeServiceAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.PharmacyViewModel

class PharmacySearchFragment : BaseFragment() {

    private lateinit var mBinding: FragmentPharmacySearchBinding
    private lateinit var dialogViewBinding: DialogSelectCityPharmaBinding
    private lateinit var pharmacyAdapter: HomeServiceAdapter
    private val pharmacyViewModel: PharmacyViewModel by activityViewModels()
    private var langData: RemoteConfigLanguage? = null


    override fun setLanguageData() {
        langData = ApplicationClass.mGlobalData
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = langData?.labPharmacyScreen?.pharmacyTitle.getSafe()
            iPharmaPrescription.apply {
                tvTitle.text = langData?.labPharmacyScreen?.doYouHaveAPrescription.getSafe()
                tvDesc.text = langData?.labPharmacyScreen?.uploadPrescriptionDesc.getSafe()
            }
            etSearch.hint = langData?.labPharmacyScreen?.eGPanadolNebulizerEnsure.getSafe()
            // default city and country if user not logged in.
            if (isUserLoggedIn().not())
                defaultCityAndCountry()
        }
    }

    override fun init() {
        setView()
        setActionbarCity()
        setObserver()
        getPharmacyCategoriesApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_pharmacy_search

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPharmacySearchBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    findNavController().popBackStack()
                }
                onAction2Click = {
                    showCityDialog()
                }
            }
            etSearch.apply {
                addTextChangedListener {
                    pharmacyAdapter.filter.filter(it)
                }
                etSearch.setOnClickListener {
                    pharmacyViewModel.fromSearch = true
                    pharmacyViewModel.categoryName = ""
                    findNavController().safeNavigate(
                        PharmacySearchFragmentDirections.actionPharmacySearchFragmentToPharmacyMedicineListFragment()
                    )
                }
            }
            iPharmaPrescription.root.setOnClickListener {
                pharmacyViewModel.bookingIdResponse.bookingId = null
                tinydb.remove(com.fatron.network_module.utils.Enums.TinyDBKeys.BOOKING_ID.key)
                pharmacyViewModel.products?.clear()
                findNavController().safeNavigate(
                    PharmacySearchFragmentDirections.actionPharmacySearchFragmentToPharmacyBookingFragment()
                )
            }
            pharmacyAdapter.itemClickListener = { item, _ ->
                pharmacyViewModel.apply {
                    fromSearch = false
                    selectedPharmacyCategory = item
                    categoryId = item.genericItemId.getSafe()
                    categoryName = item.genericItemName.getSafe()
                }
                findNavController().safeNavigate(
                    PharmacySearchFragmentDirections.actionPharmacySearchFragmentToPharmacyMedicineListFragment()
                )
            }
        }
    }

    private fun setView() {
        mBinding.apply {
            actionbar.action2Res = R.drawable.ic_expand
            pharmacyAdapter = HomeServiceAdapter()
            rvCategory.adapter = pharmacyAdapter
        }
    }

    private fun setObserver() {
        pharmacyViewModel.pharmacyCategories.observe(this) {
            mBinding.apply {
                rvCategory.setVisible((it.isNullOrEmpty().not()))
                tvNoData.setVisible((it.isNullOrEmpty()))
            }
            pharmacyAdapter.listItems = it
            pharmacyAdapter.originalList = it
        }
    }

    private fun showCityDialog() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogViewBinding = DialogSelectCityPharmaBinding.inflate(layoutInflater).apply {
                cdCountry.hint = langData?.globalString?.country.getSafe()
                cdCity.hint = langData?.globalString?.city.getSafe()
            }
            setView(dialogViewBinding.root)
            setPositiveButton(langData?.globalString?.select) { _, _ -> }
            setNegativeButton(langData?.globalString?.cancel, null)
            setCancelable(false)

            val indexSelection = 0

            dialogViewBinding.apply {
                dialogViewBinding.languData = langData
                val countryList = getCountryList()
                cdCountry.data = countryList as ArrayList<String>

                if (pharmacyViewModel.countryId != 0) {
                    val index = metaData?.countries?.indexOfFirst { it.id == pharmacyViewModel.countryId }
                    if (index != -1)
                        cdCountry.selectionIndex = index.getSafe()

                } else {
                    pharmacyViewModel.countryId = metaData?.countries?.get(indexSelection)?.id.getSafe()
                }

                cdCountry.onItemSelectedListener = { _, position: Int ->
                    pharmacyViewModel.countryId = metaData?.countries?.get(position)?.id.getSafe()
                    pharmacyViewModel.cityId=0
                    setCityList( pharmacyViewModel.countryId.getSafe())
                }

                setCityList(pharmacyViewModel.countryId.getSafe())
            }
        }.create()
        builder.setOnShowListener {
            val dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val countryId = pharmacyViewModel.countryId
                val cityId = pharmacyViewModel.cityId
                if(countryId==0 || cityId == null) {
                    Toast.makeText(requireContext(), langData?.dialogsStrings?.selectCity, Toast.LENGTH_SHORT).show()
                } else {
                    val city = getCityList(pharmacyViewModel.countryId)?.find { city -> city.id == pharmacyViewModel.cityId }
                    val country = metaData?.countries?.find { it.id == pharmacyViewModel.countryId }
                    pharmacyViewModel.country = country?.name.getSafe()
                    pharmacyViewModel.city = city?.name.getSafe()
                    mBinding.actionbar.desc = city?.name.getSafe()
                    getPharmacyCategoriesApi()
                    builder.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun setCityList(countryId: Int) {
        val cities = getCityList(countryId = countryId)
        val cityList = cities?.map { it.name }
        if (cityList?.size.getSafe() > 0) {
            dialogViewBinding.cdCity.data = arrayListOf()
            dialogViewBinding.cdCity.data = cityList as ArrayList<String>
            val index = cities.indexOfFirst { it.id == pharmacyViewModel.cityId }
            if (index != -1) {
                dialogViewBinding.cdCity.selectionIndex = index.getSafe()
            } else {
                dialogViewBinding.cdCity.selectionIndex =0
                pharmacyViewModel.cityId = cities[0].id.getSafe()
            }
        } else {
            pharmacyViewModel.cityId = null
            pharmacyViewModel.city = ""
            dialogViewBinding.cdCity.data = arrayListOf()
            dialogViewBinding.cdCity. mBinding.dropdownMenu.setText("", true)
        }

        dialogViewBinding.cdCity.onItemSelectedListener = { _, position: Int ->
            pharmacyViewModel.cityId = cities?.get(position)?.id.getSafe()
            pharmacyViewModel.city = cities?.get(position)?.name.getSafe()
            dialogViewBinding.cdCity.clearFocus()
        }
    }

    private fun defaultCityAndCountry() {
        val city = getCityList(metaData?.defaultCountryId.getSafe())?.find { city -> city.id == metaData?.defaultCityId.getSafe() }
        val country = metaData?.countries?.find { it.id == metaData?.defaultCountryId.getSafe() }
        pharmacyViewModel.country = country?.name.getSafe()
        pharmacyViewModel.city = city?.name.getSafe()
        mBinding.actionbar.desc = city?.name.getSafe()
    }

    private fun setActionbarCity() {
        if(pharmacyViewModel.city.getSafe().isEmpty()) {
            val user = DataCenter.getUser()
            user?.let {
                val city = getCityList(it.countryId.getSafe())?.find { city -> city.id == it.cityId }
                mBinding.actionbar.desc = city?.name.getSafe()
                pharmacyViewModel.cityId = city?.id.getSafe()
                pharmacyViewModel.city = city?.name.getSafe()

                val countryIndex = metaData?.countries?.indexOfFirst { country -> country.id == it.countryId.getSafe() }.getSafe()
                if (countryIndex != -1) {
                    val country = metaData?.countries?.get(countryIndex)
                    pharmacyViewModel.countryId = country?.id.getSafe()
                    pharmacyViewModel.country = country?.name.getSafe()
                }
            }
        } else {
            mBinding.actionbar.desc = pharmacyViewModel.city.getSafe()
        }
    }

    private fun getPharmacyCategoriesApi() {
        val request = PharmacyCategoriesRequest(page = 0, cityId = pharmacyViewModel.cityId)
        if (isOnline(requireActivity())) {
            pharmacyViewModel.getPharmacyCategories(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as PharmacyCategoriesResponse).let { categoriesList ->
                            pharmacyViewModel.pharmacyCategories.postValue(
                                categoriesList.data as ArrayList<GenericItem>
                            )
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