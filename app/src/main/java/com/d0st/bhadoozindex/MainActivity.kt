package com.d0st.bhadoozindex

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d0st.bhadoozindex.databinding.ActivityMainBinding
import com.d0st.bhadoozindex.dto.Cdn
import com.d0st.bhadoozindex.test.Download9State
import com.d0st.bhadoozindex.test.DownloadState
import com.d0st.bhadoozindex.test.Downloader3
import com.d0st.bhadoozindex.test.Downloader9
import com.d0st.bhadoozindex.utils.ActionListener
import com.d0st.bhadoozindex.utils.DwnAdapter
import com.d0st.bhadoozindex.utils.DwnHelper.getConfiguration
import com.d0st.bhadoozindex.utils.DwnHelper.startDownload
import com.kdownloader.KDownloader
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Collections
import java.util.Comparator
import java.util.concurrent.atomic.AtomicInteger

const val uRl1 = "https://23307459.small-file-testing.pages.dev/8f47ffd636bee9c586b9170c2e868886183a4c5f6e7d390919742863318113eb"
const val mb720 = "https://cdn-2.storage.zindex.eu.org/afff84584619ed805f8fa103a3164881a4b28e4510ede04bbd46e3720b33d165"
const val gb3_13 = "https://cdn-2.storage.zindex.eu.org/890c13f5cf13970a5d902b931bf7962698456f41e784d4364d2a2663379d785a"
sealed class KState {
    object IdleState:KState()
    data class RequestCompleted(val state: ArrayList<Map<Int,com.kdownloader.internal.DownloadRequest>>) : KState()
    data class Error(val message: String) : KState()
}

class MainActivity : AppCompatActivity(), ActionListener {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val vm: HmViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    //    "http://storage.zindex.eu.org/"
    val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"
    private lateinit var rvAdapter: StateAdapter
    private lateinit var kAdapter: kAdapter
    private val downloader = Downloader3()
//    private val downloader = Downloader9()

    private lateinit var kDownloader: KDownloader
    lateinit var dirPath: String

    private var _kResponse = MutableLiveData<KState>()
    val respose = _kResponse

    private var fetch: Fetch? = null
    private var fileAdapter: DwnAdapter? = null
    var recyclerView: RecyclerView? = null
    private val fetchNamespace = "MvilaaDownload"
    private val unknownRemainingTime: Long = -1
    private val unknownDownloadedBytesPerSecond: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kDownloader = (applicationContext as App).kDownloader
        dirPath = Environment.getExternalStorageDirectory().path + "/Download"


        rvAdapter = StateAdapter()
        kAdapter = kAdapter()
        kAdapter.kDownloader = kDownloader
        binding.rvList.adapter = rvAdapter
//        binding.kList.adapter = kAdapter

        val link = binding.link.text
        binding.okHttpGet.setOnClickListener {
            binding.fetchList.visibility = View.GONE
            binding.fetch.visibility = View.GONE
            binding.join.visibility = View.GONE
            Permission.verifyStoragePermission(this) {
                vm.loadAndCancel(link.toString()) { onSuccess ->
                    initFetch()
                   lifecycleScope.launch {
                        if(link.toString().isEmpty()){
                            downloader.main(onSuccess, mb720,this@MainActivity)
                        } else {
                            downloader.main(onSuccess, link.toString(),this@MainActivity)
                        }
                    }
                }
            }
        }

        binding.fetch.setOnClickListener {
            binding.okHttpGet.visibility = View.GONE
            binding.rvList.visibility = View.GONE
            vm.loadAndCancel(link.toString()) { onSuccess ->
                lifecycleScope.launch {
                    fetchDownloader(onSuccess)
                }
            }
        }

        binding.join.setOnClickListener {
            lifecycleScope.launch {
                joinFiles(outPath, "${outPath}Test.mkv")

            }
        }

        setUpViews()
//        val fetchConfigurationnnn: FetchConfiguration = FetchConfiguration.Builder(this)
//            .setDownloadConcurrentLimit(3)
////            .enableHashCheck(true)
//            .setGlobalNetworkType(NetworkType.ALL)
//            .enableFileExistChecks(true)
//            .enableRetryOnNetworkGain(true)
//            .setHttpDownloader(OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
//            .setNamespace(fetchNamespace)
//            .build()

