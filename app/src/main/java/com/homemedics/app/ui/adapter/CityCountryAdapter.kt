package com.homemedics.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fatron.network_module.models.response.linkaccount.CityCountry
import com.homemedics.app.R
import com.homemedics.app.base.BaseRecyclerViewAdapter
import com.homemedics.app.base.BaseViewHolder
import com.homemedics.app.databinding.ItemCityCountryBinding
import com.homemedics.app.utils.getSafe

class CityCountryAdapter : BaseRecyclerViewAdapter<CityCountryAdapter.ViewHolder, CityCountry>(){

    override var layout: (viewType: Int) -> Int
        get() = { R.layout.item_city_country }
        set(value) {}

    override fun viewHolder(view: View, viewType: Int): CityCountryAdapter.ViewHolder {
        return ViewHolder(
            ItemCityCountryBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        )
    }

    inner class ViewHolder(var binding: ItemCityCountryBinding) : BaseViewHolder<CityCountry>(binding.root) {
        override fun onBind(item: CityCountry, position: Int) {
            binding.apply {
                tvCountry.text = item.country.getSafe()
                tvAddress.text = item.city.getSafe()
            }
        }
    }
}