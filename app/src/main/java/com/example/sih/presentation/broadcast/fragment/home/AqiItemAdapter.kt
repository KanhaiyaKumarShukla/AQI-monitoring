package com.example.sih.presentation.broadcast.fragment.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sih.R
import com.example.sih.model.Aqi
import com.example.sih.socket.models.AirComponent

class AqiItemAdapter(private val aqiData: Aqi) :
    RecyclerView.Adapter<AqiItemAdapter.HorizontalViewHolder>() {

    // Extract the AQI fields into a list for easy iteration
    private val items: List<Pair<String, Pair<Int?, String>>> = aqiData.run {
        listOf(
            "AQI_IN" to (AQI_IN to (units["AQI_IN"] ?: "")),
            "AQI_US" to (AQI_US to (units["AQI_US"] ?: "")),
            "CO" to (CO to (units["CO"] ?: "")),
            "DEW" to (DEW to (units["DEW"] ?: "")),
            "H" to (H to (units["H"] ?: "")),
            "NO2" to (NO2 to (units["NO2"] ?: "")),
            "O3" to (O3 to (units["O3"] ?: "")),
            "P" to (P to (units["P"] ?: "")),
            "PM10" to (PM10 to (units["PM10"] ?: "")),
            "PM25" to (PM25 to (units["PM25"] ?: "")),
            "SO2" to (SO2 to (units["SO2"] ?: "")),
            "T" to (T to (units["T"] ?: "")),
            "W" to (W to (units["W"] ?: ""))
        )
    }

    // ViewHolder for each item
    class HorizontalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
        val itemValue: TextView = itemView.findViewById(R.id.itemValue)
        val itemUnit: TextView = itemView.findViewById(R.id.itemUnit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_aqi_component_layout, parent, false)
        return HorizontalViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HorizontalViewHolder, position: Int) {
        val (title, valueUnitPair) = items[position]
        val (value, unit) = valueUnitPair

        // Set the data in the ViewHolder
        holder.itemTitle.text = if(title=="AQI_IN")"AQI\nIN" else if(title=="AQI_US")"AQI\nUS" else if(title=="PM10")"PM\n10" else if(title=="PM25")"PM\n25" else title
        holder.itemValue.text = value?.toString() ?: "N/A"
        holder.itemUnit.text = unit
    }

    override fun getItemCount(): Int = items.size
}
