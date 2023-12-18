package com.shn.fh.posts.adapter

// PostAdapter.kt
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shn.fh.R
import com.shn.fh.posts.comments.CommentsActivity
import com.shn.fh.posts.models.Post
import com.shn.fh.utils.Consts
import com.shn.fh.utils.Utils
import org.imaginativeworld.whynotimagecarousel.ImageCarousel


// PostAdapter.kt
class PostAdapter(context: android.content.Context, userId:String, private val listener:OnItemClickListener) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val posts: MutableList<Post> = mutableListOf()
    private val context=context
    private val userId = userId

    interface OnItemClickListener {
        fun onItemClick(postId: String, liked: Boolean)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postIdTextView: TextView = itemView.findViewById(R.id.textPost)
        val likeCountTextView: TextView = itemView.findViewById(R.id.textLikes)
        val profilePic: ImageView = itemView.findViewById(R.id.iv_profile)
        val TV_postmanName: TextView = itemView.findViewById(R.id.tv_fullName)
        val TV_timeAgo: TextView = itemView.findViewById(R.id.tv_time)
        val commentCountTextView: TextView = itemView.findViewById(R.id.textComments)
//        val photoRecyclerView: RecyclerView = itemView.findViewById(R.id.recyclerPhotos)
        val imageSlider = itemView.findViewById<ImageCarousel>(R.id.image_slider)
        val BTN_Like: LinearLayout = itemView.findViewById(R.id.btn_like)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeimage)

    }

    fun clearPosts() {
        posts.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postIdTextView.text = post.description
        holder.likeCountTextView.text = (post.liked_users.size).toString() + " likes"
        holder.commentCountTextView.text = (post.comments).toString() + " comments 11 Shares"

        holder.TV_postmanName.text = (post.postmanName)
        holder.TV_timeAgo.text = (Utils
            .getTimeAgo(post.timestamp))
        holder.TV_timeAgo.text = holder.TV_timeAgo.text.toString() + " • "+ Utils.getCityName(post.lat,post.longt,context)

        Glide.with(context)
            .load(post.postmanPhoto)
            .into(holder.profilePic)

        //like button color change
        if (post.liked_users.isNotEmpty() && post.liked_users.contains(userId)){
            holder.likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.purple_500));
        }
        else{
            holder.likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.black));
        }

        /*// Set up RecyclerView for photos
        val photoAdapter = PhotoAdapter(post.photoURLs,context) // You need to create a PhotoAdapter
        holder.photoRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.photoRecyclerView.adapter = photoAdapter
*/
        holder.imageSlider.setData(post.photoSlides)
        holder.imageSlider.showTopShadow = false
        holder.imageSlider.showCaption= false
        holder.imageSlider.showNavigationButtons=false
        holder.imageSlider.autoPlay=false
       /* val builder: Zoomy.Builder = Zoomy.Builder(context as Activity).target(holder.BTN_Like)
        builder.register()*/




        holder.postIdTextView.setOnClickListener{
            val intent = Intent(context, CommentsActivity::class.java)
            intent.putExtra(Consts.KEY_POST_ID,post.postId)

            (context as Activity).startActivity(intent)

            context.overridePendingTransition(R.anim.slide_up,0)

        }
        holder.BTN_Like.setOnClickListener {
            val liked = posts[position].liked_users.contains(userId)
            if (liked){
                val new = posts[position].liked_users.toMutableList()
                new.remove(userId)
                posts[position].liked_users = new
            }
            else{
                val new = posts[position].liked_users.toMutableList()
                new.add(userId)
                posts[position].liked_users = new
            }
            notifyItemChanged(position)
            listener.onItemClick(posts[position].postId,liked)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun addPosts(newPosts: List<Post>) {
        // filter out user's own posts
        val filteredPosts = newPosts.filter { post ->
            post.userId!=userId
        }
        posts.addAll(filteredPosts)
        notifyDataSetChanged()
    }










}

