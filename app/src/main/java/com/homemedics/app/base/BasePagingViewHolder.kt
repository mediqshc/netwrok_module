package com.homemedics.app.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.base.BasePagingModel

abstract class BasePagingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun onBind(item: BasePagingModel, position: Int)
}