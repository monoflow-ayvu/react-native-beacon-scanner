import {
  DeviceEventEmitter,
  EmitterSubscription,
  NativeModules,
} from 'react-native'

interface FrameiBeacon {
  type: 'ibeacon'
  uuid: string
  major: number
  minor: number
  tx: number
}

interface FrameUID {
  type: 'uid'
  instance: string
  namespace: string
}

interface FrameAccSensor {
  type: 'accelerometer'
  x: number
  y: number
  z: number
}

interface FrameHTSensor {
  type: 'ht'
  temperature: number
  humidity: number
}

interface FrameTLM {
  type: 'tlm'
  temperature: number
  batteryVol: number
  secCount: number
  advCount: number
}

interface FrameURL {
  type: 'url'
  tx: number
  url: string
}

interface FrameLightSensor {
  type: 'light'
  battery: number
  lux: number
}

interface FrameForceSensor {
  type: 'force'
  battery: number
  force: number
}

interface FramePIRSensor {
  type: 'pir'
  battery: number
  value: number
}

interface FrameTempSensor {
  type: 'temperature'
  battery: number
  value: number
}

interface FrameTVOCSensor {
  type: 'tvoc'
  battery: number
  value: number
}

interface FrameLineBeacon {
  type: 'line'
  hwid: string
  tx: number
  authentication: string
  timestamp: number
}

interface FrameDeviceInfo {
  type: 'info'
  mac: string
  name: string
  battery: number
}

export type Frame =
  | FrameiBeacon
  | FrameUID
  | FrameAccSensor
  | FrameHTSensor
  | FrameTLM
  | FrameURL
  | FrameLightSensor
  | FrameForceSensor
  | FramePIRSensor
  | FrameTempSensor
  | FrameTVOCSensor
  | FrameLineBeacon
  | FrameDeviceInfo

export interface BeaconData {
  mac: string
  name: string
  battery: number
  rssi: number
  lastUpdate: number
  frames: Frame[]
}

export function onBeaconScan(
  cb: (beacons: BeaconData[]) => void
): EmitterSubscription {
  return DeviceEventEmitter.addListener('beacons', cb)
}

export function start(): Promise<void> {
  return NativeModules.RNBeaconScannerModule.start()
}

export function stop(): Promise<void> {
  return NativeModules.RNBeaconScannerModule.stop()
}

export function setBluetoothState(enable: boolean = true): Promise<boolean> {
  return NativeModules.RNBeaconScannerModule.setBluetoothState(enable)
}
