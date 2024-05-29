package com.homemedics.app.ui.fragment.chat

import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.View
import android.widget.AbsListView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.chat.ConversationListRequest
import com.fatron.network_module.models.request.chat.TwilioTokenRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.chat.ConversationResponse
import com.fatron.network_module.models.response.chat.ConverstaionListResponse
import com.fatron.network_module.models.response.chat.TwilioTokenResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentChatTabBinding
import com.homemedics.app.ui.adapter.ConversationListingAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ChatViewModel
import com.twilio.conversations.*
import kotlinx.coroutines.*
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ChatTabFragment : BaseFragment(), View.OnClickListener {
    var currentItems: Int = 0
    var totalItems: Int = 0
    var scrollOutItems: Int = 0
    private var mLoading = false
    private var pagEnd: Boolean = false
    private var lastPage = 0
    var delay: Long = 1000 // 1 seconds after user stops typing

    private var taskJob: Job? = null

    var last_text_edit: Long = 0
    private val chatViewModel: ChatViewModel by activityViewModels()
    private lateinit var mBinding: FragmentChatTabBinding
    private lateinit var conversationAdapter: ConversationListingAdapter
    var bookingId = ""
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var myConversationList: List<Conversation>? = null
    val langData = ApplicationClass.mGlobalData
    override fun setLanguageData() {
mBinding.apply {
    etSearch.hint =langData?.chatScreen?.searchHere
    tvNoData.text =langData?.chatScreen?.noChatFound
}
    }

    override fun getFragmentLayout(): Int = R.layout.fragment_chat_tab

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentChatTabBinding
    }

    override fun setListeners() {
        conversationAdapter.itemClickListener = { item, _ ->
            navigateToChat(item)

            //           findNavController().safeNavigate(R.id.action_chatTabFragment_to_chatFragment, bundleOf("sid" to item.chatProperties.twilioChannelServiceId))
        }

        mBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                taskJob?.cancel()
                taskJob = lifecycleScope.launch {
                    if (s?.length.getSafe() > 0) {
                        last_text_edit = System.currentTimeMillis();
                        delay(delay)
                        if (System.currentTimeMillis() > last_text_edit + delay - 500) {
                            getList()
                        }
                    } else {
                        if (last_text_edit != 0L)
                            getList()
                        last_text_edit = 0
                        if (conversationAdapter.itemCount > 0)
                            chatViewModel.conversationList.postValue(arrayListOf())

                    }

                }

            }
        })

    }

    private fun navigateToChat(item: ConversationResponse) {
        ApplicationClass.twilioChatManager?._messages?.value = arrayListOf()
        chatViewModel.selectedBooking = item
        findNavController().safeNavigate(ConversationFragmentDirections.actionChatTabFragmentToChatFragment())

    }

    override fun init() {
        myConversationList =
            ApplicationClass.twilioChatManager?.conversationClients?.myConversations
        chatViewModel.conversationList.value = arrayListOf()
        setAdapter()
        setObserver()
        if (requireActivity().intent.hasExtra("bookingId")) {
            bookingId = requireActivity().intent.getStringExtra("bookingId").getSafe()
            requireActivity().intent.removeExtra("bookingId")
        }

    }

    private fun setAdapter() {
        linearLayoutManager = LinearLayoutManager(activity)
        conversationAdapter =
            ConversationListingAdapter(tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key).not())
        mBinding.apply {
            rvConversation.adapter = conversationAdapter
            rvConversation.layoutManager = linearLayoutManager
            rvConversation.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                        Timber.e("page ${chatViewModel.pageNum}")
                        chatViewModel.pageNum = chatViewModel.pageNum.plus(1)
                        if (!pagEnd && chatViewModel.pageNum <= lastPage) {
                            getList()

                        }

                    }
                }
            })
        }
    }

    private fun setObserver() {
        ApplicationClass.twilioChatManager?.conversationUpdate?.observe(this) {
            it ?: return@observe
            if (it == Conversation.UpdateReason.LAST_MESSAGE) {
                if (myConversationList?.isEmpty()?.not().getSafe())
                    myConversationList =
                        ApplicationClass.twilioChatManager?.conversationClients?.myConversations

                checkUnreadStatus()
                ApplicationClass.twilioChatManager?.conversationUpdate?.value = null
            }
        }
        chatViewModel.conversationList.observe(this) { it ->
            it ?: return@observe
            pagEnd = false

            if (it.isNullOrEmpty().not()) {
                val tempList = conversationAdapter.listItems

                tempList.clear()
                tempList.addAll(it as ArrayList<ConversationResponse>)

//                val distinctList = tempList.distinctBy {
//                    if (it.chatProperties != null) Pair(it.chatProperties?.id, it.bookedForUser?.id)
//                    else Pair(it.partnerChatProperties?.id, it.bookedForUser?.id)
//                } as ArrayList<ConversationResponse>
                conversationAdapter.listItems = tempList.sortedByDescending {
                    if (it.chatProperties != null) {
                        it.chatProperties?.lastMessageTime.getSafe()
                    } else {
                        it.partnerChatProperties?.lastMessageTime.getSafe()
                    }
                }.toCollection(ArrayList())
                checkUnreadStatus()
            } else {
                conversationAdapter.listItems.clear()
                pagEnd = true

            }

        }
    }

    private fun checkUnreadStatus() {
        Timber.e("size ${conversationAdapter.listItems.size} ")
        conversationAdapter.listItems.forEach {
            var chatPropertyCheck = true // for chatProperties
            val sid = if (it.chatProperties != null) {
                it.chatProperties?.twilioChannelServiceId.getSafe()
            } else {
                chatPropertyCheck = false //for partnerChatProperties
                it.partnerChatProperties?.twilioChannelServiceId.getSafe()
            }

            val list = myConversationList
            val index = list?.indexOfFirst { it.sid == sid }

            if (index != null && index != -1) {
                list?.get(index.getSafe())?.getUnreadMessageCount(chatPropertyCheck)

            }
        }
    }

    private fun updateConversation(sid: String?, chatPropertyCheck: Boolean) {
        ApplicationClass.twilioChatManager?.apply {
            if (conversationClients != null) {
                if (conversationClients?.myConversations.isNullOrEmpty().not()) {

                    conversationClients?.getConversation(
                        sid
                    ) { result ->
                        if (result != null && _chatSync.value == true) {
                            val lastMsgIndex = result.lastMessageIndex.getSafe()
                            val msgIndex = result.lastReadMessageIndex.getSafe()
//check msg count
                            if (msgIndex == 0L) {
                                result.updateMsgCount(lastMsgIndex, chatPropertyCheck, sid, true)
                            } else if (msgIndex < result.lastMessageIndex.getSafe()) {
                                result.updateMsgCount(lastMsgIndex, chatPropertyCheck, sid, true)
                            } else
                                result.updateMsgCount(lastMsgIndex, chatPropertyCheck, sid, false)


                        }
                    }
                }
            }
        }

    }

    private fun Conversation.updateMsgCount(
        lastMsgIndex: Long,
        chatPropertyCheck: Boolean,
        sid: String?,
        countStatus: Boolean
    ) {
        getMessageByIndex(
            lastMsgIndex
        ) {
            var index = 0
            var count = 0
            val list = conversationAdapter.listItems
            if (countStatus && it.author != ApplicationClass.twilioChatManager?.conversationClients?.myIdentity) {
                count = -1
                //for delivery
                val obj = JSONObject()
                obj.put("lastReceivedMessage", it.messageIndex)
                this.getParticipantByIdentity(ApplicationClass.twilioChatManager?.conversationClients?.myIdentity)
                    .setAttributes(
                        Attributes(obj), object : StatusListener {
                            override fun onSuccess() {
                                Timber.e("success")
                            }

                            override fun onError(errorInfo: ErrorInfo?) {
                                super.onError(errorInfo)
                                Timber.e("error ${errorInfo?.message}")
                            }


                        })

            }
            var timeFormat = "yyyy-MM-dd hh:mm:ss aa"
            if (DateFormat.is24HourFormat(binding.root.context)) {
                timeFormat = "yyyy-MM-dd HH:mm:ss"
            }
            var dateTime = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").format(Date())
            if (lastMessageDate != null) {
                dateTime = lastMessageDate.toString()
            }
            val time = getDateInFormat(
                dateTime,
                "EEE MMM dd HH:mm:ss z yyyy",
                timeFormat
            )

            if (chatPropertyCheck) { //for chatProperty
                val item = list.find {
                    it.chatProperties?.twilioChannelServiceId.getSafe() == sid
                }
                item?.apply {
                    chatProperties?.unreadMessagesCount = count


                    chatProperties?.lastMessageTime = time
                    chatProperties?.timeLocale = null

                }

                index =
                    list.indexOfFirst { it.chatProperties?.twilioChannelServiceId.getSafe() == sid }
            } else { //for partner chat
                val item = list.find {
                    it.partnerChatProperties?.twilioChannelServiceId.getSafe() == sid
                }

                item?.apply {
                    partnerChatProperties?.unreadMessagesCount = count
                    partnerChatProperties?.lastMessageTime = time
                    partnerChatProperties?.timeLocale = null
                }

                index =
                    list.indexOfFirst { it.partnerChatProperties?.twilioChannelServiceId.getSafe() == sid }
            }

            conversationAdapter.notifyItemChanged(index)
            conversationAdapter.listItems =
                conversationAdapter.listItems.sortedByDescending {
                    if (it.chatProperties != null) {
                        it.chatProperties?.lastMessageTime.getSafe()
                    } else {
                        it.partnerChatProperties?.lastMessageTime.getSafe()
                    }
                }.toCollection(ArrayList())

        }
    }

    private fun Conversation.getUnreadMessageCount(chatPropertyCheck: Boolean) =
        CoroutineScope(Dispatchers.IO).launch {
            val msid = sid
            updateConversation(msid, chatPropertyCheck)
//            getUnreadMessagesCount { count ->
//                Timber.e("msg $msid $count")
//                if (count != null) {
//                    var index = 0
//
//                    val list = conversationAdapter.listItems
//                    if (chatPropertyCheck) { //for chatProperty
//                        list.find {
//                            it.chatProperties?.twilioChannelServiceId.getSafe() == msid
//                        }?.chatProperties?.unreadMessagesCount = count.toInt()
//                        index =
//                            list.indexOfFirst { it.chatProperties?.twilioChannelServiceId.getSafe() == msid }
//                    } else { //for partner chat
//                        list.find {
//                            it.partnerChatProperties?.twilioChannelServiceId.getSafe() == msid
//                        }?.partnerChatProperties?.unreadMessagesCount = count.toInt()
//                        index =
//                            list.indexOfFirst { it.partnerChatProperties?.twilioChannelServiceId.getSafe() == msid }
//                    }
//                    conversationAdapter.notifyItemChanged(index)
////                    conversationAdapter.listItems =
////                        conversationAdapter.listItems.sortedWith(compareBy<ConversationResponse>
////
////                        {
////                            it.id
////                        }.thenByDescending { if (it.chatProperties != null) {
////                            it.chatProperties?.unreadMessagesCount.getSafe()
////                        } else {
////                            it.partnerChatProperties?.unreadMessagesCount.getSafe()
////                        }}.thenByDescending {
////                            if (it.chatProperties != null) {
////                                it.chatProperties?.lastMessageTime.getSafe()
////                            } else {
////                                it.partnerChatProperties?.lastMessageTime.getSafe()
////                            }
////                        }).toCollection(ArrayList())
//
//
////                    conversationAdapter.listItems =
////                        conversationAdapter.listItems.sortedByDescending {
////                            if (it.chatProperties != null) {
////                                it.chatProperties?.unreadMessagesCount.getSafe()
////                            } else {
////                                it.partnerChatProperties?.unreadMessagesCount.getSafe()
////                            }
////                        }.toCollection(ArrayList())
//                    conversationAdapter.listItems =
//                        conversationAdapter.listItems.sortedByDescending {
//                            if (it.chatProperties != null) {
//                                it.chatProperties?.lastMessageTime.getSafe()
//                            } else {
//                                it.partnerChatProperties?.lastMessageTime.getSafe()
//                            }
//                        }.toCollection(ArrayList())
//
//
//                }
//            }
        }

    override fun onResume() {
        super.onResume()
        if (isOnline(requireContext())) {
            chatViewModel.pageNum = 1
            getList()
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title =  langData?.errorMessages?.internetError.getSafe(),
                    message = langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun getList() {
        var userType = 1 //doctor
        var searchData: String? = null
        if (mBinding.etSearch.text.isEmpty().not())
            searchData = mBinding.etSearch.text.toString()
        var type = "current"
        if (chatViewModel.selectedTab.key == 2)
            type = "history"
        var conversationRequest =
            ConversationListRequest(
                page = chatViewModel.pageNum,
                name = searchData,
                type = type
            )
        if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key).not()) {
            userType = 0 //customer ...show doctors
            conversationRequest =
                ConversationListRequest(page = chatViewModel.pageNum, name = searchData)
        }
        chatViewModel.getPartnersList(conversationRequest, userType)
            .observe(this) {
                when (it) {
                    is ResponseResult.Success -> {
//                        chatViewModel.conversationList.value = arrayListOf()
                        val response =
                            it.data as ResponseGeneral<ConverstaionListResponse>
                        if (response.data?.bookings?.converstionList?.isEmpty()
                                ?.not() == true
                        ) {
                            if (ApplicationClass.twilioChatManager?.conversationClientCheck == false || ApplicationClass.twilioChatManager?.conversationClients == null) {
                                getTwilioChatTokenCall()
                            }
                        }
                        chatViewModel.conversationList.value =
                            response.data?.bookings?.converstionList as ArrayList<ConversationResponse>
                        mBinding.apply {
                            rvConversation.setVisible(
                                chatViewModel.conversationList.value.isNullOrEmpty()
                                    .not()
                            )
                            tvNoData.setVisible(chatViewModel.conversationList.value.isNullOrEmpty())
                        }
                        lastPage = response.data?.bookings?.lastPage.getSafe()
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

    override fun onDestroy() {
        super.onDestroy()
        taskJob?.cancel()
    }

    override fun onClick(view: View?) {

    }

    private fun getTwilioChatTokenCall() {
        val request = TwilioTokenRequest(
            participantId = DataCenter.getUser()?.id.getSafe(),
            deviceType = getString(R.string.device)
        )
        chatViewModel.getTwilioChatToken(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {

                    val response = it.data as ResponseGeneral<TwilioTokenResponse>
                    TinyDB.instance.putString(
                        com.fatron.network_module.utils.Enums.TinyDBKeys.CHATTOKEN.key,
                        response.data?.token.getSafe()
                    )
                    if (response.data?.token?.isNotEmpty() == true && ApplicationClass.twilioChatManager?.conversationClients == null && ApplicationClass.twilioChatManager?.conversationClientCheck == false) {
                        ApplicationClass.twilioChatManager?.initializeWithAccessToken(
                            requireContext(), response.data?.token.getSafe(),
                            TinyDB.instance.getString(
                                com.fatron.network_module.utils.Enums.TinyDBKeys.FCM_TOKEN.key
                            )
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