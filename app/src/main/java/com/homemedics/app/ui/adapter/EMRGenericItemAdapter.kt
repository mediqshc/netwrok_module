package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemEmrGenericBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.loadImage
import com.homemedics.app.utils.setVisible

class EMRGenericItemAdapter : BaseRecyclerViewAdapter<EMRGenericItemAdapter.ViewHolder, GenericItem>(), Filterable {

    var itemListFiltered: ArrayList<GenericItem> = ArrayList()
    lateinit var originalList: ArrayList<GenericItem>

    var onDataFilter: ((list: ArrayList<GenericItem>) -> Unit)? = null

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_emr_generic }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemEmrGenericBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemEmrGenericBinding) : BaseViewHolder<GenericItem>(binding.root) {
        override fun onBind(item: GenericItem, position: Int) {
            binding.apply {
                tvTitle.text = item.genericItemName
                tvDesc.text = item.description
                ivIcon.apply {
                    setVisible(item.icon_url.isNullOrEmpty().not())
                    loadImage(item.icon_url, R.drawable.ic_emergency)
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString().getSafe()

                itemListFiltered = if (charString.isEmpty()) originalList else {
                    val filteredList = ArrayList<GenericItem>()

                    val filterList = originalList.filter {
                        (it.title.getSafe().contains(constraint!!,true))
                    }.toList()

                    filterList.forEach {
                        filteredList.add(it)
                    }
                    filteredList
                }
                return FilterResults().apply { values = itemListFiltered }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                itemListFiltered = if (results?.values == null)
                    ArrayList()
                else
                    results.values as ArrayList<GenericItem>

                listItems = itemListFiltered

                onDataFilter?.invoke(listItems)
            }
        }
    }
}