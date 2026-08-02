package com.alviy.moner.core.designsystem

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StyleRes
import com.alviy.moner.core.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Base for the "half/near-full height sheet, no header/footer separators, no close button"
 * dialog pattern used by Add Expense and Add Category Budget.
 */
abstract class FullScreenSheetDialogFragment(
    @StyleRes private val theme: Int = R.style.Widget_Moner_FullScreenSheet
) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val sheet = dialog.findViewById<android.widget.FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            sheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
                behavior.isDraggable = true
            }
        }
        return dialog
    }
}
