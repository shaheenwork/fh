package com.shn.fh.posts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.shn.fh.R
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityPostViewBinding
import com.shn.fh.posts.comments.CommentsActivity
import com.shn.fh.posts.models.Post
import com.shn.fh.utils.Consts
import com.shn.fh.utils.PrefManager
import com.shn.fh.utils.Utils
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class PostViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostViewBinding
    private var postId: String? = null
    private var locationId: String? = null
    private lateinit var firebaseReference: FirebaseReference
    val PAYLOAD_LIKE = "PAYLOAD_LIKE"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PrefManager.getInstance(this)
        postId = intent.getStringExtra(Consts.KEY_POST_ID)
        locationId = intent.getStringExtra(Consts.KEY_LOCATION_ID)
        firebaseReference = FirebaseReference()

        getPost()

    }

    private fun getPost() {
        val databaseRef = firebaseReference.getPostsRef().child(postId!!)
        databaseRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val post = Post()
                post.postId = postId!!
                post.comments =
                    snapshot.child(Consts.KEY_COMMENTS).value.toString()
                        .toInt()
                //  post.likes = snapshot.child(Consts.KEY_LIKES).value.toString().toInt()
                post.description =
                    snapshot.child(Consts.KEY_DESCRIPTION).value.toString()
                post.userId =
                    snapshot.child(Consts.KEY_USER_ID).value.toString()
                post.timestamp =
                    snapshot.child(Consts.KEY_TIMESTAMP).value.toString()
                        .toLong()
                post.lat =
                    snapshot.child(Consts.KEY_LATITUDE).value.toString()
                        .toDouble()
                post.longt =
                    snapshot.child(Consts.KEY_LONGITUDE).value.toString()
                        .toDouble()

                // Retrieve photo URLs as a list
                val photoSlides = ArrayList<CarouselItem>()
                //  val photoUrlsList = mutableListOf<String>()
                for (photoSnapshot in snapshot.child(Consts.KEY_PHOTO_URLS).children) {
                    val photoUrl = photoSnapshot.value.toString()
                    //   photoUrlsList.add(photoUrl)
                    photoSlides.add(CarouselItem(photoUrl, ""))
                }
                post.photoSlides = photoSlides

                // Retrieve liked users as a list
                val likedUsersList = mutableListOf<String>()
                for (likedUsersSnapshot in snapshot.child(Consts.KEY_LIKED_USERS).children) {
                    val userId = likedUsersSnapshot.key.toString()
                    likedUsersList.add(userId)
                }
                post.liked_users = likedUsersList


                //get user details
                val userDatabaseReference =
                    firebaseReference.getUsersRef()
                        .child(post.userId)
                userDatabaseReference.addListenerForSingleValueEvent(
                    object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            post.postmanName =
                                snapshot.child(Consts.KEY_DISPLAY_NAME).value.toString()
                            post.postmanPhoto =
                                snapshot.child(Consts.KEY_PROFILEPIC_URL).value.toString()


                            setPostView(post)


                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setPostView(post: Post) {
        val postIdTextView: TextView = findViewById(R.id.textPost)
        val likeCountTextView: TextView = findViewById(R.id.textLikes)
        val profilePic: ImageView = findViewById(R.id.iv_profile)
        val TV_postmanName: TextView = findViewById(R.id.tv_fullName)
        val TV_timeAgo: TextView = findViewById(R.id.tv_time)
        val commentCountTextView: TextView = findViewById(R.id.textComments)
//      val photoRecyclerView: RecyclerView = findViewById(R.id.recyclerPhotos)
        val imageSlider = findViewById<ImageCarousel>(R.id.image_slider)
        val BTN_Like: LinearLayout = findViewById(R.id.btn_like)
        val likeIcon: ImageView = findViewById(R.id.likeimage)


        postIdTextView.text = post.description
        likeCountTextView.text = (post.liked_users.size).toString() + " likes"
        commentCountTextView.text = (post.comments).toString() + " comments 11 Shares"

        TV_postmanName.text = (post.postmanName)
        TV_timeAgo.text = (Utils
            .getTimeAgo(post.timestamp))
        TV_timeAgo.text = TV_timeAgo.text.toString() + " • " + Utils.getCityName(
            post.lat,
            post.longt,
            this
        )

        Glide.with(this)
            .load(post.postmanPhoto)
            .into(profilePic)

        //like button color change
        if (post.liked_users.isNotEmpty() && post.liked_users.contains(PrefManager.getUserId())) {
            likeIcon.setColorFilter(ContextCompat.getColor(this, R.color.purple_500));
        } else {
            likeIcon.setColorFilter(ContextCompat.getColor(this, R.color.black));
        }

        /*// Set up RecyclerView for photos
        val photoAdapter = PhotoAdapter(post.photoURLs,this) // You need to create a PhotoAdapter
        photoRecyclerView.layoutManager = LinearLayoutManager(itemView.this, LinearLayoutManager.HORIZONTAL, false)
        photoRecyclerView.adapter = photoAdapter
*/
        imageSlider.setData(post.photoSlides)
        imageSlider.showTopShadow = false
        imageSlider.showCaption = false
        imageSlider.showNavigationButtons = false
        imageSlider.autoPlay = false
        /* val builder: Zoomy.Builder = Zoomy.Builder(this as Activity).target(BTN_Like)
         builder.register()*/




        postIdTextView.setOnClickListener {
            val intent = Intent(this, CommentsActivity::class.java)
            intent.putExtra(Consts.KEY_POST_ID, post.postId)

            (this as Activity).startActivity(intent)

            this.overridePendingTransition(R.anim.slide_up, 0)

        }
        BTN_Like.setOnClickListener {
            val liked = post.liked_users.contains(PrefManager.getUserId())

            firebaseReference.getLocationsRef().child(locationId!!).child(Consts.KEY_POSTS)
                .child(postId!!).child(Consts.KEY_POPULARITY).addListenerForSingleValueEvent(object :ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val pop_score = snapshot.value.toString().toDouble()

                        if (liked) {
                            val new = post.liked_users.toMutableList()
                            new.remove(PrefManager.getUserId())
                            post.liked_users = new

                            firebaseReference.getPostsRef().child(postId!!)
                                .child(Consts.KEY_LIKED_USERS)
                                .child(PrefManager.getUserId()).removeValue()

                            firebaseReference.getLocationsRef().child(locationId!!)
                                .child(Consts.KEY_POSTS)
                                .child(postId!!).child(Consts.KEY_POPULARITY)
                                .setValue(pop_score - Consts.SCORE_LIKE)

                        } else {
                            val new = post.liked_users.toMutableList()
                            new.add(PrefManager.getUserId())
                            post.liked_users = new

                            firebaseReference.getPostsRef().child(postId!!)
                                .child(Consts.KEY_LIKED_USERS)
                                .child(PrefManager.getUserId()).setValue(true)

                            firebaseReference.getLocationsRef().child(locationId!!)
                                .child(Consts.KEY_POSTS)
                                .child(postId!!).child(Consts.KEY_POPULARITY)
                                .setValue(pop_score + Consts.SCORE_LIKE)

                        }

                        //update ui
                        if (post.liked_users.contains(PrefManager.getUserId())) {
                            likeIcon.setColorFilter(
                                ContextCompat.getColor(
                                    this@PostViewActivity,
                                    R.color.purple_500
                                )
                            );
                        } else {
                            likeIcon.setColorFilter(ContextCompat.getColor(this@PostViewActivity, R.color.black));
                        }
                        likeCountTextView.text = (post.liked_users.size).toString() + " likes"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

        }

        profilePic.setOnClickListener {
            // profileClickListener.onProfileClick(post.userId)
        }
    }
}