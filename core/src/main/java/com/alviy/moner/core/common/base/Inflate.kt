package com.alviy.moner.core.common.base

import android.view.LayoutInflater
import android.view.ViewGroup

/** Matches every generated ViewBinding's `inflate(inflater, parent, attachToParent)`. */
typealias Inflate<VB> = (LayoutInflater, ViewGroup?, Boolean) -> VB
