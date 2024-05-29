package com.homemedics.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.models.response.notification.Notification
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemNotificationBinding
import com.homemedics.app.utils.*

class NotificationsAdapter :
    BaseRecyclerViewAdapter<NotificationsAdapter.ViewHolder, Notification>() {
    val metaData: MetaDataResponse? by lazy {
        TinyDB.instance.getObject(
            com.fatron.network_module.utils.Enums.TinyDBKeys.META.key,
            MetaDataResponse::class.java
        ) as MetaDataResponse?
    }
    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_notification }
        set(value) {}

    fun setListItems(list: List<Notification>?) {
        list?.let {
            val diffCallback = NotiListDiffCallback(listItems, list)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            listItems.clear()
            listItems.addAll(list)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }


    inner class ViewHolder(var binding: ItemNotificationBinding) :
        BaseViewHolder<Notification>(binding.root) {


        @SuppressLint("SetTextI18n")
        override fun onBind(item: Notification, position: Int) {
            binding.apply {
                dataManager = item
                tvDate.text =
                    getDateInFormat(item.createdAt.getSafe(), "yyyy-MM-dd HH:mm:ss", "dd MMM yyyy")
                var drawables = R.drawable.ic_notifications
                when (item.categoryID) {
                   2//chat
                    -> drawables = R.drawable.ic_chat
                    3//promo
                    -> drawables = R.drawable.ic_promo
                    4//order
                    -> drawables = R.drawable.ic_order
                }
                ivIcon.setImageResource(drawables)
                if (item.isRead == "0") {
                    ivDot.visible()
                    view9.visible()
                } else {
                    ivDot.gone()
                    view9.gone()
                }
                executePendingBindings()
            }
        }
    }

    class NotiListDiffCallback(
        val oldList: List<Notification>?,
        val newList: List<Notification>?
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList?.size.getSafe()
        }

        override fun getNewListSize(): Int {
            return newList?.size.getSafe()
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList?.get(oldItemPosition)?.id.getSafe() == newList?.get(newItemPosition)?.id.getSafe() && oldList?.get(
                oldItemPosition
            )?.isRead.getSafe() == newList?.get(newItemPosition)?.isRead.getSafe()
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList?.get(oldItemPosition)?.id.getSafe() == newList?.get(newItemPosition)?.id.getSafe() && oldList?.get(
                oldItemPosition
            )?.isRead.getSafe() == newList?.get(newItemPosition)?.isRead.getSafe()
        }
    }

}