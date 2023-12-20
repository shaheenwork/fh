package com.shn.fh.utils

import android.content.Context
import android.content.SharedPreferences

object PrefManager {


    private const val isLogin = "isLogin"
    private const val userId = "userId"
    private const val fcm_token = "fcm_token"

    private var preferences : SharedPreferences ?= null

    fun getInstance(context: Context){
        preferences = context.getSharedPreferences("userPref",Context.MODE_PRIVATE)
    }


    private fun putString(key: String, value: String) {
        val editor: SharedPreferences.Editor = preferences?.edit()!!
        editor.putString(key, value)
        editor.apply()
    }

    private fun putBoolean(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor = preferences?.edit()!!
        editor.putBoolean(key, value)
        editor.apply()
    }


    fun setIsLogin(value: Boolean) {
        putBoolean(isLogin, value)
    }
    fun getIsLogin(): Boolean {
        return preferences?.getBoolean(isLogin,false)!!
    }
    fun setFcmToken(value: String) {
        putString(fcm_token, value)
    }
    fun getFcmToken(): String {
        return preferences?.getString(fcm_token,"")!!
    }



    fun setUserId(value: String){
        putString(userId,value)
    }
    fun getUserId(): String {
        return preferences?.getString(userId,"")!!
    }




}