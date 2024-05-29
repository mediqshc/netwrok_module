package com.homemedics.app.ui.fragment.claim

import android.view.View
import android.widget.AbsListView
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
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.safeNavigate
import com.homemedics.app.utils.setVisible
import com.homemedics.app.viewmodel.ClaimViewModel
import com.homemedics.app.viewmodel.MyOrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class ClaimTabFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentOrdersBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private val claimViewModel: ClaimViewModel by activityViewModels()
    private val ordersViewModel: MyOrderViewModel by activityViewModels()
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrollOutItems: Int = 0
    private var mLoading = false
    private var pagEnd: Boolean = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var lastPage = 0

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
        }
    }

    override fun init() {
        claimViewModel.listItems = arrayListOf()
        claimViewModel.claimListResponse.postValue(null)
        populateUpcomingList()
        observe()
        mBinding.rvUpcoming.scrollToPosition(0)
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
            claimViewModel.page = 1
            findNavController().safeNavigate(
                MyClaimsFragmentDirections.actionMyClaimsFragmentToClaimDetailsNavigation()
            )
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
                    Timber.e("page ${claimViewModel.page}")
                    claimViewModel.page = claimViewModel.page.plus(1)
                    if (!pagEnd && claimViewModel.page <= lastPage) {
//                        taskAppointmentsViewModel.fromDetail = false

                        (parentFragment as MyClaimsFragment).getOrders(
                            claimViewModel.claimListRequest.apply {
                                page = claimViewModel.page
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
        claimViewModel.claimListResponse.observe(this) {
            it?.let { it ->
                pagEnd = false
//                ordersViewModel.page = it.currentPage

                if (it.orders.isNullOrEmpty().not()) {
                    val tempList = claimViewModel.listItems
                    tempList.addAll(it.orders as ArrayList<OrderResponse>)
                    claimViewModel.listItems = tempList.distinctBy {
                        Pair(
                            it.id,
                            it.bookingStatusId
                        )
                    } as ArrayList<OrderResponse>
                    ordersAdapter.notifyDataSetChanged()
                    lastPage = claimViewModel.claimListResponse.value?.lastPage.getSafe()
                    ordersAdapter.setListItems(claimViewModel.listItems)

                    /**
                     * pagination not working large screen
                     * as onScrolled callback not called on this case
                     * that's why using this patch
                     */

                    lifecycleScope.launch {
                        delay(300)

                        val lastVisibleItemPos = (mBinding.rvUpcoming.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()

                        if(lastVisibleItemPos > 5 && claimViewModel.page == 1){

                            claimViewModel.page = claimViewModel.page.plus(1)
                            if (!pagEnd && claimViewModel.page <= lastPage) {

                                (parentFragment as MyClaimsFragment).getOrders(
                                    claimViewModel.claimListRequest.apply {
                                        page = claimViewModel.page
                                    }
                                )
                            }
                        }
                    }
                }
                else {
                    claimViewModel.listItems.clear()
                    ordersAdapter.setListItems(claimViewModel.listItems)
                    pagEnd = true
                }

//                pagEnd = it.orders.isNullOrEmpty()
            } ?: kotlin.run {
                ordersAdapter.listItems = arrayListOf()
                claimViewModel.listItems = arrayListOf()
            }
            mBinding.apply {
                rvUpcoming.setVisible((ordersAdapter.listItems.isNullOrEmpty().not()))
                tvNoData.setVisible((ordersAdapter.listItems.isNullOrEmpty()))
            }
        }
    }

    private fun populateUpcomingList() {
        claimViewModel.listItems = arrayListOf()
        ordersAdapter = OrdersAdapter()
        mBinding.apply {
            rvUpcoming.adapter = ordersAdapter
            rvUpcoming.layoutManager=LinearLayoutManager(requireContext())
            linearLayoutManager = mBinding.rvUpcoming.layoutManager as LinearLayoutManager
        }
    }
}