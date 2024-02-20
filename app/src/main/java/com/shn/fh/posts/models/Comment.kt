package com.shn.fh.posts.models

data class Comment(
    var commentId: String="",
    var text: String="",
    var timestamp: Long=0,
    var userId: String="",
    var postId: String="",
    var locationId: String=""
)
