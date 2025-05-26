package com.example.sih.ui.screens.network

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sih.viewmodel.NetworkState.NetworkViewModel

@Composable
fun NetworkAwareScaffold(
    networkViewModel: NetworkViewModel = hiltViewModel(),
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val networkStatus by networkViewModel.networkStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        bottomBar = { PersistentNetworkIndicator() },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (networkStatus) {
            content(padding)
        } else {
            NetworkErrorScreen(
                modifier = Modifier.padding(padding),
                onRetry = { networkViewModel.checkNetworkStatus() }
            )
        }
    }
}
