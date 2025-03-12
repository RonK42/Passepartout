package com.example.passepartout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passepartout.data.user
import com.example.passepartout.data.usersDB
import com.example.passepartout.util.DataUtil
import com.google.firebase.auth.FirebaseAuth
import com.example.passepartout.data.footPrint

class HomeFragment : Fragment() {

    private lateinit var recyclerViewPosts: RecyclerView
    private lateinit var emptyView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts)
        emptyView = view.findViewById(R.id.emptyView)
        recyclerViewPosts.layoutManager = LinearLayoutManager(context)

        DataUtil.loadUsersFromDB { success ->
            if (success) {

                val currentUser = FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                    usersDB.users[uid]
                } ?: usersDB.users.values.firstOrNull()
                currentUser?.let { user ->
                    val footCardsList = DataUtil.convertFootPrintsToFootCards(user)
                    user.footPrints.values.forEach { fp ->
                        Log.d("FootPrint", "TripID: ${fp.tripID}, Description: ${fp.description}, Location: ${fp.location}, Photo: ${fp.photoUrl}")
                    }
                    if (footCardsList.isEmpty()) {
                        recyclerViewPosts.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                    } else {
                        recyclerViewPosts.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        recyclerViewPosts.adapter = FootCardAdapter(footCardsList.toMutableList(), true)
                    }
                }
            }
        }
    }
}
