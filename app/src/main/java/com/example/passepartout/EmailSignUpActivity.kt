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
import com.example.passepartout.util.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class EmailSignUpActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private var avatarUri: Uri? = null

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var uploadAvatar: ImageButton
    private lateinit var buttonSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_sign_up)

        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        uploadAvatar = findViewById(R.id.uploadAvatar)
        buttonSignUp = findViewById(R.id.buttonSignUp)

        uploadAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }

        buttonSignUp.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signUpWithEmail(email, password, name)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            avatarUri = data.data
            val bitmap = contentResolver.openInputStream(avatarUri!!)?.use {
                BitmapFactory.decodeStream(it)
            }
            if (bitmap != null) {
                uploadAvatar.setImageBitmap(bitmap)
                val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val newWidth = 400  // ניתן להתאים
                val newHeight = (newWidth / aspectRatio).toInt()
                val params = uploadAvatar.layoutParams
                params.width = newWidth
                params.height = newHeight
                uploadAvatar.layoutParams = params
            }
        }
    }

    private fun signUpWithEmail(email: String, password: String, name: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null) {
                        val newUser = user().apply {
                            uid = firebaseUser.uid
                            this.email = email
                            this.name = name
                        }

                        if (avatarUri != null) {
                            uploadAvatarImage(firebaseUser.uid, newUser)
                        } else {
                            saveUser(newUser)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Sign-up failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun uploadAvatarImage(uid: String, newUser: user) {
        val storageRef = FirebaseStorage.getInstance().getReference("avatars/$uid")
        storageRef.putFile(avatarUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    newUser.photoUrl = downloadUri.toString()
                    FirebaseUtils.loadPhotoWithGlide(
                        context = this,
                        imageID = downloadUri.toString(),
                        packageName = "avatars",
                        imageFormatting = "jpeg",
                        imageView = uploadAvatar
                    )
                    saveUser(newUser)
                    Toast.makeText(this, "Sign-up successful", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload avatar: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveUser(newUser: user) {
        usersDB.users[newUser.uid] = newUser
        Firebase.database.getReference("users")
            .child(newUser.uid)
            .setValue(newUser)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
