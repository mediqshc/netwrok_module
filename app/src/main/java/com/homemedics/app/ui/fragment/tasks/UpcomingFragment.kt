package com.homemedics.app.ui.fragment.tasks

import android.view.View
import android.widget.AbsListView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.AppointmentListResponse
import com.fatron.network_module.models.response.appointments.AppointmentResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentUpcomingBinding
import com.homemedics.app.ui.adapter.AppointmentListAdapters
import com.homemedics.app.ui.adapter.DataItemListing
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.TaskAppointmentsViewModel
import timber.log.Timber

class UpcomingFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentUpcomingBinding

    private lateinit var appointmentsAdapter: AppointmentListAdapters

    private val taskAppointmentsViewModel: TaskAppointmentsViewModel by activityViewModels()

    private lateinit var appointmentType: Enums.AppointmentType
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrollOutItems: Int = 0
    private var mLoading = false
    private var pagEnd: Boolean = false
    private var lastPage: Int = 0
    override fun setLanguageData() {
        mBinding.tvNoData.text=ApplicationClass.mGlobalData?.taskScreens?.noAppointmentsMsg
    }

    override fun init() {
        appointmentType = arguments?.getSerializable("type") as Enums.AppointmentType

        observe()
        populateUpcomingList()
    }

    override fun getFragmentLayout() = R.layout.fragment_upcoming

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentUpcomingBinding
    }

    override fun setListeners() {
        appointmentsAdapter.itemClickListener = { item, _ ->
            taskAppointmentsViewModel.bookingId = item.id.toString()
            taskAppointmentsViewModel.partnerServiceId = item.partnerServiceId.getSafe()

            taskAppointmentsViewModel.dutyId = item.dutyId.getSafe()
//            appointmentsAdapter.listItems.clear()
//            taskAppointmentsViewModel.listItems= arrayListOf()
            taskAppointmentsViewModel.page = 1
            taskAppointmentsViewModel.listItems.clear()

            findNavController().safeNavigate(R.id.action_appointmentsFragment_to_appointmentsDetailFragment

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
                    Timber.e("page ${taskAppointmentsViewModel.page}")
                    taskAppointmentsViewModel.page = taskAppointmentsViewModel.page.plus(1)
                    if (!pagEnd && taskAppointmentsViewModel.page <= lastPage) {
//                        taskAppointmentsViewModel.fromDetail = false
                        getAppointments(taskAppointmentsViewModel.page)

                    }

                }
            }
        })

        mBinding.apply {
            swipeRefresh.setOnRefreshListener {
                swipeRefresh.isRefreshing = false
                taskAppointmentsViewModel.page = 1
                getAppointments(taskAppointmentsViewModel.page)
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun observe() {
        taskAppointmentsViewModel.appointmentListResponse.observe(this, Observer {
            it ?: return@Observer
            it.let { lists ->
                pagEnd = false

                when (taskAppointmentsViewModel.appointmentListRequest.appointmentType) {
                    Enums.AppointmentType.UPCOMING.value.lowercase() -> {
                       taskAppointmentsViewModel.page= taskAppointmentsViewModel.appointmentListResponse.value?.upcoming?.currentPage.getSafe()

                        Timber.e("count upcoming ${taskAppointmentsViewModel.listItems?.size}")
                        if (lists.upcoming?.data.isNullOrEmpty().not()) {
                            taskAppointmentsViewModel.listItems?.addAll(it.upcoming?.data as ArrayList<AppointmentResponse>)
                        } else {
                            taskAppointmentsViewModel.listItems.clear()
                            pagEnd = true
                        }
                        lastPage = lists.upcoming?.lastPage.getSafe()
                        taskAppointmentsViewModel.listItems = taskAppointmentsViewModel.listItems.distinctBy { Pair(it.id, it.dutyId) } as ArrayList<AppointmentResponse>
                        appointmentsAdapter.submitList(taskAppointmentsViewModel.listItems.map { res ->
                            res.let { it1 ->
                                DataItemListing.DefaultItemListing(
                                    it1
                                )
                            }
                        })


                        Timber.e("appointment ${taskAppointmentsViewModel.fromSelect}  ${taskAppointmentsViewModel.listItems?.size}" + taskAppointmentsViewModel.appointmentListRequest.appointmentType)
                    }
                    Enums.AppointmentType.UNREAD.value.lowercase() -> {
                        taskAppointmentsViewModel.page= taskAppointmentsViewModel.appointmentListResponse.value?.unread?.currentPage.getSafe()

                        Timber.e("count unread ${taskAppointmentsViewModel.listItems?.size}")

                        if (lists.unread?.data.isNullOrEmpty().not()) {
                            taskAppointmentsViewModel.listItems.addAll(lists.unread?.data as ArrayList<AppointmentResponse>)
                        } else {
                            taskAppointmentsViewModel.listItems.clear()
                            pagEnd = true
                        }
                        lastPage = lists.unread?.lastPage.getSafe()
                        taskAppointmentsViewModel.listItems = taskAppointmentsViewModel.listItems.distinctBy { Pair(it.id, it.dutyId) } as ArrayList<AppointmentResponse>
                        appointmentsAdapter.submitList(taskAppointmentsViewModel.listItems.map { res ->
                            res.let { it1 ->
                                DataItemListing.DefaultItemListing(
                                    it1
                                )
                            }
                        })
                    }
                    Enums.AppointmentType.HISTORY.value.lowercase() -> {
                        taskAppointmentsViewModel.page= taskAppointmentsViewModel.appointmentListResponse.value?.history?.currentPage.getSafe()

                        Timber.e("count history ${taskAppointmentsViewModel.listItems.size}")

                        if (lists.history?.data.isNullOrEmpty().not()) { //dd
                            taskAppointmentsViewModel.listItems.addAll(lists.history?.data as ArrayList<AppointmentResponse>)
                        } else {
                            taskAppointmentsViewModel.listItems.clear()
                            pagEnd = true
                        }
                        lastPage = lists.history?.lastPage.getSafe()
                        taskAppointmentsViewModel.listItems = taskAppointmentsViewModel.listItems.distinctBy { Pair(it.id, it.dutyId) } as ArrayList<AppointmentResponse>
                        appointmentsAdapter.submitList(taskAppointmentsViewModel.listItems.map { res ->
                            res.let { it1 ->
                                DataItemListing.DefaultItemListing(
                                    it1
                                )
                            }
                        })
                    }
                }

                val items=taskAppointmentsViewModel.listItems
                if(items.size>0){
                    mBinding.rvUpcoming.setVisible(true)
                    mBinding.tvNoData.setVisible(false)
                }else
                {
                    mBinding.rvUpcoming.setVisible(false)
                    mBinding.tvNoData.setVisible(true)
                }
            }
        })
    }

    private lateinit var linearLayoutManager: LinearLayoutManager

    private fun populateUpcomingList() {
        linearLayoutManager = LinearLayoutManager(activity)
        appointmentsAdapter = AppointmentListAdapters(appointmentType)
        mBinding.apply {
            rvUpcoming.adapter = appointmentsAdapter
            rvUpcoming.layoutManager = linearLayoutManager

        }
    }

    private fun getAppointments(page: Int) {
        taskAppointmentsViewModel.getAppointments(page).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<AppointmentListResponse>
                    response.data?.let { appointmentList ->
                        if(page == 1){ //initial or pullToRefresh
                            taskAppointmentsViewModel.appointmentListResponse.postValue(AppointmentListResponse())
                        }

                        taskAppointmentsViewModel.appointmentListResponse.postValue(appointmentList)
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
                else -> {
                    hideLoader()
                }
            }
        }
    }
}
