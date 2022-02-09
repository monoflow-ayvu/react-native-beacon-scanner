package com.fernandomumbach.reactnativebeaconscanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    MTCentralManagerListener, LifecycleEventListener {
    private val LOG_TAG = "RNBeaconScannerModule"

    private var mtCentralManager: MTCentralManager? = null
    private var mApplicationContext: Context? = null
    private var mReactContext: ReactApplicationContext? = null
    private var mPendingStatePromise: Promise? = null
    private var mPendingState: Boolean? = null

    override fun getName() = LOG_TAG

    init {
        mReactContext = reactContext
    }

    override fun initialize() {
        mApplicationContext = mReactContext!!.applicationContext
        mReactContext!!.addLifecycleEventListener(this);
        mtCentralManager = MTCentralManager.getInstance(mApplicationContext!!)
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

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )

                mPendingState?.let {
                    val newState = state == BluetoothAdapter.STATE_ON
                    if (newState == it) {
                        mPendingStatePromise?.resolve(true)
                        mPendingState = null
                        mPendingStatePromise = null
                    }
                }
            }
        }
    }

    override fun onHostResume() {
        try {
            start()
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.toString())
        }
    }

    override fun onHostPause() {
        try {
            stop()
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.toString())
        }
    }

    override fun onHostDestroy() {
        try {
            stop()
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.toString())
        }
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

    private fun start() {
        Log.i(LOG_TAG, "Starting RNBeaconScannerModule")

        mtCentralManager!!.clear()
        mtCentralManager!!.setMTCentralManagerListener(this)
        
        mtCentralManager!!.startScan()

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        mReactContext!!.registerReceiver(mReceiver, filter)
    }

    private fun stop() {
        Log.i(LOG_TAG, "Stopping RNBeaconScannerModule")
//        mtCentralManager?.stopScan()
        mtCentralManager?.stopService()
        mtCentralManager?.setMTCentralManagerListener(null)
        mReactContext?.unregisterReceiver(mReceiver)
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

        // the service might be already running, try to shut it down first
        try {
            stop()
        } catch (e: Exception) {
            // no-op
        }

        try {
            start()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("Error starting scan", e)
        }
    }

    @ReactMethod
    fun stop(promise: Promise) {
        try {
            stop()
            promise.resolve(null)
        } catch (e: Exception) {
            promise.reject("Error starting scan", e)
        }
    }

    @ReactMethod
    fun setBluetoothState(enable: Boolean, promise: Promise) {
        val bluetoothManager = mApplicationContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val mBluetoothAdapter = bluetoothManager.adapter

        if (ActivityCompat.checkSelfPermission(
                mApplicationContext!!,
                Manifest.permission.BLUETOOTH_ADMIN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            promise.reject("Permission not granted", "BLUETOOTH_ADMIN permission not granted")
            return
        }

        if (!mBluetoothAdapter.isEnabled && enable) {
            mBluetoothAdapter.enable()
        } else if (mBluetoothAdapter.isEnabled && !enable) {
            mBluetoothAdapter.disable()
        } else {
            // the requested condition is already met!
            promise.resolve(true)
            return
        }

        // the promise is answered on onStateChanged
        mPendingStatePromise = promise
        mPendingState = enable
    }

    /***** END REACT METHODS ******/
}

