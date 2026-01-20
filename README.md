# BFRB Tracker - Wearable Movement Detection App

A dual-app system for tracking Body-Focused Repetitive Behaviors (BFRBs) using Wear OS watch sensors and an Android phone companion app.

## 🎯 Features

### Wear OS App
- **Real-time movement detection** using accelerometer and gyroscope sensors
- **Haptic feedback** (vibration) when repetitive movement is detected
- **User confirmation** dialog to reduce false positives
- **Foreground service** for continuous background monitoring
- **Adjustable sensitivity** thresholds for movement detection

### Phone Companion App
- **Live dashboard** showing BFRB events
- **Metrics and analytics** (events per day, weekly trends)
- **Feeling logger** to track emotional state during events
- **Data synchronization** with Wear OS watch

## 📋 Project Structure

```
BFRBTracker2/
├── mobile/                          # Android phone app
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/bfrb/tracker/
│       │   ├── MainActivity.kt      # Main dashboard screen
│       │   └── ui/theme/Theme.kt    # Material3 theming
│       └── res/
│           ├── values/
│           │   ├── strings.xml
│           │   └── themes.xml
│           └── ...
│
└── wear/                            # Wear OS watch app
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/bfrb/tracker/wear/
        │   ├── presentation/
        │   │   └── MainActivity.kt  # Watch UI and confirmation dialog
        │   └── service/
        │       └── MovementDetectionService.kt  # Sensor processing
        └── res/
            └── values/strings.xml
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- JDK 17 or later
- A Wear OS emulator or physical Wear OS device (API 30+)
- An Android phone emulator or physical device (API 26+)

### Setup Instructions

1. **Open in Android Studio**
   ```bash
   # Open the BFRBtracker2 folder in Android Studio
   File > Open > Select BFRBtracker2 directory
   ```

2. **Sync Gradle**
   - Android Studio will automatically prompt you to sync Gradle
   - Click "Sync Now" when prompted
   - Wait for dependencies to download (first time may take 5-10 minutes)

3. **Set Up Emulators** ⚠️ **YOU ARE HERE**

   **For Wear OS Watch:**
   - Open Device Manager (Tools > Device Manager)
   - Click "+ Create Device"
   - Select "Wear OS" category
   - Choose a Wear OS device (recommended: "Wear OS Large Round")
   - Select API 33 or 34 system image
   - Click "Finish"

   **For Android Phone:**
   - In Device Manager, click "+ Create Device"
   - Select "Phone" category
   - Choose a phone model (recommended: "Pixel 6")
   - Select API 34 system image
   - Click "Finish"

4. **Run the Apps**

   **Option A: Run Both Apps Separately (Recommended for First Test)**

   a. Start the Wear OS app:
   - Select "wear" from the run configuration dropdown (top toolbar)
   - Select your Wear OS emulator from device dropdown
   - Click Run (green play button)

   b. Start the Phone app:
   - Select "mobile" from the run configuration dropdown
   - Select your Android phone emulator
   - Click Run

   **Option B: Run Both Simultaneously**
   - You can run both configurations at the same time to test communication

## 🧪 Testing the Movement Detection

### On the Wear OS Watch:

1. **Grant Permissions**
   - When you first launch the watch app, it will request BODY_SENSORS permission
   - Tap "Allow" to grant the permission

2. **Start Detection**
   - Tap the "Start Detection" button
   - The status should change to "Active" (green)
   - A notification should appear saying "Movement detection active"

3. **Simulate Movement**
   - In the Android Emulator, use the "Virtual Sensors" panel:
     - Click on the three dots (...) on the emulator sidebar
     - Select "Virtual sensors"
     - Go to "Accelerometer" tab
     - Move the slider or rotate the device visualization

4. **Trigger Detection**
   - Make quick, repetitive movements in the accelerometer panel
   - The watch should vibrate and show a confirmation dialog
   - Tap "Yes" or "No" to confirm if it was a real BFRB event

### On the Phone:

1. **View Dashboard**
   - The phone app shows a simple dashboard
   - Currently displays "Events Today: 0"
   - Will show real-time updates when connected to watch (future feature)

2. **Log Feelings** (Coming Soon)
   - Tap "Log Feeling" button
   - Enter your emotional state
   - Data will be stored and synced with events

## ⚙️ Customizing Movement Detection

The movement detection algorithm is in `wear/src/main/java/com/bfrb/tracker/wear/service/MovementDetectionService.kt`

### Adjustable Parameters (line 28-33):

```kotlin
private val movementThreshold = 15.0f     // Accelerometer threshold (higher = less sensitive)
private val gyroThreshold = 2.0f          // Gyroscope threshold (rotation sensitivity)
private val detectionWindow = 500L        // Minimum time between detections (ms)
private val maxReadings = 50              // Number of readings to analyze
```

### How to Adjust for Your Specific Movement:

1. **If you get too many false positives:**
   - Increase `movementThreshold` (try 20.0f or 25.0f)
   - Increase `gyroThreshold` (try 3.0f or 4.0f)
   - Increase `detectionWindow` (try 1000L for 1 second)

2. **If you're missing real events:**
   - Decrease `movementThreshold` (try 10.0f or 12.0f)
   - Decrease `gyroThreshold` (try 1.5f or 1.0f)
   - Decrease `detectionWindow` (try 300L)

3. **For specific movement patterns:**
   - The algorithm detects repetitive patterns by analyzing variance
   - Lower variance = more repetitive motion
   - Adjust the variance threshold in line 141: `if (variance < 5.0f)`

## 🔄 Next Development Steps

### Currently Implemented ✅
- [x] Project structure with Gradle build system
- [x] Wear OS app with sensor-based movement detection
- [x] Haptic feedback (vibration) on detection
- [x] User confirmation UI on watch
- [x] Phone app with basic dashboard
- [x] Material3 UI theming

### Coming Next 🚧
- [ ] Data persistence (Room database)
- [ ] Watch-to-phone communication via Wearable DataClient API
- [ ] Real-time event synchronization
- [ ] Metrics and analytics dashboard on phone
- [ ] Feeling/emotion logger
- [ ] Charts and visualizations (using Vico library)
- [ ] Historical data viewing
- [ ] Export data functionality
- [ ] Custom movement pattern training

## 📱 Permissions Explained

### Wear OS App:
- `BODY_SENSORS` - Required to access accelerometer and gyroscope
- `VIBRATE` - For haptic feedback when movement is detected
- `WAKE_LOCK` - Keep device awake during detection
- `INTERNET` - For future data sync features

### Phone App:
- `WAKE_LOCK` - For background data sync
- `INTERNET` - For future cloud sync features

## 🐛 Troubleshooting

### Build Issues:
- **Gradle sync failed**: Make sure you have internet connection for dependency downloads
- **SDK not found**: Install Android SDK 34 via SDK Manager (Tools > SDK Manager)
- **JDK version error**: Ensure you're using JDK 17 (File > Project Structure > SDK Location)

### Runtime Issues:
- **App crashes on start**: Check logcat for errors (View > Tool Windows > Logcat)
- **Sensors not working**: Ensure you granted BODY_SENSORS permission
- **No vibration**: Check that vibration is enabled on the emulator
- **Detection not triggering**: Try adjusting threshold values (see Customization section)

### Emulator Issues:
- **Emulator won't start**: Ensure HAXM/KVM virtualization is enabled
- **Sensors not available**: Use a recent API level (33+) system image
- **Poor performance**: Allocate more RAM to emulator (AVD settings)

## 📚 Technical Details

### Movement Detection Algorithm

The algorithm uses a multi-stage approach:

1. **Continuous Sensor Reading**: Monitors accelerometer and gyroscope at ~50Hz
2. **Magnitude Calculation**: Computes movement intensity (subtracting gravity)
3. **Pattern Analysis**: Stores last 50 readings (~1 second of data)
4. **Variance Analysis**: Calculates variance to detect repetitive patterns
5. **Threshold Check**: Triggers when variance indicates repetitive motion
6. **Cooldown Period**: Prevents duplicate detections within 500ms

### Key Technologies Used

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI framework (both mobile and wear)
- **Wear OS Compose**: Optimized Compose for wearables
- **Android Sensors API**: Accelerometer and gyroscope access
- **Foreground Service**: Background monitoring on watch
- **Material3 Design**: Modern Android design system

## 📄 License

This project is created for personal BFRB management and tracking.

## 🤝 Support

For issues or questions:
1. Check the Troubleshooting section above
2. Review Android Studio logcat for error messages
3. Ensure all prerequisites are installed
4. Verify emulator/device settings

---

**Current Status**: ✅ Ready for testing in Android Studio

**Next Step**: Follow "Setup Instructions" section above to run the app!
