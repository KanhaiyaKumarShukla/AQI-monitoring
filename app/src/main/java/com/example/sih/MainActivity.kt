package com.example.sih

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.sih.databinding.ActivityMainBinding
import com.example.sih.model.AqiData
import com.example.sih.viewmodel.AqiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding?=null
    private val binding get() = _binding!!
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
        lifecycleScope.launch {
            delay(3000)
            splashScreen.setKeepOnScreenCondition { false }
        }
        _binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment=supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController=navHostFragment.navController

    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}
