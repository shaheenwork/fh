package com.shn.fh.models

data class Location(
    val name: String = "",
    val lat: Long = 0,
    val lng: Long = 0,
    val posts: Map<String, Boolean> = emptyMap()
)
