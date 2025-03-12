package com.example.passepartout

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.IOException

class MapSearchFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: FloatingActionButton
    private lateinit var currentLocationButton: FloatingActionButton
    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    var onLocationSelected: ((location: String, latitude: Double, longitude: Double) -> Unit)? = null

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_search, container, false)
        mapView = view.findViewById(R.id.mapView)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        currentLocationButton = view.findViewById(R.id.currentLocationButton)

        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {

                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        googleMap.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(requireContext(), "NEED LOCATION PERMISSION", Toast.LENGTH_SHORT).show()
                }
            }

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        searchButton.setOnClickListener { searchLocation() }
        currentLocationButton.setOnClickListener { showCurrentLocation() }

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun searchLocation() {
        val locationName = searchEditText.text.toString()
        val geocoder = Geocoder(requireContext())
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                googleMap.clear()
                googleMap.addMarker(MarkerOptions().position(latLng).title(address.getAddressLine(0)))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                onLocationSelected?.invoke(address.getAddressLine(0), address.latitude, address.longitude)
            } else {
                Toast.makeText(requireContext(), "LOCATION NOT EXIST", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "SEARCH ERROR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "NEED LOCATION PERMISSION", Toast.LENGTH_SHORT).show()
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val geocoder = Geocoder(requireContext())
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val addressLine = if (addresses != null && addresses.isNotEmpty()) {
                        addresses[0].getAddressLine(0)
                    } else {
                        "LOCATION NOT EXIST"
                    }
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title(addressLine))
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    onLocationSelected?.invoke(addressLine, location.latitude, location.longitude)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "ADDRESS ERROR", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "CANT FIND GPS LOCATION", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }
}
