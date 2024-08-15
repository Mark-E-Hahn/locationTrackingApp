package com.example.locationtrackingapp

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateMapLocation(location)
                }
            }
        }

        // Request location permission
        this.requestLocationPermission()
    }

    private fun requestLocationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[ACCESS_FINE_LOCATION] == true -> {
                    // Precise location access granted.
                    startLocationUpdates()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    // Only approximate location access granted.
                    startLocationUpdates()
                }
                else -> {
                    // No location access granted.
                    Toast.makeText(this, "Location permission is required for this app to work", Toast.LENGTH_SHORT).show()
                }
            }
        }

        when {
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the location directly
                startLocationUpdates()
            }
            else -> {
                // Request permissions
                locationPermissionRequest.launch(
                    arrayOf(
                        ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun startLocationUpdates() {
        // Creating the LocationRequest using the updated Builder API
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Set the priority for accuracy
            10000L // Set the interval in milliseconds (10 seconds)
        ).apply {
            setMinUpdateIntervalMillis(5000L) // Set the fastest interval in milliseconds (5 seconds)
        }.build()

        // Request location updates
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, // Pass the locationRequest object here
                locationCallback,
                mainLooper
            )
        }
    }

    private fun updateMapLocation(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
