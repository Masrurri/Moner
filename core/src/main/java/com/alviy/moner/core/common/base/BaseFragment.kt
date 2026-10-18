package com.alviy.moner.core.common.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    protected lateinit var binding: VB
    abstract val injectedFragment: Fragment
    private var navController: NavController? = null

    abstract fun initView()
    abstract fun observeData()

    override fun onResume() {
        super.onResume()
        val breadCrumb = buildString {
            navController?.backQueue?.toList()?.forEachIndexed { index, navBackStackEntry ->
                val destination = navBackStackEntry.destination
                if (destination is FragmentNavigator.Destination) {
                    val dName = destination.label ?: destination.id.toString()
                    if (index>0) append(" > ")
                    append(dName)
                }
            }
        }
        Log.d("Bread", breadCrumb)
    }

    protected abstract fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = initBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
        activity?.runOnUiThread {
            navController = NavHostFragment.findNavController(injectedFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    /**
     * Observe a *state* (e.g. a ViewModel StateFlow<Result<T>>).
     * Collects only while the view is at least [minState]; the latest value is re-delivered
     * every time the view becomes visible again, which is what you want for rendering state.
     */
    protected fun <T> observeStateFlow(
        flow: StateFlow<Result<T>?>,   // null = nothing requested yet -> no callback fires
        minState: Lifecycle.State = Lifecycle.State.STARTED,
        onLoading: (() -> Unit)? = null,
        onError: ((Result<T>) -> Unit)? = null,
        onSuccess: (T) -> Unit,
    ): Job {
        val owner = viewLifecycleOwner
        return owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(minState) {
                flow.filterNotNull().collect { dispatch(it, onLoading, onError, onSuccess) }
            }
        }
    }

    /**
     * Run a *one-shot* cold flow (e.g. repository.getX()) safely and return its [Job] so the
     * caller can cancel it.
     *  - runs once: unlike observeStateFlow it is NOT restarted when the view re-enters STARTED,
     *    so the network call isn't repeated;
     *  - exceptions thrown by the flow become an ERROR result instead of crashing;
     *  - callbacks are deferred until the view is STARTED (safe for navigation/dialogs);
     *  - cancelled automatically with the view.
     */
    protected fun <T> safeCallJob(
        flow: Flow<Result<T>>,
        onLoading: (() -> Unit)? = null,
        onError: ((Result<T>) -> Unit)? = null,
        onSuccess: (T) -> Unit,
    ): Job {
        val owner = viewLifecycleOwner
        return owner.lifecycleScope.launch {
            flow
                .catch { e ->
                    if (e is CancellationException) throw e
                    emit(Result.error(ErrorCodes.GENERAL, "General Error", e.message))
                }
                .collect { result ->
                    owner.lifecycle.withStarted { dispatch(result, onLoading, onError, onSuccess) }
                }
        }
    }

    // ---------------------------------------------------------------- internals
    @Suppress("UNCHECKED_CAST")
    private fun <T> dispatch(
        result: Result<T>,
        onLoading: (() -> Unit)?,
        onError: ((Result<T>) -> Unit)?,
        onSuccess: (T) -> Unit,
    ) {
        when (result.status) {
            Result.Status.LOADING -> if (onLoading != null) onLoading() else handleLoading()
            Result.Status.SUCCESS -> onSuccess(result.data as T)
            Result.Status.ERROR -> if (onError != null) onError(result) else handleError(result.code, result.title, result.message)
        }
    }

    private fun handleLoading() {

    }

    private fun handleError(code: String?, title: String?, message: String?) {
        Log.e("Dispatch-Error", "code: $code, title: $title, message: $message")
    }
}