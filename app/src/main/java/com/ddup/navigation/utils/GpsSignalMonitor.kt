package com.ddup.navigation.utils

import android.content.Context
import android.location.GpsStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.ddup.navigation.data.model.GpsSignalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GPS信号监控器
 * 用于监控GPS信号状态并发出通知
 */
class GpsSignalMonitor(private val context: Context) {
    private val TAG = "GpsSignalMonitor"
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _gpsSignalState = MutableStateFlow<GpsSignalState>(GpsSignalState.NORMAL)
    val gpsSignalState: StateFlow<GpsSignalState> = _gpsSignalState.asStateFlow()

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // 根据GPS精度判断信号强度
            val accuracy = location.accuracy
            val newState = when {
                accuracy <= 10 -> GpsSignalState.STRONG
                accuracy <= 30 -> GpsSignalState.NORMAL
                accuracy <= 100 -> GpsSignalState.WEAK
                else -> GpsSignalState.LOST
            }

            if (_gpsSignalState.value != newState) {
                Log.d(
                    TAG,
                    "GPS signal state changed: ${_gpsSignalState.value} -> $newState (accuracy: $accuracy)"
                )
                _gpsSignalState.value = newState
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // 根据GPS状态判断信号强度
            val newState = when (status) {
                GpsStatus.GPS_EVENT_STARTED -> GpsSignalState.NORMAL
                GpsStatus.GPS_EVENT_STOPPED -> GpsSignalState.LOST
                GpsStatus.GPS_EVENT_FIRST_FIX -> GpsSignalState.STRONG
                GpsStatus.GPS_EVENT_SATELLITE_STATUS -> {
                    // 获取卫星数量
                    val satellites = extras?.getInt("satellites", 0) ?: 0
                    when {
                        satellites >= 8 -> GpsSignalState.STRONG
                        satellites >= 4 -> GpsSignalState.NORMAL
                        satellites >= 2 -> GpsSignalState.WEAK
                        else -> GpsSignalState.LOST
                    }
                }

                else -> GpsSignalState.NORMAL
            }

            if (_gpsSignalState.value != newState) {
                Log.d(
                    TAG,
                    "GPS status changed: ${_gpsSignalState.value} -> $newState (status: $status)"
                )
                _gpsSignalState.value = newState
            }
        }

        override fun onProviderEnabled(provider: String) {
            Log.d(TAG, "GPS provider enabled: $provider")
            _gpsSignalState.value = GpsSignalState.NORMAL
        }

        override fun onProviderDisabled(provider: String) {
            Log.d(TAG, "GPS provider disabled: $provider")
            _gpsSignalState.value = GpsSignalState.LOST
        }
    }

    /**
     * 开始监控GPS信号
     */
    fun startMonitoring() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // 最小时间间隔，毫秒
                    1f,   // 最小距离变化，米
                    locationListener
                )
                Log.d(TAG, "GPS monitoring started")
            } else {
                Log.w(TAG, "GPS provider is disabled")
                _gpsSignalState.value = GpsSignalState.LOST
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to start GPS monitoring: ${e.message}")
            _gpsSignalState.value = GpsSignalState.LOST
        }
    }

    /**
     * 停止监控GPS信号
     */
    fun stopMonitoring() {
        try {
            locationManager.removeUpdates(locationListener)
            Log.d(TAG, "GPS monitoring stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop GPS monitoring: ${e.message}")
        }
    }

    /**
     * 监控GPS信号状态
     * 返回一个StateFlow，可以收集GPS信号状态的变化
     */
    fun monitorGpsSignal(): StateFlow<GpsSignalState> {
        startMonitoring()
        return gpsSignalState
    }
}