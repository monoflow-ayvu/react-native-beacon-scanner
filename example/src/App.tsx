import React, { useEffect } from 'react'
import { PermissionsAndroid } from 'react-native'
import RNBeaconScannerModule, { Counter } from 'react-native-beacon-scanner'

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
    console.log(RNBeaconScannerModule)

    requestLocationPermission()
      .then((granted) => {
        console.info('granted', granted)
        return RNBeaconScannerModule.start()
      })
      .then(() => console.info('scan started'))
      .catch((err) => console.error('error', err))

    return () =>
      RNBeaconScannerModule.stop().catch((err: Error) =>
        console.error('error stopping', err)
      )
  })

  return <Counter />
}

export default App
