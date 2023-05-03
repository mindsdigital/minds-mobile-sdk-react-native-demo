
### Primeiros passos

Os primeiros passos é adicionar a MindsSDK no projeto iOS do React Native, seguindo a documentação do projeto iOS em <https://api.minds.digital/docs/sdk/ios/visao_geral>

## Configure o Info.plist

```xml 
<key>NSMicrophoneUsageDescription</key>
<string>Sua descrição para o uso do microfone</string>
```

### Configure a navegação

No AppDelegate crie uma variável do tipo `UINavigationController` inicializando com o `rootViewController`.

```objective-c
// AppDelegate.h

@property (nonatomic, strong) UINavigationController *navController;
```

```objective-c
// AppDelegate.mm 

self.navController = [[UINavigationController alloc] initWithRootViewController:rootViewController];
self.window.rootViewController = self.navController;
```

### Crie o arquivo MindsDigital.swift e instancie a SDK

```swift
import Foundation
import UIKit
import MindsSDK
import AVFAudio

@objc(MindsDigital)
class MindsDigital: NSObject {
  
  var sdk: MindsSDK?
  var rnCallback: RCTResponseSenderBlock?
  var navigationController: UINavigationController?

  @objc func enrollment(_ uiNavigationController: UINavigationController, cpf: String, phone: String, callback: @escaping RCTResponseSenderBlock) {
    rnCallback = callback;
    navigationController = uiNavigationController

    DispatchQueue.main.async { [self] in
      sdk = MindsSDK(delegate: self)
      sdk?.setToken(token)
      sdk?.setExternalId(nil)
      sdk?.setExternalCustomerId(nil)
      sdk?.setPhoneNumber(phone)
      sdk?.setShowDetails(true)
      sdk?.setCpf(cpf)
      sdk?.setProcessType(MindsSDK.ProcessType.enrollment)
      sdk?.setEnvironment(.production)
      
      sdk?.initialize(on: uiNavigationController) { error in
        if let error = error {
            print("-- error: \(error)")
        }
      }
    }
  }
  
  @objc func authentication(_ uiNavigationController: UINavigationController, cpf: String, phone: String, callback: @escaping RCTResponseSenderBlock) {
    
    rnCallback = callback;
    navigationController = uiNavigationController

    DispatchQueue.main.async { [self] in
      sdk = MindsSDK(delegate: self)
      sdk?.setToken(token)
      sdk?.setExternalId(nil)
      sdk?.setExternalCustomerId(nil)
      sdk?.setPhoneNumber(phone)
      sdk?.setShowDetails(true)
      sdk?.setCpf(cpf)
      sdk?.setProcessType(MindsSDK.ProcessType.authentication)
      sdk?.setEnvironment(.production)
      
      sdk?.initialize(on: uiNavigationController) { error in
        if let error = error {
            print("-- error: \(error)")
        }
      }
      
    }
    
  }
  
  private func biometricsReceive(_ response: BiometricResponse) {
        let json: [String: Any?] = [
            "success": response.success,
                "error": [
                    "code": response.error?.code,
                    "description": response.error?.description
                ],
            "id": response.id,
            "cpf": response.cpf,
            "external_id": response.externalID,
            "created_at": response.createdAt,
                "result": [
                    "recommended_action": response.result?.recommendedAction as Any,
                    "reasons": response.result?.reasons as Any
                ],
                "details": [
                    "flag": [
                        "id": response.details?.flag?.id as Any ,
                        "type": response.details?.flag?.type as Any,
                        "description": response.details?.flag?.description as Any,
                        "status": response.details?.flag?.status as Any
                    ],
                    "voice_match": [
                        "result": response.details?.voiceMatch?.result as Any,
                        "confidence": response.details?.voiceMatch?.confidence as Any,
                        "status": response.details?.voiceMatch?.status as Any
                    ]
                ]
        ]
        if let jsonData = try? JSONSerialization.data(withJSONObject: json),
            let jsonString = String(data: jsonData, encoding: .utf8) {
            print(jsonString)
            rnCallback?([jsonString])
        }
  }
  
}

extension MindsDigital: MindsSDKDelegate {
  func showMicrophonePermissionPrompt() {
      AVAudioSession.sharedInstance().requestRecordPermission { granted in
          print("granted: \(granted)")
      }
  }

  func microphonePermissionNotGranted() {
      print("microphonePermissionNotGranted")
  }

  func onSuccess(_ response: BiometricResponse) {
    self.biometricsReceive(response)
  }
  
  func onError(_ response: BiometricResponse) {
    self.biometricsReceive(response)
  }
}

```

### Criar arquivo RCTMindsDigitalModule.h

A primeira etapa é criar nosso cabeçalho de módulo nativo personalizado principal e arquivos de implementação. Crie um novo arquivo chamado RCTMindsDigitalModule.h

```objective-c
//  RCTMindsDigitalModule.m
//  ReactNativeDemo

#import "RCTMindsDigitalModule.h"
#import <React/RCTLog.h>
#import "ReactNativeDemo-Swift.h"
#import <React/RCTUtils.h>
#import "AppDelegate.h"
#import <React/RCTRootView.h>

#import <React/RCTBridge.h>
#import <React/RCTBundleURLProvider.h>
#import <React/RCTRootView.h>

@implementation RCTMindsDigitalModule

// To export a module named RCTMindsDigitalModule
RCT_EXPORT_MODULE();


RCT_EXPORT_METHOD(enrollment:(NSString *)cpf phone:(NSString *)phone enrollmentCallback:(RCTResponseSenderBlock)enrollmentCallback)
{
 
  MindsDigital *mindsDigital = [[MindsDigital alloc] init];
  
  dispatch_async(dispatch_get_main_queue(), ^{
    
    AppDelegate *delegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
 
    [mindsDigital enrollment:delegate.navController cpf:cpf phone:phone callback:enrollmentCallback];

  });
}

RCT_EXPORT_METHOD(authentication:(NSString *)cpf phone:(NSString *)phone authenticationCallback:(RCTResponseSenderBlock)authenticationCallback)
{
  MindsDigital *mindsDigital = [[MindsDigital alloc] init];
  
  dispatch_async(dispatch_get_main_queue(), ^{
    
    AppDelegate *delegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
 
    [mindsDigital authentication:delegate.navController cpf:cpf phone:phone callback:authenticationCallback];

  });
}

@end
```

### Certifique que o arquivo com final -Bridging-Header.h contém conteúdo

```objective-c
#import <React/RCTBridgeModule.h>
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
    callback: (jsonString: string) => void,
  ): void;
  authentication(
    cpf: string,
    phone: string,
    callback: (jsonString: string) => void,
  ): void;
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

