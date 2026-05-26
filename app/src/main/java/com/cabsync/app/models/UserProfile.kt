package com.cabsync.app.models

import com.google.firebase.firestore.PropertyName

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    @get:PropertyName("isVerified")
    val isVerified: Boolean = false,
    val phoneVisibility: String = "Everyone",
    val notificationsEnabled: Boolean = true,
    val rideHistoryVisible: Boolean = true
)
