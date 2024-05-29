package com.homemedics.app.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.pharmacy.PharmacyProduct
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemPharmacyMedicineBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.*

class PharmacyListAdapter : BaseRecyclerViewAdapter<PharmacyListAdapter.ViewHolder, PharmacyProduct>() {

    override var layout: (viewType: Int) -> Int
    get() = { R.layout.item_pharmacy_medicine }
    set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPharmacyMedicineBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onDataFilter: ((list: ArrayList<GenericItem>) -> Unit)? = null
    var isRelatedItem = false

    inner class ViewHolder(var binding: ItemPharmacyMedicineBinding) : BaseViewHolder<PharmacyProduct>(binding.root) {
        @SuppressLint("SetTextI18n")
        override fun onBind(item: PharmacyProduct, position: Int) {
            val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
            val currency = DataCenter.getMeta()?.currencies?.find { it.genericItemId == item.currencyId }?.genericItemName
            binding.apply {
                ivPharma.loadImage(item.media?.file.getSafe(), R.drawable.ic_placeholder)
                tvName.text = "${item.displayName.getSafe()} ${Constants.PIPE} ${item.packageType?.genericItemName.getSafe()}"
                tvDescription.text = if (isRelatedItem) "${item.packageSize.getSafe()} ${item.dosage?.genericItemName.getSafe()} ${Constants.START}${Constants.MULTIPLY}${Constants.END} ${item.sellingVolume.getSafe()} ${item.packageType?.genericItemName?.lowercase().getSafe()} ${Constants.PIPE} ${item.category?.genericItemName.getSafe()}${Constants.END}"
                   else "${item.packageSize.getSafe()} ${item.dosage?.genericItemName.getSafe()} ${Constants.START}${Constants.MULTIPLY}${Constants.END} ${item.sellingVolume.getSafe()} ${Constants.PIPE} ${item.category?.genericItemName.getSafe()}"
                tvAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.price.getSafe()} $currency" else "$currency ${item.price.getSafe()}"
                tvProductDiscount.apply {
                    text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.oldPrice.getSafe()} $currency" else "$currency ${item.oldPrice.getSafe()}"
                    setTextColor(resources.getColor(R.color.call_red, context.theme))
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    setVisible(item.oldPrice != null)
                }
            }
        }
    }
}