package com.shn.fh.notifications.model

import com.shn.fh.user.model.User
import com.shn.fh.utils.Consts

data class Notification(
    val timestamp: Long = 0,
    val action: Int = Consts.ACTION_LIKE,
    val user: User = User(),
    val postId: String = "",
    val readStatus: Int = 0
)
