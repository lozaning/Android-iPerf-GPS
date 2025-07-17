package com.example.androidproject.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.androidproject.R
import com.example.androidproject.data.TestResult
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import kotlin.math.*

// Simple LatLng data class to replace Google Maps LatLng
data class LatLng(val latitude: Double, val longitude: Double)

class SpeedVisualizationMap @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gestureDetector = GestureDetector(context, MapGestureListener())
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleGestureListener())
    
    // OSMDroid integration
    private var mapView: MapView? = null
    private var customView: View? = null
    private var overlayView: View? = null
    
    private var userLocation: LatLng? = null
    private var apLocation: LatLng? = null
    private var testResults: List<TestResult> = emptyList()
    private var onMapTapListener: ((LatLng) -> Unit)? = null
    private var isApLocationMode = false
    private var isOsmMode = true // Always use satellite view
    
    // Map viewport
    private var centerLat = 0.0
    private var centerLng = 0.0
    private var metersPerPixel = 40.0 // Start with much wider view - 40 meters per pixel (4x more zoomed out)
    private var scaleFactor = 1.0f
    private var panX = 0f
    private var panY = 0f
    private var minZoom = 0.1f
    private var maxZoom = 10.0f
    
    // Pan accumulation threshold - update center when pan gets large
    private val maxPanOffset = 200f
    
    // Removed isometric projection - using simple 2D view
    
    // Colors
    private val userColor = Color.parseColor("#2A93D5") // primary_blue
    private val apColor = Color.parseColor("#125488") // primary_dark_blue
    
    // Base session colors - each session gets a unique color
    private val sessionBaseColors = intArrayOf(
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#FF9800"), // Orange
        Color.parseColor("#607D8B"), // Blue Grey
        Color.parseColor("#009688"), // Teal
        Color.parseColor("#E91E63")  // Pink
    )
    
    // Cache for session ID to color mapping
    private val sessionColorMap = mutableMapOf<Long, Int>()
    
    init {
        textPaint.apply {
            color = Color.BLACK
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        
        paint.apply {
            style = Paint.Style.FILL
        }
        
        // Create custom drawing view
        customView = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                drawCustomElements(canvas)
            }
            
            override fun onTouchEvent(event: MotionEvent): Boolean {
                return handleCustomTouchEvent(event)
            }
        }
        
        // Create overlay view for satellite mode
        overlayView = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                drawOverlayElements(canvas)
            }
            
            override fun onTouchEvent(event: MotionEvent): Boolean {
                // In OSM mode, let OSM handle gestures
                // Only handle AP location marking if enabled
                if (isApLocationMode) {
                    return gestureDetector.onTouchEvent(event)
                }
                return false // Let OSM handle touch events
            }
        }
        
        // Always start with satellite view
        initializeOsmMaps()
    }
    
    fun setUserLocation(location: LatLng) {
        userLocation = location
        if (centerLat == 0.0 && centerLng == 0.0) {
            centerLat = location.latitude
            centerLng = location.longitude
            metersPerPixel = 40.0 // Start at 40 meters per pixel (4x more zoomed out)
            if (isOsmMode) {
                updateOsmMapCamera()
            }
        }
        // Don't update camera if location already set - preserves zoom level
        invalidateCustomView()
    }
    
    fun setApLocation(location: LatLng) {
        apLocation = location
        invalidateCustomView()
    }
    
    fun setTestResults(results: List<TestResult>) {
        testResults = results
        invalidateCustomView()
    }
    
    fun addTestResult(result: TestResult) {
        testResults = testResults + result
        invalidateCustomView()
    }
    
    fun setOnMapTapListener(listener: (LatLng) -> Unit) {
        onMapTapListener = listener
    }
    
    fun setApLocationMode(enabled: Boolean) {
        isApLocationMode = enabled
        invalidateCustomView()
    }
    
    fun setOsmMode(enabled: Boolean) {
        // Always use satellite mode - ignore the parameter
        Log.d("SpeedVisualizationMap", "Always using satellite mode")
        isOsmMode = true
        // OSM maps are already initialized
    }
    
    fun recenterOnUser() {
        userLocation?.let { location ->
            centerLat = location.latitude
            centerLng = location.longitude
            // Reset pan offsets when recentering
            panX = 0f
            panY = 0f
            if (isOsmMode) {
                updateOsmMapCameraPosition()
            }
            invalidateCustomView()
        }
    }
    
    private fun drawCustomElements(canvas: Canvas) {
        // Custom view disabled - always use satellite view
        return
    }
    
    private fun drawOverlayElements(canvas: Canvas) {
        if (userLocation == null || mapView == null) return
        
        // Draw test results using OSM projection
        testResults.forEach { result ->
            val latLng = LatLng(result.latitude, result.longitude)
            val point = osmMapToScreenPoint(latLng)
            point?.let { drawSpeedColumnOnMap(canvas, it, result.speedMbps, result.sessionId) }
        }
        
        // Draw AP location
        apLocation?.let { location ->
            val point = osmMapToScreenPoint(location)
            point?.let { drawApLocationOnMap(canvas, it) }
        }
        
        // Draw user location
        userLocation?.let { location ->
            val point = osmMapToScreenPoint(location)
            point?.let { drawUserLocationOnMap(canvas, it) }
        }
        
        // Draw UI elements
        drawUI(canvas)
    }
    
    private fun initializeOsmMaps() {
        if (mapView == null) {
            Log.d("SpeedVisualizationMap", "Creating new OSM MapView")
            // Configure OSMDroid
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
            Configuration.getInstance().userAgentValue = "GPSSpeedTest"
            
            mapView = MapView(context)
            mapView?.setTileSource(TileSourceFactory.MAPNIK)
            mapView?.setMultiTouchControls(true)
            mapView?.setBuiltInZoomControls(false)
            
            // Keep map straight - no rotation
            mapView?.mapOrientation = 0f
            
            // Add map listener to sync overlay when map moves
            mapView?.addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    // Invalidate overlay when map scrolls to keep speed results synchronized
                    overlayView?.invalidate()
                    return false
                }
                
                override fun onZoom(event: ZoomEvent?): Boolean {
                    // Invalidate overlay when map zooms to keep speed results synchronized
                    overlayView?.invalidate()
                    return false
                }
            })
            
            // Set initial position - wait for user location, don't use default
            if (centerLat != 0.0 && centerLng != 0.0) {
                updateOsmMapCamera()
                Log.d("SpeedVisualizationMap", "Using user location: $centerLat, $centerLng")
            } else {
                Log.d("SpeedVisualizationMap", "Waiting for user location...")
            }
        } else {
            Log.d("SpeedVisualizationMap", "Reusing existing OSM MapView")
        }
        
        // Hide custom view and show OSM with overlay
        customView?.visibility = View.GONE
        mapView?.visibility = View.VISIBLE
        overlayView?.visibility = View.VISIBLE
        
        if (mapView?.parent == null) {
            addView(mapView, 0) // Add OSM at the bottom
        }
        
        if (overlayView?.parent == null) {
            addView(overlayView) // Add overlay on top
        }
    }
    
    private fun hideOsmMaps() {
        mapView?.visibility = View.GONE
        overlayView?.visibility = View.GONE
        customView?.visibility = View.VISIBLE
        invalidateCustomView()
    }
    
    private fun updateOsmMapCamera() {
        mapView?.let { map ->
            val mapController: IMapController = map.controller
            val startPoint = GeoPoint(centerLat, centerLng)
            
            // Improved zoom level calculation for closer zooming
            // Allow much higher zoom levels (up to 21) for detailed viewing
            val zoomLevel = when {
                metersPerPixel <= 0.5 -> 21  // Very close zoom
                metersPerPixel <= 1.0 -> 20  // Close zoom
                metersPerPixel <= 2.0 -> 19  // Medium-close zoom
                metersPerPixel <= 5.0 -> 18  // Medium zoom
                metersPerPixel <= 10.0 -> 17 // Default zoom
                else -> (Math.log(156543.03392 / metersPerPixel) / Math.log(2.0)).toInt().coerceIn(1, 17)
            }
            
            mapController.setZoom(zoomLevel)
            mapController.setCenter(startPoint)
        }
    }
    
    private fun updateOsmMapCameraPosition() {
        // Update only position, preserve current zoom level
        mapView?.let { map ->
            val mapController: IMapController = map.controller
            val startPoint = GeoPoint(centerLat, centerLng)
            mapController.setCenter(startPoint)
            // Don't call setZoom() to preserve current zoom level
        }
    }
    
    private fun updateCenterFromPan() {
        // Convert accumulated pan offsets to map center updates
        val deltaMetersX = panX * metersPerPixel
        val deltaMetersY = panY * metersPerPixel
        
        // Convert meters to lat/lng deltas
        val deltaLat = -deltaMetersY / 111320.0  // Negative because screen Y is inverted
        val deltaLng = deltaMetersX / (111320.0 * cos(Math.toRadians(centerLat)))
        
        centerLat += deltaLat
        centerLng += deltaLng
        
        // Reset pan offsets
        panX = 0f
        panY = 0f
        
        if (isOsmMode) {
            updateOsmMapCameraPosition()
        }
    }
    
    
    private fun invalidateCustomView() {
        customView?.invalidate()
        overlayView?.invalidate()
    }
    
    private fun osmMapToScreenPoint(latLng: LatLng): PointF? {
        return mapView?.projection?.toPixels(GeoPoint(latLng.latitude, latLng.longitude), null)?.let { point ->
            PointF(point.x.toFloat(), point.y.toFloat())
        }
    }
    
    private fun drawSpeedColumnOnMap(canvas: Canvas, point: PointF, speedMbps: Double, sessionId: Long) {
        // Draw simple speed circle - much larger sizes, maintain readability when zoomed
        val minRadius = 30f // 2x larger than before (was 15f)
        val maxRadius = 120f // 2x larger than before (was 60f)
        val radius = minRadius + (speedMbps / 25.0 * (maxRadius - minRadius)).toFloat()
        
        // Use session-based color with speed shading
        val speedColor = getSpeedShadedColor(sessionId, speedMbps)
        
        // Draw filled circle
        paint.apply {
            color = speedColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(point.x, point.y, radius, paint)
        
        // Draw circle outline for better visibility
        paint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f // Thicker outline for better visibility
        }
        canvas.drawCircle(point.x, point.y, radius, paint)
        
        // Draw speed text in center of circle - ensure minimum readable size
        textPaint.apply {
            color = Color.WHITE
            textSize = kotlin.math.max(radius * 0.5f, 24f) // Minimum 24f for readability
            setShadowLayer(6f, 3f, 3f, Color.BLACK) // Stronger shadow
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            String.format("%.1f", speedMbps),
            point.x, point.y + textPaint.textSize / 3f, // Center text vertically
            textPaint
        )
        textPaint.clearShadowLayer()
    }
    
    private fun drawSimple3DColumn(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, baseColor: Int) {
        val path = Path()
        val depth = 24f // 3x larger depth (was 8f)
        
        // Front face
        paint.apply {
            color = baseColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(x - width/2, y - height, x + width/2, y, paint)
        
        // Top face (lighter)
        paint.apply {
            color = lightenColor(baseColor, 0.3f)
        }
        path.reset()
        path.moveTo(x - width/2, y - height)
        path.lineTo(x - width/2 + depth, y - height - depth)
        path.lineTo(x + width/2 + depth, y - height - depth)
        path.lineTo(x + width/2, y - height)
        path.close()
        canvas.drawPath(path, paint)
        
        // Right face (darker)
        paint.apply {
            color = darkenColor(baseColor, 0.3f)
        }
        path.reset()
        path.moveTo(x + width/2, y - height)
        path.lineTo(x + width/2 + depth, y - height - depth)
        path.lineTo(x + width/2 + depth, y - depth)
        path.lineTo(x + width/2, y)
        path.close()
        canvas.drawPath(path, paint)
        
        // Thicker outline for better visibility
        paint.apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f // Thicker outline (was 1f)
        }
        canvas.drawRect(x - width/2, y - height, x + width/2, y, paint)
    }
    
    private fun drawApLocationOnMap(canvas: Canvas, point: PointF) {
        val size = 10f
        
        paint.apply {
            color = apColor
            style = Paint.Style.FILL
        }
        
        canvas.drawRect(
            point.x - size, point.y - size,
            point.x + size, point.y + size,
            paint
        )
        
        paint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        
        canvas.drawRect(
            point.x - size, point.y - size,
            point.x + size, point.y + size,
            paint
        )
    }
    
    private fun drawUserLocationOnMap(canvas: Canvas, point: PointF) {
        val radius = 12f
        
        paint.apply {
            color = userColor
            style = Paint.Style.FILL
        }
        
        canvas.drawCircle(point.x, point.y, radius, paint)
        
        paint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        
        canvas.drawCircle(point.x, point.y, radius, paint)
    }
    
    private fun handleCustomTouchEvent(event: MotionEvent): Boolean {
        var result = scaleGestureDetector.onTouchEvent(event)
        if (!scaleGestureDetector.isInProgress) {
            result = gestureDetector.onTouchEvent(event) || result
        }
        return result
    }
    
    
    private fun drawGrid(canvas: Canvas) {
        paint.apply {
            color = Color.LTGRAY
            strokeWidth = 2f / scaleFactor
            style = Paint.Style.STROKE
        }
        
        val gridSpacing = 50 // 50 meters
        
        // Calculate grid bounds based on current view
        val viewMetersWidth = width * metersPerPixel * 1.5 // Expand for isometric view
        val viewMetersHeight = height * metersPerPixel * 1.5
        
        val startLatMeters = centerLat * 111320.0 - viewMetersHeight / 2
        val endLatMeters = centerLat * 111320.0 + viewMetersHeight / 2
        val startLngMeters = centerLng * 111320.0 * cos(Math.toRadians(centerLat)) - viewMetersWidth / 2
        val endLngMeters = centerLng * 111320.0 * cos(Math.toRadians(centerLat)) + viewMetersWidth / 2
        
        // Draw longitude lines (north-south)
        var lngMeters = (startLngMeters / gridSpacing).toInt() * gridSpacing
        while (lngMeters <= endLngMeters) {
            val lng = lngMeters / (111320.0 * cos(Math.toRadians(centerLat)))
            val startPoint = latLngToPoint(LatLng(startLatMeters / 111320.0, lng))
            val endPoint = latLngToPoint(LatLng(endLatMeters / 111320.0, lng))
            canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, paint)
            lngMeters += gridSpacing
        }
        
        // Draw latitude lines (east-west)
        var latMeters = (startLatMeters / gridSpacing).toInt() * gridSpacing
        while (latMeters <= endLatMeters) {
            val lat = latMeters / 111320.0
            val startPoint = latLngToPoint(LatLng(lat, startLngMeters / (111320.0 * cos(Math.toRadians(centerLat)))))
            val endPoint = latLngToPoint(LatLng(lat, endLngMeters / (111320.0 * cos(Math.toRadians(centerLat)))))
            canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, paint)
            latMeters += gridSpacing
        }
    }
    
    private fun drawUserLocation(canvas: Canvas) {
        userLocation?.let { location ->
            val point = latLngToPoint(location)
            val radius = 15f / scaleFactor
            
            paint.apply {
                color = userColor
                style = Paint.Style.FILL
            }
            
            canvas.drawCircle(point.x, point.y, radius, paint)
            
            paint.apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 3f / scaleFactor
            }
            
            canvas.drawCircle(point.x, point.y, radius, paint)
        }
    }
    
    private fun drawApLocation(canvas: Canvas) {
        apLocation?.let { location ->
            val point = latLngToPoint(location)
            val size = 12f / scaleFactor
            
            paint.apply {
                color = apColor
                style = Paint.Style.FILL
            }
            
            canvas.drawRect(
                point.x - size, point.y - size,
                point.x + size, point.y + size,
                paint
            )
            
            paint.apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 3f / scaleFactor
            }
            
            canvas.drawRect(
                point.x - size, point.y - size,
                point.x + size, point.y + size,
                paint
            )
        }
    }
    
    private fun drawTestResults(canvas: Canvas) {
        testResults.forEach { result ->
            val point = latLngToPoint(LatLng(result.latitude, result.longitude))
            drawSpeedColumn(canvas, point, result.speedMbps, result.sessionId)
        }
    }
    
    private fun drawSpeedColumn(canvas: Canvas, point: PointF, speedMbps: Double, sessionId: Long) {
        // Scale for 0-25 Mbps range and make columns much larger (3x size)
        val maxHeight = 400f / scaleFactor // Much larger (was 200f)
        val columnWidth = 80f / scaleFactor // Much larger (was 40f)
        val columnHeight = (speedMbps / 25.0 * maxHeight).toFloat().coerceAtMost(maxHeight)
        
        // Use session-based color with speed shading
        val baseColor = getSpeedShadedColor(sessionId, speedMbps)
        
        // Draw simple speed circle instead of 3D column
        drawSpeedCircle(canvas, point.x, point.y, columnWidth / 2f, baseColor, speedMbps, sessionId)
        
        // Draw speed text with much better visibility (3x larger)
        textPaint.apply {
            color = Color.WHITE
            textSize = 64f / scaleFactor // Much larger text (was 32f)
            setShadowLayer(8f, 4f, 4f, Color.BLACK) // Stronger shadow
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            String.format("%.1f", speedMbps),
            point.x, point.y - columnHeight - 30f / scaleFactor, // More space
            textPaint
        )
        textPaint.clearShadowLayer()
    }
    
    private fun drawSpeedCircle(canvas: Canvas, x: Float, y: Float, radius: Float, fillColor: Int, speedMbps: Double, sessionId: Long) {
        // Draw filled circle
        paint.apply {
            color = fillColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x, y, radius, paint)
        
        // Draw outline
        paint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f / scaleFactor
        }
        canvas.drawCircle(x, y, radius, paint)
        
        // Draw speed text
        textPaint.apply {
            color = Color.WHITE
            textSize = radius * 0.8f / scaleFactor
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            String.format("%.1f", speedMbps),
            x, y + textPaint.textSize / 3f,
            textPaint
        )
        textPaint.clearShadowLayer()
    }
    
    private fun drawUI(canvas: Canvas) {
        if (isApLocationMode) {
            textPaint.apply {
                color = Color.RED
                textSize = 48f
            }
            canvas.drawText(
                "Tap to mark AP location",
                width / 2f, 100f,
                textPaint
            )
        }
    }
    
    private fun latLngToPoint(latLng: LatLng): PointF {
        // Simple 2D coordinate conversion (not used in satellite-only mode)
        val deltaLat = latLng.latitude - centerLat
        val deltaLng = latLng.longitude - centerLng
        
        val metersLat = deltaLat * 111320.0
        val metersLng = deltaLng * 111320.0 * cos(Math.toRadians(centerLat))
        
        val pixelX = (metersLng / metersPerPixel).toFloat() + width / 2f
        val pixelY = (-metersLat / metersPerPixel).toFloat() + height / 2f
        
        return PointF(pixelX, pixelY)
    }
    
    private fun pointToLatLng(x: Float, y: Float): LatLng {
        val metersX = (x - width / 2f) * metersPerPixel
        val metersY = -(y - height / 2f) * metersPerPixel
        
        val deltaLat = metersY / 111320.0
        val deltaLng = metersX / (111320.0 * cos(Math.toRadians(centerLat)))
        
        return LatLng(centerLat + deltaLat, centerLng + deltaLng)
    }
    
    // Removed isometric functions - using simple 2D satellite view only
    
    private fun lightenColor(color: Int, factor: Float): Int {
        val r = (Color.red(color) * (1 - factor) + 255 * factor).toInt()
        val g = (Color.green(color) * (1 - factor) + 255 * factor).toInt()
        val b = (Color.blue(color) * (1 - factor) + 255 * factor).toInt()
        return Color.rgb(r, g, b)
    }
    
    private fun darkenColor(color: Int, factor: Float): Int {
        val r = (Color.red(color) * (1 - factor)).toInt()
        val g = (Color.green(color) * (1 - factor)).toInt()
        val b = (Color.blue(color) * (1 - factor)).toInt()
        return Color.rgb(r, g, b)
    }
    
    private fun getSessionColor(sessionId: Long): Int {
        return sessionColorMap.getOrPut(sessionId) {
            val colorIndex = sessionColorMap.size % sessionBaseColors.size
            sessionBaseColors[colorIndex]
        }
    }
    
    private fun getSpeedShadedColor(sessionId: Long, speedMbps: Double): Int {
        val baseColor = getSessionColor(sessionId)
        
        // Define speed ranges for shading
        // 0-5 Mbps: Lightest (30% of original)
        // 5-15 Mbps: Light (50% of original) 
        // 15-25 Mbps: Medium (70% of original)
        // 25+ Mbps: Darkest (100% of original)
        val shadingFactor = when {
            speedMbps < 5.0 -> 0.3f   // Very light for slow speeds
            speedMbps < 15.0 -> 0.5f  // Light for medium speeds
            speedMbps < 25.0 -> 0.7f  // Medium for good speeds
            else -> 1.0f              // Full color for fast speeds
        }
        
        val r = (Color.red(baseColor) * shadingFactor).toInt()
        val g = (Color.green(baseColor) * shadingFactor).toInt()
        val b = (Color.blue(baseColor) * shadingFactor).toInt()
        
        return Color.rgb(r, g, b)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var result = scaleGestureDetector.onTouchEvent(event)
        if (!scaleGestureDetector.isInProgress) {
            result = gestureDetector.onTouchEvent(event) || result
        }
        return result || super.onTouchEvent(event)
    }
    
    private inner class MapGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isApLocationMode) {
                val latLng = if (isOsmMode) {
                    // Convert screen coordinates to lat/lng using OSM projection
                    val point = android.graphics.Point(e.x.toInt(), e.y.toInt())
                    mapView?.projection?.fromPixels(point.x, point.y)?.let { geoPoint ->
                        LatLng(geoPoint.latitude, geoPoint.longitude)
                    }
                } else {
                    // Convert tap coordinates to map coordinates for custom view
                    // Account for pan offset and scaling transformations
                    val adjustedX = (e.x - width / 2f) / scaleFactor + width / 2f - panX
                    val adjustedY = (e.y - height / 2f) / scaleFactor + height / 2f - panY
                    pointToLatLng(adjustedX, adjustedY)
                }
                
                latLng?.let { onMapTapListener?.invoke(it) }
                return true
            }
            return false
        }
        
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (!scaleGestureDetector.isInProgress) {
                if (isOsmMode) {
                    // In OSM mode, let OSM handle scrolling and sync our overlay
                    return false
                } else {
                    // For custom mode, accumulate pan offsets instead of directly updating center
                    panX -= distanceX / scaleFactor
                    panY -= distanceY / scaleFactor
                    
                    // Update center and reset pan when offsets get large
                    if (kotlin.math.abs(panX) > maxPanOffset || kotlin.math.abs(panY) > maxPanOffset) {
                        updateCenterFromPan()
                    }
                    
                    invalidateCustomView()
                    return true
                }
            }
            return false
        }
        
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (isOsmMode) {
                // In OSM mode, let OSM handle double-tap zoom
                return false
            } else {
                // Zoom in by 2x for custom view
                val newScale = scaleFactor * 2.0f
                if (newScale <= maxZoom) {
                    scaleFactor = newScale
                    metersPerPixel /= 2.0
                    invalidateCustomView()
                }
                return true
            }
        }
    }
    
    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (isOsmMode) {
                // In OSM mode, let OSM handle pinch zoom
                return false
            } else {
                // Handle pinch zoom for custom view
                val newScale = scaleFactor * detector.scaleFactor
                if (newScale in minZoom..maxZoom) {
                    scaleFactor = newScale
                    metersPerPixel /= detector.scaleFactor
                    invalidateCustomView()
                }
                return true
            }
        }
    }
    
    // Lifecycle methods for MapView
    fun onResume() {
        mapView?.onResume()
    }
    
    fun onPause() {
        mapView?.onPause()
    }
    
    fun onDestroy() {
        mapView?.onDetach()
    }
}