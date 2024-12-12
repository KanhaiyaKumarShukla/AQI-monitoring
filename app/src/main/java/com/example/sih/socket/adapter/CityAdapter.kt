package com.example.sih.socket.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sih.R
import com.example.sih.socket.models.City

class CityAdapter(private var cityList: List<City>) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    inner class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCityName: TextView = view.findViewById(R.id.tvCityName)
        val tvAirComponents: TextView = view.findViewById(R.id.tvAirComponents)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cityList[position]
        Log.d("TAG", "City: $city")
        holder.tvCityName.text = city.cityName
        val componentsText = city.airComponents.joinToString("\n") {
            "${it.sensorName}: ${it.sensorData} ${it.sensorUnit ?: ""}"
        }
        holder.tvAirComponents.text = componentsText
    }

    override fun getItemCount(): Int {
        return cityList.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateCityList(newCityList: List<City>) {
        cityList = newCityList
        notifyDataSetChanged()  // Notify the adapter to refresh the view
    }
}
