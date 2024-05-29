package com.homemedics.app.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemLabTestListBinding
import com.homemedics.app.locale.DefaultLocaleProvider

class LabTestCartListAdapter : BaseRecyclerViewAdapter<LabTestCartListAdapter.ViewHolder, LabTestResponse>() {

    override var layout: (viewType: Int) -> Int
    get() = { R.layout.item_lab_test_list }
    set(value) {}

    var currency: String? = ""

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLabTestListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onDeleteClick: ((item: LabTestResponse, pos: Int)->Unit)? = null

    inner class ViewHolder(var binding: ItemLabTestListBinding) : BaseViewHolder<LabTestResponse>(binding.root) {
        override fun onBind(item: LabTestResponse, position: Int) {
            val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
            binding.apply {
                tvTitle.text = item.labTest?.genericItemName
                tvDesc.text = item.labTest?.description
                tvAmount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.totalFee} $currency" else "$currency ${item.totalFee}"
                tvDiscount.apply {
                    if(item.discount.isNullOrEmpty().not() && item.discount != "0"){
                        tvDiscount.text = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.discount} $currency" else "$currency ${item.discount}"
                    }
                    setTextColor(resources.getColor(R.color.call_red, context.theme))
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }

                ivDelete.setOnClickListener {
                    onDeleteClick?.invoke(item, position)
                }
            }
        }
    }
}