# CabSync — Android App

<p align="center">
  <strong>A native Android ride-sharing & carpooling app built with Jetpack Compose and Firebase.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-API%2024%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android API 24+" />
  &nbsp;
  <img src="https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  &nbsp;
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  &nbsp;
  <img src="https://img.shields.io/badge/Firebase-Firestore%20%2B%20FCM-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase" />
</p>

---

## 📖 Overview

CabSync for Android is the native mobile companion to the [CabSync Web Platform](../CabSync-Website). It allows users to post rides, search for shared cabs, manage join requests, chat with fellow riders, and receive real-time push notifications — all backed by the same Firebase project as the website.

- **Min SDK:** API 24 (Android 7.0 Nougat)
- **Target SDK:** API 35 (Android 15)
- **Version:** 1.0.0

---

## ✨ Features

| Feature | Description |
|---|---|
| 🚀 **Onboarding** | Animated multi-step onboarding for first-time users |
| 🔐 **Authentication** | Email/password & Google Sign-In via Firebase Auth |
| 🏠 **Home Feed** | Live-updating list of all active rides from Firestore |
| 🔍 **Search & Filter** | Search by pickup, destination, date, and time with smart sorting |
| 🚗 **Post a Ride** | Create rides with route, seats, fare, AC preference, and car model |
| 📋 **Ride Details** | Full ride info, live seat count, fare split, and join request button |
| ✅ **Request System** | Passengers request → Host approves/rejects in-app |
| 💬 **Real-time Chat** | Per-ride group chat with live Firestore listeners |
| 🔔 **Push Notifications** | FCM push notifications when someone requests to join your ride |
| 👤 **Profile** | Edit photo, name, bio; view your rating and ride stats |
| 🌐 **Public Profiles** | View any user's public profile, history, and reviews |
| ⭐ **Ratings** | Rate and review users after completing a ride |
| 📜 **History** | Archive of all completed and cancelled rides |
| ⚙️ **Settings** | Account management, theme toggle, deactivate/delete account |
| 🌙 **Dark Mode** | Full dark theme support alongside light mode |
| 🚫 **Banned Screen** | Graceful experience for banned accounts with appeal info |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | [Kotlin](https://kotlinlang.org/) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| **Design System** | [Material 3](https://m3.material.io/) |
| **Navigation** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) |
| **Image Loading** | [Coil](https://coil-kt.github.io/coil/) (with SVG support) |
| **Icons** | [Material Icons Extended](https://developer.android.com/reference/kotlin/androidx/compose/material/icons/package-summary) |
| **Auth** | [Firebase Authentication](https://firebase.google.com/products/auth) + Google Sign-In |
| **Database** | [Cloud Firestore](https://firebase.google.com/products/firestore) |
| **Push Notifications** | [Firebase Cloud Messaging (FCM)](https://firebase.google.com/products/cloud-messaging) |
| **Build System** | [Gradle (Kotlin DSL)](https://docs.gradle.org/current/userguide/kotlin_dsl.html) |

---

## 📁 Project Structure

```
Cabsync-App/
│
├── app/
│   └── src/main/
│       ├── java/com/cabsync/app/
│       │   │
│       │   ├── MainActivity.kt              # Single Activity; hosts NavHost & all routes
│       │   │
│       │   ├── data/
│       │   │   ├── RideService.kt           # All ride CRUD, request handling, push trigger
│       │   │   └── UserService.kt           # User profile read/write, account deletion
│       │   │
│       │   ├── models/
│       │   │   ├── Ride.kt                  # Ride data class (Firestore-mapped)
│       │   │   └── UserProfile.kt           # User profile data class
│       │   │
│       │   ├── services/
│       │   │   └── FCMService.kt            # FirebaseMessagingService: receive & display notifications
│       │   │
│       │   └── ui/
│       │       ├── components/
│       │       │   └── Components.kt        # All shared composables (buttons, cards, inputs, etc.)
│       │       │
│       │       ├── screens/
│       │       │   ├── SplashScreen.kt      # Animated logo splash
│       │       │   ├── OnboardingPage.kt    # Onboarding carousel container
│       │       │   ├── OnboardingStep.kt    # Individual onboarding slide
│       │       │   ├── FinalOnboardingPage.kt # Last onboarding CTA slide
│       │       │   ├── LoginScreen.kt       # Email + Google login
│       │       │   ├── SignUpScreen.kt      # New account registration
│       │       │   ├── ResetPasswordScreen.kt # Password reset via email
│       │       │   ├── HomeScreen.kt        # Main feed; live ride list + FCM token init
│       │       │   ├── SearchScreen.kt      # Search & filter rides
│       │       │   ├── PostRideScreen.kt    # Create & publish a new ride
│       │       │   ├── RideDetailsScreen.kt # Full ride view, join request, host controls
│       │       │   ├── ChatScreen.kt        # Per-ride real-time group chat
│       │       │   ├── MessagesScreen.kt    # All conversations list
│       │       │   ├── JoinedRidesScreen.kt # Rides joined as passenger
│       │       │   ├── HistoryScreen.kt     # Completed & cancelled ride history
│       │       │   ├── ProfileScreen.kt     # Own profile, stats, and reviews
│       │       │   ├── PublicProfileScreen.kt # Any user's public profile
│       │       │   ├── SettingsScreen.kt    # Preferences, theme, account management
│       │       │   ├── StaticContentScreen.kt # Terms / Privacy policy viewer
│       │       │   └── BannedScreen.kt      # Shown when account is banned
│       │       │
│       │       └── theme/
│       │           ├── CabSyncTokens.kt     # Color tokens, typography, shapes
│       │           └── ThemeManager.kt      # Light/dark theme state holder
│       │
│       ├── res/
│       │   ├── drawable/                    # App icon, logo, splash assets
│       │   └── values/                      # Strings, colors, styles
│       │
│       └── AndroidManifest.xml             # Permissions, activity, FCMService declaration
│
├── build.gradle.kts                         # Project-level Gradle config
├── app/build.gradle.kts                     # App-level dependencies & build config
├── google-services.json                     # Firebase config (NOT committed to git)
└── gradlew / gradlew.bat                    # Gradle wrapper scripts
```

---

## ⚙️ Local Setup

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 21** (bundled with recent Android Studio releases)
- **Android SDK** with API 24–35 installed
- A **Firebase project** with Authentication and Firestore enabled

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/CabsyncWhole.git
cd CabsyncWhole/Cabsync-App
```

### 2. Add `google-services.json`

1. Go to [Firebase Console](https://console.firebase.google.com/) → Your Project → Project Settings → Android app.
2. Download `google-services.json`.
3. Place it in `app/google-services.json`.

> ⚠️ This file is listed in `.gitignore` and must **never** be committed to version control.

### 3. Build & Run

**Option A — Android Studio (Recommended):**
1. Open the `Cabsync-App` folder in Android Studio.
2. Wait for Gradle sync to complete.
3. Select a device/emulator and click **▶ Run**.

**Option B — Command Line:**
```bash
# Debug build
.\gradlew.bat assembleDebug

# Install directly on connected device
.\gradlew.bat installDebug
```

The debug APK will be output to:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔔 Push Notification Setup

Push notifications are sent when a passenger requests to join a ride. The flow works as follows:

```
Passenger taps "Request to Join"
        │
        ▼
[RideService.kt]
1. Writes request to Firestore
2. Fetches host's FCM token from Firestore
3. Gets own Firebase Auth ID token
4. HTTP POST → Netlify serverless function
        │
        ▼
[sendPush.js — Netlify Function]
1. Verifies Firebase Auth ID token
2. Sends notification via Firebase Admin SDK
        │
        ▼
[Host's Device — FCMService.kt]
Receives & displays the notification
```

### Required Permissions (`AndroidManifest.xml`)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

On Android 13+ (API 33+), the app requests `POST_NOTIFICATIONS` at runtime on first launch.

### FCM Token Registration

On every app launch, `HomeScreen.kt` automatically:
1. Fetches the device's current FCM token via `FirebaseMessaging.getInstance().token`.
2. Saves it to the authenticated user's Firestore document under the `fcmToken` field.

This ensures notifications always reach the correct device, even after reinstalls.

---

## 🗄️ Firestore Data Model

| Collection | Key Fields |
|---|---|
| `users` | `uid`, `name`, `email`, `photoUrl`, `fcmToken`, `rating`, `isBanned` |
| `rides` | `hostId`, `hostName`, `pickup`, `destination`, `date`, `time`, `fare`, `seats`, `availableSeats`, `status`, `passengers[]` |
| `requests` | `rideId`, `passengerId`, `hostId`, `passengerName`, `passengerPhoto`, `status` (`pending`/`approved`/`rejected`/`cancelled`) |
| `messages` | `rideId`, `senderId`, `senderName`, `text`, `timestamp` |
| `ratings` | `rideId`, `raterId`, `ratedUserId`, `score`, `review` |

---

## 🎨 Design System

CabSync uses a custom token-based design system defined in [`CabSyncTokens.kt`](app/src/main/java/com/cabsync/app/ui/theme/CabSyncTokens.kt):

| Token | Light Mode | Dark Mode | Role |
|---|---|---|---|
| Brand Yellow | `#FFD100` | `#FFD100` | Primary accent, CTAs |
| Background | `#FFFFFF` | `#121212` | Page background |
| Surface / Card | `#F5F5F5` | `#1E1E1E` | Card backgrounds |
| Text Primary | `#1A1A1A` | `#FFFFFF` | Body text |
| Text Muted | `#777777` | `#AAAAAA` | Secondary/hint text |

---

## 🗺️ Navigation Routes

All navigation is handled by a single `NavHost` in `MainActivity.kt`:

| Route | Screen |
|---|---|
| `splash` | `SplashScreen` |
| `onboarding` | `OnboardingPage` |
| `login` | `LoginScreen` |
| `signup` | `SignUpScreen` |
| `reset_password` | `ResetPasswordScreen` |
| `home` | `HomeScreen` |
| `search` | `SearchScreen` |
| `post_ride` | `PostRideScreen` |
| `ride_details/{rideId}` | `RideDetailsScreen` |
| `chat/{rideId}` | `ChatScreen` |
| `messages` | `MessagesScreen` |
| `joined_rides` | `JoinedRidesScreen` |
| `history` | `HistoryScreen` |
| `profile` | `ProfileScreen` |
| `public_profile/{userId}` | `PublicProfileScreen` |
| `settings` | `SettingsScreen` |
| `banned` | `BannedScreen` |

---

## 🤝 Related Projects

This app is part of the **CabSync** monorepo:

| Project | Description |
|---|---|
| [`Cabsync-App`](.) | ← You are here (Android native app) |
| [`CabSync-Website`](../CabSync-Website) | React/Vite web platform |

Both share the same Firebase project (Firestore, Auth, FCM).

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).

---

<p align="center">
  Made with ❤️ by the CabSync team &nbsp;·&nbsp; <a href="https://cabsync.netlify.app">cabsync.netlify.app</a>
</p>
