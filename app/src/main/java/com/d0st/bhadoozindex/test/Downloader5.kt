package com.d0st.bhadoozindex.test

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/*
* In this code,  downloadPart()  function is used to download a single part using OkHttp.
downloadParts()  function is used to download a range of parts, from  start  to  end ,
by calling  downloadPart()  function in parallel using coroutines.
downloadAllParts()  function is the main function that downloads all 65 parts in batches of 10,
until all parts are downloaded. Each batch of parts is downloaded using  downloadParts()  function,
and the downloaded parts are saved to files with names like "part1.bin", "part2.bin", etc.
The code uses  runBlocking  to run the main function in a blocking way,
so that the program doesn't exit until all parts are downloaded.
*/

class Downloader5 {

    private suspend fun downloadPart(partNumber: Int, url:String): ByteArray = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        Log.d("Downloader5", "URL = $url.part$partNumber")
        val request = Request.Builder()
            .url("$url.part$partNumber")
            .build()
        val response = client.newCall(request).execute()
        response.body.bytes()
    }

    suspend fun downloadParts(start: Int, end: Int,url:String): List<ByteArray> {
        val parts = mutableListOf<ByteArray>()
        for (i in start..end) {
//            Log.d("Downloader5","URL = $url")
            parts.add(downloadPart(i,url))
        }
        return parts
    }

    suspend fun downloadAllParts(directory:String,url: String) {
        val partsPerBatch = 5
        val totalParts = 65
        var partsDownloaded = 0
//        val jobs = mutableListOf<Job>()
//        coroutineScope {
            while (partsDownloaded < totalParts) {
                val remainingParts = totalParts - partsDownloaded
                val partsToDownload = if (remainingParts >= partsPerBatch) partsPerBatch else remainingParts
                val startPart = partsDownloaded + 1
                val endPart = partsDownloaded + partsToDownload
//                jobs += launch(Dispatchers.IO) {
                    val parts = downloadParts(startPart, endPart,url)
                    parts.forEachIndexed { index, part ->
                        val fileName = "$directory${startPart + index}.bin"
                        Log.d("Downloader5","Part No = ${startPart+index}")
                        Log.d("Downloader5","Directory = $fileName")
                        File(fileName).writeBytes(part)
                    }
//                }
                partsDownloaded += partsToDownload
            }
//        }
    }

    fun main(directory: String,url: String) = runBlocking {
        downloadAllParts(directory,url)
    }

}