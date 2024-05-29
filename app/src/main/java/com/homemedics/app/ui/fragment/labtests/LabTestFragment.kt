package com.homemedics.app.ui.fragment.labtests

import android.app.AlertDialog
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.pharmacy.PharmacyCategoriesResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogSelectCityPharmaBinding
import com.homemedics.app.databinding.FragmentLabTestBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.adapter.HomeServiceAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.LabTestViewModel
import java.net.SocketTimeoutException

class LabTestFragment : BaseFragment() {

    private lateinit var mBinding: FragmentLabTestBinding
    private lateinit var dialogViewBinding: DialogSelectCityPharmaBinding
    private lateinit var categoryAdapter: HomeServiceAdapter
    private val labTestViewModel: LabTestViewModel by activityViewModels()
    private var langData: RemoteConfigLanguage? = null


    override fun setLanguageData() {
        langData = ApplicationClass.mGlobalData
        mBinding.apply {
            lang = langData
            actionbar.title = langData?.labPharmacyScreen?.labTest.getSafe()
            iUploadPrescription.apply {
                tvTitle.text = langData?.labPharmacyScreen?.doYouHaveAPrescription.getSafe()
                tvDesc.text = langData?.labPharmacyScreen?.prescriptionDescLabCart.getSafe()
            }
            etSearch.hint = langData?.labPharmacyScreen?.searchLabTest.getSafe()
        }
//        // default city and country if user not logged in.
        if (isUserLoggedIn().not())
            defaultCityAndCountry()
    }

