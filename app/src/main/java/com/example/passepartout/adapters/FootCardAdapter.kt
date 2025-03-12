package com.example.passepartout

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.passepartout.data.footCard
import com.example.passepartout.util.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class FootCardAdapter(
    private val footCards: MutableList<footCard>,
    private val showDelete: Boolean
) : RecyclerView.Adapter<FootCardAdapter.FootCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FootCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_post, parent, false)
        return FootCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FootCardViewHolder, position: Int) {
        val card = footCards[position]
        holder.textUsername.text = card.tripName
        holder.textPostContent.text = card.description
        holder.textLikesCount.text = card.likedBy.size.toString()
        FirebaseUtils.loadPhotoWithGlide(
            context = holder.itemView.context,
            imageID = card.image,
            packageName = "tripPhotos",
            imageFormatting = "jpeg",
            imageView = holder.imagePost
        )
        FirebaseUtils.loadPhotoWithGlide(
            context = holder.itemView.context,
            imageID = card.avatar,
            packageName = "avatars",
            imageFormatting = "jpeg",
            imageView = holder.imageAvatar
        )
        holder.buttonOptions.setOnClickListener {
            val activity = holder.itemView.context as AppCompatActivity
            if (holder.mapContainer.visibility == View.GONE) {
                holder.imagePost.visibility = View.GONE
                holder.mapContainer.visibility = View.VISIBLE
                holder.mapContainer.id = View.generateViewId()
                if (holder.mapContainer.id == View.NO_ID) {
                    holder.mapContainer.id = View.generateViewId()
                }
                val mapFragment = MapDisplayFragment.newInstance(card.latitude, card.longitude, card.location)
                activity.supportFragmentManager.beginTransaction()
                    .replace(holder.mapContainer.id, mapFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                holder.mapContainer.visibility = View.GONE
                holder.imagePost.visibility = View.VISIBLE
                val fragment = activity.supportFragmentManager.findFragmentById(holder.mapContainer.id)
                fragment?.let {
                    activity.supportFragmentManager.beginTransaction().remove(it).commit()
                }
            }
        }

        if (showDelete) {
            holder.buttonDelete.visibility = View.VISIBLE
            holder.buttonDelete.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val storageRef = if (card.image.startsWith("http")) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(card.image)
                    } else {
                        FirebaseStorage.getInstance().getReference("tripPhotos/${card.image}.jpeg")
                    }
                    storageRef.delete().addOnSuccessListener {
                        val dbRef = Firebase.database.getReference("users")
                            .child(currentUser.uid)
                            .child("footPrints")
                            .child(card.tripId)
                        dbRef.removeValue().addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Foot Print Deleted", Toast.LENGTH_SHORT).show()
                            footCards.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, footCards.size)
                        }.addOnFailureListener {
                            Toast.makeText(holder.itemView.context, "Error while delete from DB", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(holder.itemView.context, "Error while delete from storage: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(holder.itemView.context, "No user", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.buttonDelete.visibility = View.GONE
        }

        holder.buttonLike.setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId == null) {
                Toast.makeText(holder.itemView.context, "No user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (card.ownerId == currentUserId) {
                Toast.makeText(holder.itemView.context, "Cant like own post", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (card.likedBy.containsKey(currentUserId)) {
                Toast.makeText(holder.itemView.context, "Already Liked", Toast.LENGTH_SHORT).show()
            } else {
                if (card.ownerId.isEmpty() || card.tripId.isEmpty()) {
                    Toast.makeText(holder.itemView.context, "No post info", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                card.likedBy[currentUserId] = true
                holder.textLikesCount.text = card.likedBy.size.toString()

                FirebaseUtils.saveData(
                    "users/${card.ownerId}/footPrints/${card.tripId}/likedBy",
                    card.likedBy
                ) { success ->
                    if (!success) {
                        Toast.makeText(holder.itemView.context, "Error in like by", Toast.LENGTH_SHORT).show()
                    }
                }
                FirebaseUtils.saveData(
                    "users/${card.ownerId}/footPrints/${card.tripId}/likesCount",
                    card.likedBy.size
                ) { success ->
                    if (!success) {
                        Toast.makeText(holder.itemView.context, "Likes count error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = footCards.size

    class FootCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textUsername: TextView = itemView.findViewById(R.id.textUsername)
        val textPostContent: TextView = itemView.findViewById(R.id.textPostContent)
        val imageAvatar: ImageView = itemView.findViewById(R.id.imageAvatar)
        val imagePost: ImageView = itemView.findViewById(R.id.imagePost)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)
        val buttonOptions: ImageButton = itemView.findViewById(R.id.buttonOptions)
        val mapContainer: FrameLayout = itemView.findViewById(R.id.mapContainer)
        val buttonLike: ImageButton = itemView.findViewById(R.id.buttonLike)
        val textLikesCount: TextView = itemView.findViewById(R.id.textLikesCount)
    }
}
