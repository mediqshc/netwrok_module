package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemPharmacyStartsBinding
import com.homemedics.app.utils.getSafe

class PharmacyServiceAdapter : BaseRecyclerViewAdapter<PharmacyServiceAdapter.ViewHolder, GenericItem>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_pharmacy_starts }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPharmacyStartsBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemPharmacyStartsBinding) :
        BaseViewHolder<GenericItem>(binding.root) {
        override fun onBind(item: GenericItem, position: Int) {
            binding.apply {
                tvDesc.maxLines = 20 //no limit
                multipleViewItem = item
                ivIcon.setImageResource(item.drawable.getSafe())
                executePendingBindings()
            }
        }
    }
}