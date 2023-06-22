package com.d0st.bhadoozindex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.d0st.bhadoozindex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val vm: HmViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.get.setOnClickListener {

            vm.loadAndCancel()
        }

    }
}