package com.nunomagg.handlers

import com.google.gson.internal.LinkedTreeMap
import com.nunomagg.data.AddressOutputResponse
import com.nunomagg.data.HandlerResponse
import com.nunomagg.data.InvalidOutputResponse
import com.nunomagg.errormessages.InvalidNumericValueErrorMessage
import com.nunomagg.errormessages.NoFreeOutputsErrorMessage
import okhttp3.*
import org.eclipse.jetty.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification

class BlockChainUnspentAddressHandlerSpec extends Specification {
    @Shared
    BlockChainEndpoints mockBlockChainEndpointRequests = Mock(BlockChainEndpoints)
    @Shared
    String validJsonBody
    @Shared
    String validNoOutputsJson
    @Shared
    String valid_tx_hash
    @Shared
    BigDecimal valid_value
    @Shared
    int valid_idx

    def setupSpec() {
        valid_tx_hash = "74f88cbde3af42ab859bca198ee820e3e16674c0d382aa77fd5668ce4e84e2eb"
        valid_value = 5708600
        valid_idx = 0

        validJsonBody = '\n' +
                '{\n' +
                '    \n' +
                '    "unspent_outputs":[\n' +
                '    \n' +
                '        {\n' +
                '            "tx_hash":"'+valid_tx_hash+'",\n' +
                '            "tx_hash_big_endian":"ebe2844ece6856fd77aa82d3c07466e1e320e88e19ca9b85ab42afe3bd8cf874",\n' +
                '            "tx_index":59591229,\n' +
                '            "tx_output_n": '+valid_idx+',\n' +
                '            "script":"76a91499bc78ba577a95a11f1a344d4d2ae55f2f857b9888ac",\n' +
                '            "value": '+valid_value+',\n' +
                '            "value_hex": "571b38",\n' +
                '            "confirmations":228101\n' +
                '        }\n' +
                '    ]\n' +
                '}'

        validNoOutputsJson = '\n' +
                '{\n' +
                '    \n' +
                '    "unspent_outputs":[]\n' +
                '}'

    }

    def setup(){
        mockBlockChainEndpointRequests = Mock(BlockChainEndpoints)
    }

    def 'should process the body of an known error message successfully'() {
        given:
        mockBlockChainEndpointRequests.getUnspentTransactions(_ as String, _ as String, _ as OkHttpClient) >>
                internalServerErrorBodyResponse("Invalid Bitcoin Address")

        def addressHandler = new BlockChainUnspentAddressHandler(mockBlockChainEndpointRequests)

        def request = Mock(spark.Request.class)
        request.params(":address") >> "address"
        request.queryParamOrDefault("limit", "1000") >> "1000"

        when:
        HandlerResponse handlerResponse = addressHandler.handle(request)

        then:
        handlerResponse.statusCode == HttpStatus.INTERNAL_SERVER_ERROR_500
        handlerResponse.outputResponse instanceof InvalidOutputResponse
        !handlerResponse.successful
    }

    def 'should transform a a transaction into an AddressOutputData Object successfully '() {
        given:
        def treeMap = new LinkedTreeMap()
        def tx_hash = "74f88cbde3af42ab859bca198ee820e3e16674c0d382aa77fd5668ce4e84e2eb"
        def output_hash_id = 1
        def value = 200
        treeMap.put("tx_hash", tx_hash)
        treeMap.put("tx_output_n", output_hash_id)
        treeMap.put("value", value)

        def addressHandler = new BlockChainUnspentAddressHandler(mockBlockChainEndpointRequests)

        when:
        def transaction = addressHandler.buildAddressOutputDataFromTransaction(treeMap)

        then:
        transaction.output_idx == output_hash_id
        transaction.tx_hash == tx_hash
        transaction.value == value
    }

    def 'should transform a transaction into an AddressOutputData even if some properties are missing '() {
        given:
        def treeMap = new LinkedTreeMap()
        def output_hash_id = 1
        def value = 200
        treeMap.put("tx_output_n", output_hash_id)
        treeMap.put("value", value)
        def addressHandler = new BlockChainUnspentAddressHandler(mockBlockChainEndpointRequests)

        when:
        def transaction = addressHandler.buildAddressOutputDataFromTransaction(treeMap)

        then:
        transaction.output_idx == output_hash_id
        transaction.value == value
    }

    def 'buildAddressOutputDataFromTransaction should return null if non of the properties exist'() {
        when:
        def addressHandler = new BlockChainUnspentAddressHandler(mockBlockChainEndpointRequests)
        def transaction = addressHandler.buildAddressOutputDataFromTransaction(new LinkedTreeMap())

        then:
        transaction == null
    }

