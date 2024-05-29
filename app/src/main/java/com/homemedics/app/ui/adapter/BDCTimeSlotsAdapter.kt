package com.homemedics.app.ui.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.partnerprofile.TimeSlot
import com.fatron.network_module.models.response.planschedule.SlotByType
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemBorderedTextviewLgBinding
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.getAppContext
import com.homemedics.app.utils.getDateInFormat
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.getString

class BDCTimeSlotsAdapter(val serviceType: CustomServiceTypeView.ServiceType?): BaseRecyclerViewAdapter<BDCTimeSlotsAdapter.ViewHolder, TimeSlot>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_bordered_textview_lg }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBorderedTextviewLgBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onTimeSlotSelected: ((item: TimeSlot, position: Int)->Unit)? = null

    inner class ViewHolder(var binding: ItemBorderedTextviewLgBinding) : BaseViewHolder<TimeSlot>(binding.root) {
        override fun onBind(item: TimeSlot, position: Int) {
            val currentFormat = getString(R.string.timeFormat24)
            val timeFormat = if (DateFormat.is24HourFormat(getAppContext()))  getString(R.string.timeFormat24) else  getString(R.string.timeFormat12)


            binding.apply {
                tvTitle.text =
                    if(serviceType == CustomServiceTypeView.ServiceType.HomeVisit) item.shift
                    else if(item.isAvailableNowSlot){
                        item.start
                    }
                    else if(item.start.isNullOrEmpty().not()) getDateInFormat(
                        StringBuilder(item.start.getSafe()).insert(2, ":").toString(),
                        currentFormat,
                        timeFormat
                    )
                    else item.start

                tvTitle.isChecked = item.isChecked

                tvTitle.setOnClickListener {
                    listItems.forEach {
                        it.isChecked = false
                    }
                    listItems[position].isChecked = true

                    notifyDataSetChanged()
                    onTimeSlotSelected?.invoke(item, position)
                }
            }
        }
    }
}