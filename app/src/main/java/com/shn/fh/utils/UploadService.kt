package com.shn.fh.utils

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.posts.models.Post
import java.io.File

class UploadService : Service() {
    private var user_lat: Double = 0.0
    private var user_lngt: Double = 0.0
    private lateinit var firebaseReference: FirebaseReference
    private lateinit var postsdatabaseReference: DatabaseReference
    private lateinit var locationdatabaseReference: DatabaseReference
    private lateinit var usersdatabaseReference: DatabaseReference
    private var storageRef: StorageReference? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val post = intent?.getParcelableExtra<Post>("post")
        val file = intent?.getSerializableExtra("file") as File
        val selectedLocation = intent.getStringExtra("selectedLocation")

        if (post != null) {
            firebaseReference = FirebaseReference()
            postsdatabaseReference = firebaseReference.getPostsRef()
            locationdatabaseReference = firebaseReference.getLocationsRef()
            usersdatabaseReference = firebaseReference.getUsersRef()

            storageRef = FirebaseStorage.getInstance().reference.child("post_photos")

            uploadPhoto(post,file,selectedLocation)
        }

        return START_NOT_STICKY
    }

    private fun uploadPhoto(post: Post, file: File, selectedLocation: String?) {
        // extract the file name with extension
        val sd = getFileName()

            val uploadTask = storageRef!!.child("$sd").putFile(Uri.fromFile(file))

            // On success, download the file URL and display it
            uploadTask.addOnSuccessListener {

                (storageRef!!.child(sd!!).downloadUrl
                    .addOnSuccessListener { uri -> //   Toast.makeText(Inchat.this,uri.toString(),Toast.LENGTH_LONG).show();
                        post.photoURLs = arrayOf(uri.toString()).toList()
                        addPost(post,selectedLocation)
                    }.addOnFailureListener { // Handle any errors
                        // hideProgressDialog()
                        Toast.makeText(
                            applicationContext,
                            "Something went wrong",
                            Toast.LENGTH_LONG
                        ).show()
                    })
            }.addOnFailureListener {
                Log.e("Firebase", "Image Upload fail")
            }


    }

    private fun getFileName(): String {

        return PrefManager.getUserId() + "_" + System.currentTimeMillis()

    }
    private fun addPost(post: Post, selectedLocation: String?) {

        post.timestamp = System.currentTimeMillis()
        post.lat = user_lat
        post.longt = user_lngt
        postsdatabaseReference.child(post.postId).setValue(post)
        usersdatabaseReference.child(post.userId).child(Consts.KEY_POSTS).child(post.postId).setValue(true)
        locationdatabaseReference.child(selectedLocation!!).child(Consts.KEY_POSTS).child(post.postId).child(Consts.KEY_TIMESTAMP).setValue(System.currentTimeMillis())
        locationdatabaseReference.child(selectedLocation).child(Consts.KEY_POSTS).child(post.postId).child(Consts.KEY_POPULARITY).setValue(post.popularity)
        Toast.makeText(this, "post added", Toast.LENGTH_LONG).show()

    }
}
