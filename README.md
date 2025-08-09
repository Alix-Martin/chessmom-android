# Chess Monitor 🏆

A modern Android app for real-time chess tournament monitoring with intelligent player tracking and notifications.

## Features ✨

### 🎯 Core Functionality
- **Real-time Tournament Monitoring** - Automatically fetch and display tournament results every 15 minutes
- **Live Game Tracking** - Monitor ongoing games with instant updates when results are posted
- **Offline Support** - Full functionality with local data caching using Room database

### 👥 Player Management
- **Watch List System** - Track your favorite players across tournaments
- **Persistent Storage** - Watch list survives app restarts using DataStore
- **All Participants View** - See complete tournament roster, not just finished games
- **Tournament Standings** - Display accurate pre-round points for proper rankings

### 🔔 Smart Notifications
- **Filtered Alerts** - Only receive notifications for games involving watched players
- **Background Monitoring** - WorkManager ensures continuous tracking even when app is closed
- **Custom Titles** - Special notification headers for watched player games

### 🎨 Visual Excellence
- **Recent Updates Highlighting** - Pale yellow background for games finished in last 5 minutes
- **Watch List Indicators** - Light blue highlighting for games involving tracked players
- **Player Name Styling** - Bold, colored names for watched players throughout the app
- **Enhanced Splash Screen** - Larger, more prominent logo (200dp)

### 📊 Smart Data Display
- **Newest First Ordering** - Most recent results always appear at the top
- **Three-Tab Interface** - Results, Players, and Watch List in organized tabs
- **Conditional UI** - Clean status display that hides irrelevant information
- **Tournament Context** - Shows player standings before current round, not mid-round progress

## Screenshots 📱

*Coming soon - add screenshots of the app in action*

## Technical Stack 🛠️

### Architecture
- **MVVM Pattern** with Repository layer
- **Clean Architecture** with separation of concerns
- **Reactive Programming** using Kotlin Flows
- **Offline-First Design** with smart caching

### Technologies
- **Kotlin** - Modern, concise language
- **Jetpack Compose** - Declarative UI toolkit
- **Room Database** - Local data persistence
- **DataStore** - Preferences and settings storage
- **Hilt** - Dependency injection
- **Retrofit** - Network communication
- **WorkManager** - Background task scheduling
- **Material 3** - Modern Material Design

### Key Libraries
```kotlin
// UI & Architecture
implementation "androidx.compose.ui:ui:$compose_version"
implementation "androidx.hilt:hilt-navigation-compose:$hilt_version"
implementation "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version"

// Data & Storage
implementation "androidx.room:room-runtime:$room_version"
implementation "androidx.room:room-ktx:$room_version"
implementation "androidx.datastore:datastore-preferences:$datastore_version"

// Network & Background
implementation "com.squareup.retrofit2:retrofit:$retrofit_version"
implementation "androidx.work:work-runtime-ktx:$work_version"

// Dependency Injection
implementation "com.google.dagger:hilt-android:$hilt_version"
```

## Getting Started 🚀

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (API level 24)
- Kotlin 1.9.0+

### Installation
1. **Clone the repository**
   ```bash
   git clone https://github.com/Alix-Martin/chessmom-android.git
   cd chessmom-android
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build and Run**
   - Wait for Gradle sync to complete
   - Connect an Android device or start an emulator
   - Click "Run" or press Ctrl+R

### Configuration
The app works out of the box with default tournament monitoring settings. To get started:

1. **Enter Tournament Details**
   - Input tournament ID (e.g., 66882)
   - Specify round number (e.g., 8)

2. **Start Monitoring**
   - Tap "Start Monitoring"
   - Grant notification permissions when prompted

3. **Add Players to Watch List**
   - Navigate to "Players" tab
   - Tap "+" next to players you want to track
   - Switch to "Watch List" tab to manage tracked players

## Usage Guide 📖

### Basic Workflow
1. **Setup Tournament** - Enter ID and round number
2. **Start Monitoring** - Begin real-time tracking
3. **Add Watch List** - Select players to track
4. **Receive Notifications** - Get alerts for watched player games
5. **View Results** - Check latest results with visual highlights

### Navigation
- **Results Tab** - View finished games with highlighting for recent updates and watched players
- **Players Tab** - Browse all tournament participants and manage watch list
- **Watch List Tab** - Quick access to tracked players with removal options

### Visual Indicators
- 🟡 **Pale Yellow** - Recently finished games (last 5 minutes)
- 🔵 **Light Blue** - Games involving watched players
- **Bold Blue Text** - Watched player names throughout the app

## API Integration 🌐

The app integrates with chess tournament systems by parsing HTML pages. It's designed to work with standard tournament software that exports results to web pages.

### Supported Formats
- PAPI tournament system exports
- Standard pairing and results tables
- Real-time tournament updates

## Contributing 🤝

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Guidelines
- Follow existing code style and architecture patterns
- Add unit tests for new features
- Update documentation as needed
- Test on multiple Android versions

### Reporting Issues
- Use GitHub Issues for bug reports and feature requests
- Provide detailed reproduction steps
- Include Android version and device information

## License 📄

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Roadmap 🗺️

### Upcoming Features
- [ ] Multiple tournament monitoring
- [ ] Push notification customization
- [ ] Player statistics and history
- [ ] Tournament calendar integration
- [ ] Dark theme support
- [ ] Export results to CSV/PDF

### Performance Improvements
- [ ] Image caching for player photos
- [ ] Optimized database queries
- [ ] Reduced battery usage
- [ ] Faster app startup

## Support 💬

For questions, suggestions, or issues:
- 📧 Open a GitHub Issue
- 💬 Start a Discussion in the repository
- 🐛 Report bugs with detailed information

## Acknowledgments 🙏

- Chess tournament organizers who make their data accessible
- The Android development community for excellent libraries and tools
- Material Design team for beautiful UI components

---

**Made with ❤️ for the chess community**

*Keep track of your favorite players and never miss an important game result!*