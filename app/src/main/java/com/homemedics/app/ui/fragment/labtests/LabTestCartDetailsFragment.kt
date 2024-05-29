package com.homemedics.app.ui.fragment.labtests

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.request.labtest.LabTestCartRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.pharmacy.OrderDetailsResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentLabtestCartDetailsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.CheckoutSplitAmountAdapter
import com.homemedics.app.ui.adapter.LabTestCartListAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.LabTestViewModel

class LabTestCartDetailsFragment : BaseFragment(), View.OnClickListener {

    private val labTestViewModel: LabTestViewModel by activityViewModels()

    private lateinit var mBinding: FragmentLabtestCartDetailsBinding

    private lateinit var adapter: LabTestCartListAdapter

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.labTest.getSafe()
        }
    }

    override fun init() {
        adapter = LabTestCartListAdapter()

        getCartDetailsApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_labtest_cart_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentLabtestCartDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    findNavController().popBackStack()
                }
                onAction2Click = {
                    //do nothing
                }
                onAction3Click = {
                    val ids = labTestViewModel.orderDetailsResponse.labCartItems?.map { it.id }
                    ids?.let {
                        DialogUtils(requireActivity())
                            .showDoubleButtonsAlertDialog(
                                title = lang?.globalString?.warning.getSafe(),
                                message = lang?.dialogsStrings?.clearCartMsg.getSafe(),
                                positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                                negativeButtonStringText = lang?.globalString?.no.getSafe(),
                                buttonCallback = {
                                    cartItemDeleteApi(null)
                                }
                            )
                    }
                }
            }

            adapter.onDeleteClick = { item, pos ->

                val request = LabTestCartRequest(
                    item_id = arrayListOf(item.id.getSafe())
                )

                cartItemDeleteApi(request)
            }

            bContinue.setOnClickListener(this@LabTestCartDetailsFragment)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bContinue -> {
                findNavController().safeNavigate(LabTestCartDetailsFragmentDirections.actionLabTestCartDetailsFragmentToLabTestBookingWithOrderDetailsFragment())
            }
        }
    }

    private fun setDataInViews(orderDetailsResponse: OrderDetailsResponse){
        val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
        mBinding.apply {

            iSelectedLab.apply {
                if(orderDetailsResponse.labCartItems.isNullOrEmpty().not()){
                    val item = orderDetailsResponse.labCartItems?.first()

                    tvTitle.text = item?.branch?.name
                    tvDesc.text = item?.branch?.streetAddress
                    ivIcon.loadImage(item?.lab?.iconUrl, R.drawable.ic_launcher_foreground)
                    ivThumbnail.gone()
                }
            }

            val paymentBreakdown = orderDetailsResponse.paymentBreakdown

            val orderDetails = arrayListOf(
                SplitAmount(specialityName = lang?.labPharmacyScreen?.labDiscount.getSafe(), fee = paymentBreakdown?.labDiscount.getSafe().toString()),
                SplitAmount(specialityName = lang?.globalString?.total.getSafe(), fee = paymentBreakdown?.totalFee.getSafe()),
                SplitAmount(specialityName = lang?.labPharmacyScreen?.sampleCollectionCharges.getSafe(), fee = paymentBreakdown?.sampleCharges.getSafe().toString()),
            )


            if(orderDetailsResponse.labCartItems.isNullOrEmpty()) {
                if(findNavController().popBackStack(R.id.labTestListFragment, true).not())
                    findNavController().popBackStack()
            }

            actionbar.dotText = labTestViewModel.orderDetailsResponse.labCartItems?.size.getSafe().toString()

            val currency = metaData?.currencies?.find { it.genericItemId == paymentBreakdown?.currencyId }?.genericItemName.getSafe()
            adapter.currency = currency
            rvMedicineProducts.adapter = adapter
            adapter.listItems = orderDetailsResponse.labCartItems.getSafe()
            val spacing = resources.getDimensionPixelSize(R.dimen.dp16)
            rvMedicineProducts.addItemDecoration(RecyclerViewItemDecorator(spacing, RecyclerViewItemDecorator.VERTICAL))

            // Split amount recyclerview
            val splitAmountAdapter = CheckoutSplitAmountAdapter()
            splitAmountAdapter.currency = currency
            rvSplitAmount.adapter = splitAmountAdapter
            splitAmountAdapter.listItems = orderDetails
            // implement item decoration for top margin

            tvPayAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${orderDetailsResponse.paymentBreakdown?.payableAmount} ${splitAmountAdapter.currency}" else "${splitAmountAdapter.currency} ${orderDetailsResponse.paymentBreakdown?.payableAmount}"
        }
    }

    private fun getCartDetailsApi(){
        val request = LabTestCartRequest(
            bookingId = labTestViewModel.bookingIdResponse.bookingId
        )

        if (isOnline(requireActivity())) {
            labTestViewModel.getCartDetails(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<OrderDetailsResponse>

                        response.data?.let { details ->
                            labTestViewModel.orderDetailsResponse = details
                            setDataInViews(details)
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message =it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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

    private fun cartItemDeleteApi(request: LabTestCartRequest? = null){

        if (isOnline(requireActivity())) {
            labTestViewModel.labTestDeleteToCart(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        getCartDetailsApi()
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message =it.error.message.getSafe(),
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.ApiError -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = mBinding.lang?.globalString?.information.getSafe(),
                                message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
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