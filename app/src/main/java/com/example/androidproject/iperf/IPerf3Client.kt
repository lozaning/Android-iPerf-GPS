package com.example.androidproject.iperf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class IPerf3Client {
    data class TestResult(
        val speedMbps: Double,
        val durationSeconds: Double,
        val bytesTransferred: Long,
        val success: Boolean,
        val error: String? = null
    )
    
    suspend fun runTest(
        serverHost: String,
        serverPort: Int = 5201,
        testDurationSeconds: Int = 10,
        onProgress: (progress: Int, bytesTransferred: Long) -> Unit = { _, _ -> }
    ): TestResult = withContext(Dispatchers.IO) {
        
        try {
            val socket = Socket(serverHost, serverPort)
            socket.soTimeout = 30000 // 30 second timeout
            
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()
            
            // Send iPerf3 client parameters
            sendIPerf3Parameters(outputStream, testDurationSeconds)
            
            // Wait for server acknowledgment
            val ack = ByteArray(4)
            inputStream.read(ack)
            
            val startTime = System.currentTimeMillis()
            val endTime = startTime + (testDurationSeconds * 1000L)
            var totalBytesTransferred = 0L
            
            val buffer = ByteArray(8192)
            var lastProgressUpdate = 0L
            
            while (System.currentTimeMillis() < endTime) {
                val currentTime = System.currentTimeMillis()
                val bytesToSend = min(buffer.size, 8192)
                
                try {
                    outputStream.write(buffer, 0, bytesToSend)
                    totalBytesTransferred += bytesToSend
                    
                    // Update progress every 100ms
                    if (currentTime - lastProgressUpdate > 100) {
                        val elapsed = currentTime - startTime
                        val progress = (elapsed * 100 / (testDurationSeconds * 1000L)).toInt()
                        onProgress(progress.coerceAtMost(100), totalBytesTransferred)
                        lastProgressUpdate = currentTime
                    }
                    
                } catch (e: IOException) {
                    // Connection might be closed by server, this is normal
                    break
                }
            }
            
            // Send test completion signal
            outputStream.flush()
            
            val actualDuration = (System.currentTimeMillis() - startTime) / 1000.0
            val speedMbps = (totalBytesTransferred * 8.0) / (actualDuration * 1024.0 * 1024.0)
            
            socket.close()
            
            TestResult(
                speedMbps = speedMbps,
                durationSeconds = actualDuration,
                bytesTransferred = totalBytesTransferred,
                success = true
            )
            
        } catch (e: Exception) {
            TestResult(
                speedMbps = 0.0,
                durationSeconds = 0.0,
                bytesTransferred = 0L,
                success = false,
                error = e.message
            )
        }
    }
    
    suspend fun runTestByDataAmount(
        serverHost: String,
        serverPort: Int = 5201,
        dataMB: Double,
        onProgress: (progress: Int, bytesTransferred: Long) -> Unit = { _, _ -> }
    ): TestResult = withContext(Dispatchers.IO) {
        
        try {
            val socket = Socket(serverHost, serverPort)
            socket.soTimeout = 60000 // 60 second timeout for data mode
            
            val outputStream = socket.getOutputStream()
            
            // Send parameters for data amount mode
            sendIPerf3ParametersForData(outputStream, dataMB)
            
            val targetBytes = (dataMB * 1024 * 1024).toLong()
            val buffer = ByteArray(8192)
            var totalBytesTransferred = 0L
            val startTime = System.currentTimeMillis()
            
            while (totalBytesTransferred < targetBytes) {
                val bytesToSend = min(buffer.size, (targetBytes - totalBytesTransferred).toInt())
                outputStream.write(buffer, 0, bytesToSend)
                totalBytesTransferred += bytesToSend
                
                val progress = ((totalBytesTransferred.toDouble() / targetBytes) * 100).toInt()
                onProgress(progress, totalBytesTransferred)
                
                // Add small delay to prevent overwhelming the server
                if (totalBytesTransferred % (1024 * 1024) == 0L) {
                    Thread.sleep(10)
                }
            }
            
            socket.close()
            
            val endTime = System.currentTimeMillis()
            val actualDuration = (endTime - startTime) / 1000.0
            val speedMbps = (totalBytesTransferred * 8.0) / (actualDuration * 1_000_000.0)
            
            TestResult(
                speedMbps = speedMbps,
                durationSeconds = actualDuration,
                bytesTransferred = totalBytesTransferred,
                success = true
            )
            
        } catch (e: Exception) {
            TestResult(
                speedMbps = 0.0,
                durationSeconds = 0.0,
                bytesTransferred = 0L,
                success = false,
                error = e.message
            )
        }
    }
    
    private fun sendIPerf3Parameters(outputStream: java.io.OutputStream, duration: Int) {
        // Simple iPerf3 parameter structure
        val parameterString = """
            {
                "tcp": true,
                "omit": 0,
                "time": $duration,
                "parallel": 1,
                "len": 8192
            }
        """.trimIndent()
        
        val paramBytes = parameterString.toByteArray()
        val buffer = ByteBuffer.allocate(4 + paramBytes.size)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.putInt(paramBytes.size)
        buffer.put(paramBytes)
        
        outputStream.write(buffer.array())
        outputStream.flush()
    }
    
    private fun sendIPerf3ParametersForData(outputStream: java.io.OutputStream, dataMB: Double) {
        // iPerf3 parameter structure for data amount mode
        val parameterString = """
            {
                "tcp": true,
                "omit": 0,
                "bytes": ${(dataMB * 1024 * 1024).toLong()},
                "parallel": 1,
                "len": 8192
            }
        """.trimIndent()
        
        val paramBytes = parameterString.toByteArray()
        val buffer = ByteBuffer.allocate(4 + paramBytes.size)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.putInt(paramBytes.size)
        buffer.put(paramBytes)
        
        outputStream.write(buffer.array())
        outputStream.flush()
    }
}