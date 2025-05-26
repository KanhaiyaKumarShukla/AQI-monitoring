package com.example.sih

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.sih.navigation.NavHostContainer
import com.example.sih.ui.screens.network.NetworkAwareScaffold
import com.example.sih.ui.theme.AppTheme
import com.example.sih.viewmodel.AqiViewModel
import com.example.sih.viewmodel.AuthViewModel
import com.example.sih.viewmodel.BlogViewModel
import com.example.sih.viewmodel.ProfileViewModel
import com.example.sih.viewmodel.SettingsViewModel
import com.example.sih.viewmodel.admin.StationViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

/*
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding?=null
    private val binding get() = _binding!!
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { true }
        lifecycleScope.launch {
            delay(3000)
            splashScreen.setKeepOnScreenCondition { false }
        }
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNavigation

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }

                R.id.nav_map -> {
                    navController.navigate(R.id.mapFragment)
                    true
                }

                R.id.nav_news -> {
                    navController.navigate(R.id.newsFragment)
                    true
                }

                R.id.nav_admin -> {
                    navController.navigate(R.id.adminControlFragment)
                    true
                }

                else -> false
            }

        }
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}

 */


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val darkMode by settingsViewModel.darkMode.collectAsState()
            val themeColor by settingsViewModel.themeColor.collectAsState()

            AppTheme(
                darkTheme = darkMode,
                themeColor = themeColor
            ){
                NetworkAwareScaffold {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val aqiViewModel: AqiViewModel = hiltViewModel()
    val blogViewModel: BlogViewModel = hiltViewModel()
    val stationViewModel: StationViewModel = hiltViewModel()

    /*
    val context = LocalContext.current
    val aqiPredictor = remember {
        (context.applicationContext as MyApplication).aqiPredictor
    }

     */

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHostContainer(
            navController = navController,
            authViewModel = viewModel,
            profileViewModel=profileViewModel,
            settingsViewModel=settingsViewModel,
            aqiViewModel=aqiViewModel,
            blogViewModel=blogViewModel,
            stationViewModel=stationViewModel,
            //aqiPredictor=aqiPredictor,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf("home", "prediction", "map", "blog", "admin")
    val selectedItem = remember { mutableStateOf(0) }

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    when (item) {
                        "home" -> Icon(Icons.Default.Home, contentDescription = "Home")
                        "prediction" -> Icon(Icons.Default.Timeline, contentDescription = "Prediction")
                        "map" -> Icon(Icons.Default.Map, contentDescription = "Map")
                        "blog" -> Icon(Icons.Default.Article, contentDescription = "blog")
                        "admin" -> Icon(Icons.Default.Settings, contentDescription = "Admin")
                    }
                },
                label = { Text(item.capitalize()) },
                selected = selectedItem.value == index,
                onClick = {
                    selectedItem.value = index
                    when (item) {
                        "home" -> navController.navigate("home")
                        "prediction" -> navController.navigate("prediction")
                        "map" -> navController.navigate("map")
                        "blog" -> navController.navigate("blog")
                        "admin" -> navController.navigate("admin")
                    }
                }
            )
        }
    }
}


@Composable
fun AdminScreen() { Text("Admin Screen") }

