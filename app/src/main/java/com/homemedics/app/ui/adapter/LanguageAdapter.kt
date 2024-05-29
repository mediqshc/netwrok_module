package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.language.LanguageItem
import com.fatron.network_module.models.response.meta.NotificationCategory
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemBorderedTexBinding
import com.homemedics.app.databinding.ItemSelectAddressBinding
import com.homemedics.app.databinding.ItemSelectLanguageBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.gone

class LanguageAdapter  : BaseRecyclerViewAdapter<LanguageAdapter.ViewHolder, MultipleViewItem>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_company }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): LanguageAdapter.ViewHolder {
        return ViewHolder(
            ItemSelectLanguageBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onItemSelected: (()->Unit)? = null

    inner class ViewHolder(var binding: ItemSelectLanguageBinding) : BaseViewHolder<MultipleViewItem>(binding.root) {
        override fun onBind(item: MultipleViewItem, position: Int) {
            binding.apply {
                tvTitle.text = item.title
//                item.drawable?.let { ivIcon.setImageResource(it) }
                rbCompany.isChecked = item.isSelected == true
                itemClickListener = {_,pos->
                    if(  listItems[pos].isSelected !=true) {
                        listItems.map { it.isSelected = false }
                        listItems[pos].isSelected = true
                        notifyDataSetChanged()
                        onItemSelected?.invoke()
                    }
                }
            }
        }
    }

    fun getSelectedItem(): MultipleViewItem? {
        return listItems.find { it.isSelected.getSafe() }
    }
}

