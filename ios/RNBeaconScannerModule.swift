//
//  RNBeaconScannerModule.swift
//  RNBeaconScannerModule
//
//  Copyright © 2022 Fernando Mumbach. All rights reserved.
//

import Foundation

@objc(RNBeaconScannerModule)
class RNBeaconScannerModule: NSObject {
  @objc
  func constantsToExport() -> [AnyHashable : Any]! {
    return ["count": 1]
  }

  @objc
  static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
