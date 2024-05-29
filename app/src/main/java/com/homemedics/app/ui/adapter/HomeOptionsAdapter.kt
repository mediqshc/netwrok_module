package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fatron.network_module.models.response.meta.HomeMenuItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemHomeOptionsBinding
import com.homemedics.app.utils.getSafe

class HomeOptionsAdapter: BaseRecyclerViewAdapter<HomeOptionsAdapter.ViewHolder, HomeMenuItem>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_home_options }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeOptionsBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemHomeOptionsBinding) : BaseViewHolder<HomeMenuItem>(binding.root) {
        override fun onBind(item: HomeMenuItem, position: Int) {
            binding.apply {
                tvTitle.text = item.name
                tvDesc.text = item.description
                ivIcon.setImageResource(item.icon.getSafe())

                if(item.isEnabled.not()){
                    tvTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey))
                    tvDesc.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey))
                    ivIcon.alpha = 0.5f
                }
            }
        }
    }
}