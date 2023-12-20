package com.shn.fh

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shn.fh.utils.PrefManager

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d("fcm","msg rcvd. someone comented")
    }

    override fun onNewToken(token: String) {
        PrefManager.getInstance(this)
        PrefManager.setFcmToken(token)
        Log.d("fcm token",token)
        super.onNewToken(token)
    }

    // Other methods for handling token refresh, etc.
}