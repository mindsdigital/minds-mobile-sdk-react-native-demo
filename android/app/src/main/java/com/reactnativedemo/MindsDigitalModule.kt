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
    fun enrollment(document: String, phone: String, enrollmentPromisse: Promise) {
        promisseResult = enrollmentPromisse
        try {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val enrollmentMindsSDK = MindsConfigJava.enrollment(
                        document,
                        phone,
                        BuildConfig.MINDS_DIGITAL_TOKEN
                    )
                    val intent = MindsDigital.getIntent(currentActivity!!.applicationContext, enrollmentMindsSDK)
                    currentActivity?.startActivityForResult(intent, 0)
                } catch(e: InvalidDocument) {
                    promisseResult.reject(e.message, "invalid_document")
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
                } catch (e: MissingMicrophonePermissionException){
                    promisseResult.reject(e.message, "missing_microphone_permission")
                } catch (e: Exception) {
                    promisseResult.reject(e.message, "MINDS_SDK_INIT_ERROR")
                }
            }
        } catch (e: Exception) {
            promisseResult.reject(e.message, "MINDS_SDK_INIT_ERROR")
        }
    }

    @ReactMethod
    fun authentication(document: String, phone: String, authenticationPromisse: Promise) {
        promisseResult = authenticationPromisse
        try {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val authenticationMindsSDK = MindsConfigJava.authentication(
                        document,
                        phone,
                        BuildConfig.MINDS_DIGITAL_TOKEN
                    )
                    val intent = MindsDigital.getIntent(currentActivity!!.applicationContext, authenticationMindsSDK)
                    currentActivity?.startActivityForResult(intent, 0)
                } catch(e: InvalidDocument) {
                    promisseResult.reject(e.message, "invalid_document")
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
                } catch (e: MissingMicrophonePermissionException){
                    promisseResult.reject(e.message, "missing_microphone_permission")
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
        val mindsSDKResponse = data?.extras?.get(VOICE_MATCH_RESPONSE) as VoiceMatchResponse?
        val reactNativeResponse: WritableMap = Arguments.createMap()
        val jsonObject = JSONObject()
        jsonObject.put("success", mindsSDKResponse?.success)
        jsonObject.put("error", JSONObject().apply {
            put("code", mindsSDKResponse?.error?.code)
            put("description", mindsSDKResponse?.error?.description)
        })
        jsonObject.put("id", mindsSDKResponse?.id)
        jsonObject.put("cpf", mindsSDKResponse?.cpf)
        jsonObject.put("external_id", mindsSDKResponse?.externalId)
        jsonObject.put("created_at", mindsSDKResponse?.createdAt)
        jsonObject.put("utc_created_at", mindsSDKResponse?.utcCreatedAt)
        jsonObject.put("result", JSONObject().apply {
            put("recommended_action", mindsSDKResponse?.result?.recommendedAction)
            put("reasons", JSONArray(mindsSDKResponse?.result?.reasons))
        })
        jsonObject.put("details", JSONObject().apply {
            jsonObject.put("flag", JSONObject().apply {
                put("type", mindsSDKResponse?.details?.flag?.type)
                put("status", mindsSDKResponse?.details?.flag?.status)
            })
            put("liveness", JSONObject().apply {
                put("status", mindsSDKResponse?.details?.liveness?.status)
                put("replay_attack", JSONObject().apply {
                    put("enabled", mindsSDKResponse?.details?.liveness?.replayAttack?.enabled)
                    put("status", mindsSDKResponse?.details?.liveness?.replayAttack?.status)
                    put("result", mindsSDKResponse?.details?.liveness?.replayAttack?.result)
                    put("confidence", mindsSDKResponse?.details?.liveness?.replayAttack?.confidence)
                    put("score", mindsSDKResponse?.details?.liveness?.replayAttack?.score)
                    put("threshold", mindsSDKResponse?.details?.liveness?.replayAttack?.threshold)
                })
                put("deepfake", JSONObject().apply {
                    put("enabled", mindsSDKResponse?.details?.liveness?.deepFake?.enabled)
                    put("status", mindsSDKResponse?.details?.liveness?.deepFake?.status)
                    put("result", mindsSDKResponse?.details?.liveness?.deepFake?.result)
                    put("confidence", mindsSDKResponse?.details?.liveness?.deepFake?.confidence)
                    put("score", mindsSDKResponse?.details?.liveness?.deepFake?.score)
                    put("threshold", mindsSDKResponse?.details?.liveness?.deepFake?.threshold)
                })
                put("sentence_match", JSONObject().apply {
                    put("enabled", mindsSDKResponse?.details?.liveness?.sentenceMatch?.enabled)
                    put("status", mindsSDKResponse?.details?.liveness?.sentenceMatch?.status)
                    put("result", mindsSDKResponse?.details?.liveness?.sentenceMatch?.result)
                    put("confidence", mindsSDKResponse?.details?.liveness?.sentenceMatch?.confidence)
                    put("score", mindsSDKResponse?.details?.liveness?.sentenceMatch?.score)
                    put("threshold", mindsSDKResponse?.details?.liveness?.sentenceMatch?.threshold)
                })
            })
            put("voice_match", JSONObject().apply {
                put("status", mindsSDKResponse?.details?.voiceMatch?.status)
                put("result", mindsSDKResponse?.details?.voiceMatch?.result)
                put("confidence", mindsSDKResponse?.details?.voiceMatch?.confidence)
                put("score", mindsSDKResponse?.details?.voiceMatch?.score)
                put("threshold", mindsSDKResponse?.details?.voiceMatch?.threshold)
            })
        })

        val jsonString = jsonObject.toString()

        promisseResult.resolve(jsonString)
    }

    override fun onNewIntent(p0: Intent?) {}
}