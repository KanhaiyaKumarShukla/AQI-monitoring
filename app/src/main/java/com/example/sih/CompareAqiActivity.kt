package com.example.sih

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sih.databinding.ActivityCompareAqiBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.json.JSONObject

class CompareAqiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompareAqiBinding
    private lateinit var lineChart:LineChart
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCompareAqiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lineChart=binding.lineChart
        val jsonData = resources.openRawResource(R.raw.city_aqi)
            .bufferedReader().use { it.readText() }
        val lineData = prepareLineData(JSONObject(jsonData))
        lineChart.data = lineData

        lineChart.description.text = "AQI Comparison"
        lineChart.animateX(1000)
        lineChart.invalidate()

    }
    private fun prepareLineData(jsonObject: JSONObject): LineData {
        val dataSets = mutableListOf<ILineDataSet>()

        val colors = listOf(Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN)

        // Parse cities and their AQI data
        val cities = jsonObject.getJSONArray("data")
        for (i in 0 until cities.length()) {
            val cityObject = cities.getJSONObject(i)
            val cityName = cityObject.getString("city")
            val aqiData = cityObject.getJSONArray("aqi_data")

            // Create entries for the graph
            val entries = mutableListOf<Entry>()
            for (j in 0 until aqiData.length()) {
                val aqiObject = aqiData.getJSONObject(j)
                val dateIndex = j.toFloat() // Use index for X-axis
                val aqi = aqiObject.getInt("aqi").toFloat()
                entries.add(Entry(dateIndex, aqi))
            }

            // Create a LineDataSet for the city
            val dataSet = LineDataSet(entries, cityName).apply {
                color = colors[i % colors.size] // Assign color to the line
                valueTextColor = colors[i % colors.size]
                lineWidth = 2f
                circleRadius = 4f
                setDrawCircleHole(false)
                setCircleColor(color)
            }
            dataSets.add(dataSet as ILineDataSet)
        }

        return LineData(dataSets)
    }
}