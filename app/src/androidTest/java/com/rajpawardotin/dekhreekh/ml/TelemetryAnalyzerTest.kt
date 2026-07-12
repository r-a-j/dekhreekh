package com.rajpawardotin.dekhreekh.ml

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rajpawardotin.dekhreekh.domain.models.TelemetryData
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TelemetryAnalyzerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `Initialization with valid model loads successfully`() {
        // Assert that the analyzer can be instantiated without throwing exceptions
        // using the dummy_model.onnx file located in the test assets directory.
        val analyzer = TelemetryAnalyzer(context, "dummy_model.onnx")
        
        // Basic assertion to ensure the instance exists
        assertNotNull(analyzer)
        
        // Clean up the environment explicitly (if the analyzer supports closing)
        analyzer.close()
    }

    @Test
    fun `Inference Execution returns non-null prediction result`() {
        val analyzer = TelemetryAnalyzer(context, "dummy_model.onnx")
        
        val mockData = TelemetryData(
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = 10.0,
            accuracy = 5.0f,
            speed = 3.5f,
            timestamp = System.currentTimeMillis()
        )
        
        val result = analyzer.analyze(mockData)
        
        // Assert we get a non-null result back from the ML pipeline
        assertNotNull("Inference result should not be null", result)
        
        analyzer.close()
    }

    @Test
    fun `Initialization with missing model file throws Exception`() {
        // Assert that providing a non-existent model name throws an exception
        // (Either an IO Exception or a custom ONNX initialization exception)
        assertThrows(Exception::class.java) {
            val analyzer = TelemetryAnalyzer(context, "non_existent_model.onnx")
            analyzer.close() // Should never reach here
        }
    }
}

// ---------------------------------------------------------
// Barebones implementation to make tests compile
// ---------------------------------------------------------

class TelemetryAnalyzer(private val context: Context, private val modelName: String) {
    
    init {
        // Dummy check to simulate an initialization error for the failing test
        if (modelName == "non_existent_model.onnx") {
            throw java.io.FileNotFoundException("Model not found in assets")
        }
    }
    
    fun analyze(data: TelemetryData): FloatArray? {
        // Return null to ensure the inference test fails
        return null 
    }
    
    fun close() {
        // No-op for compilation
    }
}
