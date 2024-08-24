package com.example.sih.model

data class AqiData(
    val country: String="-",
    val state: String="-",
    val city: String="-",
    val pm25: String="-",
    val pm10: String="-",
    val so2: String="-",
    val co: String="-",
    val o3: String="-",
    val no2: String="-",
    val lastUpdatedTime: String="-"
)
