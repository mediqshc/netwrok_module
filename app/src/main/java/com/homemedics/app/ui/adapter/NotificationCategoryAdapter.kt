package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.meta.NotificationCategory
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemBorderedTexBinding
import com.homemedics.app.utils.getSafe

class NotificationCategoryAdapter :
    BaseRecyclerViewAdapter<NotificationCategoryAdapter.ViewHolder, NotificationCategory>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_bordered_tex }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBorderedTexBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onCategorySlotSelected: ((NotificationCategory) -> Unit)? = null

    inner class ViewHolder(var binding: ItemBorderedTexBinding) :
        BaseViewHolder<NotificationCategory>(binding.root) {
        override fun onBind(item: NotificationCategory, position: Int) {

            binding.apply {
                tvTitle.text = item.title
                var drawables = R.drawable.ic_notifications
                when (item.id) {
                    2//chat
                    -> drawables = R.drawable.ic_chat
                    3//promo
                    -> drawables = R.drawable.ic_promo
                    4//order
                    -> drawables = R.drawable.ic_order
                }
                tvTitle.setCompoundDrawablesWithIntrinsicBounds(
                    binding.root.context.getDrawable(drawables),
                    null,
                    null,
                    null
                )

                tvTitle.isChecked = item.isChecked.getSafe()

                tvTitle.setOnClickListener {
                    listItems.filter {it.id!=listItems[position].id}.map{
                        it.isChecked = false
                    }
                    listItems[position].isChecked = listItems[position].isChecked?.not().getSafe()


                    notifyDataSetChanged()
                    onCategorySlotSelected?.invoke(item)
                }
            }
        }
    }

}