package com.example.sih.presentation.broadcast.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sih.common.basefragment.BaseFragment
import com.example.sih.common.constants.AppConstants
import com.example.sih.common.result.NetworkResult
import com.example.sih.databinding.FragmentAdminControlBinding
import com.example.sih.model.AqiData
import com.example.sih.presentation.broadcast.util.Item
import com.example.sih.presentation.broadcast.util.ItemAdapter
import com.example.sih.viewmodel.AqiViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.example.sih.common.constants.AppConstants.makeToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import android.location.Location
import android.text.Html
import android.widget.ScrollView
import android.widget.TextView

import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.sih.common.utils.LocationService
import com.example.sih.socket.viewmodel.SocketViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.getValue
import com.example.sih.R
import com.example.sih.databinding.FragmentHomeBinding
import com.example.sih.model.Aqi
import com.example.sih.presentation.broadcast.fragment.home.AqiItemAdapter
import com.example.sih.socket.models.AirComponent
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.LinkedList
import java.util.Queue
import kotlin.random.Random

class HomeFragment : Fragment(R.layout.fragment_home) , OnMapReadyCallback {

    private lateinit var viewModel: AqiViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val tag="TAG"
    private lateinit var itemAdapter: AqiItemAdapter
    private lateinit var aqi: AqiData
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var locationServices: LocationService
    private lateinit var binding: FragmentHomeBinding
    private lateinit var lineChart:LineChart
    private lateinit var dataSet: LineDataSet
    private lateinit var lineData: LineData
    private val entries : Queue<IndexedEntry> = LinkedList() // Data points
    private var xValue = 0f
    private lateinit var searchView: SearchView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(tag, "Fine location permission granted")
            //onLocationPermissionGranted()
            locationServices.getLiveLocation { cityName ->
                fetchAndShowData(cityName)
            }
        } else {
            //Log.d(tag, "Fine location permission denied")
            onLocationPermissionDenied()
        }
    }
    fun getRandomNumber(): Int {
        return Random.nextInt(-5, 5)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        lineChart=binding.lineChart
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE
        searchView = binding.searchView


        viewModel = ViewModelProvider(this)[AqiViewModel::class.java]
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        socketViewModel=ViewModelProvider(this)[SocketViewModel::class.java]
        itemAdapter = AqiItemAdapter(Aqi())

        fetchCityData()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { city ->
                    // Fetch city data when search is submitted
                    fetchCityData(city)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        //itemAdapter = AqiItemAdapter(socketViewModel.stationData)

        //locationServices = LocationService(requireContext(), fusedLocationProviderClient, )
        /*
        socketViewModel.cityData.observe(viewLifecycleOwner, Observer { data ->
                //adapter.apply {
                    Log.d("TAG", "Data: $data")
                    // Update data in the adapter
                    //adapter.updateCityList(data)
                    //dataAddedCount++
                    //binding.countTextView.text = "Data Added Count: $dataAddedCount"
                //}
            })

        /*
        viewModel.updatedAqi.observe(viewLifecycleOwner){ result->
            when (result) {
                is NetworkResult.Success -> {
                    Log.d(tag, "user fragment: ${result.data.toString()}")
                    binding.shimmerLayout.visibility = View.INVISIBLE
                    binding.dataLayout.visibility = View.VISIBLE
                    updateUI(result.data!!)
                }
                is NetworkResult.Error -> {
                    Log.d(tag, "user fragment: error found!: ${result.message.toString()}")
                    updateUI(AqiData())
                }
                is NetworkResult.Loading -> {
                    Log.d(tag, "user fragment: Loading...")
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.dataLayout.visibility = View.INVISIBLE
                    binding.shimmerLayout.startShimmer()
                    updateUI(AqiData())
                }
                else -> {
                    updateUI(AqiData())
                }
            }
        }

         */
        locationServices.checkLocationPermission(permissionLauncher)

         */

    }
    private fun fetchCityData(cityName: String="Bhopal"){
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE
        socketViewModel.fetchCityData(cityName)
        //val airComponents = socketViewModel.stationData?.airComponents
        socketViewModel.stationData.observe(viewLifecycleOwner) { stationsList ->
            // Combine all airComponents from the list of stations
            Log.d(tag, "Station: $stationsList")
            if (stationsList.isNullOrEmpty()) {
                // Handle empty or null data
                binding.nameTextView.text = "No Data Found"
            } else {
                // Stop shimmer and show the data
                binding.shimmerLayout.visibility = View.GONE
                binding.mainContent.visibility = View.VISIBLE
                val allAirComponents = stationsList.flatMap { station ->
                    // Access the `airComponents` from each station
                    Log.d(tag, "Station 98789: $station")
                    when (val components = station.airComponents) {
                        is List<*> -> components.filterIsInstance<AirComponent>() // If `airComponents` is a list
                        is Aqi -> {
                            Log.d(tag, components.AQI_IN.toString())
                            if (components.AQI_IN != null) {
                                val random = getRandomNumber()
                                addDataToChart(
                                    station.timeStamp.toString(),
                                    components.AQI_IN.toFloat() + random
                                )
                                binding.aqiTextView.setText((components.AQI_IN + random).toString())
                                val shapeDrawable = ShapeDrawable(OvalShape()).apply {
                                    // Set the inner color (background color)
                                    paint.color = setAqiBackground(components.AQI_IN + random)  // Red color (You can change this to any color you need)

                                    // Set the border (stroke) color
                                    paint.strokeWidth =10f
                                    paint.style = Paint.Style.STROKE  // To make it a border
                                }
                                binding.aqiTextView.background = shapeDrawable
                                //   ding.aqiTextView.setBackgroundColor(setAqiBackground(components.AQI_IN + random))

                            }
                            Log.d(tag, "AQI: ${components.AQI_IN}")
                            if (components.T != null) {
                                binding.tempTextView.setText(components.T.toString() + "°C")
                            }
                            Log.d(tag, "Temp: ${components.T}")
                            if (components.H != null) {
                                binding.humidityTextView.setText(components.H.toString() + "%")
                            }
                            Log.d(tag, "Humidity: ${components.H}")
                            if (components.CO != null) {
                                binding.coTV.setText(components.CO.toString() + " ppb")
                            }
                            Log.d(tag, "CO: ${components.CO}")
                            if (components.NO2 != null) {
                                binding.no2TV.setText(components.NO2.toString() + " ppb")
                            }
                            Log.d(tag, "NO2: ${components.NO2}")
                            if (components.O3 != null) {
                                binding.o3TV.setText(components.O3.toString() + " ppb")
                            }
                            if (components.PM10 != null) {
                                binding.pm10TV.setText(components.PM10.toString() + " µg/m³")
                            }
                            if (components.PM25 != null) {
                                binding.pm25TV.setText(components.PM25.toString() + " µg/m³")

                            }
                            if (components.SO2 != null) {
                                binding.so2TV.setText(components.SO2.toString() + " ppb")
                            }
                            listOf(components)
                        } // If it's a single `AirComponent`, wrap it in a list
                        else -> emptyList() // Handle unexpected or null cases
                    }
                }
                if (allAirComponents.isNotEmpty()) {

                    val airComponent =
                        allAirComponents.first() as Aqi // Assuming first as a placeholder
                    itemAdapter = AqiItemAdapter(airComponent)
                }
            }

        }
    }
    fun setAqiBackground(aqi: Int):Int {
        val color = when {
            aqi <= 50 -> Color.parseColor("#00FF00")
            aqi <= 100 -> Color.parseColor("#FFFF00")
            aqi <= 150 -> Color.parseColor("#FFA500") // Orange
            aqi <= 200 -> Color.parseColor("#FF0000")
            aqi <= 300 -> Color.parseColor("#A020F0") // Purple
            else -> Color.parseColor("#800000") // Maroon
        }
        return color
    }

    private fun addDataToChart(time: String, aqiIn: Float) {
        // Create a new Entry for the chart with T on X and AQI_IN on Y
        entries.add(IndexedEntry(xValue, Entry(xValue, aqiIn)))
        // Increment X value for next point
        xValue += 1f

        if (entries.size > 8) {
            entries.poll()
        }

        // Create a dataset and line data
        val dataSet = LineDataSet(entries.map { it.entry }, "Real-Time Data")
        dataSet.color = Color.parseColor("#3EAFCA")
        dataSet.setDrawCircles(true)
        dataSet.setCircleColor(Color.parseColor("#61CDD0"))
        dataSet.setValueTextColor(Color.WHITE)
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 6f

        // Create the LineData object
        val lineData = LineData(dataSet)

        // Set the data to the chart and refresh it
        lineChart.data = lineData
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    override fun onMapReady(googleMap: GoogleMap) {

        // Set default position and zoom
        val defaultLocation = LatLng(28.7041, 77.1025) // New Delhi example coordinates
        val cameraPosition = CameraPosition.builder()
            .target(defaultLocation)
            .zoom(10f)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))


        // Reduce map opacity by setting a transparent background color for the map
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        //googleMap.setOpacity(0.4f)  // Set opacity to 40% (change this value for more or less opacity)
    }

    private fun onLocationPermissionDenied(){
        requireContext().makeToast("For Better Performance Please grant location permission from setting.")
    }
    private fun fetchAndShowData(cityName: String?){
        // fetch data form socket viewmodel using city name
        if (cityName != null) {
            Log.d(tag, "Fetching data for city: $cityName")
            //CoroutineScope(Dispatchers.Main).launch {
            //     repeat(2) {i->
            //        Log.d("TAG", "Start- $i")
                    socketViewModel.fetchCityData(cityName)
            //        Log.d("TAG", "onCreate: $cityName- $i")
            //        delay(20000)
            //   }
            //}
            // Implement logic to fetch data using the city name
        } else {
            Log.d(tag, "City name not available. Unable to fetch data.")
        }
    }

    private fun updateUI(data: AqiData){

        Log.d(tag, "updateUI:${data.toString()}")
        val items = listOf(
            Item(title = "Country", value = data.country),
            Item(title = "State", value = data.state),
            Item(title = "City", value = data.city),
            Item(title = "PM2.5", value = data.pm25),
            Item(title = "PM10", value = data.pm10),
            Item(title = "SO2", value = data.so2),
            Item(title = "CO", value = data.co),
            Item(title = "O3", value = data.o3),
            Item(title = "NO2", value = data.no2),
            Item(title = "Last Updated", value = data.timeStamp)
        )
        val aqiValue= AppConstants.calculateOverallAqi(data)
        updateAqiDisplay(aqiValue)
        // Update the adapter's data
        // itemAdapter.updateItems(items)
    }
    private fun updateAqiDisplay(aqi: Int) {
        val aqiColor = AppConstants.getAqiColor(aqi)
        val aqiSuggestion = AppConstants.getAqiSuggestion(aqi)
        /*
        val aqiCircleView = binding.aqiCircleView
        val aqiSuggestionText = binding.aqiSuggestionText

        aqiCircleView.setBackgroundResource(aqiColor)
        aqiSuggestionText.text = aqiSuggestion
        binding.aqiValueTV.text=aqi.toString()

         */
    }


}
data class IndexedEntry(val index: Float, val entry: Entry)