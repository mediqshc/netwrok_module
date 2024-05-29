package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fatron.network_module.models.response.partnerprofile.DateSlotResponse
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemDateCheckableBinding
import com.homemedics.app.utils.getSafe
import timber.log.Timber

class DateSlotsAdapter : BaseRecyclerViewAdapter<DateSlotsAdapter.ViewHolder, DateSlotResponse>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_date_checkable }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDateCheckableBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onItemSelected : ((item: DateSlotResponse, pos: Int) -> Unit)? = null

    inner class ViewHolder(var binding: ItemDateCheckableBinding) : BaseViewHolder<DateSlotResponse>(binding.root) {
        override fun onBind(item: DateSlotResponse, position: Int) {
            binding.apply {
                tvDate.text = item.date
                tvDay.text = item.day

                ccLinearLayout.isChecked = item.isChecked.getSafe()

                ccLinearLayout.isClickable = item.isActive.getSafe()
                ccLinearLayout.isEnabled = item.isActive.getSafe()

                itemClickListener = { i, pos ->
                    onItemSelected?.invoke(i, pos)
                    listItems.map {
                        it.isChecked = false
                        it
                    }
                    listItems[pos].isChecked = true
                    Timber.e(item.isChecked.toString())
                    notifyDataSetChanged()
                }

                if(item.isActive.getSafe()){
                    ccLinearLayout.setBackgroundResource(R.drawable.selector_rounded_primary_border_divider)
                    tvDay.setTextColor(ContextCompat.getColor(tvDay.context, R.color.black90))
                    tvDate.setTextColor(ContextCompat.getColor(tvDay.context, R.color.black90))
                    if (ccLinearLayout.isChecked) {
                        tvDay.setTextColor(ContextCompat.getColor(tvDay.context, R.color.white))
                        tvDate.setTextColor(ContextCompat.getColor(tvDay.context, R.color.white))
                    } else {
                        tvDay.setTextColor(ContextCompat.getColor(tvDay.context, R.color.black90))
                        tvDate.setTextColor(ContextCompat.getColor(tvDay.context, R.color.black90))
                    }
                } else {
                    ccLinearLayout.setBackgroundResource(R.drawable.rounded_divider)
                    tvDay.setTextColor(ContextCompat.getColor(tvDay.context, R.color.primary20))
                    tvDate.setTextColor(ContextCompat.getColor(tvDate.context, R.color.primary20))
                }
            }
        }

    }

    fun getSelectedItem(): DateSlotResponse? {
        return listItems.find { it.isChecked.getSafe() } as DateSlotResponse?
    }
}