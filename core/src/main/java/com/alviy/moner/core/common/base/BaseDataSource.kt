package com.alviy.moner.core.common.base

import android.util.Log
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.ErrorCodes.GENERAL
import com.alviy.moner.core.common.entity.ErrorResponse
import okhttp3.ResponseBody
import retrofit2.Response
import com.alviy.moner.core.common.entity.Result
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException


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
        throw e                                        // never swallow coroutine cancellation
    } catch (e: SocketTimeoutException) {
        errorHandler(504, null)                        // was 401, which looked like "token expired"
    } catch (e: IOException) {
        errorHandler(503, null)                        // no connectivity / DNS / connection reset
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error", e)
        errorHandler(0, null)
    }

    /** Override when an API returns errors in a different JSON shape (see GmailRemoteDataSource). */
    protected open fun parseError(raw: String): ErrorResponse? =
        gson.fromJson(raw, ErrorResponse::class.java)

    private fun <T> errorHandler(httpCode: Int, rawErrorBody: String?): Result<T> {
        val code = if (httpCode.requiresGenericError()) {
            httpCode.toString()
        } else {
            rawErrorBody.toErrorResponse().code ?: GENERAL
        }
        return Result.error(
            code = code,
            title = GENERAL_ERROR_TITLE,
            message = GENERAL_ERROR_BODY,
            data = null,
        )
    }

    private fun Int.requiresGenericError() = this == 503 || this == 401 || this == 504

    private fun String?.toErrorResponse(): ErrorResponse =
        if (isNullOrBlank()) DEFAULT_ERROR
        else runCatching { parseError(this) }.getOrNull() ?: DEFAULT_ERROR

    companion object {
        private const val TAG = "BaseDataSource"
        const val GENERAL_ERROR_TITLE = "General Error"
        const val GENERAL_ERROR_BODY = "Please try again, service unreachable"
        private val DEFAULT_ERROR = ErrorResponse(GENERAL, GENERAL_ERROR_TITLE, GENERAL_ERROR_BODY)
    }
}