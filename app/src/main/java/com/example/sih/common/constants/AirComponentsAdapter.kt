package com.example.sih.common.constants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sih.model.Aqi
import com.example.sih.R
class AirComponentsAdapter(private val airComponents: List<Pair<String, String>>) :
    RecyclerView.Adapter<AirComponentsAdapter.AirComponentViewHolder>() {

    inner class AirComponentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val componentName: TextView = view.findViewById(R.id.tvItemTitle)
        val componentValue: TextView = view.findViewById(R.id.tvItemValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AirComponentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.aqi_item_layout, parent, false)
        return AirComponentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AirComponentViewHolder, position: Int) {
        val component = airComponents[position]
        holder.componentName.text = component.first
        holder.componentValue.text = component.second
    }

    override fun getItemCount(): Int = airComponents.size
}
