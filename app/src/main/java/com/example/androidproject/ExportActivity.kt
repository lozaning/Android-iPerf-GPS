package com.example.androidproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject.databinding.ActivityExportBinding
import com.example.androidproject.data.TestDatabase
import com.example.androidproject.data.TestSession
import com.example.androidproject.data.KmlExporter
import com.example.androidproject.data.TestResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class ExportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExportBinding
    private lateinit var database: TestDatabase
    private lateinit var sessionAdapter: SessionAdapter
    private var allSessions: List<TestSession> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = TestDatabase.getDatabase(this)
        
        setupUI()
        loadSessions()
    }
    
    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Setup RecyclerView
        sessionAdapter = SessionAdapter { session, isChecked ->
            // Handle checkbox change
            session.isSelected = isChecked
            updateButtonStates()
        }
        
        binding.sessionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExportActivity)
            adapter = sessionAdapter
        }
        
        // Setup buttons
        binding.selectAllButton.setOnClickListener {
            toggleSelectAll()
        }
        
        binding.deleteButton.setOnClickListener {
            deleteSelectedSessions()
        }
        
        binding.exportCsvButton.setOnClickListener {
            exportSelectedSessionsToCsv()
        }
        
        updateButtonStates()
    }
    
    private fun loadSessions() {
        lifecycleScope.launch {
            database.testDao().getAllSessions().collect { sessions ->
                allSessions = sessions.map { it.copy() } // Create mutable copies
                sessionAdapter.submitList(allSessions)
                updateButtonStates()
            }
        }
    }
    
    private fun toggleSelectAll() {
        val allSelected = allSessions.all { it.isSelected }
        allSessions.forEach { it.isSelected = !allSelected }
        sessionAdapter.notifyDataSetChanged()
        updateButtonStates()
    }
    
    private fun deleteSelectedSessions() {
        val selectedSessions = allSessions.filter { it.isSelected }
        
        if (selectedSessions.isEmpty()) {
            Toast.makeText(this, "No sessions selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        val message = if (selectedSessions.size == 1) {
            "Delete this session permanently?"
        } else {
            "Delete ${selectedSessions.size} sessions permanently?"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Delete Sessions")
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        selectedSessions.forEach { session ->
                            database.testDao().deleteSession(session.id)
                        }
                        Toast.makeText(this@ExportActivity, "Sessions deleted", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@ExportActivity, "Error deleting sessions: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    
    private fun exportSelectedSessionsToCsv() {
        val selectedSessions = allSessions.filter { it.isSelected }
        
        if (selectedSessions.isEmpty()) {
            Toast.makeText(this, "No sessions selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                // Show progress
                binding.exportCsvButton.isEnabled = false
                binding.exportCsvButton.text = "Exporting..."
                
                // Create enhanced sessions with test results
                val sessionsWithResults = mutableListOf<Pair<TestSession, List<TestResult>>>()
                
                for (session in selectedSessions) {
                    val results = database.testDao().getResultsForSession(session.id).first()
                    sessionsWithResults.add(Pair(session, results))
                }
                
                // Export to CSV
                val kmlExporter = KmlExporter(this@ExportActivity, database)
                val csvFile = kmlExporter.exportSessionsToCsv(sessionsWithResults)
                
                // Share the file
                shareCsvFile(csvFile)
                
                Toast.makeText(this@ExportActivity, "CSV file exported successfully", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@ExportActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Reset button state
                binding.exportCsvButton.isEnabled = true
                binding.exportCsvButton.text = "Export CSV"
                updateButtonStates()
            }
        }
    }
    
    private fun shareCsvFile(csvFile: java.io.File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                csvFile
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "GPS Speed Test Results")
                putExtra(Intent.EXTRA_TEXT, "GPS Speed Test results exported as CSV file for analysis")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Share CSV file"))
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun updateButtonStates() {
        val selectedCount = allSessions.count { it.isSelected }
        val totalCount = allSessions.size
        
        binding.selectAllButton.text = if (selectedCount == totalCount && totalCount > 0) {
            "Deselect All"
        } else {
            "Select All"
        }
        
        binding.deleteButton.isEnabled = selectedCount > 0
        binding.exportCsvButton.isEnabled = selectedCount > 0
        
        binding.deleteButton.text = if (selectedCount > 0) {
            "Delete ($selectedCount)"
        } else {
            "Delete"
        }
        
        binding.exportCsvButton.text = if (selectedCount > 0) {
            "Export CSV ($selectedCount)"
        } else {
            "Export CSV"
        }
    }
    
    private class SessionAdapter(
        private val onCheckboxChanged: (TestSession, Boolean) -> Unit
    ) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {
        
        private var sessions: List<TestSession> = emptyList()
        
        fun submitList(newSessions: List<TestSession>) {
            sessions = newSessions
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): SessionViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_session, parent, false)
            return SessionViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
            holder.bind(sessions[position])
        }
        
        override fun getItemCount(): Int = sessions.size
        
        inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val checkbox: CheckBox = itemView.findViewById(R.id.sessionCheckbox)
            private val title: TextView = itemView.findViewById(R.id.sessionTitle)
            private val details: TextView = itemView.findViewById(R.id.sessionDetails)
            private val location: TextView = itemView.findViewById(R.id.sessionLocation)
            
            fun bind(session: TestSession) {
                checkbox.isChecked = session.isSelected
                title.text = session.description
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val date = dateFormat.format(Date(session.startTime))
                details.text = "Server: ${session.iperfServer} | Duration: ${session.testDuration}s | Date: $date"
                
                location.text = if (session.apLatitude != 0.0 && session.apLongitude != 0.0) {
                    "AP Location: ${String.format("%.4f", session.apLatitude)}, ${String.format("%.4f", session.apLongitude)}"
                } else {
                    "AP Location: Not set"
                }
                
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    onCheckboxChanged(session, isChecked)
                }
                
                itemView.setOnClickListener {
                    checkbox.isChecked = !checkbox.isChecked
                }
            }
        }
    }
}