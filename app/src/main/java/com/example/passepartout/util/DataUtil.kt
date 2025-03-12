package com.example.passepartout.util

import com.example.passepartout.data.footCard
import com.example.passepartout.data.user
import com.example.passepartout.data.usersDB.Companion.users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID

class DataUtil {
    companion object {
        fun createTripID(): String {
            return UUID.randomUUID().toString()
        }
        fun loadUsersFromDB(onComplete: (Boolean) -> Unit) {
            val myRef = Firebase.database.getReference("users")
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val loadedUsers = mutableMapOf<String, user>()
                    for(child in snapshot.children) {
                        val uid = child.key ?: continue
                        val userObj = child.getValue(user::class.java)
                        if (userObj != null) {

                            userObj.uid = uid
                            loadedUsers[uid] = userObj
                        }
                    }
                    users.clear()
                    users.putAll(loadedUsers)
                    onComplete(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false)
                }
            })
        }

        fun addFriend(currentUser: user, friend: user) {
            currentUser.friends[friend.uid] = friend.name
            val userRef = Firebase.database.getReference("users")
                .child(currentUser.uid)
                .child("friends")
            userRef.setValue(currentUser.friends)
        }

        fun convertFootPrintsToFootCards(user: user): List<footCard> {
            val footCards = mutableListOf<footCard>()

            user.footPrints.forEach { (key, fp) ->
                val tripId = if (fp.tripID.isEmpty()) key else fp.tripID
                val card = footCard.builder()
                    .tripId(tripId)
                    .tripName(fp.location)  // מציגים את המיקום כשם הטיול
                    .description(fp.description)
                    .location(fp.location)
                    .image(fp.photoUrl)
                    .avatar(user.photoUrl)
                    .latitude(fp.latitude)
                    .longitude(fp.longitude)
                    .ownerId(user.uid)
                    .likedBy(fp.likedBy ?: mutableMapOf())
                    .build()
                footCards.add(card)
            }
            return footCards
        }
    }
}
