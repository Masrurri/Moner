package com.alviy.moner.core.domain.model

data class Receipt(
    val id: String,
    val channel: String,
    val title: String,
    val destination: String,
    val amount: String,
    val date: String,
    val time: String?,
    val method: String?,
    val note: String?
)
