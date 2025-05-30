package com.ddup.navigation.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.amap.api.navi.AMapNavi
import com.ddup.navigation.data.model.GpsSignalState
import com.ddup.navigation.data.model.Location
import com.ddup.navigation.data.model.NavigationState
import com.ddup.navigation.data.model.NavigationSummary
import com.ddup.navigation.utils.GpsSignalMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 导航仓库
 */
@Singleton
class NavigationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "NavigationRepository"
    private val gpsSignalMonitor = GpsSignalMonitor(context)
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Navigating)
    private var aMapNavi: AMapNavi? = null

    init {
        try {
            // 初始化高德导航服务
            aMapNavi = AMapNavi.getInstance(context)
            Log.d(TAG, "AMapNavi initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AMapNavi", e)
        }
    }

    /**
     * 获取当前导航状态
     */
    fun getNavigationState(): Flow<NavigationState> = _navigationState.asStateFlow()

    /**
     * 获取GPS信号状态流
     */
    fun getGpsSignalState(): Flow<GpsSignalState> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gpsSignalMonitor.monitorGpsSignal()
        } else {
            // 对于低版本系统，返回一个固定状态
            MutableStateFlow(GpsSignalState.NORMAL).asStateFlow()
        }
    }

    fun stopMonitoring() {
        return gpsSignalMonitor.stopMonitoring()
    }

    /**
     * 停止导航并生成导航摘要
     */
    suspend fun stopNavigation() {
        Log.d(TAG, "Stopping navigation")
        try {
            // 获取导航实例
            val navi = aMapNavi ?: run {
                Log.e(TAG, "AMapNavi is null, cannot stop navigation")
                return
            }

            // 获取导航路径信息
            val naviPath = navi.naviPath
            if (naviPath == null) {
                Log.e(TAG, "Navigation path is null, cannot generate summary")
                return
            }

            // 从导航服务获取实际数据
            val distance = naviPath.allLength.toFloat()
            val duration = naviPath.allTime

            // 获取起点和终点
            val startPoint = naviPath.startPoint
            val endPoint = naviPath.endPoint

            if (startPoint == null || endPoint == null) {
                Log.e(TAG, "Start point or end point is null, cannot generate summary")
                return
            }

            // 创建导航摘要
            val summary = NavigationSummary(
                distance = distance,
                duration = duration,
                startPoint = Location(startPoint.latitude, startPoint.longitude),
                endPoint = Location(endPoint.latitude, endPoint.longitude)
            )

            Log.d(TAG, "Navigation summary generated: distance=$distance, duration=$duration")

            // 更新状态为已完成
            _navigationState.update { NavigationState.Completed(summary) }

            // 停止导航
            navi.stopNavi()
            Log.d(TAG, "Navigation stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error while stopping navigation", e)
        }
    }

    /**
     * 完成导航并生成导航摘要
     */
    suspend fun completeNavigation(
        distance: Float,
        duration: Int,
        startPoint: Location,
        endPoint: Location
    ) {
        val summary = NavigationSummary(
            distance = distance,
            duration = duration,
            startPoint = startPoint,
            endPoint = endPoint
        )

        _navigationState.update { NavigationState.Completed(summary) }
    }
}