<div align="center">
    <h1>Taxi Service</h1>
    <img src="app/src/main/res/drawable/app_logo.jpg" alt="Logo" width="200"/>
</div>

A modern taxi application developed using the MVVM pattern with a variety of libraries to ensure a robust and user-friendly experience.

<div align="center">
    <h1>GIF</h1>
    <img src="app/src/main/res/drawable/example_login.gif" alt="App GIF" width="200"/>
  <img src="app/src/main/res/drawable/example_work.gif" alt="App GIF" width="200"/>
</div>

## Description

This project is a feature-rich taxi app that enables users to book rides in real-time, view driver locations on a map, and track their rides. The app utilizes a combination of modern Android libraries and Firebase for backend services, ensuring a seamless and responsive user experience. The UI is built using Jetpack Compose, providing a modern look and feel.

### Dependencies

* Kotlin 1.9.0
* Gradle 8.2.2
* Java 1.8
* Android Studio (latest version recommended)
* [Jetpack Compose](https://developer.android.com/jetpack/compose) for UI
* [Firebase](https://firebase.google.com/) for authentication, database, and storage
* [Google Maps](https://developers.google.com/maps/documentation/android-sdk/overview) for displaying maps and location services

Below are the specific libraries used in the project:

```groovy
implementation ("androidx.navigation:navigation-fragment-ktx:2.3.5")
implementation ("androidx.navigation:navigation-ui-ktx:2.3.5")
implementation("androidx.core:core-ktx:1.9.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
implementation("com.google.firebase:firebase-database-ktx:20.3.0")
implementation("com.google.firebase:firebase-firestore-ktx:24.10.1")
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.0.1")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
implementation("com.google.android.libraries.places:places:3.3.0")
implementation ("com.firebase:geofire-android:3.1.0")
implementation ("androidx.appcompat:appcompat:1.6.1")
implementation("com.firebase:geofire:2.3.1")
implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
implementation("androidx.legacy:legacy-support-v4:1.0.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
implementation ("androidx.compose.runtime:runtime-livedata:1.6.5")
implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation ("androidx.compose.foundation:foundation:1.0.0-alpha13")
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
implementation("androidx.wear.compose:compose-material:1.3.1")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.activity:activity-compose:1.7.0")
implementation(platform("androidx.compose:compose-bom:2023.08.00"))
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
testImplementation("junit:junit:4.13.2")
androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:")
debugImplementation("androidx.compose.ui:ui-tooling")
debugImplementation("androidx.compose.ui:ui-test-manifest")
runtimeOnly("androidx.compose.ui:ui:1.6.5")
implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
runtimeOnly("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation ("androidx.compose.ui:ui:1.6.5")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-analytics")
```

### Installing

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/NovikFeed/taxi-service/tree/develop
   ```
2. Navigate to the project directory:
   ```bash
   cd TaxiApp
   ```
3. Open the project in Android Studio.
4. Sync the project with Gradle files by clicking on "Sync Project with Gradle Files" in Android Studio.

### Executing Program

1. Build and run the app by selecting the desired emulator or connected device.
2. Click the "Run" button in Android Studio or use the following command in your terminal:
   ```bash
   ./gradlew assembleDebug
   ```
3. Once the app is installed on your device or emulator, you can start exploring the features.

## Help

If you encounter any issues, please refer to the following command to check the app's log output for troubleshooting:
```bash
adb logcat
```

## Version History

* 0.2
    * Various bug fixes and optimizations
    * Improved UI and user experience
    * See [commit change](https://github.com/NovikFeed/taxi-service/commits/develop/) or See [release history](https://github.com/NovikFeed/taxi-service/commits/)
* 0.1
    * Initial Release
    * Basic features implemented

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

* Inspired by various open-source projects and tutorials on Android development.

## Other Projects

[MovieApp](https://github.com/NovikFeed/MoviesApp), [Chatty](https://github.com/NovikFeed/chat-application)
