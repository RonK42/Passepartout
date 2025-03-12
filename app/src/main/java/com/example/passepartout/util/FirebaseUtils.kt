package com.example.passepartout.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.widget.ImageView
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.passepartout.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

object FirebaseUtils {

    fun <T> saveData(
        dbCategory: String,
        data: T,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val myRef = Firebase.database.getReference(dbCategory)
        myRef.setValue(data).addOnCompleteListener { task ->
            onComplete?.invoke(task.isSuccessful)
        }
    }

    // Real-time reading: This function will invoke onDataChange every time data changes.
    inline fun <reified T> readDataRealtime(
        dbCategory: String,
        crossinline onDataChange: (T?) -> Unit,
        crossinline onError: (error: DatabaseError) -> Unit = {}
    ) {
        val myRef = Firebase.database.getReference(dbCategory)
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(T::class.java)
                onDataChange(data)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }

    // One-time reading: This function reads the data once and then stops listening.
    inline fun <reified T> readDataOnce(
        dbCategory: String,
        crossinline onDataChange: (T?) -> Unit,
        crossinline onError: (error: DatabaseError) -> Unit = {}
    ) {
        val myRef = Firebase.database.getReference(dbCategory)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(T::class.java)
                onDataChange(data)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }

    fun loadPhotoWithGlide(
        context: Context,
        imageID: String,
        packageName: String,
        imageFormatting: String,
        imageView: ImageView
    ) {
        if (imageID.startsWith("http")) {
            Glide.with(context)
                .load(imageID)
                .apply(RequestOptions().placeholder(R.drawable.placeholder_svgrepo_com))
                .into(imageView)
        } else {
            // Error with the link, go to the storage directly
            val storageReference = FirebaseStorage.getInstance()
                .getReference("$packageName/$imageID.$imageFormatting")
            Glide.with(context)
                .load(storageReference)
                .apply(RequestOptions().placeholder(R.drawable.placeholder_svgrepo_com))
                .into(imageView)
        }
    }
}
