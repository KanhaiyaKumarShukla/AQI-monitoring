package com.example.sih.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sih.ui.theme.ThemeColor
import com.example.sih.viewmodel.AuthViewModel
import com.example.sih.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // States
    val darkMode by settingsViewModel.darkMode.collectAsState()
    val themeColor by settingsViewModel.themeColor.collectAsState()
    val privacyEnabled by settingsViewModel.privacyEnabled.collectAsState()
    val appVersion by settingsViewModel.appVersion.collectAsState()
    val cacheSize by settingsViewModel.cacheSize.collectAsState()

    // For dialogs
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showUpdateEmailDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Error states
    val passwordError by settingsViewModel.passwordChangeError.collectAsState()
    val emailError by settingsViewModel.emailUpdateError.collectAsState()


    LaunchedEffect(passwordError) {
        passwordError?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.clearPasswordError()
        }
    }

    LaunchedEffect(emailError) {
        emailError?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.clearEmailError()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Account Settings
            item { SectionHeader("Account") }

            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = { showChangePasswordDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Update Email Address",
                    onClick = { showUpdateEmailDialog = true }
                )
            }

            // Privacy
            item { SectionHeader("Privacy") }

            item {
                SwitchSettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Mode",
                    checked = privacyEnabled,
                    onCheckedChange = { settingsViewModel.setPrivacyEnabled(it) }
                )
            }

            // Appearance
            item { SectionHeader("Appearance") }

            item {
                SwitchSettingsItem(
                    icon = if (darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Dark Mode",
                    checked = darkMode,
                    onCheckedChange = { settingsViewModel.setDarkMode(it) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme Color",
                    value = themeColor.displayName,
                    onClick = { showThemeDialog = true }
                )
            }

            // Data & Storage
            item { SectionHeader("Data & Storage") }

            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Clear Cache",
                    value = cacheSize,
                    onClick = {
                        settingsViewModel.clearCache()
                        scope.launch {
                            snackbarHostState.showSnackbar("Cache cleared successfully")
                        }
                    }
                )
            }

            // Help & Support
            item { SectionHeader("Help & Support") }

            item {
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help Center",
                    onClick = { onNavigate("help") }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Terms & Privacy",
                    onClick = { onNavigate("terms") }
                )
            }

            // About
            item { SectionHeader("About") }

            item {
                SettingsItem(
                    icon = Icons.Default.Android,
                    title = "App Version",
                    value = appVersion,
                    onClick = {}
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Rate the App",
                    onClick = { /* Open Play Store */ }
                )
            }
        }
    }

    // Dialogs
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChangePassword = { current, new ->
                settingsViewModel.changePassword(current, new)
                showChangePasswordDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Password changed successfully")
                }
            }
        )
    }

    if (showUpdateEmailDialog) {
        UpdateEmailDialog(
            currentEmail = authViewModel.currentUser?.email ?: "",
            onDismiss = { showUpdateEmailDialog = false },
            onUpdateEmail = { newEmail ->
                settingsViewModel.updateEmail(newEmail)
                showUpdateEmailDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Email updated successfully")
                }
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeColor,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                settingsViewModel.setThemeColor(theme)
                showThemeDialog = false
            }
        )
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: (current: String, new: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        error = "Passwords don't match"
                    } else if (newPassword.length < 6) {
                        error = "Password must be at least 6 characters"
                    } else {
                        onChangePassword(currentPassword, newPassword)
                    }
                }
            ) {
                Text("Change Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UpdateEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onUpdateEmail: (String) -> Unit
) {
    var newEmail by remember { mutableStateOf(currentEmail) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Email Address") },
        text = {
            Column {
                Text("Current email: $currentEmail")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("New Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        error = "Please enter a valid email"
                    } else {
                        onUpdateEmail(newEmail)
                    }
                }
            ) {
                Text("Update Email")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeColor,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeColor) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                ThemeColor.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = theme.displayName)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SwitchSettingsItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}