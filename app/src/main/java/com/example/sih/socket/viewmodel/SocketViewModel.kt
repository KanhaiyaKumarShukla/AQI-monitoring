package com.example.sih.socket.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.sih.socket.models.AirQualityData
import com.example.sih.socket.models.City
import com.example.sih.socket.models.Stations
import com.example.sih.socket.repository.Repository

class SocketViewModel : ViewModel() {
    private val repository = Repository()

    val cityData: LiveData<List<City>> get() = repository.cityData
    val stationData:LiveData<List<Stations>> get() = repository.stationData

    fun fetchCityData(cityName: String) {
        repository.fetchCityData(cityName)
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }
    fun startFetchingCities(){
        repository.startFetchingCities()
    }
}
