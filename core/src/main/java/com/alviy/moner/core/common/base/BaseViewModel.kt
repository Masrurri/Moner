package com.alviy.moner.core.common.base

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.navigation.NavCommand
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

open class BaseViewModel : ViewModel() {
    private val _navEvents = Channel<NavCommand>(Channel.BUFFERED)
    internal val navEvents = _navEvents.receiveAsFlow()

    // One running job per key, so independent loads (e.g. "list" vs "detail") don't cancel each other.
    private val jobs = ConcurrentHashMap<String, Job>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError(throwable)
    }

    protected fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    protected open fun onError(throwable: Throwable) {
        // Override in child if needed
    }

    /** Cancels the previous job with the same [key], then launches [block] in viewModelScope. */
    protected fun launchSingle(
        key: String = DEFAULT_KEY,
        block: suspend CoroutineScope.() -> Unit,
    ): Job {
        jobs[key]?.cancel()
        val job = viewModelScope.launch(block = block)
        jobs[key] = job
        job.invokeOnCompletion { jobs.remove(key, job) } // only removes if it's still the current job
        return job
    }

    /**
     * Collects a repository flow into this state holder, cancelling any previous load with the same key.
     * [onEach] runs for every emitted result before the state is updated (side effects only).
     */
    protected fun <T> MutableStateFlow<in Result<T>>.loadFrom(
        flow: Flow<Result<T>>,
        key: String = DEFAULT_KEY,
        onEach: (Result<T>) -> Unit = {},
    ): Job = launchSingle(key) {
        flow.collect { result ->
            onEach(result)
            this@loadFrom.value = result
        }
    }

    protected fun cancelJob(key: String = DEFAULT_KEY) {
        jobs.remove(key)?.cancel()
    }

    protected fun goTo(directions: NavDirections) {
        _navEvents.trySend(NavCommand.GoTo(directions))
    }

    protected fun back() {
        _navEvents.trySend(NavCommand.Back)
    }

    protected fun backTo(@IdRes destinationId: Int, inclusive: Boolean = false) {
        _navEvents.trySend(NavCommand.BackTo(destinationId, inclusive))
    }

    protected companion object {
        const val DEFAULT_KEY = "default"
    }
}