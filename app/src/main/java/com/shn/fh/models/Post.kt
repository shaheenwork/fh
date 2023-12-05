package com.shn.fh.models

data class Post(
    var postId: String = "",
    var userId: String = "",
    var foodSpot: String = "",
    var dish: String = "",
    var description: String = "",
    var photoURLs: List<String> = emptyList(),
    var timestamp: Long = 0,
    var likes: Int = 0,
    var comments: Int = 0
)

