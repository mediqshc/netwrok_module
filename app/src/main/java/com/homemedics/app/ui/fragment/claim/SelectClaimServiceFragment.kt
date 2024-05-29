package com.homemedics.app.ui.fragment.claim

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.claim.ClaimServicesResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.pharmacy.PharmacyCategoriesResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogSelectCityPharmaBinding
import com.homemedics.app.databinding.FragmentSelectClaimServiceBinding
import com.homemedics.app.ui.adapter.DoctorConsultAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ClaimViewModel

class SelectClaimServiceFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentSelectClaimServiceBinding
    private lateinit var dialogViewBinding: DialogSelectCityPharmaBinding

    private lateinit var doctorConsultAdapter: DoctorConsultAdapter

    private val claimViewModel: ClaimViewModel by activityViewModels()
    val langData = ApplicationClass.mGlobalData

    override fun setLanguageData() {
        mBinding.langData = langData
        mBinding.apply {
            tvNoData.text = langData?.globalString?.noResultFound
            actionbar.title = langData?.claimScreen?.selectService.getSafe()
        }
    }

    override fun onResume() {
        super.onResume()
        claimViewModel.flushData()
    }

    override fun onDetach() {
        super.onDetach()
        claimViewModel.flushData()
    }

    override fun init() {
        observe()
        populateSpecialityList()
        getClaimServices()
        setActionbarCity()
    }

    override fun getFragmentLayout() = R.layout.fragment_select_claim_service

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectClaimServiceBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                showCityDialog()
            }

            doctorConsultAdapter.itemClickListener = { item, _ ->
                claimViewModel.claimRequest.claimCategoryId = item.genericItemId
                claimViewModel.category = item.genericItemName
                claimViewModel.partnerServiceId = item.partnerServiceId.getSafe()
                findNavController().safeNavigate(SelectClaimServiceFragmentDirections.actionSelectClaimServiceFragmentToSubmitClaimDetailsFragment())
            }

        }
    }

    private fun setActionbarCity() {
        //action bar city name
        if (claimViewModel.claimRequest.cityName.isNullOrEmpty()) {
            val user = DataCenter.getUser()
            user?.let {
                val city =
                    getCityList(it.countryId.getSafe())?.find { city -> city.id == it.cityId }
                mBinding.actionbar.desc = city?.name.getSafe()
                claimViewModel.claimRequest.cityId = city?.id.getSafe()
                claimViewModel.claimRequest.cityName = city?.name.getSafe()

                val countryIndex =
                    metaData?.countries?.indexOfFirst { country -> country.id == it.countryId.getSafe() }
                        .getSafe()

                if (countryIndex != -1) {
                    val country = metaData?.countries?.get(countryIndex)
                    claimViewModel.claimRequest.countryId = country?.id.getSafe()
                    claimViewModel.claimRequest.countryName =
                        country?.name.getSafe()
                    claimViewModel.countryId = country?.id.getSafe()
                }
            }
        } else mBinding.actionbar.desc = claimViewModel.claimRequest.cityName.getSafe()
    }

    private fun showCityDialog() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogViewBinding = DialogSelectCityPharmaBinding.inflate(layoutInflater)
            setView(dialogViewBinding.root)
            setPositiveButton(langData?.globalString?.select) { _, _ -> }
            setNegativeButton(langData?.globalString?.cancel, null)
            setCancelable(false)

            var indexSelection = 0

            dialogViewBinding.apply {
                dialogViewBinding.languData = langData
                cdCountry.hint = langData?.globalString?.country.getSafe()
                cdCity.hint = langData?.globalString?.city.getSafe()
                val countryList = getCountryList()
                cdCountry.data = countryList as ArrayList<String>

                if (claimViewModel.countryId != 0) {
                    val index = metaData?.countries?.indexOfFirst { it.id == claimViewModel.countryId }
                    if (index != -1) {
                        cdCountry.selectionIndex = index.getSafe()
                        claimViewModel.claimRequest.countryId = claimViewModel.countryId
                    }

                } else {
                    claimViewModel.claimRequest.countryId = metaData?.countries?.get(indexSelection)?.id.getSafe()
                }

                cdCountry.onItemSelectedListener = { _, position: Int ->
                    claimViewModel.claimRequest.countryId = metaData?.countries?.get(position)?.id.getSafe()
                    claimViewModel.claimRequest.cityId=0
                    setCityList(claimViewModel.claimRequest.countryId.getSafe())
                }

                setCityList(claimViewModel.countryId.getSafe())
            }
        }.create()
        builder.setOnShowListener {
            val dialogSaveButton = (builder as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                val countryId = claimViewModel.claimRequest.countryId
                val cityId = claimViewModel.claimRequest.cityId
                if(countryId==0 || cityId == null) {
                    Toast.makeText(requireContext(), langData?.dialogsStrings?.selectCity, Toast.LENGTH_SHORT).show()
                } else {
                    val city = getCityList(claimViewModel.claimRequest.countryId.getSafe())?.find { city -> city.id == claimViewModel.claimRequest.cityId }
                    val country = metaData?.countries?.find { it.id == claimViewModel.claimRequest.countryId }
                    claimViewModel.claimRequest.countryName = country?.name.getSafe()
                    claimViewModel.claimRequest.cityName = city?.name.getSafe()
                    mBinding.actionbar.desc = city?.name.getSafe()
                    getClaimServices()
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
            val index = cities.indexOfFirst { it.id == DataCenter.getUser()?.cityId }
            if (index != -1) {
                if (claimViewModel.cityId != 0) {
                    dialogViewBinding.cdCity.selectionIndex = claimViewModel.cityId
                    claimViewModel.claimRequest.cityId = dialogViewBinding.cdCity.selectionIndex
                } else {
                    dialogViewBinding.cdCity.selectionIndex = index.getSafe()
                    claimViewModel.claimRequest.cityId = dialogViewBinding.cdCity.selectionIndex
                }
            } else {
                dialogViewBinding.cdCity.selectionIndex =0
                claimViewModel.claimRequest.cityId = cities[0].id.getSafe()
                claimViewModel.claimRequest.cityName = cities[0].name.getSafe()
            }
        } else {
            claimViewModel.claimRequest.cityId = null
            claimViewModel.claimRequest.cityName = null
            dialogViewBinding.cdCity.data = arrayListOf()
            dialogViewBinding.cdCity. mBinding.dropdownMenu.setText("", true)
        }

        dialogViewBinding.cdCity.onItemSelectedListener = { _, position: Int ->
            claimViewModel.claimRequest.cityId = cities?.get(position)?.id.getSafe()
            claimViewModel.claimRequest.cityName = cities?.get(position)?.name.getSafe()
            claimViewModel.cityId = position
            dialogViewBinding.cdCity.clearFocus()
        }
    }

    override fun onClick(v: View?) {

    }

    private fun observe() {

    }

    private fun populateSpecialityList() {
        mBinding.apply {
            doctorConsultAdapter = DoctorConsultAdapter()
            rvSpeciality.adapter = doctorConsultAdapter
        }
    }

    private fun getClaimServices(){
        claimViewModel.getClaimServices(PageRequest(page = 0, cityId = claimViewModel.claimRequest.cityId)).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<ClaimServicesResponse>
                    response.data.let { categoriesList ->
                        doctorConsultAdapter.listItems = categoriesList?.claimServices as ArrayList<GenericItem>
                    }
                    claimViewModel.claimServicesResponse = response.data
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
                            message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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