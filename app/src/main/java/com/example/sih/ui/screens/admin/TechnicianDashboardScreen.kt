package com.example.sih.ui.screens.admin

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sih.util.Station
import com.example.sih.util.Station.StationStatus
import com.example.sih.viewmodel.AuthViewModel
import com.example.sih.viewmodel.admin.StationViewModel
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    stationViewModel: StationViewModel = hiltViewModel()
) {
    val currentUser = authViewModel.currentUser
    val stations by stationViewModel.stations.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by stationViewModel.snackbarMessage.collectAsState()
    val isLoading by stationViewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { techId ->
            stationViewModel.fetchStationsForTechnician(techId)
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                stationViewModel.clearSnackbar()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Technician Dashboard") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Your Assigned Stations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Monitor and update the status of your assigned stations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (stations.isEmpty()) {
                    EmptyStationsView()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(stations) { station ->
                            StationStatusCard(
                                station = station,
                                onStatusChange = { newStatus ->
                                    stationViewModel.updateStationStatus(station.stationId, newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStationsView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "No Stations Assigned",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You haven't been assigned any stations yet. Please contact your manager.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StationStatusCard(
    station: Station,
    onStatusChange: (StationStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = station.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = station.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Show less" else "Show more"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Current Status",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StationStatus.values().forEach { status ->
                            val statusText = when (status) {
                                StationStatus.Active -> "Active"
                                StationStatus.Inactive -> "Inactive"
                                StationStatus.UnderMaintenance -> "Maintenance"
                            }
                            val statusIcon = when (status) {
                                StationStatus.Active -> Icons.Default.CheckCircle
                                StationStatus.Inactive -> Icons.Default.Cancel
                                StationStatus.UnderMaintenance -> Icons.Default.Build
                            }
                            FilterChip(
                                selected = station.status == status,
                                onClick = { 
                                    if (!isUpdating) {
                                        isUpdating = true
                                        onStatusChange(status)
                                        isUpdating = false
                                    }
                                },
                                enabled = !isUpdating,
                                label = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 1.dp)
                                    ) {
                                        if (isUpdating && station.status == status) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = statusIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (status) {
                                        StationStatus.Active -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                        StationStatus.Inactive -> Color(0xFFF44336).copy(alpha = 0.2f)
                                        StationStatus.UnderMaintenance -> Color(0xFFFFC107).copy(alpha = 0.2f)
                                    },
                                    selectedLabelColor = when (status) {
                                        StationStatus.Active -> Color(0xFF4CAF50)
                                        StationStatus.Inactive -> Color(0xFFF44336)
                                        StationStatus.UnderMaintenance -> Color(0xFFFFC107)
                                    }
                                ),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}