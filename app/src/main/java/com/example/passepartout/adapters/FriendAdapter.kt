package com.example.passepartout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.passepartout.data.user
import com.example.passepartout.util.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FriendAdapter(private val friends: MutableList<user>) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_profile_card, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.nameTextView.text = friend.name

        FirebaseUtils.loadPhotoWithGlide(
            context = holder.itemView.context,
            imageID = friend.photoUrl,
            packageName = "avatars",
            imageFormatting = "jpeg",
            imageView = holder.avatarImageView
        )

        holder.itemView.setOnClickListener {
            val activity = holder.itemView.context as? AppCompatActivity
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragmentContainer, OtherUserFragment.newInstance(friend.uid))
                ?.addToBackStack(null)
                ?.commit()
        }

        holder.buttonAction.setOnClickListener {
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserUid != null) {
                val dbRef = FirebaseDatabase.getInstance().getReference("users")
                dbRef.child(currentUserUid).child("friends").child(friend.uid).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        friends.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, friends.size)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = friends.size

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatar)
        val nameTextView: TextView = itemView.findViewById(R.id.name)
        val buttonAction: ImageButton = itemView.findViewById(R.id.buttonAction)
    }
}
