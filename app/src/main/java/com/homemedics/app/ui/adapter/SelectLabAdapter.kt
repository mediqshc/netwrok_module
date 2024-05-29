package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.labtest.LabResponse
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemThumbTitleDescRadioBinding
import com.homemedics.app.utils.getSafe
import kotlin.properties.Delegates

class SelectLabAdapter : BaseRecyclerViewAdapter<SelectLabAdapter.ViewHolder, LabResponse>(),
    Filterable {

    var itemListFiltered: ArrayList<LabResponse> = ArrayList()
    lateinit var originalList: ArrayList<LabResponse>

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
            ItemThumbTitleDescRadioBinding.inflate(
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

    var onDataFilter: ((list: ArrayList<LabResponse>) -> Unit)? = null

    inner class ViewHolder(var binding: ItemThumbTitleDescRadioBinding) :
        BaseViewHolder<LabResponse>(binding.root) {
        override fun onBind(item: LabResponse, position: Int) {
            binding.apply {
                tvDesc.maxLines = 20 //no limit
                item.imageUrl = item.labs?.iconUrl
                item.hasLargeIcon = true
                multipleViewItem = item
                tvTitle.text = item.title.getSafe()
                executePendingBindings()
            }
        }
    }

    fun getSelectedItem(): LabResponse? {
        return listItems.find { it.isSelected.getSafe() }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""

                itemListFiltered = if (charString.isEmpty()) originalList else {
                    val filteredList = ArrayList<LabResponse>()

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
                    results.values as ArrayList<LabResponse>

                listItems = itemListFiltered

                onDataFilter?.invoke(listItems)
            }
        }
    }
}