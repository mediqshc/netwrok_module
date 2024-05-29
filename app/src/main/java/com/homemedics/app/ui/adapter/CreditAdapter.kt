package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.linkaccount.CompaniesService
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemCreditBinding
import com.homemedics.app.databinding.ItemDetailBinding
import com.homemedics.app.utils.getBoolean
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.setVisible

class CreditAdapter  : BaseRecyclerViewAdapter<CreditAdapter.ViewHolder, CompaniesService>(){

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_credit }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): CreditAdapter.ViewHolder {
        return ViewHolder(
            ItemCreditBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemCreditBinding) : BaseViewHolder<CompaniesService>(binding.root) {
        override fun onBind(item: CompaniesService, position: Int) {
            binding.apply {
                tvItem.text = item.name
                tvDesc.apply {
                    setVisible(item.conditionsApply.getBoolean())
                    text = ApplicationClass.mGlobalData?.globalString?.conditionsApply.getSafe()
                }
            }
        }
    }
}