package com.ddup.navigation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddup.navigation.data.model.Location
import com.ddup.navigation.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    init {
        // 启动定位
        viewModelScope.launch {
            try {
                // 获取初始位置
                val initialLocation = locationRepository.getCurrentLocation()
                Log.d("HomeViewModel", "initialLocation $initialLocation")
                initialLocation?.let { location ->
                    _state.update {
                        it.copy(
                            initialLocation = location
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error getting location updates", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun retryInitialLocation() {
        viewModelScope.launch {
            try {
                Log.e("HomeViewModel", "retryInitialLocation...")
                val location = locationRepository.getCurrentLocation()
                Log.e("HomeViewModel", "retryInitialLocation = $location")
                location?.let { newLocation ->
                    _state.update { it.copy(
                        initialLocation = newLocation
                    ) }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error getting initial location after permission granted", e)
            }
        }
    }

    // 设置导航目的地
    fun setDestination(latitude: Double, longitude: Double) {
        val destination = Location(latitude, longitude)
        _state.update {
            it.copy(
                destination = destination,
                showNavigateButton = true
            )
        }
    }

    // 清除导航目的地
    fun clearDestination() {
        _state.update {
            it.copy(
                destination = null,
                showNavigateButton = false
            )
        }
    }

    // 开始导航
    fun startNavigation() {
        _state.update {
            it.copy(
                navigateToNavigationScreen = true
            )
        }
    }

    // 导航页面显示后重置状态
    fun onNavigationScreenShown() {
        _state.update {
            it.copy(
                navigateToNavigationScreen = false
            )
        }
    }
}

data class MapState(
    val initialLocation: Location? = null,  // 初始定位位置
    val destination: Location? = null,      // 导航目的地
    val showNavigateButton: Boolean = false, // 是否显示导航按钮
    val navigateToNavigationScreen: Boolean = false // 是否导航到导航页面
)