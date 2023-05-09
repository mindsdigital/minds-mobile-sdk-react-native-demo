
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
  var resolveCallback: RCTPromiseResolveBlock?
  var rejectCallback: RCTPromiseRejectBlock?
  var navigationController: UINavigationController?
  
  @objc func enrollment(_ uiNavigationController: UINavigationController, cpf: String, phone: String, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolveCallback = resolver
    rejectCallback = reject
    navigationController = uiNavigationController
  
    startSDK(navigationController!, processType: .enrollment, cpf: cpf, token: "Token", telephone: phone, externalId: nil, externalCustomerId: nil)
  }
  
  @objc func authentication(_ uiNavigationController: UINavigationController, cpf: String, phone: String, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolveCallback = resolver
    rejectCallback = reject
    navigationController = uiNavigationController
    
    startSDK(navigationController!, processType: .authentication, cpf: cpf, token: "Token", telephone: phone, externalId: nil, externalCustomerId: nil)
  }
  
  private func startSDK(_ uiNavigationController: UINavigationController, processType: MindsSDK.ProcessType, cpf: String, token: String, telephone: String, externalId: String?, externalCustomerId: String?) {
    sdk = MindsSDK(delegate: self)
    sdk?.setToken(token)
    sdk?.setExternalId(externalId)
    sdk?.setExternalCustomerId(externalCustomerId)
    sdk?.setPhoneNumber(telephone)
    sdk?.setShowDetails(true)
    sdk?.setCpf(cpf)
    sdk?.setProcessType(processType)
    sdk?.setEnvironment(.sandbox)
    
    DispatchQueue.main.async {
            
    
      self.sdk?.initialize(on: uiNavigationController) { error in
        if let error = error {
          do {
            throw error
          } catch DomainError.invalidCPF(let message) {
            self.rejectCallback?(message!, "invalid_phone_number", nil)
            
          } catch DomainError.invalidPhoneNumber(let message) {
            self.rejectCallback?(message!, "invalid_phone_number", nil)
            
          } catch DomainError.customerNotFoundToPerformVerification(let message) {
            self.rejectCallback?(message!, "customer_not_found", nil)
            
          } catch DomainError.customerNotEnrolled(let message) {
            self.rejectCallback?(message!, "customer_not_enrolled", nil)
            
          } catch DomainError.customerNotCertified(let message) {
            self.rejectCallback?(message!, "customer_not_certified", nil)
            
          } catch DomainError.invalidToken {
            self.rejectCallback?("Invalid Token", "invalid_token", nil)
            
          } catch DomainError.undefinedEnvironment {
            self.rejectCallback?("No environment defined", "undefined_environment", nil)
            
          } catch DomainError.internalServerException {
            self.rejectCallback?("Internal server error", "internal_server_error", nil)
            
          } catch {
            print("\(error): \(error.localizedDescription)")
            self.rejectCallback?("ERROR", error.localizedDescription, nil)
          }
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
      self.resolveCallback?([jsonString])
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


RCT_EXPORT_METHOD(enrollment:(NSString *)cpf phone:(NSString *)phone resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
 
  MindsDigital *mindsDigital = [[MindsDigital alloc] init];
  
  dispatch_async(dispatch_get_main_queue(), ^{
    
    AppDelegate *delegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
 
    [mindsDigital enrollment:delegate.navController cpf:cpf phone:phone resolver:resolve reject: reject];

  });
}

RCT_EXPORT_METHOD(authentication:(NSString *)cpf phone:(NSString *)phone resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  MindsDigital *mindsDigital = [[MindsDigital alloc] init];
  
  dispatch_async(dispatch_get_main_queue(), ^{
    
    AppDelegate *delegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
 
    [mindsDigital authentication:delegate.navController cpf:cpf phone:phone resolver:resolve reject: reject];

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
const response = await MindsDigitalModule.authentication(
    cpf,
    phone,
);
    }
    />
```

## 📌 Observação

É importante ressaltar que o integrador deve garantir que a permissão do microfone seja fornecida em seu aplicativo Flutter antes de utilizar a SDK. Sem essa permissão, a SDK não funcionará corretamente. É responsabilidade do integrador garantir que seu aplicativo tenha as permissões necessárias para utilizar a SDK com sucesso.

