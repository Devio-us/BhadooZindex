package com.d0st.bhadoozindex.test

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.*
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class Downloader8 {

     val PARTS_PER_DOWNLOAD = 10
     val TOTAL_PARTS = 68
     val BASE_URL = "https://cdn-2.storage.zindex.eu.org/afff84584619ed805f8fa103a3164881a4b28e4510ede04bbd46e3720b33d165"
     val MAX_SIMULTANEOUS_DOWNLOADS = 5
    val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"

    val client = OkHttpClient()

    private suspend fun downloadMovieParts(partRange: IntRange) {
        val jobs = mutableListOf<Deferred<Unit>>()

        for (partNumber in partRange) {
            val job = CoroutineScope(Dispatchers.IO).async {
                val url = BASE_URL + "part.$partNumber"
                Log.d("Downloader8","URL = $url")
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                response.body.byteStream().use { inputStream ->
                    val outputFile = File("${outPath}_part$partNumber")
                    val outputStream = FileOutputStream(outputFile)
                    val buffer = ByteArray(4096)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.close()
                }
            }

            jobs.add(job)
        }

        jobs.awaitAll()
    }

    private suspend fun joinMovieParts() {

        val outputFile = File("${outPath}_full_movie.mp4")
        withContext(Dispatchers.IO) {
            val outputStream = FileOutputStream(outputFile)

            for (partNumber in 1..TOTAL_PARTS) {
                val inputFile = File("${outPath}_part.$partNumber")
                val inputStream = inputFile.inputStream()
                val buffer = ByteArray(4096)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                inputStream.close()
            }

            outputStream.close()
        }
    }

    fun main() {
        runBlocking {
            val downloadJobs = mutableListOf<Job>()
            var startPart = 1

            while (startPart <= TOTAL_PARTS) {
                val endPart = minOf(startPart + PARTS_PER_DOWNLOAD - 1, TOTAL_PARTS)
                val partRange = startPart..endPart

                val job = launch {
                    downloadMovieParts(partRange)
                }
                downloadJobs.add(job)

                startPart += PARTS_PER_DOWNLOAD

                // Limit the number of simultaneous downloads
                if (downloadJobs.size >= MAX_SIMULTANEOUS_DOWNLOADS) {
                    downloadJobs.joinAll()
                    downloadJobs.clear()
                }
            }

            // Wait for any remaining download jobs
            downloadJobs.joinAll()

            joinMovieParts()
            println("Movie downloaded and joined successfully!")
        }
    }

}