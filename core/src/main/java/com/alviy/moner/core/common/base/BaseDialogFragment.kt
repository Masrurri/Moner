package com.alviy.moner.core.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.alviy.moner.core.R

/**
 * Centered, rounded dialog with a view binding that is cleared in onDestroyView.
 * Set [horizontalMarginRes] to stretch the dialog to the screen width minus that margin on each side.
 */
abstract class BaseDialogFragment<VB : ViewBinding>(
    private val bindingInflater: Inflate<VB>,
    @StyleRes private val themeRes: Int = R.style.Widget_Moner_Dialog_Rounded,
) : DialogFragment() {

    private var _binding: VB? = null

    protected val binding: VB
        get() = checkNotNull(_binding) { "Binding accessed outside the view lifecycle of ${javaClass.simpleName}" }

    @DimenRes
    protected open val horizontalMarginRes: Int? = null

    protected abstract fun initView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, themeRes)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = bindingInflater(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onStart() {
        super.onStart()
        val marginRes = horizontalMarginRes ?: return
        val margin = resources.getDimensionPixelSize(marginRes)
        dialog?.window?.setLayout(
            resources.displayMetrics.widthPixels - margin * 2,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
