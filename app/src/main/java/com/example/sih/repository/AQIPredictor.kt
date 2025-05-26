package com.example.sih.repository

import android.content.Context
import com.example.sih.model.ModelConfig
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.channels.FileChannel

class AQIPredictor(context: Context) {
    private val interpreter: Interpreter
    private val modelConfig: ModelConfig
    private val nnapiDelegate = NnApiDelegate()

    init {
        // Load model
        val modelFile = loadModelFile(context)
        val options = Interpreter.Options().apply {
            // Add this if using SELECT_TF_OPS
            addDelegate(nnapiDelegate)
        }
        interpreter = Interpreter(modelFile, options)

        // Load config (alternative to scaler.pkl)
        modelConfig = loadConfig(context)
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val assetManager = context.assets
        val assetFileDescriptor = assetManager.openFd("aqi_predictor.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadConfig(context: Context): ModelConfig {
        // Load from JSON or hardcode your scaling values
        return ModelConfig(
            co_mean = 1.25f,
            co_std = 0.3f,
            temp_mean = 25.0f,
            temp_std = 5.0f,
            humidity_mean = 65.0f,
            humidity_std = 15.0f
        )
    }
    fun predict(co: Float, temp: Float, humidity: Float): FloatArray {
        // Normalize inputs
        val normalizedInput = floatArrayOf(
            (co - modelConfig.co_mean) / modelConfig.co_std,
            (temp - modelConfig.temp_mean) / modelConfig.temp_std,
            (humidity - modelConfig.humidity_mean) / modelConfig.humidity_std
        )

        // Create input/output buffers
        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 10, 3), DataType.FLOAT32)
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 3), DataType.FLOAT32)

        // Prepare input (assuming your model expects sequences)
        // For simplicity, we'll repeat the current values 10 times
        val sequenceInput = FloatArray(10 * 3) { index ->
            normalizedInput[index % 3]  // Repeats each value cyclically
        }
        inputBuffer.loadArray(sequenceInput)

        // Run inference
        interpreter.run(inputBuffer.buffer, outputBuffer.buffer)

        return outputBuffer.floatArray
    }

    fun close() {
        interpreter.close()
        nnapiDelegate.close()
    }
}
