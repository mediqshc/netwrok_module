package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.emr.type.EmrVital
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemBorderRoundedTwoTextViewsBinding
import com.homemedics.app.utils.DataCenter

class EMRVitalItemsAdapter : BaseRecyclerViewAdapter<EMRVitalItemsAdapter.ViewHolder, EmrVital>()  {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_border_rounded_two_text_views }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBorderRoundedTwoTextViewsBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemBorderRoundedTwoTextViewsBinding) : BaseViewHolder<EmrVital>(binding.root) {
        override fun onBind(item: EmrVital, position: Int) {
            binding.apply {
                tvTitle.text = item.value
                tvDesc.text = DataCenter.getMeta()?.emrVitals?.find { it.name == item.key }?.short_name
            }
        }
    }
}