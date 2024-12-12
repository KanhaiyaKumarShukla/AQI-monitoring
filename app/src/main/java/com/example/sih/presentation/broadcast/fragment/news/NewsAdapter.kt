package com.example.sih.presentation.broadcast.fragment.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sih.R

class BlogAdapter(private val blogs: List<Blog>, private val onClick: (Blog) -> Unit) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    class BlogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.blogTitle)
        val image: ImageView = view.findViewById(R.id.blogImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.news_items, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blog = blogs[position]
        holder.title.text = blog.title
        Glide.with(holder.image.context)
            .load(blog.imageUrl ?: R.drawable.aqi_alert)
            .into(holder.image)
        holder.itemView.setOnClickListener { onClick(blog) }
    }

    override fun getItemCount(): Int = blogs.size
}
