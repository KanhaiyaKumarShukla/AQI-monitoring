package com.example.sih.ui.screens.admin

import androidx.compose.ui.graphics.Brush
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sih.util.Station
import com.example.sih.viewmodel.StationMapViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MarkerState.Companion.invoke
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationMapScreen(
    viewModel: StationMapViewModel = hiltViewModel(),
) {
    val visibleStations by viewModel.visibleStations.collectAsState()
    val allStations by viewModel.stations.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val initialDataLoaded by viewModel.initialDataLoaded.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.5937, 78.9629), 5f)
    }

    // Initial loading state
    if (!initialDataLoaded) {
        FullScreenLoading()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.NORMAL, isBuildingEnabled = true)
        ) {
            visibleStations.forEach { station ->
                station.location?.let { location ->
                    StationMapItem(station, location, viewModel)
                }
            }
        }

        // Loading progress for remaining data
        if (isLoading) {
            LoadingProgressIndicator(loadingProgress)
        }

        selectedStation?.let { station ->
            StationDetailsDialog(
                station = station,
                onDismiss = { viewModel.clearSelection() },
                onSendNotification = { viewModel.sendNotification() }
            )
        }
    }
}

@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading initial station data...")
        }
    }
}

@Composable
private fun LoadingProgressIndicator(progress: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Loading stations: ${(progress * 100).toInt()}%",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun StationMapItem(station: Station, location: LatLng, viewModel: StationMapViewModel) {
    // Draw honeycomb polygon
    Polygon(
        points = createHoneycombPolygon(location, 5000.0),
        fillColor = when(station.status) {
            Station.StationStatus.Active -> Color.Green.copy(alpha = 0.5f)
            Station.StationStatus.Inactive -> Color.Red.copy(alpha = 0.5f)
            Station.StationStatus.UnderMaintenance -> Color.Yellow.copy(alpha = 0.5f)
        },
        strokeColor = Color.Black.copy(alpha = 0.7f),
        strokeWidth = 2f,
        clickable = true,
        onClick = {
            viewModel.selectStation(station)
            true
        }
    )

    Marker(
        state = MarkerState(position = location),
        title = station.displayName,
        snippet = station.status.name,
        icon = BitmapDescriptorFactory.defaultMarker(
            when (station.status) {
                Station.StationStatus.Active -> BitmapDescriptorFactory.HUE_GREEN
                Station.StationStatus.Inactive -> BitmapDescriptorFactory.HUE_RED
                Station.StationStatus.UnderMaintenance -> BitmapDescriptorFactory.HUE_YELLOW
            }
        ),
        onClick = {
            viewModel.selectStation(station)
            true
        }
    )
}

/*
@SuppressLint("SimpleDateFormat")
@Composable
fun StationDetailsDialog(
    station: Station,
    onDismiss: () -> Unit,
    onSendNotification: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(station.displayName, style = MaterialTheme.typography.bodyMedium) },
        text = {
            Column {
                Text("ID: ${station.stationId}")
                Text("Status: ${station.status.name.replace("_", " ")}")
                Text("Last Updated: ${SimpleDateFormat("dd MMM yyyy HH:mm").format(station.lastUpdated.toDate())}")
                Text("Address: ${station.address}")
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSendNotification()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Send Notification")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


 */

@SuppressLint("SimpleDateFormat")
@Composable
fun StationDetailsDialog(
    station: Station,
    onDismiss: () -> Unit,
    onSendNotification: () -> Unit
) {
    val statusColor = when (station.status) {
        Station.StationStatus.Active -> Color(0xFF4CAF50)  // Green
        Station.StationStatus.Inactive -> Color(0xFFF44336) // Red
        Station.StationStatus.UnderMaintenance -> Color(0xFFFFC107) // Amber
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(12.dp),
        title = {
            Text(
                text = station.displayName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                // Status Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.Start)
                ) {
                    Text(
                        text = station.status.name.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        ),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Details Section
                StationDetailItem(icon = Icons.Default.Info, text = "ID: ${station.stationId}")
                Spacer(modifier = Modifier.height(8.dp))

                StationDetailItem(
                    icon = Icons.Default.Update,
                    text = "Last Updated: ${SimpleDateFormat("dd MMM yyyy, hh:mm a").format(station.lastUpdated.toDate())}"
                )
                Spacer(modifier = Modifier.height(8.dp))

                StationDetailItem(
                    icon = Icons.Default.LocationOn,
                    text = "Address: ${station.address}",
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        },
        confirmButton = {
            val isActive = station.status == Station.StationStatus.Active
            Button(
                onClick = {
                    if (!isActive) {
                        onSendNotification()
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color.Gray.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
                    contentColor = if (isActive) Color.DarkGray else MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.padding(end = 8.dp),

            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isActive) "No Need to Notify" else "Send Notification")
            }

        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Close")
            }
        },
        containerColor = Color.White.copy(alpha = 0.9f),
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun StationDetailItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}



fun createHoneycombPolygon(center: LatLng, radiusMeters: Double): List<LatLng> {
    val points = mutableListOf<LatLng>()
    val earthRadius = 6378137.0 // Earth's radius in meters

    for (i in 0..5) {
        val angle = Math.toRadians(60.0 * i - 30.0)
        val dx = radiusMeters * cos(angle)
        val dy = radiusMeters * sin(angle)

        val lat = center.latitude + (dy / earthRadius) * (180.0 / Math.PI)
        val lng = center.longitude + (dx / (earthRadius * cos(Math.toRadians(center.latitude)))) * (180.0 / Math.PI)

        points.add(LatLng(lat, lng))
    }

    // Close the polygon
    points.add(points.first())
    return points
}