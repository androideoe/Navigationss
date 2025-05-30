package com.ddup.navigation.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.AMapNaviListener
import com.amap.api.navi.enums.NaviType
import com.amap.api.navi.model.AMapCalcRouteResult
import com.amap.api.navi.model.AMapLaneInfo
import com.amap.api.navi.model.AMapModelCross
import com.amap.api.navi.model.AMapNaviCameraInfo
import com.amap.api.navi.model.AMapNaviCross
import com.amap.api.navi.model.AMapNaviLocation
import com.amap.api.navi.model.AMapNaviRouteNotifyData
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo
import com.amap.api.navi.model.AMapServiceAreaInfo
import com.amap.api.navi.model.AimLessModeCongestionInfo
import com.amap.api.navi.model.AimLessModeStat
import com.amap.api.navi.model.NaviLatLng
import com.ddup.navigation.R
import com.ddup.navigation.data.model.GpsSignalState
import com.ddup.navigation.data.model.Location
import com.ddup.navigation.data.model.NavigationState
import com.ddup.navigation.data.model.NavigationSummary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    startLat: Double,
    startLng: Double,
    destLat: Double,
    destLng: Double,
    onNavigationComplete: (NavigationSummary) -> Unit,
    onBackToMap: () -> Unit,
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    // 获取状态
    val navigationState by viewModel.navigationState.collectAsState()
    val gpsSignalState by viewModel.gpsSignalState.collectAsState()

    // 获取字符串资源，以便在非Composable上下文中使用
    val navigationTitle = stringResource(R.string.navigation)
    val backText = stringResource(R.string.back)
    val startPointText = stringResource(R.string.start_point)
    val endPointText = stringResource(R.string.end_point)
    val completeNavigationText = stringResource(R.string.complete_navigation_text)

    // 获取GPS信号状态相关文本
    val gpsSignalNormalText = stringResource(R.string.gps_signal_normal)
    val gpsSignalWeakText = stringResource(R.string.gps_signal_weak)
    val gpsSignalLostText = stringResource(R.string.gps_signal_lost)
    val gpsSignalStrongText = stringResource(R.string.gps_signal_strong)


    // 创建导航实例
    val aMapNavi = remember { AMapNavi.getInstance(context) }
    val startPoint = remember { NaviLatLng(startLat, startLng) }
    val endPoint = remember { NaviLatLng(destLat, destLng) }

    // 当导航完成时调用回调
    LaunchedEffect(navigationState) {
        if (navigationState is NavigationState.Completed) {
            val summary = (navigationState as NavigationState.Completed).summary
            onNavigationComplete(summary)
        }
    }

    // 处理地图和导航生命周期
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    mapView.onCreate(null)
                    aMapNavi.addAMapNaviListener(object : AMapNaviListener {
                        override fun onInitNaviFailure() {}
                        override fun onInitNaviSuccess() {
                            // 初始化成功后计算路线
                            val startList = ArrayList<NaviLatLng>().apply { add(startPoint) }
                            val endList = ArrayList<NaviLatLng>().apply { add(endPoint) }
                            aMapNavi.calculateDriveRoute(startList, endList, null, NaviType.GPS)
                        }

                        override fun onCalculateRouteSuccess(result: AMapCalcRouteResult) {
                            // 路径规划成功，在地图上显示
                            aMapNavi.startNavi(NaviType.EMULATOR) // 模拟导航
                        }

                        override fun onStartNavi(type: Int) {}
                        override fun onTrafficStatusUpdate() {}
                        override fun onLocationChange(location: AMapNaviLocation) {
                            // 更新车标位置
                            location?.let {
                                val latLng = LatLng(it.coord.latitude, it.coord.longitude)
                                mapView.map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                            }
                        }

                        override fun onGetNavigationText(p0: Int, p1: String?) {

                        }

                        override fun onGetNavigationText(text: String) {}
                        override fun onEndEmulatorNavi() {
                            // 导航结束
                            completeNavigation(
                                aMapNavi, startLat, startLng, destLat, destLng, viewModel
                            )
                        }

                        override fun onArriveDestination() {
                            // 到达目的地
                            completeNavigation(
                                aMapNavi, startLat, startLng, destLat, destLng, viewModel
                            )
                        }

                        override fun onCalculateRouteFailure(p0: Int) {}
                        override fun onCalculateRouteFailure(p0: AMapCalcRouteResult?) {
                            TODO("Not yet implemented")
                        }

                        override fun onReCalculateRouteForYaw() {}
                        override fun onReCalculateRouteForTrafficJam() {}
                        override fun onArrivedWayPoint(p0: Int) {}
                        override fun onGpsOpenStatus(p0: Boolean) {}
                        override fun onNaviInfoUpdate(p0: com.amap.api.navi.model.NaviInfo?) {
                            TODO("Not yet implemented")
                        }

                        override fun updateCameraInfo(p0: Array<out AMapNaviCameraInfo>?) {}
                        override fun updateIntervalCameraInfo(
                            p0: AMapNaviCameraInfo?, p1: AMapNaviCameraInfo?, p2: Int
                        ) {
                        }

                        override fun onServiceAreaUpdate(p0: Array<out AMapServiceAreaInfo>?) {}
                        override fun showCross(p0: AMapNaviCross?) {}
                        override fun hideCross() {}
                        override fun showModeCross(p0: AMapModelCross?) {}
                        override fun hideModeCross() {}
                        override fun showLaneInfo(
                            p0: Array<out AMapLaneInfo>?, p1: ByteArray?, p2: ByteArray?
                        ) {
                        }

                        override fun showLaneInfo(p0: AMapLaneInfo?) {}
                        override fun hideLaneInfo() {}
                        override fun onCalculateRouteSuccess(p0: IntArray?) {
                            TODO("Not yet implemented")
                        }

                        override fun notifyParallelRoad(p0: Int) {}
                        override fun OnUpdateTrafficFacility(p0: Array<out AMapNaviTrafficFacilityInfo>?) {}
                        override fun OnUpdateTrafficFacility(p0: AMapNaviTrafficFacilityInfo?) {}
                        override fun updateAimlessModeStatistics(p0: AimLessModeStat?) {}
                        override fun updateAimlessModeCongestionInfo(p0: AimLessModeCongestionInfo?) {}
                        override fun onPlayRing(p0: Int) {}
                        override fun onNaviRouteNotify(p0: AMapNaviRouteNotifyData?) {}
                        override fun onGpsSignalWeak(p0: Boolean) {
                            TODO("Not yet implemented")
                        }
                    })
                    aMapNavi.setUseInnerVoice(true)
                    aMapNavi.setEmulatorNaviSpeed(75) // 设置模拟导航速度
                }

                Lifecycle.Event.ON_START -> mapView.onResume()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    aMapNavi.pauseNavi()
                }

                Lifecycle.Event.ON_STOP -> {
                    mapView.onPause()
                    aMapNavi.stopNavi()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    mapView.onDestroy()
                    aMapNavi.stopNavi()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(navigationTitle) }, navigationIcon = {
            androidx.compose.material3.IconButton(onClick = onBackToMap) {
                Icon(Icons.Default.ArrowBack, contentDescription = backText)
            }
        })
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 地图视图
            AndroidView(factory = { mapView }, update = { view ->
                val aMap = view.map

                // 配置地图
                setupNavigationMap(aMap)

                // 显示起点和终点标记
                showRouteMarkers(
                    aMap, startLat, startLng, destLat, destLng, startPointText, endPointText
                )

                // 将地图视角调整为显示整个路线
                val boundsBuilder = com.amap.api.maps.model.LatLngBounds.Builder()
                boundsBuilder.include(LatLng(startLat, startLng))
                boundsBuilder.include(LatLng(destLat, destLng))
                aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
            }, modifier = Modifier.fillMaxSize()
            )

            // GPS信号状态指示卡片
            if (gpsSignalState != GpsSignalState.NORMAL) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                when (gpsSignalState) {
                                    GpsSignalState.WEAK -> Color(0xFFFFF9C4) // 淡黄色
                                    GpsSignalState.LOST -> Color(0xFFFFCDD2) // 淡红色
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = when (gpsSignalState) {
                                GpsSignalState.NORMAL -> gpsSignalNormalText
                                GpsSignalState.WEAK -> gpsSignalWeakText
                                GpsSignalState.LOST -> gpsSignalLostText
                                GpsSignalState.STRONG -> gpsSignalStrongText
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 底部操作按钮
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(onClick = {
                    completeNavigation(
                        aMapNavi, startLat, startLng, destLat, destLng, viewModel
                    )
                }) {
                    Icon(Icons.Default.Check, contentDescription = completeNavigationText)
                }
            }

            // GPS信号强度图标
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = when (gpsSignalState) {
                        GpsSignalState.STRONG -> Icons.Default.Place
                        GpsSignalState.NORMAL -> Icons.Default.Place
                        GpsSignalState.WEAK -> Icons.Default.Place
                        GpsSignalState.LOST -> Icons.Default.Place
                    },
                    contentDescription = when (gpsSignalState) {
                        GpsSignalState.STRONG -> gpsSignalStrongText
                        GpsSignalState.WEAK -> gpsSignalWeakText
                        GpsSignalState.LOST -> gpsSignalLostText
                        else -> ""
                    },
                    tint = when (gpsSignalState) {
                        GpsSignalState.STRONG -> Color.Green
                        GpsSignalState.NORMAL -> Color.Blue
                        GpsSignalState.WEAK -> Color.Yellow
                        GpsSignalState.LOST -> Color.Red
                    }
                )
            }
        }
    }
}

