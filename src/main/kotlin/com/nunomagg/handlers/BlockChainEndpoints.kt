package com.nunomagg.handlers

import okhttp3.OkHttpClient
import okhttp3.Response

interface BlockChainEndpoints {
    fun getUnspentTransactions(address: String?, limit: String, client: OkHttpClient): Response
}