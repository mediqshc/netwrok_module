package com.homemedics.app.ui.adapter

import android.content.Context
import android.widget.ArrayAdapter
import com.homemedics.app.R

class ModArrayAdapter<T>(context: Context,val data: ArrayList<T>): ArrayAdapter<T>(context, R.layout.dropdown_item, data) {
    fun getAllItems(): ArrayList<T> = data
}