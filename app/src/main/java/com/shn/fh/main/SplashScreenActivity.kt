package com.shn.fh.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.shn.fh.databinding.ActivitySplashBinding
import com.shn.fh.posts.PostsActivity
import com.shn.fh.utils.PrefManager


@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        PrefManager.getInstance(this)

        Handler(Looper.getMainLooper()).postDelayed({
            /* val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                 this@SplashScreenActivity,
                 binding.logo,
                 ViewCompat.getTransitionName(binding.logo)!!
             )*/

            //     startActivity(Intent(this, LoginActivity::class.java), options.toBundle())
            if (PrefManager.getIsLogin()) {
                proceedToMainActivity()
            } else {
                proceedToLoginActivity()
            }

            // Handler(Looper.getMainLooper()).postDelayed({
            //    finish()
            // }, 200)


        }, 1500)

    }

    private fun proceedToMainActivity() {
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun proceedToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}