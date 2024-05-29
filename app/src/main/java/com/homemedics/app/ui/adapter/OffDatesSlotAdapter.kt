package com.homemedics.app.ui.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.offdates.OffDate
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemOffDatesBinding
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*

class OffDatesSlotAdapter : BaseRecyclerViewAdapter<OffDatesSlotAdapter.ViewHolder, OffDate>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_off_dates }
        set(value) {}

    var itemDeleteListener: ((OffDate,  position: Int) -> Unit)? = null

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemOffDatesBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemOffDatesBinding) : BaseViewHolder<OffDate>(binding.root) {
        private var currentFormat: String
        private var format: String

        init {
            val use24HourClock: Boolean = DateFormat.is24HourFormat(getAppContext())
            currentFormat = "yyyy-MM-dd HH:mm:ss"
            val format12Hours = "dd/MMM/yy hh:mm a"
            val format24Hours = "dd/MMM/yy HH:mm"
            format = if(use24HourClock) format24Hours else format12Hours
        }

        override fun onBind(item: OffDate, position: Int) {
            binding.apply {
                langData=ApplicationClass.mGlobalData
                tvVideoCall.setVisible(false)
                tvHomeVisit.setVisible(false)
                tvMessage.setVisible(false)

                val startDate = getDateInFormat(
                    item.startDate.toString(),
                    currentFormat,
                    format
                )
                val endDate = getDateInFormat(
                    item.endDate.toString(),
                    currentFormat,
                    format
                )
                tvDateTime.text = startDate
                tvDateTimeEnd.text = endDate

                if(DataCenter.getUser().isMedicalStaff())
                    llSlots.setVisible(false)
                else{
                    item.servicesIds?.forEach { i ->
                        when(i){
                            CustomServiceTypeView.ServiceType.VideoCall.id -> tvVideoCall.setVisible(true)
                            CustomServiceTypeView.ServiceType.HomeVisit.id -> tvHomeVisit.setVisible(true)
                            CustomServiceTypeView.ServiceType.Message.id -> tvMessage.setVisible(true)
                        }
                    }
                }

                ivDelete.setOnClickListener {
                    itemDeleteListener?.invoke(item, position)
                }
            }
        }
    }
}