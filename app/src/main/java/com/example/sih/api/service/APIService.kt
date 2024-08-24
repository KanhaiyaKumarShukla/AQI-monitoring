package com.example.sih.api.service

import com.example.sih.model.AqiData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface APIService {
    @GET("scrape/india/madhya-pradesh/bhopal/")
    fun getAqi(): Call<AqiData>

    @POST("scrape/india/madhya-pradesh/bhopal/")
    fun postAqi(@Body userData: AqiData): Call<AqiData>
}