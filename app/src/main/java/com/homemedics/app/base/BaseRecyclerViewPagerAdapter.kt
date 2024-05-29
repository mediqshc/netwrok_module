package com.homemedics.app.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.fatron.network_module.models.base.BasePagingModel

abstract class BaseRecyclerViewPagerAdapter<H : BasePagingViewHolder> : PagingDataAdapter<BasePagingModel, H>(REPO_COMPARATOR) {

    companion object {
        private val REPO_COMPARATOR = object : DiffUtil.ItemCallback<BasePagingModel>() {
            override fun areItemsTheSame(oldItem: BasePagingModel, newItem: BasePagingModel) =
                oldItem.baseId == newItem.baseId

            override fun areContentsTheSame(oldItem: BasePagingModel, newItem: BasePagingModel) =
                oldItem.baseId == newItem.baseId
        }
    }

    var listItems: ArrayList<BasePagingModel> = arrayListOf()
        set(value) = run {
            field = value
            notifyDataSetChanged()
        }
    var itemClickListener: ((BasePagingModel, position: Int) -> Unit)? = null
    abstract var layout: (viewType: Int) -> Int
    abstract fun viewHolder(view: View, viewType: Int): H
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H =
        viewHolder(LayoutInflater.from(parent.context)
            .inflate(layout(viewType), parent, false), viewType)

    override fun getItemCount(): Int = listItems.size
    override fun onBindViewHolder(holder: H, position: Int) {
        if (listItems[position]!=null)
        holder.onBind(listItems[position], position)
        holder.itemView.setOnClickListener {
            itemClickListener?.let {
                it(listItems[position], position)
            }
        }
    }
    fun getList() = listItems
}