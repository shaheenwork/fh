package com.shn.fh.posts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityAddNewPostBinding
import com.shn.fh.models.Post
import com.shn.fh.utils.Consts
import com.shn.fh.utils.PrefManager

class AddNewPostActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddNewPostBinding
    private lateinit var firebaseReference: FirebaseReference
    private lateinit var postsdatabaseReference: DatabaseReference
    private lateinit var locationdatabaseReference: DatabaseReference
    private lateinit var selectedLocation:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedLocation = intent.getStringExtra(Consts.KEY_LOCATION)!!

        PrefManager.getInstance(this)

        firebaseReference = FirebaseReference()
        postsdatabaseReference = firebaseReference.getPostsRef()
        locationdatabaseReference = firebaseReference.getLocationsRef()


        binding.BTNAddPost.setOnClickListener {

            val post = Post()
            post.postId = postsdatabaseReference.push().key.toString()
            post.description = binding.ETPost.text.toString()
            post.timestamp = System.currentTimeMillis()
            post.userId = PrefManager.getUserId()


            addPost(post)
        }


    }

    private fun addPost(post: Post) {

        postsdatabaseReference.child(post.postId).setValue(post)
        locationdatabaseReference.child(selectedLocation).child(Consts.KEY_POSTS).child(post.postId).setValue(true)
        Toast.makeText(this,"post added",Toast.LENGTH_LONG).show()

    }
}
