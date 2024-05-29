package com.homemedics.app.ui.adapter

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.databinding.*
import com.homemedics.app.model.MessageListViewItem
import com.homemedics.app.ui.fragment.chat.ChatFragment
import com.homemedics.app.utils.Constants
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.visible
import com.twilio.conversations.Message
import timber.log.Timber
import java.io.IOException


class ChatAdapter :
    ListAdapter<DataItemChatListing, RecyclerView.ViewHolder>(DiffCallbackChatList()) {

    private var length = 0
    private var player: MediaPlayer? = null
var voiceNotePlayCheck:Boolean=false
    var itemClickListener: ((MessageListViewItem) -> Unit)? = null
    var deleteItemClickListener: ((MessageListViewItem, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Constants.HEADER -> {
                ViewHolderHeaderText(
                    LayoutChatDateHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.SESSIONEND -> {
                ViewHolderSessionEndText(
                    LayoutChatSessionEndBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.INCOMINGTEXT -> {
                ViewHolderIncomingText(
                    LayoutChatTextMsg2ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.INCOMINGMEDIAIMAGE -> {
                ViewHolderIncomingImage(
                    LayoutChatImage2ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.INCOMINGMEDIADOC -> {
                ViewHolderIncomingDoc(
                    LayoutChatDocument2ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.INCOMINGEMR -> {
                ViewHolderIncomingEmr(
                    LayoutChatEmr2ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.INCOMINGMEDIAAUDIO -> {
                ViewHolderIncomingAudio(
                    LayoutChatAudio2ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.OUTGOINGMEDIAIMAGE -> {
                ViewHolderOutgoingImage(
                    LayoutChatImage1ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.OUTGOINGMEDIAAUDIO -> {
                ViewHolderOutgoingAudio(
                    LayoutChatAudio1ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.OUTGOINGMEDIADOC -> {
                ViewHolderOutgoingDoc(
                    LayoutChatDocument1ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.OUTGOINGEMR -> {
                ViewHolderOutgoingEmr(
                    LayoutChatEmr1ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                ViewHolderOutgoingText(
                    LayoutChatTextMsg1ViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val modelChatItem = getItem(position) as DataItemChatListing

        when (holder) {
            is ViewHolderHeaderText -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderSessionEndText -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderIncomingText -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderIncomingImage -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderIncomingDoc -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderIncomingEmr -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderIncomingAudio -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }

            is ViewHolderOutgoingText -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem

                )
                setLongClickListener(
                    holder.itemView,
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderOutgoingImage -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem

                )
                setLongClickListener(
                    holder.itemView,
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderOutgoingDoc -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
                setLongClickListener(
                    holder.itemView,
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderOutgoingEmr -> {
                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
                setLongClickListener(
                    holder.itemView,
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
            is ViewHolderOutgoingAudio -> {

                holder.onBind(
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
                setLongClickListener(
                    holder.itemView,
                    (modelChatItem as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
                )
            }
        }
    }

    private fun setLongClickListener(
        holder: View,
        item: MessageListViewItem
    ) {
        holder.setOnLongClickListener {
            //for delete
            if (item.isSelected.not()) {
                item.isSelected = true
                deleteItemClickListener?.invoke(item, true)//add
                holder.setBackgroundColor(holder.resources.getColor(R.color.purple30, null))

            } else {
                deleteItemClickListener?.invoke(item, false)//remove
                item.isSelected = false

                holder.setBackgroundColor(holder.resources.getColor(R.color.white, null))
            }
            return@setOnLongClickListener true
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item =
            (getItem(position) as DataItemChatListing.DefaultItemListing).data as MessageListViewItem
        when (item.direction) {
            Constants.HEADER -> {
                return Constants.HEADER
            }
            Constants.SESSIONEND -> {
                return Constants.SESSIONEND
            }
            Constants.OUTGOING -> {
                return when (item.type) {
                    Message.Type.MEDIA.value -> {
                        when {
                            item.mimetype?.contains("image")
                                .getSafe() -> Constants.OUTGOINGMEDIAIMAGE
                            item.mimetype?.contains("audio")
                                .getSafe() -> Constants.OUTGOINGMEDIAAUDIO
//                            item.mimetype?.contains("pdf").getSafe()
                            else -> Constants.OUTGOINGMEDIADOC
                        }
                    }
                    -1 -> Constants.OUTGOINGEMR
                    else -> Constants.OUTGOINGTEXT
                }

            }

            else -> return when (item.type) {
                Message.Type.MEDIA.value -> {
                    when {
                        item.mimetype?.contains("image").getSafe() -> Constants.INCOMINGMEDIAIMAGE
                        item.mimetype?.contains("audio").getSafe() -> Constants.INCOMINGMEDIAAUDIO
//                        item.mimetype?.contains("pdf").getSafe() ||
//                                item.mimetype?.contains("application/msword").getSafe()
                        else -> Constants.INCOMINGMEDIADOC

                    }
                }
                -1 -> Constants.INCOMINGEMR
                else -> Constants.INCOMINGTEXT
            }
        }

    }


    inner class ViewHolderHeaderText(var binding: LayoutChatDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                dataManager = item

            }
        }
    }

    inner class ViewHolderSessionEndText(var binding: LayoutChatSessionEndBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                dataManager = item

            }
        }
    }

    //incoming
    inner class ViewHolderIncomingText(var binding: LayoutChatTextMsg2ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                dataManager = item

            }
        }
    }

    inner class ViewHolderIncomingImage(var binding: LayoutChatImage2ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                dataManager = item
                tvTime.text = item.timeCreated
                tvTitle.setOnClickListener {
                    if (item.mediaUrl != null)
                        itemClickListener?.invoke(item)

                }
            }
        }
    }

    inner class ViewHolderIncomingDoc(var binding: LayoutChatDocument2ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                dataManager = item
                tvTitle.setOnClickListener {
                    if (item.mediaUrl != null)
                        itemClickListener?.invoke(item)
                }
            }
        }
    }

    inner class ViewHolderIncomingEmr(var binding: LayoutChatEmr2ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                dataManager = item
                tvTitle.setOnClickListener {
                    itemClickListener?.invoke(item)
                }
            }
        }
    }

    private fun getAudioLength(mediaSize: String):String{
        var time = ""
        val units = mediaSize.split(":") //will break the string up into an array
        if (units.size.getSafe() > 1) {
            val minutes = units.get(0) //first element

            val seconds = units.get(1).trim().toInt() //second element
            if (minutes != "00") {
                time = time + minutes+" "+ApplicationClass.mGlobalData?.chatScreen?.min+" "
            }
            time = time + seconds +" "+ ApplicationClass.mGlobalData?.chatScreen?.sec


        }
        else{
            time=mediaSize
        }
        return time
    }
    inner class ViewHolderIncomingAudio(var binding: LayoutChatAudio2ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                tvTime.text = item.timeCreated
                tvDesc.text = getAudioLength(item.mediaSize.getSafe())
                ivFileType.tag = R.string.play
                ivFileType.setOnClickListener {
                    if (item.mediaUrl != null) {
                        if (ivFileType.tag == R.string.play) {
                            if(!voiceNotePlayCheck)
                            startPlaying(item, ivFileType)
                        } else {
                            if(voiceNotePlayCheck)
                            stopPlaying(ivFileType)
                        }

                    }
                }

            }
        }
    }

    //outgoing
    inner class ViewHolderOutgoingText(var binding: LayoutChatTextMsg1ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                setViewBackground(item, binding.root)
                dataManager = item
                setMsgStatus(ivReadStatus, item.msgStatus)

                clOutgoingTextParent.setOnClickListener {

                    if (ChatFragment.selectedListItems.value?.isEmpty()?.not().getSafe()) {
                        selectItems(item, clOutgoingTextParent)
                    }
                }

            }
        }
    }

    private fun setMsgStatus(view: ImageView, msgStatus: Int) {
        if (msgStatus == Constants.DELIVERED) {
            view.visible()
            view.setImageResource(R.drawable.ic_double_tick_gray)
        } else if (msgStatus == Constants.SENT) {
            view.visible()
            view.setImageResource(R.drawable.tick_grey)
        } else if (msgStatus == Constants.READ) {
            view.visible()
            view.setImageResource(R.drawable.ic_double_tick)
        }

    }

    inner class ViewHolderOutgoingImage(var binding: LayoutChatImage1ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                dataManager = item
                setViewBackground(item, binding.root)
                tvTime.text = item.timeCreated
                setMsgStatus(ivReadStatus, item.msgStatus)
                tvTitle.setOnClickListener {
                    if (ChatFragment.selectedListItems.value?.isEmpty().getSafe()) {
                        if (item.mediaUrl != null)
                            itemClickListener?.invoke(item)
                    } else
                        selectItems(item, clOutgoingImage)
                }
                clOutgoingImage.setOnClickListener {
                    if (ChatFragment.selectedListItems.value?.isEmpty()?.not().getSafe()) {

                        selectItems(item, clOutgoingImage)
                    }
                }

            }
        }
    }

    inner class ViewHolderOutgoingDoc(var binding: LayoutChatDocument1ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                dataManager = item
                setViewBackground(item, clOutgoingDoc)
                setMsgStatus(ivReadStatus, item.msgStatus)
                tvTitle.setOnClickListener {
                    if (ChatFragment.selectedListItems.value?.isEmpty().getSafe()) {
                        if (item.mediaUrl != null)
                            itemClickListener?.invoke(item)
                    } else {
                        selectItems(item, clOutgoingDoc)
                    }

                }
                clOutgoingDoc.setOnClickListener {
                    if (ChatFragment.selectedListItems.value?.isEmpty()?.not().getSafe()) {
                        selectItems(item, clOutgoingDoc)
                    }

                }
            }
        }
    }

    inner class ViewHolderOutgoingEmr(var binding: LayoutChatEmr1ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                dataManager = item
                setViewBackground(item, clOutgoingEmr)
                setMsgStatus(ivReadStatus, item.msgStatus)
                tvTitle.setOnClickListener {
                    if (ChatFragment.selectedListItems.value?.isEmpty().getSafe()) {
                        itemClickListener?.invoke(item)
                    } else {
                        selectItems(item, clOutgoingEmr)
                    }

                }
                clOutgoingEmr.setOnClickListener {
                    if (ChatFragment.selectedListItems.value?.isEmpty()?.not().getSafe()) {
                        selectItems(item, clOutgoingEmr)
                    }

                }
            }
        }
    }

    inner class ViewHolderOutgoingAudio(var binding: LayoutChatAudio1ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: MessageListViewItem) {
            binding.apply {
                setViewBackground(item, clOutgoingAudio)
                tvTime.text = item.timeCreated
                setMsgStatus(ivReadStatus, item.msgStatus)
                tvDesc.text =getAudioLength(item.mediaSize.getSafe())
                ivFileType.tag  = R.string.play
                ivFileType.setOnClickListener {
                    if (ChatFragment.selectedListItems.value?.isEmpty()?.not().getSafe()) {

                        selectItems(item, clOutgoingAudio)
                    } else {
                        if (item.mediaUrl != null) {
                            if (ivFileType.tag == R.string.play) {
                                Timber.e("voice $voiceNotePlayCheck")
                                if(!voiceNotePlayCheck)
                                startPlaying(item, ivFileType)
                            } else {
                                if(voiceNotePlayCheck)
                                stopPlaying(ivFileType)
                            }

                        }
                    }
                }
                clOutgoingAudio.setOnClickListener {
                    if (ChatFragment.selectedListItems.value?.isEmpty()?.not().getSafe()) {

                        selectItems(item, clOutgoingAudio)
                    }
                }
            }
        }
    }

    private fun setViewBackground(item: MessageListViewItem, view: View) {
        if (item.isSelected)
            view.setBackgroundColor(view.resources.getColor(R.color.purple30, null))
        else
            view.setBackgroundColor(view.resources.getColor(R.color.white, null))
    }

    private fun selectItems(item: MessageListViewItem, view: ConstraintLayout) {
        if (item.isSelected.not()) {
            item.isSelected = true
            deleteItemClickListener?.invoke(item, true)//add
            setViewBackground(item, view)

        } else {
            deleteItemClickListener?.invoke(item, false)//remove
            item.isSelected = false
            setViewBackground(item, view)
        }

    }

    private fun startPlaying(item: MessageListViewItem, ivFileType: ImageView) {

        player = MediaPlayer().apply {
            try {
                voiceNotePlayCheck=true
                ivFileType.tag = R.string.pause
                ivFileType.setImageResource(R.drawable.ic_pause)
                setDataSource(item.mediaUrl)

                prepare()
                seekTo(length)
                start()
            } catch (e: IOException) {
            }
        }
        player?.setOnCompletionListener {
            ivFileType.tag = R.string.play
            ivFileType.setImageResource(R.drawable.ic_play_solid_black)
            voiceNotePlayCheck=false
            length = 0
            player?.release()
            player = null

        }

    }

    private fun stopPlaying(ivFileType: ImageView) {
        voiceNotePlayCheck=false
        ivFileType.tag = R.string.play
        ivFileType.setImageResource(R.drawable.ic_play_solid_black)
        player?.pause()
        length = player?.currentPosition.getSafe()

    }


}

sealed class DataItemChatListing {

    data class DefaultItemListing(val data: MessageListViewItem?) : //Here data will be custom objectz
        DataItemChatListing() {
        override val id: String = data?.sid.toString()
        override val msgStatus: Int = data?.msgStatus.getSafe()
    }

    data class ShimmerLoaderDefault(val data: MessageListViewItem?) : DataItemChatListing() {
        override val id: String = data?.uuid.toString()
        override val msgStatus: Int = data?.msgStatus.getSafe()

    }


    abstract val id: String
    abstract val msgStatus: Int


}

class DiffCallbackChatList :
    DiffUtil.ItemCallback<DataItemChatListing>() {

    override fun areItemsTheSame(
        oldItem: DataItemChatListing,
        newItem: DataItemChatListing
    ): Boolean {
        return oldItem.id == newItem.id && oldItem.msgStatus==newItem.msgStatus
    }

    override fun areContentsTheSame(
        oldItem: DataItemChatListing,
        newItem: DataItemChatListing
    ): Boolean {
        return oldItem == newItem && oldItem.msgStatus==newItem.msgStatus
    }
}