package com.homemedics.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner

class ModSpinner(context: Context, attrs: AttributeSet?) : AppCompatSpinner(
    context, attrs
) {
    var listener: OnItemSelectedListener? = null
    override fun setSelection(position: Int) {
        super.setSelection(position)
        if (listener != null) listener?.onItemSelected(null, null, position, 0)
    }

    fun setOnItemSelectedEvenIfUnchangedListener(
        listener: OnItemSelectedListener?
    ) {
        this.listener = listener
    }
}