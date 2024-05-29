package com.homemedics.app.ui.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.appointments.AppointmentResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemAppointmentsBinding
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*

class AppointmentsAdapter(val type: Enums.AppointmentType) : BaseRecyclerViewAdapter<AppointmentsAdapter.ViewHolder, AppointmentResponse>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_appointments }
        set(value) {}
//    var appointmentListItem: ArrayList<AppointmentResponse> = ArrayList()
//    fun updateListItems(appointment: List<AppointmentResponse?>?) {
//        val diffCallback = DiffUtilsCallback(appointmentListItem,
//            appointment as List<AppointmentResponse>?
//        )
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//        appointmentListItem.clear()
//        appointmentListItem.addAll(appointment as ArrayList<AppointmentResponse>)
//        listItems=appointmentListItem
//        diffResult.dispatchUpdatesTo(this)
//    }

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAppointmentsBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemAppointmentsBinding) : BaseViewHolder<AppointmentResponse>(binding.root) {
        private var orderStatuses: List<GenericItem>? = null

        init {
            orderStatuses = DataCenter.getMeta()?.orderStatuses
        }

        override fun onBind(item: AppointmentResponse, position: Int) {
            binding.apply {
                val date = getDateInFormat(item.bookingDate.getSafe(), getString(R.string.apiDateTimeFormat), getString(R.string.dateFormat2))
                var time = ""
                var timeFormat = "hh:mm aa"
                if (DateFormat.is24HourFormat(binding.root.context)) {
                    timeFormat = "HH:mm"
                }
                val currentFormat = getString(R.string.timeFormat24)

                if(item.partnerServiceId == CustomServiceTypeView.ServiceType.HomeVisit.id){
                    time = item.shift?.shift.getSafe()
                }
                else {
                    if(item.startTime.isNullOrEmpty().not())
                        time = getDateInFormat(
                            StringBuilder(item.startTime.getSafe()).insert(2, ":").toString(),
                            currentFormat,
                            timeFormat
                        )
                }
                val desc = "# ${item.uniqueIdentificationNumber} | $date | $time"
                val status = getLabelFromId(item.bookingStatusId.getSafe().toString(), orderStatuses).firstCap()
                val icon = CustomServiceTypeView.ServiceType.getServiceById(item.partnerServiceId.getSafe())?.icon.getSafe()

                tvTitle.text = item.fullName
                tvDesc.text = desc
                tvStatus.text = status
                ivIcon.setImageResource(icon)

                ivDot.setVisible(item.read.getBoolean().not())
            }
        }
    }
}


class  DiffUtilsCallback (
    oldList: List<AppointmentResponse>?,
    newList: List<AppointmentResponse>?
) : DiffUtil.Callback() {
    private var mOldList: List<AppointmentResponse>? = oldList
    private var mNewList: List<AppointmentResponse>? = newList

    override fun getOldListSize(): Int {
        return mOldList?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return mNewList?.size ?: 0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldList?.get(oldItemPosition)?.uniqueIdentificationNumber === mNewList?.get(newItemPosition)?.uniqueIdentificationNumber
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (mOldList?.get(oldItemPosition)?.uniqueIdentificationNumber == mNewList?.get(
            newItemPosition
        )?.uniqueIdentificationNumber) && (mOldList?.get(oldItemPosition)?.partnerServiceId == mNewList?.get(
            newItemPosition
        )?.partnerServiceId)
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}