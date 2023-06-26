package com.d0st.bhadoozindex.test

import android.content.Context
import android.util.Log
import android.view.View
import com.d0st.bhadoozindex.utils.DwnHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okio.buffer
import okio.sink
import java.io.File

/* In this logic,  downloadPart  function is used to download a single part using OkHttp.
It takes the part number, OkHttp client instance, download URL, and directory path as input.
It creates a request with the given URL and part number, executes it using the OkHttp client,
and saves the response body to a file in the specified directory.
downloadParts  function is used to download all the parts in parallel. It takes the download URL,
directory path, and the total number of parts as input. It creates an OkHttp client instance and
launches coroutines to download 10 parts at a time.
It waits for all the coroutines to complete before launching the next batch of 10.
The  main  function is used to run the  downloadParts  function in a blocking manner using the  runBlocking  function
from the  kotlinx.coroutines  library.
 */

class Downloader4 {

    suspend fun downloadPart(
        partNumber: Int,
        okHttpClient: OkHttpClient,
        url: String,
        directory: String,
        ctx:Context,
        view: View
    ) {
        val request = Request.Builder().url("$url.part$partNumber").build()
        val ur = "$url.part$partNumber"
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            Log.d("Downloader4", "Part Downloaded $partNumber")
            val file = File("$directory/part-$partNumber.mp4")
            file.writeBytes(response.body.bytes() ?: byteArrayOf())
//            DwnHelper.startDownload(ctx, view, "part-$partNumber", ur)
//            response.body.let { body ->
//                body.source().buffer.use { input ->
//                    file.sink().buffer().use { output ->
//                        output.writeAll(input)
//                    }
//                }
//            }
        }
    }

    private suspend fun downloadParts(url: String, directory: String, partsCount: Int,ctx:Context, view: View) {
        val logging: HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)

         val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        coroutineScope {
            val jobs = mutableListOf<Job>()
            for (i in 1..partsCount step 5) {
                for (j in i..i + 9) {
                    Log.d("Downloader4","Part No = $j")
                    Log.d("Downloader4","URL = $url.part$j")
                    if (j > partsCount) break
                    jobs += launch (Dispatchers.IO){
                        downloadPart(j, client, url+j, directory,ctx,view)
                    }
                }
                jobs.joinAll()
            }
        }
    }

    fun main(url:String,outPath:String,ctx:Context, view: View) {
        runBlocking {
            downloadParts(url, outPath, 65,ctx,view)
        }
    }

}