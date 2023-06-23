package com.d0st.bhadoozindex

import android.R.attr.path
import android.os.Bundle
import android.os.Environment
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.d0st.bhadoozindex.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.File


const val PART_SIZE = 1024 * 1024 // 1MB
class MainActivity : AppCompatActivity() {

    private val vm: HmViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    val uRl1 = "https://23307459.small-file-testing.pages.dev/8f47ffd636bee9c586b9170c2e868886183a4c5f6e7d390919742863318113eb"
    val uRl = "https://cdn-2.storage.zindex.eu.org/afff84584619ed805f8fa103a3164881a4b28e4510ede04bbd46e3720b33d165"
//    "http://storage.zindex.eu.org/"
    val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/" //location where the pdf will store


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileUrl = "$uRl.part"
//        val numParts = 2
//        val partSize = 25 * 1024 * 1024 // 25 MB in bytes
        val outputFile = File("${outPath}output.mkv")

        binding.get.setOnClickListener {

            Permission.verifyStoragePermission(this) {

                vm.loadAndCancel { onSuccess ->

                    println("part Size = ${onSuccess.parts}")
//                    main()
                    val live =  vm.downloadFileInParts(fileUrl, onSuccess.parts, outputFile)
                    live.observe(this) { state ->
                        // Handle the download state change here
                        when (state) {
                            is DownloadState.Idle -> {
                                // Download has not started.
                                println("State = Idle")
                            }

                            is DownloadState.Downloading -> {
                                // Downloading...
                                println("State = Downloading")
                            }

                            is DownloadState.PartDownloaded -> {
                                // Part ${state.partIndex + 1} downloaded.
                                println("State = PartDownloaded ${state.partIndex}")
                            }

                            is DownloadState.Joining -> {
                                // Joining downloaded parts...
                                println("State = Joining")
                            }

                            is DownloadState.Error -> {
                                // An error occurred: state.errorMessage
                                println("State = Error ${state.message}")
                            }
                        }
                    }
                }
            }

        }
//        initFetch()


    }

//    fun main() {
//        val fileUrl = "$uRl.part"
//        val numParts = 2
//        val partSize = 25 * 1024 * 1024 // 25 MB in bytes
//        val outputFile = File("${outPath}output.mp4")
//
//        downloadFileInParts(fileUrl, numParts, outputFile)
//    }
//
//    private fun initFetch() {
//        respose.observe(this) { response ->
//            when (response) {
//                is DownloadState.Idle -> {
//                    println("State = Idle")
//                }
//                is DownloadState.Downloading -> {
//                    println("State = Downloading")
//                }
//                is DownloadState.PartDownloaded -> {
//                    println("State = PartDownloaded ${response.partIndex}")
//                }
//                is DownloadState.Joining -> {
//                    println("State = Joining")
//                }
//                is DownloadState.Error -> {
//                    println("State = Error")
//                }
//            }
//        }
//    }

}