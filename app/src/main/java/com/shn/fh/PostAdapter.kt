package com.shn.fh

// PostAdapter.kt
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shn.fh.models.Post
import com.shn.fh.utils.Consts


// PostAdapter.kt
class PostAdapter(context: android.content.Context) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts: MutableList<Post> = mutableListOf()
    private val context=context


    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postIdTextView: TextView = itemView.findViewById(R.id.textPost)
        val photoRecyclerView: RecyclerView = itemView.findViewById(R.id.recyclerPhotos)

    }

    fun clearPosts() {
        posts.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postIdTextView.text = post.description

        // Set up RecyclerView for photos
        val photoAdapter = PhotoAdapter(post.photoURLs,context) // You need to create a PhotoAdapter
        holder.photoRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.photoRecyclerView.adapter = photoAdapter

        holder.postIdTextView.setOnClickListener{
            val intent = Intent(context,CommentsActivity::class.java)
            intent.putExtra(Consts.KEY_POST_ID,post.postId)

            (context as Activity).startActivity(intent)

            context.overridePendingTransition(R.anim.slide_up,0)

        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun addPosts(newPosts: List<Post>) {
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }
}

