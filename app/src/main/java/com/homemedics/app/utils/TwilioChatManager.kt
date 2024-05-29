package com.homemedics.app.utils

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fatron.network_module.models.response.chat.ConversationResponse
import com.homemedics.app.ApplicationClass
import com.homemedics.app.model.MessageListViewItem
import com.twilio.conversations.*
import org.json.JSONObject
import timber.log.Timber
import java.io.InputStream
import java.util.*
import kotlin.concurrent.schedule


class TwilioChatManager {
    var conversation: Conversation? = null
    var lastMessageInt: Long = 0
    var conversationSid: String = ""
    var isNotificationShow: Boolean = true
    var sessionEnd=MutableLiveData<Boolean?>()
    var rFcmCheck: Boolean = false
    var isMsgAvailable: Boolean = false
    var _messages = MutableLiveData<List<Message?>?>()
    val messages: LiveData<List<Message?>?> get() = _messages
    var onDeleteItemCall: ((item: Message) -> Unit)? = null
    var conversationUpdate = MutableLiveData<Conversation.UpdateReason>()
    var _chatTokenExpire = MutableLiveData<Boolean?>()
    val chatTokenExpire: LiveData<Boolean?> get() = _chatTokenExpire
    var conversationClients: ConversationsClient? = null
    var _chatSync = MutableLiveData<Boolean?>()
    var _chatError = MutableLiveData<Boolean?>()
    val chatError: LiveData<Boolean?> get() = _chatError
    val chatSync: LiveData<Boolean?> get() = _chatSync
    var chatCheck: Boolean = false
    private var fcmToken:String?=""
    var conversationClientCheck: Boolean = false
    var _sendMessSuccess = MutableLiveData<Boolean?>()
    val sendMessSuccess: LiveData<Boolean?> get() = _sendMessSuccess
    lateinit var context: Context
    fun initializeWithAccessToken(
        mContext: Context, token: String,
        mFcmToken: String?
    ) {
        Timber.e("twilio token")
        context = mContext
        val props: ConversationsClient.Properties =
            ConversationsClient.Properties.newBuilder().createProperties()
//        ConversationsClient.create(
//            context,
//            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImN0eSI6InR3aWxpby1mcGE7dj0xIn0.eyJqdGkiOiJTSzY1NzQ1ZjJjNTBjNjBhMjNhNDA5ODJiNzhhOTFmZWRlLTE2NjkxMTI4NDgiLCJncmFudHMiOnsiaWRlbnRpdHkiOiJ1c2VyMDEiLCJjaGF0Ijp7InNlcnZpY2Vfc2lkIjoiSVMxYmU5NzI4ZDczYmE0NDIwYjIyYjgwNTQwNWExNTVkMSIsInB1c2hfY3JlZGVudGlhbF9zaWQiOiJDUjExMDUwYTYwYjQ1ZDRmMzgxMDQwMzQ1MTE3ZTY5NjBkIn19LCJpYXQiOjE2NjkxMTI4NDgsImV4cCI6MTY2OTExNjQ0OCwiaXNzIjoiU0s2NTc0NWYyYzUwYzYwYTIzYTQwOTgyYjc4YTkxZmVkZSIsInN1YiI6IkFDZTg1MjQ2NDQ4NjBjNWZhNTk5M2E4ZDMzOTQ1ZjBhMzIifQ.fUIx4iHrgDx6UfpVvsMM9DPWaAws3UaiUIyLjMJd52I",
//            props,
//            object : CallbackListener<ConversationsClient> {
        conversationClientCheck = true
        ConversationsClient.create(
            context,
            token,
            props,
            object : CallbackListener<ConversationsClient> {
                override fun onSuccess(conversationsClient: ConversationsClient) {
                    Timber.e(conversationsClient.connectionState.value.toString())
                   fcmToken=mFcmToken
                    conversationClients = conversationsClient
                    conversationClients?.addListener(mConversationsClientListener)

                }

                override fun onError(errorInfo: ErrorInfo) {
                    _chatSync.postValue(false)
                    conversationClientCheck = false
                    Timber.e(errorInfo.message.toString())
                }
            })

    }

