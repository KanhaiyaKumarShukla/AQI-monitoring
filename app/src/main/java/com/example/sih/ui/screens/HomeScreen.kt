package com.example.sih.ui.screens

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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.graphics.Typeface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import com.example.sih.R
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.format.DateTimeFormatter
import androidx.core.content.ContextCompat
import kotlinx.datetime.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
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
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.random.Random

/*
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    currentAqi: Int,
    temp: Float,
    humidity: Float,
    aqiViewModel: AqiViewModel,
    parameters: List<AirParameter>,
    monthlyAqiData: Map<String, Int>,
    onSearchClicked: () -> Unit,
    onLocationClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {

    val currentAqiData by aqiViewModel.currentAqiData.collectAsState()
    val aqiHistory by aqiViewModel.aqiHistory.collectAsState()
    val currentCity by aqiViewModel.currentCity.collectAsState()
    val isLoading by aqiViewModel.isLoading.collectAsState()
    val error by aqiViewModel.error.collectAsState()
    val context = LocalContext.current
    var currentLocationName by remember { mutableStateOf("Loading...") }
    val locationService = LocationService(context)
    // Permission state
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var showRationaleDialog by remember { mutableStateOf(false) }

    // Handle permission changes
    LaunchedEffect(Unit) {
        when {
            locationPermissionState.status.isGranted -> {
                currentLocationName = locationService.getCurrentCity()
                fetchLocationAndUpdate(currentCity, aqiViewModel, context)
                Log.d("AqiRepository", "Location permission granted: $currentLocationName")
            }
            locationPermissionState.status.shouldShowRationale -> {
                showRationaleDialog = true
            }
            else -> {
                // Request permission immediately on first load
                locationPermissionState.launchPermissionRequest()
            }
        }
    }

    // Handle permission changes after initial request
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            currentLocationName = locationService.getCurrentCity()
            fetchLocationAndUpdate(currentLocationName, aqiViewModel, context)
            Log.d("AqiRepository", "Location permission granted2: $currentLocationName")
        }
    }


    if (showRationaleDialog) {
        RationaleDialog(
            onConfirm = {
                showRationaleDialog = false
                locationPermissionState.launchPermissionRequest()
            },
            onDismiss = { showRationaleDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        currentLocationName = locationService.getCurrentCity()
        aqiViewModel.setCurrentCity(currentLocationName)

    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    /*
    if (isLoading && currentAqiData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        */
        currentAqiData?.let { data ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Log.d("AqiRepository", "currentAqiData: $data")
                item {
                    // Location Selector Row
                    LocationSelector(
                        locationName = currentCity,
                        onSearchClicked = onSearchClicked,
                        onLocationClicked = {
                            if (locationPermissionState.status is PermissionStatus.Granted) {
                                onLocationClicked()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        },
                        onProfileClicked = onProfileClicked
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Main AQI Card
                    AqiCard(
                        aqiValue = currentAqi,
                        temperature = temp,
                        humidity = humidity,
                        modifier = Modifier.fillMaxWidth()
                    )


                    // Parameters Horizontal Scroll
                    Text(
                        text = "Air Components",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    ParametersBreakdown(parameters = parameters)

                    // Real-time Chart Section
                    Text(
                        text = "Real-time AQI Monitoring",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                    )
                    RealTimeAqiChart(
                        aqiData = aqiHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )

                    // Health Advisory Section
                    HealthAdvisoryCard(currentAqi = currentAqi)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Monthly AQI Calendar
                    Text(
                        text = "Monthly AQI Overview",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    AqiCalendar(monthlyAqiData = monthlyAqiData)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {

                }
            }
        }


}
*/

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    currentAqi: Int,
    temp: Float,
    humidity: Float,
    aqiViewModel: AqiViewModel,
    parameters: List<AirParameter>,
    monthlyAqiData: Map<String, Int>,
    onSearchClicked: () -> Unit,
    onLocationClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }

    // ViewModel states
    val currentAqiData by aqiViewModel.currentAqiData.collectAsState()
    val aqiHistory by aqiViewModel.aqiHistory.collectAsState()
    val currentCity by aqiViewModel.currentCity.collectAsState()
    val isCityChanging by aqiViewModel.isCityChanging.collectAsState()
    val error by aqiViewModel.error.collectAsState()
    val temperature by aqiViewModel.currentTemperature.collectAsState()
    val humidity by aqiViewModel.currentHumidity.collectAsState()


    // UI states
    var currentLocationName by remember { mutableStateOf("Detecting location...") }
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showPermissionDenied by remember { mutableStateOf(false) }


    // Permission state
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    // Initial permission check and location fetch
    LaunchedEffect(Unit) {
        checkLocationPermissionAndFetch(
            locationPermissionState,
            locationService,
            aqiViewModel,
            onRationale = { showRationaleDialog = true },
            onDenied = { showPermissionDenied = true }
        )
    }

    // Handle permission changes
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            showRationaleDialog = false
            showPermissionDenied = false
            fetchLocation(locationService, aqiViewModel)
        }
    }

    // Handle errors
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // Show rationale dialog if needed
    if (showRationaleDialog) {
        RationaleDialog(
            onConfirm = {
                showRationaleDialog = false
                locationPermissionState.launchPermissionRequest()
            },
            onDismiss = { showRationaleDialog = false }
        )
    }

    // Main content
    Box(modifier = Modifier.fillMaxSize()) {
        Log.d("AqiRepository", "currentAqiData: $currentAqiData")
        when {
            // Loading state
            isCityChanging && currentAqiData == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    GifLoader()
                }
            }

            // Permission denied state
            showPermissionDenied -> {
                PermissionDeniedContent {
                    locationPermissionState.launchPermissionRequest()
                }
            }

            // Empty data state
            currentAqiData == null -> {
                EmptyDataContent()
            }

            // Success state
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    item {
                        LocationSelector(
                            locationName = currentCity,
                            onSearchClicked = onSearchClicked,
                            onLocationClicked = {
                                if (locationPermissionState.status.isGranted) {
                                    onLocationClicked()
                                } else {
                                    locationPermissionState.launchPermissionRequest()
                                }
                            },
                            onProfileClicked = onProfileClicked
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        AqiCard(
                            aqiValue = currentAqiData!!.AQI?:0,
                            temperature = "%.1f".format(temperature ?: 32.1f).toFloat(),
                            humidity = "%.1f".format(humidity ?: 35.9f).toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Air Components",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        ParametersBreakdown(
                            currentAqiData = currentAqiData,
                            aqiHistory = aqiHistory
                        )

                        Text(
                            text = "Real-time AQI Monitoring",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                        )

                        if (aqiHistory.isNotEmpty()) {
                            RealTimeAqiChart(
                                aqiData = aqiHistory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                // Text("Historical AQI data Loading...")
                                GifLoader(size=40.dp)
                            }
                        }

                        HealthAdvisoryCard(currentAqi = currentAqiData!!.AQI?:0)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Monthly AQI Overview",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        if (aqiHistory.isNotEmpty()) {
                            AqiCalendar(
                                aqiHistory = aqiHistory,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                GifLoader(size=40.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper functions
@OptIn(ExperimentalPermissionsApi::class)
private suspend fun checkLocationPermissionAndFetch(
    permissionState: PermissionState,
    locationService: LocationService,
    aqiViewModel: AqiViewModel,
    onRationale: () -> Unit,
    onDenied: () -> Unit
) {
    when {
        permissionState.status.isGranted -> {
            fetchLocation(locationService, aqiViewModel)
        }
        permissionState.status.shouldShowRationale -> {
            onRationale()
        }
        else -> {
            permissionState.launchPermissionRequest()
            if (!permissionState.status.isGranted) {
                onDenied()
            }
        }
    }
}

private suspend fun fetchLocation(
    locationService: LocationService,
    aqiViewModel: AqiViewModel
) {
    try {
        val city = locationService.getCurrentCity()
        if (city != "Unknown") {
            aqiViewModel.setCurrentCity(city)
        }
    } catch (e: Exception) {
        Log.e("Location", "Error fetching location", e)
    }
}

@Composable
private fun PermissionDeniedContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Location permission required")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Request Permission")
        }
    }
}

