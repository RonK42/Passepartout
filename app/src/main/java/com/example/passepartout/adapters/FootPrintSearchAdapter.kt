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
import com.example.passepartout.data.usersDB
import com.example.passepartout.util.FirebaseUtils
import com.example.passepartout.util.DataUtil

class CombinedSearchAdapter(
    private var currentUser: user
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TRIP = 0
        const val VIEW_TYPE_USER = 1
    }

    private var items: List<Any> = emptyList()

    fun updateList(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is com.example.passepartout.data.footCard -> VIEW_TYPE_TRIP
            is user -> VIEW_TYPE_USER
            else -> VIEW_TYPE_TRIP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_TRIP) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.foot_print_search_card, parent, false)
            TripViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_search_card, parent, false)
            UserViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TripViewHolder -> {
                val card = items[position] as com.example.passepartout.data.footCard
                holder.bind(card)
            }
            is UserViewHolder -> {
                val userItem = items[position] as user
                holder.bind(userItem, currentUser)
            }
        }
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val footPrintPhoto: ImageView = itemView.findViewById(R.id.footPrintPhoto)
        private val footPrintName: TextView = itemView.findViewById(R.id.footPrintName)
        fun bind(card: com.example.passepartout.data.footCard) {
            footPrintName.text = "${card.tripName}: ${card.description} (${card.location})"
            FirebaseUtils.loadPhotoWithGlide(
                context = itemView.context,
                imageID = card.image,
                packageName = "tripPhotos",
                imageFormatting = "jpeg",
                imageView = footPrintPhoto
            )
        }
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatar)
        private val buttonAction: ImageButton = itemView.findViewById(R.id.buttonAction)

        fun bind(userItem: user, currentUser: user) {
            nameTextView.text = userItem.name
            FirebaseUtils.loadPhotoWithGlide(
                context = itemView.context,
                imageID = userItem.photoUrl,
                packageName = "avatars",
                imageFormatting = "jpeg",
                imageView = avatarImageView
            )
            itemView.setOnClickListener {
                val activity = itemView.context as? AppCompatActivity
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, OtherUserFragment.newInstance(userItem.uid))
                    ?.addToBackStack(null)
                    ?.commit()
            }
            if (!currentUser.friends.containsKey(userItem.uid)) {
                buttonAction.visibility = View.VISIBLE
                buttonAction.setOnClickListener {
                    DataUtil.addFriend(currentUser, userItem)
                    buttonAction.visibility = View.GONE
                }
            } else {
                buttonAction.visibility = View.GONE
            }
        }
    }
}
