package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemSelectAddressBinding
import com.homemedics.app.utils.getSafe


class SelectAddressAdapter : BaseRecyclerViewAdapter<SelectAddressAdapter.ViewHolder, MultipleViewItem>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_company }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): SelectAddressAdapter.ViewHolder {
        return ViewHolder(
            ItemSelectAddressBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemSelectAddressBinding) : BaseViewHolder<MultipleViewItem>(binding.root) {
        override fun onBind(item: MultipleViewItem, position: Int) {
            binding.apply {
                tvTitle.text = item.title
                tvDesc.text = item.desc
                rbCompany.isChecked = item.isSelected == true
                itemClickListener = {_,pos->
                    listItems.map { it.isSelected = false }
                    listItems[pos].isSelected = true
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun getSelectedItem(): MultipleViewItem? {
        return listItems.find { it.isSelected.getSafe() }
    }
}