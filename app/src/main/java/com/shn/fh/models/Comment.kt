package com.shn.fh.models

data class Comment(
    var commentId: String="",
    var text: String="",
    var timestamp: Long=0,
    var userId: String=""
)
