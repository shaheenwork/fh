package com.shn.fh.posts.models

import com.denzcoskun.imageslider.models.SlideModel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

data class Post(
    var postId: String = "",
    var userId: String = "",
    var postmanName: String = "",
    var postmanPhoto: String = "",
    var foodSpot: String = "",
    var dish: String = "",
    var description: String = "",
    var photoURLs: List<String> = emptyList(),
    var photoSlides: List<CarouselItem> = emptyList(),
    var liked_users: List<String> = emptyList(),
    var timestamp: Long = 0,
    var likes: Int = 0,
    var comments: Int = 0
)

