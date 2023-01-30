
### Primeiros passos

Os primeiros passos é adicionar a MindsSDK no projeto iOS do React Native, seguindo a documentação do projeto iOS em <https://sandbox-api.minds.digital/docs/sdk/ios/visao_geral>

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
import minds_sdk_mobile_ios
import AVFAudio

@objc(MindsDigital)
class MindsDigital: NSObject {
  
  var sdk = MindsSDK.shared
  var rnCallback: RCTResponseSenderBlock?
  var navigationController: UINavigationController?

  @objc func enrollment(_ uiNavigationController: UINavigationController, cpf: String, phone: String, callback: @escaping RCTResponseSenderBlock) {
    rnCallback = callback;
    navigationController = uiNavigationController

    DispatchQueue.main.async { [self] in
      self.sdk.token = ""
      self.sdk.setExternalId("")
      self.sdk.setPhoneNumber(phone)
      self.sdk.setCpf(cpf)
      self.sdk.setProcessType(processType: MindsSDK.ProcessType.enrollment)

      let sdkInitializer = MindsSDKInitializer()
      
      sdkInitializer.initialize(on: uiNavigationController, delegate: self) { error in
        if let error = error {
            print("-- error: \(error)")
        }
      }
      
    }
  }
  
  @objc func verification(_ uiNavigationController: UINavigationController, cpf: String, phone: String, callback: @escaping RCTResponseSenderBlock) {
    
    rnCallback = callback;
    navigationController = uiNavigationController

    DispatchQueue.main.async { [self] in
      self.sdk.token = ""
      self.sdk.setExternalId("")
      self.sdk.setPhoneNumber(phone)
      self.sdk.setCpf(cpf)
      self.sdk.setProcessType(processType: MindsSDK.ProcessType.verification)

      let sdkInitializer = MindsSDKInitializer()
      
      sdkInitializer.initialize(on: uiNavigationController, delegate: self) { error in
        if let error = error {
            print("-- error: \(error)")
        }
      }
      
    }
    
  }
  
  private func biometricsReceive(_ response: BiometricResponse) {
    var status = response.status
    if (status == "invalid_length") {
       status = "invalid_length_exception"
    }
    
    rnCallback?([[
      "status": response.status as Any,
      "confidence": response.confidence as Any,
      "match_prediction": response.matchPrediction as Any,
      "success": response.success as Any,
      "message": response.message as Any,
      "external_id": response.externalId as Any,
      "cpf": response.cpf as Any,
      "verification_id": response.verificationID as Any,
      "action": response.action as Any,
      "whitelisted": response.whitelisted as Any,
      "fraud_risk": response.fraudRisk as Any,
      "enrollment_external_id": response.enrollmentExternalId as Any,
        ]])
    
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
//  RCTMindsDigitalModule.h
#import <React/RCTBridgeModule.h>
@interface RCTMindsDigitalModule : NSObject <RCTBridgeModule>
@end
```

### Implementar o módulo nativo
Em seguida, vamos começar a implementar o módulo nativo. Crie o arquivo de implementação correspondente, RCTMindsDigitalModule.m, na mesma pasta e inclua o seguinte conteúdo:
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

RCT_EXPORT_METHOD(enrollment:(NSString *)cpf phone:(NSString *)phone enrollmentCallback:(RCTResponseSenderBlock)enrollmentCallback)
{
 
  MindsDigital *mindsDigital = [[MindsDigital alloc] init];
  
  dispatch_async(dispatch_get_main_queue(), ^{
    
    AppDelegate *delegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
 
    [mindsDigital enrollment:delegate.navController cpf:cpf phone:phone callback:enrollmentCallback];

  });
}

RCT_EXPORT_METHOD(verification:(NSString *)cpf phone:(NSString *)phone verificationCallback:(RCTResponseSenderBlock)verificationCallback)
{
  MindsDigital *mindsDigital = [[MindsDigital alloc] init];
  
  dispatch_async(dispatch_get_main_queue(), ^{
    
    AppDelegate *delegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
 
    [mindsDigital verification:delegate.navController cpf:cpf phone:phone callback:verificationCallback];

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
