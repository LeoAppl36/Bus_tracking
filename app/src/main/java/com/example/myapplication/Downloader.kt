package com.example.myapplication

interface Downloader {
    fun downloadFile(url: String): Long
}