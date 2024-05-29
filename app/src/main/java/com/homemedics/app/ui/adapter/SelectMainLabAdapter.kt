package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.fatron.network_module.models.response.labtest.LabResponse
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemSelectMainLabBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.utils.DataCenter
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.gone

class SelectMainLabAdapter : BaseRecyclerViewAdapter<SelectMainLabAdapter.ViewHolder, LabResponse>(), Filterable {

    var itemListFiltered: ArrayList<LabResponse> = ArrayList()
    lateinit var originalList: ArrayList<LabResponse>
    var showLabPrice = true

    override var layout: (viewType: Int) -> Int
    get() = { R.layout.item_select_main_lab }
    set(value) {}

    override fun viewHolder(view: View, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSelectMainLabBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    var onDataFilter: ((list: ArrayList<LabResponse>) -> Unit)? = null

    inner class ViewHolder(var binding: ItemSelectMainLabBinding) : BaseViewHolder<LabResponse>(binding.root) {
        override fun onBind(item: LabResponse, position: Int) {
            val locale = TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
            binding.apply {
                var currency = ""
                if(showLabPrice){
                    if(item.labTestLaboratory.isNullOrEmpty().not()) {
                        currency = DataCenter.getMeta()?.currencies?.find { it.genericItemId == item.labTestLaboratory?.get(0)?.currencyId }?.genericItemName.getSafe()
                        item.desc = if (locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN) "${item.labTestLaboratory?.first()?.rate.getSafe()} $currency" else "$currency ${item.labTestLaboratory?.first()?.rate.getSafe()}"
                    }
                }
                else{
                    tvDesc.gone()
                    tvSubDesc.gone()
                }
                multipleViewItem = item
                tvTitle.maxLines = 2
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
                        (it.name.getSafe().contains(constraint!!,true))
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