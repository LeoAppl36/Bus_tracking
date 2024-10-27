package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
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
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.PlaceAdapter
import com.example.myapplication.models.BusCar
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.protobuf.util.JsonFormat
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayList
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode

//class MainActivity : AppCompatActivity(), OnMapReadyCallback {
//    val file = File(Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo"), "gtfs_rapid_bus_penang.zip")
//    val destination = Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo")
//    var stop: List<Stop>? = null
//    private lateinit var googleMap: GoogleMap
//    //immediate update for current location
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//    val url = URL("https://api.data.gov.my/gtfs-realtime/arrival_time_update/prasarana?category=rapid-bus-penang")
//
//    @OptIn(DelicateCoroutinesApi::class)
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//
//        val mapView = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//        mapView!!.getMapAsync(this) // Load the map
//
//        // Ensure we have location permissions
//        if (!PermissionUtils.hasLocationPermission(this)) {
//            PermissionUtils.requestLocationPermission(this)
//        }
//
//        val executor = Executors.newSingleThreadScheduledExecutor()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//
//            if (!file.exists()) {
//                val url = "https://api.data.gov.my/gtfs-static/prasarana?category=rapid-bus-penang"
//                val downloader = AndroidDownloader(this)
//                downloader.downloadFile(url)
//            }
//            val url = URL("https://api.data.gov.my/gtfs-realtime/trip-updates/prasarana?category=rapid-bus-penang")
//
//            val agencycheck = File(Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo"), "agency.txt")
//            val stopscheck = File(Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo"), "stops.txt")
//
//            GlobalScope.launch {
//                while (!agencycheck.exists() || !stopscheck.exists()) {
//                    if (file.exists()) {
//                        unzipFolder(file, destination.toPath())
//                        break
//                    } else {
//                        delay(1000L)
//                    }
//                }
//                delay(6000L)
//            }
//
//            val lines = readFile(stopscheck).drop(1)
//
//            // Convert each line into a Stop object and save it to a list
//            stop = lines.map { line ->
//                parseLineToStop(line)
//            }
//
//            insets
//        }
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        this.googleMap = googleMap
//
//        // Now fetch and show user's current location
//        if (PermissionUtils.hasLocationPermission(this)) {
//            LocationUtils.getCurrentLocation(this) { location ->
//                location?.let {
//                    val userLatLng = LatLng(it.latitude, it.longitude)
//
//                    // Add marker for user's current location
//                    googleMap.addMarker(
//                        MarkerOptions()
//                            .position(userLatLng)
//                            .title("You are here")
//                    )
//
//                    // Move the camera to the user's current location
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
//
//                    // Filter and show nearest bus stops
//                    showNearestBusStops(it, 200.0,500.0) // 1000 meters radius
//                }
//            }
//        }
//    }
//    private fun showNearestBusStops(userLocation: Location, initialRadius: Double, expandedRadius: Double) {
//        val stopsInRange = mutableListOf<Stop>()
//        var nearestStop: Stop? = null
//        var nearestDistance: Float = Float.MAX_VALUE
//
//        // First search within the initial radius
//        stop!!.forEach { stop ->
//            val stopLocation = Location("").apply {
//                latitude = stop.stop_lat
//                longitude = stop.stop_lon
//            }
//
//            // Calculate the distance between user and the stop
//            val distance = userLocation.distanceTo(stopLocation)
//
//            // If the stop is within the initial radius, add it to the list
//            if (distance <= initialRadius) {
//                stopsInRange.add(stop)
//            }
//
//            // Track the nearest stop, even if outside the initial radius
//            if (distance < nearestDistance) {
//                nearestStop = stop
//                nearestDistance = distance
//            }
//        }
//
//        // If there are no stops in the initial radius, expand the range
//        if (stopsInRange.isEmpty()) {
//            stop!!.forEach { stop ->
//                val stopLocation = Location("").apply {
//                    latitude = stop.stop_lat
//                    longitude = stop.stop_lon
//                }
//
//                // Calculate the distance between user and the stop
//                val distance = userLocation.distanceTo(stopLocation)
//
//                // Add stops within the expanded radius to the list
//                if (distance <= expandedRadius) {
//                    stopsInRange.add(stop)
//                }
//            }
//        }
//
//        // Show the stops on the map
//        if (stopsInRange.isNotEmpty()) {
//            stopsInRange.forEach { stop ->
//                googleMap.addMarker(
//                    MarkerOptions()
//                        .position(LatLng(stop.stop_lat, stop.stop_lon))
//                        .title(stop.stop_name)
//                        .icon(bitmapDescriptorFromVector(this, R.drawable.bus_stop))
//                )
//            }
//        } else {
//            // If no stops are found even in the expanded radius, show the nearest stop
//            nearestStop?.let {
//                googleMap.addMarker(
//                    MarkerOptions()
//                        .position(LatLng(it.stop_lat, it.stop_lon))
//                        .title("Nearest stop: ${it.stop_name}")
//                        .icon(bitmapDescriptorFromVector(this, R.drawable.bus_stop))
//                )
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.stop_lat, it.stop_lon), 15f))
//            } ?: run {
//                // Handle the case where no stops are found at all
//                Toast.makeText(this, "No bus stops available", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    @Throws(IOException::class)
//    fun unzipFolder(source: File, target: Path?) {
//        ZipInputStream(FileInputStream(source)).use { zis ->
//            var zipEntry = zis.nextEntry
//            while (zipEntry != null) {
//                val newPath = zipSlipProtect(zipEntry, target!!)
//                if (zipEntry.isDirectory) {
//                    Files.createDirectories(newPath)
//                } else {
//                    if (newPath.parent != null && Files.notExists(newPath.parent)) {
//                        Files.createDirectories(newPath.parent)
//                    }
//                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING)
//                }
//                zipEntry = zis.nextEntry
//            }
//            zis.closeEntry()
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    @Throws(IOException::class)
//    fun zipSlipProtect(zipEntry: ZipEntry, targetDir: Path): Path {
//        val targetDirResolved = targetDir.resolve(zipEntry.name)
//        val normalizePath = targetDirResolved.normalize()
//        if (!normalizePath.startsWith(targetDir)) {
//            throw IOException("Bad zip entry: " + zipEntry.name)
//        }
//        return normalizePath
//    }
//
//    private fun readFile(file: File): List<String> {
//        return file.readLines()
//    }
//
//    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
//        return ContextCompat.getDrawable(context, vectorResId)?.run {
//            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
//            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
//            draw(Canvas(bitmap))
//            BitmapDescriptorFactory.fromBitmap(bitmap)
//        }
//    }
//
//    private fun parseLineToStop(line: String): Stop {
//        val data = line.split(",")
//        return Stop(data[0], data[1], data[2], data[3].toDouble(), data[4].toDouble())
//    }
//
//}
class MainActivity : AppCompatActivity(), OnMapReadyCallback, PlaceAdapter.PlaceClickListener {
    val file = File(Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo"), "gtfs_rapid_bus_penang.zip")
    val destination = Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo")
    var stop: List<Stop>? = null
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocationMarker: Marker? = null
    val url = URL("https://api.data.gov.my/gtfs-realtime/arrival_time_update/prasarana?category=rapid-bus-penang")

    private var stops: List<Stop>? = null

    private lateinit var rvPlace: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var placesClient: PlacesClient
    private var predictionsList = ArrayList<AutocompletePrediction>()
    private lateinit var adapter: PlaceAdapter
    private lateinit var btnSearchBus: Button

    private var busCarList = arrayListOf<BusCar>()

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        rvPlace = findViewById(R.id.rvPlace)
        searchView = findViewById(R.id.searchView)
        btnSearchBus = findViewById(R.id.btnSearchBus)

        // Initialize the Places API with your API key
        Places.initialize(applicationContext, "AIzaSyA0efG-0BamtRemIFEKC_NFS-8nTF9TXyo")
        placesClient = Places.createClient(this)

        // Setup RecyclerView
        rvPlace.setHasFixedSize(true)
        rvPlace.layoutManager = LinearLayoutManager(this)
        adapter = PlaceAdapter(predictionsList,this) // Assuming you update the adapter to handle AutocompletePrediction
        rvPlace.adapter = adapter

        // Setup SearchView to listen for text input and trigger API calls
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotEmpty()) {
                    rvPlace.visibility = View.VISIBLE
                    rvPlace.setBackgroundResource(R.drawable.search_bg)
                    fetchPlacePredictions(newText)
                }else
                    rvPlace.visibility = View.INVISIBLE
                return true
            }
        })

        //Search Bus that will arrive at the destination
        btnSearchBus.setOnClickListener {
            val intent = Intent(this, BusCarListActivity::class.java)
            intent.putParcelableArrayListExtra("busCars", ArrayList(busCarList))
            startActivity(intent)
        }

        GlobalScope.launch {
            fetchGtfsRealtimeFeed()
        }

        val mapView = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapView!!.getMapAsync(this)

        // Load GTFS stops data
        val stopsFile = File(Environment.getExternalStoragePublicDirectory("/Android/data/com.example.myapplication/files/res/stopinfo"), "stops.txt")
        stops = parseGtfsStops(stopsFile)

        // Ensure we have location permissions
        if (!PermissionUtils.hasLocationPermission(this)) {
            PermissionUtils.requestLocationPermission(this)
        } else {
            startLocationUpdates()
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

    private fun fetchPlacePredictions(query: String) {
        // Create a new request for autocomplete predictions
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        // Call the API to get predictions
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictionsList.clear()
                predictionsList.addAll(response.autocompletePredictions)
                adapter.notifyDataSetChanged() // Notify the adapter of new data
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching predictions: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        val nearbyStops = findNearbyStops(5.4133100, 100.3207620, stops!!, 500.0)
        showNearbyStopsOnMap(googleMap, nearbyStops, this)

        if (PermissionUtils.hasLocationPermission(this)) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        LocationUtils.startLocationUpdates(this) { location ->
            updateUserLocationOnMap(location)
            showNearestBusStops(location, 200.0, 500.0)
        }
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val location1 = Location("").apply {
            latitude = lat1
            longitude = lon1
        }
        val location2 = Location("").apply {
            latitude = lat2
            longitude = lon2
        }
        return location1.distanceTo(location2)
    }

    fun findNearbyStops(userLat: Double, userLon: Double, stops: List<Stop>, radius: Double): List<Stop> {
        return stops.filter { stop ->
            calculateDistance(userLat, userLon, stop.stop_lat, stop.stop_lon) <= radius
        }
    }

    fun showNearbyStopsOnMap(googleMap: GoogleMap, nearbyStops: List<Stop>, context: Context) {
        nearbyStops.forEach { stop ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(stop.stop_lat, stop.stop_lon))
                    .title(stop.stop_name)
                    .icon(bitmapDescriptorFromVector(context, R.drawable.bus_stop))
            )
        }
    }

    private fun updateUserLocationOnMap(location: Location) {
        val userLatLng = LatLng(5.4133100, 100.3207620)

        if (userLocationMarker == null) {
            userLocationMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(userLatLng)
                    .title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
        } else {
            userLocationMarker?.position = userLatLng
        }
    }

    private fun showNearestBusStops(userLocation: Location, initialRadius: Double, expandedRadius: Double) {
        val stopsInRange = mutableListOf<Stop>()
        var nearestStop: Stop? = null
        var nearestDistance: Float = Float.MAX_VALUE

        // First search within the initial radius
        var count = 0
        stop!!.forEach { stop ->
            count++
            val stopLocation = Location("").apply {
                Log.d("Bus Stops=====$count","${stop.stop_id}, ${stop.stop_code}")
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
                Log.d("Stop===","${stop.stop_id}, ${stop.stop_code}")
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
                Log.d("Stop=====","${it.stop_id}, ${it.stop_code}")
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationUtils.stopLocationUpdates()
    }

    private suspend fun fetchGtfsRealtimeFeed() {
        val url = "https://api.data.gov.my/gtfs-realtime/vehicle-position/prasarana?category=rapid-bus-penang"

        // OkHttp client to fetch the GTFS Realtime feed
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        try {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            if (response.isSuccessful) {
                val feed = GtfsRealtime.FeedMessage.parseFrom(response.body().byteStream())
                val jsonPrinter = JsonFormat.printer()
                Log.d("Feed==========",feed.toString())
                Log.d("JsonPrinter==========",jsonPrinter.toString())

                // Convert each vehicle position to a JSON string
                val vehiclePositions = feed.entityList.map { entity ->
                    jsonPrinter.print(entity.vehicle)
                }

                // Convert JSON string to List of Maps (equivalent to pandas' DataFrame)
                val mapper = ObjectMapper().findAndRegisterModules()
                val vehicleData: List<Unit> = vehiclePositions.map { json ->
                    mapper.readValue<Map<String, Any>>(json)
                }

                // Print the vehicle information
                Log.d("Total vehicles: ===========","${vehicleData.size}")
                vehicleData.forEach {
                    println(it)
                    Log.d("VehicleData==========",it.toString())
                }

            } else {
                println("Failed to fetch GTFS feed: ${response.message()}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onPlaceClick(prediction: AutocompletePrediction) {
        rvPlace.visibility = View.INVISIBLE
        val placeId = prediction.placeId
        getPlaceLatLng(placeId) { placeLatLng ->
            placeLatLng?.let {
                googleMap.addMarker(MarkerOptions().position(it).title(prediction.getPrimaryText(null).toString()))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                // Fetch and draw directions
                val currentLocation = userLocationMarker?.position
                if (currentLocation != null) {
                    getDirections(currentLocation, it) { directionsResult ->
                        directionsResult?.let { result ->
                            drawPolyline(result)
                        }
                    }
                }
                // Fetch bus arrival times
//                GlobalScope.launch {
//                    val busArrivalTimes = fetchBusArrivalTimes(it)
//                    withContext(Dispatchers.Main) {
//                        if (busArrivalTimes.isNotEmpty()) {
//                            busArrivalTimes.forEach { arrivalTime ->
//                                Log.d("Bus Arrival Time", arrivalTime)
//                                //Toast.makeText(this@MainActivity, arrivalTime, Toast.LENGTH_SHORT).show()
//                            }
//                        } else {
//                            Toast.makeText(this@MainActivity, "No buses arriving at this destination", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }

                // Find the nearest bus stop and fetch bus arrival times
//                val nearestStop = findNearestStopFromDestination(it, stops!!)
//                Log.d("Nearest Stop", nearestStop.toString())
//                nearestStop?.let { stop ->
//                    GlobalScope.launch {
//                        val busArrivalTimes = fetchBusTripUpdatesForStop(stop.stop_id)
//                        withContext(Dispatchers.Main) {
//                            Log.d("Bus Size", busArrivalTimes.size.toString())
//                            if (busArrivalTimes.isNotEmpty()) {
//                                Log.d("Show Bus====", busArrivalTimes.toString())
//                                busArrivalTimes.forEach { arrivalTime ->
//                                    Toast.makeText(this@MainActivity, arrivalTime, Toast.LENGTH_SHORT).show()
//                                }
//                            } else {
//                                Toast.makeText(this@MainActivity, "No buses arriving at the nearest stop", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                } ?: run {
//                    Toast.makeText(this, "No nearest bus stop found", Toast.LENGTH_SHORT).show()
//                }


                GlobalScope.launch {
                    val nearestStop = findNearestStopFromDestination(it, stops!!)
                    Log.d("Nearest Stop", nearestStop.toString())
                    val busTripUpdates = fetchBusTripUpdates()
                    val busCars = getBusCarsForStop(nearestStop!!, busTripUpdates)
                    if(busCars.isNotEmpty()) {
                        busCars.forEach { busCar ->
                            var busCarId = busCar.trip.routeId.toString()
                            var busCarLatLng = LatLng(busCar.position.latitude.toDouble(), busCar.position.longitude.toDouble())
                            var busCarLicense = busCar.vehicle.licensePlate.toString()
                            var bus = BusCar(busCarId, busCarLatLng, busCarLicense)
                            busCarList.add(bus)
                        }
                        Log.d("Bus Cars", busCars.toString())
                    }else{
                        Log.d("Bus Cars", "No bus cars found")
                    }

//                    withContext(Dispatchers.Main) {
//                        displayBusCarsOnMap(googleMap, busCars, this@MainActivity)
//                    }
                }

            }
        }
    }

    fun findNearestStopFromDestination(destination: LatLng, stops: List<Stop>): Stop? {
        var nearestStop: Stop? = null
        var nearestDistance: Float = Float.MAX_VALUE

        stops.forEach { stop ->
            val stopLocation = Location("").apply {
                latitude = stop.stop_lat
                longitude = stop.stop_lon
            }
            val destinationLocation = Location("").apply {
                latitude = destination.latitude
                longitude = destination.longitude
            }
            val distance = destinationLocation.distanceTo(stopLocation)
            if (distance < nearestDistance) {
                nearestStop = stop
                nearestDistance = distance
            }
        }
        return nearestStop
    }

    suspend fun fetchBusTripUpdatesForStop(stopId: String): List<String> {
        val url = "https://api.data.gov.my/gtfs-realtime/vehicle-position/prasarana?category=rapid-bus-penang"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val busTripUpdates = mutableListOf<String>()

        try {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            if (response.isSuccessful) {
                val feed = GtfsRealtime.FeedMessage.parseFrom(response.body().byteStream())
                Log.d("Feeee=============",feed.toString())
                feed.entityList.forEach { entity ->
                    Log.d("Entity Content List", entity.toString())
                    if (entity.hasVehicle()) {
                        val vehicle = entity.vehicle
                        Log.d("Vehicle Content", vehicle.toString())
                        if (vehicle.stopId == stopId) {
                            Log.d("Vehicle Content Same", "Yes")
                            busTripUpdates.add("Bus ${vehicle.vehicle.id} arriving at stop ${stopId}")
                        }else{
                            Log.d("Vehicle Content Same", "No")
                        }
                    } else {
                        Log.d("Entity Content", "No vehicle in entity")
                    }
                }
            } else {
                Log.e("FetchBusTripUpdates", "Failed to fetch GTFS feed: ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("FetchBusTripUpdates", "Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e("FetchBusTripUpdates", "Unexpected error: ${e.message}")
        }
        return busTripUpdates
    }

    suspend fun fetchBusTripUpdates(): List<GtfsRealtime.FeedEntity> {
        val url = "https://api.data.gov.my/gtfs-realtime/vehicle-position/prasarana?category=rapid-bus-penang"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val busTripUpdates = mutableListOf<GtfsRealtime.FeedEntity>()

        try {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            if (response.isSuccessful) {
                val feed = GtfsRealtime.FeedMessage.parseFrom(response.body().byteStream())
                busTripUpdates.addAll(feed.entityList)
            } else {
                Log.e("FetchBusTripUpdates", "Failed to fetch GTFS feed: ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("FetchBusTripUpdates", "Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e("FetchBusTripUpdates", "Unexpected error: ${e.message}")
        }
        return busTripUpdates
    }

    fun getBusCarsForStop(stop: Stop, busTripUpdates: List<GtfsRealtime.FeedEntity>): List<GtfsRealtime.VehiclePosition> {
        val busCars = mutableListOf<GtfsRealtime.VehiclePosition>()
        busTripUpdates.forEach { entity ->
            if (entity.hasVehicle()) {
                val vehiclePosition = entity.vehicle
                val stopLocation = Location("").apply {
                    latitude = stop.stop_lat
                    longitude = stop.stop_lon
                }
                val vehicleLocation = Location("").apply {
                    latitude = vehiclePosition.position.latitude.toDouble()
                    longitude = vehiclePosition.position.longitude.toDouble()
                }
                val distance = stopLocation.distanceTo(vehicleLocation)
                if (distance <= 500) { // Assuming 500 meters as the range
                    busCars.add(vehiclePosition)
                }
            }
        }
        return busCars
    }

    suspend fun fetchBusArrivalTimesForStop(stopId: String): List<String> {
        val url = "https://api.data.gov.my/gtfs-realtime/arrival_time_update/prasarana?category=rapid-bus-penang"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val busArrivalTimes = mutableListOf<String>()

        try {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            if (response.isSuccessful) {
                val feed = GtfsRealtime.FeedMessage.parseFrom(response.body().byteStream())
                feed.entityList.forEach { entity ->
                    val tripUpdate = entity.tripUpdate
                    tripUpdate.stopTimeUpdateList.forEach { stopTimeUpdate ->
                        if (stopTimeUpdate.stopId == stopId) {
                            busArrivalTimes.add("Bus ${tripUpdate.trip.tripId} arriving at ${stopTimeUpdate.arrival.time}")
                        }
                    }
                }
            } else {
                Log.e("FetchBusArrivalTimes", "Failed to fetch GTFS feed: ${response.message()}")
            }
        } catch (e: IOException) {
            Log.e("FetchBusArrivalTimes", "Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e("FetchBusArrivalTimes", "Unexpected error: ${e.message}")
        }
        return busArrivalTimes
    }
    suspend fun fetchBusArrivalTimes(destination: LatLng): List<String> {
        val url = "https://api.data.gov.my/gtfs-realtime/arrival_time_update/prasarana?category=rapid-bus-penang"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val busArrivalTimes = mutableListOf<String>()

        try {
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            if (response.isSuccessful) {
                val feed = GtfsRealtime.FeedMessage.parseFrom(response.body().byteStream())
                feed.entityList.forEach { entity ->
                    val tripUpdate = entity.tripUpdate
                    tripUpdate.stopTimeUpdateList.forEach { stopTimeUpdate ->
                        val stopLatLng = getStopLatLng(stopTimeUpdate.stopId, stops!!)
                        if (stopLatLng == destination) {
                            busArrivalTimes.add("Bus ${tripUpdate.trip.tripId} arriving at ${stopTimeUpdate.arrival.time}")
                            Log.d("Bus Arrival Time======", "Bus ${tripUpdate.trip.tripId} arriving at ${stopTimeUpdate.arrival.time}")
                        }
                    }
                }
            } else {
                println("Failed to fetch GTFS feed: ${response.message()}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return busArrivalTimes
    }

    fun getStopLatLng(stopId: String, stops: List<Stop>): LatLng? {
        val stop = stops.find { it.stop_id == stopId }
        return stop?.let { LatLng(it.stop_lat, it.stop_lon) }
    }

    fun getDirections(origin: LatLng, destination: LatLng, callback: (DirectionsResult?) -> Unit) {
        val context = GeoApiContext.Builder()
            .apiKey("AIzaSyA0efG-0BamtRemIFEKC_NFS-8nTF9TXyo")
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = DirectionsApi.newRequest(context)
                    .mode(TravelMode.DRIVING)
                    .origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .await()
                withContext(Dispatchers.Main) {
                    callback(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    fun drawPolyline(directionsResult: DirectionsResult) {
        val path = ArrayList<LatLng>()
        if (directionsResult.routes.isNotEmpty()) {
            val route = directionsResult.routes[0]
            if (route.legs.isNotEmpty()) {
                val leg = route.legs[0]
                for (step in leg.steps) {
                    val points = step.polyline.decodePath()
                    for (point in points) {
                        path.add(LatLng(point.lat, point.lng))
                    }
                }
            }
        }
        googleMap.addPolyline(PolylineOptions().addAll(path).color(ContextCompat.getColor(this, R.color.blue)).width(10f))
    }

    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Function to get place LatLng
    fun getPlaceLatLng(placeId: String, callback: (LatLng?) -> Unit) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            callback(place.latLng)
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            callback(null)
        }
    }
}
