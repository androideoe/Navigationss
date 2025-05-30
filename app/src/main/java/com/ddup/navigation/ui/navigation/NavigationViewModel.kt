package com.ddup.navigation.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddup.navigation.data.model.GpsSignalState
import com.ddup.navigation.data.model.Location
import com.ddup.navigation.data.model.NavigationState
import com.ddup.navigation.data.repository.NavigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val navigationRepository: NavigationRepository,
) : ViewModel() {

    // 导航状态
    val navigationState: StateFlow<NavigationState> = navigationRepository
        .getNavigationState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NavigationState.Navigating
        )

    // GPS信号状态
    val gpsSignalState: StateFlow<GpsSignalState> = navigationRepository
        .getGpsSignalState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GpsSignalState.NORMAL
        )

    /**
     * 停止导航
     */
    fun stopNavigation() {
        viewModelScope.launch {
            navigationRepository.stopNavigation()
        }
    }

    /**
     * 完成导航（带路线摘要）
     */
    fun completeNavigation(
        distance: Float,
        duration: Int,
        startPoint: Location,
        endPoint: Location
    ) {
        viewModelScope.launch {
            navigationRepository.completeNavigation(
                distance = distance,
                duration = duration,
                startPoint = startPoint,
                endPoint = endPoint
            )
        }
    }

    override fun onCleared() {
        navigationRepository.stopMonitoring()
    }
}