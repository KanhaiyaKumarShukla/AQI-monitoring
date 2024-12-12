package com.example.sih.socket.repository
/*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sih.socket.models.AirComponent
import com.example.sih.socket.models.AirQualityData
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject

class Repository {
    //  Creates a Socket object connected to the server at the specified URL.
    private val socket: Socket = IO.socket("https://socket-io-v1.onrender.com").apply {
        io().reconnection(true) // Ensures the socket will attempt to reconnect if the connection is lost.
        io().reconnectionAttempts(5) //  Limits reconnection attempts to 5.
        io().reconnectionDelay(2000) // Specifies a 2-second delay between reconnection
    }

    private val _cityData = MutableLiveData<AirQualityData>()
    val cityData: LiveData<AirQualityData> get() = _cityData

    init {
        // immediately set up the socket connection and listeners when the repository is created.
        setupSocket()
    }

    private fun setupSocket() {

        //Initiates the connection to the server.
        socket.connect()

        //Triggered when the socket successfully connects to the server.
        socket.on(Socket.EVENT_CONNECT) {
            // check the connection status
            Log.d("TAG", "Connected to server")
        }

        socket.on("update-data") { args ->
            try {

                if (args.isNotEmpty()) {
                    val jsonData = args[0] as JSONObject
                    Log.d("TAG", "Received: $jsonData")
                    val cityName = jsonData.getString("cityN/ ame")
                    Log.d("TAG", "city: $cityName")
                    val components = jsonData.getJSONArray("airComponents")
                    Log.d("TAG", "Components: $components")
                    Log.d("TAG", "Components length: ${components.length()}")
                    val airComponents = mutableListOf<AirComponent>()
                    for (i in 0 until components.length()) {
                        val component = components.getJSONObject(i)
                        Log.d("TAG", "DATA: $component")
                        airComponents.add(
                            AirComponent(
                                senDevId = component.getString("senDevId"),
                                sensorData = component.getInt("sensorData"),
                                sensorName = component.getString("sensorName"),
                                sensorUnit = component.optString("sensorUnit", null)
                            )
                        )
                    }

                    _cityData.postValue(AirQualityData(cityName, airComponents))
                }
            }catch (e: JSONException) {
                Log.e("TAG", "Failed to parse JSON: ${e.message}")
            } catch (e: ClassCastException) {
                Log.e("TAG", "Unexpected data type: ${e.message}")
            }
        }
        Log.d("TAG", "CITY DATA: $_cityData")

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("TAG", "Disconnected from server")
        }
    }

    fun fetchCityData(cityName: String) {
        if (socket.connected()) {
            socket.emit("fetch-city-data", cityName)
        } else {
            Log.e("TAG", "Socket is not connected")
        }
    }



    fun disconnect() {
        socket.disconnect()
        socket.close()
    }
}
*/


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sih.common.constants.AppConstants
import com.example.sih.model.Aqi
import com.example.sih.socket.models.*
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import io.socket.client.Manager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Repository {
    private val socket: Socket = IO.socket("https://socket-io-v1.onrender.com").apply {
        io().reconnection(true)
        io().reconnectionAttempts(5)
        io().reconnectionDelay(2000)
    }

    private val _cityData = MutableLiveData<List<City>>()
    val cityData: LiveData<List<City>> get() = _cityData

    private val _stationData= MutableLiveData<List<Stations>>()
    val stationData get()=_stationData
    private var pendingCity: String? = null
    init {
        setupSocket()
    }

    private fun setupSocket() {
        socket.connect()

        socket.on(Socket.EVENT_CONNECT) {
            Log.d("TAG", "Connected to server")

            pendingCity?.let {
                fetchCityData(it) // Reload pending city data if any
                pendingCity = null // Clear the pending request after processing
            }
        }


        socket.on("update-data") { args ->
            try {
                if (args.isNotEmpty()) {
                    val jsonData = args[0] as JSONObject
                    Log.d("TAG", "Received JSON: $jsonData")

                    // Parsing JSON data using Gson
                    val dataModel = Gson().fromJson(jsonData.toString(), DataModelX::class.java)
                    Log.d("TAG", "Parsed DataModel: $dataModel")

                    // Update LiveData with parsed cities
                    val cities = dataModel.data.cities
                    val station=dataModel.data.stations
                    val stations=station.toStationsList()
                    _stationData.postValue(stations)
                    _cityData.postValue(cities)
                }
            } catch (e: JSONException) {
                Log.e("TAG", "Failed to parse JSON: ${e.message}")
            } catch (e: Exception) {
                Log.e("TAG", "Unexpected error: ${e.message}")
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Log.d("TAG", "Disconnected from server")
        }
    }

   suspend fun fetchAllCitiesData(){
       for (i in 1..7) {
           val cities= AppConstants.cities[i]
           fetchCityData(cities)  // Fetch data for each city
           delay(1000) // Optional: Add delay if needed between requests
       }
   }

    fun fetchCityData(cityName: String) {
        if (socket.connected()) {
            socket.emit("fetch-city-data", cityName)
        } else {
            Log.e("TAG", "Socket is not connected")
            pendingCity=cityName
        }
        //Log.d("TAG", "City: $_cityData")
    }
    fun startFetchingCities() {
        CoroutineScope(Dispatchers.IO).launch {
            fetchAllCitiesData()
        }
    }

    private fun List<AirComponent>.toAqiData(): Aqi {
        val unitsMap = mutableMapOf<String, String>()

        this.forEach { component ->
            unitsMap[component.senDevId.uppercase()] = component.sensorUnit
        }

        return Aqi(
            AQI_IN = this.find { it.senDevId == "AQI-IN" }?.sensorData,
            AQI_US = this.find { it.senDevId == "aqi" }?.sensorData,
            CO = this.find { it.senDevId == "co" }?.sensorData,
            DEW = this.find { it.senDevId == "dew" }?.sensorData,
            H = this.find { it.senDevId == "h" }?.sensorData,
            NO2 = this.find { it.senDevId == "no2" }?.sensorData,
            O3 = this.find { it.senDevId == "o3" }?.sensorData,
            P = this.find { it.senDevId == "p" }?.sensorData,
            PM10 = this.find { it.senDevId == "pm10" }?.sensorData,
            PM25 = this.find { it.senDevId == "pm25" }?.sensorData,
            SO2 = this.find { it.senDevId == "so2" }?.sensorData,
            T = this.find { it.senDevId == "t" }?.sensorData,
            W = this.find { it.senDevId == "w" }?.sensorData,
            units = unitsMap
        )
    }

    fun Station.toStations(): Stations {
        val aqiData = this.airComponents.toAqiData()
        return Stations(
            airComponents = aqiData,
            cityName = this.cityName,
            countryName = this.countryName,
            flag = this.flag,
            formatdate = this.formatdate,
            lat = this.lat,
            locationId = this.locationId,
            locationName = this.locationName,
            lon = this.lon,
            searchType = this.searchType,
            source = this.source,
            sourceUrl = this.sourceUrl,
            stateName = this.stateName,
            stationname = this.stationname,
            timeStamp = this.timeStamp,
            updated_at = this.updated_at
        )
    }

    fun List<Station>.toStationsList(): List<Stations> {
        return this.map { it.toStations() }
    }
    fun disconnect() {
        socket.disconnect()
        socket.close()
    }
}
