package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemSpecialityConsultationBinding
import com.homemedics.app.utils.getSafe

class MedicalRecordTypeAdapter : BaseRecyclerViewAdapter<MedicalRecordTypeAdapter.ViewHolder, GenericItem>()  {

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

    inner class ViewHolder(var binding: ItemSpecialityConsultationBinding) : BaseViewHolder<GenericItem>(binding.root) {
        override fun onBind(item: GenericItem, position: Int) {
            binding.apply {
                tvTitle.text = item.genericItemName
                ivIcon.setImageResource(item.drawable.getSafe())
            }
        }
    }

}