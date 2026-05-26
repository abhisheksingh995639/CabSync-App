package com.cabsync.app.models

import com.google.firebase.firestore.PropertyName

data class ChatMessage(
    var id: String = "",
    var rideId: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var text: String = "",
    @get:com.google.firebase.firestore.Exclude
    @set:com.google.firebase.firestore.Exclude
    var timestamp: Long = 0L
) {
    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var rawTimestamp: Any?
        get() = timestamp
        set(value) {
            timestamp = when (value) {
                is Long -> value
                is Double -> value.toLong()
                is com.google.firebase.Timestamp -> value.toDate().time
                is java.util.Date -> value.time
                is Map<*, *> -> {
                    val seconds = value["seconds"] as? Long ?: 0L
                    seconds * 1000
                }
                else -> 0L
            }
        }
}

data class Review(
    var id: String = "",
    var userName: String = "",
    var userAvatar: String = "",
    var rating: Int = 0,
    var comment: String = "",
    var date: String = ""
)

data class JoinRequest(
    @get:PropertyName("uid") @set:PropertyName("uid") var userId: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var userName: String = "",
    @get:PropertyName("photoUrl") @set:PropertyName("photoUrl") var userAvatar: String = ""
)

data class RideRequest(
    var id: String = "",
    var rideId: String = "",
    var ridePickup: String = "",
    var rideDestination: String = "",
    var passengerId: String = "",
    var passengerName: String = "",
    @get:PropertyName("passengerPhoto")
    @set:PropertyName("passengerPhoto")
    var passengerPhoto: String = "",
    var hostId: String = "",
    var status: String = "pending",
    var timestamp: com.google.firebase.Timestamp? = null
)

data class Ride(
    var id: String = "",
    var hostId: String = "",
    var hostName: String = "",
    @get:PropertyName("hostPhoto")
    @set:PropertyName("hostPhoto")
    var hostAvatar: String = "",
    var pickup: String = "",
    var destination: String = "",
    @get:com.google.firebase.firestore.Exclude
    @set:com.google.firebase.firestore.Exclude
    var price: Int = 0,
    var time: String = "09:30 AM",
    var date: String = "",
    @get:PropertyName("availableSeats")
    @set:PropertyName("availableSeats")
    var seatsLeft: Int = 0,
    var seats: Int = 2,
    var isVerified: Boolean = true,
    var rating: Float = 0.0f,
    var totalRides: String = "1",
    var carModel: String = "Not Confirmed",
    var tags: List<String> = listOf("AC", "Verified"),
    var passengers: List<String> = emptyList(),
    var passengerDetails: List<JoinRequest> = emptyList(),
    var reviews: List<Review> = emptyList(),
    var status: String = "open",
    var pendingRequests: List<JoinRequest> = emptyList(),
    var createdAt: Any = java.util.Date()
) {
    @get:PropertyName("fare")
    @set:PropertyName("fare")
    var rawPrice: Any?
        get() = price
        set(value) {
            price = when (value) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull() ?: 0
                else -> 0
            }
        }
}
