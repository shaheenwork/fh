package com.shn.fh.notifications.model

import com.shn.fh.user.model.User

data class GroupedNotification(
    val notificationIds: List<String>,
    val postId: String,
    val action: Int,
    val userIds: List<String>,
    val users: List<User>,
    val timestamp: Long,
    val locationId: String,
    val readStatus: Int
)

