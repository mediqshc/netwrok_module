package com.homemedics.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.checkout.Promotions
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemCheckoutSplitAmountBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.CheckoutViewModel

class PromotionsAdapter : BaseRecyclerViewAdapter<PromotionsAdapter.ViewHolder, Promotions>() {

    var deleteClick: ((item: Promotions) -> Unit)? = null
    var deleteClickPackage: ((item: Promotions) -> Unit)? = null
    var isConfirmation = false
    var currency: String? = null

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_checkout_split_amount }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): PromotionsAdapter.ViewHolder {
        return ViewHolder(
            ItemCheckoutSplitAmountBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemCheckoutSplitAmountBinding) : BaseViewHolder<Promotions>(binding.root) {
        @SuppressLint("SetTextI18n")
        override fun onBind(item: Promotions, position: Int) {
            val locale = TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
            val promo = ApplicationClass.mGlobalData?.globalString?.promo.getSafe()
            binding.apply {
                tvTitle.text =
                    if (item.isPromotion.getSafe()) "$promo ${Constants.MINUS} ${item.promotionName.getSafe()}" else item.promotionName.getSafe()
                if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) {
                    tvAmount.text =
                        if (currency != null) "${item.discountValue.getSafe()} $currency" else item.discountValue.getSafe()
                } else {

                    tvAmount.text =
                        if (currency != null) "$currency ${item.discountValue.getSafe()}" else item.discountValue.getSafe()
                }


      /*          if (!discountvalue) {
                    //to remove PKR from amount
                    amount = tvAmount.text.toString().getCommaRemoved().replace(Regex("[^\\d.]"),"").parseDouble().getSafe()
                    tvAmount.text = amount.toString()
                    discountvalue = true
                } else {
                    amount1 = tvAmount.text.toString().getCommaRemoved().replace("[^\\d.]","").parseDouble().getSafe()
                    try {

                        val result = amount1 - amount
                        tvAmount.text = result.toString().getCommaRemoved().getSafe()
                        discountvalue = false
                        println("Converted value using toDouble(): $amount1")

                    } catch (e: NumberFormatException) {
                        println("Conversion to double failed: ${e.message}")
                    }
                }
*/

                ivDelete.apply {
                    if (isConfirmation) {
                        gone()
                    } else if (item.isPromotion?.not().getSafe()) {
                        visible()
                    } else {
//                        visible()
                        setVisible(item.promotionPromocode.isNullOrEmpty().not())
                    }
//                    if (isConfirmation) {
//                        gone()
//                    } else if (item.isPromotion?.not().getSafe()) {
//                        visible()
//                    } else {
//                        setVisible(item.promotionPromocode.isNullOrEmpty().not())
//                    }
                    setOnClickListener {
                        if (item.isPromotion.getSafe())
                            deleteClick?.invoke(item)
                        else
                            deleteClickPackage?.invoke(item)
                    }
                }
            }
        }
    }
}