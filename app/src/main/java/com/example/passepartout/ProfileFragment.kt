package com.example.passepartout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passepartout.data.usersDB
import com.example.passepartout.databinding.ProfileFragmentBinding
import com.example.passepartout.util.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: ProfileFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val localUser = usersDB.users[currentUser.uid]
            val avatarId = localUser?.photoUrl ?: currentUser.uid
            FirebaseUtils.loadPhotoWithGlide(
                context = requireContext(),
                imageID = avatarId,
                packageName = "avatars",
                imageFormatting = "jpg",
                imageView = binding.imageAvatarProfile
            )

            localUser?.let { user ->
                val friendsList = user.friends.mapNotNull { (friendUid, _) ->
                    usersDB.users[friendUid]
                }.toMutableList()

                binding.recyclerViewFriends.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerViewFriends.adapter = FriendAdapter(friendsList)
            }
        } else {
            binding.imageAvatarProfile.setImageResource(R.drawable.placeholder_svgrepo_com)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
