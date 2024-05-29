package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.linkaccount.CompanyResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemExcludedProductBinding
import com.homemedics.app.databinding.ItemSpecialityConsultationBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.loadImage

class ExcludedProductAdapter  : BaseRecyclerViewAdapter<ExcludedProductAdapter.ViewHolder, GenericItem>() ,
    Filterable {

    var itemListFiltered: ArrayList<GenericItem> = ArrayList()
    lateinit var originalList: ArrayList<GenericItem>

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_excluded_product }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemExcludedProductBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemExcludedProductBinding) : BaseViewHolder<GenericItem>(binding.root) {
        override fun onBind(item: GenericItem, position: Int) {
            binding.apply {
                multipleViewItem = item
                ivIcon.setImageResource(R.drawable.ic_circle)
            }
        }
    }

    fun getSelectedItem(): GenericItem? {
        return listItems.find { it.isSelected.getSafe() }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""

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
            }
        }
    }
}