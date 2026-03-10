package com.alviy.core.designsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.alviy.app.core.R
import com.alviy.app.core.databinding.DialogWheelBinding

class WheelDialog : DialogFragment() {
    private lateinit var binding: DialogWheelBinding
    private lateinit var listContent: List<String>
    private var selectedIndex = 0
    private var onClickPrimaryBtn: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listContent = arguments?.getStringArrayList(ARG_LIST) ?: emptyList()
        setStyle(STYLE_NO_TITLE, R.style.RoundedDialog)
        selectedIndex = arguments?.getInt(ARG_SELECTED_INDEX) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogWheelBinding.inflate(inflater, container, false)
        return binding.clContainer
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        val adapter = WheelAdapter(listContent)
        adapter.setSelectedItem(selectedIndex) // Set initial selected
        binding.rvWheel.adapter = adapter
        binding.rvWheel.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvWheel)

        binding.rvWheel.scrollToPosition(selectedIndex)

        binding.rvWheel.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == SCROLL_STATE_IDLE) {
                    val layoutManager = rv.layoutManager as LinearLayoutManager
                    val centerView = snapHelper.findSnapView(layoutManager)
                    centerView?.let {
                        selectedIndex = layoutManager.getPosition(it)
                        adapter.setSelectedItem(selectedIndex)
                    }
                }
            }
        })
    }

    private fun setupButtons() {
        binding.btnSelect.setOnClickListener {
            onClickPrimaryBtn?.invoke(listContent[selectedIndex])
            dismiss()
        }
    }

    companion object {
        private const val ARG_LIST = "arg_list"
        private const val ARG_SELECTED_INDEX = "arg_selected_index"

        fun newInstance(
            listContent: List<String>,
            initialSelectedIndex: Int = 0,
            onClickPrimaryBtn: ((String) -> Unit)? = null
        ): WheelDialog {
            val dialog = WheelDialog()
            val bundle = Bundle()
            bundle.putStringArrayList(ARG_LIST, ArrayList(listContent))
            bundle.putInt(ARG_SELECTED_INDEX, initialSelectedIndex)
            dialog.arguments = bundle
            dialog.onClickPrimaryBtn = onClickPrimaryBtn
            return dialog
        }
    }
}