    fun registerFcm(conversationClient: ConversationsClient?, fcmToken: String?) {
        if(fcmToken.isNullOrEmpty().not()) {
            rFcmCheck = true
            conversationClient?.registerFCMToken(ConversationsClient.FCMToken(fcmToken),
                object : StatusListener {
                    override fun onSuccess() {
                        Timber.e("Fcm Success")
                        rFcmCheck = true

                    }


                    override fun onError(errorInfo: ErrorInfo?) {
                        super.onError(errorInfo)
                        rFcmCheck = false
                        Timber.e("Fcm Failed ${errorInfo?.code}${errorInfo?.message}")
                    }

                })
        }
    }

    fun unregisterFcmToken( fcmToken: String?) {
        conversationClients?.unregisterFCMToken(
            ConversationsClient.FCMToken(fcmToken),
            object :
                StatusListener {
                override fun onSuccess() {
                    rFcmCheck = false
                }


                override fun onError(errorInfo: ErrorInfo?) {
                     super.onError(errorInfo)
                }

            })
    }
//     fun loadConversationList()  {
//        if (conversationClients == null || conversationClients?.myConversations == null) {
//            return
//        }
////         val list= conversationClients?.myConversations?.map { list-> ConversationResponse(list.sid, list.friendlyName,list.lastMessageDate , null) }
//
////         chatViewModel. conversationList.postValue(list as ArrayList<ConversationResponse>)
//
//    }

    fun getConversationMessages(chatCount: Int, mConversationSid: String) {
        Timber.e("get conversation messages")
        if (conversationClients != null) {
            Timber.e("get conversation messages 1 ${conversationClients?.myConversations}")
            if (conversationClients?.myConversations.isNullOrEmpty().not()) {
                conversationSid = mConversationSid
                //   displayLogs("get conversation messages 2 channel id  ${twilioChatData.value?.channelId}")
                conversationClients?.getConversation(
                    conversationSid, object : CallbackListener<Conversation?> {
                        override fun onSuccess(result: Conversation?) {
                            Timber.e(result.toString())
                            if (result != null) {
                                _chatError.value = false
                                if (result.status == Conversation.ConversationStatus.JOINED
                                ) {
                                    Timber.e("Channel is already joined ${result.friendlyName} ")
                                    if (_chatSync.value == true) {
                                        conversation = result
                                        conversation?.addListener(
                                            mDefaultConversationListener
                                        )

                                        //conversation sync first
                                        loadPreviousMessages(conversation, chatCount)
                                    }
                                } else {
                                    Timber.e("channel is not joined")
                                    conversation?.let {
                                        joinConversation(
                                            it,
                                            chatCount,
                                            conversationSid
                                        )
                                    }
                                }

                            } else {
                                Timber.e("channel is not joined")
                                conversation?.let {
                                    joinConversation(
                                        it,
                                        chatCount,
                                        conversationSid
                                    )
                                }
                            }
                        }


                        override fun onError(errorInfo: ErrorInfo?) {
                            super.onError(errorInfo)
                            _chatError.postValue(true)
                            Timber.e("Message retrieve error ${errorInfo?.code}  ${errorInfo?.message}")
//                            if (errorInfo?.code != 50430 && conversationSid.isNullOrEmpty()
//                                    .not()
//                            )
//                                createConversation(chatCount)
                        }
                    })
            } else {
                _messages.value = null
                Timber.e("channel is not joined")

            }
        }
    }

    fun setAllMessageRead() {
        conversation?.setAllMessagesRead(CallbackListener {
//        conversation?.setLastReadMessageIndex(messageIndex.getSafe(), CallbackListener {
            Timber.e("long last read $it ${conversation?.dateUpdated}")

        })
    }

    fun setConversationMessageRead(messageIndex: Long?) {
        conversation?.setLastReadMessageIndex(messageIndex.getSafe(), CallbackListener {
            Timber.e("long last read $it ${conversation?.dateUpdated}")
            conversationUpdate.value = Conversation.UpdateReason.LAST_READ_MESSAGE_INDEX
        })
    }

