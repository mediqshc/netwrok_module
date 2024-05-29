package com.homemedics.app.ui.adapter

import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import com.fatron.network_module.models.response.chat.ConversationResponse
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemConversationListBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*
import timber.log.Timber

class ConversationListingAdapter(private val plannerMode: Boolean) :
    BaseRecyclerViewAdapter<ConversationListingAdapter.ViewHolder, ConversationResponse>() {


    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_conversation_list }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ConversationListingAdapter.ViewHolder {
        return ViewHolder(
            ItemConversationListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemConversationListBinding) :
        BaseViewHolder<ConversationResponse>(binding.root) {
        override fun onBind(item: ConversationResponse, position: Int) {
            binding.apply {
                val langData=ApplicationClass.mGlobalData
                if (item.bookingMessage != null) {
                    tvStatus.text = if(item.bookingStatusId == Enums.AppointmentStatusType.CANCEL.key)
                        langData?.chatScreen?.cancelled
                    else if(item.bookingStatusId == Enums.AppointmentStatusType.COMPLETE.key ||item.bookingMessage?.sessionStatus == Enums.SessionStatuses.ENDED.key)
                        langData?.chatScreen?.sessionComplete
                    else{
                        TimeAgo.timeLeft(
                            item.bookingMessage?.timeLeft,
                            langData?.chatScreen
                        )
                    }
                    tvStatus.visibility = if (item.bookingMessage?.timeLeft.isNullOrEmpty()) View.GONE else View.VISIBLE
                    val locale=  TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
                    if(locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)
                        tvStatus.textDirection=4
                    else

                        tvStatus.textDirection=3
                }

                var time: String? = null
                time = if (item.chatProperties?.lastMessageTime.isNullOrEmpty().not())
                    getDate(item.chatProperties?.lastMessageTime,item.chatProperties?.timeLocale)
                else
                    getDate(
                        item.partnerChatProperties?.lastMessageTime,
                        item.partnerChatProperties?.timeLocale
                    )
                tvTime.text = time
                var bookedByText = SpannableStringBuilder().append(langData?.globalString?.bookedBy)
                var userName = SpannableStringBuilder().append(item.customerUser?.fullName)
                var userProfile = item.bookedForUser?.userProfilePicture?.file
                if (plannerMode) {
                    userProfile = item.partnerUser?.userProfilePicture?.file
                    val stringBuilder =
                        SpannableStringBuilder().bold { append(item.bookedForUser?.fullName) }
                    userName = stringBuilder
                    bookedByText = SpannableStringBuilder().append(langData?.globalString?.bookedFor)
                }
                if (item.customerUser?.id == item.bookedForUser?.id) {
                    if (plannerMode)
                        bookedByText.append(" ").bold { append(langData?.globalString?.self.getSafe()) }
                    else bookedByText.append(" ").append(langData?.globalString?.self.getSafe())
                } else
                    bookedByText.append(" ").append(userName)
                if (item.chatProperties != null)
                    ivOnlineStatus.setVisible(item.chatProperties?.unreadMessagesCount.getSafe() < 0)
                else
                    ivOnlineStatus.setVisible(item.partnerChatProperties?.unreadMessagesCount.getSafe() < 0)

                if (plannerMode) {
                    tvName.text = item.partnerUser?.fullName
                    tvBookedBy.text = bookedByText
                    tvBookedBy.visible()
                 var specialities=""
                    item.partnerUser?.partnerSpecialities?.forEach {
                        specialities+=it.genericItemName+","
                    }

                    if(specialities.isNotEmpty())
                        specialities=specialities.substring(0,specialities.length-1)
                    tvSpeciality.text =specialities

                } else {
                    tvName.text = item.bookedForUser?.fullName
                    tvSpeciality.text = bookedByText
                    tvBookedBy.gone()
                }
                ivDrImage.loadImage(
                    userProfile,
                    getGenderIcon(item.bookedForUser?.genderId.toString())
                )
                executePendingBindings()
            }
        }

        private fun getDate(lastMessageTime: String?, timeLocale: String?): String? {
            var date = ""
            val givenDate = getDateInFormat(
                lastMessageTime.toString().trim(),
                "yyyy-MM-dd hh:mm:ss",
                "yyyy-MM-dd",
                timeLocale
            )
            val currentDate = getCurrentDateTime("yyyy-MM-dd")
            var timeFormat = "hh:mm aa"
            if (DateFormat.is24HourFormat(binding.root.context)) {
                timeFormat = "HH:mm"
            }
            var currentFormat="yyyy-MM-dd hh:mm:ss"
            date = if (currentDate == givenDate) {
                if (timeLocale==null){
                    currentFormat="yyyy-MM-dd hh:mm:ss aa"
                    if (DateFormat.is24HourFormat(binding.root.context))
                        currentFormat="yyyy-MM-dd HH:mm:ss"

                }
                getDateInFormat(
                    lastMessageTime.toString(),
                    currentFormat  ,
                    timeFormat,
                    timeLocale
                )
            }else
                getDateInFormat(
                    lastMessageTime.toString(),
                    currentFormat,
                    "dd MMM yyyy",
                    timeLocale
                )

            return date
        }
    }
}