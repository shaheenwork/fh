package com.shn.fh.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityUserViewBinding
import com.shn.fh.posts.adapter.PostAdapter
import com.shn.fh.posts.models.Post
import com.shn.fh.utils.Consts
import com.shn.fh.utils.PrefManager
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class UserViewActivity : AppCompatActivity(), PostAdapter.OnLikeClickListener,
    PostAdapter.OnProfileClickListener {
    private lateinit var binding: ActivityUserViewBinding
    private lateinit var userId: String
    private lateinit var myUserId: String

    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView

    private var isLoading = false
    private var isLastPage = false
    private var lastPostId: String = ""

    private val postsPerPage = 30
    private var currentPage = 1

    private lateinit var userName: String
    private lateinit var userBio: String
    private lateinit var userPic: String


    private lateinit var firebaseReference: FirebaseReference
    private lateinit var userDatabaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserViewBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        PrefManager.getInstance(this)

        setContentView(binding.root)

        userId = intent.getStringExtra(Consts.KEY_USER_ID)!!
        myUserId = PrefManager.getUserId()

        firebaseReference = FirebaseReference()
        userDatabaseReference = firebaseReference.getUsersRef().child(userId)


        getUserInfo(userId)


        setupPostsRecyclerView()

        binding.tvFollowersCount.setOnClickListener {
            val intent=Intent(this, FollowListActivity::class.java)
            intent.putExtra(Consts.KEY_FOLLOWERS_OR_FOLLOWING,Consts.FLAG_FOLLOWERS)
            intent.putExtra(Consts.KEY_USER_ID,userId)
            startActivity(intent)
        }

    }

    private fun getUserInfo(userId: String) {

        userDatabaseReference.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                userName = snapshot.child(Consts.KEY_DISPLAY_NAME).value.toString()
                userPic = snapshot.child(Consts.KEY_PROFILEPIC_URL).value.toString()
                userBio = snapshot.child(Consts.KEY_USER_BIO).value.toString()
                val following = snapshot.child(Consts.KEY_FOLLOWERS).child(myUserId).exists()
                val followerCount = snapshot.child(Consts.KEY_FOLLOWERS).childrenCount
                val followCount = snapshot.child(Consts.KEY_FOLLOWING).childrenCount

                binding.tvFollowingCount.text = followCount.toString()
                binding.tvFollowersCount.text = followerCount.toString()

                if (myUserId == userId){
                    binding.btnFollow.visibility = View.GONE
                }
                else{
                    binding.btnFollow.visibility = View.VISIBLE
                    binding.btnFollow.setOnClickListener {
                        if (following) {
                            userDatabaseReference.child(Consts.KEY_FOLLOWERS).child(myUserId).removeValue()
                            firebaseReference.getUsersRef().child(myUserId).child(Consts.KEY_FOLLOWING).child(userId).removeValue()
                        } else {
                            userDatabaseReference.child(Consts.KEY_FOLLOWERS).child(myUserId).setValue(true)
                            firebaseReference.getUsersRef().child(myUserId).child(Consts.KEY_FOLLOWING).child(userId).setValue(true)
                        }
                    }
                }

                if(following){
                    binding.btnFollow.text = "Following"
                }
                else{
                    binding.btnFollow.text = "Follow"
                }

                Glide.with(applicationContext)
                    .load(userPic)
                    .into(binding.ivProfile)

                binding.tvFullName.text = userName
                binding.tvBio.text = userBio


                loadPostIDsOfLocation("")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    private fun setupPostsRecyclerView() {
        recyclerView = binding.rvPosts
        postAdapter = PostAdapter(this, userId, this, this)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = postAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount
                        && firstVisibleItem >= 0
                        && totalItemCount >= postsPerPage
                    ) {
                        loadMorePosts()
                    }
                }
            }
        })
    }

    private fun loadMorePosts() {
        // Increment currentPage after loading more posts
        loadPostIDsOfLocation("")
        currentPage++
    }

    private fun loadPostIDsOfLocation(searchTerm: String) {
        if (isLoading || isLastPage) {
            return
        }

        isLoading = true
        val databaseReference =
            firebaseReference.getUsersRef().child(userId).child(Consts.KEY_POSTS)
        // Modify the query based on whether lastPostId is empty
        val query = if (lastPostId.isNotEmpty()) {
            databaseReference.orderByKey().startAfter(lastPostId).limitToFirst(postsPerPage)
        } else {
            databaseReference.limitToFirst(postsPerPage)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newPosts = mutableListOf<Post>()
                var notIncludedPostCount = 0 //search

                for (postSnapshot in dataSnapshot.children) {
                    val postId = postSnapshot.key.toString()

                    // Load post content
                    val postDatabaseReference = firebaseReference.getPostsRef().child(postId)
                    postDatabaseReference.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // Skip the post if it doesn't match the search term
                            if (searchTerm.isNotEmpty() &&
                                !snapshot.child(Consts.KEY_DESCRIPTION).value.toString()
                                    .contains(searchTerm, ignoreCase = true)
                            ) {
                                notIncludedPostCount++
                                if (newPosts.size == (dataSnapshot.childrenCount.toInt() - notIncludedPostCount)) {
                                    postAdapter.addPosts(newPosts, false)

                                    if (newPosts.size < postsPerPage) {
                                        isLastPage = true
                                    }

                                    // Update lastPostId only after processing all posts
                                    lastPostId = newPosts[newPosts.size - 1].postId

                                    isLoading = false
                                }
                                return
                            }

                            val post = Post()
                            post.postId = postId
                            post.comments =
                                snapshot.child(Consts.KEY_COMMENTS).value.toString().toInt()
                            //  post.likes = snapshot.child(Consts.KEY_LIKES).value.toString().toInt()
                            post.description =
                                snapshot.child(Consts.KEY_DESCRIPTION).value.toString()
                            post.userId = snapshot.child(Consts.KEY_USER_ID).value.toString()
                            post.locationId = snapshot.child(Consts.KEY_LOCATION_ID).value.toString()
                            post.timestamp =
                                snapshot.child(Consts.KEY_TIMESTAMP).value.toString().trim().toLong()
                            post.lat =
                                snapshot.child(Consts.KEY_LATITUDE).value.toString().toDouble()
                            post.longt =
                                snapshot.child(Consts.KEY_LONGITUDE).value.toString().toDouble()

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

                            post.postmanName = userName
                            post.postmanPhoto = userPic

                            newPosts.add(post)

                            if (newPosts.size == (dataSnapshot.childrenCount.toInt() - notIncludedPostCount)) {
                                postAdapter.addPosts(newPosts, false)

                                if (newPosts.size < postsPerPage) {
                                    isLastPage = true
                                }

                                // Update lastPostId only after processing all posts
                                lastPostId = newPosts[newPosts.size - 1].postId

                                isLoading = false
                            }



                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle cancellation
                            isLoading = false
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                isLoading = false
                // Handle error
            }
        })
    }

    override fun onLikeClick(locationId:String, postId: String, liked: Boolean) {
        //   incrementLikeCount(postId)

        firebaseReference.getLocationsRef().child(locationId!!).child(Consts.KEY_POSTS)
            .child(postId!!).child(Consts.KEY_POPULARITY)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pop_score = snapshot.value.toString().toDouble()

                    if (!liked) {
                        firebaseReference.getPostsRef().child(postId).child(Consts.KEY_LIKED_USERS)
                            .child(PrefManager.getUserId()).setValue(true)
                        firebaseReference.getLocationsRef().child(locationId)
                            .child(Consts.KEY_POSTS).child(postId).child(Consts.KEY_POPULARITY)
                            .setValue(pop_score + Consts.SCORE_LIKE)
                    } else {
                        firebaseReference.getPostsRef().child(postId).child(Consts.KEY_LIKED_USERS)
                            .child(PrefManager.getUserId()).removeValue()
                        firebaseReference.getLocationsRef().child(locationId)
                            .child(Consts.KEY_POSTS).child(postId).child(Consts.KEY_POPULARITY)
                            .setValue(pop_score - Consts.SCORE_LIKE)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

                override fun onProfileClick(userId: String) {

    }

}