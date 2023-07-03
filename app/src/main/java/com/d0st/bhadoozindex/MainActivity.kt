package com.d0st.bhadoozindex

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.lifecycleScope
import com.d0st.bhadoozindex.databinding.ActivityMainBinding
import com.d0st.bhadoozindex.dto.Cdn
import com.d0st.bhadoozindex.test.Download9State
import com.d0st.bhadoozindex.test.DownloadState
import com.d0st.bhadoozindex.test.Downloader9
import com.kdownloader.KDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicInteger

const val uRl1 = "https://23307459.small-file-testing.pages.dev/8f47ffd636bee9c586b9170c2e868886183a4c5f6e7d390919742863318113eb"
const val mb720 = "https://cdn-2.storage.zindex.eu.org/afff84584619ed805f8fa103a3164881a4b28e4510ede04bbd46e3720b33d165"
const val gb3_13 = "https://cdn-2.storage.zindex.eu.org/890c13f5cf13970a5d902b931bf7962698456f41e784d4364d2a2663379d785a"
sealed class KState {
    object IdleState:KState()
    data class RequestCompleted(val state: ArrayList<Map<Int,com.kdownloader.internal.DownloadRequest>>) : KState()
    data class Error(val message: String) : KState()
}

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val vm: HmViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    //    "http://storage.zindex.eu.org/"
    val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"
    private lateinit var rvAdapter: StateAdapter
    private lateinit var kAdapter: kAdapter
//    private val downloader = Downloader3()
    private val downloader = Downloader9()

    private lateinit var kDownloader: KDownloader
    lateinit var dirPath: String

    private var _kResponse = MutableLiveData<KState>()
    val respose = _kResponse


    /*  Pause Algorithm = First Check Which bunch is currently downloading if 15 to 20 bunch is in process then pause download and check which file
        is downloaded in bunch like 15 is downloaded then start download from 16
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kDownloader = (applicationContext as App).kDownloader
        dirPath = Environment.getExternalStorageDirectory().path + "/Download"


        rvAdapter = StateAdapter()
        kAdapter = kAdapter()
        kAdapter.kDownloader = kDownloader
//        binding.rvList.adapter = rvAdapter
        binding.kList.adapter = kAdapter

        val link = binding.link.text
        binding.get.setOnClickListener {

            Permission.verifyStoragePermission(this) {

                vm.loadAndCancel(link.toString()) { onSuccess ->
                    initFetch()

                    lifecycleScope.launch {
//                        downloader.main(onSuccess, mb720,this@MainActivity,kDownloader)
                        kDownloader(onSuccess)
                    }

//                   lifecycleScope.launch {
//                        if(link.toString().isEmpty()){
//                            downloader.main(onSuccess, mb720,this@MainActivity)
//                        } else {
//                            downloader.main(onSuccess, link.toString(),this@MainActivity)
//                        }
//                    }
                }
            }
        }

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

   suspend fun kDownloader(Json:Cdn){

        val batchSize = 3
        val partCount = AtomicInteger(0)
        _kResponse.postValue(KState.IdleState)

        while (partCount.get() < 2) {
            val startIndex = partCount.getAndAdd(batchSize)
            val endIndex = (startIndex + batchSize - 1).coerceAtMost(2 - 1)
            Log.d("kDownloader", "startIndex = ${startIndex + 1}")
            Log.d("kDownloader", "endIndex = ${endIndex+1}")

            for (partNumber in startIndex..endIndex) {

                val request = kDownloader.newRequestBuilder(
                    "$mb720.part${partNumber + 1}", outPath, "$partNumber.mp4",
                ).tag(TAG + "1").build()

                requests.add(mapOf(partNumber+1 to request))

            }
        }

        coroutineScope {
            launch {
                joinFiles(outPath, "${outPath}${Json.name}")

            }
        }


        Log.d("kDownloader","Create Download")
        _kResponse.postValue(KState.RequestCompleted(requests))

    }

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
    private fun initFetch() {
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

}