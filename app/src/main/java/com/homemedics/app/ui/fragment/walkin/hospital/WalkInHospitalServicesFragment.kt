package com.homemedics.app.ui.fragment.walkin.hospital

import android.app.AlertDialog
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.walkin.QRCodeRequest
import com.fatron.network_module.models.request.walkin.WalkInConnectionRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.claim.ClaimConnectionsResponse
import com.fatron.network_module.models.response.walkin.WalkInQRCodeResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogMapGenericBinding
import com.homemedics.app.databinding.FragmentWalkinHospitalServicesBinding
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.ui.fragment.walkin.pharmacy.WalkInPharmacyServicesFragmentDirections
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.WalkInViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class WalkInHospitalServicesFragment : BaseFragment() {

    private lateinit var mBinding: FragmentWalkinHospitalServicesBinding
    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private lateinit var dialogSelectHospital: DialogMapGenericBinding
    private lateinit var builder: AlertDialog
    private lateinit var dialogSaveButton: Button

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.walkInScreens?.walkInHospital.getSafe()
        }
    }

    override fun init() {
        getAccountsList()
        mBinding.apply {
            val nearbyList = langData?.walkInScreens?.nearbyList.getSafe()
            val mapview = langData?.walkInScreens?.mapView.getSafe()
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(WalkInHospitalNearByListFragment())
                add(WalkInHospitalMapViewFragment())
            }
            viewPager.adapter = TabsPagerAdapter(childFragmentManager, lifecycle)
            viewPager.isUserInputEnabled = false
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> nearbyList
                    1 -> mapview
                    else -> {
                        ""
                    }
                }
            }.attach()
        }
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_walkin_hospital_services

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkinHospitalServicesBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    closeKeypad()
                    findNavController().popBackStack()
                }
                onAction2Click = {
                    walkInViewModel.fromCode = true
                    scanQRCode()
                }
                onAction3Click = {
                    walkInViewModel.fromCode = false
                    findNavController().safeNavigate(WalkInHospitalServicesFragmentDirections.actionWalkInHospitalServicesFragmentToWalkInHospitalFilterFragment())
                }
            }
        }
    }

    private fun scanQRCode() {
        val options = ScanOptions()
        options.setPrompt("")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.captureActivity = CaptureAct::class.java
        qrCodeLauncher.launch(options)
    }

    private val qrCodeLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            try {
                val data = Gson().fromJson(result.contents.toString(), QRCodeRequest::class.java)
                val request = QRCodeRequest(branch_id = data.branch_id, id = data.id)
                scanHospitalQRCode(request)
            } catch (e: JsonSyntaxException) {
                DialogUtils(requireActivity())
                    .showSingleButtonAlertDialog(
                        title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                        message =  ApplicationClass.mGlobalData?.messages?.not_valid_qr_code.getSafe(),
                        buttonCallback = {},
                    )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        walkInViewModel.isHospital = false
        walkInViewModel.isDiscountCenter = false
        walkInViewModel.walkInHospitalListLiveData.value = null
        walkInViewModel.walkInHospitalMapLiveData.value = null
        walkInViewModel.fromFilter = false
        walkInViewModel.filterClaimConnectionsResponse = null
        walkInViewModel.hosiptalServiceId = 0
        walkInViewModel.packageAccountId = 0
    }

    private fun getAccountsList() {
        val request = WalkInConnectionRequest(
            bookingId = null,
            partnerServiceId = null,
            filter = 1
        )
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInHospitalConnections(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        walkInViewModel.filterClaimConnectionsResponse = response.data as ClaimConnectionsResponse
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
                    is ResponseResult.Pending -> {}
                    is ResponseResult.Complete -> {}
                    else -> {}
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message = ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun showSelectHospitalDialog(walkInScanItem: WalkInQRCodeResponse) {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogSelectHospital = DialogMapGenericBinding.inflate(layoutInflater).apply {
                ivIcon.loadImage(walkInScanItem.qrDetails?.logo.getSafe(), R.drawable.ic_placeholder)
                tvTitle.text = walkInScanItem.qrDetails?.displayName.getSafe()
                tvDesc.text = walkInScanItem.qrDetails?.streetAddress.getSafe()
            }
            setView(dialogSelectHospital.root)
            setTitle(mBinding.langData?.globalString?.selectHospital.getSafe())
            setPositiveButton(mBinding.langData?.globalString?.select.getSafe()) { _, _ ->

            }
            setNegativeButton(mBinding.langData?.globalString?.cancel.getSafe(), null)
        }.create()

        builder.setOnShowListener{
            dialogSaveButton = builder.getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                walkInViewModel.apply {
                    if (walkInScanItem.qrDetails != null) {
                        walkInItem = walkInScanItem.qrDetails!!
                    }
                    hospitalId = walkInScanItem.qrDetails?.id
                    walkInHospitalName = walkInScanItem.qrDetails?.displayName.getSafe()
                    cityId = walkInScanItem.qrDetails?.cityId.getSafe()
                    mapLatLng.apply {
                        latitude = walkInScanItem.qrDetails?.latitude.getSafe()
                        longitude = walkInScanItem.qrDetails?.longitude.getSafe()
                    }
                }
                if (walkInViewModel.isDiscountCenter) {
                    findNavController().safeNavigate(WalkInHospitalServicesFragmentDirections.actionWalkInHospitalServicesFragmentToWalkInDiscountsCenterFragment3())
                } else {
                    findNavController().safeNavigate(WalkInHospitalServicesFragmentDirections.actionWalkInHospitalServicesFragmentToWalkInHospitalSelectServiceFragment())
                }

                builder.dismiss()
            }
        }
        builder.show()
    }

    private fun scanHospitalQRCode(request: QRCodeRequest) {
        if (isOnline(requireActivity())) {
            walkInViewModel.scanHospitalQRCode(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        val qrCodeResponse = response.data as WalkInQRCodeResponse
                        showSelectHospitalDialog(qrCodeResponse)
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
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}