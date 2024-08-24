package com.example.sih.presentation.user

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sih.R
import com.example.sih.common.basefragment.BaseFragment
import com.example.sih.common.constants.AppConstants
import com.example.sih.common.result.NetworkResult
import com.example.sih.databinding.FragmentAdminControlBinding
import com.example.sih.model.AqiData
import com.example.sih.presentation.broadcast.util.Item
import com.example.sih.presentation.broadcast.util.ItemAdapter
import com.example.sih.viewmodel.AqiViewModel

class UserFragment : BaseFragment<FragmentAdminControlBinding>(FragmentAdminControlBinding::inflate) {

    private lateinit var viewModel: AqiViewModel

    private val tag="SmsReceiver"
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var aqi: AqiData
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AqiViewModel::class.java]

        itemAdapter = ItemAdapter(emptyList())
        binding.recyclerView.adapter = itemAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

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
            Item(title = "Last Updated", value = data.lastUpdatedTime)
        )
        val aqiValue= AppConstants.calculateOverallAqi(data)
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

}