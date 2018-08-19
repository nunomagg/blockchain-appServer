package com.nunomagg.handlers

import com.nunomagg.data.HandlerResponse

interface ServiceHandler {
    fun handle(request: spark.Request): HandlerResponse
}