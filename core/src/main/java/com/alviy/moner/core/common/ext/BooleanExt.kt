package com.alviy.moner.core.common.ext

fun Boolean?.orTrue() = this ?: true
fun Boolean?.orFalse() = this ?: false
