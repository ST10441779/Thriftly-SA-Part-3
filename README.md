# ThriftlySA – Android Clothing Marketplace

## 1. Project Overview

ThriftlySA is an Android-based clothing marketplace developed using Kotlin and Jetpack Compose. The application provides users with a platform to browse, search, and publish second-hand clothing listings.

The aim of ThriftlySA is to encourage sustainable fashion by making it easier for people to buy and sell pre-loved clothing. The application combines online services with offline functionality to improve accessibility and the overall user experience.

## 2. Main Features

- **User Registration and Login:** Users can create accounts and securely access the application.
- **Google Sign-In:** Users can authenticate using their Google accounts.
- **Home Screen:** Displays available clothing listings.
- **Search and Filtering:** Users can search for clothing and browse listings by category.
- **Sell Clothing:** Users can publish their own clothing listings.
- **Favourites:** Users can save clothing listings they are interested in.
- **Offline Access:** Previously cached clothing listings remain accessible without an internet connection.
- **Data Synchronisation:** Supported saved data, including favourites, can synchronise when connectivity is restored.
- **Push Notifications:** Firebase Cloud Messaging supports notifications on user devices.
- **Profile Management:** Users can access their profile and application settings.

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Main programming language |
| Android Studio | Development environment |
| Jetpack Compose | User interface development |
| Supabase | Backend services and data management |
| Firebase Cloud Messaging | Push notifications |
| Google Sign-In | Google authentication |
| Retrofit | API communication |
| Room Database | Local data caching |
| EncryptedSharedPreferences | Secure local preference storage |
| Coroutines | Asynchronous operations |
| Git and GitHub | Version control and source code management |

## 4. Application Requirements

To build and run the application, you need:

- Android Studio.
- Android SDK.
- Kotlin support.
- An Android device or emulator.
- An internet connection for online functionality.
- A configured Supabase project.
- A configured Firebase project.

**Minimum Android version:** Android 7.0 (API 24), subject to the project's configured minimum SDK.

**Application package:** `com.example.thriftlysa`

## 5. Installation and Setup

### Step 1: Clone the Repository

Clone the project from GitHub:

```bash
https://github.com/ST10441779/Thriftly-SA-Part-3.git
```

Alternatively, download the repository as a ZIP file and extract it.

### Step 2: Open the Project

1. Open Android Studio.
2. Select **Open**.
3. Navigate to the extracted ThriftlySA project.
4. Select the project folder.
5. Allow Gradle to synchronise.

### Step 3: Configure Firebase

1. Open the Firebase Console.
2. Select the ThriftlySA Firebase project.
3. Register the Android application using the package name `com.example.thriftlysa`.
4. Download `google-services.json`.
5. Place the file inside the `app/` directory.

### Step 4: Configure Supabase

Configure the Supabase project URL and publishable key using the project's existing configuration.

Do not publish private credentials, service-role keys, passwords, or other sensitive information in the GitHub repository.

### Step 5: Run the Application

1. Connect an Android device or start an emulator.
2. Enable USB debugging if using a physical device.
3. Select the ThriftlySA run configuration.
4. Click **Run** in Android Studio.

## 6. Offline Functionality

ThriftlySA includes offline functionality to improve accessibility when users have limited or no internet connectivity.

The application supports:

- Accessing previously cached clothing listings.
- Viewing locally saved data.
- Retaining favourites for synchronisation.
- Synchronising supported information when the device reconnects to the internet.

Some application functions still require an active internet connection, including online authentication and communication with backend services.

## 7. Push Notifications

Firebase Cloud Messaging (FCM) is integrated into ThriftlySA.

The application can receive push notifications on supported Android devices. Notifications are handled through a Firebase messaging service.

## 8. Testing

The application should be tested using the following scenarios:

- Registering a new user.
- Logging in with existing credentials.
- Signing in using Google.
- Browsing clothing listings.
- Searching and filtering clothing.
- Publishing a clothing listing.
- Adding and removing favourites.
- Accessing cached listings while offline.
- Restoring internet connectivity and checking synchronisation.
- Receiving a Firebase test notification.
- Logging out of the application.

## 9. Project Structure

The Android application follows a Kotlin-based project structure.

```text
ThriftlySA/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/thriftlysa/
│   │       │       ├── MainActivity.kt
│   │       │       ├── ApiService.kt
│   │       │       ├── SecurePreferences.kt
│   │       │       ├── ThriftlyFirebaseMessagingService.kt
│   │       │       └── Other application files
│   │       │
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle.kts
│
├── gradle/
│   └── libs.versions.toml
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

*Note: The structure above illustrates the main project organisation. The actual source files includes additional folders and classes.*

## 10. Future Improvements

Possible future improvements include:

- Expanding offline listing creation and automatic upload.
- Implementing user-specific push notifications.
- Adding additional language support, including isiZulu and isiXhosa.
- Improving listing image management.
- Expanding automated testing.
- Improving accessibility and user interface customisation.

## 11. Author

**Project Name:** ThriftlySA  
**Platform:** Android  
**Programming Language:** Kotlin  
**Development Environment:** Android Studio

## 12. Conclusion

ThriftlySA provides a digital platform for buying and selling second-hand clothing. By combining a user-friendly Android interface, backend integration, offline data access and push notifications, the application aims to make sustainable fashion more accessible and convenient.
