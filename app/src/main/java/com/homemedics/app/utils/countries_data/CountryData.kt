package com.homemedics.app.utils.countries_data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream

class Country {
    val states: List<State>? = null
    val id: String? = null
    val name: String? = null
    val iso2: String? = null
    val iso3: String? = null
    val phone_code: String? = null
    val capital: String? = null

    fun getStateByName(name: String?): State? {
        return states?.find { it.name == name }
    }
    fun getStateById(id: String?): State? {
        return states?.find { it.id == id }
    }
    fun getAllCities(): List<City>{
        val list = ArrayList<City>()
        states?.forEach { it.cities?.let { it1 -> list.addAll(it1) } }
        return list
    }
}

class State {
    val cities: List<City>? = null
    val id: String? = null
    val name: String? = null

    fun getCityByName(name: String): String? {
        return cities?.find { it.name == name }?.name
    }
    fun getCityById(id: String): String? {
        return cities?.find { it.id == id }?.name
    }
}

class City {
    val id: String? = null
    val name: String? = null
}

class CountryDataProvider(context: Context) {
    private var countries: List<Country>

    init {
        val json: String? = try {
            val `is`: InputStream = context.assets.open("merged_countries.json")
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer)
        } catch (ex: IOException) {
            ex.printStackTrace()
            "[]"
        }

        val type = object : TypeToken<ArrayList<Country>>() {}.type
        countries = Gson().fromJson(json, type)
    }

    fun getCountryByName(name: String?): Country? {
        return countries.find { it.name == name }
    }

    fun getAllCountries(): List<Country> {
        return countries
    }
}