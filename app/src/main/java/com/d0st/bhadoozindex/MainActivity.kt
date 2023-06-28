package com.d0st.bhadoozindex

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.d0st.bhadoozindex.databinding.ActivityMainBinding
import com.d0st.bhadoozindex.test.DownloadState
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
        binding.get.setOnClickListener {

            Permission.verifyStoragePermission(this) {

                vm.loadAndCancel { onSuccess ->
                    initFetch()

                    lifecycleScope.launch {
                        downloader.main(onSuccess.parts,gb3_13)
                    }

//                    println("part Size = ${onSuccess.parts}")


                }
            }

        }
        binding.pause.setOnClickListener {
//            downloader.pauseInterceptor.pause()
//            downloader.call.cancel()
        }
        binding.resume.setOnClickListener {
//            downloader.pauseInterceptor.resume()
        }

    }

    private fun initFetch() {
        Log.d("Downloader3","InitFetch Called")
        downloader.respose.observe(this) { response ->
            when(response){
                is DownloadState.CurrentState -> {
                    rvAdapter.setCommonData(response.state)
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