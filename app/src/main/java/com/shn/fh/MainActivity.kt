package com.shn.fh

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityMainBinding
import com.shn.fh.posts.AddNewPostActivity
import com.shn.fh.posts.PostsFragment
import com.shn.fh.spots.SpotsFragment
import com.shn.fh.utils.Consts
import com.shn.fh.utils.Utils


class MainActivity : AppCompatActivity() {

    private val locationReqId: Int = 1
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseReference: FirebaseReference


    private lateinit var placesList: ArrayList<String>
    private var selectedTab: Int = Consts.TAB_POSTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addBtn.setOnClickListener {
            when (selectedTab) {
                0 -> {
                    val intent = Intent(this, AddNewPostActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_bottom, 0)

                }
                1 -> {
                    Toast.makeText(this, "add spots", Toast.LENGTH_LONG).show()
                }
            }

        }

        firebaseReference = FirebaseReference()

        setSupportActionBar(binding.toolbar)

        setUpTabLayout()


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

    private fun setUpTabLayout() {
        setupViewPager(binding.tabViewpager)


        binding.tabTablayout.setupWithViewPager(binding.tabViewpager)
    }

    // This function is used to add items in arraylist and assign
    // the adapter to view pager
    private fun setupViewPager(viewpager: ViewPager) {
        var adapter = ViewPagerAdapter(supportFragmentManager)

        adapter.addFragment(PostsFragment(), "Posts")
        adapter.addFragment(SpotsFragment(), "Spots")

        // setting adapter to view pager.
        viewpager.setAdapter(adapter)

        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
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
                            this@MainActivity,
                            "loc: $latitude:: $longitude",
                            Toast.LENGTH_LONG
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

    private fun setupSpinner() {
        // Set up spinner
        //   val items = arrayOf("Item 1", "Item 2", "Item 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, placesList)
        binding.placeSpinner.adapter = adapter

        // Handle spinner item selection
        binding.placeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = placesList[position]
                Toast.makeText(this@MainActivity, "Selected: $selectedItem", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected if needed
            }
        }
    }

    private fun getPlacesList() {
        val databaseReference = firebaseReference.getPlacesRef()
        placesList = ArrayList()

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (place in snapshot.children) {
                    placesList.add(place.value.toString())
                }
                setupSpinner()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        )

    }

    class ViewPagerAdapter : FragmentPagerAdapter {

        // objects of arraylist. One is of Fragment type and
        // another one is of String type.*/
        private final var fragmentList1: ArrayList<Fragment> = ArrayList()
        private final var fragmentTitleList1: ArrayList<String> = ArrayList()

        // this is a secondary constructor of ViewPagerAdapter class.
        public constructor(supportFragmentManager: FragmentManager)
                : super(supportFragmentManager)

        // returns which item is selected from arraylist of fragments.
        override fun getItem(position: Int): Fragment {
            return fragmentList1.get(position)
        }

        // returns which item is selected from arraylist of titles.
        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList1.get(position)
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
