package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemWeekDaysListBinding
import com.homemedics.app.utils.getSafe

class WeekDaysAdapter: BaseRecyclerViewAdapter<WeekDaysAdapter.ViewHolder, GenericItem>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_demo_list }
        set(value) {}

    var onCheckChanged: (()->Unit)? = null

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWeekDaysListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemWeekDaysListBinding) : BaseViewHolder<GenericItem>(binding.root) {
        override fun onBind(item: GenericItem, position: Int) {
            binding.apply {
                tvTitle.text = item.title
                switchButton.isChecked = item.isSelected.getSafe()

                switchButton.setOnCheckedChangeListener { _, b ->
                    listItems[position].isSelected = b
                    onCheckChanged?.invoke()
                }
            }
        }
    }

    fun getSelectedItems(): ArrayList<GenericItem>{
        return listItems.filter { it.isSelected.getSafe() } as ArrayList<GenericItem>
    }
}