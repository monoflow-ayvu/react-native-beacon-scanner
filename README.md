# react-native-beacon-scanner

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