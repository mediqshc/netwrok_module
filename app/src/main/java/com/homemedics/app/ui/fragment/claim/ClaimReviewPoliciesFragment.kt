package com.homemedics.app.ui.fragment.claim

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.claim.ClaimConnectRequest
import com.fatron.network_module.models.request.claim.ClaimConnectionExclusionRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.claim.ClaimConnectResponse
import com.fatron.network_module.models.response.claim.ClaimConnectionExclusionResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentClaimReviewPoliciesBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ClaimViewModel

class ClaimReviewPoliciesFragment : BaseFragment(), View.OnClickListener {
    private lateinit var mBinding: FragmentClaimReviewPoliciesBinding
    private val claimViewModel: ClaimViewModel by activityViewModels()

    private var langData: RemoteConfigLanguage? = ApplicationClass.mGlobalData

    override fun init() {
        getConnectionExclusions()
        setObserver()
    }

    override fun setLanguageData() {
        mBinding.apply {
            actionbar.title =  langData?.claimScreen?.reviewExclusion.getSafe()
            tvProductHeading.text = lang?.claimScreen?.headingClaimExclusion?.replace("[0]", claimViewModel.category.getSafe())
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_claim_review_policies

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentClaimReviewPoliciesBinding
        mBinding.lang = langData
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            bConfirm.setOnClickListener {
                val request = ClaimConnectRequest(
                    claimId = claimViewModel.claimResponse?.claim?.claimId,
                    bookingId = claimViewModel.claimResponse?.claim?.bookingId,
                    claimConnectionId = claimViewModel.selectedConnection?.id,
                    claimCategoryId = claimViewModel.claimRequest.claimCategoryId
                )
                connectClaim(request)
            }
        }
    }

    private fun setObserver() {

    }

    override fun onClick(v: View?) {

    }

    private fun getConnectionExclusions() {
        val request = ClaimConnectionExclusionRequest(
            packageId = claimViewModel.selectedConnection?.packageId?.toInt(),
            partnerServiceId = claimViewModel.partnerServiceId,
            claimCategoryId = claimViewModel.claimRequest.claimCategoryId
        )
        if (isOnline(requireActivity())) {
            claimViewModel.getConnectionExclusions(request).observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<*>
                            setDataInViews((response.data as ClaimConnectionExclusionResponse))
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
                                    message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun setDataInViews(claimConnectionExclusionResponse: ClaimConnectionExclusionResponse?) {

        val excludedCategories = claimConnectionExclusionResponse?.reviewExclusion?.excludedCategories.getSafe()
        val excludedItems = claimConnectionExclusionResponse?.reviewExclusion?.excludedItems.getSafe()
        val specialities = claimConnectionExclusionResponse?.reviewExclusion?.specialities.getSafe()

        val excludedCategoriesBuilder = StringBuilder()
        val excludedItemsBuilder = StringBuilder()
        val specialitiesBuilder = StringBuilder()

        if (claimConnectionExclusionResponse?.reviewExclusion != null) {
            if (excludedCategories.isNotEmpty()) {
                excludedCategories.forEach { categories ->
                    excludedCategoriesBuilder.append("- $categories\n")
                }
                mBinding.apply {
                    tvContent.gone()
                    tvCategories.text = excludedCategoriesBuilder.toString().ifEmpty { mBinding.lang?.claimScreen?.noCategoriesFound.getSafe() }
                }
            }
            if (excludedItems.isNotEmpty()) {
                excludedItems.forEach { items ->
                    excludedItemsBuilder.append("- $items\n")
                }
                mBinding.apply {
                    tvContent.gone()
                    tvExcludedItems.text = excludedItemsBuilder.toString().ifEmpty { mBinding.lang?.claimScreen?.noItemsExcluded.getSafe() }
                }
            }
            if (specialities.isNotEmpty()) {
                specialities.forEach { categories ->
                    specialitiesBuilder.append("- $categories\n")
                }
                mBinding.apply {
                    tvContent.gone()
                    tvCategoryHeading.text = mBinding.lang?.globalString?.specialities.getSafe()
                    tvCategories.text = specialitiesBuilder.toString().ifEmpty { mBinding.lang?.claimScreen?.noCategoriesFound.getSafe() }
                }
            }
            if (excludedCategories.isEmpty() && excludedItems.isEmpty() && specialities.isEmpty()) {
                goneViews()
                mBinding.tvExclusion.apply {
                    val exclusion = claimConnectionExclusionResponse.reviewExclusion?.exclusion
                    visible()
                    if (exclusion.isNullOrEmpty().not().getSafe()) {
                        text = exclusion.getSafe()
                    } else {
                        goneViews()
                        mBinding.apply {
                            tvProductHeading.gone()
                            tvContent.apply {
                                visible()
                                text = mBinding.lang?.globalString?.noResultFound.getSafe()
                            }
                        }
                    }
                }
            }
            mBinding.apply {
                tvExcludedItemsHeading.setVisible(excludedItems.isNotEmpty())
                tvExcludedItems.setVisible(excludedItems.isNotEmpty())
                tvCategoryHeading.setVisible(excludedCategories.isNotEmpty() || specialities.isNotEmpty())
                tvCategories.setVisible(excludedCategories.isNotEmpty() || specialities.isNotEmpty())
            }
        } else {
            goneViews()
            mBinding.apply {
                tvProductHeading.gone()
                tvContent.apply {
                    visible()
                    text = mBinding.lang?.globalString?.noResultFound.getSafe()
                }
            }
        }
    }

    private fun goneViews() {
        mBinding.apply {
            tvContent.gone()
            tvCategoryHeading.gone()
            tvCategories.gone()
            tvExcludedItemsHeading.gone()
            tvExcludedItems.gone()
        }
    }

    private fun connectClaim(request: ClaimConnectRequest) {
        if (isOnline(requireActivity())) {
            claimViewModel.connectClaim(request)
                .observe(this) { it ->
                    when (it) {
                        is ResponseResult.Success -> {
                            val response = it.data as ResponseGeneral<ClaimConnectResponse>
                            response.data?.let {
                                val orderNum = "${langData?.myOrdersScreens?.orderNum?.getSafe()} ${it.uniqueIdentificationNumber}"
                                DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                                    title = orderNum,
                                    message = langData?.claimScreen?.msgClaimSubmitted.getSafe(),
                                    buttonOneText = langData?.globalString?.ok.getSafe(),
                                    buttonCallback = {
                                        claimViewModel.flushData()
                                        findNavController().popBackStack(R.id.selectClaimServiceFragment, true)
                                    }
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
                                    message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message =ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}