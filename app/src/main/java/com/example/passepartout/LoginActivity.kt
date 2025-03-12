package com.example.passepartout

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.passepartout.data.user
import com.example.passepartout.data.usersDB
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.bumptech.glide.Glide
import com.example.passepartout.util.DataUtil

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var buttonSignInEmail: Button
    private lateinit var buttonSignInOther: Button
    private lateinit var buttonSignUpEmail: Button
    private lateinit var logoImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)


        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        buttonSignInEmail = findViewById(R.id.buttonSignInEmail)
        buttonSignInOther = findViewById(R.id.buttonSignInOther)
        buttonSignUpEmail = findViewById(R.id.buttonSignUpEmail)
        logoImageView = findViewById(R.id.imageView)

        Glide.with(this)
            .load(R.drawable.ic_launcher_foreground)
            .into(logoImageView)

        loadUserDB()

        buttonSignInEmail.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signInWithEmail(email, password)
        }

        buttonSignInOther.setOnClickListener {
            loginWithOtherProviders()
        }

        buttonSignUpEmail.setOnClickListener {
            startActivity(Intent(this, EmailSignUpActivity::class.java))
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUser()
                } else {
                    Toast.makeText(this, "Sign in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loginWithOtherProviders() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.FirebaseUITheme)
            .setAvailableProviders(providers)
            .setLogo(R.drawable.ic_launcher_foreground)
            .build()

        startActivityForResult(signInIntent, 100)
    }

    private fun loadUserDB() {
        DataUtil.loadUsersFromDB { success ->
            if (success) {
                Toast.makeText(this, "Users loaded successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
            checkUser()
        }
    }

    private fun checkUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show()
        } else {
            val localUser = usersDB.users[currentUser.uid] ?: user().apply {
                uid = currentUser.uid
                email = currentUser.email ?: ""
                phone = currentUser.phoneNumber ?: ""
            }
            if (localUser.name.isEmpty()) {
                usersDB.users[currentUser.uid] = localUser
                val intent = Intent(this, signUpActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Please complete your profile", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                checkUser()
            } else {
                Toast.makeText(this, "Sign-in error with other providers!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
