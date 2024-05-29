package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.linkaccount.CompanyResponse
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemBorderedTextThumbViewBinding
import com.homemedics.app.databinding.ItemThumbTitleDescRadioBinding
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.loadImage
import kotlin.properties.Delegates

class LinkedAccountChipViewAdapter : BaseRecyclerViewAdapter<LinkedAccountChipViewAdapter.ViewHolder, CompanyResponse>(),
    Filterable {

    var itemListFiltered: ArrayList<CompanyResponse> = ArrayList()
    lateinit var originalList: ArrayList<CompanyResponse>

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
            ItemBorderedTextThumbViewBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(listItems[position], position)
        holder.itemView.setOnClickListener {
            selectedPosition = position
            itemClickListener?.invoke(listItems[position], position)
        }
    }

    var onDataFilter: ((list: ArrayList<CompanyResponse>) -> Unit)? = null

    inner class ViewHolder(var binding: ItemBorderedTextThumbViewBinding) :
        BaseViewHolder<CompanyResponse>(binding.root) {
        override fun onBind(item: CompanyResponse, position: Int) {
            binding.apply {
                tvTitle.text = item.name
                ivThumbnail.loadImage(item.iconUrl)
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
                        (it.name.getSafe().contains(constraint!!, true))
                    }.toList()


                    filterList.forEach {
                        filteredList.add(it)
                    }
                    filteredList
                }
                return FilterResults().apply { values = itemListFiltered }
            }

            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                itemListFiltered = if (results?.values == null)
                    ArrayList()
                else
                    results.values as ArrayList<CompanyResponse>

                listItems = itemListFiltered

                onDataFilter?.invoke(listItems)
            }
        }
    }
}