@Composable
private fun EmptyDataContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No air quality data available")
    }
}

private suspend fun fetchLocationAndUpdate(
    currentLocationName: String,
    aqiViewModel: AqiViewModel,
    context: Context
) {
    try {
        aqiViewModel.setCurrentCity(currentLocationName)
    } catch (e: Exception) {

        Log.e("Location", "Error getting location", e)
    }
}


@SuppressLint("UseCompatLoadingForDrawables")
@Composable
fun RealTimeAqiChart(
    aqiData: List<AqiData>,
    modifier: Modifier = Modifier,
    updateInterval: Long = 5000
) {
    val context = LocalContext.current
    val chartRef = remember { mutableStateOf<LineChart?>(null) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    var lastProcessedData by remember { mutableStateOf<List<AqiData>>(emptyList()) }

    var lastUpdateTime by remember { mutableStateOf(0L) }
    // Only take last 12 points and memoize
    val displayData by remember(aqiData) {
        derivedStateOf { aqiData.takeLast(12) }
    }

    // Only update if data actually changed
    LaunchedEffect(displayData) {
        if (displayData != lastProcessedData) {
            lastProcessedData = displayData
            chartRef.value?.invalidate()
        }
    }

    // Prepare entries - memoized
    val entries by remember(displayData) {
        derivedStateOf {
            displayData.mapIndexed { index, data ->
                Entry(
                    index.toFloat(),
                    data.AQI?.toFloat() ?: 100f,
                    timeFormat.format(Date(data.Datatime?.toLong() ?: System.currentTimeMillis()))
                )
            }
        }
    }
    val xAxisLabels by remember(entries) {
        derivedStateOf {
            entries.map { it.data as? String ?: "" }
        }
    }

    Log.d("Chart", "Entries: $entries")

    LaunchedEffect(displayData) {
        val currentTime = System.currentTimeMillis()
        if (displayData != lastProcessedData || currentTime - lastUpdateTime >= updateInterval) {
            lastProcessedData = displayData
            lastUpdateTime = currentTime

            withContext(Dispatchers.Main) {
                chartRef.value?.let { chart ->
                    // Update chart data
                    val dataSet = LineDataSet(entries, "AQI").apply {
                        color = android.graphics.Color.parseColor("#4FC3F7")
                        lineWidth = 2.5f
                        setDrawCircles(true)
                        circleRadius = 4f
                        setDrawValues(false)
                        fillDrawable = context.getDrawable(R.drawable.aqi_gradient)
                        setDrawFilled(true)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }
                    chart.data = LineData(dataSet)

                    // Update x-axis formatter
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt().coerceIn(0, xAxisLabels.size - 1)
                            return xAxisLabels.getOrNull(index) ?: ""
                        }
                    }

                    // Animate only if data size changed
                    if (chart.data?.entryCount != entries.size) {
                        chart.animateX(1000, Easing.EaseInOutCubic)
                    }

                    chart.invalidate()
                }
            }
        }
    }



    AndroidView(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121212))
            .padding(start = 3.dp, end=2.dp, top=3.dp, bottom=5.dp),
        factory = { context ->
            LineChart(context).apply {
                // Basic setup
                setTouchEnabled(true)
                setPinchZoom(true)
                description.isEnabled = false
                legend.isEnabled = false

                // X-axis customization
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    textColor = android.graphics.Color.WHITE
                    setDrawGridLines(false)
                    typeface = Typeface.DEFAULT_BOLD
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt().coerceIn(0, entries.size - 1)
                            Log.d("Chart", "Formatted value: $value, ${entries[index]}")
                            return entries[index].data as? String ?: ""
                        }
                    }
                    // granularity = 1f // show all labels
                    // labelCount = entries.size.coerceAtMost(6)
                }

                // Left Y-axis (AQI)
                axisLeft.apply {
                    textColor = android.graphics.Color.WHITE
                    axisMinimum = 0f
                    axisMaximum = 300f
                    setLabelCount(6, true)
                    setDrawGridLines(true)
                    gridColor = android.graphics.Color.parseColor("#444444")
                    axisLineColor = android.graphics.Color.WHITE
                }

                // Right Y-axis disabled
                axisRight.isEnabled = false

                // Disable extra text
                setNoDataText("Loading AQI data...")
                setNoDataTextColor(android.graphics.Color.WHITE)

                // Store reference for updates
                chartRef.value = this
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(entries, "AQI").apply {
                // Styling the line
                color = android.graphics.Color.parseColor("#4FC3F7")
                lineWidth = 2.5f
                setDrawCircles(true)
                circleRadius = 4f
                circleHoleRadius = 2f
                circleColors = listOf(android.graphics.Color.parseColor("#4FC3F7"))
                setDrawCircleHole(true)
                setDrawValues(false)

                // Gradient fill
                fillDrawable = context.getDrawable(R.drawable.aqi_gradient)
                setDrawFilled(true)
                fillAlpha = 90

                // Enable cubic lines for smooth curves
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
            }

            val lineData = LineData(dataSet).apply {
                setValueTextColor(android.graphics.Color.WHITE)
                setValueTextSize(10f)
            }

            chart.data = lineData

            // Animate the update
            chart.animateX(1000, Easing.EaseInOutCubic)

            // Refresh
            chart.invalidate()
        }
    )
}

