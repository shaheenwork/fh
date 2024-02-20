package com.shn.fh.posts

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.shn.fh.R
import com.shn.fh.user.UserViewActivity
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityMainBinding
import com.shn.fh.notifications.NotificationsActivity
import com.shn.fh.posts.models.Location
import com.shn.fh.posts.models.Post
import com.shn.fh.posts.adapter.PostAdapter
import com.shn.fh.utils.Consts
import com.shn.fh.utils.PrefManager
import com.shn.fh.utils.Utils
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem


class PostsActivity : AppCompatActivity(), PostAdapter.OnLikeClickListener,
    PostAdapter.OnProfileClickListener {

    private val locationReqId: Int = 1
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseReference: FirebaseReference

    private lateinit var userId: String

    private lateinit var locationsList: ArrayList<Location>
    private var selectedTab: Int = Consts.TAB_POSTS


    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView

    private var isLoading = false
    private var isLastPage = false
    private var lastPostId: String? = null
    private var lastPostPopularity: Double = 0.0
    private var lastPostTimestamp: Double = 0.0

    private val postsPerPage = 3
    private var currentPage = 1
    private lateinit var selectedLocation: String

    private var folowingPostOnlyFlag: Boolean = false
    private var sortOrder: Int = Consts.SORT_TIME
    private lateinit var usersFollowingUsers: List<String?>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PrefManager.getInstance(this)

        userId = PrefManager.getUserId()
        usersFollowingUsers = ArrayList()

        binding.btnSearch.setOnClickListener {

            folowingPostOnlyFlag = true

            postAdapter.clearPosts()
            currentPage = 1
            isLastPage = false
            isLoading = false
            lastPostId = null
            loadPostIDsOfLocation(folowingPostOnlyFlag)
        }

        binding.addBtn.setOnClickListener {

            val intent = Intent(this, NotificationsActivity::class.java)
            intent.putExtra(Consts.KEY_USER_ID, PrefManager.getUserId())
            startActivity(intent)
/*
 val intent = Intent(this, AddNewPostActivity::class.java)
            intent.putExtra(Consts.KEY_LOCATION, selectedLocation)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_down)
*/


            /* when (selectedTab) {
                 0 -> {
                     val intent = Intent(this, AddNewPostActivity::class.java)
                     startActivity(intent)
                     overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_down)

                 }
                 1 -> {
                     val intent = Intent(this, AddNewSpotsActivity::class.java)
                     startActivity(intent)
                     overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_down)

                 }
             }*/

        }

        firebaseReference = FirebaseReference()

        setSupportActionBar(binding.toolbar)

        /* setUpTabLayout()
 */

        // ask for location permission
        if (!Utils.checkIfAccessFineLocationGranted(this)) {
            Utils.requestLocationPermission(
                this, locationReqId
            )
        } else {

            //setupSpinner()
            getPlacesList()

            Toast.makeText(this, Utils.getCityName(10.79185, 76.19365, this), Toast.LENGTH_LONG)
                .show()
        }


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

    /*  private fun setUpTabLayout() {
          setupViewPager(binding.tabViewpager)


          binding.tabTablayout.setupWithViewPager(binding.tabViewpager)
      }*/

    // This function is used to add items in arraylist and assign
    // the adapter to view pager
/*
    private fun setupViewPager(viewpager: ViewPager) {
        var adapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(PostsFragment(), "Posts")
        adapter.addFragment(SpotsFragment(), "Spots")

        // setting adapter to view pager.
        viewpager.adapter = adapter

        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                // This method is called when the ViewPager is scrolled.
            }

            override fun onPageSelected(position: Int) {

                when (position) {
                    0 -> {
                        selectedTab = Consts.TAB_POSTS
                    }
                    1 -> {
                        selectedTab = Consts.TAB_SPOTS
                    }

                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Called when the scroll state changes.
            }
        })
    }
*/

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationReqId -> {
                if (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Utils.getLatLong(this@PostsActivity) { latitude, longitude ->
                        Toast.makeText(
                            this@PostsActivity, "loc: $latitude:: $longitude", Toast.LENGTH_LONG
                        ).show()

                        Toast.makeText(
                            this@PostsActivity,
                            Utils.getCityName(latitude, longitude, this@PostsActivity),
                            Toast.LENGTH_LONG
                        ).show()

                    }


                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.msg_location_permission_required),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupLocationSpinner() {
        // Set up spinner
        var locationNames: ArrayList<String> = ArrayList()

        for (location in locationsList) {
            locationNames.add(location.name)
        }
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locationNames)
        binding.placeSpinner.adapter = adapter

        // Handle spinner item selection
        binding.placeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedLocation = locationsList[position].name
                onLocationChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected if needed
            }
        }
    }

    private fun getPlacesList() {
        val databaseReference = firebaseReference.getLocationsRef()
        locationsList = ArrayList()

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (place in snapshot.children) {
                    locationsList.add(Location(place.key.toString()))
                }
                setupPostsRecyclerView()
                setupLocationSpinner()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@PostsActivity,
                    getString(R.string.msg_something_wrong),
                    Toast.LENGTH_LONG
                ).show()
            }

        }

        )

    }


    // Call this function when the location changes
    private fun onLocationChanged() {
        // Reset adapter and load posts for the new location
        postAdapter.clearPosts()
        currentPage = 1
        isLastPage = false
        isLoading = false
        lastPostId = null
        loadPostIDsOfLocation(folowingPostOnlyFlag)
    }

    private fun loadPostIDsOfLocation(followingPostsOnly: Boolean) {
        if (isLoading || isLastPage) {
            return
        }

        isLoading = true

        if (followingPostsOnly) {

            if (usersFollowingUsers.isNotEmpty()) {
                //we already have list of users this user follows

                getPostsFromFollowingUsers()

            } else {

                val database = firebaseReference.getUsersRef()

                val followingRef =
                    database.child(PrefManager.getUserId()).child(Consts.KEY_FOLLOWING)

                followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // List to store user IDs that the current user is following
                        usersFollowingUsers = snapshot.children.map { it.key }.toList()
                        if (usersFollowingUsers.isEmpty()) {
                            return
                        } else {
                            getPostsFromFollowingUsers()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
        } else {

            val databaseReference = firebaseReference.getLocationsRef().child(selectedLocation)
                .child(Consts.KEY_POSTS)
            // Modify the query based on whether lastPostId is empty

            var query: Query
            when (sortOrder) {
                Consts.SORT_POPULARITY -> {
                    query = if (lastPostId != null && lastPostId!!.isNotEmpty()) {
                        databaseReference.orderByChild(Consts.KEY_POPULARITY)
                            .startAfter(lastPostPopularity).limitToFirst(postsPerPage)
                    } else {
                        databaseReference.orderByChild(Consts.KEY_POPULARITY)
                            .limitToFirst(postsPerPage)
                    }
                }
                Consts.SORT_TIME -> {
                    query = if (lastPostId != null && lastPostId!!.isNotEmpty()) {
                        databaseReference.orderByChild(Consts.KEY_TIMESTAMP)
                            .startAfter(lastPostTimestamp).limitToFirst(postsPerPage)
                    } else {
                        databaseReference.orderByChild(Consts.KEY_TIMESTAMP)
                            .limitToFirst(postsPerPage)
                    }
                }
                else -> {
                    //default - popularity

                    query = if (lastPostId != null && lastPostId!!.isNotEmpty()) {
                        databaseReference.orderByChild(Consts.KEY_POPULARITY)
                            .startAfter(lastPostPopularity).limitToFirst(postsPerPage)
                    } else {
                        databaseReference.orderByChild(Consts.KEY_POPULARITY)
                            .limitToFirst(postsPerPage)
                    }
                }
            }


            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newPosts = mutableListOf<Post>()
                    var notIncludedPostCount = 0 //search

                    for (postSnapshot in dataSnapshot.children) {
                        val postId = postSnapshot.key.toString()

                        lastPostId = postId
                        lastPostPopularity =
                            postSnapshot.child(Consts.KEY_POPULARITY).value.toString().toDouble()
                        lastPostTimestamp =
                            postSnapshot.child(Consts.KEY_TIMESTAMP).value.toString().toDouble()
                        Log.d("shnlog", "last post id 1: " + lastPostId)

                        // Load post content
                        val postDatabaseReference = firebaseReference.getPostsRef().child(postId)
                        postDatabaseReference.addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Skip the post if it doesn't match the search term
                                if (binding.etSearch.text.toString().isNotEmpty() &&
                                    !snapshot.child(Consts.KEY_DESCRIPTION).value.toString()
                                        .contains(
                                            binding.etSearch.text.toString(),
                                            ignoreCase = true
                                        )
                                ) {
                                    notIncludedPostCount++
                                    if (newPosts.size == (dataSnapshot.childrenCount.toInt() - notIncludedPostCount)) {
                                        postAdapter.addPosts(newPosts, true)

                                        isLoading = false

                                        if (newPosts.size < postsPerPage) {
                                            isLastPage = false
                                            loadMorePosts()
                                        }
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
                                post.locationId =
                                    snapshot.child(Consts.KEY_LOCATION_ID).value.toString()
                                post.userId = snapshot.child(Consts.KEY_USER_ID).value.toString()
                                post.timestamp =
                                    snapshot.child(Consts.KEY_TIMESTAMP).value.toString().toLong()
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


                                //get user details
                                val userDatabaseReference =
                                    firebaseReference.getUsersRef().child(post.userId)
                                userDatabaseReference.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        post.postmanName =
                                            snapshot.child(Consts.KEY_DISPLAY_NAME).value.toString()
                                        post.postmanPhoto =
                                            snapshot.child(Consts.KEY_PROFILEPIC_URL).value.toString()


                                        newPosts.add(post)

                                        // Check if all posts have been processed
                                        if (newPosts.size == (dataSnapshot.childrenCount.toInt() - notIncludedPostCount)) {
                                            postAdapter.addPosts(newPosts, true)

                                            if (newPosts.size < postsPerPage) {
                                                isLastPage = true
                                            }

                                            isLoading = false
                                        }


                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })


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
    }

    private fun getPostsFromFollowingUsers() {
        val databaseReference =
            firebaseReference.getLocationsRef().child(selectedLocation)
                .child(Consts.KEY_POSTS)
        // Modify the query based on whether lastPostId is empty
        val query = if (lastPostId != null && lastPostId!!.isNotEmpty()) {
            databaseReference.orderByKey().startAfter(lastPostId)
                .limitToFirst(postsPerPage)
        } else {
            databaseReference.limitToFirst(postsPerPage)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newPosts = mutableListOf<Post>()
                var notIncludedPostCount = 0 //search

                for (postSnapshot in dataSnapshot.children) {
                    val postId = postSnapshot.key.toString()

                    lastPostId = postId

                    // Load post content
                    val postDatabaseReference =
                        firebaseReference.getPostsRef().child(postId)
                    postDatabaseReference.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // Skip the post if it doesn't match the search term
                            if (!usersFollowingUsers.contains(
                                    snapshot.child(
                                        Consts.KEY_USER_ID
                                    ).value
                                ) || (binding.etSearch.text.toString()
                                    .isNotEmpty() &&
                                        !snapshot.child(Consts.KEY_DESCRIPTION).value.toString()
                                            .contains(
                                                binding.etSearch.text.toString(),
                                                ignoreCase = true
                                            ))
                            ) {
                                notIncludedPostCount++
                                if (newPosts.size == (dataSnapshot.childrenCount.toInt() - notIncludedPostCount)) {
                                    postAdapter.addPosts(newPosts, true)

                                    isLoading = false

                                    if (newPosts.size < postsPerPage) {
                                        isLastPage = false
                                        loadMorePosts()
                                    }
                                }
                                return
                            }

                            val post = Post()
                            post.postId = postId
                            post.comments =
                                snapshot.child(Consts.KEY_COMMENTS).value.toString()
                                    .toInt()
                            //  post.likes = snapshot.child(Consts.KEY_LIKES).value.toString().toInt()
                            post.description =
                                snapshot.child(Consts.KEY_DESCRIPTION).value.toString()
                            post.userId =
                                snapshot.child(Consts.KEY_USER_ID).value.toString()
                            post.locationId =
                                snapshot.child(Consts.KEY_LOCATION_ID).value.toString()
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


                                        newPosts.add(post)

                                        // Check if all posts have been processed
                                        if (newPosts.size == (dataSnapshot.childrenCount.toInt() - notIncludedPostCount)) {
                                            postAdapter.addPosts(newPosts, true)

                                            if (newPosts.size < postsPerPage) {
                                                isLastPage = true
                                            }

                                            // Update lastPostId only after processing all posts
                                            lastPostId =
                                                newPosts[newPosts.size - 1].postId

                                            //  Log.d("shnlog","last post id 2: "+lastPostId)

                                            isLoading = false
                                        }


                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })


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


    private fun loadMorePosts() {
        // Increment currentPage after loading more posts
        loadPostIDsOfLocation(folowingPostOnlyFlag)
        currentPage++
    }


    class ViewPagerAdapter : FragmentPagerAdapter {

        // objects of arraylist. One is of Fragment type and
        // another one is of String type.*/
        private final var fragmentList1: ArrayList<Fragment> = ArrayList()
        private final var fragmentTitleList1: ArrayList<String> = ArrayList()

        // this is a secondary constructor of ViewPagerAdapter class.
        public constructor(supportFragmentManager: FragmentManager) : super(supportFragmentManager)

        // returns which item is selected from arraylist of fragments.
        override fun getItem(position: Int): Fragment {
            return fragmentList1[position]
        }

        // returns which item is selected from arraylist of titles.
        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList1[position]
        }

        // returns the number of items present in arraylist.
        override fun getCount(): Int {
            return fragmentList1.size
        }

        // this function adds the fragment and title in 2 separate  arraylist.
        fun addFragment(fragment: Fragment, title: String) {
            fragmentList1.add(fragment)
            fragmentTitleList1.add(title)
        }
    }

    override fun onLikeClick(locationId: String, postId: String, liked: Boolean) {
        //   incrementLikeCount(postId)
        // update popularity score

            firebaseReference.getLocationsRef().child(locationId!!).child(Consts.KEY_POSTS)
                .child(postId!!).child(Consts.KEY_POPULARITY).addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val pop_score = snapshot.value.toString().toDouble()
                        if (!liked) {
                            firebaseReference.getPostsRef().child(postId).child(Consts.KEY_LIKED_USERS)
                                .child(PrefManager.getUserId()).setValue(true)

                            firebaseReference.getLocationsRef().child(locationId).child(Consts.KEY_POSTS)
                                .child(postId).child(Consts.KEY_POPULARITY).setValue(pop_score + Consts.SCORE_LIKE)
                        } else {
                            firebaseReference.getPostsRef().child(postId).child(Consts.KEY_LIKED_USERS)
                                .child(PrefManager.getUserId()).removeValue()

                            firebaseReference.getLocationsRef().child(locationId).child(Consts.KEY_POSTS)
                                .child(postId).child(Consts.KEY_POPULARITY).setValue(pop_score - Consts.SCORE_LIKE)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })



    }

    override fun onProfileClick(userId: String) {
        val intent = Intent(this, UserViewActivity::class.java)
        intent.putExtra(Consts.KEY_USER_ID, userId)
        startActivity(intent)

    }
}
