package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.walkin.services.WalkInService
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemSpecialityConsultationBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.loadImage

class WalkInServiceAdapter : BaseRecyclerViewAdapter< WalkInServiceAdapter.ViewHolder, WalkInService>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_speciality_consultation }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSpecialityConsultationBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemSpecialityConsultationBinding) : BaseViewHolder<WalkInService>(binding.root) {
        override fun onBind(item: WalkInService, position: Int) {
            binding.apply {
                tvTitle.text = item.services?.genericItemName.getSafe()
                ivIcon.loadImage(item.services?.icon_url.getSafe(), R.drawable.ic_emergency)
            }
        }
    }

}