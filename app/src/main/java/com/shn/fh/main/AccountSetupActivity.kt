package com.shn.fh.main

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivitySetupAccountBinding
import com.shn.fh.posts.PostsActivity
import com.shn.fh.utils.Consts
import com.shn.fh.utils.FileUtil
import com.shn.fh.utils.PrefManager
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import java.io.File

class AccountSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupAccountBinding
    private lateinit var firebaseDatabase: FirebaseReference
    private lateinit var userDatabaseReference: DatabaseReference
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var userId: String
    private lateinit var actualImage: File
    val galleryIntent = Intent(Intent.ACTION_PICK)

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


                Glide.with(this@AccountSetupActivity)
                    .load(imageUri)
                    .into(binding.ivProfile)
            }
        }

    private fun uploadPhoto(imageUri: Uri?) {
        // extract the file name with extension
        val sd = getFileName()
        lifecycleScope.launch {
            val compressedImageFile = Compressor.compress(this@AccountSetupActivity, actualImage)


            // Upload Task with upload to directory 'file'
            // and name of the file remains same
            val uploadTask = storageRef!!.child("$sd").putFile(Uri.fromFile(compressedImageFile))

            // On success, download the file URL and display it
            uploadTask.addOnSuccessListener {

                (storageRef!!.child(sd!!).downloadUrl
                    .addOnSuccessListener { uri ->

                        // set in r db

                        signUpUser(uri)
                        PrefManager.setIsLogin(true)
                        proceedToMainActivity()


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

    private fun getFileName(): String {

        return PrefManager.getUserId() + "_" + System.currentTimeMillis()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageRef = FirebaseStorage.getInstance().reference.child("profile_pics")


        name = intent.getStringExtra(Consts.KEY_DISPLAY_NAME)!!
        email = intent.getStringExtra(Consts.KEY_EMAIL)!!
      //  photoUrl = intent.getStringExtra(Consts.KEY_PHOTO_URL)!!
        userId = intent.getStringExtra(Consts.KEY_USER_ID)!!

        binding.etName.setText(name)

        firebaseDatabase = FirebaseReference()
        userDatabaseReference = firebaseDatabase.getUsersRef()


        binding.btnSave.setOnClickListener {
            if (binding.etName.text.toString().isNotEmpty()) {

                uploadPhoto(imageUri)


            }
        }


        binding.ivProfile.setOnClickListener {
            galleryIntent.type = "image/*"
            // ActivityResultLauncher callback
            imagePickerActivityResult.launch(galleryIntent)
        }

    }

    private fun signUpUser(uri: Uri) {
        userDatabaseReference.child(userId).child(Consts.KEY_EMAIL).setValue(email)
        userDatabaseReference.child(userId).child(Consts.KEY_DISPLAY_NAME).setValue(name)
        userDatabaseReference.child(userId).child(Consts.KEY_PROFILEPIC_URL).setValue(uri.toString())

    }

    private fun proceedToMainActivity() {
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
        finish()
    }
}