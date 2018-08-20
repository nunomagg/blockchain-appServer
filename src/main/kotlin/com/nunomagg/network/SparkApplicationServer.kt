package com.nunomagg.network

import com.google.gson.Gson
import com.nunomagg.data.Request
import com.nunomagg.handlers.ServiceHandler
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

        get("/address/:address") { request: spark.Request, response ->

            val handlerResponse = blockChainUnspentAddressHandler.handle(Request(
                    mapOf(Pair<String, String?>("address", request.params(":address"))),
                    mapOf(Pair<String, String?>("limit", request.queryParams("limit")))
            ))
            response.status(handlerResponse.statusCode)
            response.type(JSON_MIME_TYPE)
            gson.toJson(handlerResponse.outputResponse)
        }
    }

}