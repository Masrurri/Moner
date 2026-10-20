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
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.navigation.AppNavigator
import com.alviy.moner.core.navigation.NavCommand
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    protected lateinit var binding: VB
    abstract val injectedFragment: Fragment
    private var navController: NavController? = null

    @Inject lateinit var navigatorFactory: AppNavigator.Factory

    protected val appNav: AppNavigator get() = navigatorFactory.create(rootNavController())

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

    private fun rootNavController(): NavController {
        var rootHost: NavHostFragment? = null
        var current: Fragment? = this
        while (current != null) {
            if (current is NavHostFragment) rootHost = current
            current = current.parentFragment
        }
        return rootHost?.navController ?: findNavController()
    }

    protected fun observeNavigation(viewModel: BaseViewModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navEvents.collect { command ->
                    val navController = findNavController()
                    when (command) {
                        is NavCommand.GoTo -> navController.navigate(command.directions)
                        is NavCommand.Back -> navController.popBackStack()
                        is NavCommand.BackTo -> navController.popBackStack(command.destinationId, command.inclusive)
                    }
                }
            }
        }
    }

    /**
     * Observe a *state* (e.g. a ViewModel StateFlow<Result<T>>).
     * Collects only while the view is at least [minState]; the latest value is re-delivered
     * every time the view becomes visible again, which is what you want for rendering state.
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
     * Observe a plain *state* (e.g. a ViewModel StateFlow<T> fed by a DAO through collectFlow).
     * Collects only while the view is at least [minState] and re-renders the latest value on return,
     * so [onChange] should be idempotent UI rendering.
     */
    protected fun <T> observeState(
        flow: StateFlow<T>,
        minState: Lifecycle.State = Lifecycle.State.STARTED,
        onChange: (T) -> Unit,
    ): Job {
        val owner = viewLifecycleOwner
        return owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(minState) {
                flow.collect { onChange(it) }
            }
        }
    }

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