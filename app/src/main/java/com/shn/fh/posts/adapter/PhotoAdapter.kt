package com.shn.fh.posts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shn.fh.R

class PhotoAdapter(private val photos: List<String>,context: Context) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    val context= context

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        // Use a library like Glide or Picasso to load images into the ImageView
        val photoUrl = photos[position]
        Glide.with(holder.itemView.context)
            .load(photoUrl)
            .into(holder.imageView)

    }

    override fun getItemCount(): Int {
        return photos.size
    }
}
