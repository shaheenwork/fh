package com.shn.fh.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityFollowListBinding
import com.shn.fh.posts.adapter.PostAdapter
import com.shn.fh.user.adapter.UsersAdapter
import com.shn.fh.user.model.User
import com.shn.fh.utils.Consts

class FollowListActivity : AppCompatActivity() {
    private var userId: String? = null
    private var followOrFollowing = Consts.FLAG_FOLLOWING
    private lateinit var firebaseReference: FirebaseReference

    private lateinit var adapter: UsersAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding:ActivityFollowListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra(Consts.KEY_USER_ID)
        followOrFollowing = intent.getIntExtra(Consts.KEY_FOLLOWERS_OR_FOLLOWING,Consts.FLAG_FOLLOWING)

        firebaseReference = FirebaseReference()

        getList()



    }

    private fun getList() {
        var databaseRef  = firebaseReference.getUsersRef().child(userId!!)
        databaseRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(mainSnapshot: DataSnapshot) {
                var list:ArrayList<User> = ArrayList()


                for (userSnap in mainSnapshot.child(Consts.KEY_FOLLOWERS).children) {


                    var userID: String = userSnap.key.toString()

                    //get userDetails
                    var userDatabaseRef = firebaseReference.getUsersRef().child(userID)

                    userDatabaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            var user = User(
                                userID,
                                snapshot.child(Consts.KEY_DISPLAY_NAME).value.toString(),
                                snapshot.child(Consts.KEY_PROFILEPIC_URL).value.toString(),
                                snapshot.child(Consts.KEY_USER_BIO).value.toString(),
                                snapshot.child(Consts.KEY_FOLLOWERS).childrenCount.toInt(),
                                snapshot.child(Consts.KEY_FOLLOWING).childrenCount.toInt(),
                            )

                            list.add(user)

                            if (list.size == mainSnapshot.child(Consts.KEY_FOLLOWERS).childrenCount.toInt()) {

                                setupRecyclerView(list)

                            }


                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

                }




            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    private fun setupRecyclerView(list: List<User>) {
        recyclerView = binding.rvUsers
        adapter = UsersAdapter(this, userId!!, list)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        /*recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        })*/
    }

}