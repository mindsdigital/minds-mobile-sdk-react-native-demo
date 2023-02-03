//
//  MindsDigital.swift
//  ReactNativeDemo
//
//  Created by Divino Borges on 02/01/23.
//

import Foundation
import UIKit
import minds_sdk_mobile_ios
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
      sdk = MindsSDK()
      sdk?.setToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzZWNyZXRfNjBfYXBpIiwiY29tcGFueV9pZCI6NjB9.rxePCNyDUZWELHZj49s_oki8StezhADQVeId39NwMV4")
      sdk?.setExternalId(nil)
      sdk?.setExternalCustomerId(nil)
      sdk?.setPhoneNumber(phone)
      sdk?.setShowDetails(true)
      sdk?.setCpf(cpf)
      sdk?.setProcessType(MindsSDK.ProcessType.enrollment)
      
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
      sdk = MindsSDK()
      sdk?.setToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzZWNyZXRfNjBfYXBpIiwiY29tcGFueV9pZCI6NjB9.rxePCNyDUZWELHZj49s_oki8StezhADQVeId39NwMV4")
      sdk?.setExternalId(nil)
      sdk?.setExternalCustomerId(nil)
      sdk?.setPhoneNumber(phone)
      sdk?.setShowDetails(true)
      sdk?.setCpf(cpf)
      sdk?.setProcessType(MindsSDK.ProcessType.authentication)
      
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


