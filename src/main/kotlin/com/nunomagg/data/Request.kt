package com.nunomagg.data

data class Request (
    val pathParams : Map<String, String?>,
    val queryParams : Map<String, String?>
)
