package com.d0st.bhadoozindex

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.d0st.bhadoozindex.databinding.RvChallanItemsBinding

class StateAdapter : RecyclerView.Adapter<StateAdapter.ViewHolder>() {

    private var mList: List<String> = ArrayList()

    inner class ViewHolder(val binding: RvChallanItemsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvChallanItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(mList[position]) {
                binding.currentState.text = this
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setCommonData(newData: List<String>) {
//        Log.wtf("StateAdapter", newData.toString())

        val movieDiffUtil = CommonDiff(mList, newData)

        val diffUtilResult = DiffUtil.calculateDiff(movieDiffUtil)

//        mList.clear()
        mList = newData

        diffUtilResult.dispatchUpdatesTo(this)
    }

}

