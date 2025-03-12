package com.example.passepartout

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.example.passepartout.data.footPrint
import java.io.File
import java.util.UUID

class AddFootFragment : Fragment() {

    private lateinit var imageFootPrint: ImageView
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var uploadButton: Button

    private var selectedLocation: String = ""
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0

    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private var cameraImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_foot_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageFootPrint = view.findViewById(R.id.imageFootPrint)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        uploadButton = view.findViewById(R.id.btnUpload)


        val mapSearchFragment = MapSearchFragment()
        mapSearchFragment.onLocationSelected = { location, lat, lng ->
            selectedLocation = location
            selectedLatitude = lat
            selectedLongitude = lng
            Toast.makeText(requireContext(), "Location picked: $location", Toast.LENGTH_SHORT).show()
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapSearchFragment)
            .commit()

        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    selectedImageUri = it
                    imageFootPrint.setImageURI(it)
                }
            }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
                if (success) {
                    selectedImageUri = cameraImageUri
                    imageFootPrint.setImageURI(cameraImageUri)
                }
            }

        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    cameraImageUri = createImageUri()
                    cameraImageUri?.let { uri ->
                        cameraLauncher.launch(uri)
                    }
                } else {
                    Toast.makeText(requireContext(), "NEED CAMERA PERMISSION", Toast.LENGTH_SHORT).show()
                }
            }

        imageFootPrint.setOnClickListener {
            showImageSourceDialog()
        }


        uploadButton.setOnClickListener {
            uploadButton.isEnabled = false
            uploadImageAndSaveFootprint()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Gallery", "Camera")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose image source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*")
                    1 -> {

                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }
            }
            .show()
    }

    private fun createImageUri(): Uri? {
        val imageFile = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )
    }

    private fun uploadImageAndSaveFootprint() {
        val uri = selectedImageUri
        if (uri == null) {
            Log.e("AddFootFragment", "No image selected")
            uploadButton.isEnabled = true
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("tripPhotos/${UUID.randomUUID()}")
        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveFootprintToDatabase(downloadUrl.toString())
                }.addOnFailureListener { e ->
                    Log.e("AddFootFragment", "Failed to get download URL: ${e.message}")
                    uploadButton.isEnabled = true
                }
            }
            .addOnFailureListener { e ->
                Log.e("AddFootFragment", "Image upload failed: ${e.message}")
                uploadButton.isEnabled = true
            }
    }

    private fun saveFootprintToDatabase(imageUrl: String) {
        val descriptionText = descriptionEditText.text.toString().trim()
        if (selectedLocation.isEmpty()) {
            Toast.makeText(requireContext(), "Pick a place on map", Toast.LENGTH_SHORT).show()
            uploadButton.isEnabled = true
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("AddFootFragment", "User not logged in")
            uploadButton.isEnabled = true
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("users")
            .child(currentUser.uid)
            .child("footPrints")

        val newFootprintRef = dbRef.push()
        val key = newFootprintRef.key ?: UUID.randomUUID().toString()

        val newFootPrint = footPrint(
            tripID = key,
            description = descriptionText,
            location = selectedLocation,
            photoUrl = imageUrl,
            latitude = selectedLatitude,
            longitude = selectedLongitude
        )

        newFootprintRef.setValue(newFootPrint)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post added", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()
            }
            .addOnFailureListener { e ->
                Log.e("AddFootFragment", "Failed to save footprint: ${e.message}")
                uploadButton.isEnabled = true
            }
    }
}
