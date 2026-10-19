package com.alviy.moner.core.common.base

import android.util.Log
import com.alviy.moner.core.common.result.ErrorCodes
import com.alviy.moner.core.common.result.ErrorMessages
import com.alviy.moner.core.common.result.ErrorResponse
import com.alviy.moner.core.common.result.Result
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

/** Base for remote data sources: every Retrofit outcome becomes a [Result], never an exception. */
abstract class BaseDataSource(protected val gson: Gson) {

    /** Runs a Retrofit call that returns Response<T> and maps every outcome to [Result]. */
    protected suspend fun <T> getApiResult(call: suspend () -> Response<T>): Result<T> = try {
        val response = call()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            Result.success(body)
        } else {
            errorHandler(response.code(), response.errorBody()?.use { it.string() })
        }
    } catch (e: CancellationException) {
        throw e                                                  // never swallow coroutine cancellation
    } catch (e: SocketTimeoutException) {
        errorHandler(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, null)
    } catch (e: IOException) {
        errorHandler(HttpURLConnection.HTTP_UNAVAILABLE, null)   // no connectivity / DNS / connection reset
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error", e)
        errorHandler(UNKNOWN_HTTP_CODE, null)
    }

    /** Override when an API returns errors in a different JSON shape (see GmailRemoteDataSource). */
    protected open fun parseError(raw: String): ErrorResponse? =
        gson.fromJson(raw, ErrorResponse::class.java)

    private fun <T> errorHandler(httpCode: Int, rawErrorBody: String?): Result<T> {
        val code = if (httpCode in GENERIC_ERROR_CODES) {
            httpCode.toString()
        } else {
            rawErrorBody.toErrorResponse().code ?: ErrorCodes.GENERAL
        }
        return Result.error(
            code = code,
            title = ErrorMessages.GENERAL_TITLE,
            message = ErrorMessages.GENERAL_BODY,
        )
    }

    private fun String?.toErrorResponse(): ErrorResponse =
        if (isNullOrBlank()) DEFAULT_ERROR
        else runCatching { parseError(this) }.getOrNull() ?: DEFAULT_ERROR

    private companion object {
        const val TAG = "BaseDataSource"
        const val UNKNOWN_HTTP_CODE = 0

        /** Reported by their HTTP code alone; any body is not trusted. */
        val GENERIC_ERROR_CODES = setOf(
            HttpURLConnection.HTTP_UNAUTHORIZED,
            HttpURLConnection.HTTP_UNAVAILABLE,
            HttpURLConnection.HTTP_GATEWAY_TIMEOUT,
        )

        val DEFAULT_ERROR = ErrorResponse(ErrorCodes.GENERAL, ErrorMessages.GENERAL_TITLE, ErrorMessages.GENERAL_BODY)
    }
}
