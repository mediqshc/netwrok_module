package com.homemedics.app.ui.fragment.labtests

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.labtest.LabTestCartRequest
import com.fatron.network_module.models.request.labtest.LabTestFilterRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.labtest.LabResponse
import com.fatron.network_module.models.response.labtest.LabTestCategoriesResponse
import com.fatron.network_module.models.response.pharmacy.OrderDetailsResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectMainLabBinding
import com.homemedics.app.ui.adapter.SelectMainLabAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.LabTestViewModel
import kotlin.properties.Delegates

class SelectMainLabFragment : BaseFragment(), View.OnClickListener {

    private val labTestViewModel: LabTestViewModel by activityViewModels()
    private lateinit var mBinding: FragmentSelectMainLabBinding
    private val adapter = SelectMainLabAdapter()
    private var preSelected = -1

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            caPreferredLab.apply {
                title = lang?.labPharmacyScreen?.selectPreferredLab.getSafe()
                custDesc = lang?.labPharmacyScreen?.preferLab.getSafe()
            }
        }
    }

    override fun init() {
        mBinding.rvList.adapter = adapter
        adapter.showLabPrice = labTestViewModel.fromBLT.not()

        setupViews()
        setDataInViews()

        val request =  labTestViewModel.labTestFilterRequest
        request.page = 1

        getListApi(request)

        if (labTestViewModel.fromBLT.not())
            getCartDetailsApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_select_main_lab

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectMainLabBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                if(labTestViewModel.fromBLT)
                    findNavController().popBackStack()
                else {
                    if(labTestViewModel.orderDetailsResponse.labCartItems.isNullOrEmpty()){
                        DialogUtils(requireActivity()).showSingleButtonAlertDialog(
                            title = mBinding.lang?.globalString?.information.getSafe(),
                            message = mBinding.lang?.labPharmacyScreen?.emptyCart.getSafe(),
                        )
                    }
                    else
                        findNavController().safeNavigate(SelectMainLabFragmentDirections.actionSelectMainLabFragmentToLabTestCartDetailsFragment())
                }
            }
            adapter.apply {
                var selectedPosition by Delegates.observable(-1) { property, oldPos, newPos ->
                    val temperedOldPos = if(oldPos == -1) preSelected else oldPos

                    if (temperedOldPos != newPos) {
                        if (newPos in listItems.indices) {
                            listItems[newPos].isSelected = true
                            if (temperedOldPos != -1) {
                                listItems[temperedOldPos].isSelected = false
                                notifyItemChanged(temperedOldPos)
                            }
                            notifyItemChanged(newPos)
                        }
                    }
                }

                itemClickListener = { _, pos->
                    selectedPosition = pos

                    bAddToCart.isEnabled = getSelectedItem() != null
                }
            }

            bAddToCart.setOnClickListener {
                val newSelected = adapter.getSelectedItem()

                if(labTestViewModel.fromBLT.not() && labTestViewModel.orderDetailsResponse.labCartItems.isNullOrEmpty().not()){
                    labTestViewModel.selectedMainLab = labTestViewModel.orderDetailsResponse.labCartItems?.get(0)?.lab
                }

                if(labTestViewModel.fromBLT.not() && labTestViewModel.selectedMainLab != null && (newSelected?.id != labTestViewModel.selectedMainLab?.id) && labTestViewModel.orderDetailsResponse.labCartItems.isNullOrEmpty().not()){
                    showCartWillRemoveDialog(labTestViewModel.selectedMainLab, newSelected)
                }
                else {
                    labTestViewModel.selectedMainLab = (newSelected)
                    findNavController().safeNavigate(SelectMainLabFragmentDirections.actionSelectMainLabFragmentToSelectLabFragment())
                }
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun setupViews(){
        mBinding.apply {
            if(labTestViewModel.fromBLT){ //when coming from booking screen
                actionbar.title = lang?.labPharmacyScreen?.selectPreferredLab.getSafe()
                llTestName.gone()
                divider1.gone()
                bAddToCart.text = lang?.globalString?.select.getSafe()
            }
            else {
                actionbar.title = lang?.labPharmacyScreen?.labTest.getSafe()
                actionbar.action2Res = R.drawable.ic_shopping_cart
            }
        }
    }

    private fun setDataInViews(){
        mBinding.apply {
            val cartList = labTestViewModel.orderDetailsResponse.labCartItems
            if(cartList.isNullOrEmpty().not() && adapter.listItems.isNotEmpty()){
                val selectedLabId = cartList?.get(0)?.lab?.id
                val pos = adapter.listItems.indexOfFirst { it.id == selectedLabId }

                if(pos != -1){
                    adapter.apply {
                        listItems.map { it.isSelected = false }
                        listItems[pos].isSelected = true
                        notifyDataSetChanged()

                        bAddToCart.isEnabled = getSelectedItem() != null
                    }
                }

                preSelected = pos
            }

            tvTestName.text = labTestViewModel.selectedLabTest?.name
            tvTestDesc.text = labTestViewModel.selectedLabTest?.description
        }
    }

    private fun showCartWillRemoveDialog(prevItem: LabResponse?, newItem: LabResponse?){
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            title = mBinding.lang?.dialogsStrings?.areYouSure.getSafe(),
            message = mBinding.lang?.labPharmacyScreen?.testFromOtherLab.getSafe(),
            positiveButtonStringText = mBinding.lang?.globalString?.btnContinue.getSafe(),
            negativeButtonStringText = mBinding.lang?.globalString?.goBack.getSafe(),
            buttonCallback = {
                labTestViewModel.selectedMainLab = (newItem)
                cartItemDeleteApi()
            },
            negativeButtonCallback = {
                adapter.listItems.map { it.isSelected = false }
                val prevIndex = adapter.listItems.indexOfFirst { it.id == prevItem?.id }
                if(prevIndex != -1){
                    adapter.listItems[prevIndex].isSelected = true
                    adapter.notifyDataSetChanged()
                }
            }
        )
    }

    private fun getListApi(request: LabTestFilterRequest){
        if (isOnline(requireActivity())) {
            labTestViewModel.getLabList(request).observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<LabTestCategoriesResponse>

                        val list = response.data?.data as ArrayList<LabResponse>?
//                        list?.get(0)?.isSelected = true
                        list?.let { listItems ->
                            adapter.listItems = listItems
                            adapter.originalList = listItems
                        }

                        setDataInViews()

                        mBinding.tvNoData.setVisible(adapter.listItems.isEmpty())
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
                        findNavController().safeNavigate(SelectMainLabFragmentDirections.actionSelectMainLabFragmentToSelectLabFragment())
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
                            mBinding.actionbar.dotText = labTestViewModel.orderDetailsResponse.labCartItems?.size.getSafe().toString()
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
                                message =getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                                cancellable = false,
                                buttonCallback = {
                                    findNavController().popBackStack()
                                },
                            )
                    }
                    is ResponseResult.Pending -> {
//                        showLoader()
                    }
                    is ResponseResult.Complete -> {
//                        hideLoader()
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