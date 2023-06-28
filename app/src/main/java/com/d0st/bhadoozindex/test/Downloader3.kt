package com.d0st.bhadoozindex.test

import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.d0st.bhadoozindex.Utils
import com.tonyodev.fetch2.Download
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

sealed class DownloadState {
    data class CurrentState(val state:ArrayList<String>) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class Downloader3 {

    private val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"
    private val timeOutPartList = mutableListOf<Int>()
    private var _response = MutableLiveData<DownloadState>()
    val respose = _response
    private val currentState = ArrayList<String>()
    val latch = CountDownLatch(1)
    val pauseInterceptor = Utils.PauseInterceptor(latch)

     suspend fun okHttp(start: Int, end: Int, partNumber: Int,url: String): List<ByteArray> =
        coroutineScope {

            val chunks = mutableListOf<ByteArray>()
            val client = OkHttpClient.Builder()
                .connectionPool(ConnectionPool())
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .addInterceptor(pauseInterceptor)
                .build()

            val url = "$url.part$partNumber"
//            Log.d("Downloader3", "Url = $url")
            if (pauseInterceptor.isPaused) {
                Log.wtf("Downloader3","Downloading is Paused")
            } else {
                Log.wtf("Downloader3","Downloading is not Paused")

            }

            val responseDeferred = async(Dispatchers.IO) {
                val requests = Request.Builder()
                    .url(url)
                    .build()

                try {
                    val response = client.newCall(requests).execute().use { response ->
                        response.body.bytes()
                    }
//                    println("Downloaded: $partNumber")
//                    chunks.add(response)
                    response
                } catch (e: Exception) {
                    timeOutPartList.add(partNumber)
                    currentState.clear()
                    currentState.add("Part Download Error : $partNumber = ${e.message}")
                    _response.postValue(DownloadState.CurrentState(state =  currentState))
                    Log.wtf("Downloader3", "okHttp Error $partNumber = ${e.message}")
                    null
                }
            }
            responseDeferred.await()?.let {
                println("Downloaded: $partNumber")
                currentState.clear()
                currentState.add("Downloaded : $partNumber")
                _response.postValue(DownloadState.CurrentState(state =  currentState))
                chunks.add(it)
            }
//            if (response != null) {
//                chunks.add(response)
//            }

//        responses.forEach { chunks.add(it.await()) }
            return@coroutineScope chunks

        }

    private suspend fun downloadParallelParts(startIndex: Int, endIndex: Int,url: String) {
        try {
            coroutineScope {

                val jobs = mutableListOf<Job>()
                for (partNumber in startIndex..endIndex) {
                    val job = launch {
//                    val part = downloadPart(partNumber)
                        val part = okHttp(startIndex, endIndex, partNumber + 1,url)
                        val file = File("${outPath}Parts/${partNumber + 1}.mkv")
                        part.forEach { file.appendBytes(it) }
                    }
                    jobs.add(job)
                }
                jobs.joinAll()
            }
        } catch (e: Exception) {
            currentState.clear()
            currentState.add("Parallel Download Error = $e")
            _response.postValue(DownloadState.CurrentState(state =  currentState))
            Log.wtf("Downloader3", "Parallel Error = $e")
        }
    }

    suspend fun main(totalParts: Int,url: String) {
        val totalPartss = 13
        val batchSize = 5
        val partCount = AtomicInteger(0)
        currentState.add("Idle")
        _response.postValue(DownloadState.CurrentState(state =  currentState))

        while (partCount.get() < totalParts) {
            val startIndex = partCount.getAndAdd(batchSize)
            val endIndex = (startIndex + batchSize - 1).coerceAtMost(totalParts - 1)
            Log.d("Download3", "endIndex = $endIndex")
            Log.d("Download3", "startIndex = $startIndex")
            currentState.clear()
            currentState.add("Parts Downloading : $startIndex to $endIndex")
            _response.postValue(DownloadState.CurrentState(state =  currentState))
            downloadParallelParts(startIndex, endIndex,url)
        }

        currentState.clear()
        currentState.add("*******************************")
        _response.postValue(DownloadState.CurrentState(state =  currentState))

        coroutineScope {
            launch {
                currentState.clear()
                currentState.add("Parts Merging Start")
                _response.postValue(DownloadState.CurrentState(state =  currentState))
                joinFiles("${outPath}Parts/", "${outPath}Out/file.mkv")
                currentState.clear()
                currentState.add("File has Downloaded")
                _response.postValue(DownloadState.CurrentState(state =  currentState))
            }
        }
//        Log.d("Downloader3", "while Outer Called")
    }

//    class FileJoiner(private val inputDir: String, private val outputFile: String) {

