package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemDemoListBinding
import com.homemedics.app.databinding.ItemHomeSliderBinding
import com.homemedics.app.model.DemoModel
import com.homemedics.app.utils.setImage

class HomeSliderPagerAdapter(): BaseRecyclerViewAdapter<HomeSliderPagerAdapter.ViewHolder, String>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_home_slider }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeSliderBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemHomeSliderBinding) : BaseViewHolder<String>(binding.root) {
        override fun onBind(item: String, position: Int) {
            binding.apply {
                setImage(ivThumbnail, item)
            }
        }
    }
}