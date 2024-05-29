package com.homemedics.app.ui.custom

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.textfield.TextInputLayout
import com.homemedics.app.R
import com.homemedics.app.databinding.CustomDefaultDropdownBinding
import com.homemedics.app.ui.adapter.ModArrayAdapter
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.restoreChildViewStates
import com.homemedics.app.utils.saveChildViewStates
import com.homemedics.app.utils.setVisible
import timber.log.Timber

class CustomDefaultDropDown(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val mBinding: CustomDefaultDropdownBinding =
        CustomDefaultDropdownBinding.inflate(LayoutInflater.from(context), this, true)

    var hint: String = ""
        set(value) {
            field = value
            mBinding.textInputLayout.hint = value
        }
        get() = hint
    var drawableStart: Int? = 0
        set(value) {
            field = value
            if (value != 0)
                mBinding.ivDrawableStart.setImageResource(value ?: 0)
        }

    lateinit var adapter: ModArrayAdapter<String>
    var errorText: String? = null
        set(value) {
            field = value
            mBinding.textInputLayout.error = value
        }
    var data: ArrayList<String> = ArrayList()
        set(value) {
            field = value
            adapter = ModArrayAdapter(context, value)
            mBinding.dropdownMenu.setAdapter(adapter)
        }
    var isDropdownEnabled: Boolean = true
        set(value) {
            field = value
            mBinding.dropdownMenu.isEnabled = isDropdownEnabled
            if(isDropdownEnabled.not()) {
                mBinding.textInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
            }else
                mBinding.textInputLayout.endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
        }

    var selectionIndex: Int = 0
        set(value) {
            field = value
            mBinding.dropdownMenu.setText(data[value], false)
        }
    var onItemSelectedListener: ((String, Int) -> Unit)? = null

    init {
        val styledAttributes =
            context.obtainStyledAttributes(attrs, R.styleable.CustomDefaultDropDown)
        drawableStart =
            styledAttributes.getResourceId(R.styleable.CustomDefaultDropDown_drawableStart, 0)
        if (drawableStart == 0) mBinding.ivDrawableStart.setVisible(false) else mBinding.ivDrawableStart.setVisible(
            true
        )
        isDropdownEnabled = styledAttributes.getBoolean(
            R.styleable.CustomDefaultDropDown_android_enabled,
            true
        ).getSafe()
        hint = styledAttributes.getString(R.styleable.CustomDefaultDropDown_hintText).getSafe()
        mBinding.apply {
            dropdownMenu.setOnItemClickListener { _, _, i, l ->
                onItemSelectedListener?.invoke(data[i], i)
            }
        }
    }

    companion object {
        @BindingAdapter("selectedValue")
        @JvmStatic
        fun setSelectedValue(view: CustomDefaultDropDown, value: String?) {

        }

        @BindingAdapter("app:hintText")
        @JvmStatic
        fun sethintTextValue(view: CustomDefaultDropDown, value: String?) {

        }

        @InverseBindingAdapter(attribute = "selectedValue")
        @JvmStatic
        fun getTextValue(view: CustomDefaultDropDown): String =
            view.mBinding.dropdownMenu.text.toString()


        @JvmStatic
        @BindingAdapter("selectedValueAttrChanged")
        fun setListener(view: CustomDefaultDropDown, listener: InverseBindingListener?) {
            view.mBinding.dropdownMenu.apply {
                setOnItemClickListener { adapterView, _, pos, _ ->
                    view.selectionIndex = pos
                    view.onItemSelectedListener?.invoke(view.data[pos], pos)
                    listener?.onChange()
                }
            }
        }
    }

    //////////// state management //////////////////////////

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState()).apply {
            childrenStates = saveChildViewStates()
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        when (state){
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                state.childrenStates?.let { restoreChildViewStates(it) }
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchThawSelfOnly(container)
    }

    internal class SavedState : BaseSavedState {

        var childrenStates: SparseArray<Parcelable>? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            childrenStates = source.readSparseArray<SparseArray<Parcelable>>(javaClass.classLoader) as SparseArray<Parcelable>?
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeSparseArray(childrenStates as SparseArray<Any>)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    //////////// state management end //////////////////////////
}