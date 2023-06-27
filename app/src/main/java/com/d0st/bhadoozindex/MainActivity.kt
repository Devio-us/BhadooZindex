package com.d0st.bhadoozindex

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d0st.bhadoozindex.databinding.ActivityMainBinding
import com.d0st.bhadoozindex.test.Downloader3
import com.d0st.bhadoozindex.test.Downloader4
import com.d0st.bhadoozindex.test.Downloader5
import com.d0st.bhadoozindex.test.Downloader6
import com.d0st.bhadoozindex.test.Downloader7
import com.d0st.bhadoozindex.test.Downloader8
import com.d0st.bhadoozindex.test.TestDownloader
import com.d0st.bhadoozindex.test.main
import com.d0st.bhadoozindex.utils.ActionListener
import com.d0st.bhadoozindex.utils.DwnAdapter
import com.d0st.bhadoozindex.utils.DwnHelper
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.File
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator


class MainActivity : AppCompatActivity() , ActionListener {

    private val vm: HmViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val unknownRemainingTime: Long = -1
    private val unknownDownloadedBytesPerSecond: Long = 0
    private val fetchNamespace = "MvilaaDownload"

    private var fetch: Fetch? = null
    private var fileAdapter: DwnAdapter? = null
    var recyclerView: RecyclerView? = null


    val uRl1 = "https://23307459.small-file-testing.pages.dev/8f47ffd636bee9c586b9170c2e868886183a4c5f6e7d390919742863318113eb"
    val mb720 = "https://cdn-2.storage.zindex.eu.org/afff84584619ed805f8fa103a3164881a4b28e4510ede04bbd46e3720b33d165"
    private val gb3_13 = "https://cdn-2.storage.zindex.eu.org/890c13f5cf13970a5d902b931bf7962698456f41e784d4364d2a2663379d785a"

    //    "http://storage.zindex.eu.org/"
    val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"


    /* Pause Algorithm = First Check Which bunch is currently downloading if 15 to 20 bunch is in process then pause download and check which file
        is downloaded in bunch like 15 is downloaded then start download from 16
    */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        initDow()

        binding.get.setOnClickListener {

            Permission.verifyStoragePermission(this) {

                vm.loadAndCancel { onSuccess ->

                    lifecycleScope.launch {
                        Downloader3(binding.root,this@MainActivity,gb3_13).main(onSuccess.parts)
                    }

//                    println("part Size = ${onSuccess.parts}")


                }
            }

        }

    }

    private fun download1(url:String,part:Int,file:File){
        val live =  vm.downloadFileInParts(url, part, file)
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

    private fun initDow(){

        setUpViews()
        val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(this)
            .setDownloadConcurrentLimit(1)
            .enableHashCheck(true)
            .setGlobalNetworkType(NetworkType.ALL)
            .enableFileExistChecks(true)
            .enableRetryOnNetworkGain(true)
            .setHttpDownloader(OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
            .setNamespace(fetchNamespace)
            .build()

        fetch = Fetch.getInstance(fetchConfiguration)
    }

    private fun setUpViews() {
        recyclerView = binding.recyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        fileAdapter = DwnAdapter(this)
        recyclerView!!.adapter = fileAdapter
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        fetch?.getDownloads { downloads ->
            val list: ArrayList<Download> = ArrayList(downloads)
            Collections.sort(list, Comparator.comparingLong(Download::created))
            list.reverse()
            for (download in list) {
                fileAdapter!!.addDownload(download)
            }
            if (downloads.isEmpty()) {
                binding.noDownload.visibility = View.VISIBLE
                recyclerView!!.visibility = View.GONE
            } else {
                binding.noDownload.visibility = View.GONE
                recyclerView!!.visibility = View.VISIBLE

            }
        }?.addListener(fetchListener)
    }

    private val fetchListener: FetchListener = object : AbstractFetchListener() {
        override fun onAdded(download: Download) {
            super.onAdded(download)
            fileAdapter!!.addDownload(download)
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            fileAdapter!!.update(
                download,
                unknownRemainingTime,
                unknownDownloadedBytesPerSecond
            )
        }

        override fun onCompleted(download: Download) {
            fileAdapter!!.update(
                download,
                unknownRemainingTime,
                unknownDownloadedBytesPerSecond
            )
        }

        override fun onProgress(
            download: Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            fileAdapter!!.update(download, etaInMilliSeconds, downloadedBytesPerSecond)
        }

        override fun onPaused(download: Download) {
            fileAdapter!!.update(
                download,
                unknownRemainingTime,
                unknownDownloadedBytesPerSecond
            )
        }

        override fun onResumed(download: Download) {
            fileAdapter!!.update(
                download,
                unknownRemainingTime,
                unknownDownloadedBytesPerSecond
            )
        }

        override fun onCancelled(download: Download) {
            fileAdapter!!.update(
                download,
                unknownRemainingTime,
                unknownDownloadedBytesPerSecond
            )
        }

        override fun onRemoved(download: Download) {
            fileAdapter!!.update(
                download,
                unknownRemainingTime,
                unknownDownloadedBytesPerSecond
            )
        }

        override fun onDeleted(download: Download) {
            fileAdapter!!.update(
                download,
                unknownRemainingTime,
                unknownDownloadedBytesPerSecond
            )
        }


    }

    override fun onPauseDownload(id: Int) {
        fetch?.pause(id)
    }

    override fun onResumeDownload(id: Int) {
        fetch?.resume(id)
    }

    override fun onRemoveDownload(id: Int) {
        fetch?.remove(id)
    }

    override fun onRetryDownload(id: Int) {
        fetch?.retry(id)
    }

    override fun onDestroy() {
        Log.wtf("Bhadoo","**** App Destroyed ****")
        super.onDestroy()
    }

}