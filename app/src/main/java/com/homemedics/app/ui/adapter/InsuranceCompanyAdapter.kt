package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.labtest.LabResponse
import com.fatron.network_module.models.response.linkaccount.CompanyResponse
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemCompanyBinding
import com.fatron.network_module.models.response.meta.GenericItem
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.loadImage
import kotlin.properties.Delegates

class InsuranceCompanyAdapter : BaseRecyclerViewAdapter<InsuranceCompanyAdapter.ViewHolder, CompanyResponse>(), Filterable {

    var itemListFiltered: ArrayList<CompanyResponse> = ArrayList()
    lateinit var originalList: ArrayList<CompanyResponse>

    var onItemSelected: ((CompanyResponse,  position: Int) -> Unit)? = null

    override var layout: (viewType: Int) -> Int
    get() = { R.layout.item_company }
    set(value) {}

    var selectedPosition by Delegates.observable(-1) { property, oldPos, newPos ->
        if (oldPos != newPos) {
            if (newPos in listItems.indices) {
                listItems[newPos].isSelected = true
                if (oldPos != -1) {
                    listItems[oldPos].isSelected = false
                    notifyItemChanged(oldPos)
                }
                notifyItemChanged(newPos)
            }
        }
    }

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCompanyBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemCompanyBinding) : BaseViewHolder<CompanyResponse>(binding.root) {
        override fun onBind(item: CompanyResponse, position: Int) {
            binding.apply {
                multipleViewItem = item
                (clCompany).setOnClickListener {
                    selectedPosition = position
                    onItemSelected?.invoke(item, position)
                }
            }
        }
    }

    fun getSelectedItem(): CompanyResponse? {
        return listItems.find { it.isSelected.getSafe() }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""

                itemListFiltered = if (charString.isEmpty()) originalList else {
                    val filteredList = ArrayList<CompanyResponse>()

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
                    results.values as ArrayList<CompanyResponse>

                listItems = itemListFiltered
            }
        }
    }
}