package com.ddup.navigation.data.model

sealed class GpsSignalState {
    data object STRONG : GpsSignalState()
    data object NORMAL : GpsSignalState()
    data object WEAK : GpsSignalState()
    data object LOST : GpsSignalState()
}