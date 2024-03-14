package com.shn.fh.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.shn.fh.R
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.posts.PostsActivity
import com.shn.fh.posts.models.Post
import com.shn.fh.utils.FileUtil.getFileName
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class UpService : LifecycleService() {

    private lateinit var storageRef: FirebaseStorage
    private lateinit var firebaseReference: FirebaseReference
    private lateinit var postsdatabaseReference: DatabaseReference
    private lateinit var locationdatabaseReference: DatabaseReference
    private lateinit var usersdatabaseReference: DatabaseReference

    override fun onCreate() {
        super.onCreate()
        storageRef = FirebaseStorage.getInstance()
        firebaseReference = FirebaseReference()
        postsdatabaseReference = firebaseReference.getPostsRef()
        locationdatabaseReference = firebaseReference.getLocationsRef()
        usersdatabaseReference = firebaseReference.getUsersRef()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val imagePath = intent?.getStringExtra("imagePath")
        val post = intent?.getParcelableExtra<Post>("post")
        val selectedLocation = intent?.getStringExtra("selectedLocation")
        val lat = intent?.getDoubleExtra("lat",0.0)
        val longt = intent?.getDoubleExtra("longt",0.0)
        if (!imagePath.isNullOrBlank()) {
            val imageUri = Uri.fromFile(File(imagePath))
            startForeground(1, createNotification())
            uploadPhoto(imageUri,post!!,selectedLocation,lat,longt)
        }

        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                ""
            }

        val notificationIntent = Intent(this, PostsActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Uploading Photo")
            .setSmallIcon(R.drawable.facebook_like)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun uploadPhoto(
        imageUri: Uri?,
        post: Post,
        selectedLocation: String?,
        lat: Double?,
        longt: Double?
    ) {
        val sd = getFileName(this,imageUri)
        lifecycleScope.launch(Dispatchers.IO) {
            val compressedImageFile = Compressor.compress(this@UpService, FileUtil.from(this@UpService,imageUri))

            val uploadTask = storageRef.reference.child("$sd").putFile(Uri.fromFile(compressedImageFile))

            uploadTask.addOnSuccessListener {
                storageRef.reference.child(sd).downloadUrl.addOnSuccessListener { uri ->
                    // Handle success
                   addPost(post,selectedLocation,lat,longt)
                    PrefManager.setIsLogin(true)
                    stopForeground(true)
                    stopSelf()
                }.addOnFailureListener {
                    // Handle failure
                    Log.e("Firebase", "Image Upload fail")
                    stopForeground(true)
                    stopSelf()
                }
            }.addOnFailureListener {
                // Handle failure
                Log.e("Firebase", "Image Upload fail")
                stopForeground(true)
                stopSelf()
            }
        }
    }

    /*override fun onBind(intent: Intent?): IBinder? {
        return null
    }*/

    private fun addPost(post: Post?, selectedLocation: String?, lat: Double?, longt: Double?) {

        post!!.timestamp = System.currentTimeMillis()
        post.lat = lat!!
        post.longt = longt!!
        postsdatabaseReference.child(post.postId).setValue(post)
        usersdatabaseReference.child(post.userId).child(Consts.KEY_POSTS).child(post.postId).setValue(true)
        locationdatabaseReference.child(selectedLocation!!).child(Consts.KEY_POSTS).child(post.postId).child(Consts.KEY_TIMESTAMP).setValue(System.currentTimeMillis())
        locationdatabaseReference.child(selectedLocation).child(Consts.KEY_POSTS).child(post.postId).child(Consts.KEY_POPULARITY).setValue(post.popularity)
        Toast.makeText(this, "post added", Toast.LENGTH_LONG).show()

    }

}
