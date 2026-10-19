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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.alviy.moner.core.common.ext.collectWhile
import com.alviy.moner.core.common.result.ErrorCodes
import com.alviy.moner.core.common.result.ErrorMessages
import com.alviy.moner.core.common.result.Result
import com.alviy.moner.core.navigation.AppNavigator
import com.alviy.moner.core.navigation.NavCommand
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Base screen: owns the view binding (cleared in onDestroyView so views never leak),
 * cross-module navigation and lifecycle-safe flow collection.
 *
 *   class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate)
 */
abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: Inflate<VB>
) : Fragment() {

    private var _binding: VB? = null

    /** Valid between onCreateView and onDestroyView only. */
    protected val binding: VB
        get() = checkNotNull(_binding) { "Binding accessed outside the view lifecycle of ${javaClass.simpleName}" }

    @Inject lateinit var navigatorFactory: AppNavigator.Factory

    /** Navigates across feature graphs through the app module, which knows every graph. */
    protected val appNav: AppNavigator get() = navigatorFactory.create(rootNavController())

    protected abstract fun initView()
    protected open fun observeData() = Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = bindingInflater(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Collects [flow] while the view is STARTED; the usual way to render a ViewModel StateFlow. */
    protected fun <T> collect(flow: Flow<T>, action: suspend (T) -> Unit): Job =
        flow.collectWhile(viewLifecycleOwner, action = action)

    protected fun observeNavigation(viewModel: BaseViewModel) {
        viewModel.navEvents.collectWhile(viewLifecycleOwner) { command ->
            val navController = findNavController()
            when (command) {
                is NavCommand.GoTo -> navController.navigate(command.directions)
                is NavCommand.Back -> navController.popBackStack()
                is NavCommand.BackTo -> navController.popBackStack(command.destinationId, command.inclusive)
            }
        }
    }

    /**
     * Observe a *state* (e.g. a ViewModel StateFlow<Result<T>>).
     * Collects only while the view is at least [minState]; a value already handled is not
     * re-delivered when the view becomes visible again.
     */
    protected fun <T> observeStateFlow(
        flow: StateFlow<Result<T>?>,
        minState: Lifecycle.State = Lifecycle.State.STARTED,
        onLoading: (() -> Unit)? = null,
        onError: ((Result<T>) -> Unit)? = null,
        onSuccess: (T) -> Unit,
    ): Job {
        val owner = viewLifecycleOwner
        return owner.lifecycleScope.launch {
            var lastHandled: Result<T>? = null          // outlives each STARTED/STOPPED cycle
            owner.repeatOnLifecycle(minState) {
                flow.filterNotNull().collect { result ->
                    if (result === lastHandled) return@collect   // replayed after resume, already shown
                    lastHandled = result
                    dispatch(result, onLoading, onError, onSuccess)
                }
            }
        }
    }

    /**
     * Run a *one-shot* cold flow (e.g. repository.getX()) safely and return its [Job] so the
     * caller can cancel it.
     *  - runs once: unlike observeStateFlow it is NOT restarted when the view re-enters STARTED;
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
                    emit(Result.error(ErrorCodes.GENERAL, ErrorMessages.GENERAL_TITLE, e.message))
                }
                .collect { result ->
                    owner.lifecycle.withStarted { dispatch(result, onLoading, onError, onSuccess) }
                }
        }
    }

    /** Default for results without an onLoading callback. Override to show a shared loader. */
    protected open fun handleLoading() = Unit

    /** Default for results without an onError callback. Override to show a shared error UI. */
    protected open fun handleError(result: Result<*>) {
        Log.e(TAG, "code: ${result.code}, title: ${result.title}, message: ${result.message}")
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> dispatch(
        result: Result<T>,
        onLoading: (() -> Unit)?,
        onError: ((Result<T>) -> Unit)?,
        onSuccess: (T) -> Unit,
    ) {
        when (result.status) {
            Result.Status.LOADING -> onLoading?.invoke() ?: handleLoading()
            Result.Status.SUCCESS -> onSuccess(result.data as T)
            Result.Status.ERROR -> onError?.invoke(result) ?: handleError(result)
        }
    }

    /** The outermost NavHostFragment's controller, which owns the app-level graph. */
    private fun rootNavController(): NavController {
        var rootHost: NavHostFragment? = null
        var current: Fragment? = this
        while (current != null) {
            if (current is NavHostFragment) rootHost = current
            current = current.parentFragment
        }
        return rootHost?.navController ?: findNavController()
    }

    private companion object {
        const val TAG = "BaseFragment"
    }
}
