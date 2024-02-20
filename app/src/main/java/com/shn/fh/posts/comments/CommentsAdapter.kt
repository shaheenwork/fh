package com.shn.fh.posts.comments

// PostAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.shn.fh.R
import com.shn.fh.posts.models.Comment


// PostAdapter.kt
class CommentsAdapter(private val longClickListener: onLongClickListener) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val comments: MutableList<Comment> = mutableListOf()

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postIdTextView: TextView = itemView.findViewById(R.id.textPost)
    }

    interface onLongClickListener {
        fun onLongClick(locationId: String, postId: String, commentId: String)
    }

    fun clearComments() {
        comments.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.postIdTextView.text = comment.text

       holder.itemView.setOnLongClickListener {

           longClickListener.onLongClick(comment.locationId,comment.postId,comment.commentId)

           return@setOnLongClickListener true
       }

    }

    override fun getItemCount(): Int {
        return comments.size
    }

    fun addComments(newComments: List<Comment>) {
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}

