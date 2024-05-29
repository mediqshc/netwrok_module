package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.linkaccount.DependentResponse
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemDependentsBinding

class DependentAdapter   : BaseRecyclerViewAdapter<DependentAdapter.ViewHolder, DependentResponse>(){


    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_doctor }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): DependentAdapter.ViewHolder {
        return ViewHolder(
            ItemDependentsBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemDependentsBinding) : BaseViewHolder<DependentResponse>(binding.root) {
        override fun onBind(item: DependentResponse, position: Int) {
            binding.apply {
                data = item
            }
        }
    }
}