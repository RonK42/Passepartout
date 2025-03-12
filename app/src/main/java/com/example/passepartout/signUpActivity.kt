package com.example.passepartout

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.passepartout.data.user
import com.example.passepartout.data.usersDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class signUpActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private var avatarUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        val editTextName = findViewById<EditText>(R.id.editTextName)
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmit)
        val uploadAvatar = findViewById<ImageButton>(R.id.uploadAvatar)
        uploadAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }

        buttonSubmit.setOnClickListener {
            val enteredName = editTextName.text.toString().trim()
            if (enteredName.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val localUser = usersDB.users[currentUser.uid] ?: user().apply {
                    uid = currentUser.uid
                    email = currentUser.email ?: ""
                    phone = currentUser.phoneNumber ?: ""
                }
                localUser.name = enteredName

                usersDB.users[currentUser.uid] = localUser

                Firebase.database.getReference("users")
                    .child(currentUser.uid)
                    .setValue(localUser)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Sign-up successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            avatarUri = data.data
            val uploadAvatar = findViewById<ImageButton>(R.id.uploadAvatar)

            val bitmap = contentResolver.openInputStream(avatarUri!!)?.use {
                BitmapFactory.decodeStream(it)
            }
            if (bitmap != null) {
                uploadAvatar.setImageBitmap(bitmap)
                val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val newWidth = 400
                val newHeight = (newWidth / aspectRatio).toInt()
                val params = uploadAvatar.layoutParams
                params.width = newWidth
                params.height = newHeight
                uploadAvatar.layoutParams = params
            }
            uploadImage(avatarUri!!)
        }
    }


    private fun uploadImage(uri: Uri) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        val storageRef = FirebaseStorage.getInstance().getReference("avatars/${currentUser.uid}")
        val uploadTask = storageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val localUser = usersDB.users[currentUser.uid] ?: user().apply {
                    uid = currentUser.uid
                    email = currentUser.email ?: ""
                    phone = currentUser.phoneNumber ?: ""
                }
                localUser.photoUrl = downloadUri.toString()
                // עדכון במסד הנתונים ב-Realtime DB
                Firebase.database.getReference("users")
                    .child(currentUser.uid)
                    .setValue(localUser)
                Toast.makeText(this, "Avatar uploaded successfully", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
