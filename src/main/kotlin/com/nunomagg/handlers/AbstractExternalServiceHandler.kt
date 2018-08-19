package com.nunomagg.handlers

import com.google.gson.Gson
import com.nunomagg.data.HandlerResponse
import com.nunomagg.data.InvalidOutputResponse
import com.nunomagg.data.OutputResponse
import com.nunomagg.errormessages.InvalidBitcoinAddressErrorMessage
import com.nunomagg.errormessages.InvalidNumericValueErrorMessage
import com.nunomagg.errormessages.NoFreeOutputsErrorMessage
import okhttp3.Response
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory

abstract class AbstractExternalServiceHandler : ServiceHandler {
    companion object {
        private const val DEFAULT_ERROR_MESSAGE = "Internal Server Error"
        private val LOGGER = LoggerFactory.getLogger(AbstractExternalServiceHandler::class.java)
        private val gson: Gson = Gson()
    }

    protected fun processErrorResponse(response: Response): HandlerResponse {
        val url = response.request().url()
        val params: List<Pair<String, String?>> = url.queryParameterNames().map { it ->
            it to url.queryParameter(it)
        }
        return processErrorResponse(response.body()?.string() ?: "", params)
    }

    protected fun processErrorResponse(errorMessage: String = "", params: List<Pair<*,*>> = listOf()): HandlerResponse {
        val (message, code) =
                when (errorMessage) {
                    InvalidBitcoinAddressErrorMessage().message,
                    NoFreeOutputsErrorMessage().message,
                    InvalidNumericValueErrorMessage().message -> errorMessage to HttpStatus.INTERNAL_SERVER_ERROR_500
                    else ->
                        DEFAULT_ERROR_MESSAGE to HttpStatus.INTERNAL_SERVER_ERROR_500
                }

        LOGGER.info("HTTP: $code : $errorMessage - Query parameters: $params")
        return HandlerResponse(false, InvalidOutputResponse(message), code)
    }

    protected fun processSuccessResponse(response: Response, jsonParser: (Map<*, *>)-> OutputResponse): HandlerResponse {
        val body: String = response.body()?.string() ?: ""
        val json = gson.fromJson(body, Map::class.java)
        return HandlerResponse(true, jsonParser(json), HttpStatus.OK_200)
    }
}