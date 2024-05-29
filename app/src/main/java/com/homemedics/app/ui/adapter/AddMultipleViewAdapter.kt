package com.homemedics.app.ui.adapter

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fatron.network_module.models.generic.MultipleViewItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemAddMultipleListBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.gone
import com.homemedics.app.utils.setVisible
import com.homemedics.app.utils.visible

class AddMultipleViewAdapter: BaseRecyclerViewAdapter<AddMultipleViewAdapter.ViewHolder, MultipleViewItem>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_add_multiple_list }
        set(value) {}

    var onDeleteClick: ((item: MultipleViewItem, position: Int)->Unit)? = null
    var onEditItemCall: ((item: MultipleViewItem)->Unit)? = null
    var onItemClick: ((item: MultipleViewItem, position: Int)->Unit)? = null

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAddMultipleListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemAddMultipleListBinding) : BaseViewHolder<MultipleViewItem>(binding.root) {
        override fun onBind(item: MultipleViewItem, position: Int) {
            binding.apply {
                dataManager = item

                if (item.isRed.getSafe()) {
                    val color = ContextCompat.getColor(tvDesc.context, R.color.orange)
                    val first = item.desc
                    val next = item.redText
                    val builder = SpannableStringBuilder()
                    builder.append(first)
                    val start: Int = builder.length
                    builder.append(next)
                    val end: Int = builder.length
                    builder.setSpan(
                        ForegroundColorSpan(color),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    tvDesc.text = builder
                } else {
                    tvDesc.text = item.desc
                }

                //commented tis code for onitemclick fix, revert if malfunction anywhere
//                tvTitle.setOnClickListener{
//                    if(item.isEnabled.getSafe().not())
//                        return@setOnClickListener
//
//                    if (item.itemEndIcon!=null)
//                        onEditItemCall?.invoke(item)
//                }
//                ivAudioImage.setOnClickListener{
//                    if (item.itemEndIcon!=null)
//                        onEditItemCall?.invoke(item)
//                }

                ivIcon.setOnClickListener {
                    onEditItemCall?.invoke(item)
                }

                if (item.isRTL.getSafe()) {
                    tvTitle.textDirection = View.TEXT_DIRECTION_LTR
                }

                if (item.itemCenterIcon!=null){
                    tvTitle.setVisible(false)
                    tvDesc.setVisible(false)
                    ivAudioImage.setVisible(true)
                } else {
                    tvTitle.visible()
                    tvDesc.visible()
                    ivAudioImage.gone()
                }
                ivDelete.setOnClickListener {
                    if(item.isEnabled.getSafe().not())
                        return@setOnClickListener

                    if (item.itemEndIcon!=null)
                        onEditItemCall?.invoke(item)
                    else
                        onDeleteClick?.invoke(item, position)
                }

                item.hasSecondAction?.let { ivEdit.setVisible(it) }
                ivEdit.setOnClickListener {
                    if(item.isEnabled.getSafe().not())
                        return@setOnClickListener
                    onEditItemCall?.invoke(item)
                }

                //commented tis code for onitemclick fix, revert if malfunction anywhere
              /*  itemClickListener?.let { //to set all views for same listener by avoiding above shit
                    if(item.isEnabled.getSafe()){
                        tvTitle.setOnClickListener {
                            itemClickListener?.invoke(item, position)
                        }
                        ivDelete.setOnClickListener {
                            itemClickListener?.invoke(item, position)
                        }
                        container.setOnClickListener {
                            itemClickListener?.invoke(item, position)
                        }
                    }
               }*/

                itemClickListener = { item, pos ->
                    onItemClick?.invoke(item, pos)
                }

                //view enable/disable
                if(item.isEnabled.getSafe()){
                    val black = ContextCompat.getColor(container.context, R.color.black)
                    val grey = ContextCompat.getColor(container.context, R.color.grey)

                    ivIcon.clearColorFilter()
                    ivEdit.clearColorFilter()
                    ivDelete.clearColorFilter()

                    tvTitle.setTextColor(black)
                    tvDesc.setTextColor(grey)
                    tvSubDesc.setTextColor(grey)
                }
                else {
                    val disabled = ContextCompat.getColor(container.context, R.color.primary20)

                    ivIcon.setColorFilter(disabled)
                    ivEdit.setColorFilter(disabled)
                    ivDelete.setColorFilter(disabled)

                    tvTitle.setTextColor(disabled)
                    tvDesc.setTextColor(disabled)
                    tvSubDesc.setTextColor(disabled)
                }

                binding.executePendingBindings()
            }
        }
    }
}