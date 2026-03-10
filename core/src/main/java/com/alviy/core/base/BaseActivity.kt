package com.alviy.core.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (layoutInflater: LayoutInflater) -> VB
) : AppCompatActivity() {

    private lateinit var _binding: VB
    protected val binding get() = _binding

    abstract fun initView()
    abstract fun observeData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater.invoke(layoutInflater)
        setContentView(binding.root)

        initView()
        observeData()
    }
}