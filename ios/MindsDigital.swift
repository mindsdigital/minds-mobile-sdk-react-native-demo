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
  
  var sdk = MindsSDK.shared
  var rnCallback: RCTResponseSenderBlock?
  var navigationController: UINavigationController?

  @objc func enrollment(_ uiNavigationController: UINavigationController, cpf: String, phone: String, callback: @escaping RCTResponseSenderBlock) {
    rnCallback = callback;
    navigationController = uiNavigationController

    DispatchQueue.main.async { [self] in
      self.sdk.token = ""
      self.sdk.setExternalId("4")
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
      self.sdk.setExternalId("4")
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

