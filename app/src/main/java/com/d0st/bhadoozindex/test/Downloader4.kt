package com.d0st.bhadoozindex.test

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class Downloader4 {

    suspend fun downloadPart(
        partNumber: Int,
        okHttpClient: OkHttpClient,
        url: String,
        directory: String
    ) {
        val request = Request.Builder()
            .url("$url/$partNumber")
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val file = File("$directory/part-$partNumber")
            file.writeBytes(response.body?.bytes() ?: byteArrayOf())
        }
    }

    suspend fun downloadParts(url: String, directory: String, partsCount: Int) {
        val okHttpClient = OkHttpClient()
        coroutineScope {
            val jobs = mutableListOf<Job>()
            for (i in 1..partsCount step 10) {
                for (j in i..i + 9) {
                    if (j > partsCount) break
                    jobs += launch {
                        downloadPart(j, okHttpClient, url, directory)
                    }
                }
                jobs.joinAll()
            }
        }
    }

    fun main() {
        runBlocking {
            downloadParts("https://example.com/download", "/path/to/directory", 65)
        }
    }

}