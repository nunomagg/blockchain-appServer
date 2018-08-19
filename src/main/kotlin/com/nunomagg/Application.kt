package com.nunomagg

import com.nunomagg.handlers.BlockChainEndpointImpl
import com.nunomagg.handlers.BlockChainUnspentAddressHandler
import com.nunomagg.network.SparkApplicationServer


fun main(args: Array<String>) {
    val blockChainEndpointRequests = BlockChainEndpointImpl()
    val addressHandler = BlockChainUnspentAddressHandler(blockChainEndpointRequests)
    val applicationServer = SparkApplicationServer(addressHandler)

    applicationServer.start()
}
