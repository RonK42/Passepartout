package com.example.passepartout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passepartout.data.user
import com.example.passepartout.data.footCard
import com.example.passepartout.data.usersDB
import com.example.passepartout.databinding.OtherUserFragmentBinding
import com.example.passepartout.util.DataUtil
import com.example.passepartout.util.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase

class OtherUserFragment : Fragment() {

    private var _binding: OtherUserFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_FRIEND_UID = "friendUid"
        fun newInstance(friendUid: String): OtherUserFragment {
            val fragment = OtherUserFragment()
            val args = Bundle()
            args.putString(ARG_FRIEND_UID, friendUid)
            fragment.arguments = args
            return fragment
        }
    }

    private var friendUid: String? = null
    private var friendUser: user? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        friendUid = arguments?.getString(ARG_FRIEND_UID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OtherUserFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendUid?.let { uid ->
            val ref = Firebase.database.getReference("users").child(uid)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    friendUser = snapshot.getValue(user::class.java)
                    friendUser?.let { user ->

                        FirebaseUtils.loadPhotoWithGlide(
                            context = requireContext(),
                            imageID = user.photoUrl,
                            packageName = "avatars",
                            imageFormatting = "jpeg",
                            imageView = binding.imageAvatarOtherUser
                        )

                        val currentUserFirebase = FirebaseAuth.getInstance().currentUser
                        if (currentUserFirebase != null) {
                            val localCurrentUser = usersDB.users[currentUserFirebase.uid]
                            if (localCurrentUser != null) {

                                if (localCurrentUser.friends.containsKey(user.uid)) {
                                    binding.buttonAction.setImageResource(R.drawable.remove_profile)
                                } else {
                                    binding.buttonAction.setImageResource(R.drawable.add_profile)
                                }

                                binding.buttonAction.setOnClickListener {
                                    if (localCurrentUser.friends.containsKey(user.uid)) {

                                        val dbRef =
                                            FirebaseDatabase.getInstance().getReference("users")
                                        dbRef.child(currentUserFirebase.uid)
                                            .child("friends")
                                            .child(user.uid)
                                            .removeValue()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "FRIEND DELETED SUCCESS",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    localCurrentUser.friends.remove(user.uid)
                                                    binding.buttonAction.setImageResource(R.drawable.add_profile)
                                                } else {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "FRIEND DELETED FAIL",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        DataUtil.addFriend(localCurrentUser, user)
                                        Toast.makeText(
                                            requireContext(),
                                            "USER ADDED",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        localCurrentUser.friends[user.uid] = user.name
                                        binding.buttonAction.setImageResource(R.drawable.remove_profile)
                                    }
                                }
                                binding.buttonAction.visibility = View.VISIBLE
                            }
                        } else {
                            binding.buttonAction.visibility = View.GONE
                        }
                        val footCardsList: List<footCard> =
                            DataUtil.convertFootPrintsToFootCards(user)
                        binding.recyclerViewPostsOtherUser.layoutManager =
                            LinearLayoutManager(requireContext())
                        binding.recyclerViewPostsOtherUser.adapter =
                            FootCardAdapter(footCardsList.toMutableList(), false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OtherUserFragment", "Error loading user: ${error.message}")
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
