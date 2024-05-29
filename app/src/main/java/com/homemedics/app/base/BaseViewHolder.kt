package com.homemedics.app.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<R>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun onBind(item: R, position: Int)
}