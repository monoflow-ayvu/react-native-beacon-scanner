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