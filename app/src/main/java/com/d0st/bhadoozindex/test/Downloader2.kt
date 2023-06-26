package com.d0st.bhadoozindex.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File

suspend fun downloadFile(url: String, outputFile: File) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    val response = client.newCall(request).execute()

    response.body?.let { body ->
        body.source().buffer().use { input ->
            outputFile.sink().buffer().use { output ->
                output.writeAll(input)
            }
        }
    }
}

suspend fun downloadFileInParts(fileUrl: String, parts: Int, outputDir: File) {
    coroutineScope {
        val jobs = (1..parts).map { part ->
            async(Dispatchers.IO) {
                val partUrl = "$fileUrl.part$part"
                val outputFile = File(outputDir, "part$part")
                downloadFile(partUrl, outputFile)
            }
        }
        jobs.awaitAll()
    }
}

suspend fun joinDownloadedParts(partsDir: File, outputDir: File) {
    outputDir.outputStream().use { output ->
        partsDir.listFiles()?.sortedBy { it.name }?.forEach { partFile ->
            partFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }
    }
}

suspend fun main(
    fileUrl: String,
    numParts: Int,
    outputtDir: File,
    createDir: String
) {
    val concurrentLimit = 5
    val outputDir = File("${createDir}output")
    outputDir.mkdirs()

    val partsDir = File(createDir, "parts")
    partsDir.mkdirs()

    downloadFileInParts(fileUrl, numParts, partsDir)

    val partFiles = partsDir.listFiles()?.toList()
    partFiles?.chunked(concurrentLimit)?.forEach { chunk ->
        coroutineScope {
            chunk.map { partFile ->
                async(Dispatchers.IO) {
                    val outputFile = File(outputDir, partFile.nameWithoutExtension)
                    joinDownloadedParts(partFile, outputFile)
                }
            }.awaitAll()
        }
    }

    println("Download completed.")
}
