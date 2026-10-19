package com.alviy.moner.core.common.ext

import android.os.Bundle
import androidx.fragment.app.Fragment

/*
 * Dialogs report back through the Fragment Result API instead of stored lambdas,
 * so results still arrive after rotation or process death.
 *
 *   Host:   onDialogResult(REQUEST_MONTH) { WheelDialog.selectedItem(it)?.let(viewModel::selectMonth) }
 *           WheelDialog.newInstance(items, index, REQUEST_MONTH).showDialog(childFragmentManager, TAG)
 *   Dialog: sendDialogResult(requestKey, bundleOf(...))
 */

/** Listens for results of dialogs shown on this fragment's childFragmentManager. Call from initView. */
fun Fragment.onDialogResult(requestKey: String, onResult: (Bundle) -> Unit) {
    childFragmentManager.setFragmentResultListener(requestKey, viewLifecycleOwner) { _, bundle ->
        onResult(bundle)
    }
}

/** Delivers [result] to the fragment that showed this dialog. */
fun Fragment.sendDialogResult(requestKey: String, result: Bundle) {
    parentFragmentManager.setFragmentResult(requestKey, result)
}
