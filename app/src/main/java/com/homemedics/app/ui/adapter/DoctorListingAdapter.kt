package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemDoctorBinding
import com.homemedics.app.utils.getSafe

class DoctorListingAdapter : BaseRecyclerViewAdapter<DoctorListingAdapter.ViewHolder, PartnerProfileResponse>(){


    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_doctor }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): DoctorListingAdapter.ViewHolder {
        return ViewHolder(
            ItemDoctorBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemDoctorBinding) : BaseViewHolder<PartnerProfileResponse>(binding.root) {
        override fun onBind(item: PartnerProfileResponse, position: Int) {
            binding.apply {
                partner = item
            }
        }
    }
}