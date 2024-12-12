package com.example.sih.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterItem

class ClusterMarker (
    private val position: LatLng,
    private val title: String,
    private val aqiData: Aqi
) : ClusterItem {
    private val gson = Gson()
    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getSnippet(): String? {
        return gson.toJson(aqiData)
    }

    override fun getZIndex(): Float? {
        return null
    }
    fun getAqiData(): Aqi {
        return gson.fromJson(getSnippet(), Aqi::class.java)
    }
}