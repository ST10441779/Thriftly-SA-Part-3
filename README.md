# Thriftly SA

Thriftly SA is an Android marketplace concept for buying and selling pre-owned goods. It is built with Kotlin, Jetpack Compose and Retrofit, with a REST API expected to provide authentication, listings and favourites.

## Features currently included
- Account registration and email/password login through the REST API.
- Home feed for available listings.
- Search by title/description and category filters.
- Create and publish a listing with input validation.
- Favourite/unfavourite listings.
- Profile preferences for language, notifications and dark-mode preference.
- Encrypted local storage for session and preference values using AndroidX Security Crypto.
- Offline listing submission queue backed by SQLite. When the Home screen is opened while connected, queued listings are retried against the API.
- Basic unit tests for input validation.
- GitHub Actions workflow to run unit tests and assemble a debug APK.

## Technology
- Kotlin and Jetpack Compose
- Retrofit 3 and Gson
- Kotlin Coroutines
- SQLite (offline pending-listing queue)
- AndroidX Security Crypto
- JUnit 4

## Requirements
- Android Studio with a compatible Android SDK
- JDK 17
- A reachable Thriftly SA REST API with the endpoints listed below

## Configure the REST API
The API base URL is no longer hard-coded in `ApiClient.kt`. Set it in the project's `gradle.properties`:

```properties
THRIFTLY_API_BASE_URL=http://10.0.2.2/thriftly/
```

`10.0.2.2` is the Android emulator alias for the host computer. For a physical Android phone, replace it with the computer/server's LAN IP address, for example `http://192.168.1.10/thriftly/`. Both devices must be on the same network. Use HTTPS for a production deployment.

The app expects these endpoints:
- `POST register.php`
- `POST login.php`
- `GET listings.php`
- `POST create_listing.php`
- `POST favourite.php`

Request and response fields are defined in `ApiModels.kt` and `ApiService.kt`. The PHP/API backend is not included in this Android project ZIP.

## Offline synchronisation
When a listing cannot be submitted because the API connection fails, the app saves its request in a local SQLite queue and informs the user. The next time the Home screen loads and the API is reachable, pending listing submissions are sent and removed from the queue after the server confirms success. This is a focused offline feature, not full offline browsing.

## Security notes
- The app encrypts locally stored session/preferences. It does not persist the user's plain-text password.
- Password hashing/encryption must also be implemented by the server. The backend should hash passwords with a password-specific algorithm such as Argon2id or bcrypt and verify hashes during login. Client-side encryption alone does not protect server credentials.
- Production deployment should use HTTPS, validate all server inputs, and avoid exposing secrets in the Android app.

## Important remaining Part 3 integration work
Some rubric requirements cannot be completed or verified from this ZIP alone because the required external services and configuration are not present:
1. **SSO:** configure a real identity provider (for example Google Sign-In/Firebase Authentication), create OAuth credentials for the final package/signing certificate, and connect it to the backend.
2. **NoSQL database and blob storage:** configure a hosted service (such as Firebase/Firestore plus Cloud Storage, or another suitable provider) and implement its backend integration. The current app's REST API backend is external and was not included.
3. **Push notifications:** configure Firebase Cloud Messaging, add the Firebase project configuration and server-side push sender, then test on a physical device.
4. **Two-language support:** the settings screen currently stores a language preference, but the full app interface still needs translated Android string resources and runtime locale switching for at least two South African languages.
5. **Play Store evidence:** generate a signed release build, capture store screenshots, complete the Play Console listing and provide the required evidence.
6. **Demonstration video:** record the completed app on a physical Android phone with voice-over, including evidence of the hosted authentication/API/database records.

These require service credentials, a deployed backend, a physical phone and/or account access. They should not be represented as completed until tested.

## Build and test
From the project root:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

On Windows:

```bat
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions
The workflow in `.github/workflows/android.yml` runs unit tests and assembles the debug APK on pushes and pull requests.

## Release notes — Part 3 update
- Replaced the fixed API address with a Gradle-configurable API base URL.
- Added encrypted local preferences for session and app settings.
- Added a SQLite-backed offline queue for listing submissions and retry-on-home-load synchronisation.
- Added validation unit tests and a GitHub Actions Android build workflow.
- Added project setup, security, offline behaviour and remaining integration requirements to this README.

## Demonstration video
Add your unlisted YouTube demonstration link here after recording: **[YouTube demonstration link]**

## AI tools disclosure
AI assistance may be used to help inspect code, identify defects, draft documentation and suggest implementation changes. The developer must review, run, understand and test all generated or modified code, and disclose the specific assistance used in the final submission according to the institution's referencing requirements.

## Supabase authentication configuration

ThriftlySA now uses Supabase Auth for email/password registration and sign-in. The Android app sends authentication requests over HTTPS to the configured Supabase project. Supabase, rather than the app, securely hashes and verifies passwords. The app does not save raw passwords. On successful sign-in, the access token, refresh token, and Supabase user UUID are stored using Android EncryptedSharedPreferences.

The project is configured with the Supabase project URL and **publishable** key supplied for ThriftlySA. A publishable key is intended for client-side use; never replace it with a Supabase secret key or service-role key.

### Supabase dashboard setup

1. Open the ThriftlySA project in the Supabase Dashboard.
2. Go to **Authentication → Providers → Email** and make sure Email provider is enabled.
3. Keep email confirmation enabled for normal use. After registering, users must open the confirmation email before signing in. For a quick classroom test only, you may temporarily turn off email confirmation in the Email provider settings.
4. In **Authentication → Users**, check that a newly registered account appears.
5. Test registration with an email address you can access, confirm it if required, then sign in using the same email and password.

No manual SQL table is required for Supabase Auth to create accounts. The user's display name is saved as Auth user metadata (`full_name` and `name`).

### Important backend status

This change migrates **registration and login** to Supabase Auth. Product listing, creating listings, and favourites still use the separate legacy PHP API configured by `THRIFTLY_API_BASE_URL` (default: `http://10.0.2.2/thriftly/`). Those features will need their own Supabase database/API migration or a deployed PHP backend before they can work on a physical phone. Do not treat the listings backend as migrated just because Supabase login works.

### Supabase auth implementation references

- Supabase, *Use Supabase with Android Kotlin*: https://supabase.com/docs/guides/getting-started/quickstarts/kotlin
- Supabase, *Kotlin: Create a new user*: https://supabase.com/docs/reference/kotlin/auth-signup
- Supabase, *Kotlin: Initializing*: https://supabase.com/docs/reference/kotlin/initializing