    fun loadMorePreviousMessages(messageIndex: Long?, chatCount: Int) {
        Timber.e("last message $lastMessageInt ")
        messageIndex?.minus(1)?.let {
            conversation?.getMessagesBefore(
                it,
                chatCount,
                object : CallbackListener<List<Message>> {

                    override fun onSuccess(result: List<Message>) {
                        Timber.e("loadMorePreviousMessages")
                        setMoreMessages(result)


                    }

                    override fun onError(errorInfo: ErrorInfo) {

                        Timber.e(errorInfo.message)
                    }
                })
        }
    }

    fun setMoreMessages(messages: List<Message>) {
        try {
            val list: ArrayList<Message> = _messages.value as ArrayList<Message>
            for (message in messages.asReversed()) {
                list.add(0, message)
            }
            chatCheck = true
            _messages.postValue(list)


        } catch (exception: Exception) {

        }

    }

    fun loadPreviousMessages(conversation: Conversation?, chatCount: Int) {
        if (conversation?.synchronizationStatus?.isAtLeast(Conversation.SynchronizationStatus.ALL) == true) {
            conversation.getLastMessages(chatCount, object : CallbackListener<List<Message>> {
                override fun onSuccess(result: List<Message>) {
                    isMsgAvailable = result.isNotEmpty()
                    _messages.postValue(result)
                    if (result.isNotEmpty()) {
                        lastMessageInt = result[0].messageIndex
                        if (result.last().author != conversationClients?.myIdentity)
                            setAllMessageRead()
//                    if(result.last().author!=conversationClients?.myIdentity) {
//                        ApplicationClass.twilioChatManager?.conversation?.setLastReadMessageIndex(
//                            result.last().messageIndex,
//                            CallbackListener { Timber.e("long last read $it") })
//                    }

//                    conversation.setLastReadMessageIndex(result.last().messageIndex, CallbackListener { Timber.e("long last read $it")  })
                    }

                }

                override fun onError(errorInfo: ErrorInfo) {
                }
            })
        }
    }

    private fun joinConversation(
        conversation: Conversation,
        chatCount: Int,
        conversationSid: String
    ) {
        conversation.join(object : StatusListener {
            override fun onSuccess() {
                Timber.e("Joined success")
                Timer("SettingUp", false).schedule(30000) {
                    getConversationMessages(chatCount, conversationSid)
                }
            }

            override fun onError(errorInfo: ErrorInfo?) {
                Timber.e("Unable to join due to ${errorInfo?.message}")
                super.onError(errorInfo)
            }
        })
    }

    fun sendTextMessage(text: String, uuid: JSONObject) {
//        chatCheck = false
        Timber.e("message $text $uuid")
        val attributes = Attributes(uuid)
        val options = Message.options().withBody(text).withAttributes(attributes)
        sendMessage(options)
    }

    fun sendEmrMessage(text: JSONObject) {
//        chatCheck = false
        Timber.e("message $text  ")

        val attributes = Attributes(text)
        val options = Message.options().withAttributes(attributes)
        sendMessage(options)
    }

    private fun sendMessage(options: Message.Options) {
        chatCheck = false
        if (conversation != null) {
            conversation?.sendMessage(options, object : CallbackListener<Message> {
                override fun onSuccess(result: Message?) {
                    _sendMessSuccess.postValue(true)
                    Timber.e("send message ${result?.sid} ${result?.author}")
                }

                override fun onError(errorInfo: ErrorInfo?) {
                    super.onError(errorInfo)
                    Timber.e("error send message ${errorInfo?.code} ${errorInfo?.message}")
                }
            })
        }
    }

