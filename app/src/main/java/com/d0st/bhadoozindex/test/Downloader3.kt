package com.d0st.bhadoozindex.test

import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.d0st.bhadoozindex.Utils
import com.d0st.bhadoozindex.dto.Cdn
import com.tonyodev.fetch2.Download
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

sealed class DownloadState {
    data class CurrentState(val state: ArrayList<String>) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class Downloader3 {

    private val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"
    private val timeOutPartList = mutableListOf<Int>()
    private var _response = MutableLiveData<DownloadState>()
    val respose = _response
    private val currentState = ArrayList<String>()
    private val runningRequest = ArrayList<Int>()

    private suspend fun okHttp(
        start: Int,
        end: Int,
        partNumber: Int,
        url: String
    ): List<ByteArray> =
        coroutineScope {
            println("Part Start Download = $partNumber")

            val chunks = mutableListOf<ByteArray>()
            val client = OkHttpClient.Builder()
                .connectionPool(ConnectionPool())
//                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .callTimeout(5, TimeUnit.MINUTES)
                .build()

            val urll = "$url.part$partNumber"

             launch(Dispatchers.IO) {
                val requests = Request.Builder()
                    .url(urll)
                    .tag(partNumber)
                    .build()

                try {
                    val response = client.newCall(requests).execute().use { response ->
                        response.body.bytes()
                    }
//                    println("Downloaded: $partNumber")
                    chunks.add(response)
                    currentState.add("Downloaded : $partNumber")
                        .also { _response.postValue(DownloadState.CurrentState(state = currentState)) }

                } catch (e: Exception) {
                    timeOutPartList.add(partNumber)
                    currentState.add("Part Download Error : $partNumber = ${e.message}").also {
                        _response.postValue(DownloadState.CurrentState(state = currentState))
                    }
                    Log.wtf("Downloader3", "okHttp Error $partNumber = ${e.message}")
                }
            }
            return@coroutineScope chunks

        }


    private suspend fun downloadParallelParts(
        startIndex: Int,
        endIndex: Int,
        url: String,
        externalCacheDir: String
    ) {
        try {
            coroutineScope {

                runningRequest.add(startIndex + 1)
                runningRequest.add(endIndex + 1)

                println("Request = ${runningRequest.first()}, ${runningRequest.last()}")

                val jobs = mutableListOf<Job>()
                for (partNumber in startIndex..endIndex) {

                    currentState.add("Part Downloading : ${partNumber+1}")
                        .also {
                            _response.postValue(DownloadState.CurrentState(state = currentState))
                        }

                    val job = launch {
                        val part = okHttp(startIndex, endIndex, partNumber + 1, url)
                        val file = File("$externalCacheDir/${partNumber + 1}.bin")
                        part.forEach { file.appendBytes(it) }
                    }
                    jobs.add(job)
                }
                println("-----------Jobs size - ${jobs.size}---------------")
                jobs.joinAll()
            }
        } catch (e: Exception) {
            currentState.add("Parallel Download Error = $e").also {
                _response.postValue(DownloadState.CurrentState(state = currentState))
            }
            Log.wtf("Downloader3", "Parallel Error = $e")
        }
    }

    suspend fun main(Json: Cdn, url: String, ctx: Context) {
        val batchSize = 5
        val partCount = AtomicInteger(0)
        currentState.add("Idle").also {
            _response.postValue(DownloadState.CurrentState(state = currentState))
        }

        val externalCacheString = "${ctx.externalCacheDir}/Parts"
        val externalCacheFolder = File(externalCacheString)
        if (!externalCacheFolder.exists()) {
            externalCacheFolder.mkdirs()
        }


        while (partCount.get() < Json.parts) {
            val startIndex = partCount.getAndAdd(batchSize)
            val endIndex = (startIndex + batchSize - 1).coerceAtMost(Json.parts - 1)
            Log.d("Download3", "endIndex = $endIndex")
            Log.d("Download3", "startIndex = $startIndex")

            downloadParallelParts(startIndex, endIndex, url, externalCacheString)
        }


        currentState.add("*******************************").also {
            _response.postValue(DownloadState.CurrentState(state = currentState))
        }

        coroutineScope {
            launch {
                currentState.add("Parts Merging Start").also {
                    _response.postValue(DownloadState.CurrentState(state = currentState))
                }
                joinFiles("$externalCacheString/", "${outPath}${Json.name}")
                currentState.add("File has Downloaded").also {
                    _response.postValue(DownloadState.CurrentState(state = currentState))
                }
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
    private suspend fun joinFiles(inputDir: String, outputFile: String) =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(inputDir)
                val files = dir.listFiles { _, name -> name.endsWith(".bin") }
                val out = FileOutputStream(outputFile)
                files?.sortedBy { it.nameWithoutExtension.toInt() }?.forEach { file ->
//                    Log.d("Downloader3", "Sorted files = $file")
                    val input =
                        FileInputStream(file).buffered()  // Default Buffer Size - 8192 (8 * 1024)
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