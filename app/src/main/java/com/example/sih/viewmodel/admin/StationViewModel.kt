package com.example.sih.viewmodel.admin

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih.repository.StationRepository
import com.example.sih.util.Station
import com.example.sih.util.Station.StationStatus
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class StationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: StationRepository
) : ViewModel() {

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _selectedStation = MutableStateFlow<Station?>(null)
    val selectedStation: StateFlow<Station?> = _selectedStation.asStateFlow()

    private val _geocodingInProgress = MutableStateFlow(false)
    val geocodingInProgress: StateFlow<Boolean> = _geocodingInProgress.asStateFlow()

    fun fetchStationsForTechnician(technicianId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // First get the technician's profile to get their assigned stations
                val technicianProfile = firestore.collection("profiles")
                    .document(technicianId)
                    .get()
                    .await()

                val assignedStationIds = technicianProfile.get("stations") as? List<String> ?: emptyList()
                
                if (assignedStationIds.isEmpty()) {
                    _stations.value = emptyList()
                    _snackbarMessage.value = "No stations assigned"
                    return@launch
                }

                // Fetch all assigned stations
                val stationsSnapshot = firestore.collection("stations")
                    .whereIn("stationId", assignedStationIds)
                    .get()
                    .await()

                val stations = stationsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Station::class.java)?.copy(stationId = doc.id)
                }
                
                Log.d("admin", "StationViewModel, fetchStationsForTechnician: $stations")
                _stations.value = geocodeStationsIfNeeded(stations)
            } catch (e: Exception) {
                Log.e("admin", "fetchStationsForTechnician error: ${e.message}")
                _snackbarMessage.value = "Failed to load stations: ${e.message}"
                _stations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun fetchStationsForState(state: String?) {
        if (state.isNullOrEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Using repository instead of direct Firestore access
                val stations = repository.getStationsByState(state)
                _stations.value = geocodeStationsIfNeeded(stations)
            } catch (e: Exception) {
                Log.d("manager", "fetchStationsForState error: ${e.message}")
                _snackbarMessage.value = "Failed to load stations: ${e.message}"
                _stations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    private suspend fun geocodeStationsIfNeeded(stations: List<Station>): List<Station> {
        _geocodingInProgress.value = true
        return try {
            stations.map { station ->
                if (station.location == null) {
                    repository.geocodeStationLocation(station)
                } else {
                    station
                }
            }
        } catch (e: Exception) {
            Log.e("StationViewModel", "Geocoding error", e)
            stations // Return original stations if geocoding fails
        } finally {
            _geocodingInProgress.value = false
        }
    }


    fun updateStationStatus(stationId: String, newStatus: StationStatus) {
        viewModelScope.launch {
            try {
                // Update local state immediately
                _stations.value = _stations.value.map { station ->
                    if (station.stationId == stationId) {
                        station.copy(status = newStatus)
                    } else {
                        station
                    }
                }

                // Update Firestore
                firestore.collection("stations").document(stationId)
                    .update("status", newStatus.name)
                    .await()

                _snackbarMessage.value = "Status updated successfully"
            } catch (e: Exception) {
                // Revert local state if Firestore update fails
                _stations.value = _stations.value.map { station ->
                    if (station.stationId == stationId) {
                        station.copy(status = station.status) // Revert to original status
                    } else {
                        station
                    }
                }
                Log.e("admin", "StationViewModel, updateStationStatus error: ${e.message}")
                _snackbarMessage.value = "Failed to update status: ${e.message}"
            }
        }
    }
    fun selectStation(station: Station) {
        _selectedStation.value = station
    }
    fun clearSelection() {
        _selectedStation.value = null
    }

    fun sendNotification() {
        viewModelScope.launch {
            try {
                _selectedStation.value?.stationId?.let { stationId ->
                    repository.sendNotification(stationId)
                    _snackbarMessage.value = "Notification sent successfully"
                }
            } catch (e: Exception) {
                Log.d("admin", "sendNotification error: ${e.message}")
                _snackbarMessage.value = "Failed to send notification: ${e.message}"
            }
        }
    }
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}