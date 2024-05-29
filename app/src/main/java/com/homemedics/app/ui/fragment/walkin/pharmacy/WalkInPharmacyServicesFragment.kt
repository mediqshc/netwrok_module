package com.homemedics.app.ui.fragment.walkin.pharmacy

import android.app.AlertDialog
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.walkin.QRCodeRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.walkin.WalkInQRCodeResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogMapGenericBinding
import com.homemedics.app.databinding.FragmentWalkinPharmacyServicesBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.utils.CaptureAct
import com.homemedics.app.utils.DialogUtils
import com.homemedics.app.utils.getErrorMessage
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.isOnline
import com.homemedics.app.utils.loadImage
import com.homemedics.app.utils.safeNavigate
import com.homemedics.app.viewmodel.WalkInViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


class WalkInPharmacyServicesFragment : BaseFragment() {

    private lateinit var mBinding: FragmentWalkinPharmacyServicesBinding
    private val walkInPharmacyViewModel: WalkInViewModel by activityViewModels()
    private var langData:RemoteConfigLanguage? = null
    private lateinit var dialogSelectPharmacy: DialogMapGenericBinding
    private lateinit var builder: AlertDialog
    private lateinit var dialogSaveButton: Button

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.walkInScreens?.walkInPharmacy.getSafe()
        }
    }

    override fun init() {
        mBinding.apply {
            val nearbyList = langData?.walkInScreens?.nearbyList.getSafe()
            val mapview = langData?.walkInScreens?.mapView.getSafe()
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(WalkInPharmacyNearByListFragment())
                add(WalkInPharmacyMapViewFragment())
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

    override fun getFragmentLayout(): Int = R.layout.fragment_walkin_pharmacy_services

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkinPharmacyServicesBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    closeKeypad()
                    findNavController().popBackStack()
                }
                onAction2Click = {
                    scanQRCode()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        walkInPharmacyViewModel.apply {
            isPharmacy = false
            isDiscountCenter = false
            walkInPharmacyListLiveData.value = null
            walkInPharmacyMapLiveData.value = null
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
                scanPharmacyQRCode(request)
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

    private fun showSelectPharmacyDialog(walkInItemQRCode: WalkInQRCodeResponse) {
        builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).apply {
            dialogSelectPharmacy = DialogMapGenericBinding.inflate(layoutInflater).apply {
                ivIcon.loadImage(walkInItemQRCode.qrDetails?.logo.getSafe(), R.drawable.ic_placeholder)
                tvTitle.text = walkInItemQRCode.qrDetails?.displayName.getSafe()
                tvDesc.text = walkInItemQRCode.qrDetails?.streetAddress.getSafe()
            }
            setView(dialogSelectPharmacy.root)
            setTitle(langData?.globalString?.selectPharmacy.getSafe())
            setPositiveButton(langData?.globalString?.select.getSafe()) { _, _ ->

            }
            setNegativeButton(langData?.globalString?.cancel.getSafe(), null)
        }.create()

        builder.setOnShowListener{
            dialogSaveButton = builder.getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton.setOnClickListener {
                walkInPharmacyViewModel.apply {
                    if (walkInItemQRCode.qrDetails != null) {
                        walkInItem = walkInItemQRCode.qrDetails!!
                    }
                    pharmacyId = walkInItemQRCode.qrDetails?.id
                    walkInPharmacyName = walkInItemQRCode.qrDetails?.displayName.getSafe()
                    cityId = walkInItemQRCode.qrDetails?.cityId.getSafe()
                    mapLatLng.apply {
                        latitude = walkInItemQRCode.qrDetails?.latitude.getSafe()
                        longitude = walkInItemQRCode.qrDetails?.longitude.getSafe()
                    }
                }
                if (walkInPharmacyViewModel.isDiscountCenter) {
                    findNavController().safeNavigate(WalkInPharmacyServicesFragmentDirections.actionWalkInPharmacyServicesFragmentToWalkInDiscountsCenterFragment())
                } else {
                    findNavController().safeNavigate(WalkInPharmacyServicesFragmentDirections.actionWalkInPharmacyServicesFragmentToWalkInPharmacyDetailsFragment())
                }

                builder.dismiss()
            }
        }
        builder.show()
    }
    private fun scanPharmacyQRCode(request: QRCodeRequest) {
        if (isOnline(requireActivity())) {
            walkInPharmacyViewModel.scanPharmacyQRCode(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        val qrCodeResponse = response.data as WalkInQRCodeResponse
                        showSelectPharmacyDialog(qrCodeResponse)
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
                    title = langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}