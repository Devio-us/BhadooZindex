package com.d0st.bhadoozindex

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.lifecycleScope
import com.d0st.bhadoozindex.databinding.ActivityMainBinding
import com.d0st.bhadoozindex.test.DownloadState
import com.d0st.bhadoozindex.test.Downloader3
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : AppCompatActivity() {

    private val vm: HmViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding


    val uRl1 = "https://23307459.small-file-testing.pages.dev/8f47ffd636bee9c586b9170c2e868886183a4c5f6e7d390919742863318113eb"
    val mb720 = "https://cdn-2.storage.zindex.eu.org/afff84584619ed805f8fa103a3164881a4b28e4510ede04bbd46e3720b33d165"
    private val gb3_13 = "https://cdn-2.storage.zindex.eu.org/890c13f5cf13970a5d902b931bf7962698456f41e784d4364d2a2663379d785a"

    //    "http://storage.zindex.eu.org/"
    val outPath = Environment.getExternalStorageDirectory().toString() + "/Download/"
    private lateinit var rvAdapter: StateAdapter
    private val downloader = Downloader3()

    /*  Pause Algorithm = First Check Which bunch is currently downloading if 15 to 20 bunch is in process then pause download and check which file
        is downloaded in bunch like 15 is downloaded then start download from 16
    */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvAdapter = StateAdapter()
        binding.rvList.adapter = rvAdapter

        val link = binding.link.text
        binding.get.setOnClickListener {

            Permission.verifyStoragePermission(this) {

                vm.loadAndCancel(link.toString()) { onSuccess ->
                    initFetch()

                    lifecycleScope.launch {
                        if(link.toString().isEmpty()){
                            downloader.main(onSuccess, mb720,this@MainActivity)
                        }else {
                            downloader.main(onSuccess, link.toString(),this@MainActivity)
                        }
                    }
                }
            }
        }

        binding.test.setOnClickListener {
            val externalFilesDir = this.getExternalFilesDir("parts") // Get the application's internal storage directory
            println("---external file dir ${externalFilesDir?.absolutePath}")
            println("---external cache dir ${this.externalCacheDir}")
            println("---cache dir ${this.cacheDir}")
            val customFolder = File(externalFilesDir, "com.d0st.bhadoozindex")
            if (!customFolder.exists()) {
                println("------Create folder in data")
                customFolder.mkdirs() // Create the folder if it doesn't exist
            }
            val file = File(customFolder, "Parts.txt") // Create the file within the custom folder

            file.writeText("Hello, World!") //
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initFetch() {
        Log.d("Downloader3","InitFetch Called")
        downloader.respose.observe(this) { response ->
            when(response){
                is DownloadState.CurrentState -> {
                    val resp = response.state.reversed()
                    rvAdapter.setCommonData(resp)
                    rvAdapter.notifyDataSetChanged()
                    Log.d("Downloader3","CurrentState = ${response.state}")
                }
                is DownloadState.Error -> {
                    Log.wtf("Downloader3","DownloadState Error ${response.message}")
                }
            }

//            when (response) {
//                is Resource.Success -> {
//                    UiHelper.loadingDialogBuilder.dismissSafe()
//                    response.data?.data?.let {
//                        rvAdapter.setCommonData(it)
//                    }
//                }
//                is Resource.Error -> {
//                    UiHelper.loadingDialogBuilder.dismissSafe()
//                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
//                }
//                is Resource.Loading -> {
//                    UiHelper.loadingDialog(requireContext())
//                }
//            }
        }

    }

}