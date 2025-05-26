package com.example.sih.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih.repository.StationRepository
import com.example.sih.util.Station
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StationMapViewModel @Inject constructor(
    private val repository: StationRepository
) : ViewModel() {

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    private val _visibleStations = MutableStateFlow<List<Station>>(emptyList())
    val visibleStations: StateFlow<List<Station>> = _visibleStations.asStateFlow()

    private val _selectedStation = MutableStateFlow<Station?>(null)
    val selectedStation: StateFlow<Station?> = _selectedStation.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _initialDataLoaded = MutableStateFlow(false)
    val initialDataLoaded: StateFlow<Boolean> = _initialDataLoaded.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getStationsStream()
                .mapLatest { stations ->
                    _isLoading.value = true
                    _loadingProgress.value = 0f

                    val totalStations = stations.size
                    val processedStations = mutableListOf<Station>()

                    // Process in parallel with limited concurrency
                    stations.chunked(5).forEachIndexed { index, chunk ->
                        // Process each chunk in parallel
                        val processedChunk = chunk.map { station ->
                            async(Dispatchers.IO) {
                                if (station.location == null) {
                                    repository.geocodeStationLocation(station)
                                } else {
                                    station
                                }
                            }
                        }.awaitAll()

                        processedStations.addAll(processedChunk)
                        _visibleStations.value = processedStations.toList()

                        // Update progress
                        _loadingProgress.value = processedStations.size.toFloat() / totalStations.toFloat()

                        // Mark initial load complete after first chunk
                        if (index == 0) {
                            _initialDataLoaded.value = true
                        }
                    }

                    processedStations
                }
                .collect { stations ->
                    _stations.value = stations
                    _isLoading.value = false
                    _loadingProgress.value = 1f
                }
        }
    }

    fun fetchStationsForState(state: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _visibleStations.value = repository.getStationsByState(state)
            } catch (e: Exception) {
                // Handle error
                _visibleStations.value = emptyList()
            } finally {
                _isLoading.value = false
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
            _selectedStation.value?.stationId?.let { stationId ->
                repository.sendNotification(stationId)
            }
        }
    }
}