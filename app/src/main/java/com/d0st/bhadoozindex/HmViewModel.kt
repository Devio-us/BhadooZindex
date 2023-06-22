package com.d0st.bhadoozindex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d0st.bhadoozindex.dto.File
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltViewModel
class HmViewModel @Inject constructor():ViewModel() {

val uRl = "https://23307459.small-file-testing.pages.dev/8f47ffd636bee9c586b9170c2e868886183a4c5f6e7d390919742863318113eb.json"

    fun loadAndCancel(){
        viewModelScope.launch {
            val getResult = Ok(OkHttpClient()).get(uRl)

            if (getResult.isSuccess) {
                val response = getResult.getOrElse { "" }

                val obj:File = Json.decodeFromString(string = response)
                println("GET request successful. Response: $response")

                println("name = ${obj.name} , size = ${obj.size} , partDetail = ${obj.part_details}")
            } else {
                val exception = getResult.exceptionOrNull()
                println("GET request failed: ${exception?.message}")
            }
        }
    }

}