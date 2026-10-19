package com.alviy.moner.api.gmail.auth

import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.common.api.Scope

/** OAuth setup for read-only Gmail access, shared by every screen that fetches receipts. */
object GmailAuthorization {

    const val SCOPE_READONLY = "https://www.googleapis.com/auth/gmail.readonly"

    fun readOnlyRequest(): AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(SCOPE_READONLY)))
            .build()
}
