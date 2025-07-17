# GPS Speed Test App

A comprehensive Android application for testing and analyzing WiFi/network performance across geographical locations with precise GPS tracking and device orientation data.

## 📱 Overview

GPS Speed Test is a powerful field testing tool that combines network speed testing (via iPerf3) with GPS location tracking and device orientation sensing. It's designed for wireless network engineers, researchers, and anyone who needs to analyze network performance across different physical locations and device orientations.

### Key Features

- **Real-time Network Speed Testing** using iPerf3 protocol
- **High-precision GPS Tracking** with accuracy measurements
- **Device Orientation Capture** (azimuth, pitch, roll) via gyroscope/magnetometer
- **Interactive Map Visualization** with satellite imagery
- **Session-based Data Organization** for multiple test runs
- **Comprehensive Data Export** to CSV format for analysis
- **Antenna Pattern Analysis** through orientation-aware measurements

## 🚀 Getting Started

### Prerequisites

- Android device running API level 24+ (Android 7.0+)
- Location permissions enabled
- iPerf3 server running on your network
- Network connectivity to the iPerf3 server

### Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/GPSSpeedTest.git
   ```

2. Open the project in Android Studio

3. Build and install on your Android device:
   ```bash
   ./gradlew installDebug
   ```

### Quick Start

1. **Launch the app** and grant location permissions
2. **Tap "Mark AP"** to start a new test session
3. **Enter your iPerf3 server details** (IP:Port)
4. **Position yourself** at your Access Point location and the app will mark it
5. **Configure test parameters** (duration or data amount)
6. **Run tests** either manually or in auto mode as you move around
7. **Export data** to CSV for analysis

## 📊 How It Works

### Test Process

1. **Session Creation**: Each test session is associated with a specific Access Point location and iPerf3 server
2. **GPS Tracking**: The app continuously tracks your precise location using high-accuracy GPS
3. **Orientation Sensing**: Device orientation (azimuth, pitch, roll) is captured via sensors
4. **Speed Testing**: Network throughput is measured using the iPerf3 protocol
5. **Data Storage**: All measurements are stored locally in a Room database
6. **Visualization**: Results are displayed on an interactive satellite map

### Technical Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Main Activity │    │  Mark AP Screen │    │  Export Screen  │
│                 │    │                 │    │                 │
│ • Map Overview  │    │ • Live Testing  │    │ • Data Export   │
│ • Session List  │    │ • GPS Tracking  │    │ • CSV Generation│
│ • Historical    │    │ • Speed Tests   │    │ • File Sharing  │
│   Data View     │    │ • Orientation   │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────────────┐
                    │     Room Database       │
                    │                         │
                    │ • TestSession entities  │
                    │ • TestResult entities   │
                    │ • Relational data       │
                    └─────────────────────────┘
```

## 🗺️ Map Visualization

The app features an interactive map system built on OSMDroid (OpenStreetMap) that provides:

- **Satellite imagery** for real-world context
- **GPS location tracking** with accuracy indicators
- **Access Point markers** showing test origin points
- **Speed result visualization** with color-coded circles
- **Session-based color coding** to distinguish between different test runs
- **Zoom and pan controls** for detailed analysis

### Color Coding System

- Each test session gets a unique color (Green, Blue, Purple, Orange, etc.)
- Within each session, speed is represented by color intensity:
  - **Lighter shades**: Slower speeds (0-5 Mbps)
  - **Medium shades**: Moderate speeds (5-15 Mbps) 
  - **Darker shades**: Faster speeds (15+ Mbps)

## 📱 Screen Breakdown

### Main Screen
- **Interactive Map**: Shows all historical test data
- **Session Overview**: Visual representation of all test sessions
- **Navigation**: Access to Mark AP and Export screens

### Mark AP Screen
- **Access Point Setup**: Mark your AP location at current GPS position
- **Live Testing Interface**: Manual or automatic test execution
- **Real-time Feedback**: GPS coordinates, orientation data, and test results
- **Test Configuration**: Duration-based or data-amount-based testing

### Export Screen
- **Session Management**: Select/deselect test sessions
- **Data Export**: Generate CSV files with comprehensive data
- **Session Deletion**: Clean up unwanted test data

## 📈 Data Collection

### Captured Metrics

Each test captures the following comprehensive dataset:

**Session Information:**
- `session_id`: Unique identifier for the test session
- `session_description`: Human-readable session name
- `session_start_time`: When the session began
- `iperf_server`: Server IP and port used for testing
- `test_duration`: Configured test duration
- `ap_latitude`, `ap_longitude`: Access Point GPS coordinates

**Test Results:**
- `result_id`: Unique identifier for each individual test
- `timestamp`: Exact time when test was performed
- `latitude`, `longitude`: GPS coordinates where test was performed
- `distance_from_ap`: Calculated distance from Access Point
- `speed_mbps`: Measured network throughput in Mbps
- `test_duration_seconds`: Actual test duration
- `data_mb`: Amount of data transferred during test
- `azimuth`: Device heading (0-360°)
- `pitch`: Device tilt forward/backward
- `roll`: Device rotation left/right
- `accuracy`: GPS accuracy estimate in meters

### Data Quality

- **High-precision GPS**: Uses Android's FusedLocationProvider for optimal accuracy
- **Sensor Fusion**: Combines accelerometer and magnetometer for reliable orientation
- **Network Protocol**: iPerf3 provides industry-standard throughput measurements
- **Time Synchronization**: All measurements include precise timestamps

