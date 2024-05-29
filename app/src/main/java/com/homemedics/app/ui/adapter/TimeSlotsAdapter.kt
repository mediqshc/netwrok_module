package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.planschedule.SlotByType
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemTimeSlotBinding
import com.homemedics.app.databinding.ItemTimeSlotChipBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.getAppContext

class TimeSlotsAdapter: BaseRecyclerViewAdapter<TimeSlotsAdapter.ViewHolder, SlotByType>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_time_slot }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTimeSlotBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onSlotChipDelete: ((item: SlotByType, position: Int)-> Unit)? = null

    inner class ViewHolder(var binding: ItemTimeSlotBinding) : BaseViewHolder<SlotByType>(binding.root) {
        override fun onBind(item: SlotByType, position: Int) {
            binding.apply {
                item.drawable?.let { ivIcon.setImageResource(it) }
                item.slots?.forEachIndexed {index,it->
                    val itemView = ItemTimeSlotChipBinding.inflate(LayoutInflater.from(getAppContext()))
                    itemView.tvTitle.text = it.title
                    itemView.ivDelete.setOnClickListener {
                        onSlotChipDelete?.invoke(item, index)
                    }
                  val locale=  TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
                    if(locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)
                        llSlots.isRtl=true
                    llSlots.addView(itemView.root)
                }
            }
        }
    }
}