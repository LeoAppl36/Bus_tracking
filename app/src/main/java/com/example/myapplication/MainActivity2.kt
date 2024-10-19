//package com.example.myapplication
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.google.transit.realtime.GtfsRealtime.FeedMessage
//import java.net.URL
//import android.util.Log
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.IOException
//
//class MainActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Launch a coroutine to perform the network operation
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // URL of the GTFS-Realtime feed
//
//                // Parse the protobuf data from the URL
//                val feed = FeedMessage.parseFrom(url.openStream())
//
//                // Process the feed on the IO thread
//                for (entity in feed.entityList) {
//                    if (entity.hasTripUpdate()) {
//                        // Move to the Main thread to update the UI
//                        withContext(Dispatchers.Main) {
//                            Log.d("MainActivity", entity.tripUpdate.toString())
//                        }
//                    }
//                }
//
//            } catch (e: IOException) {
//                e.printStackTrace()
//                withContext(Dispatchers.Main) {
//                    Log.e("MainActivity", "Error fetching GTFS data: ${e.message}")
//                }
//            }
//        }
//    }
//}