package com.example.sih.presentation.broadcast.fragment

import android.Manifest
import android.R.attr.theme
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sih.R
import com.example.sih.common.basefragment.BaseFragment
import com.example.sih.databinding.FragmentAdminControlBinding
import com.example.sih.model.AqiData
import com.example.sih.presentation.broadcast.util.Item
import com.example.sih.presentation.broadcast.util.ItemAdapter
import com.example.sih.viewmodel.AqiViewModel
import com.example.sih.common.constants.AppConstants
import com.example.sih.databinding.FragmentHomeBinding
import com.example.sih.model.Aqi
import com.example.sih.presentation.broadcast.fragment.home.MyAdapter
import com.example.sih.socket.models.AirComponent
import com.example.sih.socket.viewmodel.SocketViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import kotlin.random.Random

class AdminControlFragment : Fragment(R.layout.admin_control_fragment), OnMapReadyCallback {

    private lateinit var viewModel: AqiViewModel

    private val tag="SmsReceiver"
    private val SMS_PERMISSION_CODE = 1
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var socketViewModel: SocketViewModel
    private lateinit var binding: FragmentAdminControlBinding
    private lateinit var barChart: BarChart
    private var dataList =  mutableListOf<Pair<String, Float>>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var myAdapter:MyAdapter


    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(tag,"onReceive is called:smsReceiver")
            val aqiData = AqiData(
                country = intent?.getStringExtra("country") ?: "",
                state = intent?.getStringExtra("state") ?: "",
                city = intent?.getStringExtra("city") ?: "",
                pm25 = intent?.getStringExtra("pm25")?: "",
                pm10 = intent?.getStringExtra("pm10")?: "",
                so2 = intent?.getStringExtra("so2")?: "",
                co = intent?.getStringExtra("co")?: "",
                o3 = intent?.getStringExtra("o3")?: "",
                no2 = intent?.getStringExtra("no2")?: "",
                timeStamp = intent?.getStringExtra("lastUpdatedTime") ?: ""
            )
            Log.d(tag, aqiData.toString())
            viewModel.saveAqiData(aqiData)
            postOnApi(aqiData)
            //updateUI(aqiData)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

    }

    private fun postOnApi(aqiData: AqiData) {

        viewModel.postAqi(aqiData){

            if(it?.city!=null){

                Log.d(tag, "Success post $it")
                viewModel.saveAqiData(aqiData)
                //updateUI(aqiData)
            }else{
                Log.d(tag, "Error registering new user")
            }
        }
    }

    private fun checkForSmsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            ), SMS_PERMISSION_CODE)
        } else {
            Log.d(tag, "Request granted in check")
            viewModel.loadLastAqiData()

            // Register receiver to handle incoming SMS
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(smsReceiver, IntentFilter("AqiDataReceived"))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(tag, "Request granted in request permission")
            // readSmsMessages()
            viewModel.loadLastAqiData()
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(smsReceiver, IntentFilter("AqiDataReceived"))
        }
    }
    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AqiViewModel::class.java]

        itemAdapter = ItemAdapter(emptyList())
        binding.recyclerView.adapter = itemAdapter

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Observe changes in lastAqiData
        viewModel.lastAqiData.observe(viewLifecycleOwner) { aqiData ->
            aqiData?.let {
                // Update UI with the new AQI data
                Log.d(tag, "auiData:\n${it}")
                updateUI(it)

            }
        }

        checkForSmsPermission()
    }*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        socketViewModel = ViewModelProvider(this)[SocketViewModel::class.java]
        binding = FragmentAdminControlBinding.bind(view)
        barChart = binding.barChart
        setupBarChart()
        recyclerView=binding.recyclerView
        socketViewModel.startFetchingCities()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        myAdapter = MyAdapter(emptyList())
        recyclerView.adapter = myAdapter
        socketViewModel.stationData.observe(viewLifecycleOwner) { stationsList ->
            Log.d(tag, "station: $stationsList")
            if (!stationsList.isNullOrEmpty()) {
                // Extract city names and AQI values
                val stationAqiData = stationsList.mapNotNull { station ->
                    val cityName = station.cityName // Assuming there's a `city` property in the station object
                    val aqiValue = when (val components = station.airComponents) {
                        is List<*> -> components.filterIsInstance<Aqi>().firstOrNull()?.AQI_IN // If airComponents is a list
                        is Aqi -> components.AQI_IN // If airComponents is a single Aqi object
                        else -> null
                    }
                    Log.d(tag, "cityName: $cityName $aqiValue")
                    if (cityName != null && aqiValue != null) Pair(cityName, (aqiValue+getRandomNumber()).toFloat()) else null

                }

                // Update the dataList used for the chart
                //dataList.clear()
                dataList.addAll(stationAqiData)

                if (dataList.size > 8) {
                    dataList = dataList.takeLast(8).toMutableList()
                }
                Log.d(tag, "cityAqi: $stationAqiData")
                // Update the bar chart
                updateBarGraph()
            }
        }

    }

    fun getRandomNumber(): Int {
        return Random.nextInt(-5, 5)
    }


        /*
    private fun updateUI(data: AqiData){

        binding.shimmerLayout.visibility = View.INVISIBLE
        binding.dataLayout.visibility = View.VISIBLE
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
        val aqiValue=AppConstants.calculateOverallAqi(data)
        updateAqiDisplay(aqiValue)
        // Update the adapter's data
        itemAdapter.updateItems(items)
    }
    private fun updateAqiDisplay(aqi: Int) {
        val aqiColor = AppConstants.getAqiColor(aqi)
        val aqiSuggestion = AppConstants.getAqiSuggestion(aqi)

        val aqiCircleView = binding.aqiCircleView
        val aqiSuggestionText = binding.aqiSuggestionText

        aqiCircleView.setBackgroundResource(aqiColor)
        aqiSuggestionText.text = aqiSuggestion
        binding.aqiValueTV.text=aqi.toString()
    }

     */
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to prevent memory leaks
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(smsReceiver)
    }

    private fun setupBarChart() {
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setScaleEnabled(true)
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f // Set minimum Y value to 0
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.isEnabled = false // Disable X-axis labels
    }

    private fun updateBarGraph() {
        // Sort data by AQI value in descending order


        dataList.sortByDescending { it.second }
        val topCities = dataList.sortedByDescending { it.second }.take(8)

        Log.d(tag, "DATA List: $dataList")

        // Convert data to BarEntries
        val entries = dataList.mapIndexed { index, pair ->
            BarEntry(index.toFloat(), pair.second)
        }
        Log.d(tag, "entries: $entries")

        // Create BarDataSet and attach it to BarData
        val barDataSet = BarDataSet(entries, "AQI by City")
        barDataSet.valueTextSize = 12f
        barDataSet.setColors(ContextCompat.getColor(requireContext(), R.color.colorGray))

        barDataSet.valueFormatter = IndexAxisValueFormatter(dataList.map { it.first })

        // Set up the X-axis labels
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(dataList.map { it.first }) // Set city names
        xAxis.position = XAxis.XAxisPosition.BOTTOM // Position labels at the bottom
        xAxis.granularity = 1f // Ensure labels appear per bar
        xAxis.isGranularityEnabled = true
        xAxis.labelRotationAngle = -45f

        // Update the chart
        barChart.data = BarData(barDataSet)
        barChart.invalidate() // Refresh the chart

        updateRecyclerView(topCities)
    }
    private fun updateRecyclerView(topCities: List<Pair<String, Float>>) {
        //val recyclerView = findViewById<RecyclerView>(R.id.recyclerView) // Or reference RecyclerView as needed
        val adapter = binding.recyclerView.adapter as? MyAdapter

        if (adapter != null) {
            adapter.updateData(topCities)  // Updating the adapter with the filtered data
        }
    }


}

