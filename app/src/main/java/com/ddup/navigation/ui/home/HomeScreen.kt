package com.ddup.navigation.ui.home

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ddup.navigation.R
import com.ddup.navigation.data.model.Location
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartNavigation: (start: Location, destination: Location) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val mapView = remember { MapView(context) }

    // 处理导航
    LaunchedEffect(state.navigateToNavigationScreen) {
        android.util.Log.d("HomeScreen", "LaunchedEffect triggered, navigateToNavigationScreen: ${state.navigateToNavigationScreen}")
        android.util.Log.d("HomeScreen", "initialLocation: ${state.initialLocation}")
        android.util.Log.d("HomeScreen", "destination: ${state.destination}")
        if (state.navigateToNavigationScreen) {
            state.initialLocation?.let { startLocation ->
                state.destination?.let { destLocation ->
                    onStartNavigation(startLocation, destLocation)
                    viewModel.onNavigationScreenShown()
                }
            }
        }
    }

    // 获取字符串资源，以便在非Composable上下文中使用
    val destinationText = stringResource(R.string.destination)
    val startNavigationText = stringResource(R.string.start_navigation)
    val locationPermissionText = stringResource(R.string.location_permission_required)

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    )

    // Check for permissions when the screen is first composed
    LaunchedEffect(key1 = true) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    // Show a snackbar if permissions are not granted
    LaunchedEffect(key1 = locationPermissionsState.allPermissionsGranted) {
        if (!locationPermissionsState.allPermissionsGranted) {
            snackbarHostState.showSnackbar(
                message = locationPermissionText
            )
        } else {
            viewModel.retryInitialLocation()
        }
    }

    // 处理地图生命周期
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onResume()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 地图视图
            AndroidView(
                factory = { mapView },
                update = { view ->
                    val aMap = view.map

                    // 配置地图（仅首次设置）
                    setupMap(aMap)

                    // 设置地图点击监听
                    aMap.setOnMapClickListener { latLng ->
                        // 清除之前的标记
                        aMap.clear()

                        // 添加新标记
                        aMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(destinationText)
                                .snippet("${latLng.latitude}, ${latLng.longitude}")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )

                        // 更新ViewModel中的目的地
                        viewModel.setDestination(latLng.latitude, latLng.longitude)
                    }

                    // 如果存在目的地，显示标记
                    state.destination?.let { destination ->
                        val destinationLatLng = LatLng(destination.latitude, destination.longitude)
                        if (aMap.mapScreenMarkers.isEmpty()) {
                            aMap.addMarker(
                                MarkerOptions()
                                    .position(destinationLatLng)
                                    .title(destinationText)
                                    .snippet("${destinationLatLng.latitude}, ${destinationLatLng.longitude}")
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_RED
                                        )
                                    )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (state.showNavigateButton) {
                FloatingActionButton(
                    onClick = { viewModel.startNavigation() },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .align(Alignment.BottomCenter),
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = startNavigationText
                    )
                }
            }

            // 权限未授予时显示提示
            if (!locationPermissionsState.allPermissionsGranted) {
                Text(
                    text = locationPermissionText,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}

private fun setupMap(aMap: AMap) {
    // 启用我的位置图层
    aMap.isMyLocationEnabled = true

    // 设置我的位置样式 - 只显示当前位置，不自动跟随
    val myLocationStyle = MyLocationStyle()
    // 只定位一次，且不跟随移动
    myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
    // 设置定位间隔
    myLocationStyle.interval(2000)
    // 显示定位蓝点
    myLocationStyle.showMyLocation(true)
    aMap.myLocationStyle = myLocationStyle

    // UI设置
    val uiSettings = aMap.uiSettings
    uiSettings.isZoomControlsEnabled = true
    uiSettings.isCompassEnabled = true
    uiSettings.isMyLocationButtonEnabled = false

    // 设置缩放级别
    aMap.moveCamera(CameraUpdateFactory.zoomTo(17f))
}