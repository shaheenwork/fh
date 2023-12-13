package com.shn.fh

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.viewpager.widget.ViewPager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityMainBinding
import com.shn.fh.models.Location
import com.shn.fh.models.Post
import com.shn.fh.posts.AddNewPostActivity
import com.shn.fh.posts.PostsFragment
import com.shn.fh.spots.SpotsFragment
import com.shn.fh.utils.Consts
import com.shn.fh.utils.PrefManager
import com.shn.fh.utils.Utils


class MainActivity : AppCompatActivity() {

    private val locationReqId: Int = 1
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseReference: FirebaseReference

    private lateinit var userId:String

    private lateinit var locationsList: ArrayList<Location>
    private var selectedTab: Int = Consts.TAB_POSTS


    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView

    private var isLoading = false
    private var isLastPage = false
    private  var lastPostId:String = ""

    private val postsPerPage = 3
    private var currentPage = 1
    private lateinit var selectedLocation: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = PrefManager.getUserId()

        binding.addBtn.setOnClickListener {

            val intent = Intent(this, AddNewPostActivity::class.java)
            intent.putExtra(Consts.KEY_LOCATION,selectedLocation)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_down)







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
        postAdapter = PostAdapter(this,userId)

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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationReqId -> {
                if (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Utils.getLatLong(this@MainActivity) { latitude, longitude ->
                        Toast.makeText(
                            this@MainActivity, "loc: $latitude:: $longitude", Toast.LENGTH_LONG
                        ).show()

                        Toast.makeText(
                            this@MainActivity,
                            Utils.getCityName(latitude, longitude, this@MainActivity),
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
                    locationsList.add(place.getValue(Location::class.java)!!)
                }
                setupPostsRecyclerView()
                setupLocationSpinner()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
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
        lastPostId=""
        loadPostIDsOfLocation()
    }

    private fun loadPostIDsOfLocation() {
        if (isLoading || isLastPage) {
            return
        }

        isLoading = true
        val databaseReference = firebaseReference.getLocationsRef().child(selectedLocation).child(Consts.KEY_POSTS)
        // Modify the query based on whether lastPostId is empty
        val query = if (lastPostId.isNotEmpty()) {
            databaseReference.orderByKey().startAfter(lastPostId).limitToFirst(postsPerPage)
        } else {
            databaseReference.limitToFirst(postsPerPage)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newPosts = mutableListOf<Post>()

                for (postSnapshot in dataSnapshot.children) {
                    val postId = postSnapshot.key.toString()

                    // Load post content
                    val postDatabaseReference = firebaseReference.getPostsRef().child(postId)
                    postDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val post = Post()
                            post.postId = postId
                            post.comments = snapshot.child(Consts.KEY_COMMENTS).value.toString().toInt()
                            post.likes = snapshot.child(Consts.KEY_LIKES).value.toString().toInt()
                            post.description = snapshot.child(Consts.KEY_DESCRIPTION).value.toString()
                            post.userId = snapshot.child(Consts.KEY_USER_ID).value.toString()

                            // Retrieve photo URLs as a list
                            val photoUrlsList = mutableListOf<String>()
                            for (photoSnapshot in snapshot.child(Consts.KEY_PHOTO_URLS).children) {
                                val photoUrl = photoSnapshot.value.toString()
                                photoUrlsList.add(photoUrl)
                            }
                            post.photoURLs = photoUrlsList

                            newPosts.add(post)

                            // Check if all posts have been processed
                            if (newPosts.size == dataSnapshot.childrenCount.toInt()) {
                                postAdapter.addPosts(newPosts)

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


    private fun loadMorePosts() {
        // Increment currentPage after loading more posts
        loadPostIDsOfLocation()
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
}
