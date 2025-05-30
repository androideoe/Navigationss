package com.ddup.navigation.data.model

/**
 * 导航摘要
 */
data class NavigationSummary(
    val distance: Float,  // 导航总距离，单位：米
    val duration: Int,    // 导航总时间，单位：秒
    val startPoint: Location,  // 起点
    val endPoint: Location     // 终点
)