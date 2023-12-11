package com.shn.fh.databaseReference

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shn.fh.utils.Consts

class FirebaseReference {

    companion object {
        private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
        private var databaseReference: DatabaseReference = database.reference
    }

    fun getRootReference(): DatabaseReference {
        return databaseReference.child("test1")
    }

    fun getLocationsRef(): DatabaseReference {
        return getRootReference().child(Consts.KEY_LOCATIONS)
    }
    fun getPostsRef(): DatabaseReference {
        return getRootReference().child(Consts.KEY_POSTS)
    }
    fun getCommentsRef(postID:String): DatabaseReference {
        return getRootReference().child(Consts.KEY_COMMENTS).child(postID)
    }
    fun getUsersRef(): DatabaseReference {
        return getRootReference().child(Consts.KEY_USERS)
    }

}