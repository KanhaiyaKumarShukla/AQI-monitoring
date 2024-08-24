package com.example.sih.presentation.broadcast.util

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sih.R
import com.example.sih.databinding.AqiItemLayoutBinding

class ItemViewHolder(private val binding: AqiItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Item) {
        binding.tvItemTitle.text = item.title
        binding.tvItemValue.text = item.value
    }
}
