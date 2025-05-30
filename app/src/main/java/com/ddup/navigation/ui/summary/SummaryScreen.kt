package com.ddup.navigation.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import com.ddup.navigation.R
import com.ddup.navigation.data.model.NavigationSummary
import com.ddup.navigation.utils.formatDistance
import com.ddup.navigation.utils.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    summary: NavigationSummary,
    onBackToMap: () -> Unit
) {

    val summaryTitle = stringResource(R.string.navigation_summary)
    val backText = stringResource(R.string.back)
    val backToMapText = stringResource(R.string.back_to_map)
    val navigationCompleteText = stringResource(R.string.navigation_complete)
    val totalDistanceText = stringResource(R.string.total_distance)
    val totalDurationText = stringResource(R.string.total_duration)
    val startPointText = stringResource(R.string.start_point)
    val endPointText = stringResource(R.string.end_point)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(summaryTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackToMap) {
                        Icon(Icons.Default.ArrowBack, contentDescription = backText)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 导航路线地图预览
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            ) {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                        }
                    },
                    update = { mapView ->
                        val aMap = mapView.map

                        // 显示起点和终点标记
                        val startLatLng =
                            LatLng(summary.startPoint.latitude, summary.startPoint.longitude)
                        val endLatLng =
                            LatLng(summary.endPoint.latitude, summary.endPoint.longitude)

                        aMap.addMarker(
                            MarkerOptions()
                                .position(startLatLng)
                                .title(startPointText)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )

                        aMap.addMarker(
                            MarkerOptions()
                                .position(endLatLng)
                                .title(endPointText)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )

                        // 绘制路线
                        aMap.addPolyline(
                            PolylineOptions()
                                .add(startLatLng, endLatLng)
                                .width(10f)
                                .color(0xFF4CAF50.toInt())
                        )

                        // 将地图视角调整为显示整个路线
                        val boundsBuilder = LatLngBounds.Builder()
                        boundsBuilder.include(startLatLng)
                        boundsBuilder.include(endLatLng)
                        aMap.moveCamera(
                            CameraUpdateFactory.newLatLngBounds(
                                boundsBuilder.build(),
                                50
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 导航摘要信息
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = navigationCompleteText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 距离信息
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "  $totalDistanceText: ${formatDistance(summary.distance)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 时间信息
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "  $totalDurationText: ${formatDuration(summary.duration)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 起点信息
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "  $startPointText: ${summary.startPoint.latitude}, ${summary.startPoint.longitude}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 终点信息
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "  $endPointText: ${summary.endPoint.latitude}, ${summary.endPoint.longitude}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 返回地图按钮
            Button(
                onClick = onBackToMap,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = backToMapText)
            }
        }
    }
}