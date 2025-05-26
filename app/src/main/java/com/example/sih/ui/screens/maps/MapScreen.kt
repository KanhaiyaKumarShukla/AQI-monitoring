package com.example.sih.ui.screens.maps

import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.PermissionStatus
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.rememberPermissionState
import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.graphics.Typeface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.sih.util.AqiData
import com.example.sih.util.LocationService
import com.example.sih.viewmodel.AqiViewModel
import kotlin.math.ceil
import android.Manifest
import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.random.Random
import android.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import android.graphics.Color
import android.location.Geocoder
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.window.DialogProperties
import java.io.IOException

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: AqiViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current

    // Location permission state
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Camera position state
    val cameraPositionState = rememberCameraPositionState()

    // Track if we've requested permission before
    var hasRequestedPermission by remember { mutableStateOf(false) }

    // Check if we have location permission
    val hasLocationPermission = locationPermissionState.status.isGranted

    // Location callback
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    // Move camera in coroutine scope
                    viewModel.viewModelScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 12f),
                            1000
                        )
                    }
                }
            }
        }
    }
    val scope = rememberCoroutineScope()

    // Location client
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Handle permission changes
    LaunchedEffect(locationPermissionState.status) {
        when (locationPermissionState.status) {
            is PermissionStatus.Granted -> {
                // Get current location if permission granted
                /*
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            viewModel.viewModelScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(latLng, 12f),
                                    1000
                                )
                                Log.d("AqiViewModelSearch", "Moving camera to 1: $latLng")
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("MapScreen", "Error getting location: ${e.message}")
                }

                 */
            }
            is PermissionStatus.Denied -> {
                if ((locationPermissionState.status as PermissionStatus.Denied).shouldShowRationale) {
                    // Show rationale if denied but can ask again
                    if (!hasRequestedPermission) {
                        locationPermissionState.launchPermissionRequest()
                        hasRequestedPermission = true
                    }
                }
            }
        }
    }

    // First launch effect to request permission if not granted
    LaunchedEffect(Unit) {

        scope.launch {
            viewModel.loadAllCitiesData()
            viewModel.startRegularUpdates()
        }
        if (!hasLocationPermission && !hasRequestedPermission) {
            locationPermissionState.launchPermissionRequest()
            hasRequestedPermission = true
        }
    }

    val allCitiesData by viewModel.allCitiesData.collectAsState()

    // Collect AQI data
    val aqiData by viewModel.currentAqiData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()


    var showDialog by remember { mutableStateOf(false) }
    var selectedAqiData by remember { mutableStateOf<AqiData?>(null) }

    // Map properties
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isBuildingEnabled = true,
                isIndoorEnabled = true
            )
        )
    }

    // Map UI controls
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = true,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = true,
                zoomGesturesEnabled = true
            )
        )
    }

    // Handle searched location from ViewModel
    LaunchedEffect(viewModel.currentLocation) {
        viewModel.currentLocation.value?.let { latLng ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(latLng, 12f),
                1000
            )
            Log.d("AqiViewModelSearch", "Moving camera to $latLng")
        }
    }


    Scaffold(
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .padding(bottom = 90.dp)
            ) {
                // Search button
                FloatingActionButton(
                    onClick = {
                        navController.navigate("search") {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(bottom = 8.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }

                // Current location button
                if (hasLocationPermission) {
                    FloatingActionButton(
                        onClick = {
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    location?.let {
                                        val latLng = LatLng(it.latitude, it.longitude)
                                        viewModel.viewModelScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(latLng, 12f),
                                                1000
                                            )
                                            Log.d("AqiViewModelSearch", "Moving camera to 2: $latLng")
                                        }
                                    }
                                }
                            } catch (e: SecurityException) {
                                Log.e("MapScreen", "Error getting location: ${e.message}")
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Google Map
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings
            ) {

                aqiData?.let { data ->
                    // Get LatLng from city name
                    val cityLatLng = rememberCityLatLng(data.City)
                    val position = cityLatLng ?: cameraPositionState.position.target

                    Marker(
                        state = MarkerState(position = position),
                        icon = rememberAqiMarker(aqi = data.AQI ?: 0),
                        onClick = {
                            // Show dialog using state
                            selectedAqiData = data
                            showDialog = true
                            true
                        }
                    )
                }
                allCitiesData.forEach { data ->
                    val cityLatLng = rememberCityLatLng(data.City)
                    val position = cityLatLng ?: cameraPositionState.position.target
                    Marker(
                        state = MarkerState(position = position),
                        icon = rememberAqiMarker(data.AQI ?: 0),
                        onClick = {
                            selectedAqiData = data
                            showDialog = true
                            true
                        }
                    )

                }

            }
            if (showDialog && selectedAqiData != null) {
                AqiDetailDialog(
                    aqiData = selectedAqiData!!,
                    onDismiss = {
                        showDialog = false
                        selectedAqiData = null
                    }
                )
            }

            // Permission rationale dialog
            if (!hasLocationPermission && hasRequestedPermission) {
                AlertDialog(
                    onDismissRequest = { /* Don't allow dismiss */ },
                    title = { Text("Location Permission Needed") },
                    text = { Text("This app needs location permission to show your current location and nearby AQI data.") },
                    confirmButton = {
                        TextButton(
                            onClick = { locationPermissionState.launchPermissionRequest() }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun rememberCityLatLng(cityName: String?): LatLng? {
    val context = LocalContext.current
    var latLng by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(cityName) {
        if (cityName != null) {
            latLng = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context)
                    val addresses = geocoder.getFromLocationName(cityName, 1)
                    if (addresses?.isNotEmpty() == true) {
                        val address = addresses[0]
                        LatLng(address.latitude, address.longitude)
                    } else {
                        null
                    }
                } catch (e: IOException) {
                    Log.e("Geocoder", "Error getting location from city name", e)
                    null
                }
            }
        }
    }

    return latLng
}

@Composable
fun rememberAqiMarker(aqi: Int): BitmapDescriptor {
    val context = LocalContext.current
    val resources = context.resources

    return remember(aqi) {
        val density = resources.displayMetrics.density
        val size = (52 * density).toInt() // Marker size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Define gradient colors based on AQI
        val (startColor, endColor) = getAqiGradientColors(aqi)

        // Create gradient shader
        val shade = RadialGradient(
            size / 2f, size / 2f, size / 2f,
            startColor, endColor, Shader.TileMode.CLAMP
        )

        // Draw gradient circle
        val paint = Paint().apply {
            shader = shade
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Add shadow
        val shadowPaint = Paint().apply {
            color = Color.argb(80, 0, 0, 0)
            setShadowLayer(6f * density, 0f, 3f * density, Color.BLACK)
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.3f, shadowPaint)

        // Draw main circle
        canvas.drawCircle(size / 2f, size / 2f, size / 2.3f, paint)

        // Add white border
        val borderPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2f * density
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.3f, borderPaint)

        // Draw AQI text
        val textPaint = Paint().apply {
            color = if (aqi <= 100) Color.BLACK else Color.WHITE
            isAntiAlias = true
            textSize = 20f * density
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val xPos = canvas.width / 2f
        val yPos = (canvas.height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f)

        canvas.drawText(aqi.toString(), xPos, yPos, textPaint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

private fun getAqiGradientColors(aqi: Int): Pair<Int, Int> {
    return when {
        aqi <= 50 -> Pair(
            Color.parseColor("#81C784"), // Light Green
            Color.parseColor("#4CAF50")  // Green
        )
        aqi <= 100 -> Pair(
            Color.parseColor("#FFF176"), // Light Yellow
            Color.parseColor("#FFEB3B") // Yellow
        )
        aqi <= 150 -> Pair(
            Color.parseColor("#FFB74D"), // Light Orange
            Color.parseColor("#FF9800") // Orange
        )
        aqi <= 200 -> Pair(
            Color.parseColor("#E57373"), // Light Red
            Color.parseColor("#F44336") // Red
        )
        aqi <= 300 -> Pair(
            Color.parseColor("#BA68C8"), // Light Purple
            Color.parseColor("#9C27B0") // Purple
        )
        else -> Pair(
            Color.parseColor("#AD1457"), // Dark Pink
            Color.parseColor("#880E4F") // Maroon
        )
    }
}

// Attractive AQI detail dialog
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AqiDetailDialog(
    aqiData: AqiData,
    onDismiss: () -> Unit
) {
    val aqiLevel = when {
        aqiData.AQI!! <= 50 -> "Good"
        aqiData.AQI <= 100 -> "Moderate"
        aqiData.AQI <= 150 -> "Unhealthy for Sensitive Groups"
        aqiData.AQI <= 200 -> "Unhealthy"
        aqiData.AQI <= 300 -> "Very Unhealthy"
        else -> "Hazardous"
    }

    val aqiColor = when {
        aqiData.AQI <= 50 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        aqiData.AQI <= 100 -> androidx.compose.ui.graphics.Color(0xFFFFEB3B) // Yellow
        aqiData.AQI <= 150 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
        aqiData.AQI <= 200 -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
        aqiData.AQI <= 300 -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
        else -> androidx.compose.ui.graphics.Color(0xFF880E4F) // Maroon
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // AQI value with colored circle background
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = aqiColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = aqiData.AQI.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = aqiColor
                        )
                        Text(
                            text = aqiLevel,
                            style = MaterialTheme.typography.titleMedium,
                            color = aqiColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Air quality parameters
                AqiParameterRow(
                    name = "PM2.5",
                    value = "${aqiData.PM25} µg/m³",
                    icon = Icons.Default.Air
                )
                AqiParameterRow(
                    name = "PM10",
                    value = "${aqiData.PM10} µg/m³",
                    icon = Icons.Default.Air
                )
                AqiParameterRow(
                    name = "Ozone",
                    value = "${aqiData.O3} ppb",
                    icon = Icons.Default.WbSunny
                )
                AqiParameterRow(
                    name = "NO2",
                    value = "${aqiData.NO2} ppb",
                    icon = Icons.Default.Factory
                )
                AqiParameterRow(
                    name = "SO2",
                    value = "${aqiData.SO2} ppb",
                    icon = Icons.Default.Factory
                )
                AqiParameterRow(
                    name = "CO",
                    value = "${aqiData.CO} ppm",
                    icon = Icons.Outlined.LocalFireDepartment
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AqiDetailDialog(
    aqiData: AqiData,
    onDismiss: () -> Unit
) {
    val aqiLevel = when {
        aqiData.AQI!! <= 50 -> "Good"
        aqiData.AQI <= 100 -> "Moderate"
        aqiData.AQI <= 150 -> "Unhealthy for Sensitive Groups"
        aqiData.AQI <= 200 -> "Unhealthy"
        aqiData.AQI <= 300 -> "Very Unhealthy"
        else -> "Hazardous"
    }

    val aqiColor = when {
        aqiData.AQI <= 50 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        aqiData.AQI <= 100 -> androidx.compose.ui.graphics.Color(0xFFF8E32C) // Yellow
        aqiData.AQI <= 150 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
        aqiData.AQI <= 200 -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
        aqiData.AQI <= 300 -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Purple
        else -> androidx.compose.ui.graphics.Color(0xFF880E4F) // Maroon
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Semi-transparent white background with rounded corners
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.95f),
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // AQI value with colored circle background
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            color = aqiColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = aqiData.AQI.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = aqiColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 100.dp)
                        )
                        Text(
                            text = aqiLevel,
                            style = MaterialTheme.typography.titleMedium,
                            color = aqiColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Air quality parameters
                AqiParameterRow(
                    name = "PM2.5",
                    value = "${aqiData.PM25} µg/m³",
                    icon = Icons.Default.Air
                )
                AqiParameterRow(
                    name = "PM10",
                    value = "${aqiData.PM10} µg/m³",
                    icon = Icons.Default.Air
                )
                AqiParameterRow(
                    name = "Ozone",
                    value = "${aqiData.O3} ppb",
                    icon = Icons.Default.WbSunny
                )
                AqiParameterRow(
                    name = "NO2",
                    value = "${aqiData.NO2} ppb",
                    icon = Icons.Default.Factory
                )
                AqiParameterRow(
                    name = "SO2",
                    value = "${aqiData.SO2} ppb",
                    icon = Icons.Default.Factory
                )
                AqiParameterRow(
                    name = "CO",
                    value = "${aqiData.CO} ppm",
                    icon = Icons.Outlined.LocalFireDepartment
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Close", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun AqiParameterRow(name: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMyComposable() {
    val aqi=AqiData(
        AQI=300,AQI_Bucket="Hazardous", Benzene=200.0, CO=200.0, PM10=200.0, PM25=200.0, SO2=200.0, NH3=200.0, NO2=200.0, NO=200.0, NOx=200.0, O3=200.0, City = "Bhopal"

    )
    AqiDetailDialog(aqi, {})
}
