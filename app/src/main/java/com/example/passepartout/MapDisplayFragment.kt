package com.example.passepartout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapDisplayFragment : Fragment(), OnMapReadyCallback {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var locationTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            latitude = it.getDouble(ARG_LATITUDE, 0.0)
            longitude = it.getDouble(ARG_LONGITUDE, 0.0)
            locationTitle = it.getString(ARG_LOCATION_TITLE, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment)
                as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val latLng = LatLng(latitude, longitude)
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(latLng).title(locationTitle))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"
        private const val ARG_LOCATION_TITLE = "locationTitle"

        fun newInstance(latitude: Double, longitude: Double, locationTitle: String) =
            MapDisplayFragment().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_LATITUDE, latitude)
                    putDouble(ARG_LONGITUDE, longitude)
                    putString(ARG_LOCATION_TITLE, locationTitle)
                }
            }
    }
}
