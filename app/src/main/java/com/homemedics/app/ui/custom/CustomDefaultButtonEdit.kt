package com.homemedics.app.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.os.Parcel
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableString
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.InverseBindingMethod
import androidx.databinding.InverseBindingMethods
import com.homemedics.app.R
import com.homemedics.app.databinding.CustomDefaultButtoneditBinding
import com.homemedics.app.utils.getSafe


class CustomDefaultButtonEdit(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    private val defaultCharLength = 50

    val mBinding: CustomDefaultButtoneditBinding =
        CustomDefaultButtoneditBinding.inflate(LayoutInflater.from(context), this, true)

    var text: String = ""
        set(value) {
            field = value
            mBinding.editText.setText(SpannableString(value))
        }
        get() = mBinding.editText.text.toString()

    var hint: String = ""
        set(value) {
            field = value
            mBinding.textInputLayout.hint = value
        }
    var buttonText: String = ""
        set(value) {
            field = value
            mBinding.send.text = value
        }
    var verifHelperText: String? = null
        set(value) {
            field = value
            mBinding.textInputLayout.helperText = value
        }
    var errorText: String? = null
        set(value) {
            field = value
            mBinding.textInputLayout.error = value
        }

    var maxLength: Int? = 0
        set(value) {
            field = value
            if (value != 0)
                mBinding.editText.filters =
                    arrayOf<InputFilter>(InputFilter.LengthFilter(value ?: defaultCharLength))
        }
    var helperTextColor: Int? = 0
        set(value) {
            field = value
            mBinding.textInputLayout.setHelperTextColor(value?.let { ColorStateList.valueOf(it) })

        }

    var imeOptionCallback: (() -> Unit)? = null

    init {
        val styledAttributes =
            context.obtainStyledAttributes(attrs, R.styleable.CustomDefaultButtonEdit)

        text =  styledAttributes.getString(R.styleable.CustomDefaultButtonEdit_android_text).getSafe()
        hint =
            styledAttributes.getString(R.styleable.CustomDefaultButtonEdit_android_hint).getSafe()
        buttonText =
            styledAttributes.getString(R.styleable.CustomDefaultButtonEdit_buttonText).getSafe()
        verifHelperText =
            styledAttributes.getString(R.styleable.CustomDefaultButtonEdit_verifHelperText)
                .getSafe()
        helperTextColor =    styledAttributes.getInt(R.styleable.CustomDefaultButtonEdit_helperTextColor, 0).getSafe()
        errorText =
            styledAttributes.getString(R.styleable.CustomDefaultButtonEdit_error)
                .getSafe()
        maxLength = styledAttributes.getInt(
            R.styleable.CustomDefaultButtonEdit_android_maxLength,
            defaultCharLength
        ).getSafe()
        val inputType = styledAttributes.getInt(
            R.styleable.CustomDefaultButtonEdit_android_inputType,
            InputType.TYPE_CLASS_TEXT
        ).getSafe()
        val imeOptions =
            styledAttributes.getInt(R.styleable.CustomDefaultButtonEdit_android_imeOptions, 0)
                .getSafe()

        mBinding.editText.inputType = inputType
        mBinding.editText.imeOptions = imeOptions

        mBinding.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == imeOptions) {
                imeOptionCallback?.invoke()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        if (errorText != null || verifHelperText != null) {
            mBinding.editText.doAfterTextChanged {
                errorText = null
                verifHelperText = null
            }
        }
    }
    //////////// two way binding adapter ///////////////////

    @InverseBindingMethods(
        InverseBindingMethod(
            type = CustomDefaultButtonEdit::class,
            attribute = "android:text",
            method = "getText"
        )
    )
    class CustomDefaultButtonEditTextBinder {
        companion object {
            @JvmStatic
            @BindingAdapter(value = ["android:textAttrChanged"])
            fun setListener(editText: CustomDefaultButtonEdit, listener: InverseBindingListener?) {
                if (listener != null) {
                    editText.mBinding.editText.doOnTextChanged { text, start, before, count ->
                        listener.onChange()
                    }
                }
            }

            @JvmStatic
            @BindingAdapter("android:text")
            fun setText(editText: CustomDefaultButtonEdit, text: String?) {
                text?.let {
                    if (it != editText.text) {
                        editText.text = it
                    }
                }
            }
        }
    }

    //////////// two way binding end ///////////////////
    //////////// state management //////////////////////////

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.childrenStates = SparseArray<Any?>()
        for (i in 0 until childCount) {
            getChildAt(i).saveHierarchyState(ss.childrenStates as SparseArray<Parcelable>?)
        }
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        for (i in 0 until childCount) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates as SparseArray<Parcelable>?)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchThawSelfOnly(container)
    }

    internal class SavedState : BaseSavedState {
        var childrenStates: SparseArray<*>? = null

        constructor(superState: Parcelable?) : super(superState) {}
        private constructor(`in`: Parcel, classLoader: ClassLoader?) : super(`in`) {
            childrenStates = `in`.readSparseArray<Any>(classLoader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeSparseArray<Any>(childrenStates)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.ClassLoaderCreator<SavedState> = object :
                Parcelable.ClassLoaderCreator<SavedState> {
                override fun createFromParcel(source: Parcel, loader: ClassLoader?): SavedState {
                    return SavedState(source, loader)
                }

                override fun createFromParcel(source: Parcel): SavedState {
                    return createFromParcel(source, null)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    //////////// state management end //////////////////////////

}

private fun <T> Parcel.writeSparseArray(childrenStates: T?) {

}