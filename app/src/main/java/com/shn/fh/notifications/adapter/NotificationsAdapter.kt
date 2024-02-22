package com.shn.fh.notifications.adapter

// PostAdapter.kt
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shn.fh.R
import com.shn.fh.notifications.model.GroupedNotification
import com.shn.fh.posts.PostViewActivity
import com.shn.fh.user.UserViewActivity
import com.shn.fh.utils.Consts
import com.shn.fh.utils.Utils


// PostAdapter.kt
class NotificationsAdapter(
    private val context: Context,
    private val userId: String,
    private val notifList: List<GroupedNotification>
) : RecyclerView.Adapter<NotificationsAdapter.PostViewHolder>() {


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
        val notification = notifList[position]

        holder.userName.text = Utils.getNotificationText(notification)

        Glide.with(context)
            .load(notification.users[notification.users.size - 1].userPic)
            .into(holder.profilePic)


        holder.itemView.setOnClickListener {

            if (notification.action == Consts.ACTION_COMMENT || notification.action == Consts.ACTION_LIKE) {
                val intent = Intent(context, PostViewActivity::class.java)
                intent.putExtra(Consts.KEY_POST_ID, notification.postId)
                intent.putExtra(Consts.KEY_LOCATION_ID, notification.locationId)
                (context as Activity).startActivity(intent)
            } else {
                val intent = Intent(context, UserViewActivity::class.java)
                intent.putExtra(
                    Consts.KEY_USER_ID,
                    notification.users[notification.users.size - 1].userId
                )  
                (context as Activity).startActivity(intent)
            }

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
        return notifList.size
    }


}

