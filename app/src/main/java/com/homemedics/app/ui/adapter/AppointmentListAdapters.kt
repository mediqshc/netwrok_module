package com.homemedics.app.ui.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.response.appointments.AppointmentResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.R
import com.homemedics.app.databinding.ItemAppointmentsBinding
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*

class AppointmentListAdapters(val type: Enums.AppointmentType):ListAdapter<DataItemListing, RecyclerView.ViewHolder>(DiffCallbackList()) {
    var itemClickListener: ((AppointmentResponse,  position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemAppointmentsBinding.inflate(
            layoutInflater,
            parent,
            false
        ) // add binding accordingly

        return     DefaultListingViewHolder(binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val modelProductItem = getItem(position) as DataItemListing.DefaultItemListing

        when (holder) {

            is DefaultListingViewHolder -> {
                holder.bind(
                    modelProductItem.data,position
                )
            }



        }
    }

    private inner class DefaultListingViewHolder(private val binding: ItemAppointmentsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var orderStatuses: List<GenericItem>? = null

        init {
            orderStatuses = DataCenter.getMeta()?.orderStatuses
        }
        fun bind(
            item: AppointmentResponse,
            position: Int,

            ) {
            itemView.setOnClickListener {
                itemClickListener?.let {
                    it(item, position)
                }
            }
                binding.apply {
                    var bookingDate=item.bookingDate.getSafe()
                    if(item.partnerServiceId==CustomServiceTypeView.ServiceType.HealthCare.id){
                        bookingDate="${item.bookingDate.getSafe()} 00:00:00"
                    }
                    val date = getDateInFormat(bookingDate, getString(R.string.apiDateTimeFormat), getString(
                        R.string.dateFormat2)
                    )
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
                        if(item.startTime.isNullOrEmpty().not()){
                            var startTime=if(item.startTime?.contains(":").getSafe())item.startTime else StringBuilder(item.startTime.getSafe()).insert(2, ":").toString()
                            time = getDateInFormat(
                                startTime.getSafe(),
                                currentFormat,
                                timeFormat
                            )
                    }}
                    val desc =  "${Constants.START}${Constants.HASH} ${item.uniqueIdentificationNumber}${Constants.END}  ${Constants.PIPE} $date ${Constants.PIPE} ${Constants.START}$time${Constants.END} "
                    val status = getLabelsFromId(item.bookingStatusId.getSafe().toString(), orderStatuses).firstCap()
                    val icon = CustomServiceTypeView.ServiceType.getServiceById(item.partnerServiceId.getSafe())?.icon.getSafe()
                    val dutyStatus = DataCenter.getMeta()?.dutyStatuses?.find { it.genericItemId == item.bookingStatusId }?.label

                    tvTitle.text = item.fullName
                    tvDesc.text = desc
//                    tvDesc.textDirection=3
//
//                    tvDesc.layoutDirection= View.LAYOUT_DIRECTION_RTL
//                    tvStatus.text = status
                    ivIcon.setImageResource(icon)

                    tvStatus.text = if (DataCenter.getUser().isDoctor()) status else dutyStatus

                    ivDot.setVisible(item.read.getBoolean().not())
                    executePendingBindings()

                }


        }

    }




}


sealed class DataItemListing {

    data class DefaultItemListing(val data: AppointmentResponse) : //Here data will be custom objectz
        DataItemListing() {
        override val id = data.uniqueIdentificationNumber.toString()
    }

    abstract val id: String


}

class DiffCallbackList :
    DiffUtil.ItemCallback<DataItemListing>() {

    override fun areItemsTheSame(
        oldItem: DataItemListing,
        newItem: DataItemListing
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: DataItemListing,
        newItem: DataItemListing
    ): Boolean {
        return oldItem == newItem
    }

}
