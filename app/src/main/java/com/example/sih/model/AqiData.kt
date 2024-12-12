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
    val timeStamp: String = "-",
    val longitude: Double=0.0,
    val latitude:  Double=0.0,
    val temperature:  Double=0.0
)

data class Aqi(
    val AQI_IN: Int? = null,
    val AQI_US: Int? = null,
    val CO: Int? = null,
    val DEW: Int? = null,
    val H: Int? = null,
    val NO2: Int? = null,
    val O3: Int? = null,
    val P: Int? = null,
    val PM10: Int? = null,
    val PM25: Int? = null,
    val SO2: Int? = null,
    val T: Int? = null,
    val W: Int? = null,
    val units: Map<String, String> = emptyMap()
)

