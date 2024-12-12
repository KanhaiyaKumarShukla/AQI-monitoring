package com.example.sih.common.constants

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sih.R
import com.example.sih.api.service.APIService
import com.example.sih.model.Aqi
import com.example.sih.model.AqiBreakpoints
import com.example.sih.model.AqiData
import com.example.sih.model.ClusterMarker
import com.example.sih.socket.models.AirComponent
import com.example.sih.socket.models.Station
import com.example.sih.socket.models.Stations
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data object AppConstants{
    private val pm25Breakpoints = listOf(
        AqiBreakpoints(0.0, 30.0, 0, 50),
        AqiBreakpoints(31.0, 60.0, 51, 100),
        AqiBreakpoints(61.0, 90.0, 101, 200),
        AqiBreakpoints(91.0, 120.0, 201, 300),
        AqiBreakpoints(121.0, 250.0, 301, 400),
        AqiBreakpoints(251.0, Double.MAX_VALUE, 401, 500)
    )

    private val pm10Breakpoints = listOf(
        AqiBreakpoints(0.0, 50.0, 0, 50),
        AqiBreakpoints(51.0, 100.0, 51, 100),
        AqiBreakpoints(101.0, 250.0, 101, 200),
        AqiBreakpoints(251.0, 350.0, 201, 300),
        AqiBreakpoints(351.0, 430.0, 301, 400),
        AqiBreakpoints(431.0, Double.MAX_VALUE, 401, 500)
    )

    private val coBreakpoints = listOf(
        AqiBreakpoints(0.0, 1.0, 0, 50),
        AqiBreakpoints(1.1, 2.0, 51, 100),
        AqiBreakpoints(2.1, 10.0, 101, 200),
        AqiBreakpoints(10.1, 17.0, 201, 300),
        AqiBreakpoints(17.1, 34.0, 301, 400),
        AqiBreakpoints(34.1, Double.MAX_VALUE, 401, 500)
    )

    private val o3Breakpoints = listOf(
        AqiBreakpoints(0.0, 50.0, 0, 50),
        AqiBreakpoints(51.0, 100.0, 51, 100),
        AqiBreakpoints(101.0, 168.0, 101, 200),
        AqiBreakpoints(169.0, 208.0, 201, 300),
        AqiBreakpoints(209.0, 748.0, 301, 400),
        AqiBreakpoints(749.0, Double.MAX_VALUE, 401, 500)
    )

    private val no2Breakpoints = listOf(
        AqiBreakpoints(0.0, 40.0, 0, 50),
        AqiBreakpoints(41.0, 80.0, 51, 100),
        AqiBreakpoints(81.0, 180.0, 101, 200),
        AqiBreakpoints(181.0, 280.0, 201, 300),
        AqiBreakpoints(281.0, 400.0, 301, 400),
        AqiBreakpoints(401.0, Double.MAX_VALUE, 401, 500)
    )

    private val so2Breakpoints = listOf(
        AqiBreakpoints(0.0, 40.0, 0, 50),
        AqiBreakpoints(41.0, 80.0, 51, 100),
        AqiBreakpoints(81.0, 380.0, 101, 200),
        AqiBreakpoints(381.0, 800.0, 201, 300),
        AqiBreakpoints(801.0, 1600.0, 301, 400),
        AqiBreakpoints(1601.0, Double.MAX_VALUE, 401, 500)
    )

    fun calculateOverallAqi(aqiData: AqiData): Int {

        val pm25 = aqiData.pm25.toDoubleOrNull() ?: 0.0
        val pm10 = aqiData.pm10.toDoubleOrNull() ?: 0.0
        val o3 = aqiData.o3.toDoubleOrNull() ?: 0.0
        val co = aqiData.co.toDoubleOrNull() ?: 0.0
        val no2 = aqiData.no2.toDoubleOrNull() ?: 0.0
        val so2 = aqiData.so2.toDoubleOrNull() ?: 0.0

        val aqiValues = listOf(
            calculateSubIndex(pm25, AppConstants.pm25Breakpoints),
            calculateSubIndex(pm10, AppConstants.pm10Breakpoints),
            calculateSubIndex(co, AppConstants.coBreakpoints),
            calculateSubIndex(o3, AppConstants.o3Breakpoints),
            calculateSubIndex(no2, AppConstants.no2Breakpoints),
            calculateSubIndex(so2, AppConstants.so2Breakpoints)
        )
        // Return the highest AQI as the overall AQI
        return aqiValues.maxOrNull() ?: -1
    }
    private fun calculateSubIndex(cp: Double, breakpoints: List<AqiBreakpoints>): Int {
        for (breakpoint in breakpoints) {
            if (cp in breakpoint.bplo..breakpoint.bphi) {
                return ((breakpoint.ihi - breakpoint.ilo) / (breakpoint.bphi - breakpoint.bplo) * (cp - breakpoint.bplo) + breakpoint.ilo).toInt()
            }
        }
        return -1 // Return -1 if the concentration does not fall within any range
    }

    fun getAqiColor(aqi: Int): Int {
        return when (aqi) {
            in 0..50 -> R.color.aqi_good
            in 51..100 -> R.color.aqi_moderate
            in 101..150 -> R.color.aqi_unhealthy_sensitive
            in 151..200 -> R.color.aqi_unhealthy
            in 201..300 -> R.color.aqi_very_unhealthy
            in 301..500 -> R.color.aqi_hazardous
            else -> R.color.black
        }
    }

    fun getAqiSuggestion(aqi: Int): String {
        return when (aqi) {
            in 0..50 -> "Air quality is considered satisfactory, and air pollution poses little or no risk."
            in 51..100 -> "Air quality is acceptable; however, for some pollutants, there may be a moderate health concern for a very small number of people who are sensitive to air pollution."
            in 101..150 -> "Members of sensitive groups may experience health effects. The general public is not likely to be affected."
            in 151..200 -> "Everyone may begin to experience health effects; members of sensitive groups may experience more serious health effects."
            in 201..300 -> "Health alert: everyone may experience more serious health effects."
            in 301..500 -> "Health warning of emergency conditions. The entire population is more likely to be affected."
            else -> "AQI value is out of range or invalid. Please check the data."
        }
    }

    // we can use this to customize the dialog box according to layout:
    fun showDetailsDialog(clusterMarker: ClusterMarker, context: Context) {
        val station = clusterMarker.title
        val dialogView = LayoutInflater.from(context).inflate(R.layout.aqi_dialog_box_layout, null)
        val airComponentsRecycler = dialogView.findViewById<RecyclerView>(R.id.recycler_air_components)
        val airComponentsData = getAirComponentsData(clusterMarker.getAqiData())
        airComponentsRecycler.layoutManager = LinearLayoutManager(context)
        airComponentsRecycler.adapter = AirComponentsAdapter(airComponentsData)

        val titleView = LayoutInflater.from(context).inflate(R.layout.dialog_title_detail_layout, null) as TextView
        titleView.text = "${station} Details"
        AlertDialog.Builder(context)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }
    fun getAirComponentsData(aqiData: Aqi): List<Pair<String, String>> {
        val airComponents = mutableListOf<Pair<String, String>>()

        Aqi::class.members.filterIsInstance<kotlin.reflect.KProperty1<Aqi, *>>().forEach { property ->
            val value = property.get(aqiData)
            val unit = aqiData.units[property.name.uppercase()] ?: ""
            if (value != null && property.name != "units") {
                airComponents.add(property.name to "$value $unit")
            }
        }

        return airComponents
    }


    /*
    fun showAqiDataDialog(aqiData: Aqi, context: Context) {
       // Prepare the message dynamically
       val messageBuilder = StringBuilder()

       // Reflection is used here to iterate over all properties of Aqi and build the dialog content
       Aqi::class.members.filterIsInstance<kotlin.reflect.KProperty1<Aqi, *>>().forEach { property ->
           val value = property.get(aqiData)
           val unit = aqiData.units[property.name.uppercase()] ?: ""
           if (value != null && property.name != "units") {
               messageBuilder.append("${property.name}: $value $unit\n")
           }
       }

       // Create and show the dialog
       val dialog = AlertDialog.Builder(context)
           .setTitle("AQI Details")
           .setMessage(messageBuilder.toString().trim())
           .setPositiveButton("Close", null)
           .create()

       dialog.show()
    }
         */


    fun resizeBitmap(context: Context, resourceId: Int, width: Int, height: Int): BitmapDescriptor {
       val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
       val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
       return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }

    fun Context.makeToast(message: String, isLong: Boolean = false) {
       Toast.makeText(this, message, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }




    private const val BASE_URL = "https://aqi-fetch-amaan-hussains-projects.vercel.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
       level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
       .addInterceptor(loggingInterceptor)
       .build()

    private val retrofit = Retrofit.Builder()
       .baseUrl(BASE_URL)
       .addConverterFactory(GsonConverterFactory.create())
       .client(client)
       .build()

    val apiService: APIService = retrofit.create(APIService::class.java)
    val cities = listOf(
        "Visakhapatnam", "Vijayawada", "Guntur", "Tirupati", "Kakinada", "Rajahmundry",
        "Itanagar", "Tawang", "Naharlagun", "Ziro", "Pasighat",
        "Guwahati", "Dibrugarh", "Jorhat", "Silchar", "Nagaon", "Tezpur",
        "Patna", "Gaya", "Bhagalpur", "Muzaffarpur", "Purnia", "Munger",
        "Raipur", "Bilaspur", "Durg", "Bhilai", "Korba",
        "Panaji", "Margao", "Vasco da Gama", "Mapusa", "Ponda",
        "Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar", "Gandhinagar",
        "Gurugram", "Faridabad", "Panchkula", "Ambala", "Hisar",
        "Shimla", "Dharamshala", "Kullu", "Manali", "Solan",
        "Ranchi", "Jamshedpur", "Dhanbad", "Bokaro", "Hazaribagh",
        "Bengaluru", "Mysuru", "Hubli", "Mangaluru", "Belagavi", "Davangere",
        "Thiruvananthapuram", "Kochi", "Kozhikode", "Kottayam", "Thrissur", "Malappuram",
        "Bhopal", "Indore", "Gwalior", "Ujjain", "Jabalpur", "Sagar",
        "Mumbai", "Pune", "Nagpur", "Nashik", "Thane", "Aurangabad",
        "Imphal", "Shillong", "Aizawl", "Kohima", "Agartala",
        "Delhi", "Chandigarh", "Jaipur", "Lucknow", "Kolkata", "Chennai", "Bengaluru"
    )

    fun setGradientBackground(view: View, startColor: String, endColor: String, angle: Int = 0, cornerRadius: Float = 0f) {
        // Create a GradientDrawable with a linear gradient
        val gradientDrawable = GradientDrawable(
            // Set gradient direction based on angle
            GradientDrawable.Orientation.LEFT_RIGHT, // You can change this to dynamic based on the angle
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        )

        // Set additional properties
        gradientDrawable.gradientType = GradientDrawable.LINEAR_GRADIENT
        gradientDrawable.cornerRadius = cornerRadius

        // Set the gradient background to the provided view
        view.background = gradientDrawable
    }


}