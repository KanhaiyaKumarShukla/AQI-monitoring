package com.example.sih.presentation.broadcast.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.sih.R
import com.example.sih.common.constants.AppConstants
import com.example.sih.common.constants.AppConstants.makeToast
import com.example.sih.common.constants.AppConstants.showDetailsDialog
import com.example.sih.common.utils.LocationService
import com.example.sih.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.sih.model.ClusterMarker
import com.example.sih.presentation.broadcast.util.CustomClusterRenderer
import com.example.sih.socket.models.Stations
import com.example.sih.socket.viewmodel.SocketViewModel
import com.example.sih.viewmodel.DroneViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.PolyUtil
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val pathCoordinates = mutableListOf<LatLng>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var viewModel: DroneViewModel
    private val drones=mutableListOf<String>()
    private lateinit var clusterManager: ClusterManager<ClusterMarker>
    private lateinit var socketViewModel: SocketViewModel
    private val stations = mutableListOf<Stations>()
    private lateinit var locationServices: LocationService
    private val tag="TAG"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(tag, "Permission granted: $isGranted")
        if (isGranted) {
            Log.d(tag, "Fine location permission granted")
            //onLocationPermissionGranted()
            locationServices.getLiveLocation { cityName ->
                Log.d(tag, "city: $cityName")
                fetchAndShowData(cityName)
            }
        } else {
            Log.d(tag, "Fine location permission denied")
            onLocationPermissionDenied()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapBinding.bind(view)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel = ViewModelProvider(this)[DroneViewModel::class.java]

        socketViewModel=ViewModelProvider(this)[SocketViewModel::class.java]
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationServices = LocationService(requireContext(), fusedLocationProviderClient, )
        //CoroutineScope(Dispatchers.Main).launch {
        //    repeat(3) { iteration ->
                // Execute your task here
                // socketViewModel.fetchCityData("bhopal")
        //        Log.d("TAG", "onCreate: bhopal")
        //        delay(5000)
        //    }
        //    Log.d("TAG", "Task completed!")
        //}
        //uploadDroneAndLocationDataToFirestore()
        Log.d(tag, "MapAcitity")
        //locationServices.checkLocationPermission(permissionLauncher)
        socketViewModel.startFetchingCities()



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
    private fun onLocationPermissionDenied(){
        requireContext().makeToast("For Better Performance Please grant location permission from setting.")
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        setupClusterManager()
        val lat=23.10844
        val lon=77.511428
        zoomToLocation(mMap, LatLng(lat, lon))
        socketViewModel.stationData.observe(this) { stationList ->
            // stations.clear()
            stations.addAll(stationList)
            addMarkersToMap() // Add markers when data is fetched
        }

        // startDroneTracking()
    }
    private fun startDroneTracking() {
        // Example: Simulated location updates

        // Simulate drone movement

        lifecycleScope.launch {
            viewModel.droneDataFlow.collect { dronesData ->
                for ((droneId, locations) in dronesData) {
                    Log.d("DroneData", "Drone: $droneId, Locations: $locations")
                }
            }
        }

        // Observe a specific drone data
        lifecycleScope.launch {
            viewModel.getDroneData("Drone1").collect { locations ->
                Log.d("Drone1Data", "Locations: $locations")
            }
        }

        val inputStream = resources.openRawResource(R.raw.locations)
        val json = inputStream.bufferedReader().use { it.readText() }

        // Parse JSON into List<Location>
        val gson = Gson()
        val type = object : TypeToken<List<Location>>() {}.type
        val locations: List<Location> = gson.fromJson(json, type)

        // Convert to LatLng
        val latLngList = locations.map { LatLng(it.latitude, it.longitude) }
        drawRoute(latLngList)
        zoomToLocation(mMap, latLngList[0])
        val resizedIcon = AppConstants.resizeBitmap(requireContext(), R.drawable.camera_drone, 100, 100)
        val markerOptions = MarkerOptions()
            .position(latLngList.last())
            .icon(resizedIcon)

        mMap.addMarker(markerOptions)



        // Observe a specific location data
        lifecycleScope.launch {
            viewModel.getLocationData("Location1").collect { locations ->
                Log.d("DroneLocationData", "Location1 Data: $locations")
            }
        }


        // Launch a coroutine in the lifecycleScope to observe and collect drone data
        /*
        lifecycleScope.launch {
            viewModel.droneDataFlow.collect { dronesData ->
                // Iterate through each drone's data
                clusterManager.clearItems()

                for ((droneId, locations) in dronesData) {
                    Log.d("DroneData", "Drone: $droneId, Locations: $locations")

                    if (locations.isEmpty()) continue
                    // List to store the coordinates for drawing the polyline
                    // val pathCoordinates = mutableListOf<LatLng>()

                    // Update the map with the drone's locations
                    for ((index, location) in locations.withIndex()) {
                        // Add a marker for the drone's position
                        val loc = LatLng(location.latitude, location.longitude)
                        val aqiData = AqiData(
                            country = "Unknown", // Add actual data if available
                            state = "Unknown",
                            city = "Unknown",
                            pm25 = location.pm25.toString(),
                            pm10 = location.pm10.toString(),
                            so2 = location.so2.toString(),
                            co = location.co.toString(),
                            o3 = location.o3.toString(),
                            no2 = location.no2.toString(),
                            timeStamp = location.timeStamp,
                            longitude = location.longitude,
                            latitude = location.latitude,
                            temperature = location.temperature
                        )

                        val marker = ClusterMarker(loc, "Drone ID: $droneId", aqiData)
                        clusterManager.addItem(marker)

                        // Move the camera to focus on the latest location
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 60f))
                    }
                }
            }
        }
        */

    }

    private fun generateData(
        centralLat: Double,
        centralLon: Double,
        numDrones: Int,
        numLocations: Int
    ): Pair<Map<String, Any>, Map<String, Any>> {
        val droneData = mutableMapOf<String, Any>()
        val locationData = mutableMapOf<String, MutableList<Map<String, Any>>>()

        for (droneIndex in 1..numDrones) {
            val droneName = "Drone$droneIndex"
            drones.add(droneName)
            val locations = mutableListOf<Map<String, Any>>()

            var currentLat = centralLat
            var currentLon = centralLon

            for (locationIndex in 1..numLocations) {
                val randomAngle = Math.random() * 2 * Math.PI
                val randomRadius = (10..20).random() / 111000.0 // Approximately 10-20 meters
                val offsetLat = randomRadius * cos(randomAngle)
                val offsetLon = randomRadius * sin(randomAngle) / cos(centralLat * Math.PI / 180)
                currentLat += offsetLat
                currentLon += offsetLon

                val locationName = "Location$locationIndex"
                val locationDataPoint = mapOf(
                    "latitude" to currentLat,
                    "longitude" to currentLon,
                    "timestamp" to System.currentTimeMillis().toString(),
                    "pm10" to (10..50).random().toString(),
                    "pm25" to (5..30).random().toString(),
                    "co" to (400..500).random().toString(),
                    "so2" to (1..10).random().toString(),
                    "o3" to (5..20).random().toString(),
                    "temperature" to (20..35).random().toDouble()
                )

                locations.add(locationDataPoint)

                // Add data to location collection
                if (locationData[locationName] == null) {
                    locationData[locationName] = mutableListOf()
                }
                locationData[locationName]?.add(locationDataPoint)
            }
            droneData[droneName] = locations
        }
        return Pair(droneData, locationData)
    }

    private fun uploadDroneAndLocationDataToFirestore() {

        // Generate drone and location data
        val (droneData, locationData) = generateData(37.7749, -122.4194, 4, 5)

        // Upload Drone Data
        db.collection("drones")
            .document("allDrones")
            .set(droneData)
            .addOnSuccessListener {
                Log.d("db","Drone data uploaded successfully")
            }
            .addOnFailureListener { e ->
                Log.d("db","Error uploading drone data: ${e.message}")
            }

        // Upload Location Data
        db.collection("locations")
            .document("allLocations")
            .set(locationData)
            .addOnSuccessListener {
                Log.d("dbLocation","Location data uploaded successfully")
            }
            .addOnFailureListener { e ->
                Log.d("dbLocation","Error uploading location data: ${e.message}")
            }
    }
    private fun setupClusterManager() {
        clusterManager = ClusterManager(requireContext(), mMap)

        // Set a custom renderer
        clusterManager.renderer = CustomClusterRenderer(requireContext(), mMap, clusterManager)

        // Link cluster manager to map events
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        // Handle cluster marker clicks
        clusterManager.setOnClusterItemClickListener { clusterMarker ->
            AppConstants.showDetailsDialog(clusterMarker, requireContext())
            true
        }
    }
    private fun addMarkersToMap() {
        clusterManager.clearItems()
        stations.forEach { station ->
            val lat = station.lat.toDoubleOrNull()
            val lon = station.lon.toDoubleOrNull()
            if (lat != null && lon != null) {
                val aqiData = station.airComponents
                val clusterMarker = ClusterMarker(
                    LatLng(lat, lon),
                    station.stationname,
                    aqiData
                )
                clusterManager.addItem(clusterMarker)
                //zoomToLocation(mMap, LatLng(lat, lon))
                Log.d("map", "added: ${station.stationname}, ${station.lat}, ${station.lon}")
            }
        }
        clusterManager.cluster()
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng, waypoints: List<LatLng>? = null): String {
        val originParam = "origin=${origin.latitude},${origin.longitude}"
        val destParam = "destination=${dest.latitude},${dest.longitude}"
        val waypointsParam = waypoints?.joinToString("|") { "${it.latitude},${it.longitude}" }?.let { "waypoints=$it" }
        val key = "AIzaSyDd4gRN7vvt0t0vACMAa5tKuTy5Pqze24Y" // Replace with your API key

        return "https://maps.googleapis.com/maps/api/directions/json?$originParam&$destParam&$waypointsParam&key=$key"
    }

    private fun drawRoute(locations: List<LatLng>) {
        Log.d("drawRoute", "begin")
        GlobalScope.launch(Dispatchers.Main) {
            try {
                // Split locations into origin, waypoints, and destination
                val origin = locations.first()
                val dest = locations.last()
                val waypoints = if (locations.size > 2) locations.subList(1, locations.size - 1) else null

                fetchRoute(origin, dest, waypoints) { routePoints ->
                    if (routePoints.isNotEmpty()) {
                        Log.d("route", routePoints.toString())
                        val polylineOptions = PolylineOptions()
                            .addAll(routePoints)
                            .width(10f)
                            .color(Color.RED)
                            .geodesic(true)
                            .clickable(true)


                        mMap.addPolyline(polylineOptions)


                    }
                }
            } catch (e: IOException) {
                Log.e("fetchRoute", "Error fetching route", e)
                // Handle error (e.g., show error message)
            }
        }
    }

    private suspend fun fetchRoute(
        origin: LatLng,
        dest: LatLng,
        waypoints: List<LatLng>? = null,
        callback: (List<LatLng>) -> Unit
    ) {
        val client = OkHttpClient()
        withContext(Dispatchers.IO) {
            val url = getDirectionsUrl(origin, dest, waypoints)
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseText = response.body?.string() ?: ""
                val directionsResponse = Gson().fromJson(responseText, DirectionsResponse::class.java)

                if (directionsResponse.routes.isNotEmpty()) {
                    val route = directionsResponse.routes[0]
                    val decodedPath = PolyUtil.decode(route.overview_polyline.points)
                    callback(decodedPath)
                } else {
                    throw IOException("No routes found")
                }
            }
        }
    }

    private fun zoomToLocation(map: GoogleMap, location: LatLng, zoomLevel: Float = 10f) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, zoomLevel)
        map.animateCamera(cameraUpdate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val overview_polyline: OverviewPolyline
)

data class OverviewPolyline(
    val points: String
)

data class Location(
    val latitude: Double,
    val longitude: Double
)