        /*V1*/
//        suspend fun joinFiles() = withContext(Dispatchers.IO) {
//            try {
//                val dir = File(inputDir)
//                val files = dir.listFiles { _, name -> name.endsWith(".mkv") }
//                val out = FileOutputStream(outputFile)
//                if (files != null) {
//                    for (file in files.sortedBy { it.nameWithoutExtension.toInt() }) {
//                        Log.d("Downloader3", "files = ${file}")
//                        val input = FileInputStream(file)
//                        val buffer = ByteArray(4096)
//                        var length: Int
//                        while (input.read(buffer).also { length = it } > 0) {
//                            out.write(buffer, 0, length)
//                        }
//                        input.close()
//                        file.delete()
//                    }
//                }
//                out.close()
//                Log.d("Downloader3", "Complete Merging")
//            } catch (e: Exception) {
//                Log.wtf("Downloader3", "File Merging Error = ${e.message}")
//            }
//        }

        /*V2 GPT*/
//        suspend fun joinFiles() = withContext(Dispatchers.IO) {
//            try {
//                val dir = File(inputDir)
//                val files = dir.listFiles { _, name -> name.endsWith(".mkv") }
//                files?.sortedBy { it.nameWithoutExtension.toInt() }?.forEach { file ->
//                    Log.d("Downloader3", "files = $file")
//                    FileInputStream(file).use { input ->
//                        FileOutputStream(outputFile, true).use { out ->
//                            val buffer = ByteArray(4096)
//                            var length: Int
//                            while (input.read(buffer).also { length = it } > 0) {
//                                out.write(buffer, 0, length)
//                            }
//                        }
//                    }
//                    file.delete()
//                }
//                Log.d("Downloader3", "Complete Merging")
//            } catch (e: Exception) {
//                Log.wtf("Downloader3", "File Merging Error = ${e.message}")
//            }
//        }


        /*V3 Bito*/
//        suspend fun joinFiles() = withContext(Dispatchers.IO) {
//            try {
//                val dir = File(inputDir)
//                val files = dir.listFiles { _, name -> name.endsWith(".mkv") }
//                if (files != null) {
//                    val outputStream = BufferedOutputStream(FileOutputStream(outputFile))
//                    val buffer = ByteArray(4096)
//                    for (file in files.sortedBy { it.nameWithoutExtension.toInt() }) {
//                        Log.d("Downloader3", "files = ${file}")
//                        val inputStream = BufferedInputStream(FileInputStream(file))
//                        var length: Int
//                        while (inputStream.read(buffer).also { length = it } > 0) {
//                            outputStream.write(buffer, 0, length)
//                        }
//                        inputStream.close()
//                        file.delete()
//                    }
//                    outputStream.close()
//                }
//                Log.d("Downloader3", "Complete Merging")
//            } catch (e: Exception) {
//                Log.wtf("Downloader3", "File Merging Error = ${e.message}")
//            }
//        }

        /*V4 Gpt */
        /*        suspend fun joinFiles() = withContext(Dispatchers.IO) {
                    try {
                        val dir = File(inputDir)
                        val files = dir.listFiles { _, name -> name.endsWith(".mkv") }
                        val out = FileOutputStream(outputFile)
                        files?.sortedBy { it.nameWithoutExtension.toInt() }?.forEach { file ->
                            Log.d("Downloader3", "files = $file")
                            FileInputStream(file).use { input ->
                                input.copyTo(out)
        //                        input.close()
                            }

                            file.delete()
                        }
                        out.close()
                        Log.d("Downloader3", "Complete Merging")
                    } catch (e: Exception) {
                        Log.wtf("Downloader3", "File Merging Error = ${e.message}")
                    }
                }*/

        /*V5 GPT*/
        private suspend fun joinFiles(inputDir: String, outputFile: String) = withContext(Dispatchers.IO) {
            try {
                val dir = File(inputDir)
                val files = dir.listFiles { _, name -> name.endsWith(".mkv") }
                val out = FileOutputStream(outputFile)
                files?.sortedBy { it.nameWithoutExtension.toInt() }?.forEach { file ->
//                    Log.d("Downloader3", "files = $file")
                    val input = FileInputStream(file).buffered()
                    input.copyTo(out)
                    input.close()
                    file.delete()
                }
                out.close()
//                Log.d("Downloader3", "Complete Merging")
            } catch (e: Exception) {
//                Log.wtf("Downloader3", "File Merging Error = ${e.message}")
            }
        }

//    }



}