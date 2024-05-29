package com.homemedics.app.ui.fragment.claim

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.request.claim.ClaimConnectionExclusionRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.claim.ClaimConnectionsResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentClaimCheckoutBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.CheckoutSplitAmountAdapter
import com.homemedics.app.ui.adapter.LinkedConnectionsAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ClaimViewModel

class ClaimCheckoutFragment : BaseFragment(), View.OnClickListener {

    private val claimViewModel: ClaimViewModel by activityViewModels()
    private lateinit var mBinding: FragmentClaimCheckoutBinding
    private var currency = ""
    private var fees = ""

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.checkoutScreen?.checkout.getSafe()
        }
    }

    override fun init() {
        setObserver()
        setDataInViews()
        getClaimConnections()
    }

    override fun getFragmentLayout() = R.layout.fragment_claim_checkout

    override fun getViewModel() {

    }

    private fun setObserver() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentClaimCheckoutBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                requireActivity().onBackPressed()
            }
            bSubmit.setOnClickListener {
                findNavController().safeNavigate(ClaimCheckoutFragmentDirections.actionClaimCheckoutFragmentToClaimReviewPoliciesFragment())
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun setDataInViews(data: ClaimConnectionsResponse? = null) {
        val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        mBinding.apply {
            fees = claimViewModel.claimRequest.amount.getSafe()

            val service = "${claimViewModel.claimServicesResponse?.claimServices?.find {
                        it.genericItemId == claimViewModel.claimRequest.claimCategoryId
                    }?.genericItemName}"

            currency = DataCenter.getMeta()?.currencies?.find { it.genericItemId == claimViewModel.claimResponse?.claim?.bookingDetails?.currencyId }?.genericItemName.getSafe()

            val subtotal = fees

            val serviceCharges = "${service.getSafe()} ${lang?.claimScreen?.claim.getSafe().lowercase()}"
            val orderDetails = arrayListOf(
                SplitAmount(specialityName = serviceCharges, fee = subtotal),
            )
            val splitAmountAdapter = CheckoutSplitAmountAdapter()
            splitAmountAdapter.listItems = orderDetails
            rvSplitAmount.adapter = splitAmountAdapter

            tvPayableAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "$fees $currency" else "$currency $fees"

            data?.let {
                val connections = data.claimConnections
                val connectionsAdapter = LinkedConnectionsAdapter()
                connectionsAdapter.itemClickListener = { item, pos ->
                    if(item.onHold.getBoolean().not()) {
                        claimViewModel.selectedConnection = connections?.get(pos)

                        connectionsAdapter.listItems.map {
                            it.isChecked = false
                            it
                        }
                        connectionsAdapter.listItems[pos].isChecked = true
                        connectionsAdapter.notifyDataSetChanged()

                        bSubmit.isEnabled = splitAmountAdapter.listItems[0].fee?.toDouble().getSafe() <= connections?.get(pos)?.claimPackage?.credit?.amount?.toDouble().getSafe()
                    }
                }
                connectionsAdapter.listItems = connections.getSafe()
                rvConnections.adapter = connectionsAdapter
                tvNoData.setVisible(connectionsAdapter.listItems.isEmpty())
            }
        }
    }

    private fun getClaimConnections() {
        val request = ClaimConnectionExclusionRequest(
            partnerServiceId = claimViewModel.partnerServiceId,
            claimCategoryId = claimViewModel.claimRequest.claimCategoryId,
            bookingId = claimViewModel.bookingId
        )
        if (isOnline(requireActivity())) {

            claimViewModel.getClaimConnections(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        setDataInViews((response.data as ClaimConnectionsResponse))
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = it.error.message.getSafe(),
                                buttonCallback = {
                                    getClaimConnections()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                buttonCallback = {
                                    getClaimConnections()
                                },
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