package com.example.sih.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sih.repository.AqiRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sih.util.AqiData
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

/*
@HiltViewModel
class AqiViewModel @Inject constructor(application: Application) : ViewModel() {
    private val repository = AqiRepository(application)
    private val _lastAqiData = MutableLiveData<AqiData?>()
    val lastAqiData: LiveData<AqiData?> get() = _lastAqiData

    private val _currentLocation = mutableStateOf(LocationData())
    val currentLocation: State<LocationData> = _currentLocation

    val updatedAqi : LiveData<NetworkResult<AqiData>>
    get() = repository.aqi

    fun saveAqiData(aqiData: AqiData) {
        repository.saveAqiData(aqiData)
        _lastAqiData.value = aqiData
    }
    init {
        viewModelScope.launch {
            repository.getAqi()
        }
    }

    fun loadLastAqiData() {
        _lastAqiData.value = repository.getLastAqiData()
    }

    fun postAqi(userData: AqiData, onResult: (AqiData?) -> Unit){
        viewModelScope.launch {
            repository.postAqi(userData, onResult)
        }
    }
    class AqiViewModel : ViewModel() {
        // Current location state
    private val _currentLocation = mutableStateOf(LocationData())
    val currentLocation: State<LocationData> = _currentLocation

    // Other states (AQI, weather, etc.)
    private val _currentAqi = mutableStateOf(0)
    val currentAqi: State<Int> = _currentAqi

    private val _temperature = mutableStateOf(0f)
    val temperature: State<Float> = _temperature

    private val _humidity = mutableStateOf(0f)
    val humidity: State<Float> = _humidity

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Update location and fetch new data
    fun updateLocation(name: String, latLng: LatLng) {
        _isLoading.value = true

        // Update location
        _currentLocation.value = LocationData(
            name = name,
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )

        // Fetch new AQI and weather data for this location
        viewModelScope.launch {
            try {
                // Fetch AQI data (mock implementation - replace with real API call)
                val aqiResponse = fetchAqiData(latLng.latitude, latLng.longitude)
                _currentAqi.value = aqiResponse.aqi

                // Fetch weather data (mock implementation)
                val weatherResponse = fetchWeatherData(latLng.latitude, latLng.longitude)
                _temperature.value = weatherResponse.temperature
                _humidity.value = weatherResponse.humidity

            } catch (e: Exception) {
                // Handle errors (you might want to show a snackbar)
                Log.e("AQIViewModel", "Error fetching data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Mock data classes and functions - replace with real implementations
    private suspend fun fetchAqiData(lat: Double, lng: Double): AqiResponse {
        // Simulate network delay
        delay(1000)
        return AqiResponse(
            aqi = (0..500).random(), // Random AQI for demo
            components = mapOf(
                "pm2_5" to (0.0..50.0).random(),
                "pm10" to (0.0..100.0).random(),
                "o3" to (0.0..200.0).random()
            )
        )
    }

    private suspend fun fetchWeatherData(lat: Double, lng: Double): WeatherResponse {
        delay(800)
        return WeatherResponse(
            temperature = (-10f..40f).random(),
            humidity = (0f..100f).random()
        )
    }
}

// Data classes
data class LocationData(
    val name: String = "Unknown Location",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class AqiResponse(
    val aqi: Int,
    val components: Map<String, Double>
)

data class WeatherResponse(
    val temperature: Float,
    val humidity: Float
)

 */


