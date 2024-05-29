package com.homemedics.app.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemCheckoutSplitAmountBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.setVisible
import com.homemedics.app.utils.visible

class CheckoutSplitAmountAdapter : BaseRecyclerViewAdapter<CheckoutSplitAmountAdapter.ViewHolder, SplitAmount>() {

    var currency: String? = null

    var lang: RemoteConfigLanguage? = null

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_checkout_split_amount }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCheckoutSplitAmountBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemCheckoutSplitAmountBinding) : BaseViewHolder<SplitAmount>(binding.root) {
        override fun onBind(item: SplitAmount, position: Int) {
            val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
            lang = ApplicationClass.mGlobalData
            binding.apply {
                if (item.isAvailable == 0 && item.isSubstitute == 0) {
                    tvTitle.apply {
                        text = item.specialityName
                        setTextColor(resources.getColor(R.color.grey, context.theme))
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                    tvAmount.apply {
                        text = if (currency != null) {
                            if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.fee} $currency" else "$currency ${item.fee}"
                        } else {
                            item.fee
                        }
                        setTextColor(resources.getColor(R.color.grey, context.theme))
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                } else {
                    tvTitle.text = item.specialityName
                    tvAmount.text = if (currency != null) {
                        if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.fee} $currency" else "$currency ${item.fee}"
                    }
                    else {
                        item.fee
                    }
                }

                if (item.isAvailable == 0 && item.isSubstitute == 1) {
                    tvAmount.apply {
                        setTextColor(resources.getColor(R.color.call_red, context.theme))
                    }
                    tvAlternate.apply {
                        visible()
                        text = lang?.globalString?.alternateAvailable.getSafe()
                    }
                }

            }
        }
    }

}