package com.homemedics.app.utils

import androidx.recyclerview.widget.DiffUtil
import com.fatron.network_module.models.generic.MultipleViewItem

class AddMultipleViewDiffUtilsCallback (
    oldList: List<MultipleViewItem>?,
    newList: List<MultipleViewItem>?
) : DiffUtil.Callback() {
    private var mOldList: List<MultipleViewItem>? = oldList
    private var mNewList: List<MultipleViewItem>? = newList

    override fun getOldListSize(): Int {
        return mOldList?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return mNewList?.size ?: 0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldList?.get(oldItemPosition)?.title === mNewList?.get(newItemPosition)?.title
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (mOldList?.get(oldItemPosition)?.title == mNewList?.get(
            newItemPosition
        )?.title) && (mOldList?.get(oldItemPosition)?.desc == mNewList?.get(
            newItemPosition
        )?.desc)
    }
}