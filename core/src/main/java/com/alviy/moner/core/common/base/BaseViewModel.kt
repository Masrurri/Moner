package com.alviy.moner.core.common.base

import android.util.Log
import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.alviy.moner.core.common.result.Result
import com.alviy.moner.core.navigation.NavCommand
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

abstract class BaseViewModel : ViewModel() {
    private val _navEvents = Channel<NavCommand>(Channel.BUFFERED)
    internal val navEvents = _navEvents.receiveAsFlow()

    // One running job per key, so independent loads (e.g. "list" vs "detail") don't cancel each other.
    private val jobs = ConcurrentHashMap<String, Job>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError(throwable)
    }

    /** Launches [block] in viewModelScope; uncaught exceptions go to [onError] instead of crashing. */
    protected fun launchSafely(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(exceptionHandler, block = block)

    /** Called for exceptions escaping [launchSafely]. Override to surface them to the UI. */
    protected open fun onError(throwable: Throwable) {
        Log.e(javaClass.simpleName, "Unhandled error", throwable)
    }

    /** Cancels the previous job with the same [key], then launches [block] in viewModelScope. */
    protected fun launchSingle(
        key: String = DEFAULT_KEY,
        block: suspend CoroutineScope.() -> Unit,
    ): Job {
        jobs[key]?.cancel()
        val job = viewModelScope.launch(exceptionHandler, block = block)
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

    /** Screen state: shared while the UI is subscribed (+5s grace for configuration changes). */
    protected fun <T> Flow<T>.stateWhileSubscribed(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), initial)

    /** State that must be current before the UI subscribes (e.g. read synchronously on a click). */
    protected fun <T> Flow<T>.stateEagerly(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.Eagerly, initial)

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
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
