package com.d0st.bhadoozindex

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d0st.bhadoozindex.dto.Cdn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import javax.inject.Inject

sealed class DownloadState {
    object Idle : DownloadState()
    object Downloading : DownloadState()
    data class PartDownloaded(val partIndex: Int) : DownloadState()
    object Joining : DownloadState()
    data class Error(val message: String) : DownloadState()
}

@HiltViewModel
class HmViewModel @Inject constructor() : ViewModel() {

    val uRl1 = "https://23307459.small-file-testing.pages.dev/8f47ffd636bee9c586b9170c2e868886183a4c5f6e7d390919742863318113eb.json"
    val uRl = "https://cdn-2.storage.zindex.eu.org/afff84584619ed805f8fa103a3164881a4b28e4510ede04bbd46e3720b33d165.json"
    var logging: HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
    private var _response = MutableLiveData<DownloadState>()
    val respose = _response

    fun loadAndCancel(onSuccess: (Cdn) -> Unit) {
        viewModelScope.launch {
            val getResult = Ok(client).get(uRl)

            if (getResult.isSuccess) {
                val response = getResult.getOrElse { "" }

                val obj: Cdn = Json.decodeFromString(string = response)
                println("GET request successful. Response: $response")

                println("name = ${obj.name} , size = ${obj.size} , partDetail = ${obj.part_details}")
                onSuccess.invoke(obj)
            } else {
                val exception = getResult.exceptionOrNull()
                println("GET request failed: ${exception?.message}")
            }
        }
    }

     fun downloadFileInParts(
        fileUrl: String,
        numParts: Int,
        outputFile: File
    ): MutableLiveData<DownloadState> {
        val live = MutableLiveData<DownloadState>()
        live.value = DownloadState.Downloading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val parts = (0 until numParts).map { partIndex ->
                    println("URL = $fileUrl${partIndex + 1}")
                    async(Dispatchers.IO) {

                        val request = Request.Builder()
                            .url("$fileUrl${partIndex + 1}")
                            .build()

                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            response.body?.let { savePartToFile(it, partIndex) }
                            live.postValue(DownloadState.PartDownloaded(partIndex))
                        } else {
                            throw Exception("Failed to download part $partIndex")
                        }
                    }
                }

                parts.awaitAll()

                withContext(Dispatchers.IO) {
                    live.postValue(DownloadState.Joining)
                    joinPartsToFile(numParts, outputFile)
                }
                live.postValue(DownloadState.Idle)
            } catch (e: Exception) {
                live.postValue(DownloadState.Error(e.message ?: "Unknown error occurred"))
            }

        }
        return live
    }

    fun savePartToFile(responseBody: ResponseBody, partIndex: Int) {

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

//        val file: File = File(path, "/$fname")
        val outputFile = File(path, "/part_$partIndex.part")
        responseBody.byteStream().use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    fun joinPartsToFile(numParts: Int, outputFile: File) {
        println("Part = $numParts , file = $outputFile")
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        outputFile.outputStream().use { outputStream ->
            for (partIndex in 0 until numParts) {
                val partFile = File(path, "/part_$partIndex.part")
                partFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
                partFile.delete()
            }
        }
    }

}