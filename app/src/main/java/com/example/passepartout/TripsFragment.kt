package com.example.passepartout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passepartout.data.footCard
import com.example.passepartout.data.usersDB
import com.example.passepartout.util.DataUtil
import com.google.firebase.auth.FirebaseAuth

class TripsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerViewTrips)
        recyclerView.layoutManager = LinearLayoutManager(context)

        DataUtil.loadUsersFromDB { success ->
            if (success) {
                val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
                val currentUser = currentUserUid?.let { usersDB.users[it] }
                if (currentUser != null) {
                    val friendsTrips = mutableListOf<footCard>()

                    for (friendUid in currentUser.friends.keys) {
                        val friend = usersDB.users[friendUid]
                        friend?.let { friendUser ->
                            friendUser.footPrints.forEach { (key, fp) ->
                                val tripId = if (fp.tripID.isEmpty()) key else fp.tripID
                                val imageUrl = if (fp.photoUrl.isNotEmpty()) fp.photoUrl else friendUser.photoUrl
                                val card = footCard.builder()
                                    .tripId(tripId)
                                    .tripName(friendUser.name)
                                    .description(fp.description)
                                    .location(fp.location)
                                    .image(imageUrl)
                                    .avatar(friendUser.photoUrl)
                                    .latitude(fp.latitude)
                                    .longitude(fp.longitude)
                                    .ownerId(friendUser.uid)
                                    .likedBy(fp.likedBy ?: mutableMapOf())
                                    .build()
                                friendsTrips.add(card)
                            }
                        }
                    }
                    recyclerView.adapter = FootCardAdapter(friendsTrips, false)
                }
            }
        }
    }
}
