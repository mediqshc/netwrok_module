package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.models.response.partnerprofile.Services
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.FragmentServiceItemBinding
import com.homemedics.app.databinding.ItemFamilyConnectionListBinding
import com.homemedics.app.model.ConnectionType
import com.homemedics.app.utils.*

class ServiceAdapter : BaseRecyclerViewAdapter<ServiceAdapter.ViewHolder, Services>() {


    override var layout: (viewType: Int) -> Int
        get() = { R.layout.fragment_service_item }
        set(value) {}

    var onServiceCheckChange: ((item: Services, position: Int)->Unit)? = null

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentServiceItemBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: FragmentServiceItemBinding) : BaseViewHolder<Services>(binding.root) {
        override fun onBind(item: Services, position: Int) {
            binding.apply {
                tvTitle.text = item.name
                tvDesc.text = item.description
                tbServiceCheck.isChecked=item.offerService
                tbServiceCheck.setOnClickListener {
//                    item.offerService = tbServiceCheck.isChecked.not()

                    listItems[position].offerService = tbServiceCheck.isChecked
                    notifyDataSetChanged()
                    onServiceCheckChange?.invoke(item, position)
                }
            }
        }
    }

}