## 🔧 Configuration

### iPerf3 Server Setup

The app requires an iPerf3 server running on your network. Here's how to set it up:

**On Linux/macOS:**
```bash
# Install iperf3
sudo apt-get install iperf3  # Ubuntu/Debian
brew install iperf3          # macOS

# Run server
iperf3 -s -p 5201
```

**On Windows:**
```bash
# Download from https://iperf.fr/iperf-download.php
iperf3.exe -s -p 5201
```

### Test Parameters

The app supports two testing modes:

1. **Duration Mode**: Test for a specific number of seconds (e.g., 10 seconds)
2. **Data Mode**: Test until a specific amount of data is transferred (e.g., 100 MB)

### Auto Testing

Enable auto mode for continuous testing as you move around:
- Tests run automatically every few seconds
- Stops when you disable auto mode
- Ideal for walking/driving surveys

## 📊 Data Analysis

### CSV Export Format

The exported CSV contains all raw data for analysis in Excel, Google Sheets, or specialized tools:

```csv
session_id,session_description,session_start_time,iperf_server,test_duration,ap_latitude,ap_longitude,result_id,timestamp,latitude,longitude,distance_from_ap,speed_mbps,test_duration_seconds,data_mb,azimuth,pitch,roll,accuracy
1,"Office Test","2025-01-15 10:30:00","192.168.1.100:5201",10,45.0065214,-93.2615774,1,"2025-01-15 10:31:23",45.006520,-93.261556,1.7,24.5,10.1,31.2,315.0,-0.5,-86.1,3.2
```

### Analysis Possibilities

**Coverage Analysis:**
- Map signal strength across geographical areas
- Identify dead zones and optimal coverage areas
- Compare performance at different distances from AP

**Antenna Pattern Analysis:**
- Correlate device orientation with signal strength
- Analyze directional antenna patterns
- Optimize antenna positioning

**Performance Optimization:**
- Identify factors affecting network performance
- Compare different AP locations
- Analyze environmental impact on signal quality

## 🛠️ Development

### Architecture

The app follows modern Android development practices:

- **Language**: Kotlin
- **Architecture**: MVVM with Room database
- **UI**: View Binding with Material Design components
- **Maps**: OSMDroid for offline-capable mapping
- **Networking**: Custom iPerf3 client implementation
- **Sensors**: Android Sensor Framework for orientation
- **Location**: FusedLocationProviderClient for GPS

### Key Dependencies

```gradle
dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Database
    implementation 'androidx.room:room-runtime:2.5.0'
    implementation 'androidx.room:room-ktx:2.5.0'
    kapt 'androidx.room:room-compiler:2.5.0'
    
    // Location Services
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    
    // Maps
    implementation 'org.osmdroid:osmdroid-android:6.1.16'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
}
```

### Building from Source

1. **Clone the repository**
2. **Open in Android Studio Arctic Fox or later**
3. **Sync Gradle files**
4. **Build and run** on device or emulator

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Generate release APK
./gradlew assembleRelease
```

## 🔒 Permissions

The app requires the following permissions:

- **ACCESS_FINE_LOCATION**: For high-precision GPS tracking
- **ACCESS_COARSE_LOCATION**: Fallback location access
- **INTERNET**: For iPerf3 network testing
- **ACCESS_NETWORK_STATE**: To check network connectivity

## 🐛 Troubleshooting

### Common Issues

**GPS Not Working:**
- Ensure location permissions are granted
- Check that location services are enabled
- Try testing outdoors for better GPS signal

**iPerf3 Connection Failed:**
- Verify iPerf3 server is running and accessible
- Check firewall settings on server
- Ensure device can reach server IP address
- Try telnet to server port to test connectivity

**Map Not Loading:**
- Check internet connectivity
- Clear app data if tiles are corrupted
- Ensure device has sufficient storage

**Tests Running Slowly:**
- Check network congestion
- Verify iPerf3 server performance
- Consider reducing test duration

### Performance Tips

- **GPS Accuracy**: Wait for GPS to stabilize before starting tests
- **Battery Life**: Use auto-testing sparingly to preserve battery
- **Data Usage**: Monitor data consumption during large tests
- **Storage**: Regularly export and clean old test data

## 🚀 Use Cases

### WiFi Site Surveys
- **Coverage Mapping**: Test signal strength across buildings/areas
- **Capacity Planning**: Measure performance under different loads
- **Troubleshooting**: Identify problem areas and interference sources

### Research Applications
- **Academic Studies**: Collect data for wireless networking research
- **Performance Analysis**: Analyze real-world network behavior
- **Environmental Impact**: Study how physical environment affects wireless signals

### Network Optimization
- **Access Point Placement**: Find optimal AP locations
- **Antenna Tuning**: Optimize antenna orientation and pattern
- **Performance Validation**: Verify network design meets requirements

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

### Development Guidelines
- Follow Android coding standards
- Write comprehensive tests
- Update documentation for new features
- Ensure backward compatibility

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **OSMDroid** for providing excellent offline mapping capabilities
- **iPerf3** for the reliable network testing protocol
- **Android Open Source Project** for the robust mobile platform
- **Material Design** for the beautiful UI components

## 📞 Support

For questions, issues, or feature requests:
- Open an issue on GitHub
- Check the troubleshooting section above
- Review existing issues and discussions

---

**GPS Speed Test** - Professional network performance analysis with precise location tracking.