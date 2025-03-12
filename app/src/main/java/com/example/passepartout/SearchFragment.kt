package com.example.passepartout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passepartout.data.footCard
import com.example.passepartout.data.usersDB
import com.example.passepartout.data.user
import com.example.passepartout.util.DataUtil
import com.google.firebase.auth.FirebaseAuth

class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: CombinedSearchAdapter

    private var currentUser: user? = null
    private var allUsers: List<user> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.search_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewSearchResults)
        recyclerView.layoutManager = LinearLayoutManager(context)
        searchView = view.findViewById(R.id.searchView)

        adapter = CombinedSearchAdapter(user())
        recyclerView.adapter = adapter

        DataUtil.loadUsersFromDB { success ->
            if (success) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                currentUser = uid?.let { usersDB.users[it] } ?: usersDB.users.values.firstOrNull()
                allUsers = usersDB.users.values.toList()
                currentUser?.let { user ->
                    adapter = CombinedSearchAdapter(user)
                    recyclerView.adapter = adapter
                }
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().trim()
                if (currentUser == null) return false

                val combinedResults = mutableListOf<Any>()
                currentUser?.friends?.keys?.forEach { friendUid ->
                    val friendUser = usersDB.users[friendUid]
                    friendUser?.let { friend ->
                        friend.footPrints.values.forEach { fp ->
                            if (fp.description.contains(query, ignoreCase = true) ||
                                fp.location.contains(query, ignoreCase = true)
                            ) {
                                val card = footCard.builder()
                                    .tripId(fp.tripID)
                                    .tripName(friend.name)
                                    .description(fp.description)
                                    .location(fp.location)
                                    .image(fp.photoUrl)
                                    .avatar(friend.photoUrl)
                                    .build()
                                combinedResults.add(card)
                            }
                        }
                    }
                }

                val nonFriends = allUsers.filter {
                    it.uid != currentUser!!.uid && !currentUser!!.friends.containsKey(it.uid)
                }
                nonFriends.forEach { userItem ->
                    if (userItem.name.contains(query, ignoreCase = true)) {
                        combinedResults.add(userItem)
                    }
                }

                if (query.isEmpty()) {
                    adapter.updateList(emptyList())
                } else {
                    adapter.updateList(combinedResults)
                }

                Log.d("SearchFragment", "Combined results count: ${combinedResults.size}")
                return true
            }
        })
    }
}