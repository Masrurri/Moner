package com.alviy.moner.core.common.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Collects [flow] only while [owner] is at least [minState], restarting on every return.
 * Replaces the launch + repeatOnLifecycle + collect boilerplate in screens.
 */
fun <T> Flow<T>.collectWhile(
    owner: LifecycleOwner,
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit,
): Job = owner.lifecycleScope.launch {
    owner.repeatOnLifecycle(minState) { collect { action(it) } }
}
