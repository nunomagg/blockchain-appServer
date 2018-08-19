package com.nunomagg.handlers

import okhttp3.OkHttpClient
import okhttp3.Response

class BlockChainEndpointImpl : BlockChainEndpoints{
    override fun getUnspentTransactions(address: String?, limit: String, client: OkHttpClient): Response {
        val request = okhttp3.Request.Builder()
                .url("https://blockchain.info/unspent?active=$address&limit=$limit")
                .build()
        return client.newCall(request).execute()
    }
}