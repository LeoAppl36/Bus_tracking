package com.example.myapplication.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    const val LOCATION_REQUEST_CODE = 1

    // Check if location permission has been granted
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permission from the user
    fun requestLocationPermission(activity: Activity) {
        // Check if we should show an explanation for why the permission is needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Show an explanation to the user asynchronously
            showPermissionRationaleDialog(activity) {
                // Retry permission request after explanation
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_REQUEST_CODE
                )
            }
        } else {
            // No explanation needed, directly request permission
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }
    }

    // Show a rationale dialog explaining why location permissions are needed
    private fun showPermissionRationaleDialog(activity: Activity, onProceed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs location access to show your current location and nearby buses. Please allow access to continue.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onProceed()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Do nothing, user has denied permission
            }
            .create()
            .show()
    }
}
