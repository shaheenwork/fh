package com.shn.fh.posts.models

import android.os.Parcel
import android.os.Parcelable

data class ParcelableCarouselItem(val image: String, val caption: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(image)
        parcel.writeString(caption)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableCarouselItem> {
        override fun createFromParcel(parcel: Parcel): ParcelableCarouselItem {
            return ParcelableCarouselItem(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableCarouselItem?> {
            return arrayOfNulls(size)
        }
    }
}
