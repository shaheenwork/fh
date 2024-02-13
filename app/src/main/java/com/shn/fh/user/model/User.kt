package com.shn.fh.user.model

data class User(
    var userId:String="",
    var userName:String="",
    var userPic:String="",
    var userBio:String="",
    var followerCount:Int=0,
    var followCount:Int=0,
)
