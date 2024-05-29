package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.planschedule.SlotByType
import com.fatron.network_module.models.response.planschedule.WeeklySlots
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemTimeSlotByWeekBinding
import com.homemedics.app.utils.RecyclerViewItemDecorator

class WeeklyTimeSlotsAdapter: BaseRecyclerViewAdapter<WeeklyTimeSlotsAdapter.ViewHolder, WeeklySlots>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_time_slot_by_week }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTimeSlotByWeekBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onSlotChipDelete: ((item: SlotByType, position: Int, weeklySlot: WeeklySlots)-> Unit)? = null

    inner class ViewHolder(var binding: ItemTimeSlotByWeekBinding) : BaseViewHolder<WeeklySlots>(binding.root) {
        override fun onBind(item: WeeklySlots, position: Int) {
            val slotsAdapter = TimeSlotsAdapter()
            binding.apply {
                slotsAdapter.onSlotChipDelete = { slotByTypeItem, position ->
                    onSlotChipDelete?.invoke(slotByTypeItem, position, item)
                }

                tvDay.text = item.title
                rvSlots.adapter = slotsAdapter
                slotsAdapter.listItems = item.slots as ArrayList<SlotByType>
            }
        }
    }
}