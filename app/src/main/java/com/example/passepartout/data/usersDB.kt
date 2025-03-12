package com.example.passepartout.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class usersDB {
    companion object {
        val users: HashMap<String, user> = HashMap()
    }
}
