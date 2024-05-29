package com.homemedics.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.homemedics.app.R
import com.homemedics.app.databinding.LayoutActionbarHomeBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.gone
import com.homemedics.app.utils.setVisible
import com.homemedics.app.utils.visible

class CustomActionbar(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    val mBinding: LayoutActionbarHomeBinding =
        LayoutActionbarHomeBinding.inflate(LayoutInflater.from(context), this, true)

    var action1Res: Int? = 0
        set(value) {
            field = value
            if (value != 0)
                mBinding.ivBack.setImageResource(value ?: 0)
        }

    var action2Res: Int? = 0
        set(value) {
            field = value
            if (value == 0) {
                mBinding.ivAction2.setVisible(false)
            } else {
                mBinding.ivAction2.setVisible(true)
                mBinding.ivAction2.setImageResource(value ?: 0)
            }
        }

    var action3Res: Int? = 0
        set(value) {
            field = value
            if (value == 0) {
                mBinding.ivAction3.setVisible(false)
            } else {
                mBinding.ivAction3.setVisible(true)
                mBinding.ivAction3.setImageResource(value ?: 0)
            }
        }

    var title: String = ""
        set(value) {
            field = value
            mBinding.tvTitle.text = value
        }
        get() = mBinding.tvTitle.text.toString()

    var desc: String = ""
        set(value) {
            field = value
            mBinding.tvDesc.text = value
        }
        get() = mBinding.tvDesc.text.toString()

    var dotText: String? = null
        set(value) {
            field = value

            try {
                mBinding.tvDot.text = value
                mBinding.tvDot.setVisible(value.isNullOrEmpty().not() && value?.toInt().getSafe() > 0)
            }
            catch (nfe: java.lang.NumberFormatException){
                mBinding.tvDot.setVisible(false)
            }
        }
        get() = mBinding.tvDot.text.toString()

    var onAction1Click: (() -> Unit)? = null
    var onAction2Click: (() -> Unit)? = null
    var onAction3Click: (() -> Unit)? = null

    init {
        val styledAttributes =
            context.obtainStyledAttributes(attrs, R.styleable.CustomActionbar)

        action1Res =
            styledAttributes.getResourceId(R.styleable.CustomActionbar_action1Res, 0)
        action2Res =
            styledAttributes.getResourceId(R.styleable.CustomActionbar_action2Res, 0)
        action3Res =
            styledAttributes.getResourceId(R.styleable.CustomActionbar_action3Res, 0)
        title = styledAttributes.getString(R.styleable.CustomActionbar_titleText).getSafe()
        desc = styledAttributes.getString(R.styleable.CustomActionbar_desc).getSafe()
        dotText = styledAttributes.getString(R.styleable.CustomActionbar_dotText).getSafe()

        if (action1Res == 0) {
            mBinding.ivBack.setVisible(false)
        } else {
            mBinding.ivBack.setVisible(true)
        }


        mBinding.apply {
            ivBack.setOnClickListener {
                onAction1Click?.invoke()
            }
            ivAction2.setOnClickListener {
                onAction2Click?.invoke()
            }
            ivAction3.setOnClickListener {
                onAction3Click?.invoke()
            }
            tvDesc.setOnClickListener {
                onAction2Click?.invoke()
            }
        }
    }
}