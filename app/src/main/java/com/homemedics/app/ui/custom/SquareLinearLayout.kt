package com.homemedics.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout


class SquareLinearLayout(context: Context, attrs: AttributeSet)
    : LinearLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}