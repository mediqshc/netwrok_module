package com.homemedics.app.ui.fragment.notifications

import android.content.Intent
import android.view.View
import android.widget.AbsListView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.request.notification.NotificationReadRequest
import com.fatron.network_module.models.request.notification.NotificationRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.NotificationCategory
import com.fatron.network_module.models.response.notification.Notification
import com.fatron.network_module.models.response.notification.NotificationResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentNotificationBinding
import com.homemedics.app.ui.activity.ChatActivity
import com.homemedics.app.ui.adapter.NotificationCategoryAdapter
import com.homemedics.app.ui.adapter.NotificationsAdapter
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import com.homemedics.app.viewmodel.NotificationViewModel
import timber.log.Timber

class NotificationFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentNotificationBinding

    private lateinit var notificationAdapter: NotificationsAdapter
    private lateinit var notificationCategAdapter: NotificationCategoryAdapter
    private val notiViewModel: NotificationViewModel by activityViewModels()

    private val emrViewModel: EMRViewModel by activityViewModels()
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrollOutItems: Int = 0
    private var mLoading = false
    private var pagEnd: Boolean = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var lastPage = 0
    override fun setLanguageData() {
        val langData = ApplicationClass.mGlobalData
        mBinding.apply {
            actionbar.title = langData?.notificationScreens?.notification.getSafe()
            tvNoData.text = langData?.notificationScreens?.noNotiAvailable.getSafe()
        }
    }

    override fun init() {
        observe()
        mBinding.rvNotification.scrollToPosition(0)

        populateCategory()
        populateNotiList()
        pagination()
        getNoti(
            NotificationRequest(
                categoryId = notiViewModel.categoryId
            )
        )
    }

