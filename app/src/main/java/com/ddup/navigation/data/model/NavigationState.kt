package com.ddup.navigation.data.model

/**
 * 导航状态
 */
sealed class NavigationState {
    data object Navigating : NavigationState()
    data class Completed(val summary: NavigationSummary) : NavigationState()
}