private fun completeNavigation(
    aMapNavi: AMapNavi,
    startLat: Double,
    startLng: Double,
    destLat: Double,
    destLng: Double,
    viewModel: NavigationViewModel
) {
    // 获取导航信息
    val naviPath = aMapNavi.naviPath
    val distance = naviPath?.allLength?.toFloat() ?: 0f
    val duration = naviPath?.allTime ?: 0

    // 停止导航
    aMapNavi.stopNavi()

    // 通知ViewModel完成导航
    viewModel.completeNavigation(
        distance = distance,
        duration = duration,
        startPoint = Location(startLat, startLng),
        endPoint = Location(destLat, destLng)
    )
}

private fun setupNavigationMap(aMap: AMap) {
    // 启用我的位置图层
    aMap.isMyLocationEnabled = true

    // 设置我的位置样式
    val myLocationStyle = MyLocationStyle()
    // 设置为导航模式，显示车标
    myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
    // 设置定位间隔
    myLocationStyle.interval(1000)
    // 显示定位蓝点
    myLocationStyle.showMyLocation(true)
    aMap.myLocationStyle = myLocationStyle

    // UI设置
    val uiSettings = aMap.uiSettings
    uiSettings.isZoomControlsEnabled = true
    uiSettings.isCompassEnabled = true
    uiSettings.isScaleControlsEnabled = true
    uiSettings.isMyLocationButtonEnabled = false

    // 设置缩放级别
    aMap.moveCamera(CameraUpdateFactory.zoomTo(17f))
}

private fun showRouteMarkers(
    aMap: AMap,
    startLat: Double,
    startLng: Double,
    destLat: Double,
    destLng: Double,
    startPointText: String,
    endPointText: String
) {
    // 添加起点标记
    aMap.addMarker(
        MarkerOptions()
            .position(LatLng(startLat, startLng))
            .title(startPointText)
            .snippet("$startLat, $startLng")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
    )

    // 添加终点标记
    aMap.addMarker(
        MarkerOptions()
            .position(LatLng(destLat, destLng))
            .title(endPointText)
            .snippet("$destLat, $destLng")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
    )

    // 添加起点到终点的连线
    val points = listOf(
        LatLng(startLat, startLng),
        LatLng(destLat, destLng)
    )
    aMap.addPolyline(
        com.amap.api.maps.model.PolylineOptions()
            .addAll(points)
            .width(10f)
            .color(0xFFFF0000.toInt())
    ) // 红色线条
}