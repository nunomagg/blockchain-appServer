package com.nunomagg.network

import com.nunomagg.data.*
import com.nunomagg.errormessages.InvalidBitcoinAddressErrorMessage
import com.nunomagg.handlers.ServiceHandler
import okhttp3.OkHttpClient
import okhttp3.Request
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class SparkApplicationServerSpec extends Specification {

//with transaction  --> 1F1tAaz5x1HUXrCNLbtMDqcw6o5GNn4xqX
//without --> 1Aff4FgrtA1dZDwajmknWTwU2WtwUvfiXa

    @Shared
    def httpClient = new OkHttpClient()
    int port = 8181
    def valid_tx_hash = "74f88cbde3af42ab859bca198ee820e3e16674c0d382aa77fd5668ce4e84e2eb"
    def valid_value = 5708600
    def valid_idx = 0
    def validJsonResponse = '{' +
            '"outputs":[' +
            '{' +
            '"value":' + valid_value + ',' +
            '"tx_hash":"' + valid_tx_hash + '",' +
            '"output_idx":' + valid_idx +
            '}' +
            ']' +
            '}'

    @Shared
    ApplicationServer applicationServer


    def "should start a new SparkApplication server and request an Address - Request should return success response if address is correct"() {
        given:
        List<AddressOutputData> outputData = [new AddressOutputData(valid_value, valid_tx_hash, valid_idx)]
        AddressOutputResponse addressOutputResponse = new AddressOutputResponse(outputData)
        def blockChainUnspentAddressHandler = Mock(ServiceHandler.class)
        blockChainUnspentAddressHandler.handle(_ as spark.Request) >> new HandlerResponse(true, addressOutputResponse, 200)

        applicationServer = new SparkApplicationServer(blockChainUnspentAddressHandler)
        applicationServer.start(port)

        when:
        TimeUnit.SECONDS.sleep(1)
        Request request = new Request.Builder()
                .url("""http://0.0.0.0:$port/address/anAddress""")
                .build()
        def response = httpClient.newCall(request).execute()
        def bodyAsString = response.body().string()
        applicationServer.stop()
        TimeUnit.SECONDS.sleep(1)

        then:
        response.successful

        print(bodyAsString)

        and:
        bodyAsString == validJsonResponse
    }

    def "should start a new SparkApplication server and return Http code 500 if failed"() {
        given:
        def invalidBitcoinAddressMessage = new InvalidBitcoinAddressErrorMessage().getMessage()
        OutputResponse invalidOutputResponse = new InvalidOutputResponse(invalidBitcoinAddressMessage)
        def blockChainUnspentAddressHandler = Mock(ServiceHandler.class)
        blockChainUnspentAddressHandler.handle(_ as spark.Request) >> new HandlerResponse(false, invalidOutputResponse, 500)

        applicationServer = new SparkApplicationServer(blockChainUnspentAddressHandler)
        applicationServer.start(port)

        when:
        TimeUnit.SECONDS.sleep(1)
        Request request = new Request.Builder()
                .url("""http://0.0.0.0:$port/address/anAddress""")
                .build()
        def response = httpClient.newCall(request).execute()
        def bodyAsString = response.body().string()
        applicationServer.stop()

        then:
        !response.successful

        print(bodyAsString)

        and:
        bodyAsString == """{"message":"$invalidBitcoinAddressMessage"}"""
    }
}