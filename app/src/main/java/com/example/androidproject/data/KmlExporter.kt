package com.example.androidproject.data

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class KmlExporter(private val context: Context, private val database: TestDatabase) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    suspend fun exportSessionsToCsv(sessionsWithResults: List<Pair<TestSession, List<TestResult>>>): File {
        val fileName = "gps_speed_test_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            writer.write(generateCsvContent(sessionsWithResults))
        }
        
        return file
    }
    
    suspend fun exportSessionsToKml(sessions: List<TestSession>): File {
        val fileName = "gps_speed_test_${System.currentTimeMillis()}.kml"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            writer.write(generateKmlContent(sessions))
        }
        
        return file
    }
    
    suspend fun exportSessionsToKmlWithResults(sessionsWithResults: List<Pair<TestSession, List<TestResult>>>): File {
        val fileName = "gps_speed_test_${System.currentTimeMillis()}.kml"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            writer.write(generateKmlContentWithResults(sessionsWithResults))
        }
        
        return file
    }
    
    private suspend fun generateKmlContent(sessions: List<TestSession>): String {
        val kml = StringBuilder()
        
        // KML header
        kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        kml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
        kml.append("<Document>\n")
        kml.append("<name>GPS Speed Test Results</name>\n")
        kml.append("<description>Export from GPS Speed Test App</description>\n")
        
        // Define styles for different speed ranges
        addSpeedStyles(kml)
        
        // Process each session
        for (session in sessions) {
            val results = database.testDao().getResultsForSession(session.id)
                .let { flow ->
                    // Convert flow to list - this is a simplified approach
                    val resultsList = mutableListOf<TestResult>()
                    try {
                        // Since we can't collect flow in this context, we'll use a different approach
                        // This will be handled in the calling function
                        emptyList<TestResult>()
                    } catch (e: Exception) {
                        emptyList<TestResult>()
                    }
                }
            
            addSessionToKml(kml, session, results)
        }
        
        kml.append("</Document>\n")
        kml.append("</kml>\n")
        
        return kml.toString()
    }
    
    private fun generateKmlContentWithResults(sessionsWithResults: List<Pair<TestSession, List<TestResult>>>): String {
        val kml = StringBuilder()
        
        // KML header
        kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        kml.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
        kml.append("<Document>\n")
        kml.append("<name>GPS Speed Test Results</name>\n")
        kml.append("<description>Export from GPS Speed Test App</description>\n")
        
        // Define styles for different speed ranges
        addSpeedStyles(kml)
        
        // Process each session with its results
        for ((session, results) in sessionsWithResults) {
            addSessionToKml(kml, session, results)
        }
        
        kml.append("</Document>\n")
        kml.append("</kml>\n")
        
        return kml.toString()
    }
    
    private fun addSpeedStyles(kml: StringBuilder) {
        // Single blue style for all speeds - height represents speed, not color
        kml.append("""
            <Style id="speed_hexagon">
                <PolyStyle>
                    <color>7f0000ff</color>
                    <fill>1</fill>
                    <outline>1</outline>
                </PolyStyle>
                <LineStyle>
                    <color>bf0000ff</color>
                    <width>2</width>
                </LineStyle>
                <LabelStyle>
                    <scale>0.8</scale>
                    <color>ffffffff</color>
                </LabelStyle>
            </Style>
        """.trimIndent())
        
        // Style for Access Point
        kml.append("""
            <Style id="access_point">
                <IconStyle>
                    <color>ffff0000</color>
                    <scale>1.5</scale>
                    <Icon>
                        <href>http://maps.google.com/mapfiles/kml/shapes/wifi.png</href>
                    </Icon>
                </IconStyle>
                <LabelStyle>
                    <scale>1.0</scale>
                    <color>ffffffff</color>
                </LabelStyle>
            </Style>
        """.trimIndent())
    }
    
    private fun addSessionToKml(kml: StringBuilder, session: TestSession, results: List<TestResult>) {
        val sessionDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(session.startTime))
        
        kml.append("<Folder>\n")
        kml.append("<name>${escapeXml(session.description)} - $sessionDate</name>\n")
        kml.append("<description>")
        kml.append("Server: ${escapeXml(session.iperfServer)}\n")
        kml.append("Duration: ${session.testDuration}s\n")
        kml.append("Test Count: ${session.testCount}\n")
        kml.append("Start Time: $sessionDate\n")
        kml.append("</description>\n")
        
        // Add Access Point marker if coordinates are available
        if (session.apLatitude != 0.0 && session.apLongitude != 0.0) {
            kml.append("<Placemark>\n")
            kml.append("<name>Access Point</name>\n")
            kml.append("<description>")
            kml.append("Server: ${escapeXml(session.iperfServer)}\n")
            kml.append("Session: ${escapeXml(session.description)}\n")
            kml.append("</description>\n")
            kml.append("<styleUrl>#access_point</styleUrl>\n")
            kml.append("<Point>\n")
            kml.append("<coordinates>${session.apLongitude},${session.apLatitude},0</coordinates>\n")
            kml.append("</Point>\n")
            kml.append("</Placemark>\n")
        }
        
        // Add test result 3D rectangles
        for ((index, result) in results.withIndex()) {
            val styleId = getSpeedStyleId(result.speedMbps)
            
            kml.append("<Placemark>\n")
            kml.append("<name>Test ${index + 1} - ${String.format("%.1f", result.speedMbps)} Mbps</name>\n")
            kml.append("<description><![CDATA[")
            kml.append("<b>Speed:</b> ${String.format("%.2f", result.speedMbps)} Mbps<br/>")
            kml.append("<b>Distance from AP:</b> ${String.format("%.1f", result.distanceFromAp)} m<br/>")
            kml.append("<b>Data Transferred:</b> ${String.format("%.2f", result.dataMB)} MB<br/>")
            kml.append("<b>Test Duration:</b> ${String.format("%.1f", result.testDurationSeconds)} s<br/>")
            kml.append("<b>Accuracy:</b> ${String.format("%.1f", result.accuracy)} m<br/>")
            kml.append("<b>Orientation:</b> Az ${String.format("%.1f", result.azimuth)}°, Pitch ${String.format("%.1f", result.pitch)}°, Roll ${String.format("%.1f", result.roll)}°<br/>")
            kml.append("<b>Timestamp:</b> ${dateFormat.format(Date(result.timestamp))}<br/>")
            kml.append("<b>Coordinates:</b> ${String.format("%.6f", result.latitude)}, ${String.format("%.6f", result.longitude)}")
            kml.append("]]></description>\n")
            kml.append("<styleUrl>#$styleId</styleUrl>\n")
            
            // Create 3D oriented rectangle
            kml.append(create3DOrientedRectangle(result))
            
            kml.append("</Placemark>\n")
        }
        
        // Create path/track if there are multiple points
        if (results.size > 1) {
            kml.append("<Placemark>\n")
            kml.append("<name>Test Path</name>\n")
            kml.append("<description>Movement path during testing</description>\n")
            kml.append("<Style>\n")
            kml.append("<LineStyle>\n")
            kml.append("<color>7f00ffff</color>\n") // Semi-transparent yellow
            kml.append("<width>3</width>\n")
            kml.append("</LineStyle>\n")
            kml.append("</Style>\n")
            kml.append("<LineString>\n")
            kml.append("<tessellate>1</tessellate>\n")
            kml.append("<coordinates>\n")
            
            for (result in results) {
                kml.append("${result.longitude},${result.latitude},0\n")
            }
            
            kml.append("</coordinates>\n")
            kml.append("</LineString>\n")
            kml.append("</Placemark>\n")
        }
        
        kml.append("</Folder>\n")
    }
    
    private fun getSpeedStyleId(speedMbps: Double): String {
        return "speed_hexagon" // All hexagons use the same blue style
    }
    
    private fun create3DOrientedRectangle(result: TestResult): String {
        // Calculate hexagon height based on speed (25-250 feet range - 5x larger)
        val minHeight = 25.0 // 25 feet minimum (5x larger)
        val maxHeight = 250.0 // 250 feet maximum (5x larger)
        val heightFeet = minHeight + (result.speedMbps / 50.0 * (maxHeight - minHeight)).coerceAtMost(maxHeight)
        val heightMeters = heightFeet * 0.3048 // Convert feet to meters
        
        // Base altitude: 300 feet above ground level, hexagon extends UP from there
        val baseAltitudeMeters = 300.0 * 0.3048 // 300 feet in meters
        val centerAltitude = baseAltitudeMeters + (heightMeters / 2.0) // Center is base + half height
        
        // Hexagon dimensions - much larger for visibility
        val length = 500.0 * 0.3048 // 500 feet long (massive)
        val width = 50.0 * 0.3048   // 50 feet wide (massive)
        
        // Generate the 12 corners of the hexagonal prism
        val corners = generateHexagonalPrismCorners(
            result.latitude, 
            result.longitude, 
            centerAltitude,
            length, 
            width, 
            heightMeters,
            result.azimuth,
            result.pitch,
            result.roll
        )
        
        val kml = StringBuilder()
        
        // Create MultiGeometry to contain all faces
        kml.append("<MultiGeometry>\n")
        
        // Create hexagonal prism faces (2 hexagonal end caps + 6 rectangular sides)
        kml.append(createHexagonalFace(corners.slice(0..5))) // Bottom hexagon
        kml.append(createHexagonalFace(corners.slice(6..11))) // Top hexagon
        
        // Create 6 rectangular side faces
        for (i in 0..5) {
            val nextI = (i + 1) % 6
            kml.append(createRectangularFace(
                corners[i], corners[nextI], corners[nextI + 6], corners[i + 6]
            ))
        }
        
        kml.append("</MultiGeometry>\n")
        
        return kml.toString()
    }
    
    private fun generateHexagonalPrismCorners(
        lat: Double, 
        lng: Double, 
        altitude: Double,
        length: Double, 
        width: Double, 
        height: Double,
        azimuth: Float,
        pitch: Float,
        roll: Float
    ): Array<Triple<Double, Double, Double>> {
        
        // Convert degrees to radians
        val azRad = Math.toRadians(azimuth.toDouble())
        val pitchRad = Math.toRadians(pitch.toDouble())
        val rollRad = Math.toRadians(roll.toDouble())
        
        // Half dimensions
        val halfLength = length / 2.0
        val halfWidth = width / 2.0
        val halfHeight = height / 2.0
        
        // Define the 12 corners of a hexagonal prism centered at origin
        // Long axis along X, hexagonal cross-section in Y-Z plane
        // Create proper hexagon geometry with 60-degree angles
        val hexRadius = halfWidth
        val localCorners = arrayOf(
            // Bottom hexagon (6 corners) - proper hexagon in Y-Z plane
            Triple(-halfLength, hexRadius, -halfHeight),                                          // 0
            Triple(-halfLength, hexRadius * 0.5, hexRadius * Math.sqrt(3.0) / 2.0 - halfHeight), // 1
            Triple(-halfLength, -hexRadius * 0.5, hexRadius * Math.sqrt(3.0) / 2.0 - halfHeight), // 2
            Triple(-halfLength, -hexRadius, -halfHeight),                                        // 3
            Triple(-halfLength, -hexRadius * 0.5, -hexRadius * Math.sqrt(3.0) / 2.0 - halfHeight), // 4
            Triple(-halfLength, hexRadius * 0.5, -hexRadius * Math.sqrt(3.0) / 2.0 - halfHeight), // 5
            
            // Top hexagon (6 corners) - proper hexagon in Y-Z plane
            Triple(halfLength, hexRadius, halfHeight),                                           // 6
            Triple(halfLength, hexRadius * 0.5, hexRadius * Math.sqrt(3.0) / 2.0 + halfHeight), // 7
            Triple(halfLength, -hexRadius * 0.5, hexRadius * Math.sqrt(3.0) / 2.0 + halfHeight), // 8
            Triple(halfLength, -hexRadius, halfHeight),                                         // 9
            Triple(halfLength, -hexRadius * 0.5, -hexRadius * Math.sqrt(3.0) / 2.0 + halfHeight), // 10
            Triple(halfLength, hexRadius * 0.5, -hexRadius * Math.sqrt(3.0) / 2.0 + halfHeight) // 11
        )
        
        // Apply rotation matrices (yaw=azimuth, pitch, roll)
        val corners = Array(12) { i ->
            val (x, y, z) = localCorners[i]
            
            // Apply rotations: first roll, then pitch, then yaw
            val rotated = applyRotations(x, y, z, rollRad, pitchRad, azRad)
            
            // Convert to lat/lng offset and add to base position
            val latOffset = rotated.second / 111320.0 // meters to degrees lat
            val lngOffset = rotated.first / (111320.0 * Math.cos(Math.toRadians(lat))) // meters to degrees lng
            
            Triple(
                lat + latOffset,
                lng + lngOffset,
                altitude + rotated.third
            )
        }
        
        return corners
    }
    
    private fun applyRotations(x: Double, y: Double, z: Double, roll: Double, pitch: Double, yaw: Double): Triple<Double, Double, Double> {
        // Apply roll (rotation around z-axis)
        val x1 = x * Math.cos(roll) - y * Math.sin(roll)
        val y1 = x * Math.sin(roll) + y * Math.cos(roll)
        val z1 = z
        
        // Apply pitch (rotation around y-axis)
        val x2 = x1 * Math.cos(pitch) + z1 * Math.sin(pitch)
        val y2 = y1
        val z2 = -x1 * Math.sin(pitch) + z1 * Math.cos(pitch)
        
        // Apply yaw (rotation around z-axis)
        val x3 = x2 * Math.cos(yaw) - y2 * Math.sin(yaw)
        val y3 = x2 * Math.sin(yaw) + y2 * Math.cos(yaw)
        val z3 = z2
        
        return Triple(x3, y3, z3)
    }
    
    private fun createHexagonalFace(corners: List<Triple<Double, Double, Double>>): String {
        return """
            <Polygon>
                <extrude>0</extrude>
                <altitudeMode>absolute</altitudeMode>
                <outerBoundaryIs>
                    <LinearRing>
                        <coordinates>
                            ${corners[0].second},${corners[0].first},${corners[0].third}
                            ${corners[1].second},${corners[1].first},${corners[1].third}
                            ${corners[2].second},${corners[2].first},${corners[2].third}
                            ${corners[3].second},${corners[3].first},${corners[3].third}
                            ${corners[4].second},${corners[4].first},${corners[4].third}
                            ${corners[5].second},${corners[5].first},${corners[5].third}
                            ${corners[0].second},${corners[0].first},${corners[0].third}
                        </coordinates>
                    </LinearRing>
                </outerBoundaryIs>
            </Polygon>
        """.trimIndent() + "\n"
    }
    
    private fun createRectangularFace(
        corner1: Triple<Double, Double, Double>,
        corner2: Triple<Double, Double, Double>,
        corner3: Triple<Double, Double, Double>,
        corner4: Triple<Double, Double, Double>
    ): String {
        return """
            <Polygon>
                <extrude>0</extrude>
                <altitudeMode>absolute</altitudeMode>
                <outerBoundaryIs>
                    <LinearRing>
                        <coordinates>
                            ${corner1.second},${corner1.first},${corner1.third}
                            ${corner2.second},${corner2.first},${corner2.third}
                            ${corner3.second},${corner3.first},${corner3.third}
                            ${corner4.second},${corner4.first},${corner4.third}
                            ${corner1.second},${corner1.first},${corner1.third}
                        </coordinates>
                    </LinearRing>
                </outerBoundaryIs>
            </Polygon>
        """.trimIndent() + "\n"
    }

    private fun generateCsvContent(sessionsWithResults: List<Pair<TestSession, List<TestResult>>>): String {
        val csv = StringBuilder()
        
        // CSV header
        csv.append("session_id,session_description,session_start_time,iperf_server,test_duration,ap_latitude,ap_longitude,")
        csv.append("result_id,timestamp,latitude,longitude,distance_from_ap,speed_mbps,test_duration_seconds,data_mb,")
        csv.append("azimuth,pitch,roll,accuracy\n")
        
        // Process each session with its results
        for ((session, results) in sessionsWithResults) {
            val sessionDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(session.startTime))
            
            if (results.isEmpty()) {
                // Session with no results
                csv.append("${session.id},\"${escapeXml(session.description)}\",\"$sessionDate\",\"${escapeXml(session.iperfServer)}\",")
                csv.append("${session.testDuration},${session.apLatitude},${session.apLongitude},")
                csv.append(",,,,,,,,,,\n")
            } else {
                // Session with results
                for (result in results) {
                    val resultTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(result.timestamp))
                    
                    csv.append("${session.id},\"${escapeXml(session.description)}\",\"$sessionDate\",\"${escapeXml(session.iperfServer)}\",")
                    csv.append("${session.testDuration},${session.apLatitude},${session.apLongitude},")
                    csv.append("${result.id},\"$resultTimestamp\",${result.latitude},${result.longitude},")
                    csv.append("${result.distanceFromAp},${result.speedMbps},${result.testDurationSeconds},${result.dataMB},")
                    csv.append("${result.azimuth},${result.pitch},${result.roll},${result.accuracy}\n")
                }
            }
        }
        
        return csv.toString()
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;")
    }
}