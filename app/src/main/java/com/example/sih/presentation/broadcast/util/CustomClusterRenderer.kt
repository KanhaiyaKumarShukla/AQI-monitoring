package com.example.sih.presentation.broadcast.util

import android.annotation.SuppressLint
import com.example.sih.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.graphics.Color
import android.widget.TextView
import com.example.sih.model.ClusterMarker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CustomClusterRenderer(
    context:Context,
    map:GoogleMap,
    clusterManager:ClusterManager<ClusterMarker>
) : DefaultClusterRenderer<ClusterMarker>(context, map, clusterManager) {

    private val markerView: View = LayoutInflater.from(context).inflate(R.layout.marker_aqi, null)

    @SuppressLint("SetTextI18n")
    override fun onBeforeClusterItemRendered(item: ClusterMarker, markerOptions: MarkerOptions) {
        val aqiData = item.getAqiData()

        // Update the PM10 text
        val aqiTextView = markerView.findViewById<TextView>(R.id.tv_aqi)
        aqiTextView.text = "${aqiData.AQI_US}\naqi"

        // Determine the background color based on PM10 levels
        val aqi = aqiData.AQI_US?: 0 // Default to 0 if parsing fails
        val backgroundColor = when {
            aqi <= 100 -> Color.parseColor("#00FF00") // Green
            aqi in 101..150 -> Color.parseColor("#FFFF00") // Yellow
            else -> Color.parseColor("#FF0000") // Red
        }

        // Access the layer-list drawable
        /*
        val backgroundDrawable = markerView.background.mutate() as? android.graphics.drawable.LayerDrawable
        backgroundDrawable?.let {
            // Find the inner drawable using its ID
            val innerDrawable = it.findDrawableByLayerId(R.id.inner_background) as? android.graphics.drawable.GradientDrawable
            innerDrawable?.setColor(backgroundColor) // Set the color dynamically
        }

         */

        // Convert the custom marker view to BitmapDescriptor
        val markerIcon = createCustomMarkerBitmap()
        markerOptions.icon(markerIcon)
        markerOptions.title(item.title)
    }


    private fun createCustomMarkerBitmap(): BitmapDescriptor {
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            markerView.measuredWidth,
            markerView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

