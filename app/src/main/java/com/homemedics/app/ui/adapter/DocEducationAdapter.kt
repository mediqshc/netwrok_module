package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.partnerprofile.EducationResponse
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemAddMultipleListBinding
import com.homemedics.app.utils.setVisible

class DocEducationAdapter  : BaseRecyclerViewAdapter<DocEducationAdapter.ViewHolder, EducationResponse>() {

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_add_multiple_list }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAddMultipleListBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemAddMultipleListBinding) : BaseViewHolder<EducationResponse>(binding.root) {
        override fun onBind(item: EducationResponse, position: Int) {
            binding.apply {
                ivDelete.setVisible(false)
                dataManager=item
            }
        }
    }

}