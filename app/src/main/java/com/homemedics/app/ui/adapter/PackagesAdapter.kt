package com.homemedics.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.homemedics.app.R
import  com.fatron.network_module.models.response.packages.Package
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemPackageBinding
import com.homemedics.app.utils.*

class PackagesAdapter : BaseRecyclerViewAdapter<PackagesAdapter.ViewHolder, Package>() {
    var packageClick: ((item: Package) -> Unit)? = null

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_package }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): PackagesAdapter.ViewHolder {
        return ViewHolder(
            ItemPackageBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemPackageBinding) :
        BaseViewHolder<Package>(binding.root) {
        @SuppressLint("SetTextI18n")
        override fun onBind(item: Package, position: Int) {
            binding.apply {
                tvDays.text = item.valid_days.getSafe().toString()
                tvName.text = item.name.getSafe()
                tvDescription.text = item.description.getSafe()
                tvPrice.text = "PKR "+item.amount.getSafe().toString()

                item.promotion_media?.file?.let { url ->
                    ivBanner.loadImage(url)
                }

                llPackage.apply {
                    setOnTouchListener { _, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                // Handle touch down event
                                true // Consume the event to prevent click event
                            }
                            MotionEvent.ACTION_UP -> {
                                // Handle touch up event
                                packageClick?.invoke(item)
                                true // Consume the event to prevent click event
                            }
                            else -> false // Return false to indicate that you haven't consumed the event
                        }
                    }
                }
            }
        }
    }
}