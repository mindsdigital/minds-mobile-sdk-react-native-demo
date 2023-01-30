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
import org.json.JSONArray
import org.json.JSONObject

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
    fun authentication(cpf: String, phone: String, authenticationCallback: Callback) {
        callback = authenticationCallback
        val authenticationMindsSDK = MindsConfigJava.authentication(
            cpf,
            phone,
            BuildConfig.MINDS_DIGITAL_TOKEN
        )
        try {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val i = MindsDigital.getIntent(
                        currentActivity!!.applicationContext,
                        authenticationMindsSDK
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

        val jsonObject = JSONObject()
        jsonObject.put("success", mindsSDKResponse.success)
        jsonObject.put("error", JSONObject().apply {
            put("code", mindsSDKResponse.error?.code)
            put("description", mindsSDKResponse.error?.description)
        })
        jsonObject.put("id", mindsSDKResponse.id)
        jsonObject.put("cpf", mindsSDKResponse.cpf)
        jsonObject.put("external_id", mindsSDKResponse.external_id)
        jsonObject.put("created_at", mindsSDKResponse.created_at)
        jsonObject.put("result", JSONObject().apply {
            put("recommended_action", mindsSDKResponse.result?.recommended_action)
            put("reasons", JSONArray(mindsSDKResponse.result?.reasons))
        })
        jsonObject.put("details", JSONObject().apply {
            jsonObject.put("flag", JSONObject().apply {
                put("id", mindsSDKResponse.details?.flag?.id)
                put("type", mindsSDKResponse.details?.flag?.type)
                put("description", mindsSDKResponse.details?.flag?.description)
                put("status", mindsSDKResponse.details?.flag?.status)
            })
            put("voice_match", JSONObject().apply {
                put("result", mindsSDKResponse.details?.voice_match?.result)
                put("confidence", mindsSDKResponse.details?.voice_match?.confidence)
                put("status", mindsSDKResponse.details?.voice_match?.status)
            })
        })

        val jsonString = jsonObject.toString()

        callback.invoke(jsonString)
    }

    override fun onNewIntent(p0: Intent?) {}
}