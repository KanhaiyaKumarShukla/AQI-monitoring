package com.example.sih.common.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.widget.Toast
import com.example.sih.common.constants.AppConstants.makeToast
import java.util.Locale


class LocationService(
    private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient,

) {

    private val tag = "TAG"

    fun checkLocationPermission(activityResultLauncher: ActivityResultLauncher<String>) {
        if (isLocationPermissionGranted()) {
            Log.d(tag, "Permission granted")
        } else {
            context.makeToast("For Better Performance Please grant location permission from setting.")
            requestLocationPermission(activityResultLauncher)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission(activityResultLauncher: ActivityResultLauncher<String>) {
        activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    fun getLiveLocation(onCityFetched: (String?) -> Unit) {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d(tag, "Lat: $latitude, Long: $longitude")

                // Fetch city name
                val cityName = findCity(latitude, longitude)
                onCityFetched(cityName)
            } else {
                Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                onCityFetched(null)
            }
        }
    }

    private fun findCity(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val cityName = addresses[0].locality
                Log.d(tag, "City: $cityName")
                cityName
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
