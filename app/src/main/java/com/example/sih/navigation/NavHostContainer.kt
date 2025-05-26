package com.example.sih.navigation

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import com.example.sih.AdminScreen
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.sih.repository.AQIPredictor
import com.example.sih.ui.screens.AirParameter
import com.example.sih.ui.screens.HelpScreen
import com.example.sih.ui.screens.HomeScreen
import com.example.sih.ui.screens.LoginScreen
import com.example.sih.ui.screens.ProfileScreen
import com.example.sih.ui.screens.SearchScreen
import com.example.sih.ui.screens.SettingsScreen
import com.example.sih.ui.screens.SignupScreen
import com.example.sih.ui.screens.TermsPrivacyScreen
import com.example.sih.ui.screens.blog.BlogDetailScreen
import com.example.sih.ui.screens.blog.BlogEditorScreen
import com.example.sih.ui.screens.blog.BlogListScreen
import com.example.sih.ui.screens.blog.MyBlogsScreen
import com.example.sih.ui.screens.admin.AdminDashboardScreen
import com.example.sih.ui.screens.admin.LoginPromptScreen
import com.example.sih.ui.screens.admin.ManagerDashboardScreen
import com.example.sih.ui.screens.admin.ManagerStationMapScreen
import com.example.sih.ui.screens.admin.StationMapScreen
import com.example.sih.ui.screens.admin.TechnicianDashboardScreen
import com.example.sih.ui.screens.admin.UserProfileScreen
import com.example.sih.ui.screens.maps.MapScreen
import com.example.sih.ui.screens.prediction.PredictionScreen
import com.example.sih.viewmodel.AqiViewModel
import com.example.sih.viewmodel.AuthViewModel
import com.example.sih.viewmodel.BlogViewModel
import com.example.sih.viewmodel.ProfileViewModel
import com.example.sih.viewmodel.SettingsViewModel
import com.example.sih.viewmodel.admin.StationViewModel


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NavHostContainer(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    aqiViewModel: AqiViewModel = hiltViewModel(),
    blogViewModel: BlogViewModel = hiltViewModel(),
    stationViewModel: StationViewModel = hiltViewModel(),
    //aqiPredictor: AQIPredictor,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        val sampleParameters = listOf(
            AirParameter(
                "PM2.5", 12.4f, "µg/m³",
                history = listOf(32f, 30f, 28f, 25f, 26f, 24f, 22f, 25f, 28f),
                timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
            ),
            AirParameter(
                "PM10", 24.0f, "µg/m³",
                history = listOf(32f, 30f, 28f, 25f, 36f, 24f, 32f, 25f, 28f),
                timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
            ),
            AirParameter(
                "O₃", 0.042f, "ppm",
                history = listOf(32f, 30f, 28f, 25f, 26f, 24f, 22f, 25f, 28f),
                timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
            ),
            AirParameter(
                "CO", 1.2f, "ppm",
                history = listOf(32f, 30f, 28f, 25f, 26f, 24f, 22f, 25f, 28f),
                timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
            ),
            AirParameter(
                "NO₂", 0.023f, "ppm",
                history = listOf(32f, 30f, 28f, 25f, 26f, 24f, 22f, 25f, 28f),
                timestamps = List(9) { System.currentTimeMillis() - (8 - it) * 3600000 }
            )
        )
        val monthlyAqiData = mapOf(
            "2025-03-01" to 45,
            "2025-03-02" to 68,
            "2025-03-03" to 102,
            "2025-03-04" to 145,
            "2025-03-05" to 67,
            "2025-03-06" to 82,
            "2025-03-07" to 65,
            "2025-03-08" to 88,
            "2025-03-09" to 120,
        )
        composable("home") {
            val isLoggedIn = authViewModel.isLoggedIn.collectAsState()
            HomeScreen(
                currentAqi = 85,
                temp = 28.5f,
                humidity = 60f,
                aqiViewModel = aqiViewModel,
                parameters = sampleParameters,
                monthlyAqiData = monthlyAqiData,
                onSearchClicked = {
                    navController.navigate("search")
                  },
                onLocationClicked = { /* Get current location */ },
                onProfileClicked = {
                    if (isLoggedIn.value) {
                        navController.navigate("profile")
                    } else {
                        navController.navigate("login")
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    /*
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }

                     */
                    Log.d("login", "NavLoginScreen popback")

                    navController.popBackStack()
                },
                onNavigateToSignup = {
                    navController.navigate("signup")
                }
            )
        }
        composable("signup") {
            SignupScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                profileViewModel = profileViewModel,
                onLogout = {
                    navController.popBackStack()
                    navController.navigate("login")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable("settings") {

            SettingsScreen(
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable("help") {
            HelpScreen(
                onBack = { navController.popBackStack() },
                onContactSupport = {
                    // Handle contact support action
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@yourapp.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "App Support Request")
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Send email..."))
                    } catch (e: ActivityNotFoundException) {
                        // Handle case where no email client is installed
                        Toast.makeText(
                            context,
                            "Please install an email app to contact support",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                onFaqItemClick = { question ->
                    // Handle FAQ item click if needed
                }
            )
        }
        composable("terms") {
            TermsPrivacyScreen(
                onBack = { navController.popBackStack() },
                // Only include onAcceptTerms if this is a mandatory acceptance screen
                // Otherwise omit the parameter to hide the accept button
                onAcceptTerms = { accepted ->
                    if (accepted) {
                        // Save acceptance and proceed
                        navController.popBackStack()
                    }
                }
            )
        }
        composable("search") {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onLocationSelected = { name, latLng ->
                    aqiViewModel.updateLocation(name, latLng)
                    navController.popBackStack()
                }
            )
        }

        composable("blog") {
            BlogListScreen(blogViewModel, navController)
        }

        composable("my_blogs") {
            MyBlogsScreen(blogViewModel, navController)
        }
        composable("editor") {
            BlogEditorScreen(blogViewModel, navController)
        }
        composable(
            "editor/{blogId}",
            arguments = listOf(navArgument("blogId") { type = NavType.StringType })
        ) { backStackEntry ->
            BlogEditorScreen(
                blogViewModel,
                navController,
                backStackEntry.arguments?.getString("blogId")
            )
        }
        composable(
            "blog_detail/{blogId}",
            arguments = listOf(navArgument("blogId") { type = NavType.StringType })
        ) { backStackEntry ->
            BlogDetailScreen(
                backStackEntry.arguments?.getString("blogId") ?: "",
                blogViewModel,
                { navController.popBackStack() }
            )
        }
        composable("map") {
            MapScreen(
                viewModel=aqiViewModel,
                navController=navController
            )
        }
        //composable("admin") { StationMapScreen() }

        composable("admin") {
            val isLoggedIn = authViewModel.isLoggedIn.collectAsState()
            Log.d("login", "NavadminScreen ${isLoggedIn.value}")
            if (isLoggedIn.value) {
                LaunchedEffect(authViewModel.currentUser) {
                    authViewModel.currentUser?.let { user ->
                        authViewModel.loadUserProfile()
                    }
                }

                val userRole = authViewModel.userProfile.collectAsState()

                when (userRole.value?.role) {
                    "admin" -> AdminDashboardScreen(navController, authViewModel)
                    "manager" -> ManagerDashboardScreen(navController, authViewModel)
                    "technician" -> TechnicianDashboardScreen(navController, authViewModel)
                    "viewer" -> StationMapScreen(viewModel = hiltViewModel())
                    else -> LoginPromptScreen (
                        onLoginClick = {
                            Log.d("login", "NavadminScreen if")
                            navController.navigate("login") {
                                popUpTo("admin") { inclusive = true }
                            }
                        }
                    )
                }
            } else  {
                LoginPromptScreen (
                    onLoginClick= {
                        Log.d("login", "NavadminScreen else")
                        navController.navigate("login") {
                            popUpTo("admin") { inclusive = true }
                        }
                    }
                )
            }

        }

        composable("station_map") {
            StationMapScreen(viewModel = hiltViewModel())
        }

        composable("user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            UserProfileScreen(
                userId = backStackEntry.arguments?.getString("userId") ?: "",
                navController = navController
            )
        }
        composable("manager_station_map") {
            ManagerStationMapScreen(
                viewModel=stationViewModel,
                authViewModel=authViewModel
            )
        }

        composable("prediction") {
            PredictionScreen()
        }
    }
}
