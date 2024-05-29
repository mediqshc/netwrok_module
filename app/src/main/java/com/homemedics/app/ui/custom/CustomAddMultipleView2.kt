package com.homemedics.app.ui.custom

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import com.fatron.network_module.models.generic.MultipleViewItem
import com.homemedics.app.R
import com.homemedics.app.databinding.LayoutAddMultipleViewBinding
import com.homemedics.app.ui.adapter.AddMultipleViewAdapter2
import com.homemedics.app.utils.AddMultipleViewDiffUtilsCallback
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.loadImage
import com.homemedics.app.utils.setVisible

class CustomAddMultipleView2(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    val mBinding: LayoutAddMultipleViewBinding =
        LayoutAddMultipleViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var itemsAdapter: AddMultipleViewAdapter2
    var listItems: ArrayList<MultipleViewItem> = ArrayList()
        set(value) {
            val diffUtilsCallback = AddMultipleViewDiffUtilsCallback(field, value)
            val diffResult = DiffUtil.calculateDiff(diffUtilsCallback)
            field.clear()
            field.addAll(value)
            mBinding.rvItems.adapter?.let { diffResult.dispatchUpdatesTo(it) }
        }

    var title: String = ""
        set(value) {
            field = value
            mBinding.tvHeading.text = value
        }
        get() = field

    var custDesc: String = ""
        set(value) {
            field = value
            if (value.isNotEmpty())
                mBinding.tvCustDesc.setVisible(true)
            else
                mBinding.tvCustDesc.setVisible(false)
            mBinding.tvCustDesc.text = value
        }
        get() = field

    var tvAddButton: String = ""
        set(value) {
            field = value
            mBinding.tvAddNew.text = value
        }
        get() = field

    var cnicUri: Uri? = null
        set(value) {
            field = value
            if (value != null){
                mBinding.ivCnic.setVisible(true)
                mBinding.ivCnic.loadImage(cnicUri, R.drawable.loading_animation)
            }else
                mBinding.ivCnic.setVisible(false)
        }

    var addNewRes: Int? = 0
        set(value) {
            field = value
            if (value != 0)
                mBinding.tvAddNew.setCompoundDrawablesWithIntrinsicBounds(0, 0, value.getSafe(), 0)
        }

    var showAddButton: Boolean? = true
        set(value) {
            field = value
            mBinding.tvAddNew.setVisible(showAddButton.getSafe())
        }

    var onAddItemClick: (() -> Unit)? = null
    var onEditItemCall: ((item: MultipleViewItem) -> Unit)? = null
    var onDeleteClick: ((item: MultipleViewItem, position: Int) -> Unit)? = null

    init {
        val styledAttributes =
            context.obtainStyledAttributes(attrs, R.styleable.CustomAddMultipleView)

        itemsAdapter = AddMultipleViewAdapter2()
        itemsAdapter.listItems = listItems

        title = styledAttributes.getString(R.styleable.CustomAddMultipleView_titleText).getSafe()
        custDesc = styledAttributes.getString(R.styleable.CustomAddMultipleView_custDesc).getSafe()
        tvAddButton =
            styledAttributes.getString(R.styleable.CustomAddMultipleView_addNewText).getSafe()
        showAddButton =
            styledAttributes.getBoolean(R.styleable.CustomAddMultipleView_showAddButton, true).getSafe()

        if (cnicUri != null) {
            mBinding.ivCnic.setVisible(true)
        } else {
            mBinding.ivCnic.setVisible(false)
        }

        mBinding.apply {
            tvHeading.text = title
            tvAddNew.text = tvAddButton
            itemsAdapter.onEditItemCall = {item->
                onEditItemCall?.invoke(item)
            }
            itemsAdapter.onDeleteClick = { item, position ->
                onDeleteClick?.invoke(item, position)
                // handling from
//                listItems.remove(item)
//                itemsAdapter.notifyDataSetChanged()
            }
            rvItems.adapter = itemsAdapter

            tvAddNew.setOnClickListener {
                onAddItemClick?.invoke()
            }
        }

        shouldShowTopBottomSpace()
    }

    fun addItemToList(item: MultipleViewItem){
        listItems.add(item)
        itemsAdapter.notifyDataSetChanged()
    }

    private fun shouldShowTopBottomSpace(){
        mBinding.apply {
            sTop.setVisible((switchButton.visibility == View.GONE && tvAddNew.visibility == View.GONE))
            sBottom.setVisible((switchButton.visibility == View.GONE && tvAddNew.visibility == View.GONE))
        }
    }

    companion object {

        @BindingAdapter("app:addNewText")
        @JvmStatic
        fun setaddNewTextValue(view: CustomAddMultipleView2, value: String?) {
            view. tvAddButton=value.getSafe()
        }
        @BindingAdapter("app:titleText")
        @JvmStatic
        fun settitleTextValue(view: CustomAddMultipleView2, value: String?) {
           view. title=value.getSafe()
        }

    }
}