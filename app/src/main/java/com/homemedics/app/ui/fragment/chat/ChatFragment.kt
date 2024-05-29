package com.homemedics.app.ui.fragment.chat

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import androidx.constraintlayout.widget.Barrier
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.fatron.network_module.models.request.chat.ConversationSessionRequest
import com.fatron.network_module.models.request.chat.SendEmrRequest
import com.fatron.network_module.models.request.chat.TwilioTokenRequest
import com.fatron.network_module.models.request.chat.chatDetailRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.chat.ConversationResponse
import com.fatron.network_module.models.response.chat.TwilioTokenResponse
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.databinding.FragmentChatBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.model.MessageListViewItem
import com.homemedics.app.ui.activity.CheckoutActivity
import com.homemedics.app.ui.adapter.ChatAdapter
import com.homemedics.app.ui.adapter.DataItemChatListing
import com.homemedics.app.ui.custom.CustomAudio
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.ChatViewModel
import com.homemedics.app.viewmodel.EMRViewModel
import com.twilio.conversations.CallbackListener
import com.twilio.conversations.Conversation
import com.twilio.conversations.ErrorInfo
import com.twilio.conversations.Message
import kotlinx.coroutines.*
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ChatFragment : BaseFragment(), View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private val chatViewModel: ChatViewModel by activityViewModels()
    private lateinit var mBinding: FragmentChatBinding
    private var conversationAdapter: ChatAdapter = ChatAdapter()
    private var newMessagesLoaded = false
    private var isEmr = false
    private lateinit var dialogViewBinding: FragmentAddVoicenoteBinding
    private var length = 0
    private var player: MediaPlayer? = null

    private var dialogSaveButton: Button? = null
    private val emrViewModel: EMRViewModel by activityViewModels()
    private var file: File? = null
    private lateinit var meter: Chronometer
    private var absolutePath: String = ""
    private var elapsedMillis = 0L
    private var recorder: MediaRecorder? = null
    private lateinit var animBlink: Animation
    private lateinit var fileUtils: FileUtils
    var msgIndex: Long? = null
    var fromOrder: Boolean? = false

    private lateinit var audio: CustomAudio

    companion object {
        var selectedListItems = MutableLiveData<MutableList<MessageListViewItem>>(arrayListOf())
    }

    private var messageList = arrayListOf<MessageListViewItem>()
    private var dateList: List<String>? = arrayListOf<String>()
    private var sid = ""
    val langData = ApplicationClass.mGlobalData
    override fun setLanguageData() {
        mBinding.apply {
            tvSendMsgText.text = langData?.chatScreen?.sendMsg
            sendMessageInclude.etMessage.setHint("${langData?.globalString?.Messages}...")
            val locale =
                TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
            if (locale == DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)
                include.brDelete.type = Barrier.LEFT
            else
                include.brDelete.type = Barrier.RIGHT
        }

    }

    override fun getFragmentLayout(): Int = R.layout.fragment_chat

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentChatBinding
    }

    override fun init() {
        audio = CustomAudio(requireContext())
        ApplicationClass.twilioChatManager?.isMsgAvailable = true
        fromOrder = arguments?.getBoolean("fromOrder")
        if (arguments?.getString("sid").isNullOrEmpty().not()) {
            callChatDetailApi(arguments?.getString("sid"))
        } else {
            setNGetData()
        }
        if (ApplicationClass.twilioChatManager?.conversationClientCheck == false || ApplicationClass.twilioChatManager?.conversationClients == null)
            getTwilioChatToken()
//        conversationAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
        ApplicationClass.twilioChatManager?.isNotificationShow = false
        initializeRecyclerview()
        setObserver()
        fileUtils = FileUtils()
        fileUtils.init(this)
        animBlink = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.blink
        )
    }

    override fun onPause() {
        super.onPause()
        audio.onPause()

        if (isEmr == false)
            ApplicationClass.twilioChatManager?.conversation?.removeAllListeners()
    }

    override fun onResume() {
        super.onResume()
        if (isEmr == false) {
            ApplicationClass.twilioChatManager?.conversation?.addListener(ApplicationClass.twilioChatManager?.mDefaultConversationListener)
            if (messageList.isNotEmpty() && messageList.last().author != ApplicationClass.twilioChatManager?.conversationClients?.myIdentity)
                ApplicationClass.twilioChatManager?.setAllMessageRead()
        } else
            isEmr = false
    }

    private fun setNGetData() {
        if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key).not() && emrViewModel.emrID != 0)
            sendEmrMessage()

        selectedListItems.value = arrayListOf()

        sid = if (chatViewModel.selectedBooking?.chatProperties != null) {
            chatViewModel.selectedBooking?.chatProperties?.twilioChannelServiceId.getSafe()
        } else {
            chatViewModel.selectedBooking?.partnerChatProperties?.twilioChannelServiceId.getSafe()
        }
        if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key)
                .not() && (chatViewModel.selectedBooking?.bookingMessage?.sessionStatus == Enums.SessionStatuses.ENDED.key || chatViewModel.selectedBooking?.bookingStatusId == Enums.AppointmentStatusType.CANCEL.key)
        )
            mBinding.apply {
                sendMessageInclude.llChatInput.gone()
                renewSessionInclude.langData = langData
                renewSessionInclude.llSessionEnd.visible()
            }

        initActionbar(chatViewModel.selectedBooking)
    }

    private fun sendEmrMessage() {
        mBinding.rvChat.scrollToPosition(0)
        if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key)
                .not()
        )
            callSendEmrApi(
                SendEmrRequest(
                    emrId = emrViewModel.emrID,
                    customerId = chatViewModel.selectedBooking?.bookedForUser?.id,
                    doctorId = chatViewModel.selectedBooking?.partnerUserId
                )
            )
        ApplicationClass.twilioChatManager?.sendEmrMessage(getAttributeObject(1))
    }

    private fun getAttributeObject(fromEmr: Int? = 0): JSONObject {
        val messageUuid = UUID.randomUUID().toString()
        val obj = JSONObject()
        obj.put(Constants.UUID, messageUuid)
        if (fromEmr.getSafe() == 1) { //from emr
            obj.put(Constants.EMR_SHARED, true)
            obj.put(Constants.EMR_ID, emrViewModel.emrID)
            obj.put(Constants.EMR_NUM, emrViewModel.emrNum)
            obj.put(Constants.EMR_TYPE, emrViewModel.selectedEMRType?.key)
            obj.put(
                Constants.BOOKING_ID,
                chatViewModel.selectedBooking?.bookingMessage?.bookingId.getSafe()
            )
            emrViewModel.emrID = 0
        } else if (fromEmr.getSafe() == 2) {//
            Timber.e("elapsedMillis ${audio.elapsedMillis}")
            val dateString: String = DateFormat.format("mm:ss", Date(audio.elapsedMillis)).toString()
            obj.put(Constants.AUDIO_LENGTH, dateString)

        }
        return obj
    }

    override fun onDestroy() {
        super.onDestroy()
        ApplicationClass.twilioChatManager?.conversation?.removeAllListeners()
        ApplicationClass.twilioChatManager?.conversationSid = ""
        ApplicationClass.twilioChatManager?._messages?.value = null
        ApplicationClass.twilioChatManager?.conversation = null
        ApplicationClass.twilioChatManager?.isNotificationShow = true
        chatViewModel.pageNum = 1
        selectedListItems.value = arrayListOf()
    }

    override fun setListeners() {

        ApplicationClass.twilioChatManager?.onDeleteItemCall = { message ->
            var index =
                messageList.indexOfFirst { it.sid == message.sid }
            messageList.removeIf { it.sid == message.sid }
            val messageDateList = messageList.filter {
                it.dateCreated == message.dateCreatedAsDate.time.asMessageDateString()
                        || it.timeCreated == message.dateCreatedAsDate.time.asMessageDateString()
            }
            if (messageDateList.size <= 1) {
                index =
                    messageList.indexOfFirst { it.dateCreated == message.dateCreatedAsDate.time.asMessageDateString() || it.timeCreated == message.dateCreatedAsDate.time.asMessageDateString() }
                messageList.removeIf { it.dateCreated == message.dateCreatedAsDate.time.asMessageDateString() || it.timeCreated == message.dateCreatedAsDate.time.asMessageDateString() }
            }
            (ApplicationClass.twilioChatManager?.messages?.value as ArrayList<Message>).removeIf { it.sid == message.sid }


            conversationAdapter.submitList(
                messageList.map { DataItemChatListing.DefaultItemListing(it) }
            )
            selectedListItems.value?.removeIf { it.sid == message.sid }

            selectedListItems.value = selectedListItems.value
            mBinding.progressBar.gone()
        }

        conversationAdapter.deleteItemClickListener = { item, actionCheck ->
            if (actionCheck) {
                selectedListItems.value?.add(item)
                showDeleteIcon()
            } else {
                selectedListItems.value?.remove(item)
                showDeleteIcon()
            }
        }

        conversationAdapter.itemClickListener = { item ->

            if (item.type == Message.Type.MEDIA.value) {

                when {
                    item.mimetype?.contains("image").getSafe() -> if (item.mediaUrl != null) {
                        openImage(item.mediaUrl.getSafe())
                    } else {
                        showToast("Please wait..")
                    }

                    else -> if (item.mediaUrl != null) {
                        openDoc(item.mediaUrl)
                    } else {
                        showToast("Please wait..")
                    }
                }
            } else if (item.type == -1) {
                navigateToEmrDetail(item)
            }

        }



        mBinding.apply {
            renewSessionInclude.bBookConsultation.setOnClickListener {
                callRenewSession()
            }
            include.apply {
                ivBack.setOnClickListener {
                    requireActivity().onBackPressed()
                }
                ivDelete.setOnClickListener {
                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(langData?.dialogsStrings?.confirmDelete.getSafe(),
                        langData?.dialogsStrings?.deleteMessage.getSafe(),
                        positiveButtonStringText = langData?.globalString?.yes.getSafe(),
                        negativeButtonStringText = langData?.globalString?.cancel.getSafe(),
                        buttonCallback = {
                            deleteMessages()
                        })

                }
            }

            sendMessageInclude.apply {
                etMessage.doAfterTextChanged {
                    if (it?.length.getSafe() > 0) {
                        ivSend.visible()
                        ivCancel.gone()
                        attachmentsInclude.llAttachments.gone()
                        ivAttachment.gone()
                    } else {
                        ivSend.gone()
                        ivCancel.gone()
                        ivAttachment.visible()
                    }
                }
                ivMic.setOnClickListener {


                    ivCancel.gone()
                    ivAttachment.visible()
                    ivSend.gone()
                    etMessage.text.clear()

                    attachmentsInclude.llAttachments.gone()

                    fileUtils.requestAudioPermission(requireActivity()) { result ->
                        if (result)
                            showVoiceNoteDialog()
                        else
                            displayNeverAskAgainDialog(langData?.dialogsStrings?.recordPermissions.getSafe())
                    }

                }

                ivCancel.setOnClickListener {
                    attachmentsInclude.llAttachments.gone()
                    ivAttachment.visible()
                    ivCancel.gone()
                }

                ivAttachment.setOnClickListener {
                    attachmentsInclude.llAttachments.visible()
                    ivCancel.visible()
                    ivAttachment.gone()
                }

                ivSend.setOnClickListener {
                    etMessage.text.toString().takeIf { it.isNotBlank() }
                        ?.let { message ->
                            if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key)) {
                                if (chatViewModel.selectedBooking?.bookingMessage?.sessionStatus == 0) {
                                    if (fromOrder.getSafe().not())
                                        callChatSession()
                                }
                            }
                            ApplicationClass.twilioChatManager?.sendTextMessage(
                                message,
                                getAttributeObject()
                            )
                            etMessage.text?.clear()

                        }
                }

//                IvVoicePause.setOnClickListener {
//                    if (::meter.isInitialized) {
//                        elapsedMillis = SystemClock.elapsedRealtime() - meter.base
//                        stopRecording()
//                        if (elapsedMillis == 0L) {
//                            deleteAudioFile()
//
//                        } else
//                            saveFile()
//                    }
//
//
//                }
//
//
//                tvCancel.setOnClickListener {
//                    stopRecording()
//                    deleteAudioFile()
//
//
//                }
            }

            attachmentsInclude.apply {
                ivEMR.setOnClickListener {
                    if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key).not()) {
                        findNavController().safeNavigate(
                            ChatFragmentDirections.actionChatFragmentToPatientEmrNavigation(
                                bookingId = chatViewModel.selectedBooking?.bookingMessage?.bookingId.getSafe()
                            )
                        )
                    } else {
                        emrViewModel.partnerServiceId =
                            CustomServiceTypeView.ServiceType.Message.id.toString()

                        emrViewModel.bookingId =
                            chatViewModel.selectedBooking?.bookingMessage?.bookingId.getSafe()
                        emrViewModel.customerId =
                            chatViewModel.selectedBooking?.bookedForUser?.id.getSafe().toString()
                        emrViewModel.isDraft = true
                        emrViewModel.emrChat = true
                        emrViewModel.docEmr.value = false
                        chatViewModel.selectedBooking?.bookedForUser?.apply {
                            emrViewModel.consultationFilterRequest.customerId = id
                            emrViewModel.selectedFamily = FamilyConnection(
                                id = id,
                                userId = id,
                                familyMemberId = id,
                                fullName = fullName,
                            )
                        }

                        isEmr = true
                        findNavController().safeNavigate(ChatFragmentDirections.actionChatFragmentToMedicalRecordsNavigation())

                    }
                }
                ivCamera.setOnClickListener {
                    selectCaptureImage(mCameraCaptureOnly = true, addImageOnly = false)
                }
                ivImage.setOnClickListener {
                    selectCaptureImage(mCameraCaptureOnly = true, addImageOnly = true)
                }
                ivDoc.setOnClickListener {
                    fileUtils.requestFilePermissions(requireActivity(), false) { result ->
                        if (result == null) {
                            displayNeverAskAgainDialog(langData?.dialogsStrings?.storagePermissions.getSafe())
                        } else {
                            file = result.uri.let { uri ->
                                uri?.let {
                                    fileUtils.getMimeType(requireContext(), uri = uri)?.let { it1 ->
                                        fileUtils.copyUriToFile(
                                            requireContext(),
                                            it,
                                            fileUtils.getFileNameFromUri(
                                                requireContext(), uri
                                            ),
                                            it1
                                        )
                                    }
                                }
                            }
                            saveFile()
                        }
                    }
                }
            }

        }
    }


    private fun setObserver() {

        ApplicationClass.twilioChatManager?.sessionEnd?.observe(this) {
            it ?: return@observe
            if (it == true) {
                findNavController().popBackStack()
                ApplicationClass.twilioChatManager?.sessionEnd?.value = null
            }
        }
        ApplicationClass.twilioChatManager?.chatError?.observe(this) {
            it ?: return@observe
            if (it) {
                mBinding.progressBar.gone()
                if (messageList.isEmpty())
                    ApplicationClass.twilioChatManager?.getConversationMessages(20, sid)
            }
        }
        ApplicationClass.twilioChatManager?.conversationUpdate?.observe(this) {
            it ?: return@observe
            updateConversation()
//            if (it == Conversation.UpdateReason.LAST_MESSAGE) {
////                updateMessageStatus()
////                ApplicationClass.twilioChatManager?.getConversationUpdated(sid)
//            }
//            if (messageList.last().author != ApplicationClass.twilioChatManager?.conversationClients?.myIdentity) {
//                if (ApplicationClass.twilioChatManager?.conversationClients != null) {
//                    ApplicationClass.twilioChatManager?.getConversationMessages(
//                        20,
//                        mConversationSid = sid
//                    )
//                }
//            }
        }

        emrViewModel.docEmr.observe(this) {
            if (it) {
                if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key)) {
                    if (chatViewModel.selectedBooking?.bookingMessage?.sessionStatus == 0) {
                        if (fromOrder.getSafe().not())
                            callChatSession()
                        emrViewModel.docEmr.value = false
                    }
                }
            }
        }

        chatViewModel.twilioToken.observe(this) {
            if ((it?.isNotEmpty() == true && ApplicationClass.twilioChatManager?.conversationClients == null && ApplicationClass.twilioChatManager?.conversationClientCheck == false) || ApplicationClass.twilioChatManager?.chatTokenExpire?.value == true) {
//                && ApplicationClass.twilioChatManager?.sendMessSuccess?.value == true
                ApplicationClass.twilioChatManager?.apply {
                    _chatSync.value = false
                    _chatTokenExpire.value = false
                    initializeWithAccessToken(
                        requireContext(), it,
                        TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.FCM_TOKEN.key)
                    )
                }
                Timber.e("observer twilioToken")

            }
        }

        ApplicationClass.twilioChatManager?.apply {
            chatTokenExpire.observe(this@ChatFragment) {
                if (it == true) {
                    Timber.e("observer chatTokenExpire")
                    getTwilioChatToken()
                    ApplicationClass.twilioChatManager?.conversationClients = null

                }
            }
            chatSync.observe(this@ChatFragment) {
                it ?: return@observe
                if (it == false) {
                    getTwilioChatToken()
                } else if (it == true) {
                    if (conversationClients != null) {
                        val token =
                            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.FCM_TOKEN.key)
                        if (token != null && rFcmCheck == false)
                            registerFcm(
                                ApplicationClass.twilioChatManager?.conversationClients,
                                token
                            )
                        mBinding.progressBar.visible()
                        if (messageList.isEmpty())
                            getConversationMessages(20, sid)
                    }

                }
            }
            messages.observe(this@ChatFragment) {

                mBinding.progressBar.gone()
                if (it.isNullOrEmpty().not()) {
                    mBinding.rvChat.visible()
                    mBinding.tvSendMsgText.gone()
                    setData(it as List<Message>)
                } else {
                    if (ApplicationClass.twilioChatManager?.isMsgAvailable?.not().getSafe()) {
                        mBinding.rvChat.gone()
                        mBinding.tvSendMsgText.visible()

                    }
                }
            }
            sendMessSuccess.observe(this@ChatFragment) {
                if (it == true) {
                    Timber.e("observer sendMessSuccess")
                    mBinding.rvChat.visible()
                    ApplicationClass.twilioChatManager?._sendMessSuccess?.value = false
                }
            }
        }

        selectedListItems.observe(this) {
            showDeleteIcon()
        }
    }

    private fun saveFile() {
        if (file?.let {
                metaData?.maxFileSize?.let { it1 ->
                    fileUtils.photoUploadValidation(
                        it,
                        it1
                    )
                }
            }.getSafe()) {
            showFileSizeDialog()

        } else {
            mBinding.attachmentsInclude.llAttachments.gone()
            mBinding.sendMessageInclude.ivCancel.gone()
            mBinding.sendMessageInclude.ivAttachment.visible()
            sendMediaMessage()

        }
    }

    private fun sendMediaMessage() {
        val uri = file?.let { fileUtils.getUriFromFile(context = requireContext(), it) }
        val mimeType = uri?.let { fileUtils.getMimeType(requireContext(), uri = it) }
        val inputStream = uri?.let { requireContext().contentResolver.openInputStream(it) }
        val fileName = uri?.let {
            fileUtils.getFileNameFromUri(
                requireContext(), it
            )
        }
        if (inputStream != null) {

            ApplicationClass.twilioChatManager?.sendMediaMessage(
                uri.toString(),
                inputStream = inputStream,
                fileName,
                mimeType,
                getAttributeObject(if (mimeType?.contains("audio").getSafe()) 2 else 0)
            ) { status ->
                Timber.e("status $status")

                if (status.getSafe()) {
                    mBinding.progressBar.visible()
                    mBinding.sendMessageInclude.ivAttachment.isClickable = false
                    mBinding.sendMessageInclude.ivMic.isClickable = false
//                    mBinding.sendMessageInclude.etMessage.isClickable = false
//                    mBinding.sendMessageInclude.etMessage.isFocusable = false
                } else {
                    mBinding.progressBar.gone()

//                    mBinding.sendMessageInclude.etMessage.isClickable = true
                    mBinding.sendMessageInclude.ivAttachment.isClickable = true
                    mBinding.sendMessageInclude.ivMic.isClickable = true

//                    mBinding.sendMessageInclude.etMessage.isFocusable = true
                }

            }

        }
    }

    private fun showFileSizeDialog() {
        DialogUtils(requireActivity())
            .showSingleButtonAlertDialog(
                title = langData?.globalString?.information.getSafe(),
                message = langData?.dialogsStrings?.fileSize.getSafe(),
                buttonCallback = {},
            )
    }

    private fun updateMessageStatus() {
        messageList.forEach {
            var status: Int? = null
            if (msgIndex != null && it.index.getSafe() <= msgIndex.getSafe()) {
                status = Constants.READ
            }

            if (status != null) {
                it.msgStatus = status
            }
        }

        val lastVisibleMessageIndex =
            (mBinding.rvChat.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        Timber.e("SCROLL TO POS: ${conversationAdapter.itemCount - 1} $lastVisibleMessageIndex")
        // Scroll list to bottom when it was at the bottom before submitting new messages
        if (ApplicationClass.twilioChatManager?.chatCheck.getSafe().not()) {
            val commitCallback: java.lang.Runnable? =
                if (lastVisibleMessageIndex == conversationAdapter.itemCount - 1) {
                    Runnable {
                        mBinding.rvChat.postDelayed(Runnable {
                            Timber.e("SCROLL TO POS1: ${conversationAdapter.itemCount - 1}")

                            if (conversationAdapter.itemCount > 0) {
                                mBinding.rvChat.smoothScrollToPosition(
                                    conversationAdapter.itemCount - 1
                                )
                            }

//                        }, 500)
//                            if (mBinding.rvChat.adapter?.itemCount != -1)
//                            mBinding.rvChat.smoothScrollToPosition(
//                                conversationAdapter.itemCount - 1
//                            )
                        }, 600)

                    }
                } else {
                    null
                }

            if (mBinding.rvChat.adapter?.itemCount != -1)
                mBinding.rvChat.adapter?.itemCount?.let {
                    mBinding.rvChat.smoothScrollToPosition(
                        it
                    )
//                    (mBinding.rvChat.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(2, 20);


                }
            conversationAdapter.submitList(
                messageList.map { DataItemChatListing.DefaultItemListing(it) }, commitCallback
            )
        } else {
            if (messageList.isNotEmpty() && (messageList.last().author != ApplicationClass.twilioChatManager?.conversationClients?.myIdentity)) {
                val lastVisibleMessageIndex =
                    (mBinding.rvChat.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (lastVisibleMessageIndex > 9) {
                    mBinding.rvChat.postDelayed(Runnable {
                        Timber.e("SCROLL TO POS1: ${conversationAdapter.itemCount - 1}")
                        mBinding.rvChat.smoothScrollToPosition(
                            conversationAdapter.itemCount - 1
                        )
                    }, 500)
                }
            }

            conversationAdapter.submitList(
                messageList.map { DataItemChatListing.DefaultItemListing(it) })

        }
    }


    private fun updateConversation() {
        ApplicationClass.twilioChatManager?.apply {
            if (conversationClients != null) {
                if (conversationClients?.myConversations.isNullOrEmpty().not()) {

                    conversationClients?.getConversation(
                        sid
                    ) { result ->
                        Timber.e(result.toString())
                        if (result != null) {

                            if (result.status == Conversation.ConversationStatus.JOINED
                            ) {
                                try {
                                    Timber.e("${msgIndex} last ${result.lastReadMessageIndex}")
                                    msgIndex = result.lastReadMessageIndex

                                    updateMessageStatus()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setData(list: List<Message>) {
        if (mBinding.messageListRefresher.isRefreshing) {
            mBinding.messageListRefresher.isRefreshing = false
        }
        mBinding.progressBar.gone()
        messageList = arrayListOf()
        ApplicationClass.twilioChatManager?.conversationClients?.let {

            dateList = list.distinctBy { it.dateCreatedAsDate.time.asMessageDateString() }
                .map { it.dateCreatedAsDate.time.asMessageDateString() } as ArrayList<String>

            list.asMessageDataItems(
                it.myIdentity
            )

        }
        msgIndex = ApplicationClass.twilioChatManager?.conversation?.lastReadMessageIndex
        updateMessageStatus()


    }

    private fun List<Message>.asMessageDataItems(identity: String, uuid: String = "") {
        dateList?.forEach {
            val list =
                this.filter { message -> message.dateCreatedAsDate.time.asMessageDateString() == it }

            if (list.isNotEmpty()) {
                if (list[0].attributes.jsonObject?.has(
                        Constants.SESSION_EXPIRE
                    ).getSafe()
                ) {

                    if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key)
                            .not() && (chatViewModel.selectedBooking?.bookingMessage?.sessionStatus == Enums.SessionStatuses.ENDED.key && chatViewModel.selectedBooking?.bookingStatusId == Enums.AppointmentStatusType.CANCEL.key)
                    )
                        mBinding.apply {
                            sendMessageInclude.llChatInput.gone()
                            renewSessionInclude.llSessionEnd.visible()
                        }

                }
                messageList.add(
                    MessageListViewItem(
                        direction = Constants.HEADER,
                        dateCreated = list[0].dateCreatedAsDate.time.asMessageDateHeaderString()
                            ?: langData?.chatScreen?.today,
                        timeCreated = list[0].dateCreatedAsDate.time.asMessageDateString()
                    )
                ) //header
                list.mapIndexed { index, item ->
                    val message = item.toMessageDataItem(
                        identity,
                        uuid,
                        item.messageIndex,
                        isAuthorChanged(index)
                    )
                    if (message != null) {
                        messageList.add(
                            message
                        )
                    }
                }

            }

        }
    }

    private fun Message.toMessageDataItem(
        currentUserIdentity: String = participant.identity,
        uuid: String = "",
        index: Long,
        authorChanged: Boolean
    ): MessageListViewItem? {
        val authorName = this.author

        val chatDir =
            if (this.attributes.jsonObject?.has(
                    Constants.SESSION_EXPIRE
                ).getSafe() &&
                this.attributes.jsonObject?.getBoolean(Constants.SESSION_EXPIRE).getSafe()
            )
                Constants.SESSIONEND else if (this.author == currentUserIdentity) Constants.OUTGOING else {
                if (this.attributes.jsonObject?.has("partner_user_id").getSafe() &&
                    this.attributes.jsonObject?.getInt("partner_user_id") == DataCenter.getUser()?.id
                )
                    Constants.OUTGOING
                else
                    Constants.INCOMING
            }
//for delivery
        var lastReceivedMessage: String? = null
        var status = Constants.SENT
//        if (this.participant.attributes.jsonObject?.has("lastReceivedMessage").getSafe()) {
//            if (this.participant.attributes.jsonObject?.get("lastReceivedMessage")==this.messageIndex)
//            status = Constants.DELIVERED
//
//            lastReceivedMessage =
//                this.participant.attributes.jsonObject?.get("lastReceivedMessage").toString()
//            Timber.e("last re ${ ApplicationClass.twilioChatManager?.conversation?.getParticipantByIdentity(this.author)?.attributes?.jsonObject?.get("lastReceivedMessage")} ${this.participant.attributes.jsonObject?.get("lastReceivedMessage")}")
//        }

        if (this.hasMedia().getSafe())
            this.getMediaContentTemporaryUrl(index)
        val selectItem = selectedListItems.value?.find { it.sid == this.sid }
        var type = this.type.value
        if (this.attributes.jsonObject?.has(Constants.EMR_SHARED).getSafe() &&
            this.attributes.jsonObject?.getBoolean(Constants.EMR_SHARED).getSafe()
        )
            type = -1
        if (this.attributes.jsonObject?.has(
                Constants.SESSION_EXPIRE
            ).getSafe().not() || (this.attributes.jsonObject?.has(
                Constants.SESSION_EXPIRE
            ).getSafe() &&
                    this.attributes.jsonObject?.getBoolean(Constants.SESSION_EXPIRE)
                        .getSafe() && tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key))
        )
            return MessageListViewItem(
                sid = this.sid,
                uuid = uuid,
                index = this.messageIndex,
                direction = chatDir,
                body = this.messageBody ?: "",
                author = authorName,
                isAuthorChange = authorChanged,
                dateCreated = if (this.attributes.jsonObject?.has(
                        Constants.SESSION_EXPIRE
                    ).getSafe().not()
                ) this.dateCreatedAsDate.time.asMessageDateString() else "",
                timeCreated = this.dateCreatedAsDate.time.asMessageTimeString(),
                sendStatus = if (this.author == currentUserIdentity) Constants.SENT else Constants.UNDEFINED,
                type = type,
                mimetype = if (this.hasMedia().getSafe()) this.mediaType else "",
                mediaName = if (this.hasMedia().getSafe()) {
                    if (this.mediaFileName.isNullOrEmpty())
                        getFileName(this.mediaType, this.dateCreatedAsDate.time)
                    else
                        this.mediaFileName.lowercase(Locale.US).replaceFirstChar { it.uppercase() }
                } else "",
                mediaSize = if (this.hasMedia()
                        .getSafe()
                ) {
                    if (this.mediaType.contains("audio")
                            .getSafe() && this.attributes.jsonObject?.has(Constants.AUDIO_LENGTH)
                            .getSafe()
                    ) this.attributes.jsonObject?.getString(Constants.AUDIO_LENGTH) else (this.mediaSize / 1024).toString() + ' ' + langData?.chatScreen?.kb.getSafe()
                } else "",
                recordNum = if (this.attributes.jsonObject?.has(Constants.EMR_SHARED)
                        .getSafe() && this.attributes.jsonObject?.has(Constants.EMR_NUM).getSafe()
                ) this.attributes.jsonObject?.getString(Constants.EMR_NUM).getSafe() else "",
                recordType = if (this.attributes.jsonObject?.has(Constants.EMR_SHARED)
                        .getSafe() && this.attributes.jsonObject?.has(Constants.EMR_TYPE).getSafe()
                ) getRecordType(
                    this.attributes.jsonObject?.getInt(Constants.EMR_TYPE).getSafe()
                ) else langData?.chatScreen?.messageConsultation.getSafe(),
                attribute = this.attributes.jsonObject,
                msgStatus = status,
                lastReceivedMessage = lastReceivedMessage
                //            mediaUrl = if (this.hasMedia().getSafe()) mediaUrl else ""
            ).apply {
                tag = R.string.play
                isSelected = selectItem != null
            }
        return null
    }

    private fun getFileName(mediaType: String, time: Long): String {
        return when {
            mediaType.contains("pdf")
                .getSafe() -> "$time.pdf"
            mediaType.contains("image")
                .getSafe() -> "$time.png"
            else -> time.toString()
        }
    }

    private fun getRecordType(emrType: Int): String {
        val emrObject = metaData?.customerEmrTypes?.find { it.genericItemId == emrType }
        return emrObject?.genericItemName?.getSafe()
            ?: langData?.chatScreen?.messageConsultation.getSafe()
    }

    private fun deleteMessages() =
        CoroutineScope(Dispatchers.IO).launch {
            selectedListItems.value?.forEach { it ->
                withContext(Dispatchers.Main) {
                    mBinding.progressBar.visible()
                }
                it.index?.let { it1 ->
                    ApplicationClass.twilioChatManager?.deleteMessage(it1, sid)
                }

            }
        }


    private fun Message.getMediaContentTemporaryUrl(index: Long) =
        CoroutineScope(Dispatchers.IO).launch {
            getMediaContentTemporaryUrl(object : CallbackListener<String> {
                override fun onSuccess(contentTemporaryUrl: String) {
                    messageList.find { it.index == index }?.mediaUrl = contentTemporaryUrl

                    val mIndex = messageList.indexOfFirst { it.index == index }
                    conversationAdapter.notifyItemChanged(mIndex)
                }

                override fun onError(errorInfo: ErrorInfo) {}

            })
        }

    private fun List<Message>.isAuthorChanged(index: Int): Boolean {
        if (index == 0) return true
        return this[index].author != this[index - 1].author
    }

    private fun Long.asMessageTimeString(): String {
        if (this == 0L) {
            return ""
        }
//        val instant = Instant.fromEpochMilliseconds(this)
//        val now = Clock.System.now()
//        val timeZone = TimeZone.currentSystemDefault()
//        val days: Int = instant.daysUntil(now, timeZone)
        var timeFormat = "hh:mm aa"
        if (DateFormat.is24HourFormat(binding.root.context)) {
            timeFormat = "HH:mm"
        }
//        val dateFormat = if (days == 0) timeFormat else timeFormat
        return SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date(this))
    }

    private fun Long.asMessageDateString(): String {
        if (this == 0L) {
            return ""
        }
        val dateFormat = "dd/MM/yyyy"
        return SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date(this))
    }

    private fun Long.asMessageDateHeaderString(): String? {
        if (this == 0L) {
            return ""
        }
        return TimeAgo.getTimeAgo(this, "dd MMMM yyyy")
    }

    private fun initActionbar(selectedBooking: ConversationResponse?) {
        mBinding.include.apply {
            //set date in views
            var userName = selectedBooking?.customerUser?.fullName
            var bookedFor =
                "${langData?.globalString?.bookedFor.getSafe()} ${langData?.globalString?.self.getSafe()}"
            var userProfile = selectedBooking?.customerUser?.userProfilePicture?.file

            if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key)
                    .not() || DataCenter.getUser()?.id != selectedBooking?.partnerUser?.id
            ) {
//            if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key).not() || DataCenter.getUser()?.id!=selectedBooking?.partnerUser?.id) {
                userName = selectedBooking?.partnerUser?.fullName
                userProfile = selectedBooking?.partnerUser?.userProfilePicture?.file
                bookedFor =
                    if (selectedBooking?.customerUser?.id == selectedBooking?.bookedForUser?.id) {
                        "${langData?.globalString?.bookedFor.getSafe()} ${langData?.globalString?.self.getSafe()}"
                    } else
                        "${langData?.globalString?.bookedFor.getSafe()} ${selectedBooking?.bookedForUser?.fullName}"
            } else if (DataCenter.getUser()?.id == selectedBooking?.partnerUser?.id) {
                userName = selectedBooking?.bookedForUser?.fullName
                userProfile = selectedBooking?.bookedForUser?.userProfilePicture?.file
                bookedFor =
                    if (selectedBooking?.customerUser?.id == selectedBooking?.bookedForUser?.id) {
                        "${langData?.globalString?.bookedBy.getSafe()} ${langData?.globalString?.self.getSafe()}"
                    } else
                        "${langData?.globalString?.bookedBy.getSafe()} ${selectedBooking?.customerUser?.fullName}"
            }

            val status =
                if (selectedBooking?.bookingStatusId == Enums.AppointmentStatusType.CANCEL.key)
                    langData?.chatScreen?.cancelled
                else if (selectedBooking?.bookingStatusId == Enums.AppointmentStatusType.COMPLETE.key || selectedBooking?.bookingMessage?.sessionStatus == Enums.SessionStatuses.ENDED.key)
                    langData?.chatScreen?.sessionComplete
                else {
                    TimeAgo.timeLeft(
                        selectedBooking?.bookingMessage?.timeLeft,
                        langData?.chatScreen
                    )
                }

            mBinding.include.apply {
                tvName.text = userName
                tvStatus.text = "${Constants.END}$status${Constants.START}"
                val locale =
                    TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
                if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)
                    tvStatus.textDirection = 4
                else

                    tvStatus.textDirection = 3
                tvStatus.visible()
                tvBookedBy.text = bookedFor
                ivDrImage.loadImage(
                    userProfile,
                    getGenderIcon(selectedBooking?.bookedForUser?.genderId.toString())
                )
            }

        }
    }

    private fun initializeRecyclerview() {
        mBinding.rvChat.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                val lastVisibleMessageIndex =
                    (mBinding.rvChat.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                conversationAdapter.itemCount.let {
                    if (it > 0 && lastVisibleMessageIndex >= it - 5) {
                        mBinding.rvChat.postDelayed(Runnable {
                            Timber.e("SCROLL TO POS: ${it - 1}")
                            mBinding.rvChat.smoothScrollToPosition(
                                it - 1
                            )
                        }, 100)
                    }
                }
            }
        }
        mBinding.sendMessageInclude.etMessage.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                Timber.e("count ${mBinding.rvChat.adapter?.itemCount} ${conversationAdapter.itemCount - 1}")
                mBinding.rvChat.adapter?.itemCount?.let {
                    mBinding.rvChat.scrollToPosition(
                        it
                    )
                }

            }
        }

        mBinding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        mBinding.rvChat.adapter = conversationAdapter
        mBinding.messageListRefresher.setOnRefreshListener(this)
        if (mBinding.messageListRefresher.isRefreshing) {
            mBinding.messageListRefresher.isRefreshing = false
        }


    }

    override fun onRefresh() {
        loadMessageOnRefresh()
    }


    private fun loadMessageOnRefresh() {
        try {
            newMessagesLoaded = true
            if (ApplicationClass.twilioChatManager?.messages?.value?.size ?: 0 > 0) {

                if (ApplicationClass.twilioChatManager?.messages?.value?.get(0)?.messageIndex ?: 0 > 0) {

                    ApplicationClass.twilioChatManager?.loadMorePreviousMessages(
                        ApplicationClass.twilioChatManager?.messages?.value?.get(0)?.messageIndex,
                        10
                    )
                } else {
                    if (mBinding.messageListRefresher.isRefreshing) {
                        mBinding.messageListRefresher.isRefreshing = false
                    }
                }
            } else {
                if (mBinding.messageListRefresher.isRefreshing) {
                    mBinding.messageListRefresher.isRefreshing = false
                }
            }

        } catch (exception: Exception) {

        }
    }

    override fun onClick(view: View?) {

    }

    private fun getTwilioChatToken() {
        val request = TwilioTokenRequest(
            participantId = DataCenter.getUser()?.id.getSafe(),
            deviceType = getString(R.string.device)
        )

        chatViewModel.getTwilioChatToken(request).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<TwilioTokenResponse>
                    tinydb.putString(
                        com.fatron.network_module.utils.Enums.TinyDBKeys.CHATTOKEN.key,
                        response.data?.token.getSafe()
                    )
                    chatViewModel._twilioToken.value = response.data?.token

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

            }
        }
    }

    private fun callRenewSession() {
        val checkOutIntent = Intent(requireContext(), CheckoutActivity::class.java)
        checkOutIntent.putExtra("fromBDC", true)
        checkOutIntent.putExtra("fromChat", true)
        checkOutIntent.putExtra(
            Constants.BOOKING_ID,
            chatViewModel.selectedBooking?.bookingMessage?.bookingId
        )
        startActivity(checkOutIntent)
    }

    private fun callChatSession() {
        chatViewModel.selectedBooking?.bookingMessage?.bookingId?.getSafe()
            ?.let { ConversationSessionRequest(bookingId = it) }?.let {
                chatViewModel.callChatSession(it).observe(this) {
                    when (it) {
                        is ResponseResult.Success -> {
                            chatViewModel.selectedBooking?.bookingMessage?.sessionStatus = 1
                            //                    val response = it.data as ResponseGeneral<TwilioTokenResponse>
                            //                    chatViewModel._twilioToken.value = response.data?.token

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

                    }
                }
            }
    }

    private fun callChatDetailApi(sid: String?) {

        chatViewModel.callChatDetailApi(chatDetailRequest(sid)).observe(this) {
            when (it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<ConversationResponse>
                    chatViewModel.selectedBooking = response.data
                    ApplicationClass.twilioChatManager?.apply {
                        if (conversationClients != null) {
                            if (messageList.isEmpty())
                                getConversationMessages(20, sid.getSafe())
                        }
                    }
                    setNGetData()
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

            }
        }

    }

    private fun callSendEmrApi(request: SendEmrRequest) {

        chatViewModel.callSendEmrApi(request).observe(this) {
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
                    showLoader()
                }
                is ResponseResult.Complete -> {
                    hideLoader()
                }

            }
        }

    }

    private fun openDoc(mediaUrl: String?) {
        val browserIntent = Intent(Intent.ACTION_VIEW)
        browserIntent.setDataAndType(Uri.parse(mediaUrl), "application/pdf")

        val chooser =
            Intent.createChooser(browserIntent, langData?.chatScreen?.chooserTitle.getSafe())
        chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK // optional
        startActivity(chooser)
    }

    private fun navigateToEmrDetail(item: MessageListViewItem) {
        emrViewModel.apply {
            emrID = item.attribute?.getString(Constants.EMR_ID)?.toInt().getSafe()
            isPatient = true
        }
        findNavController().safeNavigate(
            ChatFragmentDirections.actionChatFragmentToCustomerConsultationRecordDetailsFragment2()
        )
    }

    private fun openImage(mediaUrl: String) {
        val displayRectangle = Rect()
        val window: Window = requireActivity().getWindow()
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        val builder =
            AlertDialog.Builder(requireContext())
        val viewGroup: ViewGroup = requireActivity().findViewById(android.R.id.content)
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_view_image, viewGroup, false)

        builder.setView(dialogView)


        val ivBack = dialogView.findViewById<ImageView>(R.id.ivBack)

        val ivApptImage = dialogView.findViewById<ImageView>(R.id.ivApptImage)
        ivApptImage.layoutParams.width = (resources.displayMetrics.widthPixels * 0.80).toInt()
        ivApptImage.layoutParams.height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        Glide.with(requireContext())
            .load(mediaUrl)
            .fitCenter()
            .into(ivApptImage)

        val alertDialog = builder.create()
        alertDialog.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        ivBack.setOnClickListener {
            alertDialog.dismiss()
        }

    }

    private fun showDeleteIcon() {
        if (selectedListItems.value?.size.getSafe() > 0) {
            mBinding.include.ivDelete.visible()
            mBinding.include.tvStatus.gone()
        } else {
            mBinding.include.ivDelete.gone()
            mBinding.include.tvStatus.visible()
        }
    }

    private fun deleteAudioFile() {
        if (file?.exists().getSafe()) {
            file?.delete()
        }
    }

    private fun startRecording() {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireContext())
        } else {
            MediaRecorder()
        }
        recorder.apply {

            this?.setAudioSource(MediaRecorder.AudioSource.MIC)
            this?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            this?.setOutputFile(absolutePath)
            this?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                this?.prepare()
                this?.start()
                lifecycleScope.launch {
                    delay(1000L)

                    meter.start()
                    meter.startAnimation(animBlink)


                }

            } catch (e: IOException) {
                Timber.e("Exception prepare() failed ${Log.getStackTraceString(e)}")
            }


        }
    }


    private fun showVoiceNoteDialog() {

        audio.apply {
            title = langData?.dialogsStrings?.addVoiceNote.getSafe()
            var voiceNoteText = langData?.dialogsStrings?.patientVoiceNote

            if (tinydb.getBoolean(Enums.PlannerMode.PLANNER_MODE.key).not())
                voiceNoteText = langData?.dialogsStrings?.voiceNoteDescription
            positiveButtonText = langData?.globalString?.done.getSafe()
            negativeButtonText = langData?.globalString?.cancel.getSafe()
            voiceNote = voiceNoteText.getSafe()
            onSaveFile = { mfile, time ->
                file = mfile
                elapsedMillis = time
                saveFile()
            }
            show()
        }
    }

    private fun selectCaptureImage(mCameraCaptureOnly: Boolean, addImageOnly: Boolean) {
        fileUtils.requestPermissions(
            requireActivity(), mOnlyImageCheck = addImageOnly,
            mCameraCaptureOnly = mCameraCaptureOnly
        ) { result ->
            if (result == null) {
                displayNeverAskAgainDialog(langData?.dialogsStrings?.storagePermissions.getSafe())
            } else {
                file = result.uri.let { uri ->
                    uri?.let {
                        fileUtils.copyUriToFile(
                            requireContext(),
                            it,
                            fileUtils.getFileNameFromUri(
                                requireContext(), uri
                            ),
                            Constants.MEDIA_TYPE_IMAGE
                        )
                    }
                }
                saveFile()
            }
        }
    }

    private fun displayNeverAskAgainDialog(message: String) {
        DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
            message = message,
            positiveButtonText = R.string.permit_manual,
            negativeButtonText = R.string.close,
            buttonCallback = {
                mBinding.sendMessageInclude.apply {
                    llMessage.visible()
                    ivMic.visible()

                }
                context?.let { gotoAppSettings(it) }
            },
            negativeButtonCallback = {
                mBinding.sendMessageInclude.apply {
                    llMessage.visible()
                    ivMic.visible()

                }
            },
            cancellable = false
        )

    }

    private fun startPlaying(itemId: String?) {

        player = MediaPlayer().apply {
            try {
                dialogViewBinding.IvPlay.tag = R.string.pause
                dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_pause)
                setDataSource(itemId)

                prepare()
                seekTo(length)
                start()
            } catch (e: IOException) {
            }
        }
        player?.setOnCompletionListener {
            dialogViewBinding.IvPlay.tag = R.string.play
            dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_play_arrow)
            length = 0
            player?.release()
            player = null

        }

    }


    private fun stopPlaying() {
        dialogViewBinding.IvPlay.tag = R.string.play
        dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_play_arrow)
        player?.pause()
        length = player?.currentPosition.getSafe()

    }
}