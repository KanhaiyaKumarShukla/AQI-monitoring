package com.example.sih.socket.models

import com.example.sih.model.Aqi

data class Station(
    val airComponents: List<AirComponent>,
    val cityName: String,
    val countryName: String,
    val flag: String,
    val formatdate: String,
    val lat: String,
    val locationId: String,
    val locationName: String,
    val lon: String,
    val searchType: String,
    val source: String,
    val sourceUrl: String,
    val stateName: String,
    val stationname: String,
    val timeStamp: String,
    val updated_at: String
)
data class Stations(
    val airComponents: Aqi,
    val cityName: String,
    val countryName: String,
    val flag: String,
    val formatdate: String,
    val lat: String,
    val locationId: String,
    val locationName: String,
    val lon: String,
    val searchType: String,
    val source: String,
    val sourceUrl: String,
    val stateName: String,
    val stationname: String,
    val timeStamp: String,
    val updated_at: String
)
