//
//package com.example.myapplication.utils
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import androidx.core.app.ActivityCompat
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.tasks.Task
//import com.google.android.gms.location.LocationResult
//import android.location.Location
//
//object LocationUtils {
//    fun getCurrentLocation(context: Context, callback: (Location?) -> Unit) {
//        val fusedLocationClient: FusedLocationProviderClient =
//            LocationServices.getFusedLocationProviderClient(context)
//
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            callback(null)
//            return
//        }
//
//        val locationTask: Task<Location> = fusedLocationClient.lastLocation
//        locationTask.addOnSuccessListener { location: Location? ->
//            callback(location)
//        }
//    }
//}
package com.example.myapplication.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

object LocationUtils {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationUpdateListener: ((Location) -> Unit)? = null

    fun startLocationUpdates(context: Context, updateInterval: Long = 5000, fastestInterval: Long = 2000, listener: (Location) -> Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationUpdateListener = listener

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = updateInterval
            val fastestInterval = fastestInterval
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    locationUpdateListener?.invoke(it)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}