package com.example.androidproject

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.example.androidproject.databinding.ActivityMarkApBinding
import com.example.androidproject.data.TestDatabase
import com.example.androidproject.data.TestSession
import com.example.androidproject.data.TestResult
import com.example.androidproject.iperf.IPerf3Client
import com.example.androidproject.map.LatLng
import kotlinx.coroutines.*
import java.util.concurrent.CancellationException
import kotlin.math.*

class MarkApActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMarkApBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private lateinit var database: TestDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    
    private var currentLocation: Location? = null
    private var isTestRunning = false
    private var isAutoMode = false
    private var autoTestJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())
    
    // Session data
    private var sessionId: Long = 0
    private var currentSession: TestSession? = null
    private var apLocation: Location? = null
    private var previousResult: TestResult? = null
    private var currentResult: TestResult? = null
    
    // Sensor data
    private var lastAccelerometerValues = FloatArray(3)
    private var lastMagnetometerValues = FloatArray(3)
    private var currentAzimuth = 0f
    private var currentPitch = 0f
    private var currentRoll = 0f
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }
            else -> {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarkApBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        database = TestDatabase.getDatabase(this)
        sharedPreferences = getSharedPreferences("GPSSpeedTest", Context.MODE_PRIVATE)
        
        setupSensors()
        setupUI()
        requestLocationPermission()
        startLocationUpdates()
    }
    
    private fun setupSensors() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }
    
    private fun setupUI() {
        // Manual test button
        binding.manualTestButton.setOnClickListener {
            if (!isTestRunning && !isAutoMode) {
                runManualTest()
            }
        }
        
        // Auto test button
        binding.autoTestButton.setOnClickListener {
            toggleAutoMode()
        }
        
        // Done button
        binding.doneButton.setOnClickListener {
            finish()
        }
        
        // OSM toggle button
        binding.markApSatelliteToggleButton.setOnClickListener {
            toggleOsmMode()
        }
        
        // Recenter button
        binding.markApRecenterButton.setOnClickListener {
            recenterMap()
        }
        
        // Update initial state
        updateUI()
    }
    
    private fun toggleOsmMode() {
        // Always use satellite mode - ignore toggle
    }
    
    private fun recenterMap() {
        binding.markApMap.recenterOnUser()
    }
    
    private fun showIpInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter iPerf Server IP")
        
        val input = android.widget.EditText(this)
        input.hint = "192.168.1.100:5201"
        
        // Load and prefill the last used IP address
        val lastUsedIp = sharedPreferences.getString("last_iperf_server", "")
        if (!lastUsedIp.isNullOrEmpty()) {
            input.setText(lastUsedIp)
            input.setSelection(lastUsedIp.length) // Place cursor at end
        }
        
        builder.setView(input)
        
        builder.setPositiveButton("OK") { _, _ ->
            val serverInput = input.text.toString().trim()
            if (serverInput.isNotEmpty()) {
                // Save the IP address for next time
                sharedPreferences.edit()
                    .putString("last_iperf_server", serverInput)
                    .apply()
                
                createSession(serverInput)
            } else {
                Toast.makeText(this, "Please enter a valid IP address", Toast.LENGTH_SHORT).show()
            }
        }
        
        builder.setNegativeButton("Cancel") { _, _ ->
            finish()
        }
        
        builder.show()
    }
    
    private fun createSession(serverInput: String) {
        lifecycleScope.launch {
            try {
                // Mark AP location at current GPS position
                getCurrentLocation { location ->
                    if (location != null) {
                        apLocation = location
                        
                        // Create test session
                        val session = TestSession(
                            description = "Field Test Session",
                            iperfServer = serverInput,
                            testDuration = 10, // Default 10 seconds
                            apLatitude = location.latitude,
                            apLongitude = location.longitude,
                            startTime = System.currentTimeMillis()
                        )
                        
                        lifecycleScope.launch {
                            sessionId = database.testDao().insertSession(session)
                            currentSession = session
                            
                            // Update map with AP location
                            binding.markApMap.setApLocation(LatLng(location.latitude, location.longitude))
                            
                            Toast.makeText(this@MarkApActivity, "AP location marked at current position", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MarkApActivity, "Unable to get GPS location", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MarkApActivity, "Error creating session: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun runManualTest() {
        currentSession ?: return
        
        // Get test parameters from UI
        val testValue = binding.testValueInput.text.toString().toIntOrNull() ?: 10
        val isDurationMode = binding.durationMode.isChecked
        
        lifecycleScope.launch {
            if (isDurationMode) {
                // Duration mode: run test for specified seconds
                runTest(testValue.toDouble(), null)
            } else {
                // Data mode: run test until specified MB is transferred
                runTest(null, testValue.toDouble())
            }
        }
    }
    
    private fun toggleAutoMode() {
        isAutoMode = !isAutoMode
        
        if (isAutoMode) {
            startAutoMode()
        } else {
            stopAutoMode()
        }
        
        updateUI()
    }
    
    private fun startAutoMode() {
        autoTestJob = lifecycleScope.launch {
            try {
                while (isActive && isAutoMode) {
                    if (!isTestRunning) {
                        // Get current settings from UI
                        val testValue = binding.testValueInput.text.toString().toIntOrNull() ?: 10
                        val isDurationMode = binding.durationMode.isChecked
                        
                        if (isDurationMode) {
                            // Duration mode: use specified seconds
                            runTest(testValue.toDouble(), null)
                        } else {
                            // Data mode: use specified MB
                            runTest(null, testValue.toDouble())
                        }
                        
                        delay(1000) // Wait 1 second between tests
                    } else {
                        delay(100) // Check again in 100ms
                    }
                }
            } catch (e: CancellationException) {
                // This is expected when auto mode is stopped, don't show error
            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(this@MarkApActivity, "Auto mode error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun stopAutoMode() {
        autoTestJob?.cancel()
        autoTestJob = null
        isAutoMode = false
        updateUI()
    }
    
    private suspend fun runTest(durationSeconds: Double?, dataMB: Double?) {
        val session = currentSession ?: return
        val location = currentLocation ?: return
        
        isTestRunning = true
        handler.post { 
            updateUI()
            val mode = if (durationSeconds != null) "Duration: ${durationSeconds.toInt()}s" else "Data: ${dataMB?.toInt()}MB"
            binding.currentResultText.text = "Current: Running test ($mode)..."
        }
        
        try {
            val client = IPerf3Client()
            val parts = session.iperfServer.split(":")
            val host = parts[0]
            val port = if (parts.size > 1) parts[1].toInt() else 5201
            
            val result = if (durationSeconds != null) {
                // Duration mode
                client.runTest(
                    serverHost = host,
                    serverPort = port,
                    testDurationSeconds = durationSeconds.toInt(),
                    onProgress = { _, _ -> }
                )
            } else if (dataMB != null) {
                // Data mode
                client.runTestByDataAmount(
                    serverHost = host,
                    serverPort = port,
                    dataMB = dataMB,
                    onProgress = { _, _ -> }
                )
            } else {
                // Default fallback
                client.runTest(
                    serverHost = host,
                    serverPort = port,
                    testDurationSeconds = 10,
                    onProgress = { _, _ -> }
                )
            }
            
            if (result.success) {
                val distance = apLocation?.let { ap ->
                    calculateDistance(ap.latitude, ap.longitude, location.latitude, location.longitude)
                } ?: 0.0
                
                val testResult = TestResult(
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis(),
                    latitude = location.latitude,
                    longitude = location.longitude,
                    distanceFromAp = distance,
                    speedMbps = result.speedMbps,
                    testDurationSeconds = result.durationSeconds,
                    dataMB = result.bytesTransferred / (1024.0 * 1024.0),
                    azimuth = currentAzimuth,
                    pitch = currentPitch,
                    roll = currentRoll,
                    accuracy = location.accuracy
                )
                
                database.testDao().insertResult(testResult)
                
                handler.post {
                    // Update previous result
                    previousResult = currentResult
                    currentResult = testResult
                    
                    // Update map with new result
                    binding.markApMap.addTestResult(testResult)
                    
                    updateResultsDisplay()
                }
            } else {
                handler.post {
                    binding.currentResultText.text = "Current: Test failed - ${result.error}"
                }
            }
        } catch (e: Exception) {
            handler.post {
                binding.currentResultText.text = "Current: Error - ${e.message}"
            }
        } finally {
            isTestRunning = false
            handler.post { updateUI() }
        }
    }
    
    private fun updateResultsDisplay() {
        previousResult?.let { prev ->
            binding.previousResultText.text = "Previous: ${String.format("%.1f", prev.speedMbps)} Mbps at ${String.format("%.0f", prev.distanceFromAp)}m"
        }
        
        currentResult?.let { curr ->
            binding.currentResultText.text = "Current: ${String.format("%.1f", curr.speedMbps)} Mbps at ${String.format("%.0f", curr.distanceFromAp)}m"
        }
    }
    
    private fun updateUI() {
        // Update GPS display
        currentLocation?.let { location ->
            binding.currentGpsText.text = "GPS: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)} (±${String.format("%.1f", location.accuracy)}m)"
        }
        
        // Update gyro display
        binding.currentGyroText.text = "Gyro: Az ${String.format("%.1f", currentAzimuth)}°, Pitch ${String.format("%.1f", currentPitch)}°, Roll ${String.format("%.1f", currentRoll)}°"
        
        // Update button states
        binding.manualTestButton.isEnabled = !isTestRunning && !isAutoMode
        binding.autoTestButton.text = if (isAutoMode) "Stop Auto" else "Start Auto"
        binding.autoTestButton.isEnabled = !isTestRunning || isAutoMode
        
        // Update test results if no test is running
        if (!isTestRunning) {
            if (previousResult == null && currentResult == null) {
                binding.previousResultText.text = "Previous: No previous test"
                binding.currentResultText.text = "Current: No test running"
            }
        }
    }
    
    private fun startLocationUpdates() {
        // Start periodic location updates
        handler.post(object : Runnable {
            override fun run() {
                getCurrentLocation()
                handler.postDelayed(this, 1000) // Update every second
            }
        })
    }
    
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            getCurrentLocation()
            showIpInputDialog()
        }
    }
    
    private fun getCurrentLocation(callback: ((Location?) -> Unit)? = null) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback?.invoke(null)
            return
        }
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                    return CancellationTokenSource().token
                }
                override fun isCancellationRequested(): Boolean = false
            }
        ).addOnSuccessListener { location ->
            currentLocation = location
            location?.let {
                // Update map with user location
                binding.markApMap.setUserLocation(LatLng(it.latitude, it.longitude))
            }
            callback?.invoke(location)
        }.addOnFailureListener {
            callback?.invoke(null)
        }
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
    
    override fun onResume() {
        super.onResume()
        binding.markApMap.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }
    
    override fun onPause() {
        super.onPause()
        binding.markApMap.onPause()
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    lastAccelerometerValues = it.values.clone()
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    lastMagnetometerValues = it.values.clone()
                }
            }
            updateOrientation()
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
    
    private fun updateOrientation() {
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        
        if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometerValues, lastMagnetometerValues)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            
            currentAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            currentPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            currentRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            
            // Normalize azimuth to 0-360 range
            if (currentAzimuth < 0) currentAzimuth += 360f
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        binding.markApMap.onDestroy()
        autoTestJob?.cancel()
    }
    
}