package com.alviy.moner.core.common.base

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.alviy.moner.core.navigation.NavCommand
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

open class BaseViewModel : ViewModel() {
    private val _navEvents = Channel<NavCommand>(Channel.BUFFERED)
    internal val navEvents = _navEvents.receiveAsFlow()

    // One running job per key, so independent loads don't cancel each other. collectFlow keys by its
    // target state (identity), so each MutableStateFlow has at most one active collector.
    private val jobs = ConcurrentHashMap<Any, Job>()

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
        key: Any = DEFAULT_KEY,
        block: suspend CoroutineScope.() -> Unit,
    ): Job {
        jobs[key]?.cancel()
        val job = viewModelScope.launch(block = block)
        jobs[key] = job
        job.invokeOnCompletion { jobs.remove(key, job) } // only removes if it's still the current job
        return job
    }

    /**
     * Collects this flow (Room DAO, repository, or Retrofit-backed [Result] flow) into [target] in viewModelScope.
     * The job is tracked per [target]: collecting into the same target again cancels the previous collector
     * (latest request wins, e.g. switching month), while different targets run independently — so the same
     * API called with different requests just needs a separate state. No keys, flatMapLatest or stateIn needed.
     * Upstream errors go to [onError] instead of crashing the scope; [onEach] runs after [target] is updated.
     */
    protected fun <T> Flow<T>.collectFlow(
        target: MutableStateFlow<in T>,
        onError: (Throwable) -> Unit = ::onError,
        onEach: suspend (T) -> Unit = {},
    ): Job = launchSingle(target) {
        this@collectFlow
            .onEach { value ->
                target.value = value
                onEach(value)
            }
            .catch { onError(it) }
            .collect()
    }

    /** Cancels the job for [key]; pass a collectFlow target to stop observing into it. */
    protected fun cancelJob(key: Any = DEFAULT_KEY) {
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