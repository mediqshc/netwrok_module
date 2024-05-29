package com.homemedics.app.ui.fragment.myorder

import android.view.View
import android.widget.AbsListView
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentOrdersBinding
import com.homemedics.app.ui.adapter.OrdersAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.Enums
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.safeNavigate
import com.homemedics.app.utils.setVisible
import com.homemedics.app.viewmodel.CheckoutViewModel
import com.homemedics.app.viewmodel.ClaimViewModel
import com.homemedics.app.viewmodel.MyOrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class OrdersFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentOrdersBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private val ordersViewModel: MyOrderViewModel by activityViewModels()
    private val checkoutViewModel: CheckoutViewModel by activityViewModels()
    private val claimViewModel: ClaimViewModel by activityViewModels()
    private lateinit var appointmentType: Enums.OrdersType
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrollOutItems: Int = 0
    private var mLoading = false
    private var pagEnd: Boolean = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var lastPage = 0
    private var isResume=false

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
        }
    }

    override fun init() {
        claimViewModel.isAttachment = false
        ordersViewModel.listItems = arrayListOf()
        ordersViewModel.ordersListResponse.postValue(null)
        populateUpcomingList()
        observe()
    }

    override fun getFragmentLayout() = R.layout.fragment_orders

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentOrdersBinding
    }

    override fun setListeners() {
        ordersAdapter.itemClickListener = { item, _ ->
            ordersViewModel.selectedOrder = item

            ordersViewModel.apply {
                bookingId = item.id.toString()
                partnerUserId = item.partnerUser?.id.getSafe()
                serviceId = item.partnerServiceId.getSafe()
            }
            ordersViewModel.page = 1
            ordersAdapter.listItems = arrayListOf()
            ordersViewModel.listItems= arrayListOf()
            when(item.partnerServiceId){
                CustomServiceTypeView.ServiceType.Claim.id -> {
                    findNavController().safeNavigate(
                        MyOrderFragmentDirections.actionMyOrderFragmentToClaimDetailsNavigation()
                    )
                }
                CustomServiceTypeView.ServiceType.WalkInPharmacy.id -> {
                    findNavController().safeNavigate(
                       R.id.action_myOrderFragment_to_walkin_order_details_navigation,
                        bundleOf(
                            "attachment_visibility" to true,

                            )
                    )

                }
                CustomServiceTypeView.ServiceType.WalkInLaboratory.id -> {
                    findNavController().safeNavigate(
                       // MyOrderFragmentDirections.actionMyOrderFragmentToWalkinLabOrderDetailsNavigation()
                       R.id.action_myOrderFragment_to_walkin_lab_order_details_navigation,
                        bundleOf(
                            "attachment_visibility_lab" to true

                        )


                    )
                }
                CustomServiceTypeView.ServiceType.WalkInHospital.id -> {
                    findNavController().safeNavigate(
                        R.id.action_myOrderFragment_to_walkin_hospital_order_details_navigation,
                        bundleOf(
                            "attachment_visibility_hospital" to true

                        )

                    )
                }
                else -> {
                    findNavController().safeNavigate(
                        MyOrderFragmentDirections.actionMyOrderFragmentToOrdersDetailFragment()
                    )
                }
            }
        }

        mBinding.rvUpcoming.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    mLoading = true;
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItems = linearLayoutManager.childCount
                totalItems = linearLayoutManager.itemCount
                scrollOutItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (mLoading && (currentItems + scrollOutItems == totalItems)) {
                    mLoading = false
                    Timber.e("page ${ordersViewModel.page}")
                    ordersViewModel.page = ordersViewModel.page.plus(1)
                    if ((!pagEnd && ordersViewModel.page <= lastPage)  && !isResume) {
//                        taskAppointmentsViewModel.fromDetail = false

                        (parentFragment as MyOrderFragment).getOrders(
                            ordersViewModel.ordersRequest.apply {
                                page = ordersViewModel.page
                            }
                        )
                    }

                }
            }
        })
    }

    override fun onClick(v: View?) {

    }


    private fun observe() {
        ordersViewModel.ordersListResponse.observe(this) {
            it?.let { it ->
                pagEnd = false
//                ordersViewModel.page = it.currentPage
                if (it.orders.isNullOrEmpty().not()) {
                    val tempList = ordersViewModel.listItems
                    tempList.addAll(it.orders as ArrayList<OrderResponse>)
                    ordersViewModel.listItems = tempList.distinctBy {
                        Pair(
                            it.id,
                            it.bookingStatusId
                        )
                    } as ArrayList<OrderResponse>
                    ordersAdapter.notifyDataSetChanged()
                    lastPage = ordersViewModel.ordersListResponse.value?.lastPage.getSafe()
                    ordersAdapter.setListItems(ordersViewModel.listItems)

                    /**
                     * pagination not working large screen
                     * as onScrolled callback not called on this case
                     * that's why using this patch
                     */

                    lifecycleScope.launch {
                        delay(300)

                        val lastVisibleItemPos = (mBinding.rvUpcoming.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()

                        if(lastVisibleItemPos > 5 && ordersViewModel.page == 1){

                            ordersViewModel.page = ordersViewModel.page.plus(1)
                            if (!pagEnd && ordersViewModel.page <= lastPage) {

                                (parentFragment as MyOrderFragment).getOrders(
                                    ordersViewModel.ordersRequest.apply {
                                        page = ordersViewModel.page
                                    }
                                )
                            }
                        }
                    }
                }
                else {
                    ordersViewModel.listItems= arrayListOf()
                    ordersViewModel.listItems.clear()
                    ordersAdapter.setListItems(ordersViewModel.listItems)
                    pagEnd = true
                }

//                pagEnd = it.orders.isNullOrEmpty()

                //if coming from checkout details
                val orderId = checkoutViewModel.bookedOrderId
                if(orderId != null){
                    ordersAdapter.listItems.find { it.id == orderId }?.let { item ->
                        ordersViewModel.apply {
                            bookingId = item.id.toString()
                            partnerUserId = item.partnerUser?.id.getSafe()
                            serviceId = item.partnerServiceId.getSafe()
                        }

                        checkoutViewModel.bookedOrderId = null

                        findNavController().safeNavigate(
                            MyOrderFragmentDirections.actionMyOrderFragmentToOrdersDetailFragment()
                        )
                    }
                }
            } ?: kotlin.run {
                ordersAdapter.listItems = arrayListOf()
                ordersViewModel.listItems = arrayListOf()
            }
            mBinding.apply {
                rvUpcoming.setVisible((ordersAdapter.listItems.isNullOrEmpty().not()))
                tvNoData.setVisible((ordersAdapter.listItems.isNullOrEmpty()))
            }
          if (ordersViewModel.page!= it?.lastPage.getSafe())
              isResume=false
        }
    }

    override fun onResume() {
        super.onResume()
        isResume=true

    }

    override fun onDestroy() {
        super.onDestroy()
        ordersViewModel.selectedOrder=null
    }
    private fun populateUpcomingList() {
        ordersViewModel.listItems = arrayListOf()
        ordersAdapter = OrdersAdapter()
        mBinding.apply {
            rvUpcoming.adapter = ordersAdapter
            rvUpcoming.layoutManager=LinearLayoutManager(requireContext())
            linearLayoutManager = mBinding.rvUpcoming.layoutManager as LinearLayoutManager
        }
    }



}