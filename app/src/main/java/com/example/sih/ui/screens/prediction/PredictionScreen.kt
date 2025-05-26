package com.example.sih.ui.screens.prediction

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.sih.repository.AQIPredictor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import kotlinx.serialization.Serializable

// Data classes for API
@Serializable
data class PredictionRequest(
    val co: Float,
    val temperature: Float,
    val humidity: Float
)

@Serializable
data class PredictionResponse(
    val CO_AQI: Float,
    val Temperature: Float,
    val Humidity: Float
)

// API Interface
interface AQIApiService {
    @POST("predict")
    suspend fun getPrediction(@Body request: PredictionRequest): PredictionResponse
}

@Composable
fun PredictionScreen() {
    var isApiLoading by remember { mutableStateOf(false) }
    var apiPrediction by remember { mutableStateOf<PredictionResponse?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Initialize Retrofit
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://kanhaiya-aqi-1.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apiService = remember { retrofit.create(AQIApiService::class.java) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "AQI Prediction",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        if (apiPrediction == null) {
            // Initial state with guidance
            EmptyPredictionState()
        }

        // Prediction Button
        Button(
            onClick = {
                isApiLoading = true
                apiPrediction = null
                coroutineScope.launch {
                    try {
                        val request = PredictionRequest(
                            co = 2.5f,
                            temperature = 25f,
                            humidity = 60f
                        )
                        val response = apiService.getPrediction(request)
                        Log.d("predict", "PredictionResponse: $response")
                        apiPrediction = response
                    } catch (e: Exception) {
                        Log.d("predict", "PredictionError: $e")
                    } finally {
                        isApiLoading = false
                    }
                }
            },
            enabled = !isApiLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            if (isApiLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Get Next Day Prediction",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Prediction Results
        AnimatedVisibility(
            visible = apiPrediction != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            apiPrediction?.let { pred ->
                PredictionResults(pred)
            }
        }
    }
}

@Composable
private fun EmptyPredictionState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Air,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Get Tomorrow's Air Quality Forecast",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Click the button below to predict tomorrow's air quality based on current conditions.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PredictionResults(prediction: PredictionResponse) {
    val isDarkTheme = isSystemInDarkTheme()
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Main Prediction Card
        Card(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDarkTheme) 4.dp else 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Next Day Prediction Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Prediction Values
                PredictionResultRow(
                    icon = Icons.Default.Co2,
                    label = "CO AQI",
                    value = prediction.CO_AQI.format(2)
                )
                PredictionResultRow(
                    icon = Icons.Default.Thermostat,
                    label = "Temperature",
                    value = "${prediction.Temperature.format(2)}°C"
                )
                PredictionResultRow(
                    icon = Icons.Default.WaterDrop,
                    label = "Humidity",
                    value = "${prediction.Humidity.format(2)}%"
                )
            }
        }

        // AQI Interpretation Card
        val (aqiLevel, aqiColor, suggestions) = getAQIInterpretation(prediction.CO_AQI)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = aqiColor.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = aqiColor
                    )
                    Text(
                        text = "Air Quality: $aqiLevel",
                        style = MaterialTheme.typography.titleMedium,
                        color = aqiColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = suggestions,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun PredictionResultRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getAQIInterpretation(aqi: Float): Triple<String, Color, String> {
    return when {
        aqi <= 50 -> Triple(
            "Good",
            Color(0xFF4CAF50),
            "Air quality is satisfactory, and air pollution poses little or no risk. Perfect for outdoor activities!"
        )
        aqi <= 100 -> Triple(
            "Moderate",
            Color(0xFFFFC107),
            "Air quality is acceptable. However, there may be a risk for some people, particularly those who are unusually sensitive to air pollution."
        )
        aqi <= 150 -> Triple(
            "Unhealthy for Sensitive Groups",
            Color(0xFFFF9800),
            "Members of sensitive groups may experience health effects. The general public is less likely to be affected. Consider reducing prolonged outdoor activities."
        )
        aqi <= 200 -> Triple(
            "Unhealthy",
            Color(0xFFFF5722),
            "Everyone may begin to experience health effects. Members of sensitive groups may experience more serious health effects. Avoid prolonged outdoor activities."
        )
        aqi <= 300 -> Triple(
            "Very Unhealthy",
            Color(0xFFE91E63),
            "Health alert: The risk of health effects is increased for everyone. Avoid outdoor activities and wear a mask if going outside."
        )
        else -> Triple(
            "Hazardous",
            Color(0xFF9C27B0),
            "Health warning of emergency conditions: everyone is more likely to be affected. Avoid all outdoor activities and wear appropriate protection."
        )
    }
}

fun Float.format(digits: Int) = "%.${digits}f".format(this)
