package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemDemoListBinding
import com.homemedics.app.model.DemoModel

class DemoAdapter: BaseRecyclerViewAdapter<DemoAdapter.ViewHolder, DemoModel>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_demo_list }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDemoListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemDemoListBinding) : BaseViewHolder<DemoModel>(binding.root) {
        override fun onBind(item: DemoModel, position: Int) {
            binding.apply {
                tvTitle.text = item.title
            }
        }
    }
}