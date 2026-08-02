package com.alviy.moner.core.common.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater, container, false)
        return binding?.root
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
        _binding = null
    }
}