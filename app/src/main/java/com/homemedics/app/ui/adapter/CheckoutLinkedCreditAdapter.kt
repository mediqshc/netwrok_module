package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.request.checkout.LinkedCredit
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemCheckoutLinkedCreditsBinding
import com.homemedics.app.utils.loadImage

class CheckoutLinkedCreditAdapter : BaseRecyclerViewAdapter<CheckoutLinkedCreditAdapter.ViewHolder, LinkedCredit>() {


    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_checkout_linked_credits }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCheckoutLinkedCreditsBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemCheckoutLinkedCreditsBinding) : BaseViewHolder<LinkedCredit>(binding.root) {
        override fun onBind(item: LinkedCredit, position: Int) {
            binding.apply {
                tvTitle.text = item.name
                tvAmount.text = item.amount
                ivIcon.loadImage(item.iconUrl, R.drawable.ic_dollar_promo)
            }
        }
    }

}