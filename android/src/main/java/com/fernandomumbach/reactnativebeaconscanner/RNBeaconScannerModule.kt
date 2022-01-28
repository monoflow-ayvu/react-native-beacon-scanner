package com.fernandomumbach.reactnativebeaconscanner

import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import com.facebook.react.bridge.*
import org.altbeacon.beacon.*

import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import java.util.*


class RNBeaconScannerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext),
    RangeNotifier {
    private val LOG_TAG = "RNBeaconScannerModule"

    private var mBeaconManager: BeaconManager? = null
    private var mApplicationContext: Context? = null
    private var mReactContext: ReactApplicationContext? = null
    private var mGlobalRegion: Region

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
        val arr = Arguments.createArray()

        beacons?.iterator()?.forEach {
            val bMap = Arguments.createMap()
            bMap.putString("address", it.bluetoothAddress)
            bMap.putString("name", it.bluetoothName)
            bMap.putDouble("distance", it.distance)
            bMap.putInt("rssi", it.rssi)
            bMap.putDouble("avgRSSI", it.runningAverageRssi)
            bMap.putInt("manufacturer", it.manufacturer)
            bMap.putInt("scanCount", it.packetCount)
            bMap.putString("uuid", it.id1.toUuid().toString())
            bMap.putInt("major", it.id2.toInt())
            bMap.putInt("minor", it.id3.toInt())

            val dataArr = Arguments.createArray()
            it.dataFields.iterator().forEach {
                dataArr.pushDouble(it.toDouble())
            }
            bMap.putArray("data", dataArr)

            val extraArr = Arguments.createArray()
            it.extraDataFields.iterator().forEach {
                extraArr.pushDouble(it.toDouble())
            }
            bMap.putArray("extra", extraArr)

            bMap.putString("16bitUUID", UUID.nameUUIDFromBytes(it.serviceUuid128Bit).toString())

            arr.pushMap(bMap)
        }

        mReactContext?.let { sendEventArray(it, "beacons", arr) }
    }

    /***** END CALLBACKS ******/

    /***** UTILS ******/

    private fun beaconManager(ctx: Context): BeaconManager {
        val instance = BeaconManager.getInstanceForApplication(ctx)

        // Add all the beacon types we want to discover
//        instance.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_LAYOUT))
//        instance.beaconParsers.add(BeaconParser().setBeaconLayout(ALTBEACON_LAYOUT))
        instance.beaconParsers.add(BeaconParser().setBeaconLayout("m:0-1=a108,x"))

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

    private fun sendEventArray(
        reactContext: ReactContext,
        eventName: String,
        params: WritableArray?
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

