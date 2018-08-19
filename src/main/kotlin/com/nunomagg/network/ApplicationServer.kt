package com.nunomagg.network

interface ApplicationServer {
    fun start(port: Int)
    fun start()
    fun stop()
}