package com.shn.fh.notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityNotificationsBinding
import com.shn.fh.notifications.model.Notification
import com.shn.fh.notifications.adapter.NotificationsAdapter
import com.shn.fh.user.model.User
import com.shn.fh.utils.Consts

class NotificationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationsBinding
    private var userId: String? = null
    private lateinit var firebaseReference: FirebaseReference
    private lateinit var adapter: NotificationsAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = "userId1"
       // userId = intent.getStringExtra(Consts.KEY_USER_ID)
        firebaseReference = FirebaseReference()


        getNotifications()
    }

    private fun getNotifications() {
        var databaseRef  = firebaseReference.getNotifications().child(userId!!).orderByChild(Consts.KEY_TIMESTAMP)
        databaseRef.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(mainSnapshot: DataSnapshot) {
                var list:ArrayList<Notification> = ArrayList()


                for (dataSnapshot in mainSnapshot.children) {

                    val userID: String = dataSnapshot.child(Consts.KEY_USER_ID).value.toString()
                    val timestamp: Long = dataSnapshot.child(Consts.KEY_TIMESTAMP).value.toString().toLong()
                    val action: Int = dataSnapshot.child(Consts.KEY_ACTION_NOTIFICATION).value.toString().toInt()
                    val readStatus: Int = dataSnapshot.child(Consts.KEY_READ_STATUS).value.toString().toInt()
                    var postId =""
                    var locationId =""
                    if (action!=Consts.ACTION_FOLLOW) {
                         postId = dataSnapshot.child(Consts.KEY_POST_ID).value.toString()
                         locationId = dataSnapshot.child(Consts.KEY_LOCATION_ID).value.toString()
                    }

                    //get userDetails
                    val userDatabaseRef = firebaseReference.getUsersRef().child(userID)

                    userDatabaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            val user = User(
                                userID,
                                snapshot.child(Consts.KEY_DISPLAY_NAME).value.toString(),
                                snapshot.child(Consts.KEY_PROFILEPIC_URL).value.toString(),
                                "snapshot.child(Consts.KEY_USER_BIO).value.toString()",
                                0,
                                0,
                            )

                            val notification = Notification(timestamp,action,user,postId,locationId,readStatus)

                            list.add(notification)

                            if (list.size == mainSnapshot.childrenCount.toInt()) {

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

    private fun setupRecyclerView(list: List<Notification>) {
        recyclerView = binding.rvNotif
        adapter = NotificationsAdapter(this, userId!!, list)

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