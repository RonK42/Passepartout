package com.example.passepartout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.passepartout.data.usersDB
import com.example.passepartout.util.DataUtil
import com.google.firebase.auth.FirebaseAuth

class StartAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the splash/start layout
        setContentView(R.layout.start_app_layout)

        // Load the user database
        DataUtil.loadUsersFromDB { success ->
            // Once loaded, check if the user is already authenticated
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // If the user is logged in, redirect to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // If no user is logged in, redirect to LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
            }
            // Close the start activity so it won't remain in the back stack
            finish()
        }
    }
}
