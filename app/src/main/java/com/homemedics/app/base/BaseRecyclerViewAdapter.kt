package com.homemedics.app.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<H : BaseViewHolder<M>, M> : RecyclerView.Adapter<H>() {
    var listItems: ArrayList<M> = arrayListOf()
        set(value) = run {
            field = value
            notifyDataSetChanged()
        }
    var itemClickListener: ((M,  position: Int) -> Unit)? = null
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
                if(position < listItems.size) //to avoid monkey testing
                    it(listItems[position], position)
            }
        }
    }
    fun getList() = listItems
}