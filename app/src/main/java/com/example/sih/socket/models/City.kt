package com.example.sih.socket.models

data class City(
    val airComponents: List<AirComponent>,
    val cityName: String,
    val countryName: String,
    val flag: String,
    val locationId: String,
    val locationName: String,
    val searchType: String,
    val source: String,
    val sourceUrl: String,
    val stateName: String,
    val timeStamp: String,
    val timeago: String
)