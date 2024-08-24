package com.example.sih.presentation.broadcast.fragment

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sih.R
import com.example.sih.common.basefragment.BaseFragment
import com.example.sih.databinding.FragmentAdminControlBinding
import com.example.sih.model.AqiBreakpoints
import com.example.sih.model.AqiData
import com.example.sih.presentation.broadcast.util.Item
import com.example.sih.presentation.broadcast.util.ItemAdapter
import com.example.sih.viewmodel.AqiViewModel
import com.example.sih.common.constants.AppConstants

class AdminControlFragment : BaseFragment<FragmentAdminControlBinding>(FragmentAdminControlBinding::inflate) {

    private lateinit var viewModel: AqiViewModel

    private val tag="SmsReceiver"
    private val SMS_PERMISSION_CODE = 1
    private lateinit var itemAdapter: ItemAdapter

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
                lastUpdatedTime = intent?.getStringExtra("lastUpdatedTime") ?: ""
            )
            Log.d(tag, aqiData.toString())
            viewModel.saveAqiData(aqiData)
            postOnApi(aqiData)
            updateUI(aqiData)
        }
    }

    private fun postOnApi(aqiData: AqiData) {

        viewModel.postAqi(aqiData){

            if(it?.city!=null){

                Log.d(tag, "Success post $it")
                viewModel.saveAqiData(aqiData)
                updateUI(aqiData)
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
            //readSmsMessages()
            viewModel.loadLastAqiData()
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(smsReceiver, IntentFilter("AqiDataReceived"))
        }
    }
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
    }
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
            Item(title = "Last Updated", value = data.lastUpdatedTime)
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
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to prevent memory leaks
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(smsReceiver)
    }
}