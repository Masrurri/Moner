package com.alviy.moner.core.common.entity

/**
 * App-wide result wrapper. NOTE: this intentionally shadows kotlin.Result inside this project;
 * always import com.alviy.moner.core.common.Result explicitly.
 */
data class Result<out T>(
    val status: Status,
    val data: T?,
    val message: String?,
    val title: String?,
    val code: String?,
) {
    enum class Status { SUCCESS, ERROR, LOADING }

    val isSuccess: Boolean get() = status == Status.SUCCESS
    val isError: Boolean get() = status == Status.ERROR

    companion object {
        fun <T> success(data: T): Result<T> = Result(Status.SUCCESS, data, null, null, null)

        fun <T> error(
            code: String? = null,
            title: String? = null,
            message: String?,
            data: T? = null,
        ): Result<T> = Result(Status.ERROR, data, message, title, code)

        fun <T> loading(data: T? = null): Result<T> = Result(Status.LOADING, data, null, null, null)
    }
}

/** Re-types an ERROR result so it can be returned as Result<R>. */
fun <R> Result<*>.propagateError(): Result<R> = Result.error(code, title, message)

/** Transform the data of a SUCCESS result; ERROR / LOADING pass through. */
@Suppress("UNCHECKED_CAST")
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (status) {
    Result.Status.SUCCESS -> Result.success(transform(data as T))
    Result.Status.ERROR -> propagateError()
    Result.Status.LOADING -> Result.loading()
}

/** Chain a second call that depends on this one's data. Lambda may call suspend functions. */
@Suppress("UNCHECKED_CAST")
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> = when (status) {
    Result.Status.SUCCESS -> transform(data as T)
    Result.Status.ERROR -> propagateError()
    Result.Status.LOADING -> Result.loading()
}