    override fun init() {
        val labBookingId = TinyDB.instance.getInt(Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
        if (labBookingId == 0) {
            labTestViewModel.flushData(true)
        }
        setView()
        setActionbarCity()
        setObserver()

        getLabTestCategoriesApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_lab_test

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentLabTestBinding
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
            etSearch.setOnClickListener {
                labTestViewModel.fromSearch = true
                labTestViewModel.fromBLT = false
//                labTestViewModel.labTestFilterRequest.partnerType = Enums.Profession.DOCTOR.key
                findNavController().safeNavigate(LabTestFragmentDirections.actionLabTestFragmentToLabTestListFragment())

            }

            iUploadPrescription.root.setOnClickListener {
                tinydb.remove(Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
                labTestViewModel.flushData(true)
                labTestViewModel.labTestFilterRequest.labTestId = null
                labTestViewModel.fromBLT = true
                findNavController().safeNavigate(LabTestFragmentDirections.actionLabTestFragmentToLabTestBookingFragment())
            }

            categoryAdapter.itemClickListener = { item, _ ->
                labTestViewModel.fromBLT = false
                labTestViewModel.labTestFilterRequest.categoryId = item.genericItemId
                labTestViewModel.labTestFilterRequest.categoryName = item.genericItemName
                findNavController().safeNavigate(LabTestFragmentDirections.actionLabTestFragmentToLabTestListFragment())
            }
        }
    }

    private fun defaultCityAndCountry() {
        val city =
            getCityList(metaData?.defaultCountryId.getSafe())?.find { city -> city.id == metaData?.defaultCityId.getSafe() }
        val country = metaData?.countries?.find { it.id == metaData?.defaultCountryId.getSafe() }
        labTestViewModel.labTestFilterRequest.countryId = country?.id.getSafe()
        labTestViewModel.labTestFilterRequest.countryName = country?.name.getSafe()
        labTestViewModel.labTestFilterRequest.cityId = city?.id.getSafe()
        labTestViewModel.labTestFilterRequest.cityName = city?.name.getSafe()
        mBinding.actionbar.desc = city?.name.getSafe()
    }

    private fun setView() {
        mBinding.apply {
            iUploadPrescription.tvDesc.text =
                langData?.labPharmacyScreen?.prescriptionDescLabCart.getSafe()
            actionbar.action2Res = R.drawable.ic_expand
            categoryAdapter = HomeServiceAdapter()
            rvCategory.adapter = categoryAdapter
        }
    }

    private fun setObserver() {
        labTestViewModel.labTestCategories.observe(this) {
            mBinding.apply {
                rvCategory.setVisible((it.isNullOrEmpty().not()))
                tvNoData.setVisible((it.isNullOrEmpty()))
            }
            categoryAdapter.listItems = it
            categoryAdapter.originalList = it
        }
    }

    private fun showCityDialog() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogViewBinding = DialogSelectCityPharmaBinding.inflate(layoutInflater)
            setView(dialogViewBinding.root)
            setPositiveButton(langData?.globalString?.select) { _, _ -> }
            setNegativeButton(langData?.globalString?.cancel, null)
            setCancelable(false)

            val indexSelection = 0

            dialogViewBinding.apply {
                dialogViewBinding.languData = langData
                cdCountry.hint = langData?.globalString?.country.getSafe()
                cdCity.hint = langData?.globalString?.city.getSafe()
                val countryList = getCountryList()
                cdCountry.data = countryList as ArrayList<String>

                if (labTestViewModel.labTestFilterRequest.countryId != 0) {
                    val index =
                        metaData?.countries?.indexOfFirst { it.id == labTestViewModel.labTestFilterRequest.countryId }
                    if (index != -1)
                        cdCountry.selectionIndex = index.getSafe()

                } else {
                    labTestViewModel.labTestFilterRequest.countryId =
                        metaData?.countries?.get(indexSelection)?.id.getSafe()
                }

                cdCountry.onItemSelectedListener = { _, position: Int ->
                    labTestViewModel.labTestFilterRequest.countryId =
                        metaData?.countries?.get(position)?.id.getSafe()
                    labTestViewModel.labTestFilterRequest.cityId = 0
                    setCityList(labTestViewModel.labTestFilterRequest.countryId.getSafe())
                }

                setCityList(labTestViewModel.labTestFilterRequest.countryId.getSafe())
            }
        }.create()
        builder.setOnShowListener {
            val dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val countryId = labTestViewModel.labTestFilterRequest.countryId
                val cityId = labTestViewModel.labTestFilterRequest.cityId
                if (countryId == 0 || cityId == null) {
                    Toast.makeText(
                        requireContext(),
                        langData?.dialogsStrings?.selectCity,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val city =
                        getCityList(labTestViewModel.labTestFilterRequest.countryId.getSafe())?.find { city -> city.id == labTestViewModel.labTestFilterRequest.cityId }
                    val country =
                        metaData?.countries?.find { it.id == labTestViewModel.labTestFilterRequest.countryId }
                    labTestViewModel.labTestFilterRequest.countryName = country?.name.getSafe()
                    labTestViewModel.labTestFilterRequest.cityName = city?.name.getSafe()
                    mBinding.actionbar.desc = city?.name.getSafe()
                    getLabTestCategoriesApi()
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
            val index =
                cities.indexOfFirst { it.id == labTestViewModel.labTestFilterRequest.cityId }
            if (index != -1) {
                dialogViewBinding.cdCity.selectionIndex = index.getSafe()
            } else {
                dialogViewBinding.cdCity.selectionIndex = 0
                labTestViewModel.labTestFilterRequest.cityId = cities[0].id.getSafe()
                labTestViewModel.labTestFilterRequest.cityName = cities[0].name.getSafe()
            }
        } else {
            labTestViewModel.labTestFilterRequest.cityId = null
            labTestViewModel.labTestFilterRequest.cityName = null
            dialogViewBinding.cdCity.data = arrayListOf()
            dialogViewBinding.cdCity.mBinding.dropdownMenu.setText("", true)
        }

        dialogViewBinding.cdCity.onItemSelectedListener = { _, position: Int ->
            labTestViewModel.labTestFilterRequest.cityId = cities?.get(position)?.id.getSafe()
            labTestViewModel.labTestFilterRequest.cityName = cities?.get(position)?.name.getSafe()
            dialogViewBinding.cdCity.clearFocus()
        }
    }

    private fun setActionbarCity() {
        if (labTestViewModel.labTestFilterRequest.cityName.getSafe().isEmpty()) {
            val user = DataCenter.getUser()
            user?.let {
                val city =
                    getCityList(it.countryId.getSafe())?.find { city -> city.id == it.cityId }
                mBinding.actionbar.desc = city?.name.getSafe()
                labTestViewModel.labTestFilterRequest.cityId = city?.id.getSafe()
                labTestViewModel.labTestFilterRequest.cityName = city?.name.getSafe()

                val countryIndex =
                    metaData?.countries?.indexOfFirst { country -> country.id == it.countryId.getSafe() }
                        .getSafe()
                val country = metaData?.countries?.get(countryIndex)
                labTestViewModel.labTestFilterRequest.countryId = country?.id.getSafe()
                labTestViewModel.labTestFilterRequest.countryName = country?.name.getSafe()
            }
        } else {
            mBinding.actionbar.desc = labTestViewModel.labTestFilterRequest.cityName.getSafe()
        }
    }

    private fun getLabTestCategoriesApi() {
        if (isOnline(requireContext())) {


            labTestViewModel.getLabTestCategories(
                PageRequest(
                    page = 0,
                    cityId = labTestViewModel.labTestFilterRequest.cityId
                )
            ).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as PharmacyCategoriesResponse).let { categoriesList ->
                            labTestViewModel.labTestCategories.postValue(
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
                    else -> {
                        hideLoader()
                    }
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError ?: getString(
                        R.string.error_internet
                    ),
                    message = ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg
                        ?: getString(R.string.internet_error),
                    buttonCallback = {},
                )
        }
    }
}