package com.example.sih.model


data class CitiesResponse(
    val error: Boolean,
    val msg: String,
    val data: List<String>
)
data class CountryRequest(val country: String)

