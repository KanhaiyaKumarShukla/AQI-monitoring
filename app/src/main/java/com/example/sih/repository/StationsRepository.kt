package com.example.sih.repository

import android.location.Geocoder
import android.util.Log
import com.example.sih.util.Station
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface StationRepository {
    fun getStationsStream(): Flow<List<Station>>
    suspend fun sendNotification(stationId: String)
    suspend fun geocodeStationLocation(station: Station): Station
    suspend fun getStationsByState(state: String): List<Station>
}

@Singleton
class StationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val geocoder: Geocoder
) : StationRepository {

    override fun getStationsStream(): Flow<List<Station>> = callbackFlow {
        val listener = firestore.collection("stations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val stations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Station::class.java)?.copy(stationId = doc.id)
                } ?: emptyList()

                trySend(stations)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun geocodeStationLocation(station: Station): Station {
        return try {
            val address = geocoder.getFromLocationName(
                "${station.displayName}, ${station.state}",
                1
            )?.firstOrNull()

            address?.let {
                station.copy(location = LatLng(it.latitude, it.longitude))
            } ?: station
        } catch (e: Exception) {
            // Return original station if geocoding fails
            station
        }
    }

    override suspend fun getStationsByState(state: String): List<Station> {
        return try {
            val snapshot = firestore.collection("stations")
                .whereEqualTo("state", state)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Station::class.java)?.copy(stationId = doc.id)
            }
        } catch (e: Exception) {
            // Log error or handle as needed
            Log.e("StationRepository", "Error fetching stations by state", e)
            emptyList()
        }
    }

    override suspend fun sendNotification(stationId: String) {
        try {
            // Example implementation - adjust based on your notification system
            val notificationData = hashMapOf(
                "stationId" to stationId,
                "timestamp" to FieldValue.serverTimestamp(),
                "message" to "Maintenance required at station $stationId"
            )

            firestore.collection("notifications")
                .add(notificationData)
                .await()
        } catch (e: Exception) {
            Log.e("StationRepository", "Error sending notification", e)
            throw e // Or handle differently based on your needs
        }
    }
}