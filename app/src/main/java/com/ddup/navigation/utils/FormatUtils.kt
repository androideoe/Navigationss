package com.ddup.navigation.utils

/**
 * 格式化距离
 * @param distance 距离（米）
 * @return 格式化后的距离字符串
 */
fun formatDistance(distance: Float): String {
    return when {
        distance < 1000 -> "${distance.toInt()}米"
        else -> String.format("%.1f公里", distance / 1000)
    }
}

/**
 * 格式化时间
 * @param seconds 秒数
 * @return 格式化后的时间字符串
 */
fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return when {
        hours > 0 -> String.format("%d小时%d分%d秒", hours, minutes, remainingSeconds)
        minutes > 0 -> String.format("%d分%d秒", minutes, remainingSeconds)
        else -> String.format("%d秒", remainingSeconds)
    }
}