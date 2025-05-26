package com.example.sih.util

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp

data class Station(
    val stationId: String = "",
    val stationName: String = "",
    val city: String = "",
    val state: String = "",
    val status: StationStatus = StationStatus.Active,
    val lastUpdated: Timestamp = Timestamp.now(),
    val location: LatLng? = null
) {
    enum class StationStatus { Active, Inactive, UnderMaintenance }

    val displayName: String
        get() = stationName.substringBefore(" - ")

    val address: String
        get() = "$city, $state"

}
