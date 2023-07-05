package com.d0st.bhadoozindex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.d0st.bhadoozindex.databinding.KItemBinding
import com.kdownloader.KDownloader
import com.kdownloader.Status

class kAdapter() : RecyclerView.Adapter<kAdapter.ViewHolder>() {

    private var mList: ArrayList<Map<Int,com.kdownloader.internal.DownloadRequest>> = arrayListOf()
    lateinit var kDownloader: KDownloader

    inner class ViewHolder(val binding: KItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = KItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var download1 = 1
        val key = mList[position][position+1]!!
        with(holder) {
            with(mList[position]) {
//                binding.startCancelButton1.setOnClickListener {
                    if (binding.startCancelButton1.text.equals("Start")) {
                        download1 = kDownloader.enqueue(key,
                            onStart = {
                                binding.status1.text = "Started"
                                binding.startCancelButton1.text = "Cancel"
                                binding.resumePauseButton1.isEnabled = true
                                binding.resumePauseButton1.visibility = View.VISIBLE
                                binding.resumePauseButton1.text = "Pause"
                            },
                            onProgress = {
                                binding.status1.text = "In Progress"
                                binding.progressBar1.progress = it
                                binding.progressText1.text = "$it%"
                            },
                            onCompleted = {
                                println("Complete = $position")
                                binding.status1.text = "Completed"
                                binding.progressText1.text = "100%"
                                binding.startCancelButton1.visibility = View.GONE
                                binding.resumePauseButton1.visibility = View.GONE
                            },
                            onError = {
                                binding.status1.text = "Error : $it"
                                binding.resumePauseButton1.visibility = View.GONE
                                binding.progressBar1.progress = 0
                                binding.progressText1.text = "0%"
                            },
                            onPause = {
                                binding.status1.text = "Paused"
                            }
                        )
                    } else {
                        kDownloader.cancel(download1)
                        binding.startCancelButton1.text = "Start"
                    }
//                }

                binding.resumePauseButton1.setOnClickListener{
                    if (kDownloader.status(download1) == Status.PAUSED) {
                        binding.resumePauseButton1.text = "Pause"
                        kDownloader.resume(download1)
                    } else {
                        binding.resumePauseButton1.text = "Resume"
                        kDownloader.pause(download1)
                    }
                }
            }
            binding.fileName1.text = mList[position].toString()
        }

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setCommonData(newData: ArrayList<Map<Int,com.kdownloader.internal.DownloadRequest>>) {
//        Log.wtf("StateAdapter", newData.toString())

        val movieDiffUtil = CommonDiff(mList, newData)

        val diffUtilResult = DiffUtil.calculateDiff(movieDiffUtil)

//        mList.clear()
        mList = newData

        diffUtilResult.dispatchUpdatesTo(this)
    }

}

