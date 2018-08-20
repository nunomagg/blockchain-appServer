package com.nunomagg.handlers

import com.nunomagg.data.Request
import com.nunomagg.data.HandlerResponse

interface ServiceHandler {
    fun handle(request: Request): HandlerResponse
}