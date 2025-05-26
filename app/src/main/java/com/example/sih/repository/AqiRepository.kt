package com.example.sih.repository

import android.content.Context
import android.util.Log
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sih.util.AqiData
import com.example.sih.util.Weather
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.random.Random

class AqiRepository @Inject constructor(
    val firestore: FirebaseFirestore
) {
    // Cache with city + timeKey as the key
    private val cache = mutableMapOf<String, AqiData>()
    // Cache for historical data with city + startTime as the key
    private val historyCache = mutableMapOf<String, List<AqiData>>()

    suspend fun getCurrentAqiData(city: String): AqiData? {
        val timeKey = getCurrentTimeKey()
        val cacheKey = "$city-$timeKey"

        // Check cache first
        cache[cacheKey]?.let { return it }

        return try {
            // First try current time key
            var document = firestore.collection("cities")
                .document(city)
                .collection(timeKey)
                .document("data")
                .get()
                .await()

            Log.d("AqiRepository", "Current data for $city, $timeKey, ${document.data}")
            // If no data at current time, try previous time slots
            if (!document.exists()) {
                val calendar = Calendar.getInstance()
                for (i in 1..5) { // Try last 5 time slots (25 seconds)
                    calendar.add(Calendar.SECOND, -5)
                    val fallbackTimeKey = getTimeKey(calendar)
                    val fallbackCacheKey = "$city-$fallbackTimeKey"

                    document = firestore.collection("cities")
                        .document(city)
                        .collection(fallbackTimeKey)
                        .document("data")
                        .get()
                        .await()

                    Log.d("AqiRepository", "Fallback data for $fallbackCacheKey, ${document.data}")

                    if (document.exists()) {
                        Log.d("AqiRepository", "Fallback found at $fallbackCacheKey")
                        document.toObject(AqiData::class.java)?.also { data ->
                            Log.d("AqiRepository", "Data: $data")
                            // Update cache with actual time key (not fallback)
                            cache[cacheKey] = data.copy(Datatime = System.currentTimeMillis().toString())
                            return cache[cacheKey]
                        }
                    }
                }
            }

            Log.d("AqiRepository", "Data found at $timeKey, ${document.data}")
            if (document.exists()) {
                document.toObject(AqiData::class.java)?.also { data ->
                    Log.d("AqiRepository", "Data: $data")
                    // Update cache with actual time key (not fallback)
                    cache[cacheKey] = data.copy(Datatime = System.currentTimeMillis().toString())
                    return cache[cacheKey]
                }
            }

            // If still no data, generate random data
            generateRandomAqiData(city).also {
                cache[cacheKey] = it
                Log.d("AqiRepository", "Generated random data for $city")
            }
        } catch (e: Exception) {
            Log.e("AqiRepository", "Error getting current AQI data: $city", e)
            generateRandomAqiData(city) // Return random data on error
        }
    }

    suspend fun getNewAqiDataSince(city: String, lastTimestamp: Long): List<AqiData> {
        val cacheKey = "$city-$lastTimestamp"

        Log.d("AqiRepositorySince", "Getting new data for $city since $lastTimestamp")
        // Check history cache first
        historyCache[cacheKey]?.let { return it }

        val newData = mutableListOf<AqiData>()
        val currentTime = Calendar.getInstance()

        for (i in 0 until 6) {
            val time = Calendar.getInstance().apply {
                timeInMillis = currentTime.timeInMillis - (i * 5 * 1000)
            }
            val timeKey = getTimeKey(time)
            var timeInMillis = time.timeInMillis

            if (time.timeInMillis <= lastTimestamp) continue

            val documentCacheKey = "$city-$timeKey"

            try {
                var document = firestore.collection("cities")
                    .document(city)
                    .collection(timeKey)
                    .document("data")
                    .get()
                    .await()

                Log.d("AqiRepositorySince", "Found Since at $documentCacheKey, ${document.data}")

                // If no data, try previous time slots
                if (!document.exists()) {
                    val fallbackCalendar = Calendar.getInstance().apply {
                        timeInMillis = time.timeInMillis
                    }
                    for (j in 1..3) { // Try 3 previous slots (15 seconds)
                        fallbackCalendar.add(Calendar.SECOND, -5)
                        val fallbackTimeKey = getTimeKey(fallbackCalendar)

                        document = firestore.collection("cities")
                            .document(city)
                            .collection(fallbackTimeKey)
                            .document("data")
                            .get()
                            .await()

                        Log.d("AqiRepositorySince", "Fallback found Since at $fallbackTimeKey, ${document.data}")
                        if (document.exists()) break
                    }
                }

                document.toObject(AqiData::class.java)?.let { aqiData ->
                    val cachedData = aqiData.copy(Datatime = timeInMillis.toString())
                    Log.d("AqiRepositorySince", "Data found for 2 $documentCacheKey, $cachedData")
                    newData.add(cachedData)
                    cache[documentCacheKey] = cachedData
                } ?: run {
                    // If still no data, add random data
                    newData.add(generateRandomAqiData(city, timeInMillis))
                }
            } catch (e: Exception) {
                Log.e("AqiRepository", "Error getting AQI data for $timeKey", e)
                newData.add(generateRandomAqiData(city, timeInMillis))
            }
        }

        val result = newData.sortedBy { it.Datatime }
        historyCache[cacheKey] = result
        return result
    }

    private fun generateRandomAqiData(city: String, timestamp: Long = System.currentTimeMillis()): AqiData {
        // Generate random AQI and determine the bucket
        val randomAqi = Random.nextInt(50, 201) // 501 because upper bound is exclusive
        val aqiBucket = when (randomAqi) {
            in 0..50 -> "Good"
            in 51..100 -> "Satisfactory"
            in 101..200 -> "Moderate"
            in 201..300 -> "Poor"
            in 301..400 -> "Very Poor"
            else -> "Severe"
        }

        return AqiData(
            AQI = randomAqi,
            AQI_Bucket = aqiBucket,
            Benzene = Random.nextDouble(5.0, 20.0).roundTo(2),
            CO = Random.nextDouble(5.0, 20.0).roundTo(2),
            City = city,
            Datatime = timestamp.toString(),
            Document_ID = "random_${System.currentTimeMillis()}",
            NH3 = Random.nextDouble(20.0, 100.0).roundTo(2),
            NO = Random.nextDouble(10.0, 50.0).roundTo(2),
            NO2 = Random.nextDouble(20.0, 100.0).roundTo(2),
            NOx = Random.nextDouble(20.0, 150.0).roundTo(2),
            O3 = Random.nextDouble(20.0, 200.0).roundTo(2),
            PM10 = Random.nextDouble(70.0, 200.0).roundTo(1),
            PM25 = Random.nextDouble(30.0, 90.0).roundTo(1),
            SO2 = Random.nextDouble(8.0, 50.0).roundTo(2),
            Toluene = Random.nextDouble(11.0, 30.0).roundTo(2),
            Xylene = Random.nextDouble(5.0, 20.0).roundTo(2),
            completeness = Random.nextInt(80, 101), // Upper bound is exclusive, so use 101
            null_count = Random.nextInt(0, 6) // Upper bound is exclusive, so use 6
        )
    }

    // Helper extension function to round Double values
    private fun Double.roundTo(decimals: Int): Double {
        val multiplier = 10.0.pow(decimals)
        return kotlin.math.round(this * multiplier) / multiplier
    }


    private fun getTimeKey(calendar: Calendar): String {
        val minutes = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        val seconds = (calendar.get(Calendar.SECOND) / 5 * 5).toString().padStart(2, '0')
        return "$minutes-$seconds"
    }
    private fun getCurrentTimeKey(): String {
        val currentTime = Calendar.getInstance()
        return getTimeKey(currentTime)
    }

    private val weatherCache = mutableMapOf<String, Weather>()

    suspend fun getCurrentWeather(city: String): Weather? {
        val timeKey = getCurrentTimeKey()
        val cacheKey = "$city-weather-$timeKey"

        // Check cache first
        weatherCache[cacheKey]?.let { return it }

        return try {
            // Try current time key
            var document = firestore.collection("weather")
                .document(city)
                .collection(timeKey)
                .document("data")
                .get()
                .await()

            Log.d("AqiRepository", "Current weather for $city, $timeKey, ${document.data}")

            // If no data at current time, try previous time slots
            if (!document.exists()) {
                val calendar = Calendar.getInstance()
                for (i in 1..5) { // Try last 5 time slots (25 seconds)
                    calendar.add(Calendar.SECOND, -5)
                    val fallbackTimeKey = getTimeKey(calendar)
                    val fallbackCacheKey = "$city-weather-$fallbackTimeKey"

                    document = firestore.collection("weather")
                        .document(city)
                        .collection(fallbackTimeKey)
                        .document("data")
                        .get()
                        .await()

                    Log.d("AqiRepository", "Fallback weather for $fallbackCacheKey, ${document.data}")

                    if (document.exists()) {
                        document.toObject(Weather::class.java)?.also { weather ->
                            weatherCache[cacheKey] = weather
                            return weather
                        }
                    }
                }
            }

            if (document.exists()) {
                document.toObject(Weather::class.java)?.also { weather ->
                    weatherCache[cacheKey] = weather
                    return weather
                }
            }

            // If still no data, generate random weather
            generateRandomWeather().also {
                weatherCache[cacheKey] = it
            }
        } catch (e: Exception) {
            Log.e("AqiRepository", "Error getting weather data: $city", e)
            generateRandomWeather() // Return random weather on error
        }
    }

    private fun generateRandomWeather(): Weather {
        return Weather(
            humidity = Random.nextFloat() * 100, // 0-100%
            temperature = Random.nextFloat() * 40, // 0-50°C
            pressure = Random.nextFloat() * 50 + 950, // 950-1000 hPa
            wind_speed = Random.nextFloat() * 20 // 0-30 m/s
        )
    }
}