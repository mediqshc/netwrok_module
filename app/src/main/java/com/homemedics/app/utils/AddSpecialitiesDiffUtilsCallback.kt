package com.homemedics.app.utils

import androidx.recyclerview.widget.DiffUtil
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.model.SpecialitiesModel

class AddSpecialitiesDiffUtilsCallback(
    oldList: List<GenericItem>?,
    newList: List<GenericItem>?
) : DiffUtil.Callback() {

    private var mOldList: List<GenericItem>? = oldList
    private var mNewList: List<GenericItem>? = newList

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
        )?.title) && (mOldList?.get(oldItemPosition)?.drawable == mNewList?.get(
            newItemPosition
        )?.drawable)
    }
}