package com.homemedics.app.ui.adapter

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.models.response.orders.ScheduledDutyResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemVisitScheduleBinding
import com.homemedics.app.utils.*

class VisitSchedulesAdapter(): BaseRecyclerViewAdapter<VisitSchedulesAdapter.ViewHolder, ScheduledDutyResponse>() {

    val metaData: MetaDataResponse? by lazy {
        TinyDB.instance.getObject(
            Enums.TinyDBKeys.META.key,
            MetaDataResponse::class.java
        ) as MetaDataResponse?
    }

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_visit_schedule }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemVisitScheduleBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemVisitScheduleBinding) : BaseViewHolder<ScheduledDutyResponse>(binding.root) {
        @SuppressLint("SetTextI18n")
        override fun onBind(item: ScheduledDutyResponse, position: Int) {
            val currentFormat = getString(R.string.timeFormat24)
            val timeFormat = if (DateFormat.is24HourFormat(getAppContext()))  getString(R.string.timeFormat24) else  getString(R.string.timeFormat12)
            val deviceLocaleStartTime = getDateInFormat(
                StringBuilder(item.startTime.getSafe()).toString(),
                currentFormat,
                timeFormat
            )
            val deviceLocaleEndTime = getDateInFormat(
                StringBuilder(item.endTime.getSafe()).toString(),
                currentFormat,
                timeFormat
            )

            binding.apply {
                val dutyStatus = metaData?.dutyStatuses?.find { it.genericItemId == item.statusId }?.label

                tvTitle.text = item.partnerUser?.fullName
                tvDesc.text = "${getDateInFormat(item.date.getSafe(), "yyyy-MM-dd", "dd MMMM yyyy")} ${Constants.PIPE} ${Constants.START}$deviceLocaleStartTime${Constants.END} - ${Constants.START}$deviceLocaleEndTime${Constants.END}"
                ivIcon.loadImage(item.partnerUser?.userProfilePicture?.file, getGenderIcon(item.partnerUser?.genderId.getSafe().toString()))
                tvStatus.text = dutyStatus
            }
        }
    }
}