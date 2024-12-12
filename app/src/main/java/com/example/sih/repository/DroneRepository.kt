package com.example.sih.repository

import android.util.Log
import com.example.sih.model.AqiData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class DroneRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getDroneDataFlow(): Flow<Map<String, List<AqiData>>> = callbackFlow {
        val listenerRegistration = firestore.collection("drones")
            .document("allDrones")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // Close the flow in case of an error
                    close(error)
                    return@addSnapshotListener
                }
                val dronesData = mutableMapOf<String, List<AqiData>>()
                value?.data?.forEach { droneId, locations ->
                    Log.d("DroneData-", locations.toString())
                    val locationList = (locations as? List<Map<String, Any>>)?.map { map ->
                        Log.d("DroneData--", map.toString())
                        AqiData(
                            latitude = map["latitude"] as? Double ?: 0.0,
                            longitude = map["longitude"] as? Double ?: 0.0,
                            timeStamp = map["timestamp"] as? String ?: "-",
                            pm10 = map["pm10"] as? String?: "-",
                            pm25 = map["pm25"] as? String?: "-",
                            co = map["co"] as? String?: "-",
                            so2 = map["so2"] as? String?: "-",
                            o3 = map["o3"] as? String?: "-",
                            temperature = map["temperature"] as? Double?: 0.0
                        )

                    } ?: emptyList()
                    Log.d("DroneData**", locationList.toString())

                    dronesData[droneId] = locationList
                }

                trySend(dronesData).isSuccess
        }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    fun getDroneData(droneId: String): Flow<List<AqiData>> = callbackFlow {
        val listenerRegistration = firestore.collection("drones")
            .document("allDrones")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val droneData = snapshot?.data?.get(droneId)?.let { locations ->
                    Log.d("DroneData-&", locations.toString())
                    (locations as? List<Map<String, Any>>)?.map { map ->
                        Log.d("DroneData--&", map.toString())
                        AqiData(
                            latitude = map["latitude"] as? Double ?: 0.0,
                            longitude = map["longitude"] as? Double ?: 0.0,
                            timeStamp = map["timestamp"] as? String ?: "-",
                            pm10 = map["pm10"] as? String?: "-",
                            pm25 = map["pm25"] as? String?: "-",
                            co = map["co"] as? String?: "-",
                            so2 = map["so2"] as? String?: "-",
                            o3 = map["o3"] as? String?: "-",
                            temperature = map["temperature"] as? Double?: 0.0
                        )
                    } ?: emptyList()
                } ?: emptyList()

                trySend(droneData).isSuccess
            }

        awaitClose { listenerRegistration.remove() }
    }

    fun getLocationData(locationId: String): Flow<List<AqiData>> = callbackFlow {
        val listenerRegistration = firestore.collection("locations")
            .document("allLocations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val locationData = snapshot?.data?.get(locationId)?.let { timestamps ->
                    Log.d("DroneLocationData-&", timestamps.toString())
                    (timestamps as? List<Map<String, Any>>)?.map { map ->
                        Log.d("DroneLocationData--&", map.toString())
                        AqiData(
                            latitude = map["latitude"] as? Double ?: 0.0,
                            longitude = map["longitude"] as? Double ?: 0.0,
                            timeStamp = map["timestamp"] as? String ?: "-",
                            pm10 = map["pm10"] as? String?: "-",
                            pm25 = map["pm25"] as? String?: "-",
                            co = map["co"] as? String?: "-",
                            so2 = map["so2"] as? String?: "-",
                            o3 = map["o3"] as? String?: "-",
                            temperature = map["temperature"] as? Double?: 0.0
                        )
                    } ?: emptyList()
                } ?: emptyList()
                Log.d("DroneLocationData", locationData.toString())

                trySend(locationData).isSuccess
            }

        awaitClose { listenerRegistration.remove() }
    }

}
