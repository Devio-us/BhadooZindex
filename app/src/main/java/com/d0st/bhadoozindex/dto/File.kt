package com.d0st.bhadoozindex.dto

import kotlinx.serialization.Serializable

@Serializable
data class File(
    val name:String,
    val size:Int,
    val parts:Int,
    val part_details:Map<String, String>,
    val sha256:String
)
@Serializable
data class Parts(
    val partNo:String,
)