        fetch = Fetch.Impl.getInstance(getConfiguration(this))

//        binding.test.setOnClickListener {
//            val externalFilesDir = this.getExternalFilesDir("parts") // Get the application's internal storage directory
//            println("---external file dir ${externalFilesDir?.absolutePath}")
//            println("---external cache dir ${this.externalCacheDir}")
//            println("---cache dir ${this.cacheDir}")
//            val customFolder = File(externalFilesDir, "com.d0st.bhadoozindex")
//            if (!customFolder.exists()) {
//                println("------Create folder in data")
//                customFolder.mkdirs() // Create the folder if it doesn't exist
//            }
//            val file = File(customFolder, "Parts.txt") // Create the file within the custom folder
//
//            file.writeText("Hello, World!") //
//        }

//        binding.cancel.setOnClickListener {
//
//        }

    }
    private val requests = ArrayList<Map<Int,com.kdownloader.internal.DownloadRequest>>()

   fun fetchDownloader(Json:Cdn){

        val batchSize = 3
        val partCount = AtomicInteger(1)

        while (partCount.get() < Json.parts) {
            val startIndex = partCount.getAndAdd(batchSize)
            val endIndex = (startIndex + batchSize - 1).coerceAtMost(5)
            Log.d("kDownloader", "startIndex = ${startIndex}")
            Log.d("kDownloader", "endIndex = ${endIndex}")

            for (partNumber in startIndex..endIndex) {

                startDownload(this@MainActivity,binding.root,"${partNumber}.bin","$mb720.part${partNumber}","${startIndex}${endIndex}".toInt())

//                requests.add(mapOf(partNumber+1 to request))

            }
        }
       fetchDownloads()

       Log.d("kDownloader","Create Download")

    }

    private fun setUpViews() {
        recyclerView = binding.fetchList
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        fileAdapter = DwnAdapter(this)
        recyclerView!!.adapter = fileAdapter
    }

    private fun fetchDownloads(){
        fetch?.getDownloads { downloads ->
            val list: java.util.ArrayList<Download> = java.util.ArrayList(downloads)
            Collections.sort(list, Comparator.comparingLong(Download::created))
            list.reverse()
            for (download in list) {
                fileAdapter!!.addDownload(download)
            }
            if (downloads.isEmpty()) {
//                binding.noDownload.visibility = View.VISIBLE
                recyclerView!!.visibility = View.GONE
            } else {
//                binding.noDownload.visibility = View.GONE
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
            println("complete grp= ${download.group} complete url = ${download.url}")
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

//   suspend fun kDownloader(Json:Cdn){
//
//        val batchSize = 3
//        val partCount = AtomicInteger(0)
//        _kResponse.postValue(KState.IdleState)
//
//        while (partCount.get() < 2) {
//            val startIndex = partCount.getAndAdd(batchSize)
//            val endIndex = (startIndex + batchSize - 1).coerceAtMost(2 - 1)
//            Log.d("kDownloader", "startIndex = ${startIndex + 1}")
//            Log.d("kDownloader", "endIndex = ${endIndex+1}")
//
//            for (partNumber in startIndex..endIndex) {
//
//                val request = kDownloader.newRequestBuilder(
//                    "$mb720.part${partNumber + 1}", outPath, "$partNumber.mp4",
//                ).tag(TAG + "1").build()
//
//                requests.add(mapOf(partNumber+1 to request))
//
//            }
//        }
//
//        coroutineScope {
//            launch {
//                joinFiles(outPath, "${outPath}${Json.name}")
//
//            }
//        }
//
//
//        Log.d("kDownloader","Create Download")
//        _kResponse.postValue(KState.RequestCompleted(requests))
//
//    }

    private suspend fun joinFiles(inputDir: String, outputFile: String) =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(inputDir)
                val files = dir.listFiles { _, name -> name.endsWith(".bin") }
                val out = FileOutputStream(outputFile)
                files?.sortedBy { it.nameWithoutExtension.toInt() }?.forEach { file ->
                    Log.d("Downloader3", "Sorted files = $file")
                    val input =
                        FileInputStream(file).buffered()  // Default Buffer Size - 8192 (8 * 1024)
                    input.copyTo(out)
                    input.close()
                    file.delete()
                }
                out.close()
//                Log.d("Downloader3", "Complete Merging")
            } catch (e: Exception) {
//                Log.wtf("Downloader3", "File Merging Error = ${e.message}")
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun kInitFetch() {
        Log.d("Downloader3","InitFetch Called")
        respose.observe(this) { response ->
            when(response){
                is KState.IdleState -> {

                    Log.d("KState","IdleState")
                }
                is KState.RequestCompleted -> {
                     kAdapter.setCommonData(response.state)
                    kAdapter.notifyDataSetChanged()
                }
                is KState.Error -> {
                    Log.wtf("KState","DownloadState Error ${response.message}")
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initFetch() {
        Log.d("Downloader3", "InitFetch Called")
        downloader.respose.observe(this) { response ->
            when (response) {
                is DownloadState.CurrentState -> {
                    val resp = ArrayList(response.state).reversed()
                    rvAdapter.setCommonData(resp)
                    rvAdapter.notifyDataSetChanged()
                    Log.d("Downloader3", "CurrentState = ${response.state}")
                }

                is DownloadState.Error -> {
                    Log.wtf("Downloader3", "DownloadState Error ${response.message}")
                }
            }
        }
    }

}