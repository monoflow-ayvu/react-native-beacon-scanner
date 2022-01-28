package com.fernandomumbach.reactnativebeaconscanner

import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import com.facebook.react.bridge.*

import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import java.util.*
import com.minew.beaconplus.sdk.MTCentralManager
import com.minew.beaconplus.sdk.MTPeripheral
import com.minew.beaconplus.sdk.interfaces.MTCentralManagerListener
import com.minew.beaconplus.sdk.frames.MinewFrame

import java.util.ArrayList

import com.minew.beaconplus.sdk.MTFrameHandler
import com.minew.beaconplus.sdk.enums.FrameType
import com.minew.beaconplus.sdk.frames.AccFrame
import com.minew.beaconplus.sdk.frames.DeviceInfoFrame
import com.minew.beaconplus.sdk.frames.IBeaconFrame


class RNBeaconScannerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext),
    MTCentralManagerListener {
    private val LOG_TAG = "RNBeaconScannerModule"

    private var mtCentralManager: MTCentralManager? = null
    private var mApplicationContext: Context? = null
    private var mReactContext: ReactApplicationContext? = null

    override fun getName() = LOG_TAG

    init {
        mReactContext = reactContext
    }

    override fun initialize() {
        mApplicationContext = mReactContext!!.applicationContext
        mtCentralManager = MTCentralManager.getInstance(mApplicationContext!!)

        mtCentralManager?.setMTCentralManagerListener(this)
    }

    /***** CALLBACKS ******/

    override fun onScanedPeripheral(peripherals: List<MTPeripheral>) {
        val arr = Arguments.createArray()

        for (mtPeripheral in peripherals) {
            val bMap = Arguments.createMap()
            val mtFrameHandler = mtPeripheral.mMTFrameHandler

            Log.w(LOG_TAG, "found mac: " + mtFrameHandler.mac)

            bMap.putString("mac", mtFrameHandler.mac)
            bMap.putString("name", mtFrameHandler.name)
            bMap.putInt("battery", mtFrameHandler.battery)
            bMap.putInt("rssi", mtFrameHandler.rssi)
            bMap.putDouble("lastUpdate", mtFrameHandler.lastUpdate.toDouble())

            val dataFrames = Arguments.createArray()
            val advFrames = mtFrameHandler.advFrames

            advFrames.forEach {
                val frameType = it.frameType
                when (frameType) {
                    FrameType.FrameiBeacon -> {
                        it as IBeaconFrame
                        val iBeaconMap = Arguments.createMap()
                        iBeaconMap.putString("type", "ibeacon")
                        iBeaconMap.putString("uuid", it.uuid)
                        iBeaconMap.putInt("major", it.major)
                        iBeaconMap.putInt("minor", it.minor)
                        iBeaconMap.putInt("tx", it.txPower)
                        dataFrames.pushMap(iBeaconMap)
                    }

                    FrameType.FrameAccSensor -> {
                        it as AccFrame
                        val accMap = Arguments.createMap()
                        accMap.putString("type", "accelerometer")
                        accMap.putDouble("x", it.xAxis)
                        accMap.putDouble("y", it.yAxis)
                        accMap.putDouble("z", it.zAxis)
                        dataFrames.pushMap(accMap)
                    }
                }
            }

            bMap.putArray("frames", dataFrames)

            arr.pushMap(bMap)
        }

        mReactContext?.let { sendEventArray(it, "beacons", arr) }
    }

//    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
//        Log.w(LOG_TAG, "Beacons " + beacons.toString())
//        val arr = Arguments.createArray()
//
//        beacons?.iterator()?.forEach {
//            val bMap = Arguments.createMap()
//            bMap.putString("address", it.bluetoothAddress)
//            bMap.putString("name", it.bluetoothName)
//            bMap.putDouble("distance", it.distance)
//            bMap.putInt("rssi", it.rssi)
//            bMap.putDouble("avgRSSI", it.runningAverageRssi)
//            bMap.putInt("manufacturer", it.manufacturer)
//            bMap.putInt("scanCount", it.packetCount)
//            bMap.putString("uuid", it.id1.toUuid().toString())
//            bMap.putInt("major", it.id2.toInt())
//            bMap.putInt("minor", it.id3.toInt())
//
//            val dataArr = Arguments.createArray()
//            it.dataFields.iterator().forEach {
//                dataArr.pushDouble(it.toDouble())
//            }
//            bMap.putArray("data", dataArr)
//
//            val extraArr = Arguments.createArray()
//            it.extraDataFields.iterator().forEach {
//                extraArr.pushDouble(it.toDouble())
//            }
//            bMap.putArray("extra", extraArr)
//
//            bMap.putString("16bitUUID", UUID.nameUUIDFromBytes(it.serviceUuid128Bit).toString())
//
//            arr.pushMap(bMap)
//        }
//
//        mReactContext?.let { sendEventArray(it, "beacons", arr) }
//    }

    /***** END CALLBACKS ******/

    /***** UTILS ******/

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
        mtCentralManager?.startService()
        mtCentralManager?.startScan()
    }

    @ReactMethod
    fun stop() {
        mtCentralManager?.stopService()
        mtCentralManager?.stopScan()
    }

    /***** END REACT METHODS ******/
}

