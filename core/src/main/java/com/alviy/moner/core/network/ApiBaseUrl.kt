package com.alviy.moner.core.network

/** All base URLs in one place. Base URLs must end with "/". */
object ApiBaseUrl {
    const val GMAIL = "https://gmail.googleapis.com/"
    // const val RATES = "https://api.exchangerate.host/"

    /** Hosts that are allowed to receive the Gmail OAuth token. Never send it anywhere else. */
    val GOOGLE_AUTH_HOSTS = setOf("gmail.googleapis.com", "www.googleapis.com")
}