    def 'should return an HandlerResponse with the correct error message'(){
        when:
        def errorMessage = new InvalidNumericValueErrorMessage().message
        def addressHandler = new BlockChainUnspentAddressHandler(mockBlockChainEndpointRequests)
        def handlerResponse = addressHandler.processErrorResponse(errorMessage, _ as List)

        then:
        handlerResponse.statusCode == 500
        !handlerResponse.successful
        handlerResponse.outputResponse instanceof InvalidOutputResponse
        InvalidOutputResponse outputResponse = (InvalidOutputResponse) handlerResponse.outputResponse
        outputResponse.message == errorMessage

        when:
        errorMessage = new NoFreeOutputsErrorMessage().message
        handlerResponse = addressHandler.processErrorResponse(errorMessage, _ as List)
        outputResponse = (InvalidOutputResponse) handlerResponse.outputResponse

        then:
        handlerResponse.statusCode == 500
        !handlerResponse.successful
        handlerResponse.outputResponse instanceof InvalidOutputResponse

        outputResponse.message == errorMessage
    }

    def 'should return an HandlerResponse with a generic message if the error is unknown'(){
        when:
        def errorMessage = "A generic error message"
        def addressHandler = new BlockChainUnspentAddressHandler(mockBlockChainEndpointRequests)
        def handlerResponse = addressHandler.processErrorResponse(errorMessage, _ as List)

        then:
        handlerResponse.statusCode == 500
        !handlerResponse.successful
        handlerResponse.outputResponse instanceof InvalidOutputResponse
        InvalidOutputResponse outputResponse = (InvalidOutputResponse) handlerResponse.outputResponse
        outputResponse.message == "Internal Server Error"

    }

    def 'should test that given a valid JsonResponse from blockChain the output is process correctly'(){
        given:
        mockBlockChainEndpointRequests.getUnspentTransactions(_ as String, _ as String, _ as OkHttpClient) >>
                validUnspentTransactionResponse(validJsonBody)

        def request = Mock(spark.Request.class)
        request.params(":address") >> "address"
        request.queryParamOrDefault("limit", "1000") >> "1000"

        def addressHandler = new BlockChainUnspentAddressHandler(mockBlockChainEndpointRequests)

        when:
        HandlerResponse handlerResponse = addressHandler.handle(request)

        then:
        handlerResponse.outputResponse instanceof AddressOutputResponse

        when:
        def addressOutputResponse = (AddressOutputResponse) handlerResponse.outputResponse
        def outputData = addressOutputResponse.outputs

        then:
        handlerResponse.statusCode == HttpStatus.OK_200
        handlerResponse.successful

        and:
        outputData.size() == 1
        outputData.get(0).output_idx == valid_idx
        outputData.get(0).tx_hash == valid_tx_hash
        outputData.get(0).value == valid_value
    }


    def 'should test that given a valid empty JsonResponse from blockChain the output is process correctly'(){
        given:
        mockBlockChainEndpointRequests.getUnspentTransactions(_ as String, _ as String, _ as OkHttpClient) >>
                validUnspentTransactionResponse(validNoOutputsJson)

        def request = Mock(spark.Request.class)
        request.params(":address") >> "address"
        request.queryParamOrDefault("limit", "1000") >> "1000"

        def addressHandler = new BlockChainUnspentAddressHandler(mockBlockChainEndpointRequests)

        when:
        HandlerResponse handlerResponse = addressHandler.handle(request)

        then:
        handlerResponse.outputResponse instanceof AddressOutputResponse

        when:
        def addressOutputResponse = (AddressOutputResponse) handlerResponse.outputResponse
        def outputData = addressOutputResponse.outputs

        then:
        handlerResponse.statusCode == HttpStatus.OK_200
        handlerResponse.successful

        and:
        outputData.size() == 0
    }

    private static Response validUnspentTransactionResponse(String jsonBody) {
        new Response.Builder()
                .code(HttpStatus.OK_200)
                .body(ResponseBody.create(MediaType.parse("json"), jsonBody))
                .request(new Request.Builder()
                .url("https://blockchain.info/unspent?active=1F1tAaz5x1HUXrCNLbtMDqcw6o5GNn4xqX")
                .build()
        )
                .protocol(Protocol.HTTP_2)
                .message("some message")
                .build()
    }

    private static Response internalServerErrorBodyResponse(String errorMessage) {
        new Response.Builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(ResponseBody.create(MediaType.parse("text"), errorMessage))
                .request(new Request.Builder()
                .url("https://blockchain.info/unspent?active=address")
                .build()
        )
                .protocol(Protocol.HTTP_2)
                .message("some message")
                .build()
    }
}
