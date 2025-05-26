package com.example.sih.ui.screens.admin

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sih.util.Station
import com.example.sih.util.Station.StationStatus
import com.example.sih.viewmodel.AuthViewModel
import com.example.sih.viewmodel.ProfileViewModel
import com.example.sih.viewmodel.UserProfile
import com.example.sih.viewmodel.admin.RoleManagementViewModel
import com.example.sih.viewmodel.admin.StationViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    roleViewModel: RoleManagementViewModel = hiltViewModel(),
    stationViewModel: StationViewModel = hiltViewModel()
) {
    var showAppointDialog by remember { mutableStateOf(false) }
    var newTechEmail by remember { mutableStateOf("") }
    var showRemoveDialog by remember { mutableStateOf<String?>(null) }
    var expandedTechnician by remember { mutableStateOf<String?>(null) }
    var selectedStations by remember { mutableStateOf<Set<String>>(emptySet()) }

    val currentUser by authViewModel.userProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val snackbarMessage by roleViewModel.snackbarMessage.collectAsState()
    val stationSnackbarMessage by stationViewModel.snackbarMessage.collectAsState()
    val isLoading by roleViewModel.isLoading.collectAsState()
    val stations by stationViewModel.stations.collectAsState()

    // Fetch technicians when screen loads
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            user.uid.let { managerId ->
                roleViewModel.fetchTechniciansForManager(managerId)
            }
            user.assignedState?.let { state ->
                stationViewModel.fetchStationsForState(state)
            }
        }
    }

    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            roleViewModel.clearSnackbar()
        }
    }
    LaunchedEffect(stationSnackbarMessage) {
        stationSnackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            stationViewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        modifier = Modifier.padding(8.dp),
                        action = {
                            data.visuals.actionLabel?.let { actionLabel ->
                                TextButton(onClick = { data.dismiss() }) {
                                    Text(actionLabel)
                                }
                            }
                        }
                    ) {
                        Text(data.visuals.message)
                    }
                }
            )
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Manager Dashboard",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Logging out...")
                                authViewModel.logout()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .padding(end = 12.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("manager_station_map") },
                    modifier = Modifier.wrapContentSize(),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "View Map",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stations Map",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                ExtendedFloatingActionButton(
                    onClick = { showAppointDialog = true },
                    modifier = Modifier.wrapContentSize(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Appoint Technician",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "Appoint Technician",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
        ) {
            // Technicians List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Technician Management",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${roleViewModel.technicians.value.size} technicians",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (roleViewModel.technicians.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "No Technicians",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No technicians assigned yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Click the + button to appoint one",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    items(roleViewModel.technicians.value) { technician ->
                        TechnicianCard(
                            technician = technician,
                            stations = stations,
                            isExpanded = expandedTechnician == technician.uid,
                            onClick = {
                                expandedTechnician = if (expandedTechnician == technician.uid) null else technician.uid
                            },
                            onRemove = { showRemoveDialog = technician.uid },
                            onMessage = {
                                stationViewModel.sendNotification()
                            },
                            onViewProfile = { navController.navigate("user_profile/${technician.uid}") },
                            stationViewModel = stationViewModel
                        )
                    }
                }
            }
        }
    }

    // Appoint Technician Dialog
    if (showAppointDialog && currentUser != null) {
        AppointTechnicianDialog(
            email = newTechEmail,
            stations = stations,
            selectedStations = selectedStations,
            onEmailChange = { newTechEmail = it },
            onStationSelect = { stationId ->
                selectedStations = if (selectedStations.contains(stationId)) {
                    selectedStations - stationId
                } else {
                    selectedStations + stationId
                }
            },
            onAppoint = {
                roleViewModel.appointTechnician(
                    newTechEmail,
                    currentUser!!.uid,
                    selectedStations.toList()
                ) { success, message ->
                    if (success) {
                        showAppointDialog = false
                        selectedStations = emptySet()
                        newTechEmail = ""
                    }
                }
            },
            onDismiss = {
                showAppointDialog = false
                selectedStations = emptySet()
                newTechEmail = ""
            }
        )
    }

    // Remove Technician Dialog
    showRemoveDialog?.let { technicianId ->
        AlertDialog(
            onDismissRequest = { showRemoveDialog = null },
            title = {
                Text(
                    "Remove Technician",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove this technician?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        roleViewModel.removeTechnician(technicianId, currentUser!!.uid){ success, message ->
                            if (success) {
                                showAppointDialog = false
                            }
                        }
                        showRemoveDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.padding(end = 8.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "Remove",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showRemoveDialog = null },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            titleContentColor = MaterialTheme.colorScheme.error,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TechnicianCard(
    technician: UserProfile,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onMessage: () -> Unit,
    onViewProfile: () -> Unit,
    stations: List<Station> = emptyList(),
    stationViewModel: StationViewModel
) {
    val elevation = if (isExpanded) 8.dp else 4.dp
    val shape = RoundedCornerShape(12.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(elevation),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            // Header Row (unchanged)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = technician.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = technician.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = technician.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Expand/Collapse Icon
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expanded Content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // Assigned Stations Section
                if (technician.stations.isNotEmpty()) {
                    Text(
                        text = "ASSIGNED STATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Fixed: Replace LazyColumn with Column and add fixed height
                    Column(
                        modifier = Modifier
                            .heightIn(max = 200.dp) // Constrain maximum height
                            .verticalScroll(rememberScrollState()) // Add scroll if content exceeds height
                    ) {
                        technician.stations.forEach { stationId ->
                            val station = stations.find { it.stationId == stationId }
                            StationItem(
                                station = station,
                                stationId = stationId,
                                modifier = Modifier.padding(vertical = 4.dp),
                                onStatusChange = { newStatus ->
                                    station?.stationId?.let { id ->
                                        stationViewModel.updateStationStatus(id, newStatus)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No stations assigned",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons (unchanged)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledTonalButton(
                        onClick = onViewProfile,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Profile",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }

                    FilledTonalButton(
                        onClick = onMessage,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Message",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Message",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onRemove,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Remove",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/*
@Composable
fun StationItem(
    station: Station?,
    stationId: String,
    modifier: Modifier = Modifier
) {
    val statusColor = when(station?.status) {
        Station.StationStatus.Active -> MaterialTheme.colorScheme.tertiary
        Station.StationStatus.UnderMaintenance -> MaterialTheme.colorScheme.secondary
        Station.StationStatus.Inactive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station?.stationName ?: "Station $stationId",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stationId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = station?.status?.name ?: "Unknown",
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

 */
@Composable
fun StationItem(
    station: Station?,
    stationId: String,
    modifier: Modifier = Modifier,
    onStatusChange: (Station.StationStatus) -> Unit = {}
) {
    val statusColor = when(station?.status) {
        Station.StationStatus.Active -> MaterialTheme.colorScheme.tertiary
        Station.StationStatus.UnderMaintenance -> MaterialTheme.colorScheme.secondary
        Station.StationStatus.Inactive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    val statusOptions = remember {
        Station.StationStatus.values().associateWith { status ->
            when(status) {
                Station.StationStatus.Active -> "Active"
                Station.StationStatus.UnderMaintenance -> "Maintenance"
                Station.StationStatus.Inactive -> "Inactive"
            }
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Station Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Station Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station?.stationName ?: "Station $stationId",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = station?.address ?: "Unknown location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Current Status Badge
                Text(
                    text = station?.status?.let { statusOptions[it] } ?: "Unknown",
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Change Buttons
            if (station != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StationStatus.values().forEach { status ->
                        val isCurrentStatus = status == station.status
                        val buttonColors = if (isCurrentStatus) {
                            ButtonDefaults.buttonColors(
                                containerColor = statusColor.copy(alpha = 0.2f),
                                contentColor = statusColor
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(
                            onClick = { if (!isCurrentStatus) onStatusChange(status) },
                            colors = buttonColors,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isCurrentStatus
                        ) {
                            Text(
                                text = statusOptions[status] ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointTechnicianDialog(
    email: String,
    stations: List<Station>,
    selectedStations: Set<String>,
    onEmailChange: (String) -> Unit,
    onStationSelect: (String) -> Unit,
    onAppoint: () -> Unit,
    onDismiss: () -> Unit
) {
    var emailError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Appoint New Technician",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        onEmailChange(it)
                        emailError = if (it.isBlank()) "Email is required" else null
                    },
                    label = { Text("Technician Email") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Assign Stations (${selectedStations.size} selected)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (stations.isEmpty()) {
                    Text(
                        text = "No stations available in your state",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Column {
                        stations.forEach { station ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onStationSelect(station.stationId) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Checkbox(
                                    checked = selectedStations.contains(station.stationId),
                                    onCheckedChange = { onStationSelect(station.stationId) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Column {
                                    Text(
                                        text = station.displayName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = station.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isBlank()) {
                        emailError = "Email is required"
                        return@Button
                    }
                    if (selectedStations.isEmpty()) {
                        // You might want to show an error here or make it optional
                        return@Button
                    }
                    onAppoint()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && selectedStations.isNotEmpty(),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text("Appoint Technician")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}