    fun sendMediaMessage(
        uri: String,
        inputStream: InputStream,
        fileName: String?,
        mimeType: String?, uuid: JSONObject,
        startMessageCallBack: (messageStatus: Boolean?) -> Unit,
    ) {
        var message: MessageListViewItem? = null
        val attributes = Attributes(uuid)
        var options = Message.options().withMedia(inputStream, mimeType).withAttributes(attributes)
            .withMediaProgressListener(object : ProgressListener {
                override fun onStarted() {
                    message = MessageListViewItem(
                        sid = "-1",
                        uuid = uuid.getString(Constants.UUID),
                        index = -1,
                        direction = Constants.OUTGOING,
                        body = null,
                        author = conversationClients?.myIdentity,
                        sendStatus = Constants.SENDING,
                        type = Message.Type.MEDIA.value,
                        mimetype = mimeType,
                        mediaName = fileName,
                        mediaUrl = uri

                    )
                    startMessageCallBack.invoke(true)
                    Timber.d("Upload started for $uri")
//                    conversationsRepository.updateMessageMediaUploadStatus(
//                        messageUuid,
//                        uploading = true
//                    )
                }

                override fun onProgress(uploadedBytes: Long) {
//                    uploadedBytes ->
                    Timber.d("Upload progress for $uri: $uploadedBytes bytes")
//                    conversationsRepository.updateMessageMediaUploadStatus(
//                        messageUuid,
//                        uploadedBytes = uploadedBytes
//                    )
                }

                override fun onCompleted(mediaSid: String?) {
                    Timber.d("Upload for $uri complete")
                    startMessageCallBack.invoke(false)
//                    conversationsRepository.updateMessageMediaUploadStatus(
//                        messageUuid,
//                        uploading = false
//                    )
                }
            })
        if (fileName != null) {
            options = options.withMediaFileName(fileName)
        }

        sendMessage(options)
    }

    fun deleteMessage(index: Long, sid: String) {

        conversation?.getMessageByIndex(
            index, object : CallbackListener<Message> {

                override fun onSuccess(message: Message) {
                    conversationClients?.getConversation(
                        sid,
                        object : CallbackListener<Conversation> {
                            override fun onSuccess(result: Conversation?) {
                                result?.removeMessage(
                                    message,
                                    object : StatusListener {

                                        override fun onSuccess() {
                                        }

                                        override fun onError(errorInfo: ErrorInfo) =
                                            showToast(errorInfo.message)

                                    }
                                )
                            }

                            override fun onError(errorInfo: ErrorInfo) =
                                showToast("get conv ${errorInfo.message}")

                        })
                }

                override fun onError(errorInfo: ErrorInfo) = showToast(errorInfo.message)

            })
    }

    val mConversationsClientListener: ConversationsClientListener =
        object : ConversationsClientListener {
            override fun onConversationAdded(conversation: Conversation) {}
            override fun onConversationUpdated(
                conversation: Conversation,
                updateReason: Conversation.UpdateReason
            ) {
                conversationUpdate.value = updateReason

            }

            override fun onConversationDeleted(conversation: Conversation) {}
            override fun onConversationSynchronizationChange(conversation: Conversation) {}
            override fun onError(errorInfo: ErrorInfo) {
                Timber.e("client error ${errorInfo.message}")
            }

            override fun onUserUpdated(user: User, updateReason: User.UpdateReason) {}
            override fun onUserSubscribed(user: User) {
                registerFcm(conversationClients, fcmToken)
            }
            override fun onUserUnsubscribed(user: User) {}
            override fun onClientSynchronization(synchronizationStatus: ConversationsClient.SynchronizationStatus) {
                when (synchronizationStatus) {
                    ConversationsClient.SynchronizationStatus.COMPLETED -> {
                        _chatSync.postValue(true)
                        if (fcmToken != null   && conversationClients != null) {
                            registerFcm(conversationClients, fcmToken)
                        }
                        Timber.e("Complete")

                    }
                    ConversationsClient.SynchronizationStatus.STARTED, ConversationsClient.SynchronizationStatus.CONVERSATIONS_COMPLETED -> {
                        Timber.e("STARTED or CONVERSATIONS_COMPLETED")

                    }
                    ConversationsClient.SynchronizationStatus.FAILED -> {
                        _chatSync.postValue(false)
                        Timber.e("Failed")
                    }
                }

            }

            override fun onNewMessageNotification(s: String, s1: String, l: Long) {
                Timber.e(
                    "new message $s $s1" +
                            ""
                )
            }

