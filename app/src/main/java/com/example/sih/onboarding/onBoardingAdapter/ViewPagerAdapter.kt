package com.example.sih.onboarding.onBoardingAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sih.R
import com.example.sih.model.OnBoardingItems

class ViewPagerAdapter(private val context: Context, private val pageContents: List<OnBoardingItems>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.onboarding_pager_layout, parent, false)
        return PagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        val content = pageContents[position]
        holder.titleTV.text = content.titleContent
        holder.imageView.setImageResource(content.imageResId)
    }

    override fun getItemCount(): Int {
        return pageContents.size
    }

    inner class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTV: TextView = itemView.findViewById(R.id.titleTV)
        val imageView: ImageView =itemView.findViewById(R.id.imageView)
    }
}
