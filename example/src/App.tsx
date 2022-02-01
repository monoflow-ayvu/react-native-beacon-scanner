import React, { useEffect } from 'react'
import { PermissionsAndroid, Text } from 'react-native'
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
      console.info(JSON.stringify(beacons, undefined, 2))
    })

    requestLocationPermission()
      .then(async (granted) => {
        if (granted) {
          console.log('turning bluetooth on...')
          await setBluetoothState(true)
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

  return <Text>Started! Please check react-native logs.</Text>
}

export default App
