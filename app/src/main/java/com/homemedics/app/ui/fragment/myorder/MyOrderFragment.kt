package com.homemedics.app.ui.fragment.myorder

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.fatron.network_module.models.request.orders.OrdersRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.ordersdetails.OrdersListResponse
import com.fatron.network_module.repository.ResponseResult
import com.google.android.material.tabs.TabLayoutMediator
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentMyorderBinding
import com.homemedics.app.ui.activity.HomeActivity
import com.homemedics.app.ui.adapter.TabsPagerAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CheckoutViewModel
import com.homemedics.app.viewmodel.ClaimViewModel
import com.homemedics.app.viewmodel.MyOrderViewModel

class MyOrderFragment : BaseFragment() {

    private lateinit var mBinding: FragmentMyorderBinding
    private val ordersViewModel: MyOrderViewModel by activityViewModels()
    private val checkoutViewModel: CheckoutViewModel by activityViewModels()
    private val claimViewModel: ClaimViewModel by activityViewModels()

    override fun onDetach() {
        super.onDetach()
        ordersViewModel.flushData()
        resetOrderList()

    }



    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    resetOrderList()

                    if (checkoutViewModel.fromCheckout) { //when coming from checkout success > view details > order details
                        checkoutViewModel.fromCheckout = false

                        navigateToHome()

                    }
                }
            }
        )
    }

    private fun resetOrderList(){
        ordersViewModel.listItems = arrayListOf()
        ordersViewModel.ordersListResponse.postValue(null)
    }

    private fun navigateToHome(){
        val intent = Intent(requireActivity(), HomeActivity::class.java) //probably checkout activity
        requireActivity().finishAffinity()
        startActivity(intent)
    }

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.myOrdersScreens?.myOrders.getSafe()
        }
    }

    override fun init() {
        claimViewModel.isAttachment = false
        ordersViewModel.ordersRequest.page = 1
        initTabsPager()
        if(checkoutViewModel.fromCheckout)
            handleBackPress()

        if (arguments?.getString(Constants.BOOKING_ID)!=null) {
          val bookingId = arguments?.getString(Constants.BOOKING_ID)?:"0"
            arguments?.clear()
            findNavController().safeNavigate(R.id.action_notification_myOrderFragment_to_ordersDetailFragment ,
                bundleOf( Constants.BOOKINGID to bookingId.toInt())



            )
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_myorder

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentMyorderBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                resetOrderList()
                if(findNavController().popBackStack().not()){
                    navigateToHome()
                }
            }
        }
    }

    private fun initTabsPager() {
        val current = mBinding.lang?.myOrdersScreens?.current.getSafe()
        val history = mBinding.lang?.myOrdersScreens?.history.getSafe()
        mBinding.apply {
            TabsPagerAdapter.fragments = ArrayList<Fragment>().apply {
                add(OrdersFragment())
                add(OrdersFragment())
            }
            viewPager.adapter = TabsPagerAdapter(childFragmentManager, lifecycle)
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab, position ->
                tab.text = when (position) {
                    0 -> current
                    1 -> history
                    else -> {""}
                }
            }.attach()

            viewPager.isUserInputEnabled = false

            viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    var type = ""
                    var orderType = ""
                    when (position) {
                        0 -> {
                            orderType = getString(R.string.current_type)
                            type = lang?.globalString?.current.getSafe()
                            ordersViewModel.selectedTab.postValue(Enums.OrdersType.CURRENT)
                        }
                        1 -> {
                            orderType = getString(R.string.history_type)
                            type = lang?.globalString?.history.getSafe()
                            ordersViewModel.selectedTab.postValue(Enums.OrdersType.HISTORY)
                        }
                        else -> {
                            orderType = getString(R.string.current_type)
                            type = lang?.globalString?.current.getSafe()
                            ordersViewModel.selectedTab.postValue(Enums.OrdersType.CURRENT)
                        }
                    }
                    ordersViewModel.page = 1
                    ordersViewModel.ordersListResponse.postValue(null)
                    ordersViewModel.listItems = arrayListOf()
                    ordersViewModel.ordersRequest.ordersType = orderType.lowercase()
                    ordersViewModel.ordersRequest.page = ordersViewModel.page
                    if (isOnline(requireContext())) {
                        getOrders(ordersViewModel.ordersRequest)
                    } else {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                title = lang?.errorMessages?.internetError.getSafe(),
                                message = lang?.errorMessages?.internetErrorMsg.getSafe(),
                                buttonCallback = {},
                            )
                    }
                }
            })
        }
    }

    fun getOrders(request: OrdersRequest) {
        ordersViewModel.getOrders(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<OrdersListResponse>
                    response.data?.let { appointmentList ->
                        ordersViewModel.ordersListResponse.postValue(appointmentList)
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
                            message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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