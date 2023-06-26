package com.d0st.bhadoozindex.test

import android.content.Context
import android.util.Log
import android.view.View
import com.d0st.bhadoozindex.utils.DwnHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class Downloader3 (private val view: View, private val ctx:Context, private val url:String){

    private suspend fun downloadPart(partNumber: Int): String {
        delay(10000)
        val fileName = "test$partNumber.mkv"
        val fileUrl = "$url.part$partNumber"
        DwnHelper.startDownload(ctx, view, fileName, fileUrl)
        return "Part $partNumber"
    }

    private suspend fun downloadParallelParts(startIndex: Int, endIndex: Int) {
        coroutineScope {
            val jobs = mutableListOf<Job>()
            for (partNumber in startIndex..endIndex) {
                val job = launch {
                    val part = downloadPart(partNumber)
                    println("Downloaded: $part")
                }
                jobs.add(job)
            }
            jobs.joinAll()
        }
    }

    suspend fun main() {
        val totalParts = 65
        val batchSize = 10

        val partCount = AtomicInteger(0)

        while (partCount.get() < totalParts) {
            val startIndex = partCount.getAndAdd(batchSize)
            val endIndex = (startIndex + batchSize - 1).coerceAtMost(totalParts - 1)
            Log.d("Download3","endIndex = $endIndex")
            Log.d("Download3","startIndex = $startIndex")
            downloadParallelParts(startIndex, endIndex)
        }
    }


}