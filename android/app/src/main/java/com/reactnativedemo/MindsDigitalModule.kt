package com.reactnativedemo
import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.*
import digital.minds.clients.sdk.android.MindsDigital
import digital.minds.clients.sdk.kotlin.data.model.VoiceMatchResponse
import digital.minds.clients.sdk.kotlin.domain.constants.VOICE_MATCH_RESPONSE
import digital.minds.clients.sdk.kotlin.domain.exceptions.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MindsDigitalModule internal constructor(context: ReactApplicationContext?) :
    ReactContextBaseJavaModule(context), ActivityEventListener {

    private lateinit var promisseResult: Promise

    init {
        context?.addActivityEventListener(this)
    }

    @ReactMethod
    fun enrollment(cpf: String, phone: String, enrollmentPromisse: Promise) {
        promisseResult = enrollmentPromisse
        try {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val enrollmentMindsSDK = MindsConfigJava.enrollment(
                        cpf,
                        phone,
                        BuildConfig.MINDS_DIGITAL_TOKEN
                    )
                    val intent = MindsDigital.getIntent(currentActivity!!.applicationContext, enrollmentMindsSDK)
                    currentActivity?.startActivityForResult(intent, 0)
                } catch (e: InvalidCPF) {
                    promisseResult.reject(e.message, "invalid_cpf")
                } catch (e: InvalidPhoneNumber) {
                    promisseResult.reject(e.message, "invalid_phone_number")
                } catch (e: CustomerNotFoundToPerformVerification) {
                    promisseResult.reject(e.message, "customer_not_found")
                } catch (e: CustomerNotEnrolled) {
                    promisseResult.reject(e.message, "customer_not_enrolled")
                } catch (e: CustomerNotCertified) {
                    promisseResult.reject(e.message, "customer_not_certified")
                } catch (e: InvalidToken) {
                    promisseResult.reject(e.message, "invalid_token")
                } catch (e: InternalServerException) {
                    promisseResult.reject(e.message, "internal_server_error")
                } catch (e: Exception) {
                    promisseResult.reject(e.message, "MINDS_SDK_INIT_ERROR")
                }
            }
        } catch (e: Exception) {
            promisseResult.reject(e.message, "MINDS_SDK_INIT_ERROR")
        }
    }

    @ReactMethod
    fun authentication(cpf: String, phone: String, authenticationPromisse: Promise) {
        promisseResult = authenticationPromisse
        try {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val authenticationMindsSDK = MindsConfigJava.authentication(
                        cpf,
                        phone,
                        BuildConfig.MINDS_DIGITAL_TOKEN
                    )
                    val intent = MindsDigital.getIntent(currentActivity!!.applicationContext, authenticationMindsSDK)
                    currentActivity?.startActivityForResult(intent, 0)
                } catch (e: InvalidCPF) {
                    promisseResult.reject(e.message, "invalid_cpf")
                } catch (e: InvalidPhoneNumber) {
                    promisseResult.reject(e.message, "invalid_phone_number")
                } catch (e: CustomerNotFoundToPerformVerification) {
                    promisseResult.reject(e.message, "customer_not_found")
                } catch (e: CustomerNotEnrolled) {
                    promisseResult.reject(e.message, "customer_not_enrolled")
                } catch (e: CustomerNotCertified) {
                    promisseResult.reject(e.message, "customer_not_certified")
                } catch (e: InvalidToken) {
                    promisseResult.reject(e.message, "invalid_token")
                } catch (e: InternalServerException) {
                    promisseResult.reject(e.message, "internal_server_error")
                } catch (e: Exception) {
                    promisseResult.reject(e.message, "MINDS_SDK_INIT_ERROR")
                }
            }
        } catch (e: Exception) {
            promisseResult.reject(e.message, "MINDS_SDK_INIT_ERROR")
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

        promisseResult.resolve(jsonString)
    }

    override fun onNewIntent(p0: Intent?) {}
}