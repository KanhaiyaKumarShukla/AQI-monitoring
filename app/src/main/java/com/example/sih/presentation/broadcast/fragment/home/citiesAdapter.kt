package com.example.sih.presentation.broadcast.fragment.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sih.R

// MyAdapter.kt
class MyAdapter(private var mData: List<Pair<String, Float>>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = mData[position]
        holder.itemTextView.text = "${item.first}: ${item.second}"
    }

    override fun getItemCount(): Int = mData.size

    // Update the data in the adapter
    fun updateData(newData: List<Pair<String, Float>>) {
        mData = newData
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTextView: TextView = itemView.findViewById(R.id.itemTextView)
    }
}

