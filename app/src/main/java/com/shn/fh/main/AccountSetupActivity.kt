package com.shn.fh.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.DatabaseReference
import com.shn.fh.R
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.databinding.ActivitySetupAccountBinding
import com.shn.fh.posts.PostsActivity
import com.shn.fh.utils.Consts
import com.shn.fh.utils.PrefManager

class AccountSetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupAccountBinding
    private lateinit var firebaseDatabase: FirebaseReference
    private lateinit var userDatabaseReference: DatabaseReference
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var photoUrl: String
    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        name = intent.getStringExtra(Consts.KEY_DISPLAY_NAME)!!
        email = intent.getStringExtra(Consts.KEY_EMAIL)!!
      //  photoUrl = intent.getStringExtra(Consts.KEY_PHOTO_URL)!!
        userId = intent.getStringExtra(Consts.KEY_USER_ID)!!

        binding.etName.setText(name)

        firebaseDatabase = FirebaseReference()
        userDatabaseReference = firebaseDatabase.getUsersRef()


        binding.btnSave.setOnClickListener {
            if (binding.etName.text.toString().isNotEmpty()) {

                signUpUser()
                PrefManager.setIsLogin(true)
                proceedToMainActivity()


            }
        }

    }

    private fun signUpUser() {
        userDatabaseReference.child(userId).child(Consts.KEY_EMAIL).setValue(email)
        userDatabaseReference.child(userId).child(Consts.KEY_DISPLAY_NAME).setValue(name)
      //  userDatabaseReference.child(userId).child(Consts.KEY_PHOTO_URL).setValue(photoUrl)

    }

    private fun proceedToMainActivity() {
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
        finish()
    }
}