            override fun onAddedToConversationNotification(s: String) {}
            override fun onRemovedFromConversationNotification(s: String) {}
            override fun onNotificationSubscribed() {
                Timber.e(
                    "new notification "
                )
            }

            override fun onNotificationFailed(errorInfo: ErrorInfo) {
                Timber.e(
                    "new notification  ${errorInfo.code} ${errorInfo.message}"
                )
            }

            override fun onConnectionStateChange(connectionState: ConversationsClient.ConnectionState) {
                if (connectionState.value == ConversationsClient.ConnectionState.CONNECTED.value)
                else if (connectionState.value == ConversationsClient.ConnectionState.DENIED.value ||
                    connectionState.value == ConversationsClient.ConnectionState.DISCONNECTED.value ||
                    connectionState.value == ConversationsClient.ConnectionState.ERROR.value ||
                    connectionState.value == ConversationsClient.ConnectionState.FATAL_ERROR.value
                )

//                CONNECTING(0), //!< Transport is trying to connect and register or trying to recover.
//                CONNECTED(1), //!< Transport is working.
//                DISCONNECTED(2), //!< Transport is not working.
//                DENIED(3), //!< Transport was not enabled because authentication token is invalid or not authorized.
//                ERROR(4), //!< Error in connecting or sending transport message. Possibly due to offline.
//                FATAL_ERROR(5);
                    Timber.e("network ${connectionState.name} ${ConversationsClient.ConnectionState.CONNECTED} ${connectionState.value}")
            }

            override fun onTokenExpired() {
                _chatTokenExpire.value = true
                Timber.e("token expire")
            }

            override fun onTokenAboutToExpire() {

                Timber.e("token About To expire")
            }
        }


    val mDefaultConversationListener: ConversationListener =
        object : ConversationListener {
            override fun onMessageAdded(message: Message) {
                if (message.author != conversationClients?.myIdentity) {
                    chatCheck = true
                    setAllMessageRead()
                }

//                else { //for delivery
//                    val obj = JSONObject()
//                    obj.put("lastReceivedMessage", message.messageIndex)
//                    message.participant.setAttributes(
//                        Attributes(obj)
//                    ) {
//                        Timber.e("success")
//                    }
//
//                }

                if (messages.value.isNullOrEmpty().not()) {
                    val listMessage: ArrayList<Message> =
                        messages.value as ArrayList<Message>
                    listMessage.add(message)
                    _messages.value = listMessage
                } else {
                    val listMessage: ArrayList<Message> = arrayListOf()
                    listMessage.add(message)
                    _messages.value = listMessage
                }
            }

            override fun onMessageUpdated(message: Message, updateReason: Message.UpdateReason) {
                Timber.e("m updated", "Message updated: " + message.messageBody)
            }

            override fun onMessageDeleted(message: Message) {
                onDeleteItemCall?.invoke(message)
                Timber.e("m deleted", "Message deleted")
//                (_messages.value as ArrayList<Message> ).removeIf { it.sid==message.sid }

            }

            override fun onParticipantAdded(participant: Participant) {
                Timber.e("m part added", "Participant added: " + participant.identity)
            }

            override fun onParticipantUpdated(
                participant: Participant,
                updateReason: Participant.UpdateReason
            ) {
                Timber.e(
                    "m part updated",
                    "Participant updated: " + participant.lastReadMessageIndex + " " + updateReason.toString()
                )

                if (participant.lastReadMessageIndex != null && participant.lastReadMessageIndex > conversation?.lastReadMessageIndex.getSafe())
                    setConversationMessageRead(participant.lastReadMessageIndex)
            }

            override fun onParticipantDeleted(participant: Participant) {
//                Log.d(MainActivity.TAG, "Participant deleted: " + participant.identity)
            }

            override fun onTypingStarted(conversation: Conversation, participant: Participant) {
//                Log.d(MainActivity.TAG, "Started Typing: " + participant.identity)
            }

            override fun onTypingEnded(conversation: Conversation, participant: Participant) {
//                Log.d(MainActivity.TAG, "Ended Typing: " + participant.identity)
            }

            override fun onSynchronizationChanged(conversation: Conversation) {}
        }
}