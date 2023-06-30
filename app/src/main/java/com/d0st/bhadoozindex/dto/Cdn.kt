package com.d0st.bhadoozindex.dto

import kotlinx.serialization.Serializable

@Serializable
data class Cdn(
    val name:String,
    val size:Long,
    val parts:Int,
    val part_details:Map<String, String>,
    val sha256:String
)