package com.shn.fh.user.adapter

// PostAdapter.kt
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shn.fh.R
import com.shn.fh.posts.comments.CommentsActivity
import com.shn.fh.posts.models.Post
import com.shn.fh.user.UserViewActivity
import com.shn.fh.user.model.User
import com.shn.fh.utils.Consts
import com.shn.fh.utils.Utils


// PostAdapter.kt
class UsersAdapter(
    private val context: android.content.Context,
    private val userId: String,
    private val usersList: List<User>
) : RecyclerView.Adapter<UsersAdapter.PostViewHolder>() {

    private val users: List<User> = usersList


    interface OnProfileClickListener {
        fun onProfileClick(userId: String)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.tv_name)
         val profilePic: ImageView = itemView.findViewById(R.id.iv_profile)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_users, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val user = users[position]
        holder.userName.text = user.userName

        Glide.with(context)
            .load(user.userPic)
            .into(holder.profilePic)


        holder.itemView.setOnClickListener {
            val intent = Intent(context, UserViewActivity::class.java)
            intent.putExtra(Consts.KEY_USER_ID, user.userId)
            (context as Activity).startActivity(intent)

        }


       /* holder.profilePic.setOnClickListener {
            profileClickListener.onProfileClick(users[position].userId)
        }*/
    }

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {

          /* for (payload in payloads){
               if (payload == PAYLOAD_LIKE){
                   //like button color change
                   if (users[position].liked_users.isNotEmpty() && users[position].liked_users.contains(userId)) {
                       holder.likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.purple_500));
                   } else {
                       holder.likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.black));
                   }
                   holder.likeCountTextView.text = (users[position].liked_users.size).toString() + " likes"
               }
           }*/


        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }


}

