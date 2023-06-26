package com.d0st.bhadoozindex.test

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class Downloader7 {

    suspend fun downloadChunk(chunkNumber: Int): ByteArray {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://example.com/chunk/$chunkNumber")
            .build()
        val response = client.newCall(request).execute()
        return response.body.bytes() ?: throw Exception("Failed to download chunk $chunkNumber")
    }
    suspend fun downloadChunks(start: Int, end: Int,url:String): List<ByteArray> = coroutineScope {
        val chunks = mutableListOf<ByteArray>()
        val client = OkHttpClient()
        val requests = (start..end).map { chunkNumber ->
            Log.d("Downloader7","Url = $url.part$chunkNumber")
            Request.Builder().url("$url.part$chunkNumber").build()
        }
        val responses = requests.map { request ->
            async(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    response.body.bytes() ?: throw Exception("Failed to download chunk ${request.url}")
                }
            }
        }
        responses.forEach { chunks.add(it.await()) }
        chunks
    }
    suspend fun downloadAndJoinChunks(start: Int, end: Int, fileName: String,url:String) {
        val chunks = downloadChunks(start, end,url)
        val file = File(fileName)
        chunks.forEach { chunk -> file.appendBytes(chunk) }
    }
    suspend fun downloadAllChunks(url:String) {
        val chunksPerBatch = 3
        val totalChunks = 65
        var chunksDownloaded = 0
        val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"

        while (chunksDownloaded < totalChunks) {
            val remainingChunks = totalChunks - chunksDownloaded
            val chunksToDownload = if (remainingChunks >= chunksPerBatch) chunksPerBatch else remainingChunks
            val startChunk = chunksDownloaded + 1
            val endChunk = chunksDownloaded + chunksToDownload
            downloadAndJoinChunks(startChunk, endChunk, "${outPath}_chunks_${startChunk}_to_${endChunk}.octet-stream",url)
            chunksDownloaded += chunksToDownload
            if (chunksDownloaded >= 10) {
                downloadAndJoinChunks(1, 10, "${outPath}_first_10_chunks.octet-stream",url)
            }
        }
        downloadAndJoinChunks(1, totalChunks, "${outPath}_all_chunks.octet-stream",url)
    }
    fun main(url:String) = runBlocking(Dispatchers.Default) {
        downloadAllChunks(url)
    }

}