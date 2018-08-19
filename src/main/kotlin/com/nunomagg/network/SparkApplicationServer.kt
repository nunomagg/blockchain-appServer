package com.nunomagg.network

import com.google.gson.Gson
import com.nunomagg.handlers.ServiceHandler
import spark.Request
import spark.Spark.get
import spark.Spark.port

class SparkApplicationServer(private val blockChainUnspentAddressHandler: ServiceHandler) : ApplicationServer {
    companion object {
        const val DEFAULT_PORT = 8080
        const val JSON_MIME_TYPE = "application/json"
        private val gson: Gson = Gson()
    }

    override fun stop() {
        spark.Spark.stop()
    }
    override fun start() {
        start(DEFAULT_PORT)
    }

    override fun start(port: Int) {
        port(port)

        get("/address/:address") { request: Request, response ->
            val handlerResponse = blockChainUnspentAddressHandler.handle(request)
            response.status(handlerResponse.statusCode)
            response.type(JSON_MIME_TYPE)
            gson.toJson(handlerResponse.outputResponse)
        }
    }

}