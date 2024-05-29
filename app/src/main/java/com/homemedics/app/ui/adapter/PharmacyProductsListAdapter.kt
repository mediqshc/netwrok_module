package com.homemedics.app.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.pharmacy.Product
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemPharmaMedicineBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*

class PharmacyProductsListAdapter : BaseRecyclerViewAdapter<PharmacyProductsListAdapter.ViewHolder, Product>() {

    override var layout: (viewType: Int) -> Int
    get() = { R.layout.item_pharma_medicine }
    set(value) {}

    var onActionPLusClick: ((item: Product, position: Int) -> Unit)? = null
    var onActionMinusClick: ((item: Product, position: Int) -> Unit)? = null
    var onWarningClick: (() -> Unit)? = null
    var isShowPrescription = false
    var langData: RemoteConfigLanguage? = null

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPharmaMedicineBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemPharmaMedicineBinding) : BaseViewHolder<Product>(binding.root) {
        @SuppressLint("SetTextI18n")
        override fun onBind(item: Product, position: Int) {
            val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
            val currency = DataCenter.getMeta()?.currencies?.find { it.genericItemId == item.product?.currencyId }?.genericItemName.getSafe()
            val quantity = item.quantity.getSafe()
            binding.apply {
                ivWarning.setVisible(item.product?.prescriptionRequired.getBoolean() && isShowPrescription)
                ivIcon.loadImage(item.product?.media?.file.getSafe())
                tvTitle.text = "${item.product?.displayName.getSafe()} | ${item.product?.packageType?.genericItemName.getSafe()}"
                tvDesc.text = "${item.product?.packageSize.getSafe()} ${item.product?.dosage?.genericItemName.getSafe()} | ${item.product?.category?.genericItemName.getSafe()}"
                tvPrice.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.price?.round(2)} $currency" else "$currency ${item.price?.round(2)}"
                tvTotalAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${langData?.globalString?.total.getSafe()} ${item.subtotal?.round(2)} $currency" else "${langData?.globalString?.total.getSafe()} $currency ${item.subtotal?.round(2)}"
                tvDiscount.apply {
                    text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.product?.oldPrice?.getSafe()} $currency" else "$currency ${item.product?.oldPrice?.getSafe()}"
                    setTextColor(resources.getColor(R.color.call_red, context.theme))
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    setVisible(item.product?.oldPrice != null)
                }
                iCartButtons.apply {
                    tvQuantity.text = quantity.toString()
                    ivAdd.setOnClickListener {
                        onActionPLusClick?.invoke(item, position)
                    }
                    ivSub.setOnClickListener {
                        onActionMinusClick?.invoke(item, position)
                    }
                }
                ivWarning.setOnClickListener {
                    onWarningClick?.invoke()
                }
            }
        }
    }

}