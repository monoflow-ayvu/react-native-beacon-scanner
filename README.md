# react-native-beacon-scanner

## NOTE

This scanner only scans for Minew beacons. Please keep that in mind.

All frames provided by [Minew's documentation](https://docs.beaconyun.com/Android/Android_BeaconPlus_Software_Development_Kit_Guide.html#start-developing) are implemented.

## How to Install (Android)

* Add the following to AndroidManifest.xml:

```xml
  <service android:name="com.minew.beaconplus.sdk.ConnectService"/>
  <service android:name="com.minew.beaconplus.sdk.services.DfuService"/>
  <receiver android:name="com.minew.beaconplus.sdk.receivers.BluetoothChangedReceiver"
      android:exported="true">
      <intent-filter>
          <action android:name="android.bluetooth.adapter.action.STATE_CHANGED"/>
      </intent-filter>
  </receiver>
```

## How to use

1. First you'll need to ensure the permission for bluetooth and fine location are given.

```typescript
import { PermissionsAndroid } from 'react-native'

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
```

2. Then subscribe to beacon events from the lib

```typescript
import {setBluetoothState} from 'react-native-beacon-scanner'

const scanner = onBeaconScan((beacons) => {
  console.info('Beacons found: ', beacons.length)
  console.info(JSON.stringify(beacons, undefined, 2))
})

// Do not forget to clear subscription when not needed anymore
scanner.remove()
```

3. Finally, make sure bluetooth is ON and request the library to start scanning:

```typescript
import {
  setBluetoothState,
  start,
  stop,
} from 'react-native-beacon-scanner'

requestLocationPermission()
  .then(async (granted) => {
    if (granted) {
      console.log('ensuring bluetooth is on...')
      await setBluetoothState(true)
    } else {
      throw new Error('bluetooth permission not granted')
    }
  })
  // scan starts here
  .then(() => start())
  // here on we already have started scanning
  .then(() => console.info('scan started!'))
  // oops! Something happened
  .catch((err) => console.error('error', err))
```


See the _example_ folder for a working demo.