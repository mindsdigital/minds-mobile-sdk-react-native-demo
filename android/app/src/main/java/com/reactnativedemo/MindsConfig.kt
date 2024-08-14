package com.reactnativedemo

import digital.minds.clients.sdk.kotlin.domain.helpers.Environment
import digital.minds.clients.sdk.kotlin.domain.helpers.ProcessType
import digital.minds.clients.sdk.kotlin.main.MindsSDK

object MindsConfigJava {
    fun enrollment(document: String, phone: String, token: String): MindsSDK {
        return MindsSDK.Builder()
            .setToken(token)
            .setDocument(document)
            .setPhoneNumber(phone)
            .setShowDetails(true)
            .setProcessType(ProcessType.ENROLLMENT)
            .setEnvironment(Environment.STAGING)
            .build()
    }

    fun authentication(document: String, phone: String, token: String): MindsSDK {
        return MindsSDK.Builder()
            .setToken(token)
            .setDocument(document)
            .setPhoneNumber(phone)
            .setShowDetails(true)
            .setProcessType(ProcessType.AUTHENTICATION)
            .setEnvironment(Environment.STAGING)
            .build()
    }
}