@HiltViewModel
class AqiViewModel @Inject constructor(
    private val repository: AqiRepository
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _currentAqiData = MutableStateFlow<AqiData?>(null)
    val currentAqiData: StateFlow<AqiData?> = _currentAqiData.asStateFlow()

    private val _aqiHistory = MutableStateFlow<List<AqiData>>(emptyList())
    @OptIn(FlowPreview::class)
    val aqiHistory: StateFlow<List<AqiData>> = _aqiHistory
        .debounce(1000) // Add 1-second debounce
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var updateTimer: Timer? = null

    private val _currentCity = MutableStateFlow("")
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _isCityChanging = MutableStateFlow(false)
    val isCityChanging: StateFlow<Boolean> = _isCityChanging.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var timer: Timer? = null
    private var lastUpdateTime = 0L

    fun setCurrentCity(city: String) {
        if (city == _currentCity.value) return

        _isCityChanging.value = true
        _currentCity.value = city
        _aqiHistory.value = emptyList()
        Log.d("AqiViewModel", "Setting current city to $city, emptyHistory")
        lastUpdateTime = 0
        startAqiUpdates()
    }

    private fun startAqiUpdates() {
        timer?.cancel()
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    Log.d("AqiViewModel", "Updating AQI data...")
                    fetchLatestData()
                    fetchLatestWeatherData()
                }
            }, 0, 10000) // Update every 5 seconds
        }
    }

    private fun fetchLatestData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val city = _currentCity.value
                if (city.isBlank()) return@launch

                // 1. Get current data point
                val currentData = repository.getCurrentAqiData(city)
                Log.d("AqiViewModel", "Fetching AQI data for city: $city, $currentData")
                currentData?.let {
                    _currentAqiData.value = it
                    _isCityChanging.value = false

                    // 2. Get any new data points since last update
                    Log.d("AqiViewModel", "Fetching new AQI ... $lastUpdateTime")
                    if (lastUpdateTime > 0) {
                        Log.d("AqiViewModel", "Fetching new AQI data since last update... ${_aqiHistory.value}")
                        val newData = repository.getNewAqiDataSince(city, lastUpdateTime)
                        if (newData.isNotEmpty()) {
                            _aqiHistory.update { history ->
                                (history + newData)
                                    .distinctBy { it.Datatime }
                                    .sortedBy { it.Datatime }
                                    .takeLast(72) // Keep last hour (12*6=72 data points)
                            }
                        }
                    } else {
                        // First load - get last 12 data points (1 minute)
                        Log.d("AqiViewModel", "Fetching initial AQI data...")
                        val initialData = repository.getNewAqiDataSince(city, 0)
                        Log.d("AqiViewModel", "Fetching initial AQI data... ${_aqiHistory.value}, ${initialData.size}")
                        _aqiHistory.value = initialData.takeLast(12)
                    }

                    lastUpdateTime = currentData.Datatime?.toLong() ?: 0
                    Log.d("AqiViewModel", "Updating lastUpdateTime to $lastUpdateTime")
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch AQI data: ${e.message}"
                _isCityChanging.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add weather state flows
    private val _currentTemperature = MutableStateFlow<Float?>(null)
    val currentTemperature: StateFlow<Float?> = _currentTemperature.asStateFlow()

    private val _currentHumidity = MutableStateFlow<Float?>(null)
    val currentHumidity: StateFlow<Float?> = _currentHumidity.asStateFlow()

    private fun fetchLatestWeatherData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val city = _currentCity.value
                if (city.isBlank()) return@launch

                // 1. Get current AQI data point
                val currentData = repository.getCurrentAqiData(city)
                Log.d("AqiViewModel", "Fetching AQI data for city: $city, $currentData")
                currentData?.let {
                    _currentAqiData.value = it
                }

                // 2. Get weather data
                val weather = repository.getCurrentWeather(city)
                weather?.let {
                    _currentTemperature.value = it.temperature
                    _currentHumidity.value = it.humidity
                }

                // 3. Get any new AQI data points since last update
                Log.d("AqiViewModel", "Fetching new AQI ... $lastUpdateTime")
                if (lastUpdateTime > 0) {
                    Log.d("AqiViewModel", "Fetching new AQI data since last update... ${_aqiHistory.value}")
                    val newData = repository.getNewAqiDataSince(city, lastUpdateTime)
                    if (newData.isNotEmpty()) {
                        _aqiHistory.update { history ->
                            (history + newData)
                                .distinctBy { it.Datatime }
                                .sortedBy { it.Datatime }
                                .takeLast(72)
                        }
                    }
                } else {
                    // First load - get last 12 data points (1 minute)
                    Log.d("AqiViewModel", "Fetching initial AQI data...")
                    val initialData = repository.getNewAqiDataSince(city, 0)
                    Log.d("AqiViewModel", "Fetching initial AQI data... ${_aqiHistory.value}, ${initialData.size}")
                    _aqiHistory.value = initialData.takeLast(12)
                }

                lastUpdateTime = currentData?.Datatime?.toLong() ?: 0
                Log.d("AqiViewModel", "Updating lastUpdateTime to $lastUpdateTime")
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateLocation(name: String, latLng: LatLng) {
        _currentCity.value=name
        _currentLocation.value=latLng
        Log.d("AqiViewModelSearch", "Updating location to $name, $latLng, ${_currentLocation.value}")
    }


    private val _allCitiesData = MutableStateFlow<List<AqiData>>(emptyList())
    val allCitiesData = _allCitiesData.asStateFlow()

    fun loadAllCitiesData() {
        firestore.collection("cities")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreListener", "Listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val cities = snapshot.documents.map { it.id }
                    Log.d("FirestoreListener", "Current cities: $cities")
                    loadCitiesData(cities)
                } else {
                    Log.d("FirestoreListener", "Current cities: empty")
                }
            }
    }
    private fun loadCitiesData(cities: List<String>) {
        viewModelScope.launch {
            _allCitiesData.value = cities.mapNotNull { city ->
                try {
                    repository.getCurrentAqiData(city)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        updateTimer?.cancel()
    }

    private val _gpsLocation = MutableStateFlow<LatLng?>(null)
    val gpsLocation: StateFlow<LatLng?> = _gpsLocation.asStateFlow()

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(context: Context): LatLng? {
        return try {
            val location = LocationServices.getFusedLocationProviderClient(context)
                .lastLocation.await()
            location?.let {
                LatLng(it.latitude, it.longitude).also { latLng ->
                    _gpsLocation.value = latLng
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun startRegularUpdates() {
        updateTimer?.cancel()
        updateTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    viewModelScope.launch {
                        // Update current city first
                        _currentCity.value.takeIf { it.isNotEmpty() }?.let { city ->
                            repository.getCurrentAqiData(city)?.let { data ->
                                _currentAqiData.value = data
                            }
                        }

                        // Then update all cities
                        val updatedCities = _allCitiesData.value.map { data ->
                            repository.getCurrentAqiData(data.City ?: "") ?: data
                        }
                        _allCitiesData.value = updatedCities
                    }
                }
            }, 0, 30000) // Update every 30 seconds
        }
    }
}