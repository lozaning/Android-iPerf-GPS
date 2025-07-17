package com.example.androidproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.example.androidproject.databinding.ActivityMainBinding
import com.example.androidproject.data.TestDatabase
import com.example.androidproject.data.TestResult
import com.example.androidproject.map.LatLng
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: TestDatabase
    
    private var currentLocation: Location? = null
    
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
                Toast.makeText(this, "Location permission required for GPS tracking", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = TestDatabase.getDatabase(this)
        
        setupUI()
        requestLocationPermission()
        loadAllTestData()
    }

    private fun setupUI() {
        // Mark AP button - launches Mark AP activity
        binding.markApButton.setOnClickListener {
            launchMarkApActivity()
        }
        
        // Export button - launches Export activity
        binding.exportButton.setOnClickListener {
            launchExportActivity()
        }
        
        // Removed OSM toggle - always use satellite view
        
        // Recenter button
        binding.recenterButton.setOnClickListener {
            recenterMap()
        }
    }
    
    
    private fun recenterMap() {
        binding.mainMap.recenterOnUser()
    }

    private fun launchMarkApActivity() {
        val intent = Intent(this, MarkApActivity::class.java)
        startActivity(intent)
    }
    
    private fun launchExportActivity() {
        val intent = Intent(this, ExportActivity::class.java)
        startActivity(intent)
    }

    private fun loadAllTestData() {
        lifecycleScope.launch {
            try {
                // Load all test results from all sessions
                database.testDao().getAllResults().collect { results ->
                    // Update map with all historical data
                    binding.mainMap.setTestResults(results)
                    
                    // Load all sessions to show AP locations
                    database.testDao().getAllSessions().collect { sessions ->
                        sessions.forEach { session ->
                            if (session.apLatitude != 0.0 && session.apLongitude != 0.0) {
                                binding.mainMap.setApLocation(LatLng(session.apLatitude, session.apLongitude))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading test data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mainMap.onResume()
        
        // Refresh data when returning to main screen
        loadAllTestData()
        
        // Center map on current location
        getCurrentLocation { location ->
            location?.let {
                binding.mainMap.setUserLocation(LatLng(it.latitude, it.longitude))
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        binding.mainMap.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        binding.mainMap.onDestroy()
    }
    


    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            getCurrentLocation()
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
        ).addOnSuccessListener { location: Location? ->
            currentLocation = location
            callback?.invoke(location)
        }.addOnFailureListener { e ->
            callback?.invoke(null)
        }
    }






}