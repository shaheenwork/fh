package com.shn.fh.posts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shn.fh.utils.FileUtil
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivityAddNewPostBinding
import com.shn.fh.posts.models.Post
import com.shn.fh.utils.Consts
import com.shn.fh.utils.PrefManager
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import java.io.File

class AddNewPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewPostBinding
    private lateinit var firebaseReference: FirebaseReference
    private lateinit var postsdatabaseReference: DatabaseReference
    private lateinit var locationdatabaseReference: DatabaseReference
    private lateinit var selectedLocation: String
    private lateinit var actualImage: File


    //photo
    private var storageRef: StorageReference? = null
    var imageUri: Uri? = null
    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
    // lambda expression to receive a result back, here we
        // receive single item(photo) on selection
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                // getting URI of selected Image
                imageUri = result.data?.data
                actualImage = FileUtil.from(this, result.data?.data)


                binding.image1.visibility = View.VISIBLE
                binding.BTNPhoto1.visibility = View.GONE

                Glide.with(this@AddNewPostActivity)
                    .load(imageUri)
                    .into(binding.image1)
            }
        }

    private fun uploadPhoto(imageUri: Uri?, post: Post) {
        // extract the file name with extension
        val sd = getFileName()
        lifecycleScope.launch {
            val compressedImageFile = Compressor.compress(this@AddNewPostActivity, actualImage)


            // Upload Task with upload to directory 'file'
            // and name of the file remains same
            val uploadTask = storageRef!!.child("$sd").putFile(Uri.fromFile(compressedImageFile))

            // On success, download the file URL and display it
            uploadTask.addOnSuccessListener {

                (storageRef!!.child(sd!!).downloadUrl
                    .addOnSuccessListener { uri -> //   Toast.makeText(Inchat.this,uri.toString(),Toast.LENGTH_LONG).show();
                        post.photoURLs = arrayOf(uri.toString()).toList()
                        addPost(post)
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

    }

    private fun getFileName(): String? {

        return PrefManager.getUserId() + "_" + System.currentTimeMillis()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedLocation = intent.getStringExtra(Consts.KEY_LOCATION)!!

        PrefManager.getInstance(this)

        firebaseReference = FirebaseReference()
        postsdatabaseReference = firebaseReference.getPostsRef()
        locationdatabaseReference = firebaseReference.getLocationsRef()

        storageRef = FirebaseStorage.getInstance().reference.child("post_photos")


        val galleryIntent = Intent(Intent.ACTION_PICK)
        binding.BTNAddPost.setOnClickListener {

            val post = Post()
            post.postId = postsdatabaseReference.push().key.toString()
            post.description = binding.ETPost.text.toString()
            post.timestamp = System.currentTimeMillis()
            post.userId = PrefManager.getUserId()

            uploadPhoto(imageUri, post)
        }



        binding.BTNPhoto1.setOnClickListener {

            // PICK INTENT picks item from data
            // and returned selected item

            // here item is type of image
            galleryIntent.type = "image/*"
            // ActivityResultLauncher callback
            imagePickerActivityResult.launch(galleryIntent)

        }


    }

    private fun addPost(post: Post) {

        postsdatabaseReference.child(post.postId).setValue(post)
        locationdatabaseReference.child(selectedLocation).child(Consts.KEY_POSTS).child(post.postId)
            .setValue(true)
        Toast.makeText(this, "post added", Toast.LENGTH_LONG).show()

    }
}
