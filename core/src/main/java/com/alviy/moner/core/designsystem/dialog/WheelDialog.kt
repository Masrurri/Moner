package com.alviy.moner.core.designsystem.dialog

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.common.base.BaseDialogFragment
import com.alviy.moner.core.common.ext.sendDialogResult
import com.alviy.moner.core.databinding.DialogWheelBinding
import com.alviy.moner.core.designsystem.adapter.WheelAdapter

/** Snapping wheel picker (e.g. month selection). Result: [selectedItem]. */
class WheelDialog : BaseDialogFragment<DialogWheelBinding>(DialogWheelBinding::inflate) {

    private val items: List<String> by lazy { requireArguments().getStringArrayList(ARG_ITEMS).orEmpty() }
    private val requestKey: String get() = requireArguments().getString(ARG_REQUEST_KEY).orEmpty()
    private var selectedIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedIndex = savedInstanceState?.getInt(STATE_SELECTED_INDEX)
            ?: requireArguments().getInt(ARG_SELECTED_INDEX)
    }

    override fun initView() {
        setupWheel()
        binding.btnSelect.setOnClickListener {
            items.getOrNull(selectedIndex)?.let { item ->
                sendDialogResult(requestKey, bundleOf(RESULT_ITEM to item))
            }
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTED_INDEX, selectedIndex)
    }

    private fun setupWheel() = with(binding.rvWheel) {
        val wheelAdapter = WheelAdapter(items).apply { setSelectedItem(selectedIndex) }
        val snapHelper = PagerSnapHelper()
        adapter = wheelAdapter
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        snapHelper.attachToRecyclerView(this)
        scrollToPosition(selectedIndex)

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                val manager = rv.layoutManager as LinearLayoutManager
                snapHelper.findSnapView(manager)?.let {
                    selectedIndex = manager.getPosition(it)
                    wheelAdapter.setSelectedItem(selectedIndex)
                }
            }
        })
    }

    companion object {
        const val TAG = "WheelDialog"
        private const val ARG_ITEMS = "arg_items"
        private const val ARG_SELECTED_INDEX = "arg_selected_index"
        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val STATE_SELECTED_INDEX = "state_selected_index"
        private const val RESULT_ITEM = "result_item"

        fun newInstance(items: List<String>, initialSelectedIndex: Int = 0, requestKey: String) =
            WheelDialog().apply {
                arguments = bundleOf(
                    ARG_ITEMS to ArrayList(items),
                    ARG_SELECTED_INDEX to initialSelectedIndex,
                    ARG_REQUEST_KEY to requestKey,
                )
            }

        fun selectedItem(result: Bundle): String? = result.getString(RESULT_ITEM)
    }
}
