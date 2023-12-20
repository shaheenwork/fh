package com.shn.fh.posts

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import java.util.*

class AddNewPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewPostBinding
    private lateinit var firebaseReference: FirebaseReference
    private lateinit var postsdatabaseReference: DatabaseReference
    private lateinit var locationdatabaseReference: DatabaseReference
    private lateinit var usersdatabaseReference: DatabaseReference
    private lateinit var selectedLocation: String
    private lateinit var actualImage: File

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    private var user_lat: Double = 0.0
    private var user_lngt: Double = 0.0


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

    private fun getFileName(): String {

        return PrefManager.getUserId() + "_" + System.currentTimeMillis()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        selectedLocation = intent.getStringExtra(Consts.KEY_LOCATION)!!

        PrefManager.getInstance(this)

        firebaseReference = FirebaseReference()
        postsdatabaseReference = firebaseReference.getPostsRef()
        locationdatabaseReference = firebaseReference.getLocationsRef()
        usersdatabaseReference = firebaseReference.getUsersRef()

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


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            ) as List<Address>

                        user_lat = list[0].latitude
                        user_lngt = list[0].longitude

                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun addPost(post: Post) {

        post.timestamp = System.currentTimeMillis()
        post.lat = user_lat
        post.longt = user_lngt
        postsdatabaseReference.child(post.postId).setValue(post)
        usersdatabaseReference.child(post.userId).child(Consts.KEY_POSTS).child(post.postId).setValue(true)
        locationdatabaseReference.child(selectedLocation).child(Consts.KEY_POSTS).child(post.postId)
            .setValue(true)
        Toast.makeText(this, "post added", Toast.LENGTH_LONG).show()

    }
}
