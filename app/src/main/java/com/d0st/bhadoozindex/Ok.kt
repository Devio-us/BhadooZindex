package com.d0st.bhadoozindex

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException

class Ok (private val client: OkHttpClient) {
    suspend fun get(url: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Unexpected response code: ${response.code}"))
                }

                val responseBody = response.body?.string() ?: ""
                Result.success(responseBody)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun post(url: String, requestBody: RequestBody): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).post(requestBody).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Unexpected response code: ${response.code}"))
                }

                val responseBody = response.body?.string() ?: ""
                Result.success(responseBody)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Add more request methods as needed (PUT, DELETE, etc.)
}
