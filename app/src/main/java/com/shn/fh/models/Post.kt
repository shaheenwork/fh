package com.shn.fh.models

data class Post(
    val postId: String,
    val userId: String,
    val foodSpot: String,
    val dish: String,
    val description: String,
    val photoURLs: List<String>,
    val timestamp: Long,
    val likes: Int,
    val comments: Int
)
