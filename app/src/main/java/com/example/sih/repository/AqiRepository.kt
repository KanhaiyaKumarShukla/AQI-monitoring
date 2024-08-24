package com.example.sih.repository

import android.content.Context
import android.util.Log
import com.example.sih.common.constants.AppConstants
import com.example.sih.model.AqiData
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sih.common.result.NetworkResult

class AqiRepository(private val context: Context) {
    private val tag = "SmsReceiver"
    private val sharedPreferences = context.getSharedPreferences("aqi_data", Context.MODE_PRIVATE)

    fun saveAqiData(aqiData: AqiData) {
        Log.d(tag, "saveAqiData is being called")

        // Create a JSON object with default values of "-"
        val json = JSONObject().apply {
            put("country", aqiData.country ?: "-")
            put("state", aqiData.state ?: "-")
            put("city", aqiData.city ?: "-")
            put("pm25", aqiData.pm25 ?: "-")
            put("pm10", aqiData.pm10 ?: "-")
            put("so2", aqiData.so2 ?: "-")
            put("co", aqiData.co ?: "-")
            put("o3", aqiData.o3 ?: "-")
            put("no2", aqiData.no2 ?: "-")
            put("lastUpdatedTime", aqiData.lastUpdatedTime ?: "-")
        }

        // Save the JSON string to SharedPreferences
        sharedPreferences.edit()
            .putString("last_aqi_data", json.toString())
            .apply()
    }


    fun getLastAqiData(): AqiData? {

        val jsonString = sharedPreferences.getString("last_aqi_data", null)
        val jsonObject = if (jsonString != null) JSONObject(jsonString) else null

        return AqiData(
            country = jsonObject?.optString("country", "-") ?: "-",
            state = jsonObject?.optString("state", "-") ?: "-",
            city = jsonObject?.optString("city", "-") ?: "-",
            pm25 = jsonObject?.optString("pm25", "-") ?: "-",
            pm10 = jsonObject?.optString("pm10", "-") ?: "-",
            so2 = jsonObject?.optString("so2", "-") ?: "-",
            co = jsonObject?.optString("co", "-") ?: "-",
            o3 = jsonObject?.optString("o3", "-") ?: "-",
            no2 = jsonObject?.optString("no2", "-") ?: "-",
            lastUpdatedTime = jsonObject?.optString("lastUpdatedTime", "-") ?: "-"
        )

    }

    private val apiService = AppConstants.apiService
    private val _aqi=MutableLiveData<NetworkResult<AqiData>>()
    val aqi: LiveData<NetworkResult<AqiData>>
        get() = _aqi

    suspend fun getAqi() {

        _aqi.value=NetworkResult.Loading()

        apiService.getAqi().enqueue(object : Callback<AqiData> {
            override fun onResponse(call: Call<AqiData>, response: Response<AqiData>) {
                if(response.isSuccessful && response.body()!=null){
                    val aqiData=response.body()
                    _aqi.value=NetworkResult.Success(aqiData!!)
                    Log.d("successful result", aqiData.toString())
                }else if(response.errorBody()!=null){
                    _aqi.value=NetworkResult.Error("Something went Wrong")
                    Log.e(tag,response.errorBody().toString())
                }else{
                    Log.e(tag,"something error")
                    _aqi.value=NetworkResult.Error("Something went Wrong")
                }
            }

            override fun onFailure(call: Call<AqiData>, t: Throwable) {
                Log.e(tag,"Error Found! api response failed")
                _aqi.value=NetworkResult.Error(t.message)
            }
        })

    }

    suspend fun postAqi(userData: AqiData, onResult: (AqiData?) -> Unit) {
        apiService.postAqi(userData).enqueue(
            object : Callback<AqiData> {
                override fun onFailure(call: Call<AqiData>, t: Throwable) {
                    onResult(null)
                    Log.d(tag, "failure: NULL")
                }

                override fun onResponse(call: Call<AqiData>, response: Response<AqiData>) {
                    val addedAqi = response.body()
                    onResult(addedAqi)
                    Log.d(tag, "success: $addedAqi")
                }
            }
        )
    }

}