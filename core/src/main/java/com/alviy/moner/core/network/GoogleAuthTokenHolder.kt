package com.alviy.moner.core.network

import javax.inject.Inject
import javax.inject.Singleton

/** Current OAuth access token. Set it after each authorize(); no need to rebuild OkHttp. */
@Singleton
class GoogleAuthTokenHolder @Inject constructor() {
    @Volatile var accessToken: String? = null
}