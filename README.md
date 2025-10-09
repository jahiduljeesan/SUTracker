# 🚌 Subahon Student Map

A real-time university bus tracking and student map application built for **Sonargaon University**.  
Students can view live bus positions and route paths, while drivers can share their location updates in real time.

---

## 🚀 Features

### 🎓 For Students
- View **active university buses** on Google Maps in real time.
- See **driver info**, **route name**, **heading direction**, and **phone number**.
- Tap on a bus marker to view **ETA** and **distance** from your current location.
- Scroll through the list of all active buses at the bottom of the screen.

### 👨‍✈️ For Drivers
- **Start / Stop Tracking** their assigned bus with a single tap.
- Automatically upload **current GPS location** and **heading** to Firestore every few seconds.
- Set **bus route** and **status** dynamically.

### 🔗 Shared Features
- Google Maps integration with animated bus markers.
- Realtime Firestore listeners for instant updates.
- Role-based UI switching (student or driver).
- Lightweight Lottie animations for loading and transitions.

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-------------|
| **Frontend** | Android (Kotlin + XML) |
| **Architecture** | MVVM (ViewModel + LiveData + Firestore listeners) |
| **Backend** | Firebase Firestore + Firebase Auth |
| **Location** | FusedLocationProviderClient (Google Play Services) |
| **Map** | Google Maps SDK for Android |
| **UI** | RecyclerView, Lottie Animation, AlertDialog |
| **IDE** | Android Studio Hedgehog / Jellyfish |

---

## 📱 Core Components

### `StudentMapFragment.kt`
- Handles map initialization, live updates, and UI for both student and driver roles.
- Dynamically switches between `studentBottomView` and `driverBottomView`.
- Observes Firestore changes via `StudentMapViewModel`.

### `StudentMapViewModel.kt`
- Uses `addSnapshotListener()` to listen for **driver location changes**.
- Loads route polylines from Firestore’s `routes` collection.
- Keeps `buses` and `routePolylines` LiveData synced in real time.

### `BusListAdapter.kt`
- Displays a scrollable list of active buses below the map.
- Provides quick “Call Driver” action.
- Automatically updates with `updateList()` when Firestore data changes.

---
