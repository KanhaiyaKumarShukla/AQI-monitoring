package com.example.sih.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sih.common.result.NetworkResult
import com.example.sih.model.AqiData
import com.example.sih.repository.AqiRepository
import kotlinx.coroutines.launch

class AqiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AqiRepository(application)
    private val _lastAqiData = MutableLiveData<AqiData?>()
    val lastAqiData: LiveData<AqiData?> get() = _lastAqiData

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
}