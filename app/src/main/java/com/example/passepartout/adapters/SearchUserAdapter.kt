package com.example.passepartout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passepartout.data.user
import com.example.passepartout.data.usersDB
import com.example.passepartout.util.FirebaseUtils
import com.example.passepartout.util.DataUtil

class SearchUserAdapter(
    private val currentUser: user,
    private var userList: List<user>
) : RecyclerView.Adapter<SearchUserAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name)
        val avatarImageView: ImageView = itemView.findViewById(R.id.avatar)
        val buttonAction: ImageButton = itemView.findViewById(R.id.buttonAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_search_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userItem = userList[position]
        holder.nameTextView.text = userItem.name

        FirebaseUtils.loadPhotoWithGlide(
            context = holder.itemView.context,
            imageID = userItem.photoUrl,
            packageName = "avatars",
            imageFormatting = "jpeg",
            imageView = holder.avatarImageView
        )

        if (currentUser.friends.containsKey(userItem.uid)) {
            holder.buttonAction.visibility = View.GONE
        } else {
            holder.buttonAction.visibility = View.VISIBLE
            holder.buttonAction.setOnClickListener {
                DataUtil.addFriend(currentUser, userItem)
                holder.buttonAction.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateList(newList: List<user>) {
        userList = newList
        notifyDataSetChanged()
    }
}
