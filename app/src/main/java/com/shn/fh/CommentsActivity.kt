package com.shn.fh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityCommentsBinding
import com.shn.fh.models.Comment
import com.shn.fh.utils.Consts

class CommentsActivity : AppCompatActivity() {
    lateinit var postID: String
    private var isLoading = false
    private var isLastPage = false
    private  var lastCommentId:String = ""
    private lateinit var adapter: CommentsAdapter


    private val commentsPerPage = 3
    private var currentPage = 1
    private lateinit var firebaseReference: FirebaseReference
    private lateinit var binding:ActivityCommentsBinding
    private lateinit var comments:ArrayList<Comment>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseReference = FirebaseReference()

        postID = intent.getStringExtra(Consts.KEY_POST_ID)!!

        setupPostsRecyclerView()
        getComments(postID)




    }

    private fun getComments(postID: String) {

        comments = ArrayList()

        if (isLoading || isLastPage) {
            return
        }

        isLoading = true
        val databaseReference = firebaseReference.getCommentsRef(postID)
        val query = if (lastCommentId.isNotEmpty()) {
            databaseReference.orderByKey().startAt(lastCommentId).limitToFirst(commentsPerPage)
        } else {
            databaseReference.limitToFirst(commentsPerPage)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newComments = mutableListOf<Comment>()

                for (snapshot in dataSnapshot.children) {
                    val commentID = snapshot.key.toString()
                    lastCommentId = commentID // Update lastPostId for pagination

                    val comment = Comment()
                    comment.commentId = commentID
                    comment.userId = snapshot.child("userId").value.toString()
                    comment.text = snapshot.child("text").value.toString()
                    comment.timestamp = snapshot.child("timestamp").value.toString().toLong()

                    newComments.add(comment)

                }
                if (newComments.size == dataSnapshot.childrenCount.toInt()) {
                    adapter.addComments(newComments)

                    if (newComments.size < commentsPerPage) {
                        isLastPage = true
                    }

                    isLoading = false
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
        getComments(postID)
        currentPage++
    }

    private fun setupPostsRecyclerView() {

        adapter = CommentsAdapter()

        val layoutManager = LinearLayoutManager(this)
        binding.rvComments.layoutManager = layoutManager
        binding.rvComments.adapter = adapter

        binding.rvComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount
                        && firstVisibleItem >= 0
                        && totalItemCount >= commentsPerPage
                    ) {
                        loadMorePosts()
                    }
                }
            }
        })
    }

}