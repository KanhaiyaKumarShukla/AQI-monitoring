package com.example.sih.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sih.model.AqiData
import com.example.sih.repository.DroneRepository
import kotlinx.coroutines.flow.Flow

class DroneViewModel : ViewModel() {
    private val repository = DroneRepository()

    val droneDataFlow= repository.getDroneDataFlow()

    fun getDroneData(droneId: String): Flow<List<AqiData>> {
        return repository.getDroneData(droneId)
    }

    fun getLocationData(locationId: String): Flow<List<AqiData>> {
        return repository.getLocationData(locationId)
    }
}
