package com.shn.fh.databaseReference

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseReference {

    companion object {
        private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
        private var databaseReference: DatabaseReference = database.reference
    }

    fun getRootReference(): DatabaseReference {
        return databaseReference.child("test1")
    }

    fun getLocationsRef(): DatabaseReference {
        return getRootReference().child("locations")
    }
    fun getPostsRef(): DatabaseReference {
        return getRootReference().child("posts")
    }
    fun getCommentsRef(postID:String): DatabaseReference {
        return getRootReference().child("comments").child(postID)
    }
    fun getUsersRef(): DatabaseReference {
        return getRootReference().child("users")
    }

}