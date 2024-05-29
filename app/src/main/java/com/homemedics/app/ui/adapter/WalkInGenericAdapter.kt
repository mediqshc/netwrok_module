package com.homemedics.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.walkinpharmacy.WalkInItemResponse
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemWalkinGenericBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.setVisible

class WalkInGenericAdapter : BaseRecyclerViewAdapter<WalkInGenericAdapter.ViewHolder, WalkInItemResponse>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_walkin_generic}
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWalkinGenericBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemWalkinGenericBinding) :
        BaseViewHolder<WalkInItemResponse>(binding.root) {
        @SuppressLint("SetTextI18n")
        override fun onBind(item: WalkInItemResponse, position: Int) {
            binding.apply {
                tvDistance.apply {
                    val km = item.kilometer
                    text = "${"%.2f".format(km)} ${ApplicationClass.mGlobalData?.globalString?.kilometer.getSafe()}"
                    setVisible(km != null && km != 0.0)
                }
                multipleViewItem = item
                executePendingBindings()
            }
        }
    }
}