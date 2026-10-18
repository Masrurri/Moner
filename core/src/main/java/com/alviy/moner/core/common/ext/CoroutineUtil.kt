package com.alviy.moner.core.common.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.alviy.moner.core.common.entity.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

/** Emits LOADING, then whatever [networkCall] returns (SUCCESS or ERROR). */
fun <A> resultFlow(
    dispatcher: CoroutineDispatcher = Dispatchers.IO, // injectable for tests
    networkCall: suspend () -> Result<A>,
): Flow<Result<A>> = flow {
    emit(Result.loading())
    emit(networkCall())
}.flowOn(dispatcher)