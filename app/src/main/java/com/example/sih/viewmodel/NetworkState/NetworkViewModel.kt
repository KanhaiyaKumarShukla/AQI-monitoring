package com.example.sih.viewmodel.NetworkState

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : ViewModel() {
    private val _networkStatus = MutableStateFlow(true) // Assume connected by default
    val networkStatus: StateFlow<Boolean> = _networkStatus

    init {
        checkNetworkStatus()
        setupNetworkCallback()
    }

    fun checkNetworkStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        val isConnected = activeNetwork != null &&
                connectivityManager.getNetworkCapabilities(activeNetwork)?.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) == true

        _networkStatus.value = isConnected
    }

    private fun setupNetworkCallback() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkStatus.value = true
            }

            override fun onLost(network: Network) {
                _networkStatus.value = false
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }
}