package com.fernandomumbach.reactnativebeaconscanner

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.minew.beaconplus.sdk.MTCentralManager
import com.minew.beaconplus.sdk.MTPeripheral
import com.minew.beaconplus.sdk.enums.BluetoothState
import com.minew.beaconplus.sdk.enums.FrameType
import com.minew.beaconplus.sdk.frames.*
import com.minew.beaconplus.sdk.interfaces.MTCentralManagerListener


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
                when (it.frameType) {
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

                    FrameType.FrameUID -> {
                        it as UidFrame
                        val map = Arguments.createMap()
                        map.putString("type", "uid")
                        map.putString("instance", it.instanceId)
                        map.putString("namespace", it.namespaceId)
                        dataFrames.pushMap(map)
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

                    FrameType.FrameHTSensor -> {
                        it as HTFrame
                        val map = Arguments.createMap()
                        map.putString("type", "ht")
                        map.putDouble("temperature", it.temperature)
                        map.putDouble("humidity", it.humidity)
                        dataFrames.pushMap(map)
                    }

                    FrameType.FrameTLM -> {
                        it as TlmFrame
                        val map = Arguments.createMap()
                        map.putString("type", "tlm")
                        map.putDouble("temperature", it.temperature)
                        map.putInt("batteryVol", it.batteryVol)
                        map.putInt("secCount", it.secCount)
                        map.putInt("advCount", it.advCount)
                        dataFrames.pushMap(map)
                    }

                    FrameType.FrameURL -> {
                        it as UrlFrame
                        val map = Arguments.createMap()
                        map.putString("type", "url")
                        map.putInt("tx", it.txPower)
                        map.putString("url", it.urlString)
                        dataFrames.pushMap(map)
                    }

                    FrameType.FrameLightSensor -> {
                        it as LightFrame
                        val map = Arguments.createMap()
                        map.putString("type", "light")
                        map.putInt("battery", it.battery)
                        map.putInt("lux", it.luxValue)
                        dataFrames.pushMap(map)
                    }

                    FrameType.FrameForceSensor -> {
                        it as ForceFrame
                        val map = Arguments.createMap()
                        map.putString("type", "force")
                        map.putInt("battery", it.battery)
                        map.putInt("force", it.force)
                        dataFrames.pushMap(map)
                    }

                    FrameType.FramePIRSensor -> {
                        it as PIRFrame
                        val map = Arguments.createMap()
                        map.putString("type", "pir")
                        map.putInt("battery", it.battery)
                        map.putInt("pir", it.value)
                        dataFrames.pushMap(map)
                    }

                    FrameType.FrameTempSensor -> {
                        it as TemperatureFrame
                        val map = Arguments.createMap()
                        map.putString("type", "temperature")
                        map.putInt("battery", it.battery)
                        map.putDouble("temperature", it.value.toDouble())
                        dataFrames.pushMap(map)
                    }

                    FrameType.FrameTVOCSensor -> {
                        it as TvocFrame
                        val map = Arguments.createMap()
                        map.putString("type", "tvoc")
                        map.putInt("battery", it.battery)
                        map.putInt("tvoc", it.value)
                        dataFrames.pushMap(map)
                    }

                    FrameType.FrameLineBeacon -> {
                        it as LineBeaconFrame
                        val map = Arguments.createMap()
                        map.putString("type", "line")
                        map.putString("hwid", it.hwid)
                        map.putInt("tx", it.txPower)
                        map.putString("auth", it.authentication)
                        map.putInt("timestamp", it.timesTamp)
                        dataFrames.pushMap(map)
                    }

                    FrameType.FrameDeviceInfo -> {
                        it as DeviceInfoFrame
                        val map = Arguments.createMap()
                        map.putString("type", "info")
                        map.putString("mac", it.mac)
                        map.putString("name", it.name)
                        map.putInt("battery", it.battery)
                        dataFrames.pushMap(map)
                    }

                    else -> {}
                }
            }

            bMap.putArray("frames", dataFrames)

            arr.pushMap(bMap)
        }

        mReactContext?.let { sendEventArray(it, "beacons", arr) }
    }

    /***** END CALLBACKS ******/

    /***** UTILS ******/

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
    fun start(promise: Promise) {
        val state = mtCentralManager?.getBluetoothState(mApplicationContext)
        if (state != BluetoothState.BluetoothStatePowerOn) {
            promise.reject("Bluetooth not powered on", "Bluetooth is not powered on")
            return
        }

        try {
            mtCentralManager?.startService()
            mtCentralManager?.startScan()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("Error starting scan", e)
        }
    }

    @ReactMethod
    fun stop(promise: Promise) {
        try {
            mtCentralManager?.stopScan()
            mtCentralManager?.stopService()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("Error starting scan", e)
        }
    }

    @ReactMethod
    fun setBluetoothState(enable: Boolean, promise: Promise) {
        val bluetoothManager = mApplicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = bluetoothManager.getAdapter()
        if (mBluetoothAdapter.isEnabled != enable) {
            if (ActivityCompat.checkSelfPermission(
                    mApplicationContext!!,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                promise.reject("Permission not granted", "BLUETOOTH_ADMIN permission not granted")
                return
            }

            if (enable) {
                mBluetoothAdapter.enable()
            } else {
                mBluetoothAdapter.disable()
            }
        }

        promise.resolve(true)
    }

    /***** END REACT METHODS ******/
}

