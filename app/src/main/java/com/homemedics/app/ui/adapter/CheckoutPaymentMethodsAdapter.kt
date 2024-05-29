package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.meta.PaymentMethod
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemCheckoutPaymentOptionsBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.loadImage

class CheckoutPaymentMethodsAdapter : BaseRecyclerViewAdapter<CheckoutPaymentMethodsAdapter.ViewHolder, GenericItem>() {


    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_checkout_payment_options }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCheckoutPaymentOptionsBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemCheckoutPaymentOptionsBinding) : BaseViewHolder<GenericItem>(binding.root) {
        override fun onBind(item: GenericItem, position: Int) {
            binding.apply {
                tvTitle.text = item.genericItemName
                ivIcon.loadImage(item.icon_url, R.drawable.ic_placeholder)
                radio.isChecked = item.isChecked.getSafe()
            }
        }
    }

    private fun getSelectedItem(): GenericItem?{
        return listItems.firstOrNull { it.isChecked.getSafe() }
    }

}