package com.d0st.bhadoozindex.test

import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.View
import com.d0st.bhadoozindex.utils.DwnHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class Downloader3(private val view: View, private val ctx: Context, private val url: String) {

    val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"

    val timeOutPartList = mutableListOf<Int>()

    private suspend fun downloadPart(partNumber: Int): String {
        delay(10000)
        val fileName = "test$partNumber.mkv"
        val fileUrl = "$url.part$partNumber"
//        DwnHelper.startDownload(ctx, view, fileName, fileUrl)
        return "Part $partNumber"
    }

    private suspend fun okHttp(start: Int, end: Int, partNumber: Int): List<ByteArray> =
        coroutineScope {

            val chunks = mutableListOf<ByteArray>()
            val client = OkHttpClient.Builder()
                .callTimeout(1, TimeUnit.MINUTES)
                .build()

            val url = "$url.part$partNumber"
//        val requests = (start..end).map { chunkNumber ->
            Log.d("Downloader3", "Url = $url")
//            Request.Builder().url("$url.part$chunkNumber").build()
//        }
//        val responses = requests.map { request ->
//            async(Dispatchers.IO) {
//                client.newCall(request).execute().use { response ->
//                    response.body.bytes() ?: throw Exception("Failed to download chunk ${request.url}")
//                }
//            }
//        }

            launch(Dispatchers.IO) {
                val requests = Request.Builder()
                    .url(url)
                    .build()

                try {
                    val response = client.newCall(requests).execute().use { response ->
                        response.body.bytes()
                    }
                    println("Downloaded: $partNumber")
                    chunks.add(response)
                } catch (e: Exception) {
                    timeOutPartList.add(partNumber)
                    Log.wtf("Downloader3", "okHttp Error $partNumber = ${e.message}")
                }
            }

//        responses.forEach { chunks.add(it.await()) }
            return@coroutineScope chunks

        }

    private suspend fun downloadParallelParts(startIndex: Int, endIndex: Int) {
        try {
            coroutineScope {

                val jobs = mutableListOf<Job>()
                for (partNumber in startIndex..endIndex) {
                    val job = launch {
//                    val part = downloadPart(partNumber)
                        val part = okHttp(startIndex, endIndex, partNumber + 1)
                        val file = File("${outPath}Parts/${partNumber + 1}.mkv")
                        part.forEach { file.appendBytes(it) }
                    }
                    jobs.add(job)
                }
                jobs.joinAll()
            }
        }catch (e:Exception){
            Log.wtf("Downloader3","Parallel Error = $e")
        }
    }

    suspend fun main(totalParts: Int) {
        val totalPartss = 13
        val batchSize = 5

        val partCount = AtomicInteger(0)

        while (partCount.get() < totalParts) {
            val startIndex = partCount.getAndAdd(batchSize)
            val endIndex = (startIndex + batchSize - 1).coerceAtMost(totalParts - 1)
            Log.d("Download3", "endIndex = $endIndex")
            Log.d("Download3", "startIndex = $startIndex")
            downloadParallelParts(startIndex, endIndex)
        }

        coroutineScope {
//            val inputDir = "/path/to/Download/folder"
//            val outputFile = "/path/to/output/file.mkv"

            launch {
                FileJoiner("${outPath}Parts/", "${outPath}Out/file.mkv").joinFiles()
            }
        }
        Log.d("Downloader3", "while Outer Called")
    }

    class FileJoiner(private val inputDir: String, private val outputFile: String) {

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
        suspend fun joinFiles() = withContext(Dispatchers.IO) {
            try {
                val dir = File(inputDir)
                val files = dir.listFiles { _, name -> name.endsWith(".mkv") }
                val out = FileOutputStream(outputFile)
                files?.sortedBy { it.nameWithoutExtension.toInt() }?.forEach { file ->
                    Log.d("Downloader3", "files = $file")
                    val input = FileInputStream(file).buffered()
                    input.copyTo(out)
                    input.close()
                    file.delete()
                }
                out.close()
                Log.d("Downloader3", "Complete Merging")
            } catch (e: Exception) {
                Log.wtf("Downloader3", "File Merging Error = ${e.message}")
            }
        }

    }

}