package com.homemedics.app.ui.fragment.walkin.hospital

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.walkin.WalkInListRequest
import com.fatron.network_module.models.request.walkin.WalkInServicesFilterRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.claim.ClaimConnectionsResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.walkinpharmacy.WalkInPharmacyListResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentWalkInHospitalFilterBinding
import com.homemedics.app.utils.DialogUtils
import com.homemedics.app.utils.getErrorMessage
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.isOnline
import com.homemedics.app.viewmodel.WalkInViewModel

class WalkInHospitalFilterFragment : BaseFragment() {

    private lateinit var mBinding: FragmentWalkInHospitalFilterBinding
    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private var mWalkInServicesFilterRequest = WalkInServicesFilterRequest()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            layoutButtons.apply {
                actionbar.title = lang?.globalString?.filter.getSafe()
                cdHospitalServices.hint = lang?.walkInScreens?.hospitalServices.getSafe()
                cdPackageAccount.hint = lang?.walkInScreens?.packageAccount.getSafe()
                bClearFilter.text = lang?.globalString?.clearFilter.getSafe()
                bSave.text = lang?.globalString?.applyFilter.getSafe()
            }
        }
    }

    override fun init() {
        initDropDowns()
    }

    private fun initDropDowns() {
        mBinding.apply {
            layoutButtons.bSave.isEnabled = walkInViewModel.hosiptalServiceId != 0 && walkInViewModel.packageAccountId != 0
            layoutButtons.bClearFilter.isEnabled = walkInViewModel.hosiptalServiceId != 0 && walkInViewModel.packageAccountId != 0
        }
        setHospitalServicesData(metaData?.walkInHospitalCategories)
        setConnectionData(connection = walkInViewModel.filterClaimConnectionsResponse)
    }

    override fun getFragmentLayout() = R.layout.fragment_walk_in_hospital_filter

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkInHospitalFilterBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }

            layoutButtons.bSave.setOnClickListener {
                getWalkInHospitalList()
            }

            layoutButtons.bClearFilter.setOnClickListener {
                walkInViewModel.walkInServicesFilterRequest = WalkInServicesFilterRequest()
                mWalkInServicesFilterRequest = walkInViewModel.walkInServicesFilterRequest

                cdHospitalServices.data = arrayListOf()
                cdPackageAccount.data = arrayListOf()

                initDropDowns()

                cdHospitalServices.mBinding.dropdownMenu.setText("")
                cdPackageAccount.mBinding.dropdownMenu.setText("")

                llContainer.requestFocus()

                walkInViewModel.packageAccountId = 0
                walkInViewModel.hosiptalServiceId = 0
            }

            cdHospitalServices.onItemSelectedListener = { _, position: Int ->
                val selectedHospitalService = metaData?.walkInHospitalCategories?.get(position)
                walkInViewModel.hosiptalServiceId = selectedHospitalService?.genericItemId.getSafe()
                cdHospitalServices.selectionIndex = position
                layoutButtons.bSave.isEnabled = true
                layoutButtons.bClearFilter.isEnabled = true
            }
            cdPackageAccount.onItemSelectedListener = { _, position: Int ->
                val selectedPackageAccount = walkInViewModel.filterConnection?.get(position)
                walkInViewModel.packageAccountId = selectedPackageAccount?.id.getSafe()
                cdPackageAccount.selectionIndex = position
                layoutButtons.bSave.isEnabled = true
                layoutButtons.bClearFilter.isEnabled = true
            }
        }
    }

    private fun setHospitalServicesData(walkInHospitalServices: List<GenericItem>?) {
        mBinding.apply {
            var hospitalIndex = -1
            if (walkInViewModel.hosiptalServiceId != 0) {
                hospitalIndex =
                    walkInHospitalServices?.indexOfFirst { it.genericItemId == walkInViewModel.hosiptalServiceId.getSafe() }
                        .getSafe()
            }
            val indexSelection = hospitalIndex
            walkInHospitalServices?.let { walkInService ->
                val services = walkInService.getSafe()
                val hospitalService: List<String> = services.map {
                    it.genericItemName.getSafe()
                }
                cdHospitalServices.data = hospitalService.getSafe()

                if (hospitalIndex > -1) {
                    cdHospitalServices.selectionIndex = indexSelection
                    val hospitalsCategories = walkInHospitalServices[indexSelection]
                    walkInViewModel.hosiptalServiceId = hospitalsCategories.genericItemId.getSafe()
                }
            }
        }
    }

    private fun setConnectionData(connection: ClaimConnectionsResponse? = null) {
        var connectionIndex = -1
        if (walkInViewModel.packageAccountId != 0) {
            connectionIndex =
                connection?.walkInConnections?.indexOfFirst { it.genericItemId == walkInViewModel.packageAccountId.getSafe() }
                    .getSafe()
        }
        val indexSelection = connectionIndex
        walkInViewModel.filterConnection = connection?.walkInConnections.getSafe()
        mBinding.apply {
            connection?.let { walkInConnection ->
                val connections = walkInConnection.walkInConnections.getSafe()
                val packageAccounts: List<String> = connections.map {
                    if (it.company != null)
                        "${it.company?.genericItemName.getSafe()} - ${it.claimPackage?.name.getSafe()}"
                    else
                        "${it.insurance?.genericItemName.getSafe()} - ${it.claimPackage?.name.getSafe()}"
                }
                cdPackageAccount.data = packageAccounts.getSafe()

                if (connectionIndex > -1) {
                    cdPackageAccount.selectionIndex = indexSelection
                    val selectedConnection = connections[indexSelection]
                    walkInViewModel.packageAccountId = selectedConnection.genericItemId.getSafe()
                }
            }
        }
    }

    private fun getWalkInHospitalList() {
        val request = WalkInListRequest(
            latitude = walkInViewModel.currentLatLng?.latitude.getSafe(),
            longitude = walkInViewModel.currentLatLng?.longitude.getSafe(),
            service = if (walkInViewModel.hosiptalServiceId != 0) walkInViewModel.hosiptalServiceId else null,
            filterPackage = if (walkInViewModel.packageAccountId != 0) walkInViewModel.packageAccountId else null,
            page = 0
        )
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInHospitalList(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as WalkInPharmacyListResponse).let { hospitalList ->
                            walkInViewModel.walkInHospitalListLiveData.postValue(
                                hospitalList.walkInHospitals?.walkInList.getSafe()
                            )
                            walkInViewModel.walkInHospitalMapLiveData.postValue(
                                hospitalList.walkInHospitals?.walkInList.getSafe()
                            )
                            walkInViewModel.fromFilter = false
                            walkInViewModel.fromCode = false
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