package com.example.sih.ui.screens.admin

import androidx.compose.ui.graphics.Brush
import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.sih.viewmodel.AuthViewModel
import com.example.sih.viewmodel.StationMapViewModel
import com.example.sih.viewmodel.admin.StationViewModel
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ManagerStationMapScreen(
    viewModel: StationViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val stations by viewModel.stations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val selectedStation by viewModel.selectedStation.collectAsState()
    val geocodingInProgress by viewModel.geocodingInProgress.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.5937, 78.9629), 5f)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch stations for manager's state when screen loads
    LaunchedEffect(userProfile) {
        userProfile?.assignedState?.let { state ->
            Log.d("manager", "State: $state")
            viewModel.fetchStationsForState(state)
        }
    }

    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearSnackbar()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Stations in ${userProfile?.assignedState ?: "Your State"}") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.5f),
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading || geocodingInProgress) {
                FullScreenLoading()
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(mapType = MapType.NORMAL)
                ) {
                    stations.forEach { station ->
                        Log.d("manager", "Station: $station")
                        station.location?.let { location ->
                            StationMapItems(
                                station = station,
                                location = location,
                                viewModel = viewModel
                            )
                        }
                    }
                }
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
}
@SuppressLint("UnrememberedMutableState")
@Composable
private fun StationMapItems(station: Station, location: LatLng, viewModel: StationViewModel) {
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