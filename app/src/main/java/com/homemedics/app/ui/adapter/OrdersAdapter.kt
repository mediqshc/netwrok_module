package com.homemedics.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.meta.PartnerService
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemAppointmentsBinding
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*

class OrdersAdapter : BaseRecyclerViewAdapter<OrdersAdapter.ViewHolder, OrderResponse>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_appointments }
        set(value) {}

    fun setListItems(list: List<OrderResponse>?){
        list?.let {
            val diffCallback = OrdersListDiffCallback(listItems, list)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            listItems.clear()
            listItems.addAll(list)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAppointmentsBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemAppointmentsBinding) : BaseViewHolder<OrderResponse>(binding.root) {
        private var orderStatuses: List<GenericItem>? = null
        private var claimStatuses: List<GenericItem>? = null
        private var claimCategories: List<GenericItem>? = null
        private var services: List<PartnerService>? = null

        init {
            orderStatuses = DataCenter.getMeta()?.orderStatuses
            claimStatuses = DataCenter.getMeta()?.claimWalkInStatuses
            claimCategories = DataCenter.getMeta()?.claimCategories
            services = DataCenter.getMeta()?.partnerServiceType
        }

        @SuppressLint("SetTextI18n")
        override fun onBind(item: OrderResponse, position: Int) {
            val lang = ApplicationClass.mGlobalData
            val self = lang?.globalString?.self.getSafe()
            val start = "\u2066"
            val end = "\u2069"
            val hash = "\u0023"
            binding.apply {
                val isClaimOrder = item.partnerServiceId == CustomServiceTypeView.ServiceType.Claim.id
                ivWalkIn.gone()

                val statusList = when(item.partnerServiceId){
                    CustomServiceTypeView.ServiceType.Claim.id,
                    CustomServiceTypeView.ServiceType.WalkInPharmacy.id,
                    CustomServiceTypeView.ServiceType.WalkInLaboratory.id,
                    CustomServiceTypeView.ServiceType.WalkInHospital.id -> claimStatuses
                    else -> orderStatuses
                }
                val status = getLabelsFromId(
                    item.bookingStatusId.getSafe().toString(),
                    statusList
                ).firstCap()

                val icon = CustomServiceTypeView.ServiceType.getServiceById(item.partnerServiceId.getSafe())?.icon.getSafe()

                val medicalStaffSpecialties = DataCenter.getMeta()?.specialties?.medicalStaffSpecialties
                val partnerServices = DataCenter.getMeta()?.partnerServiceType?.find { it.id == item.partnerServiceId }?.shortName

                when (item.partnerServiceId) {
                    CustomServiceTypeView.ServiceType.HealthCare.id -> {
                        tvTitle.text = getLabelFromId(item.specialityId.toString(), medicalStaffSpecialties)
                    }
                    CustomServiceTypeView.ServiceType.PharmacyService.id -> {
                        tvTitle.text = partnerServices.getSafe()
                    }
                    CustomServiceTypeView.ServiceType.LaboratoryService.id -> {
                        tvTitle.text = partnerServices.getSafe()
                    }
                    CustomServiceTypeView.ServiceType.Claim.id -> {
                        val category = claimCategories?.find { it.genericItemId == item.claim?.claimCategoryId}
                        val title = if(category != null) "${category.genericItemName} ${lang?.claimScreen?.claim}" else lang?.claimScreen?.claim
                        tvTitle.text = title
                    }
                    CustomServiceTypeView.ServiceType.WalkInPharmacy.id,
                    CustomServiceTypeView.ServiceType.WalkInLaboratory.id,
                    CustomServiceTypeView.ServiceType.WalkInHospital.id -> {
                        val service = services?.find { it.id == item.partnerServiceId}
                        val title = service?.shortName
                        tvTitle.text = title
                        ivWalkIn.visible()
                    }
                    else -> {
                        tvTitle.text = item.partnerUser?.fullName
                    }
                }

                val bookForName = if(item.bookedForUser?.id == DataCenter.getUser()?.id) self
                    else item.bookedForUser?.fullName

                if (item.bookingDate != null) {
                    tvDesc.text = "$start$hash ${item.uniqueIdentificationNumber}$end | $start${item.bookingDate?.let { bookingDate ->
                        getDateInFormat(
                            bookingDate, "yyyy-MM-dd hh:mm:ss", "dd MMM yyyy")
                    }}$end | $bookForName"
                } else {
                    tvDesc.text = "$start$hash ${item.uniqueIdentificationNumber}$end | $bookForName"
                }
                tvStatus.text = status
                ivIcon.setImageResource(icon)

                when(item.bookingStatusId.getSafe()) {
                    Enums.AppointmentStatusType.CONFIRMATIONPENDING.key -> {
                        ivDot.setVisible(true)
                    }
                    Enums.AppointmentStatusType.RESCHEDULED.key -> {
                        ivDot.setVisible(true)
                    }
                    Enums.AppointmentStatusType.RESCHEDULING.key -> {
                        ivDot.setVisible(true)
                    }
                    else -> {
                        ivDot.setVisible(false)
                    }
                }
            }
        }
    }

    class OrdersListDiffCallback(
        val oldList: List<OrderResponse>?,
        val newList: List<OrderResponse>?
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList?.size.getSafe()
        }

        override fun getNewListSize(): Int {
            return newList?.size.getSafe()
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList?.get(oldItemPosition)?.id.getSafe() == newList?.get(newItemPosition)?.id.getSafe()
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList?.get(oldItemPosition)?.uniqueIdentificationNumber.getSafe() == newList?.get(newItemPosition)?.uniqueIdentificationNumber.getSafe()
        }
    }
}