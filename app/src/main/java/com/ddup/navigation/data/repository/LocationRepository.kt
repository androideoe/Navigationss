package com.ddup.navigation.data.repository

import android.content.Context
import android.util.Log
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.ddup.navigation.data.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "LocationRepository"

    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "Getting current location")
        var client: AMapLocationClient? = null
        try {
            client = AMapLocationClient(context)
            val locationOption = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isOnceLocation = true
                Log.d(TAG, "Current location option configured: mode=Hight_Accuracy, onceLocation=true")
            }

            val locationListener = AMapLocationListener { location ->
                if (location != null) {
                    when (location.errorCode) {
                        0 -> {
                            Log.d(TAG, "Current location received: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}m")
                            continuation.resume(
                                Location(
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            )
                        }
                        else -> {
                            Log.e(TAG, "Failed to get current location: errorCode=${location.errorCode}, errorInfo=${location.errorInfo}")
                            continuation.resume(null)
                        }
                    }
                } else {
                    Log.e(TAG, "Current location received null location")
                    continuation.resume(null)
                }
                client?.apply {
                    stopLocation()
                    onDestroy()
                }
                client = null
            }

            client?.setLocationOption(locationOption)
            client?.setLocationListener(locationListener)
            client?.startLocation()
            Log.d(TAG, "Current location request started")
        } catch (e: Exception) {
            Log.e(TAG, "Error while getting current location", e)
            client?.apply {
                stopLocation()
                onDestroy()
            }
            client = null
            continuation.resume(null)
        }
    }

}