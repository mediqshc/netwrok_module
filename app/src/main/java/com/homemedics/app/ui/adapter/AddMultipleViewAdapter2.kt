package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.generic.MultipleViewItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemAddMultipleList2Binding
import com.homemedics.app.utils.setVisible

class AddMultipleViewAdapter2: BaseRecyclerViewAdapter<AddMultipleViewAdapter2.ViewHolder, MultipleViewItem>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_add_multiple_list }
        set(value) {}

    var onDeleteClick: ((item: MultipleViewItem, position: Int)->Unit)? = null
    var onEditItemCall: ((item: MultipleViewItem)->Unit)? = null

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAddMultipleList2Binding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemAddMultipleList2Binding) : BaseViewHolder<MultipleViewItem>(binding.root) {
        override fun onBind(item: MultipleViewItem, position: Int) {
            binding.apply {
                dataManager = item
                tvTitle.setOnClickListener{
                    if (item.itemEndIcon!=null)
                        onEditItemCall?.invoke(item)
                }
//                ivAudioImage.setOnClickListener{
//                    if (item.itemEndIcon!=null)
//                        onEditItemCall?.invoke(item)
//                }
//                if (item.itemCenterIcon!=null){
//                    tvTitle.setVisible(false)
//                    tvDesc.setVisible(false)
//                    ivAudioImage.setVisible(true)
//                }
                ivDelete.setOnClickListener {
                    if (item.itemEndIcon!=null)
                        onEditItemCall?.invoke(item)
                    else
                    onDeleteClick?.invoke(item, position)
                }

                item.hasSecondAction?.let { ivEdit.setVisible(it) }
                ivEdit.setOnClickListener {
                    onEditItemCall?.invoke(item)
                }
            }
        }
    }
}