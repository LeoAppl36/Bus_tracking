package com.example.myapplication

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Define the location callback to receive location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                // Handle location updates here
                // Store in SharedPreferences or Database if needed
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "LocationChannel")
            .setContentTitle("Location Tracking")
            .setContentText("Your location is being tracked")
             // Make sure you have this icon
            .build()

        // Start the service in the foreground with a notification
        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        // Create a location request using the builder pattern (as per the latest APIs)
        val locationRequest = LocationRequest.Builder(5000L)
            .setMinUpdateIntervalMillis(2000L)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return null since this is a started service, not a bound service
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop receiving location updates when the service is destroyed
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
