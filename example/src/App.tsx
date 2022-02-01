// eslint-disable-next-line @typescript-eslint/no-unused-vars
import React, { useEffect } from 'react'
import { PermissionsAndroid } from 'react-native'
import {
  onBeaconScan,
  setBluetoothState,
  start,
  stop,
} from 'react-native-beacon-scanner'

export async function requestLocationPermission() {
  try {
    const granted = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      {
        title: 'Example App',
        message: 'Example App access to your location ',
        buttonPositive: 'OK',
        buttonNegative: 'Cancel',
        buttonNeutral: 'Ask Me Later',
      }
    )
    if (granted === PermissionsAndroid.RESULTS.GRANTED) {
      return true
    } else {
      return false
    }
  } catch (err) {
    console.warn(err)
    return false
  }
}

const App = () => {
  useEffect(() => {
    const scanner = onBeaconScan((beacons) => {
      console.info('Beacons found: ', beacons.length)
      console.dir(beacons)
    })

    requestLocationPermission()
      .then((granted) => {
        if (granted) {
          return setBluetoothState(true)
        } else {
          throw new Error('bluetooth permission not granted')
        }
      })
      .then(() => start())
      .then(() => console.info('scan started'))
      .catch((err) => console.error('error', err))

    return () => {
      scanner.remove()
      stop().catch((err: Error) => console.error('error stopping', err))
    }
  })

  return null
}

export default App
