package com.reactnativedemo

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import com.facebook.react.bridge.*
import digital.minds.clients.sdk.android.MindsDigital
import digital.minds.clients.sdk.kotlin.data.model.VoiceMatchResponse
import digital.minds.clients.sdk.kotlin.domain.constants.LOG_TAG
import digital.minds.clients.sdk.kotlin.domain.constants.VOICE_MATCH_RESPONSE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MindsDigitalModule internal constructor(context: ReactApplicationContext?) :
    ReactContextBaseJavaModule(context), ActivityEventListener {

    private lateinit var callback: Callback

    init {
        context?.addActivityEventListener(this)
    }

    @ReactMethod
    fun enrollment(cpf: String, phone: String, enrollmentCallback: Callback) {
        callback = enrollmentCallback
        val enrollmentMindsSDK = MindsConfigJava.enrollment(
            cpf,
            phone,
            BuildConfig.MINDS_DIGITAL_TOKEN
        )
        try {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val i = MindsDigital.getIntent(
                        currentActivity!!.applicationContext,
                        enrollmentMindsSDK
                    )
                    currentActivity?.startActivityForResult(i, 0)
                } catch (e: Exception) {
                    callback.invoke(e.toString())
                }
            }
        } catch (e: Exception) {
            callback.invoke(e.toString())
        }
    }

    @ReactMethod
    fun verification(cpf: String, phone: String, verificationCallback: Callback) {
        val verificationMindsSDK = MindsConfigJava.verification(
            cpf,
            phone,
            BuildConfig.MINDS_DIGITAL_TOKEN
        )
        try {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val i = MindsDigital.getIntent(
                        currentActivity!!.applicationContext,
                        verificationMindsSDK
                    )
                    currentActivity?.startActivityForResult(i, 0)
                } catch (e: Exception) {
                    callback.invoke(e.toString())
                }
            }
        } catch (e: Exception) {
            callback.invoke(e.toString())
        }
    }

    override fun getName(): String {
        return "MindsDigitalModule"
    }

    override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        val mindsSDKResponse = data?.extras?.get(VOICE_MATCH_RESPONSE) as VoiceMatchResponse

        val reactNativeResponse: WritableMap = Arguments.createMap()

        mindsSDKResponse.id?.let { reactNativeResponse.putInt("id", it) }
        mindsSDKResponse.success?.let { reactNativeResponse.putBoolean("success", it) }
        reactNativeResponse.putString("message", mindsSDKResponse.message)
        reactNativeResponse.putString("external_id", mindsSDKResponse.externalId)
        reactNativeResponse.putString("status", mindsSDKResponse.status)
        reactNativeResponse.putString("cpf", mindsSDKResponse.cpf)
        reactNativeResponse.putString("verification_id", mindsSDKResponse.verificationId)
        reactNativeResponse.putString("action", mindsSDKResponse.action)
        mindsSDKResponse.whitelisted?.let { reactNativeResponse.putBoolean("whitelisted", it) }
        reactNativeResponse.putString("fraud_risk", mindsSDKResponse.fraudRisk)
        reactNativeResponse.putString("enrollment_external_id", mindsSDKResponse.enrollmentExternalId)
        reactNativeResponse.putString("match_prediction", mindsSDKResponse.matchPrediction)
        reactNativeResponse.putString("confidence", mindsSDKResponse.confidence)

        callback.invoke(reactNativeResponse)
    }

    override fun onNewIntent(p0: Intent?) {}
}