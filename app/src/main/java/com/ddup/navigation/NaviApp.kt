package com.ddup.navigation

import android.app.Application
import com.amap.api.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NaviApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 设置隐私合规
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
    }
}