//    override fun onResume() {
//        super.onResume()
//        getNoti(
//            NotificationRequest(
//                categoryId = notiViewModel.categoryId
//            )
//        )
//    }

    override fun getFragmentLayout() = R.layout.fragment_notification

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentNotificationBinding
    }

    override fun setListeners() {
        mBinding.actionbar.onAction1Click = {
            findNavController().popBackStack()
        }
        notificationAdapter.itemClickListener = { item, _ ->
            //click on noti item
            if (item.isRead == "0")
                callReadNotif(NotificationReadRequest(notificationId = item.id.getSafe()))
            handleNotifRedirections(item)
        }
        notificationCategAdapter.onCategorySlotSelected = {
            if (it.isChecked.getSafe())
                notiViewModel.categoryId = it.id.getSafe()
            else
                notiViewModel.categoryId = 0
            notiViewModel.page = 1
            notiViewModel.listItems = arrayListOf()
            getNoti(
                NotificationRequest(
                    categoryId = notiViewModel.categoryId,
                    page = notiViewModel.page
                )
            )
        }

//        mBinding.rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                    mLoading = true;
//                }
//            }
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                currentItems = linearLayoutManager.childCount
//                totalItems = linearLayoutManager.itemCount
//                scrollOutItems = linearLayoutManager.findFirstVisibleItemPosition();
//
//                if (mLoading && (currentItems + scrollOutItems == totalItems)) {
//                    mLoading = false
//                    notiViewModel.page = notiViewModel.page.plus(1)
//                    if (!pagEnd && notiViewModel.page <= lastPage) {
//                        getNoti(
//                            NotificationRequest(
//                                categoryId = notiViewModel.categoryId,
//                                page = notiViewModel.page
//                            )
//                        )
//                    }
//
//                }
//            }
//        })
    }

    override fun onClick(v: View?) {

    }

    fun getNoti(request: NotificationRequest) {
        notiViewModel.getNoti(request).observe(this) {
            notiViewModel.notiListResponse.value = null
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<NotificationResponse>
                    response.data?.let { notificationList ->
                        isLastPage = notiViewModel.page == notificationList.lastPage
                        currentPage = notificationList.currentPage
                        notiViewModel.notiListResponse.postValue(notificationList)
                        loading = false
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

    //call notification read
    fun callReadNotif(request: NotificationReadRequest) {
        notiViewModel.callReadNotif(request).observe(this) {
            notiViewModel.notiListResponse.value = null
            when (it) {
                is ResponseResult.Success -> {

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
                }
                is ResponseResult.Complete -> {

                }
                else -> {

                }
            }
        }
    }

    private fun observe() {
        notiViewModel.notiListResponse.observe(this) {
            it?.let { it ->
                pagEnd = false
//                ordersViewModel.page = it.currentPage

                if (it.notifications?.isEmpty()?.not().getSafe()) {
                    val tempList = notiViewModel.listItems
                    tempList.addAll(it.notifications as ArrayList<Notification>)
                    notiViewModel.listItems = tempList.distinctBy {
                        it.id

                    } as ArrayList<Notification>
                    notificationAdapter.notifyDataSetChanged()

                    lastPage = notiViewModel.notiListResponse.value?.lastPage.getSafe()
                    notificationAdapter.setListItems(notiViewModel.listItems)
                } else {
                    notiViewModel.listItems.clear()
                    notificationAdapter.setListItems(notiViewModel.listItems)
                    pagEnd = true
                }


            }
            mBinding.apply {
                rvNotification.setVisible((notificationAdapter.listItems.isNullOrEmpty().not()))
                tvNoData.setVisible((notificationAdapter.listItems.isNullOrEmpty()))
            }
        }
    }

    private fun populateNotiList() {
        notificationAdapter = NotificationsAdapter()
        mBinding.apply {
            rvNotification.adapter = notificationAdapter
            linearLayoutManager = mBinding.rvNotification.layoutManager as LinearLayoutManager

        }
    }

    private fun populateCategory() {
        notificationCategAdapter = NotificationCategoryAdapter()
        notificationCategAdapter.listItems =
            metaData?.notificationCategories as ArrayList<NotificationCategory>
        mBinding.rvNotifCategory.adapter = notificationCategAdapter
    }

    private fun handleNotifRedirections(item: Notification) {
        notiViewModel.listItems = arrayListOf()
        notiViewModel.page = 1
        when (item.typeID) {
            //5, //6,//9,//17,//18,19,//20,//22
            Enums.CallPNType.TYPE_BOOKING_CREATED.key, Enums.CallPNType.TYPE_BOOKING_COMPLETED.key,
            Enums.CallPNType.TYPE_HOME_VISIT_UPDATE.key, Enums.CallPNType.TYPE_PAYMENT_RECEIVED.key,
            Enums.CallPNType.TYPE_BOOKING_REMINDER.key, Enums.CallPNType.TYPE_BOOKING_RESCHEDULED_REQUEST.key,
            Enums.CallPNType.TYPE_BOOKING_RESCHEDULED_ACCEPTED_BY_CUSTOMER.key,
            Enums.CallPNType.TYPE_BOOKING_CANCELLED.key, Enums.CallPNType.TYPE_HOME_HEALTHCARE_PAYMENT_CONFIRMATION.key,
            Enums.CallPNType.TYPE_HOME_VISIT_BOOKING_CONFIRMATION.key,
            Enums.CallPNType.TYPE_DELIVERY_COMPLETED.key
//                , Enums.CallPNType.TYPE_MESSAGE_SESSION_START.key
            -> {
                findNavController().safeNavigate(
                    R.id.action_notificationFragment_to_orders_navigation,
                    bundleOf(Constants.BOOKING_ID to item.entityId)
                )


            }
            Enums.CallPNType.BOOKING_RESCHEDULED_ACCEPTED.key, Enums.CallPNType.TYPE_PARTNER_BOOKING_REMINDER.key,
            Enums.CallPNType.TYPE_BOOKING_RESCHEDULED_PROCESSED.key -> {//7,32 33
                findNavController().safeNavigate(
                    R.id.action_notificationFragment_to_task_navigation,
                    bundleOf(
                        Constants.BOOKING_ID to item.entityId,
                        "partnerServiceId" to item.properties?.partnerServiceId,
                        "dutyId" to item.properties?.dutyId
                    )
                )
            }//doc
            Enums.CallPNType.TYPE_PARTNER_REQUEST_APPROVED.key -> {
                findNavController().safeNavigate(R.id.action_notificationFragment_to_partnerprofile_navigation)

            } //27,//10
            Enums.CallPNType.TYPE_CHAT_MESSAGE.key, Enums.CallPNType.TYPE_MESSAGE_SESSION_EXPIRED.key, Enums.CallPNType.TYPE_MESSAGE_SESSION_START.key -> {

                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra(Constants.SID, item.properties?.sid)
                startActivity(intent)

            } //21,//22
            //12, //11 //29
            Enums.CallPNType.TYPE_EMR_SHARED_WITH_DOCTOR.key, Enums.CallPNType.TYPE_MEDICAL_RECORD_SHARED.key, Enums.CallPNType.TYPE_EMR_REPORT_UPLOAD.key -> {
                emrViewModel.apply {
                    emrID = item.entityId?.toInt().getSafe()
                    isPatient = true
                }
                findNavController().safeNavigate(
                    R.id.action_notificationFragment_to_customer_emr_consultation_navigation

                )
            }
            Enums.CallPNType.TYPE_SMS_VERIFICATION.key -> {}//13
            Enums.CallPNType.TYPE_FAMILY_MEMBER_INVITED.key, Enums.CallPNType.TYPE_FAMILY_MEMBER_ADDED.key, Enums.CallPNType.TYPE_FAMILY_MEMBER_LINKED.key -> {
                findNavController().safeNavigate(
                    R.id.action_notificationFragment_to_personalprofile_navigation,
                    bundleOf("fromNoti" to true, "NotiType" to item.typeID)
                )
            }//14
            Enums.CallPNType.TYPE_CUSTOMER_REGISTRATION_COMPLETE.key -> {
                findNavController().popBackStack()
            }//15
            Enums.CallPNType.TYPE_EMPLOYEE_REGISTRATION_COMPLETE.key -> {}//16
            Enums.CallPNType.TYPE_PARTNER_REQUEST_REJECTED.key, Enums.CallPNType.TYPE_PARTNER_REQUEST_PROCESSED.key -> {
                if (DataCenter.getUser().isCustomer())
                    findNavController().safeNavigate(R.id.action_notificationFragment_to_become_partner_navigation)
                else {
                    findNavController().safeNavigate(R.id.action_notificationFragment_to_partnerprofile_navigation)
                }

            }//23
            Enums.CallPNType.TYPE_EMPLOYEE_LINKED.key,
            Enums.CallPNType.TYPE_CUSTOMER_LINKED.key -> {
                findNavController().safeNavigate(R.id.action_notificationFragment_to_linked_account_navigation)
            }
            Enums.CallPNType.TYPE_WALK_IN_TRANSACTION.key, Enums.CallPNType.TYPE_WALK_IN_APPROVED.key,
            Enums.CallPNType.TYPE_WALK_IN_REJECTED.key, Enums.CallPNType.TYPE_WALK_IN_DOCUMENT.key,
            Enums.CallPNType.WALK_IN_REQUEST_CONFIRMED.key, Enums.CallPNType.TYPE_WALK_IN_REQUEST.key,
            Enums.CallPNType.WALK_IN_CANCELLED_CUSTOMER.key, Enums.CallPNType.WALK_IN_CANCELLED_ADMIN.key -> {
                val services =
                    metaData?.partnerServiceType?.find { it.id == item.properties?.bookingDetail?.partnerServiceId }
                when (services?.id) {
                    CustomServiceTypeView.ServiceType.WalkInPharmacy.id ->//Walk-In Pharmacy

                        findNavController().safeNavigate(
                            R.id.action_notificationFragment_to_walkin_order_details_navigation,
                            bundleOf(
                                "fromNoti" to true,
                                "bookingId" to item.properties?.bookingDetail?.walkInPharmacyId
                            )
                        )
                    CustomServiceTypeView.ServiceType.WalkInHospital.id ->//Walk-In Hospital

                        findNavController().safeNavigate(
                            R.id.action_notificationFragment_to_walkin_hosp_order_details_navigation,
                            bundleOf(
                                "fromNoti" to true,
                                "bookingId" to item.properties?.bookingDetail?.walkInHospitalId
                            )
                        )
                    CustomServiceTypeView.ServiceType.WalkInLaboratory.id ->//Walk-In Laboratory

                        findNavController().safeNavigate(
                            R.id.action_notificationFragment_to_walkin_lab_order_details_navigation,
                            bundleOf(
                                "fromNoti" to true,
                                "bookingId" to item.properties?.bookingDetail?.walkInLaboratoryId
                            )
                        )
                }
            }

            Enums.CallPNType.TYPE_CLAIM_TRANSACTION.key, Enums.CallPNType.TYPE_CLAIM_REJECTED.key,
            Enums.CallPNType.TYPE_CLAIM_DOCUMENT.key, Enums.CallPNType.TYPE_CLAIM_APPROVED.key,
            Enums.CallPNType.TYPE_CLAIM_SETTLEMENT_ON_HOLD.key, Enums.CallPNType.TYPE_CLAIM_SETTLED.key,
            Enums.CallPNType.WALK_IN_CANCELLED_USER.key -> {
                findNavController().safeNavigate(
                    R.id.action_notificationFragment_to_claim_details_navigation,
                    bundleOf(
                        "fromNoti" to true,
                        "bookingId" to item.properties?.bookingDetail?.claimId
                    )
                )
            }

        }
    }

    override fun onDestroy() {
        notiViewModel.apply {
            categoryId = 0
            listItems.clear()
            notiListResponse.value = null

        }
        super.onDestroy()

    }

    private var loading = false
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var isLastPage = false
    var currentPage: Int? = 1

    private fun pagination() {
        mBinding.apply {
            val layoutManager = rvNotification.layoutManager as LinearLayoutManager
            rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) { //check for scroll down
                        visibleItemCount = layoutManager.childCount
                        totalItemCount = layoutManager.itemCount
                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                        if (loading.not()) {
                            if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                                notiViewModel.page = currentPage?.plus(1).getSafe()
                                if(isLastPage.not()){
                                    getNoti(
                                        NotificationRequest(
                                            categoryId = notiViewModel.categoryId,
                                            page = notiViewModel.page
                                        )
                                    )
                                    loading = true
                                }
                            }
                        }
                    }
                }
            })
        }
    }
}