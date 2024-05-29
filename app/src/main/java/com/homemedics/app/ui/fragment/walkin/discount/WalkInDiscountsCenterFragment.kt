package com.homemedics.app.ui.fragment.walkin.discount

import android.annotation.SuppressLint
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

import com.fatron.network_module.models.request.walkin.WalkInListRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.email.HospitalDiscountCenter
import com.fatron.network_module.models.response.pharmacy.HospitalDiscountCenterRequest
import com.fatron.network_module.models.response.walkin.WalkInDiscountsResponse
import com.fatron.network_module.models.response.walkin.WalkInQRCodeResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentWalkinDiscountsCenterBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.WalkInViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.logging.Handler

class WalkInDiscountsCenterFragment : BaseFragment() {

    private lateinit var mBinding : FragmentWalkinDiscountsCenterBinding
    private val walkInViewModel: WalkInViewModel by activityViewModels()
    private var locale: String? = null
    private var off: String? = null

    override fun getFragmentLayout() = R.layout.fragment_walkin_discounts_center

    @SuppressLint("SetTextI18n")
    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = walkInViewModel.walkInItem.displayName.getSafe()
            tvTermsCondition.text = "* ${lang?.globalString?.termsConditionApply.getSafe()}"
        }
    }

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkinDiscountsCenterBinding
    }

    @SuppressLint("SetTextI18n")
    override fun init() {
        locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        off = mBinding.lang?.globalString?.off.getSafe()


        walkInViewModel.apply {
            mBinding.apply {
                iDialogMapGeneric.apply {
                    tvTitle.text = walkInViewModel.walkInItem.displayName.getSafe()
                    tvAddress.text = walkInViewModel.walkInItem.streetAddress.getSafe()
                    tvDistance.apply {
                        setVisible(walkInItem.kilometer != null && walkInItem.kilometer != 0.0)
                        text = "${"%.2f".format(walkInItem.kilometer)} ${ApplicationClass.mGlobalData?.globalString?.kilometer.getSafe()}"
                    }
                    tvDesc.text = ApplicationClass.mGlobalData?.globalString?.discountCenter.getSafe()
                }
            }
        }

        if (walkInViewModel.isPharmacy) {
            getWalkInPharmacyDiscount(
                WalkInListRequest(
                    latitude = walkInViewModel.currentLatLng?.latitude.getSafe(),
                    longitude = walkInViewModel.currentLatLng?.longitude.getSafe(),
                    centerId = walkInViewModel.walkInItem.companyId.getSafe(),
                )
            )
        }
        if (walkInViewModel.isLab) {
            getWalkInLaboratoryDiscount(
                WalkInListRequest(
                    latitude = walkInViewModel.currentLatLng?.latitude.getSafe(),
                    longitude = walkInViewModel.currentLatLng?.longitude.getSafe(),
                    centerId = walkInViewModel.walkInItem.companyId.getSafe(),
                )
            )
        }
        if (walkInViewModel.isHospital) {
            getWalkInHospitalDiscount(
                WalkInListRequest(
                    latitude = walkInViewModel.currentLatLng?.latitude.getSafe(),
                    longitude = walkInViewModel.currentLatLng?.longitude.getSafe(),
                    centerId = walkInViewModel.walkInItem.companyId.getSafe(),
                )
            )
        }
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    findNavController().popBackStack()
                }
                onAction2Click = {
                    openDefaultMap(
                        walkInViewModel.mapLatLng, requireActivity()
                    )
                }
            }
        }

        mBinding.bSelect.setOnClickListener {


            if (!walkInViewModel.walkInLabDiscountCenter) {
                getWalkInHospitalDiscountCenter(
                    HospitalDiscountCenterRequest(
                        healthcare_id = walkInViewModel.hospitalId
                    )
                )

            }
            else {
                walkInLabDiscountCenter(
                     HospitalDiscountCenterRequest(

                         lab_id = walkInViewModel.labId
                     )
                 )

            }



        }






    }

    override fun onDetach(){
        super.onDetach()
        walkInViewModel.walkInLabDiscountCenter=false
        walkInViewModel.labDiscountCenterBooked=false
        walkInViewModel.hospitalDiscountCenterBooked=false
    }
    @SuppressLint("SetTextI18n")
    private fun setData(discount: WalkInDiscountsResponse) {
        val discountCenter = discount.discount
        mBinding.apply {
            tvDiscount.text =
                "${discountCenter?.promotionDiscountCenter?.percentage.getSafe()}% $off"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getWalkInPharmacyDiscount(request: WalkInListRequest) {
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInPharmacyDiscount(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        val discountResponse = response.data as WalkInDiscountsResponse
                        setData(discountResponse)
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        if (it.generalResponse.message?.contains("discount_not_available").getSafe()) {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                                    message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                    buttonCallback = {
                                        findNavController().popBackStack()
                                    },
                                    cancellable = false
                                )
                        } else {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                                    message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                    buttonCallback = {},
                                )
                        }
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

    @SuppressLint("SetTextI18n")
    private fun getWalkInLaboratoryDiscount(request: WalkInListRequest) {
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInLaboratoryDiscount(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        val discountResponse = response.data as WalkInDiscountsResponse
                        setData(discountResponse)
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        if (it.generalResponse.message?.contains("discount_not_available").getSafe()) {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                                    message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                    buttonCallback = {
                                        findNavController().popBackStack()
                                    },
                                    cancellable = false
                                )
                        } else {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                                    message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                    buttonCallback = {},
                                )
                        }
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

    @SuppressLint("SetTextI18n")
    private fun getWalkInHospitalDiscount(request: WalkInListRequest) {
        if (isOnline(requireActivity())) {
            walkInViewModel.getWalkInHospitalDiscount(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        val discountResponse = response.data as WalkInDiscountsResponse
                        setData(discountResponse)
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {},
                            )
                    }
                    is ResponseResult.ApiError -> {
                        if (it.generalResponse.message?.contains("discount_not_available").getSafe()) {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                                    message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                    buttonCallback = {
                                        findNavController().popBackStack()
                                    },
                                    cancellable = false
                                )
                        } else {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                                    message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                    buttonCallback = {},
                                )
                        }
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
    @SuppressLint("SetTextI18n")
    private fun getWalkInHospitalDiscountCenter(request: HospitalDiscountCenterRequest) {
        if (isOnline(requireActivity())) {
            walkInViewModel.getHospitalDiscountCenter(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        val discountResponse = response.data as HospitalDiscountCenter
                        walkInViewModel.hospitalDiscountCenterBooked=true

                        if(walkInViewModel.hospitalDiscountCenterBooked)
                        {   mBinding.bSelect.gone()
                            mBinding.tvDiscountConfirmationMsg.visible()
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
                        if (it.generalResponse.message?.contains("discount_not_available").getSafe()) {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                                    message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                    buttonCallback = {
                                        findNavController().popBackStack()
                                    },
                                    cancellable = false
                                )
                        } else {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = ApplicationClass.mGlobalData?.globalString?.information.getSafe(),
                                    message =  getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                    buttonCallback = {},
                                )
                        }
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
    private fun walkInLabDiscountCenter(request: HospitalDiscountCenterRequest) {
        if (isOnline(requireActivity())) {
            walkInViewModel.getLabDiscountCenter(request).observe(this) {
                when(it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        walkInViewModel.labDiscountCenterBooked=true

                        if(walkInViewModel.labDiscountCenterBooked)
                        {  mBinding.bSelect.gone()
                            mBinding.tvDiscountConfirmationMsg.visible()
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
           /* DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = lan?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )*/
        }
    }
}