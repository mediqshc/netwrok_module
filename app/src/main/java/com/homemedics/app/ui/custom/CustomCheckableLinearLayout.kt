package com.homemedics.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.*


class CustomCheckableLinearLayout(context: Context, attrs: AttributeSet)
    : LinearLayout(context, attrs), Checkable {

    private val mCheckedStateSet = intArrayOf(android.R.attr.state_checked)
    private var mOnCheckedChangeListener: OnCheckedChangeListener? = null
    private var mChecked = false
    lateinit var tvTitle: TextView


    init {
        init()
    }

    private fun init() {
        isClickable = true

        setOnClickListener {
            toggle()
        }
    }

    override fun isChecked(): Boolean {
        return mChecked
    }

    override fun toggle() {
        isChecked = !mChecked
    }

    override fun setChecked(b: Boolean) {
        if (b != mChecked) {
            mChecked = b
            refreshDrawableState()

            mOnCheckedChangeListener?.onCheckedChanged(this, mChecked)
        }
    }


    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            View.mergeDrawableStates(drawableState, mCheckedStateSet)
        }
        return drawableState
    }

    /**
     * Register a callback to be invoked when the checked state of this view changes.
     *
     * @param listener the callback to call on checked state change
     */
    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        mOnCheckedChangeListener = listener
    }

    interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param checkableView The view whose state has changed.
         * @param isChecked     The new checked state of checkableView.
         */
        fun onCheckedChanged(
            checkableView: View?,
            isChecked: Boolean
        )
    }

    //////////// two way binding adapter ///////////////////

    companion object{
        @JvmStatic
        @BindingAdapter("android:checkedAttrChanged")
        fun setListener(view: CustomCheckableLinearLayout, listener: InverseBindingListener?){
            if(listener != null){
                view.setOnCheckedChangeListener(object : OnCheckedChangeListener{
                    override fun onCheckedChanged(checkableView: View?, isChecked: Boolean) {
                        listener.onChange()
                    }
                })
            }
        }

        @BindingAdapter("android:checked")
        fun setChecked(view: CustomCheckableLinearLayout, isChecked: Boolean?){
            isChecked?.let {
                if(it != isChecked){
                    view.isChecked = it
                }
            }
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "android:checked")
        fun getChecked(view: CustomCheckableLinearLayout): Boolean{
            return view.mChecked
        }
    }

    //////////// two way binding end ///////////////////
}