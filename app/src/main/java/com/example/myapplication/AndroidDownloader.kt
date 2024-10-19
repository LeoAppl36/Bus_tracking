package com.example.myapplication

import android.app.DownloadManager
import android.content.Context
import android.util.Log
import androidx.core.net.toUri


class AndroidDownloader (private val context: Context): Downloader{
    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    override fun downloadFile(url: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setTitle("gtfs_rapid_bus_penang")
            .setDestinationInExternalFilesDir(context,"res/stopinfo" ,"gtfs_rapid_bus_penang.zip")
        return downloadManager.enqueue(request)
    }
}

