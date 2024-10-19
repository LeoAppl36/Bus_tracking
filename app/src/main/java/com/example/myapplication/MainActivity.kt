package com.example.myapplication

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.transit.realtime.GtfsRealtime
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import com.google.android.gms.location.*
import com.example.myapplication.utils.LocationUtils
import com.example.myapplication.utils.PermissionUtils
import android.content.Intent
import android.widget.Toast

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    val file = File(Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo"), "gtfs_rapid_bus_penang.zip")
    val destination = Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo")
    var stop: List<Stop>? = null
    private lateinit var googleMap: GoogleMap
    //immediate update for current location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    val url = URL("https://api.data.gov.my/gtfs-realtime/arrival_time_update/prasarana?category=rapid-bus-penang")

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val mapView = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapView!!.getMapAsync(this) // Load the map

        // Ensure we have location permissions
        if (!PermissionUtils.hasLocationPermission(this)) {
            PermissionUtils.requestLocationPermission(this)
        }

        val executor = Executors.newSingleThreadScheduledExecutor()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            if (!file.exists()) {
                val url = "https://api.data.gov.my/gtfs-static/prasarana?category=rapid-bus-penang"
                val downloader = AndroidDownloader(this)
                downloader.downloadFile(url)
            }
            val url = URL("https://api.data.gov.my/gtfs-realtime/trip-updates/prasarana?category=rapid-bus-penang")

            val agencycheck = File(Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo"), "agency.txt")
            val stopscheck = File(Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo"), "stops.txt")

            GlobalScope.launch {
                while (!agencycheck.exists() || !stopscheck.exists()) {
                    if (file.exists()) {
                        unzipFolder(file, destination.toPath())
                        break
                    } else {
                        delay(1000L)
                    }
                }
                delay(6000L)
            }

            val lines = readFile(stopscheck).drop(1)

            // Convert each line into a Stop object and save it to a list
            stop = lines.map { line ->
                parseLineToStop(line)
            }

            insets
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Now fetch and show user's current location
        if (PermissionUtils.hasLocationPermission(this)) {
            LocationUtils.getCurrentLocation(this) { location ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)

                    // Add marker for user's current location
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("You are here")
                    )

                    // Move the camera to the user's current location
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                    // Filter and show nearest bus stops
                    showNearestBusStops(it, 200.0,500.0) // 1000 meters radius
                }
            }
        }
    }
    private fun showNearestBusStops(userLocation: Location, initialRadius: Double, expandedRadius: Double) {
        val stopsInRange = mutableListOf<Stop>()
        var nearestStop: Stop? = null
        var nearestDistance: Float = Float.MAX_VALUE

        // First search within the initial radius
        stop!!.forEach { stop ->
            val stopLocation = Location("").apply {
                latitude = stop.stop_lat
                longitude = stop.stop_lon
            }

            // Calculate the distance between user and the stop
            val distance = userLocation.distanceTo(stopLocation)

            // If the stop is within the initial radius, add it to the list
            if (distance <= initialRadius) {
                stopsInRange.add(stop)
            }

            // Track the nearest stop, even if outside the initial radius
            if (distance < nearestDistance) {
                nearestStop = stop
                nearestDistance = distance
            }
        }

        // If there are no stops in the initial radius, expand the range
        if (stopsInRange.isEmpty()) {
            stop!!.forEach { stop ->
                val stopLocation = Location("").apply {
                    latitude = stop.stop_lat
                    longitude = stop.stop_lon
                }

                // Calculate the distance between user and the stop
                val distance = userLocation.distanceTo(stopLocation)

                // Add stops within the expanded radius to the list
                if (distance <= expandedRadius) {
                    stopsInRange.add(stop)
                }
            }
        }

        // Show the stops on the map
        if (stopsInRange.isNotEmpty()) {
            stopsInRange.forEach { stop ->
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(stop.stop_lat, stop.stop_lon))
                        .title(stop.stop_name)
                        .icon(bitmapDescriptorFromVector(this, R.drawable.bus_stop))
                )
            }
        } else {
            // If no stops are found even in the expanded radius, show the nearest stop
            nearestStop?.let {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.stop_lat, it.stop_lon))
                        .title("Nearest stop: ${it.stop_name}")
                        .icon(bitmapDescriptorFromVector(this, R.drawable.bus_stop))
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.stop_lat, it.stop_lon), 15f))
            } ?: run {
                // Handle the case where no stops are found at all
                Toast.makeText(this, "No bus stops available", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun unzipFolder(source: File, target: Path?) {
        ZipInputStream(FileInputStream(source)).use { zis ->
            var zipEntry = zis.nextEntry
            while (zipEntry != null) {
                val newPath = zipSlipProtect(zipEntry, target!!)
                if (zipEntry.isDirectory) {
                    Files.createDirectories(newPath)
                } else {
                    if (newPath.parent != null && Files.notExists(newPath.parent)) {
                        Files.createDirectories(newPath.parent)
                    }
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING)
                }
                zipEntry = zis.nextEntry
            }
            zis.closeEntry()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun zipSlipProtect(zipEntry: ZipEntry, targetDir: Path): Path {
        val targetDirResolved = targetDir.resolve(zipEntry.name)
        val normalizePath = targetDirResolved.normalize()
        if (!normalizePath.startsWith(targetDir)) {
            throw IOException("Bad zip entry: " + zipEntry.name)
        }
        return normalizePath
    }

    private fun readFile(file: File): List<String> {
        return file.readLines()
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun parseLineToStop(line: String): Stop {
        val data = line.split(",")
        return Stop(data[0], data[1], data[2], data[3].toDouble(), data[4].toDouble())
    }

}
