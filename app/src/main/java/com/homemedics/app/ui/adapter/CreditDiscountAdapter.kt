package com.homemedics.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.linkaccount.CompaniesDiscounts
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemDiscountsBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*

class CreditDiscountAdapter  : BaseRecyclerViewAdapter<CreditDiscountAdapter.ViewHolder, CompaniesDiscounts>(){

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_discounts }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): CreditDiscountAdapter.ViewHolder {
        return ViewHolder(
            ItemDiscountsBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemDiscountsBinding) : BaseViewHolder<CompaniesDiscounts>(binding.root) {
        @SuppressLint("SetTextI18n")
        override fun onBind(item: CompaniesDiscounts, position: Int) {
            val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
            val currency = DataCenter.getMeta()?.currencies?.find { it.genericItemId == item.currencyId }?.genericItemName.getSafe()
            val off = ApplicationClass.mGlobalData?.globalString?.off.getSafe()
            val on = ApplicationClass.mGlobalData?.globalString?.on.getSafe()
            binding.apply {
                tvItem.text = if (item.discountType?.contains("Free", true).getSafe())
                    "${item.discountType} ${item.name.getSafe()}"
                else if (item.discountType?.contains("Percentage", true).getSafe())
                    "${Constants.START}${item.value.getSafe()}% $on ${item.name.getSafe()}${Constants.END}"
                else {
                    if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.name.getSafe()} $off ${item.value.getSafe()} $currency" else "$currency ${item.value.getSafe()} $off ${item.name.getSafe()}"
                }

                tvDesc.apply {
                    setVisible(item.promoCode.isNullOrEmpty().not())
                    text = "${ApplicationClass.mGlobalData?.globalString?.promoCode.getSafe()} ${item.promoCode.getSafe()}"
                }
                tvCondition.apply {
                    setVisible(item.conditionsApply.getBoolean())
                    text = ApplicationClass.mGlobalData?.globalString?.conditionsApply.getSafe()
                }
            }
        }
    }
}