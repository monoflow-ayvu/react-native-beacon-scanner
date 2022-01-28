package com.fernandomumbach.reactnativebeaconscanner

import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import com.facebook.react.bridge.*
import org.altbeacon.beacon.*

import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter


class RNBeaconScannerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext),
    RangeNotifier {
    private val LOG_TAG = "RNBeaconScannerModule"

    private var mBeaconManager: BeaconManager? = null
    private var mApplicationContext: Context? = null
    private var mReactContext: ReactApplicationContext? = null
    private var mGlobalRegion: Region

//    private val IBEACON_LAYOUT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"
    private val IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
    private val ALTBEACON_LAYOUT = BeaconParser.ALTBEACON_LAYOUT

    override fun getName() = LOG_TAG

    init {
        mReactContext = reactContext
        mGlobalRegion = Region("all-beacons-region", null, null, null)
    }

    override fun initialize() {
        mApplicationContext = mReactContext!!.applicationContext
        mBeaconManager = this.beaconManager(mApplicationContext!!)

        mBeaconManager!!.addRangeNotifier(this)
    }

    /***** CALLBACKS ******/

    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        Log.w(LOG_TAG, "Beacons " + beacons.toString())
        val map = Arguments.createMap()
        val arr = Arguments.createArray()

        beacons?.iterator()?.forEach {
            val bMap = Arguments.createMap()
            bMap.putString("address", it.bluetoothAddress)
            bMap.putString("name", it.bluetoothName)
            bMap.putDouble("distance", it.distance)
            bMap.putInt("rssi", it.rssi)
            bMap.putDouble("avgRSSI", it.runningAverageRssi)
            bMap.putInt("manufacturer", it.manufacturer)
//            bMap.putArray("data", Arguments.fromArray(it.dataFields))
//            bMap.putArray("extra", Arguments.fromArray(it.extraDataFields))

            arr.pushMap(bMap)
        }
        map.putArray("beacons", arr)
        mReactContext?.let { sendEvent(it, "beacons", map) }
    }

    /***** END CALLBACKS ******/

    /***** UTILS ******/

    private fun beaconManager(ctx: Context): BeaconManager {
        val instance = BeaconManager.getInstanceForApplication(ctx)

        // Add all the beacon types we want to discover
        instance.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_LAYOUT))
        instance.beaconParsers.add(BeaconParser().setBeaconLayout(ALTBEACON_LAYOUT))

        return instance
    }

    private fun sendEvent(
        reactContext: ReactContext,
        eventName: String,
        params: WritableMap?
    ) {
        if (reactContext.hasActiveCatalystInstance()) {
            reactContext
                .getJSModule(RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        }
    }

    /***** END UTILS ******/

    /***** REACT METHODS ******/

    @ReactMethod
    fun start() {
        mBeaconManager?.startRangingBeacons(mGlobalRegion)
    }

    @ReactMethod
    fun stop() {
        mBeaconManager?.stopRangingBeacons(mGlobalRegion)
    }

    /***** END REACT METHODS ******/
}

