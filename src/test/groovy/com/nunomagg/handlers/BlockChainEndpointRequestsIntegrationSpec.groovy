package com.nunomagg.handlers

import okhttp3.OkHttpClient
import spock.lang.Shared
import spock.lang.Specification

class BlockChainEndpointRequestsIntegrationSpec extends Specification {
    @Shared
    def httpClient

    def setupSpec(){
        httpClient = new OkHttpClient()
    }

    def validBitcoinAddress = "1F1tAaz5x1HUXrCNLbtMDqcw6o5GNn4xqX"

    def "should return not success when called with a valid bitcoin address"(){
        given:
        BlockChainEndpoints blockChainEndpointRequests = new BlockChainEndpointImpl()

        when:
        def unspentTransactions = blockChainEndpointRequests.getUnspentTransactions("AnInvalidAddress", "1", httpClient)

        then:
        unspentTransactions.body().string() == "Invalid Bitcoin Address"

        and:
        !unspentTransactions.successful
    }

    def "should return success when called with a valid bitcoin address"(){
        given:
        BlockChainEndpoints blockChainEndpointRequests = new BlockChainEndpointImpl()

        when:
        def unspentTransactions = blockChainEndpointRequests.getUnspentTransactions(validBitcoinAddress, "1", httpClient)

        then:
        unspentTransactions.successful
    }

}
