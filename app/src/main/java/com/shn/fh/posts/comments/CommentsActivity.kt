package com.shn.fh.posts.comments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityCommentsBinding
import com.shn.fh.posts.models.Comment
import com.shn.fh.posts.models.Post
import com.shn.fh.utils.Consts
import com.shn.fh.utils.PrefManager

class CommentsActivity : AppCompatActivity() {
    lateinit var postID: String
    private var isLoading = false
    private var isLastPage = false
    private  var lastCommentId:String = ""
    private lateinit var adapter: CommentsAdapter


    private val commentsPerPage = 3
    private var currentPage = 1
    private lateinit var firebaseReference: FirebaseReference
    private lateinit var commentsDatabaseReference : DatabaseReference
    private lateinit var binding:ActivityCommentsBinding
    private lateinit var comments:ArrayList<Comment>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseReference = FirebaseReference()
        PrefManager.getInstance(this)

        postID = intent.getStringExtra(Consts.KEY_POST_ID)!!
        commentsDatabaseReference = firebaseReference.getCommentsRef(postID)

        setupPostsRecyclerView()
        getComments()

        binding.BTNPostComment.setOnClickListener {
            val comment = binding.ETComment.text.toString()
            if (comment.isNotEmpty()){
                addComment(comment)
            }

        }



    }

    private fun addComment(comment: String) {
        val key = commentsDatabaseReference.push().key
        commentsDatabaseReference.child(key!!).child(Consts.KEY_COMMENT_ID).setValue(key)
        commentsDatabaseReference.child(key).child(Consts.KEY_TEXT).setValue(comment)
        commentsDatabaseReference.child(key).child(Consts.KEY_TIMESTAMP).setValue(System.currentTimeMillis())
        commentsDatabaseReference.child(key).child(Consts.KEY_USER_ID).setValue(PrefManager.getUserId())

        Toast.makeText(this, "comment added", Toast.LENGTH_LONG).show()


        //load comments
        adapter.clearComments()
        currentPage = 1
        isLastPage = false
        isLoading = false
        lastCommentId=""
        incrementCommentsCount(postID)
        getComments()

    }

    fun incrementCommentsCount(postId: String) {

        firebaseReference.getPostsRef().child(postId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val post = currentData.getValue(Post::class.java)

                // Check if the post exists
                if (post == null) {
                    // Handle error or return Transaction.success(currentData) if you don't want to create the post
                    return Transaction.success(currentData)
                }

                // Increment the likes count
                post.comments = post.comments + 1

                // Set the updated value
                currentData.value = post

                return Transaction.success(currentData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed) {
                    // Like count updated successfully
                } else {
                    // Transaction failed, handle error
                    if (databaseError != null) {
                        // Handle the error, you might want to retry or inform the user
                    }
                }
            }
        })
    }


    private fun getComments() {

        if (isLoading || isLastPage) {
            return
        }

        isLoading = true
        // Modify the query based on whether lastPostId is empty
        val query = if (lastCommentId.isNotEmpty()) {
            commentsDatabaseReference.orderByKey().startAfter(lastCommentId).limitToFirst(commentsPerPage)
        } else {
            commentsDatabaseReference.limitToFirst(commentsPerPage)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newComments = mutableListOf<Comment>()

                for (commentsSnapshot in dataSnapshot.children) {

                    val comment = Comment()
                    comment.commentId = commentsSnapshot.child(Consts.KEY_COMMENT_ID).value.toString()
                    comment.userId = commentsSnapshot.child(Consts.KEY_USER_ID).value.toString()
                    comment.text = commentsSnapshot.child(Consts.KEY_TEXT).value.toString()
                    comment.timestamp = commentsSnapshot.child(Consts.KEY_TIMESTAMP).value.toString().toLong()

                    newComments.add(comment)

                    // Check if all posts have been processed
                    if (newComments.size == dataSnapshot.childrenCount.toInt()) {
                        adapter.addComments(newComments)

                        if (newComments.size < commentsPerPage) {
                            isLastPage = true
                        }

                        // Update lastPostId only after processing all posts
                        lastCommentId = newComments[newComments.size - 1].commentId

                        isLoading = false
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                isLoading = false
                // Handle error
            }
        })




    }
    private fun loadMoreComments() {
        // Increment currentPage after loading more posts
        getComments()
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
                        loadMoreComments()
                    }
                }
            }
        })
    }

}