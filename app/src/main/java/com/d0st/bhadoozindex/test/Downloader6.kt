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

/*
In this code,downloadChunk() function is used to download a single chunk using OkHttp. downloadChunks()function is used to download a range of chunks,from start to end ,
in parallel using OkHttp's asynchronous API. Requests for each chunk are created using a loop, and then exec uted asynchronously using  async  builder.
The  responses  list will contain the responses for each chunk,
and  forEach  loop is used to extract the byte array from each response and add it to the  chunks  list.
downloadAndJoinChunks()  function is used to download a range of chunks, from  start  to  end ,
and join them into a single file with the given  fileName . The function calls  downloadChunks()  to download the chunks,
and then appends each chunk to the file using  File.appendBytes() .
downloadAllChunks()  function is the main function that downloads all 65 chunks in batches of 10,
until the first 10 chunks are downloaded. Each batch of chunks is downloaded using  downloadAndJoinChunks()  function,
and the downloaded chunks are joined into a file with a name like "chunks_1_to_10.octet-stream", "chunks_11_to_20.octet-stream", etc.
After the first 10 chunks are downloaded, the first 10 chunks are joined into a separate file called "first_10_chunks.octet-stream".*/


class Downloader6 {

//    suspend fun downloadChunk(chunkNumber: Int,url:String): ByteArray {
//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url("http://example.com/chunk/$chunkNumber")
//            .build()
//        val response = client.newCall(request).execute()
//        return response.body.bytes() ?: throw Exception("Failed to download chunk $chunkNumber")
//    }
    suspend fun downloadChunks(start: Int, end: Int,url:String): List<ByteArray> = coroutineScope {
        val chunks = mutableListOf<ByteArray>()
        val client = OkHttpClient()
        val requests = (start..end).map { chunkNumber ->
            Log.d("Downloader6","Url = $url.part$chunkNumber")
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
        val chunksPerBatch = 5
        val totalChunks = 65
        var chunksDownloaded = 0
        val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"

        while (chunksDownloaded < totalChunks) {
            val remainingChunks = totalChunks - chunksDownloaded
            val chunksToDownload = if (remainingChunks >= chunksPerBatch) chunksPerBatch else remainingChunks
            val startChunk = chunksDownloaded + 1
            val endChunk = chunksDownloaded + chunksToDownload
            downloadAndJoinChunks(startChunk, endChunk, "${outPath}_chunks_${startChunk}_to_${endChunk}.mp4",url)
            chunksDownloaded += chunksToDownload
            if (chunksDownloaded >= 10) {
                Log.d("Downloader6","URI - ${outPath}_first_10_chunks.mp4")
                downloadAndJoinChunks(1, 10, "${outPath}_first_10_chunks.mp4",url)
                break
            }
        }
    }
    fun main(url:String) = runBlocking {
        downloadAllChunks(url)
    }

}