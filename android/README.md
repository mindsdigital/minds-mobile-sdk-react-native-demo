### Primeiros passos

Os primeiros passos é adicionar a MindsSDK no projeto Android do React Native, seguindo a documentação do projeto Android em <https://api.minds.digital/docs/sdk/android/visao_geral>

### Criar arquivo MindsConfigJava.kt

Em seguida deve ser criado o arquivo MindsConfigJava.kt

```kotlin
object MindsConfigJava {
    fun enrollment(cpf: String, phone: String, token: String): MindsSDK {
        return MindsSDK.Builder()
            .setToken(token)
            .setCPF(cpf)
            .setPhoneNumber(phone)
            .setShowDetails(true)
            .setProcessType(ProcessType.ENROLLMENT)
            .setEnvironment(Environment.PRODUCTION)
            .build()
    }

    fun authentication(cpf: String, phone: String, token: String): MindsSDK {
        return MindsSDK.Builder()
            .setToken(token)
            .setCPF(cpf)
            .setPhoneNumber(phone)
            .setShowDetails(true)
            .setProcessType(ProcessType.AUTHENTICATION)
            .setEnvironment(Environment.PRODUCTION)
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


interface MindsDigitalInterface {
  enrollment(
    cpf: string,
    phone: string,
  ): Promise<string>;
  authentication(
    cpf: string,
    phone: string,
  ): Promise<string>;
}

export default MindsDigitalModule as MindsDigitalInterface;
```
Exemplo de classe que pode ser criada para mapear o JSON 

```javascript
interface VoiceMatchResponse {
  success?: boolean;
  error?: Error;
  id?: number;
  cpf?: string;
  external_id?: string;
  created_at?: string;
  result?: Result;
  details?: Details;
}

interface Error {
  code: string;
  description: string;
}

interface Result {
  recommended_action: string;
  reasons: Array<string>;
}

interface Details {
  flag: Flag;
  voice_match: VoiceMatch;
}

interface Flag {
  id: number;
  type: string;
  description: string;
  status: string;
}

interface VoiceMatch {
  result: string;
  confidence: string;
  status: string;
}
```

Chame os métodos enrollment ou authentication do `MindsDigitalModule` criado.

```javascript
<Button
    title="Autenticação por voz"
    color="#141540"
    onPress={() =>
        MindsDigitalModule.authentication(
        cpf,
        phone,
        (response: jsonString) => {
            let json = JSON.parse(jsonString)
            console.log(json);
        },
        )
    }
    />
```

## 📌 Observação

É importante ressaltar que o integrador deve garantir que a permissão do microfone seja fornecida em seu aplicativo Flutter antes de utilizar a SDK. Sem essa permissão, a SDK não funcionará corretamente. É responsabilidade do integrador garantir que seu aplicativo tenha as permissões necessárias para utilizar a SDK com sucesso.

