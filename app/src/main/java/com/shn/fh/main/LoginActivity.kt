package com.shn.fh.main

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.shn.fh.R
import com.shn.fh.databaseReference.FirebaseReference
import com.shn.fh.posts.PostsActivity
import com.shn.fh.utils.Consts
import com.shn.fh.utils.PrefManager


class LoginActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val reqCode: Int = 123
    private val tag = "LoginActivityTag"

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseDatabase: FirebaseReference
    private lateinit var userDatabaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        FirebaseApp.initializeApp(this)
        PrefManager.getInstance(this)


        firebaseDatabase = FirebaseReference()
        userDatabaseReference = firebaseDatabase.getUsersRef()


        val signInButton: SignInButton = findViewById(R.id.bt_sign_in);


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

        logout()

        signInButton.setOnClickListener { view: View? ->

            if (isInternetConnected(this@LoginActivity)) {
                signInGoogle()
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.msg_no_internet),
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    }

    private fun proceedToMainActivity() {
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }

    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, reqCode)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqCode) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun logout() {
        mGoogleSignInClient.signOut().addOnCompleteListener(OnCompleteListener<Void?> { task ->
            if (task.isSuccessful) {
                firebaseAuth.signOut()

            }
        })
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                updateUI(account)
            }
        } catch (e: ApiException) {
            // Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            if (Consts.DEBUGGABLE)
                Log.e(tag, e.message.toString())
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser

                // Check if the user is already signed up
                if (user != null) {
                    if (Consts.DEBUGGABLE)
                        Log.d(tag, "userID= " + user.uid)

                    PrefManager.setUserId(user.uid)
                    checkIfUserExists(user.uid, account)
                }
            }
        }
    }

    private fun checkIfUserExists(userId: String, account: GoogleSignInAccount) {
        userDatabaseReference.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (Consts.DEBUGGABLE)
                            Log.d(tag, "already exist")
                        PrefManager.setIsLogin(true)
                        proceedToMainActivity()
                    } else {
                        // User doesn't exist, proceed with the sign-up process
                        if (Consts.DEBUGGABLE)
                            Log.d(tag, "not exist")
                        signUpUser(userId, account)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors if needed
                }
            })
    }

    private fun signUpUser(userId: String, account: GoogleSignInAccount) {
        userDatabaseReference.child(userId).child(Consts.KEY_EMAIL).setValue(account.email)
        userDatabaseReference.child(userId).child(Consts.KEY_DISPLAY_NAME)
            .setValue(account.displayName)
        userDatabaseReference.child(userId).child(Consts.KEY_PHOTO_URL).setValue(account.photoUrl)

    }

}