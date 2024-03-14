package com.shn.fh.posts.models


import android.os.Parcel
import android.os.Parcelable

data class Post(
    var postId: String = "",
    var locationId: String = "",
    var userId: String = "",
    var postmanName: String = "",
    var postmanPhoto: String = "",
    var lat: Double = 0.0,
    var longt: Double = 0.0,
    var foodSpot: String = "",
    var dish: String = "",
    var description: String = "",
    var photoURLs: List<String> = emptyList(),
    var photoSlides: List<ParcelableCarouselItem> = emptyList(),
    var liked_users: List<String> = emptyList(),
    var timestamp: Long = 0,
    var likes: Int = 0,
    var comments: Int = 0,
    var popularity: Double = 0.0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createTypedArrayList(ParcelableCarouselItem.CREATOR) ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(locationId)
        parcel.writeString(userId)
        parcel.writeString(postmanName)
        parcel.writeString(postmanPhoto)
        parcel.writeDouble(lat)
        parcel.writeDouble(longt)
        parcel.writeString(foodSpot)
        parcel.writeString(dish)
        parcel.writeString(description)
        parcel.writeStringList(photoURLs)
        parcel.writeTypedList(photoSlides)
        parcel.writeStringList(liked_users)
        parcel.writeLong(timestamp)
        parcel.writeInt(likes)
        parcel.writeInt(comments)
        parcel.writeDouble(popularity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}