@Composable
fun RationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Permission Needed") },
        text = { Text("We need your location to show local air quality data") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Health Advisory Component
@Composable
fun HealthAdvisoryCard(currentAqi: Int) {
    val (title, description, color) = when (currentAqi) {
        in 0..50 -> Triple(
            "Good Air Quality",
            "Perfect day for outdoor activities! Enjoy the fresh air.",
            Color(0xFF4CAF50)
        )
        in 51..100 -> Triple(
            "Moderate",
            "Unusually sensitive people should consider reducing prolonged outdoor exertion.",
            Color(0xFFFFC107)
        )
        in 101..150 -> Triple(
            "Unhealthy for Sensitive Groups",
            "Children, elderly and those with respiratory issues should limit outdoor activities.",
            Color(0xFFFF9800)
        )
        in 151..200 -> Triple(
            "Unhealthy",
            "Everyone may begin to experience health effects. Limit outdoor activities.",
            Color(0xFFF44336)
        )
        else -> Triple(
            "Hazardous",
            "Health emergency - avoid all outdoor activities and keep windows closed.",
            Color(0xFF9C27B0)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Advisory",
                    tint = color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Advisory",
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

/*
@SuppressLint("NewApi")
@Composable
fun AqiCalendar(monthlyAqiData: Map<String, Int>) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Month Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous month")
            }

            Text(
                text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            IconButton(onClick = { selectedMonth = selectedMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next month")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Grid - Using simple FlowRow instead of LazyVerticalGrid
        Column(modifier = Modifier.fillMaxWidth()) {
            // Day headers (S, M, T, W, T, F, S)
            Row(modifier = Modifier.fillMaxWidth()) {
                DayOfWeek.values().forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Calendar days grid
            val firstDayOfWeek = selectedMonth.atDay(1).dayOfWeek.value % 7
            val daysInMonth = selectedMonth.lengthOfMonth()
            val totalWeeks = ceil((firstDayOfWeek + daysInMonth) / 7f).toInt()

            repeat(totalWeeks) { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { dayOfWeek ->
                        val dayIndex = week * 7 + dayOfWeek
                        if (dayIndex >= firstDayOfWeek && dayIndex < firstDayOfWeek + daysInMonth) {
                            val day = dayIndex - firstDayOfWeek + 1
                            val date = selectedMonth.atDay(day)
                            val dateKey = date.format(DateTimeFormatter.ISO_DATE)
                            val aqi = monthlyAqiData[dateKey] ?: 0

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .aspectRatio(1f)
                                    .background(
                                        color = if (aqi > 0) getAqiColor(aqi).copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = if (aqi > 0) 0.8f else 0.5f
                                        )
                                    )
                                    if (aqi > 0) {
                                        Text(
                                            text = aqi.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        } else {
                            // Empty cell for days outside current month
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

 */
@SuppressLint("NewApi", "UnrememberedMutableState")
@Composable
fun AqiCalendar(
    aqiHistory: List<AqiData>,
    modifier: Modifier = Modifier
) {
    /*
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    // Create synthetic dates for the AQI history data
    val monthlyAqiData by derivedStateOf {
        // Create date-based mapping starting from today and going backwards
        val today = LocalDate.now()
        val dateToAqi = mutableMapOf<String, Int>()

        // We'll assign each AQI data point to a date, starting from today and going back
        var currentDate = today
        for (aqiData in aqiHistory.take(31)) { // Limit to last 31 days (1 month)
            aqiData.AQI?.let { aqi ->
                dateToAqi[currentDate.toString()] = aqi
            }
            currentDate = currentDate.minusDays(1)
        }

        // Filter for the selected month
        dateToAqi.filterKeys { dateStr ->
            val date = LocalDate.parse(dateStr)
            date.year == selectedMonth.year && date.month == selectedMonth.month
        }
    }

     */
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    // Persistent mapping of dates to AQI values
    val dateToAqi = remember { mutableStateMapOf<String, Int>() }

    // Update the mapping when aqiHistory changes, but only for new dates
    LaunchedEffect(aqiHistory) {
        val today = LocalDate.now()
        var currentDate = today

        // Only update dates that haven't been set before
        aqiHistory.forEachIndexed { index, aqiData ->
            aqiData.AQI?.let { aqi ->
                val dateKey = currentDate.toString()
                if (!dateToAqi.containsKey(dateKey)) {
                    dateToAqi[dateKey] = aqi
                }
                currentDate = currentDate.minusDays(1)
            }
        }
    }

    // Filter for the selected month
    val monthlyAqiData by derivedStateOf {
        dateToAqi.filterKeys { dateStr ->
            val date = LocalDate.parse(dateStr)
            date.year == selectedMonth.year && date.month == selectedMonth.month
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Month Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { selectedMonth = selectedMonth.minusMonths(1) },
                enabled = selectedMonth.isAfter(YearMonth.of(2023, 1)) // Set your minimum date
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous month")
            }

            Text(
                text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            IconButton(
                onClick = { selectedMonth = selectedMonth.plusMonths(1) },
                enabled = selectedMonth.isBefore(YearMonth.now())
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next month")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Grid
        Column(modifier = Modifier.fillMaxWidth()) {
            // Day headers (S, M, T, W, T, F, S)
            Row(modifier = Modifier.fillMaxWidth()) {
                DayOfWeek.values().forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                }
            }

            // Calendar days grid
            val firstDayOfWeek = selectedMonth.atDay(1).dayOfWeek.value % 7
            val daysInMonth = selectedMonth.lengthOfMonth()
            val totalWeeks = ceil((firstDayOfWeek + daysInMonth) / 7f).toInt()

            repeat(totalWeeks) { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { dayOfWeek ->
                        val dayIndex = week * 7 + dayOfWeek
                        if (dayIndex >= firstDayOfWeek && dayIndex < firstDayOfWeek + daysInMonth) {
                            val day = dayIndex - firstDayOfWeek + 1
                            val date = selectedMonth.atDay(day)
                            val dateKey = date.toString()
                            val aqi = monthlyAqiData[dateKey] ?: 0

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .aspectRatio(1f)
                                    .background(
                                        color = if (aqi > 0) getAqiColor(aqi).copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = if (aqi > 0) 0.8f else 0.5f
                                        )
                                    )
                                    if (aqi > 0) {
                                        Text(
                                            text = aqi.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        } else {
                            // Empty cell for days outside current month
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

// Helper function to get color based on AQI value
fun getAqiColor(aqi: Int): Color {
    return when (aqi) {
        in 0..50 -> Color(0xFF4CAF50) // Good
        in 51..100 -> Color(0xFFFFEB3B) // Moderate
        in 101..150 -> Color(0xFFFF9800) // Unhealthy for sensitive groups
        in 151..200 -> Color(0xFFF44336) // Unhealthy
        in 201..300 -> Color(0xFF9C27B0) // Very Unhealthy
        else -> Color(0xFF880E4F) // Hazardous
    }
}

// Data class for monthly AQI
data class MonthlyAqiData(
    val date: LocalDate,
    val aqi: Int
)

@Composable
fun AqiCard(
    aqiValue: Int,
    temperature: Float,
    humidity: Float,
    modifier: Modifier = Modifier
) {
    val (color, status) = when (aqiValue) {
        in 0..50 -> Color(0xFF4CAF50) to "Good"
        in 51..100 -> Color(0xFFFFC107) to "Moderate"
        in 101..150 -> Color(0xFFFF9800) to "Unhealthy for Sensitive Groups"
        in 151..200 -> Color(0xFFF44336) to "Unhealthy"
        in 201..300 -> Color(0xFF9C27B0) to "Very Unhealthy"
        else -> Color(0xFF607D8B) to "Hazardous"
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column - Temperature and Humidity
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                WeatherParameter(
                    icon = Icons.Default.Thermostat,
                    value = "$temperature°C",
                    label = "Temperature"
                )

                Spacer(modifier = Modifier.height(12.dp))

                WeatherParameter(
                    icon = Icons.Default.WaterDrop,
                    value = "$humidity%",
                    label = "Humidity"
                )
            }

            // Right Column - AQI (larger and centered)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AQI",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = aqiValue.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = status,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun WeatherParameter(
    icon: ImageVector,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ParametersBreakdown(
    currentAqiData: AqiData?,
    aqiHistory: List<AqiData>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Convert AQI data to AirParameter list
    val parameters = remember(currentAqiData, aqiHistory) {
        if (currentAqiData == null) return@remember emptyList()

        listOf(
            AirParameter(
                name = "PM2.5",
                value = currentAqiData.PM25?.toFloat() ?: 0f,
                unit = "µg/m³",
                history = aqiHistory.mapNotNull { it.PM25?.toFloat() }.takeLast(9).map { value ->
                    (value / 80f) * 100f  // Normalize to 0-100 scale
                }
            ),
            AirParameter(
                name = "PM10",
                value = currentAqiData.PM10?.toFloat() ?: 0f,
                unit = "µg/m³",
                history = aqiHistory.mapNotNull { it.PM10?.toFloat() }.takeLast(9).map { value ->
                    (value / 160f) * 100f  // Normalize to 0-100 scale
                }
            ),
            AirParameter(
                name = "CO",
                value = currentAqiData.CO?.toFloat() ?: 0f,
                unit = "ppm",
                history = aqiHistory.mapNotNull { it.CO?.toFloat() }.takeLast(9).map { value ->
                    (value / 15f) * 100f  // Normalize to 0-100 scale
                }
            ),
            AirParameter(
                name = "NO2",
                value = currentAqiData.NO2?.toFloat() ?: 0f,
                unit = "ppb",
                history = aqiHistory.mapNotNull { it.NO2?.toFloat() }.takeLast(9).map { value ->
                    (value / 80f) * 100f  // Normalize to 0-100 scale
                }
            ),
            AirParameter(
                name = "O3",
                value = currentAqiData.O3?.toFloat() ?: 0f,
                unit = "ppb",
                history = aqiHistory.mapNotNull { it.O3?.toFloat() }.takeLast(9).map { value ->
                    (value / 150f) * 100f  // Normalize to 0-100 scale
                }
            ),
            AirParameter(
                name = "SO2",
                value = currentAqiData.SO2?.toFloat() ?: 0f,
                unit = "ppb",
                history = aqiHistory.mapNotNull { it.SO2?.toFloat() }.takeLast(9).map { value ->
                    (value / 40f) * 100f  // Normalize to 0-100 scale
                }
            )
        ).filter { parameter ->
            // Only show parameters with valid current values
            parameter.value > 0 || parameter.history.any { it > 0 }
        }
    }

    if (parameters.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (currentAqiData == null) "Loading air quality data..."
                else "No parameter data available",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        Row(
            modifier = modifier
                .horizontalScroll(scrollState)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            parameters.forEach { parameter ->
                ParameterCard(parameter = parameter)
            }
        }
    }
}

fun normalizeValue(value: Float, maxValue: Float): Float {
    return value.coerceIn(0f, maxValue) / maxValue
}

fun enhanceVariation(data: List<Float>): List<Float> {
    if (data.size < 2) return data
    return data.mapIndexed { index, value ->
        // Add small random fluctuations (2-5% of value)
        val fluctuation = when {
            index == 0 -> (data[1] - value) * 0.2f
            index == data.size - 1 -> (value - data[index-1]) * 0.2f
            else -> (data[index+1] - data[index-1]) * 0.1f
        }
        (value + fluctuation).coerceIn(0f, 1f)
    }
}

/*
@Composable
fun ParameterCard(parameter: AirParameter) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = parameter.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = parameter.value.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = parameter.unit,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Mini Sparkline Graph Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Here you would implement your actual sparkline graph
                // For now just showing a placeholder
                Text(
                    text = "Graph",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 10.sp
                )
            }
        }
    }
}
*/

@Composable
fun ParameterCard(
    parameter: AirParameter,
    modifier: Modifier = Modifier
) {
    Log.d("Parameter", "ParameterCard: $parameter")
    val isDarkTheme = isSystemInDarkTheme()
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    Card(
        modifier = modifier
            .width(120.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Parameter name (top left)
            Text(
                text = parameter.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            // Main content row (value + icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Value and unit column
                Column {
                    Text(
                        text = "%.1f".format(parameter.value),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = parameter.unit,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Icon on the right
                Icon(
                    imageVector = getAirQualityIcon(parameter.name),
                    contentDescription = parameter.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                        .padding(end=8.dp)

                )
            }


                MPAndroidSparkline(
                    data = generateRandomSparklineData(),
                    currentValue = parameter.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
        }
    }
}

fun generateRandomSparklineData(count: Int = 7): List<Float> {
    return List(count) {
        Random.nextFloat().coerceIn(0.1f, 0.9f)
    }
}
fun getAirQualityIcon(parameterName: String): ImageVector {
    return when (parameterName.lowercase()) {


        "pm2.5" -> Icons.Filled.Cloud
        "pm10" -> Icons.Filled.Air
        "o3" -> Icons.Filled.WbSunny
        "co" -> Icons.Filled.LocalGasStation
        "no2" -> Icons.Filled.Factory
        else -> Icons.Filled.Eco // Default for unknown parameters
    }
}

/*
@Composable
fun MPAndroidSparkline(
    data: List<Float>,
    currentValue: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chartColors = remember {
        listOf(
            ContextCompat.getColor(context, R.color.chart_line),
            ContextCompat.getColor(context, R.color.chart_fill_start),
            ContextCompat.getColor(context, R.color.chart_fill_end)
        )
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                // Basic setup
                setTouchEnabled(false)
                setPinchZoom(false)
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                setNoDataText("")

                // X-axis customization
                xAxis.apply {
                    setDrawLabels(false)
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    spaceMin = 0.1f  // Reduce space on sides
                    spaceMax = 0.1f
                    granularity = 1f
                }

                // Left Y-axis
                axisLeft.apply {
                    setDrawLabels(false)
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    axisMinimum = 0f
                    spaceTop = 0f
                    spaceBottom = 0f
                    granularity = 1f
                }

                // Right Y-axis disabled
                axisRight.isEnabled = false

                // Configure data set
                val entries = data.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }

                val dataSet = LineDataSet(entries, "").apply {
                    color = chartColors[0]
                    lineWidth = 1.5f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.2f

                    // Gradient fill
                    fillDrawable = ContextCompat.getDrawable(context, R.drawable.chart_gradient)
                    setDrawFilled(true)
                    fillAlpha = 90
                }


                this.data = LineData(dataSet).apply {
                    setDrawValues(false)
                    setValueTextSize(0f)
                }

                // Additional settings to minimize space
                minOffset = 0f
                extraLeftOffset = 0f
                extraRightOffset = 0f
                extraTopOffset = 0f
                extraBottomOffset = 0f

                // Disable all chart padding
                setPadding(0, 0, 0, 0)
                animateX(1000, Easing.EaseInOutCubic)
            }
        },
        modifier = modifier
    )
}

 */

@Composable
fun MPAndroidSparkline(
    data: List<Float>,
    currentValue: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chartColors = remember {
        listOf(
            ContextCompat.getColor(context, R.color.chart_line),
            ContextCompat.getColor(context, R.color.chart_fill_start),
            ContextCompat.getColor(context, R.color.chart_fill_end)
        )
    }
    Log.d("Sparkline", "value: $currentValue")

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                // Basic setup
                setTouchEnabled(false)
                setPinchZoom(false)
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                setNoDataText("")

                // X-axis customization
                xAxis.apply {
                    setDrawLabels(false)
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    spaceMin = 0.1f  // Reduce space on sides
                    spaceMax = 0.1f
                    granularity = 1f
                }

                // Left Y-axis
                axisLeft.apply {
                    setDrawLabels(false)
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    axisMinimum = 0f
                    spaceTop = 0f
                    spaceBottom = 0f
                    granularity = 1f
                }

                // Right Y-axis disabled
                axisRight.isEnabled = false

                // Configure data set
                val entries = data.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }

                val dataSet = LineDataSet(entries, "").apply {
                    color = chartColors[0]
                    lineWidth = 1.5f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    cubicIntensity = 0.2f

                    // Gradient fill
                    fillDrawable = ContextCompat.getDrawable(context, R.drawable.chart_gradient)
                    setDrawFilled(true)
                    fillAlpha = 90
                }


                this.data = LineData(dataSet).apply {
                    setDrawValues(false)
                    setValueTextSize(0f)
                }

                // Additional settings to minimize space
                minOffset = 0f
                extraLeftOffset = 0f
                extraRightOffset = 0f
                extraTopOffset = 0f
                extraBottomOffset = 0f

                // Disable all chart padding
                setPadding(0, 0, 0, 0)
                animateX(1000, Easing.EaseInOutCubic)
            }
        },
        modifier = modifier
    )
}

@Composable
fun LocationSelector(
    locationName: String,
    onSearchClicked: () -> Unit,
    onLocationClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Current Location
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onLocationClicked) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Current Location"
                )
            }
            Text(
                text = locationName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }

        // Actions
        Row {
            IconButton(onClick = onSearchClicked) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
            IconButton(onClick = onProfileClicked) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            }
        }
    }
}

// Data classes
data class AirParameter(
    val name: String,
    val value: Float,
    val unit: String,
    val history: List<Float>,
    val timestamps: List<Long> = emptyList()
)



@Preview(
    name = "Mobile Preview",
    device = "id:pixel_9_pro"
)
@Composable
fun PreviewMyComposable() {
    val sampleParameters = listOf(
        AirParameter(
            "PM2.5", 12.4f, "µg/m³",
            history = listOf(32f, 30f, 28f, 25f, 26f, 24f, 22f, 25f, 28f),
            timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
        ),
        AirParameter(
            "PM10", 24.0f, "µg/m³",
            history = listOf(32f, 30f, 28f, 25f, 36f, 24f, 32f, 25f, 28f),
            timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
        ),
        AirParameter(
            "O₃", 0.042f, "ppm",
            history = listOf(32f, 30f, 28f, 25f, 26f, 24f, 22f, 25f, 28f),
            timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
        ),
        AirParameter(
            "CO", 1.2f, "ppm",
            history = listOf(32f, 30f, 28f, 25f, 26f, 24f, 22f, 25f, 28f),
            timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
        ),
        AirParameter(
            "NO₂", 0.023f, "ppm",
            history = listOf(32f, 30f, 28f, 25f, 26f, 24f, 22f, 25f, 28f),
            timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
        )
    )
    val monthlyAqiData = mapOf(
        "2025-03-01" to 45,
        "2025-03-02" to 68,
        "2025-04-03" to 102,
        "2025-03-04" to 145,
        "2025-03-05" to 67,
        "2025-04-06" to 82,
        "2025-03-07" to 65,
        "2025-03-08" to 88,
        "2025-04-09" to 120,
    )

}
