//
//  RCTMindsDigitalModule.m
//  ReactNativeDemo
//
//  Created by Divino Borges on 26/12/22.
//

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

