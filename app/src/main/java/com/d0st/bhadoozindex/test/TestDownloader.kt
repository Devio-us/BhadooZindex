package com.d0st.bhadoozindex.test

import com.d0st.bhadoozindex.dto.Cdn
import com.lyrebirdstudio.fileboxlib.core.FileBoxConfig
import com.lyrebirdstudio.fileboxlib.core.FileBoxMultiRequest
import com.lyrebirdstudio.fileboxlib.core.FileBoxMultiResponse
import com.lyrebirdstudio.fileboxlib.core.FileBoxProvider
import com.lyrebirdstudio.fileboxlib.core.FileBoxRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class TestDownloader {
    val uRl = "https://23307459.small-file-testing.pages.dev/8f47ffd636bee9c586b9170c2e868886183a4c5f6e7d390919742863318113eb"
    lateinit var disposable: Disposable

//    fun withFileBoc (onSuccess: Cdn){
//        println("onSuccess Part = ${onSuccess.part_details["1"]}")
//        val fileBoxMultipleRequest = FileBoxMultiRequest(
//            arrayListOf(
//                FileBoxRequest("$uRl.part1"),
//                FileBoxRequest("$uRl.part2"),
////                        FileBoxRequest("https://url3.json"),
////                        FileBoxRequest("https://url4.mp4")
//            )
//        )
//
//        val fileBox = FileBoxProvider.newInstance(applicationContext, FileBoxConfig.createDefault())
//
//        disposable = fileBox.get(fileBoxMultipleRequest)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { fileBoxResponse ->
//                when (fileBoxResponse) {
//                    is FileBoxMultiResponse.Downloading -> {
//                        val progress = fileBoxResponse.progress
//                        val ongoingFileResponseList = fileBoxResponse.fileBoxResponseList
//                        println("Downloading = $ongoingFileResponseList")
//                    }
//                    is FileBoxMultiResponse.Complete -> {
//                        val ongoingFileResponseList = fileBoxResponse.fileBoxResponseList
//                        val comp = ongoingFileResponseList.map { return@map it.record.getReadableFilePath() }
//                        println("Complete = $comp")
//                    }
//                    is FileBoxMultiResponse.Error -> {
//                        val error = fileBoxResponse.throwable
//                        val ongoingFileResponseList = fileBoxResponse.fileBoxResponseList
//                    }
//                }
//            }
//    }


}