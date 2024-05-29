package com.homemedics.app.ui.fragment.myorder

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.orders.ScheduledVisitsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.orders.ScheduledDutiesListResponse
import com.fatron.network_module.models.response.orders.ScheduledDutyResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentVisitSchedulesBinding
import com.homemedics.app.ui.adapter.VisitSchedulesAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.MyOrderViewModel
import timber.log.Timber

class VisitSchedulesFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentVisitSchedulesBinding
    private val ordersViewModel: MyOrderViewModel by activityViewModels()
    private lateinit var listAdapter: VisitSchedulesAdapter
    private lateinit var request: ScheduledVisitsRequest

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.myOrdersScreens?.visitSchedules.getSafe()
        }
    }

    override fun init() {
        request = ScheduledVisitsRequest(bookingId = ordersViewModel.bookingId.toInt())

        populateList()
        getSchedules(request)
    }

    override fun getFragmentLayout() = R.layout.fragment_visit_schedules

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentVisitSchedulesBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().navigateUp()
            }

            listAdapter.itemClickListener = {
                item, _ ->
                ordersViewModel.dutyId = item.id
                ordersViewModel.visitDate = item.date
                ordersViewModel.scheduledDuty = item
                findNavController().safeNavigate(VisitSchedulesFragmentDirections.actionVisitSchedulesFragmentToScheduledOrdersDetailFragment())
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private var loading = false
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var isLastPage = false
    var currentPage: Int? = 1
    private fun populateList() {
        mBinding.apply {
            listAdapter = VisitSchedulesAdapter()
            rvList.adapter = listAdapter
            val layoutManager = rvList.layoutManager as LinearLayoutManager

            rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    if (dy > 0) { //check for scroll down
                        visibleItemCount = layoutManager.childCount
                        totalItemCount = layoutManager.itemCount
                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                        if (loading.not()) {
                            if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
//                                loading = false
                                Timber.e("Last Item Wow !")

                                request.page = currentPage?.plus(1)

                                if(isLastPage.not()){
                                    getSchedules(request)
                                    loading = true
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun getSchedules(request: ScheduledVisitsRequest){
        if (isOnline(requireActivity())) {
            ordersViewModel.getHHCDuties(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<ScheduledDutiesListResponse>
                        response.data?.let {
                            listAdapter

                        }

                        response.data?.let {
                            isLastPage = request.page == it.lastPage
                            currentPage = it.currentPage

                            val tempList = listAdapter.listItems
                            tempList.addAll(it.data ?: arrayListOf())
                            listAdapter.listItems = tempList.distinctBy {
                                it.id
                            } as ArrayList<ScheduledDutyResponse>
                            listAdapter.notifyDataSetChanged()

                            mBinding.rvList.setVisible(listAdapter.listItems.isNotEmpty())
                            mBinding.tvNoData.setVisible(listAdapter.listItems.isEmpty())

                            loading = false
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
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