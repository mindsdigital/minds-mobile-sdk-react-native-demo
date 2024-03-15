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
  
  func getSDKToken() -> String {
      if let path = Bundle.main.path(forResource: "AppConfig", ofType: "plist") {
          if let dictionary = NSDictionary(contentsOfFile: path) {
              if let sdkToken = dictionary["token"] as? String {
                  return sdkToken
              }
          }
      }
      return ""
  }
  
  @objc func enrollment(_ uiNavigationController: UINavigationController, document: String, phone: String, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolveCallback = resolver
    rejectCallback = reject
    navigationController = uiNavigationController
  
    startSDK(navigationController!, processType: .enrollment, document: document, token: getSDKToken(), telephone: phone, externalId: nil, externalCustomerId: nil)
  }
  
  @objc func authentication(_ uiNavigationController: UINavigationController, document: String, phone: String, resolver: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolveCallback = resolver
    rejectCallback = reject
    navigationController = uiNavigationController
    
    startSDK(navigationController!, processType: .authentication, document: document, token: getSDKToken(), telephone: phone, externalId: nil, externalCustomerId: nil)
  }
  
  private func startSDK(_ uiNavigationController: UINavigationController, processType: MindsSDK.ProcessType, document: String, token: String, telephone: String, externalId: String?, externalCustomerId: String?) {
    sdk = MindsSDK(delegate: self)
    sdk?.setToken(token)
    sdk?.setExternalId(externalId)
    sdk?.setExternalCustomerId(externalCustomerId)
    sdk?.setPhoneNumber(telephone)
    sdk?.setShowDetails(true)
    sdk?.setDocument(document)
    sdk?.setProcessType(processType)
    sdk?.setEnvironment(.staging)
    
    DispatchQueue.main.async {
            
    
      self.sdk?.initialize(on: uiNavigationController) { error in
        if let error = error {
          do {
            throw error
          } catch DomainError.invalidDocument(let message) {
            self.rejectCallback?(message!, "invalid_document", nil)
            
          } catch DomainError.invalidCPF(let message) {
            self.rejectCallback?(message!, "invalid_cpf", nil)
      
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
  
  private func biometricsReceive(_ response: BiometricResponse?) {
    let json: [String: Any?] = [
      "success": response?.success as Any,
      "error": [
          "code": response?.error?.code as Any,
          "description": response?.error?.description as Any
      ],
      "id": response?.id as Any,
      "cpf": response?.cpf as Any,
      "external_id": response?.externalID as Any,
      "created_at": response?.createdAt as Any,
      "utc_created_at": response?.utcCreatedAt as Any,
      "result": [
          "recommended_action": response?.result?.recommendedAction as Any,
          "reasons": response?.result?.reasons as Any
      ],
      "details": [
          "flag": [
              "type": response?.details?.flag?.type as Any,
              "status": response?.details?.flag?.status as Any
          ],
          "liveness": [
              "status": response?.details?.liveness?.status as Any,
              "replay_attack": [
                  "enabled": response?.details?.liveness?.replayAttack?.enabled as Any,
                  "status": response?.details?.liveness?.replayAttack?.status as Any,
                  "result": response?.details?.liveness?.replayAttack?.result as Any,
                  "confidence": response?.details?.liveness?.replayAttack?.confidence as Any,
                  "score": response?.details?.liveness?.replayAttack?.score as Any,
                  "threshold": response?.details?.liveness?.replayAttack?.threshold as Any
              ],
              "deepfake": [
                  "enabled": response?.details?.liveness?.deepFake?.enabled as Any,
                  "status": response?.details?.liveness?.deepFake?.status as Any,
                  "result": response?.details?.liveness?.deepFake?.result as Any,
                  "confidence": response?.details?.liveness?.deepFake?.confidence as Any,
                  "score": response?.details?.liveness?.deepFake?.score as Any,
                  "threshold": response?.details?.liveness?.deepFake?.threshold as Any
              ],
              "sentence_match": [
                  "enabled": response?.details?.liveness?.sentenceMatch?.enabled as Any,
                  "status": response?.details?.liveness?.sentenceMatch?.status as Any,
                  "result": response?.details?.liveness?.sentenceMatch?.result as Any,
                  "confidence": response?.details?.liveness?.sentenceMatch?.confidence as Any,
                  "score": response?.details?.liveness?.sentenceMatch?.score as Any,
                  "threshold": response?.details?.liveness?.sentenceMatch?.threshold as Any
              ]
          ],
          "voice_match": [
              "status": response?.details?.voiceMatch?.status as Any,
              "result": response?.details?.voiceMatch?.result as Any,
              "confidence": response?.details?.voiceMatch?.confidence as Any,
              "score": response?.details?.voiceMatch?.score as Any,
              "threshold": response?.details?.voiceMatch?.threshold as Any
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
  
  func onSuccess(_ response: BiometricResponse?) {
    self.biometricsReceive(response)
  }
  
  func onError(_ response: BiometricResponse?) {
    self.biometricsReceive(response)
  }
}
