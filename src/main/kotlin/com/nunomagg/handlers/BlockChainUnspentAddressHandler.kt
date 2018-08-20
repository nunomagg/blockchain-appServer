package com.nunomagg.handlers

import com.google.gson.internal.LinkedTreeMap
import com.nunomagg.data.AddressOutputData
import com.nunomagg.data.AddressOutputResponse
import com.nunomagg.data.Request
import com.nunomagg.data.HandlerResponse
import okhttp3.OkHttpClient
import java.math.BigDecimal

class BlockChainUnspentAddressHandler(private val blockChainEndpointRequestsImpl: BlockChainEndpoints) : AbstractExternalServiceHandler() {
    companion object {
        private const val DEFAULT_UNSPENT_TRANSACTIONS_LIMIT = "1000"
        private val httpClient = OkHttpClient()
    }

    override fun handle(request: Request): HandlerResponse {
        val response = blockChainEndpointRequestsImpl.getUnspentTransactions(
                request.pathParams["address"],
                request.queryParams.getOrDefault("limit", DEFAULT_UNSPENT_TRANSACTIONS_LIMIT)!!,
                httpClient
        )

        return try {
            if (response.isSuccessful) {
                processSuccessResponse(response, jsonToAddressServiceOutputData)
            } else {
                processErrorResponse(response)
            }
        } catch (ex: Exception) {
            processErrorResponse()
        }
    }

    private var jsonToAddressServiceOutputData: (Map<*, *>) -> AddressOutputResponse = { json ->
        val output = json["unspent_outputs"] as List<*>
        AddressOutputResponse(
                output.mapNotNull { buildAddressOutputDataFromTransaction(it as LinkedTreeMap<*, *>) })
    }

    private fun buildAddressOutputDataFromTransaction(transaction: LinkedTreeMap<*, *>): AddressOutputData? {
        var value: BigDecimal? = null
        var txHash: String? = null
        var txOutputNumber: Number? = null
        transaction.entries.forEach { property ->
            when (property.key) {
                "tx_hash" -> txHash = property.value as String
                "tx_output_n" ->
                    txOutputNumber = when {
                        property.value is Int -> BigDecimal(property.value as Int)
                        property.value is Double -> BigDecimal(property.value as Double)
                        property.value is String -> BigDecimal(property.value as Long)
                        else -> null
                    }
                "value" -> {
                    value = when {
                        property.value is Int -> BigDecimal(property.value as Int)
                        property.value is Double -> BigDecimal(property.value as Double)
                        property.value is String -> BigDecimal(property.value as String)
                        else -> null
                    }
                }
            }
        }
        if (txHash.isNullOrBlank() && value == null && txOutputNumber == null) {
            return null
        }
        return AddressOutputData(value, txHash ?: "", txOutputNumber)
    }
}