package com.example.passepartout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.passepartout.databinding.ActivityMainBinding
import com.example.passepartout.util.FirebaseUtils
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var avatarPickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
        binding.bottomNavigation.selectedItemId = R.id.action_home

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                    true
                }
                R.id.action_search -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, SearchFragment())
                        .commit()
                    true
                }
                R.id.action_add -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, AddFootFragment())
                        .commit()
                    true
                }
                R.id.trips -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, TripsFragment())
                        .commit()
                    true
                }
                R.id.action_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        avatarPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                changeUserAvatar(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                AuthUI.getInstance().signOut(this).addOnCompleteListener {
                    returnToLogin()
                }
                true
            }
            R.id.action_change_avatar -> {

                avatarPickerLauncher.launch("image/*")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun changeUserAvatar(imageUri: Uri) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Unauthorized user", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("avatars/${UUID.randomUUID()}")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    FirebaseUtils.saveData("users/${currentUser.uid}/photoUrl", downloadUrl.toString()) { success ->
                        if (success) {
                            Toast.makeText(this, "Avatar Updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Avatar Update Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Photo link error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Photo upload error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun returnToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

