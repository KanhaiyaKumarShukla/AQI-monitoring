package com.example.sih.socket.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sih.R
import com.example.sih.databinding.ActivitySocketBinding
import com.example.sih.socket.adapter.CityAdapter
import com.example.sih.socket.models.AirQualityData
import com.example.sih.socket.viewmodel.SocketViewModel

class SocketActivity : AppCompatActivity() {
    private val viewModel: SocketViewModel by viewModels()
    private val cities = mutableListOf<String>()
    //private val adapter = AirQualityAdapter(cities)
    private lateinit var binding: ActivitySocketBinding
    private var dataAddedCount = 0
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        val adapter = CityAdapter(emptyList())
        binding.aqiRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.aqiRecyclerView.adapter = adapter

        viewModel.cityData.observe(this, Observer { data ->
            adapter.apply {
                Log.d("TAG", "Data: $data")
                // Update data in the adapter
                adapter.updateCityList(data)
                dataAddedCount++
                binding.countTextView.text = "Data Added Count: $dataAddedCount"
            }
        })


        binding.fetchDataButton.setOnClickListener {
            val cityName = binding.cityInput.text.toString()
            if (cityName.isNotBlank() && !cities.contains(cityName)) {
                viewModel.fetchCityData(cityName)
                // viewModel.fetchCityData("Bhopal")
                Log.d("TAG", "cityName: $cityName")
            } else {
                Toast.makeText(this, "City already added or invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }
    /*
    private fun updateRecyclerView(data: AirQualityData) {
        val adapter = AQIAdapter(data.airComponents)
        aqiRecyclerView.adapter = adapter
    }

     */

}
