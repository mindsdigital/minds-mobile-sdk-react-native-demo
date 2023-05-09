//
//  MindsDigital.swift
//  ReactNativeDemo
//
//  Created by Divino Borges on 02/01/23.
//

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
