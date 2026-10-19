package com.alviy.moner.core.common.ext

import com.alviy.moner.core.common.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** Emits LOADING, then whatever [call] returns (SUCCESS or ERROR), on [dispatcher]. */
fun <A> resultFlow(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    call: suspend () -> Result<A>,
): Flow<Result<A>> = flow {
    emit(Result.loading())
    emit(call())
}.flowOn(dispatcher)
