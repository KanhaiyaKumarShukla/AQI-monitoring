package com.example.sih.ui.screens.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sih.viewmodel.NetworkState.NetworkViewModel

@Composable
fun PersistentNetworkIndicator() {
    val networkViewModel: NetworkViewModel = hiltViewModel()
    val networkStatus by networkViewModel.networkStatus.collectAsState()

    if (!networkStatus) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SignalWifiOff,
                    contentDescription = "No Internet",
                    tint = MaterialTheme.colorScheme.onError
                )
                Text(
                    text = "No Internet Connection",
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}