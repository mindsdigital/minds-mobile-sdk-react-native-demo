### Primeiros passos

Os primeiros passos é adicionar a MindsSDK no projeto Android do React Native, seguindo a documentação do projeto Android em <https://api.minds.digital/docs/sdk/android/visao_geral>

### Criar arquivo MindsConfigJava.kt

Em seguida deve ser criado o arquivo MindsConfigJava.kt

```kotlin
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
```

### Criar um arquivo de módulo nativo personalizado
O próximo passo é criar o arquivo em Kotlin MindsDigitalModule.kt dentro da pasta `android/app/src/main/java/com/your-app-name/ `. Esse arquivo Kotlin contém a classe do seu módulo nativo.

```kotlin
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
        callback = verificationCallback
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
```

### Registrando o Módulo

Uma vez que um módulo nativo é escrito, ele precisa ser registrado no React Native. Para fazer isso, você precisa adicionar seu módulo nativo a um `ReactPackage` e registrá-lo no `ReactPackage` do React Native. Durante a inicialização, o React Native fará um loop sobre todos os pacotes e, para cada `ReactPackage`, registrará cada módulo nativo dentro dele.

O React Native invoca o método `createNativeModules()` em `ReactPackage` para obter a lista de módulos nativos a serem registrados. Para Android, se um módulo não for instanciado e retornado em createNativeModules, ele não estará disponível no JavaScript.

Para adicionar seu módulo nativo ao `ReactPackage`, primeiro crie uma nova classe Java chamada `MindsDigitalPackage.kt` que implementa `ReactPackage` dentro da `android/app/src/main/java/com/your-app-name/`:

Em seguida, adicione o seguinte conteúdo:

```kotlin
package com.reactnativedemo

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class MindsDigitalPackage : ReactPackage {
    override fun createNativeModules(reactApplicationContext: ReactApplicationContext): List<NativeModule> {
        val modules: MutableList<NativeModule> = ArrayList()
        modules.add(MindsDigitalModule(reactApplicationContext))
        return modules
    }

    override fun createViewManagers(reactApplicationContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}

```
Este arquivo importa o módulo nativo que você criou, `MindsDigitalModule`. Em seguida, ele instancia `MindsDigitalModule` dentro da função `createNativeModules()` e retorna como uma lista de `NativeModules` a serem registrados.

Para registrar o pacote `MindsDigitalModule`, você deve adicionar o `MindsDigitalPackage` à lista de pacotes retornados no método `getPackages()` do `ReactNativeHost`. Abra seu arquivo `MainApplication.java`, que pode ser encontrado no seguinte caminho: `android/app/src/main/java/com/your-app-name/MainApplication.java`

Localize o método `getPackages()` do ReactNativeHost e adicione seu pacote à lista de pacotes que o `getPackages()` retorna:

```kotlin
@Override
protected List<ReactPackage> getPackages() {
    @SuppressWarnings("UnnecessaryLocalVariable")
    List<ReactPackage> packages = new PackageList(this).getPackages();
    // Packages that cannot be autolinked yet can be added manually here, for example:
    // packages.add(new MyReactNativePackage());
    packages.add(new MindsDigitalPackage());
    return packages;
}

```

### Usando dentro do React Native

Crie a interface para o `MindsDigitalModule` em TypeScript.

```javascript
import {NativeModules} from 'react-native';

const {MindsDigitalModule} = NativeModules;

export interface MindsSDKResponse {
  id?: number;
  success?: boolean;
  message?: string;
  externalId?: string;
  status?: string;
  cpf?: string;
  verificationId?: string;
  action?: string;
  whitelisted?: boolean;
  fraudRisk?: string;
  enrollmentExternalId?: string;
  matchPrediction?: string;
  confidence?: string;
}

interface MindsDigitalInterface {
  enrollment(
    cpf: string,
    phone: string,
    callback: (response: MindsSDKResponse) => void,
  ): void;
  verification(
    cpf: string,
    phone: string,
    callback: (response: MindsSDKResponse) => void,
  ): void;
}

export default MindsDigitalModule as MindsDigitalInterface;
```

Chame os métodos enrollment ou verification do `MindsDigitalModule` criado.

```javascript
<Button
    title="Autenticação por voz"
    color="#141540"
    onPress={() =>
        MindsDigitalModule.enrollment(
        cpf,
        phone,
        (response: MindsSDKResponse) => {
            let json = JSON.stringify(response, null, 4);
            console.log(json);
        },
        )
    }
    />
```
