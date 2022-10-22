package com.reactnativedemo

import digital.minds.clients.sdk.kotlin.domain.helpers.ProcessType
import digital.minds.clients.sdk.kotlin.main.MindsSDK

object MindsConfigJava {
    fun enrollment(cpf: String, phone: String, token: String): MindsSDK {
        return MindsSDK.Builder()
            .setToken(token)
            .setCPF(cpf)
            .setExternalID("4")
            .setPhoneNumber(phone)
            .setProcessType(ProcessType.ENROLLMENT)
            .build()
    }

    fun verification(cpf: String, phone: String, token: String): MindsSDK {
        return MindsSDK.Builder()
            .setToken(token)
            .setCPF(cpf)
            .setExternalID("4")
            .setPhoneNumber(phone)
            .setProcessType(ProcessType.VERIFICATION)
            .build()
    }
}