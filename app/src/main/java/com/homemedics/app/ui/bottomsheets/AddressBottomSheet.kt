package com.homemedics.app.ui.bottomsheets

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.user.UserLocationResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseBottomSheetFragment
import com.homemedics.app.databinding.FragmentAddressDetailBinding
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ProfileViewModel

class AddressBottomSheet : BaseBottomSheetFragment()  {
    private lateinit var mBinding: FragmentAddressDetailBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            etStreet.hint = lang?.locationString?.street.getSafe()
            etFloor.hint = lang?.locationString?.floor.getSafe()
            cdCategory.hint = lang?.globalString?.category.getSafe()
            etOther.hint = lang?.globalString?.other.getSafe()
        }
    }

    override fun init() {
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
        isCancelable = true

        val displayMetrics = requireActivity().resources.displayMetrics
        val height = displayMetrics.heightPixels
        val maxHeight = (height * 0.70).toInt()

        val layoutParams =
            mBinding.addressLayout.layoutParams as FrameLayout.LayoutParams
        layoutParams.height = maxHeight
        mBinding.addressLayout.layoutParams = layoutParams

        mBinding.addressModel = profileViewModel.pickedAddressModel
//        mBinding.addressModel = profileViewModel.address.value

        val categoryList = metaData.locationCategories?.map { it.genericItemName } as ArrayList<String>
        mBinding.cdCategory.data = categoryList
    }

    override fun getFragmentLayout(): Int  = R.layout.fragment_address_detail

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddressDetailBinding
    }

    override fun setListeners() {
        mBinding.apply {
            cdCategory.onItemSelectedListener = { category: String, position: Int ->
//                profileViewModel.address.value?.apply {
                profileViewModel.pickedAddressModel.apply {
                    this.categoryId =  metaData.locationCategories?.get(position)?.genericItemId.getSafe()
                }
                etOther.setVisible(category == mBinding.lang?.globalString?.other.getSafe())
            }

            bSaveCont.setOnClickListener {
                validation()

            }
        }
    }

    private fun validation() {

        profileViewModel.pickedAddressModel.apply {

            if ( streetAddress.isEmpty()) {
                mBinding.etStreet.errorText = mBinding.lang?.fieldValidationStrings?.streetEmpty.getSafe()
                mBinding.etStreet.requestFocus()
                return
            }

            if ( category.isEmpty()) {
                mBinding.cdCategory.errorText = mBinding.lang?.fieldValidationStrings?.categoryEmpty.getSafe()
                mBinding.etStreet.requestFocus()
                return
            }

            if (mBinding.etOther.isVisible && other.isNullOrEmpty() ||
                category.uppercase().contains(mBinding.lang?.globalString?.other?.uppercase().getSafe()) && other.isNullOrEmpty()) {

                mBinding.etOther.errorText = mBinding.lang?.fieldValidationStrings?.otherEmpty.getSafe()
                mBinding.etOther.requestFocus()
                return
        }


            var category = category
            if (category.uppercase().getSafe().contains(mBinding.lang?.globalString?.other?.uppercase().getSafe()))
                category = other.getSafe()

            title = category
            desc = address
            drawable = R.drawable.ic_location_trans
        }

        val fromBDC = arguments?.getBoolean("fromBDC")
        if (fromBDC.getSafe()) {
            callSaveAddressApi()
        } else {
//            profileViewModel.address.value?.let { it1 ->
            profileViewModel.pickedAddressModel.let { it1 ->
                profileViewModel.addresses.add(
                    it1
                )
            }
            findNavController().safeNavigate(AddressBottomSheetDirections.actionAddressBottomSheetToPersonalProfileFragment())
        }
    }

    private fun callSaveAddressApi() {
//        val userLocation = profileViewModel.address.value
        val userLocation = profileViewModel.pickedAddressModel
        var userLoc: UserLocation? = null
        userLocation.apply {
            userLoc = UserLocation(
                address,
                categoryId.toString(),
                floor,
                latitude.toString(),
                longitude.toString(),
                other,
                streetAddress,
                region,
                DataCenter.getUser()?.id.toString(),
                sublocality = subLocality
            )
        }
        userLoc?.let {
            if (isOnline(requireActivity())) {
                profileViewModel.saveAddressCall(it).observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            hideLoader()
                            val response = it.data as ResponseGeneral<UserLocationResponse>
                            val user = DataCenter.getUser()
                            user?.let {
                                it.userLocations= response.data?.userLocation
                                TinyDB.instance.putObject(Enums.TinyDBKeys.USER.key, it)
                            }

                            findNavController().safeNavigate(AddressBottomSheetDirections.actionAddressBottomSheetToPersonalProfileFragment())


                        }
                        is ResponseResult.Failure -> {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    message = it.error.message.getSafe(),
                                    buttonCallback = {
                                        findNavController().popBackStack()
                                    },
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener {
            val bottomSheet =
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.skipCollapsed = true
            val dp = resources.getDimensionPixelSize(R.dimen.dp60)
            behavior.peekHeight = dp
            behavior.isDraggable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return bottomSheetDialog

    }

}