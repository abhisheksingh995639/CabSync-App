package com.cabsync.app.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.cabsync.app.models.Review
import com.cabsync.app.models.Ride
import com.google.firebase.firestore.FirebaseFirestore

object RideService {
    private val SAMPLE_REVIEWS = listOf(
        Review(
            id = "r1",
            userName = "Karan J.",
            userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Karan",
            rating = 5,
            comment = "Great driver, very punctual and the car was clean.",
            date = "2 days ago"
        ),
        Review(
            id = "r2",
            userName = "Sanya M.",
            userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Sanya",
            rating = 4,
            comment = "Smooth journey, but we reached 10 mins late due to traffic.",
            date = "1 week ago"
        )
    )

    val MOCK_RIDES = listOf(
        Ride(
            id = "1",
            hostName = "Rahul Sharma",
            hostAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Rahul",
            pickup = "Gorakhpur",
            destination = "Lucknow",
            price = 540,
            time = "09:30 AM",
            date = "2026-05-15",
            seatsLeft = 2,
            isVerified = true,
            rating = 4.9f,
            totalRides = "1.2K",
            carModel = "Maruti Swift",
            tags = listOf("AC", "Top Rated"),
            reviews = SAMPLE_REVIEWS
        ),
        Ride(
            id = "2",
            hostName = "Priya Verma",
            hostAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Priya",
            pickup = "Lucknow",
            destination = "Delhi",
            price = 1250,
            time = "06:00 AM",
            date = "2026-05-16",
            seatsLeft = 3,
            isVerified = true,
            rating = 4.8f,
            totalRides = "850",
            carModel = "Honda City",
            tags = listOf("AC", "Quiet Ride"),
            reviews = SAMPLE_REVIEWS
        ),
        Ride(
            id = "3",
            hostName = "Abhishek Singh",
            hostAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Abhishek",
            pickup = "Gorakhpur",
            destination = "Bhopal",
            price = 610,
            time = "09:30 AM",
            date = "2026-05-15",
            seatsLeft = 1,
            isVerified = true,
            rating = 4.9f,
            totalRides = "1.5K",
            carModel = "Tata Nexon",
            tags = listOf("AC", "Pro Driver"),
            reviews = SAMPLE_REVIEWS
        ),
        Ride(
            id = "4",
            hostName = "Anjali Gupta",
            hostAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Anjali",
            pickup = "Delhi",
            destination = "Jaipur",
            price = 800,
            time = "10:00 AM",
            date = "2026-05-17",
            seatsLeft = 2,
            isVerified = true,
            rating = 4.7f,
            totalRides = "420",
            carModel = "Hyundai i20",
            tags = listOf("AC", "Music Lover"),
            reviews = SAMPLE_REVIEWS
        )
    )

    var liveRides by mutableStateOf<List<Ride>>(emptyList())
        private set

    private var listenerReg: com.google.firebase.firestore.ListenerRegistration? = null

    fun startListening() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            return
        }
        if (listenerReg != null) return

        try {
            val db = FirebaseFirestore.getInstance()
            listenerReg = db.collection("rides")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Firestore listen error: $error")
                        listenerReg = null
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val fetchedRides = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Ride::class.java)?.apply { id = doc.id }
                            } catch (e: Exception) {
                                println("Error parsing ride document ${doc.id}: $e")
                                null
                            }
                        }
                        liveRides = fetchedRides
                    }
                }
        } catch (e: Exception) {
            println("Firestore init error: $e")
            listenerReg = null
        }
    }

    fun parseTimeToMinutes(timeStr: String): Int {
        if (timeStr.isBlank()) return 0
        return try {
            val clean = timeStr.trim().uppercase()
            val parts = clean.substringBefore(" ").split(":")
            var hours = parts[0].toInt()
            val mins = if (parts.size > 1) parts[1].substringBefore("M").substringBefore("A").substringBefore("P").trim().toInt() else 0
            if (clean.contains("PM") && hours < 12) {
                hours += 12
            } else if (clean.contains("AM") && hours == 12) {
                hours = 0
            }
            hours * 60 + mins
        } catch (e: Exception) {
            0
        }
    }

    fun normalizeDate(d: String): String {
        val clean = d.trim().replace("-", "/")
        val parts = clean.split("/")
        if (parts.size == 3 && parts[0].length == 4) {
            return "${parts[2]}/${parts[1]}/${parts[0]}"
        }
        return clean
    }

    fun matchRides(pickup: String, destination: String, searchedDate: String = "", searchedTime: String = ""): List<Ride> {
        val list = liveRides
        val queryP = pickup.lowercase().trim()
        val queryD = destination.lowercase().trim()

        val filtered = if (queryP.isEmpty() && queryD.isEmpty()) {
            list
        } else {
            list.filter { ride ->
                val matchP = if (queryP.isNotEmpty()) ride.pickup.lowercase().contains(queryP) else false
                val matchD = if (queryD.isNotEmpty()) ride.destination.lowercase().contains(queryD) else false

                if (queryP.isNotEmpty() && queryD.isNotEmpty()) {
                    matchP && matchD
                } else {
                    matchP || matchD
                }
            }
        }

        val targetMins = if (searchedTime.isNotBlank()) parseTimeToMinutes(searchedTime) else -1
        val normTargetDate = if (searchedDate.isNotBlank()) normalizeDate(searchedDate) else ""

        return filtered.sortedWith(compareBy<Ride> { ride ->
            if (normTargetDate.isNotBlank()) {
                if (normalizeDate(ride.date) == normTargetDate) 0 else 1
            } else 0
        }.thenBy { ride ->
            if (targetMins >= 0) {
                val rideMins = parseTimeToMinutes(ride.time)
                Math.abs(rideMins - targetMins)
            } else {
                0
            }
        }.thenByDescending { ride ->
            ride.rating
        })
    }

    fun postRide(ride: Ride, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("rides").document()
            val rideWithId = ride.copy(id = ref.id)
            
            ref.set(rideWithId)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun observeMessages(rideId: String, onUpdate: (List<com.cabsync.app.models.ChatMessage>) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
        try {
            val db = FirebaseFirestore.getInstance()
            return db.collection("messages")
                .whereEqualTo("rideId", rideId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val msgs = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(com.cabsync.app.models.ChatMessage::class.java)?.apply { id = doc.id }
                            } catch (e: Exception) { null }
                        }.sortedBy { it.timestamp }
                        onUpdate(msgs)
                    }
                }
        } catch (e: Exception) {
            return null
        }
    }

    fun sendMessage(rideId: String, text: String) {
        try {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("messages").document()
            val msg = com.cabsync.app.models.ChatMessage(
                id = ref.id,
                rideId = rideId,
                senderId = user.uid,
                senderName = user.displayName?.ifEmpty { null } ?: user.email?.substringBefore('@') ?: "User",
                text = text,
                timestamp = System.currentTimeMillis()
            )
            ref.set(msg)
        } catch (e: Exception) {
            println("Send message error: $e")
        }
    }

    fun joinRide(rideId: String, onSuccess: () -> Unit) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("rides").document(rideId)
            ref.update("passengers", com.google.firebase.firestore.FieldValue.arrayUnion(user.uid))
                .addOnSuccessListener { onSuccess() }
        } catch (e: Exception) {
            println("Join ride error: $e")
        }
    }

    fun observeRequests(rideId: String, onUpdate: (List<com.cabsync.app.models.RideRequest>) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
        try {
            val db = FirebaseFirestore.getInstance()
            return db.collection("requests")
                .whereEqualTo("rideId", rideId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Observe requests error: $error")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(com.cabsync.app.models.RideRequest::class.java)?.apply { id = doc.id }
                            } catch (e: Exception) {
                                println("Parse request error for ${doc.id}: $e")
                                null
                            }
                        }
                        onUpdate(list)
                    }
                }
        } catch (e: Exception) {
            return null
        }
    }

    fun observeMyRequest(rideId: String, userId: String, onUpdate: (com.cabsync.app.models.RideRequest?) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
        try {
            val db = FirebaseFirestore.getInstance()
            return db.collection("requests")
                .whereEqualTo("rideId", rideId)
                .whereEqualTo("passengerId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Observe my request error: $error")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val reqs = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(com.cabsync.app.models.RideRequest::class.java)?.apply { id = doc.id }
                            } catch (e: Exception) { null }
                        }.filter { it.status != "cancelled" }
                        onUpdate(reqs.firstOrNull())
                    }
                }
        } catch (e: Exception) {
            return null
        }
    }

    fun observeMyAllRequests(userId: String, onUpdate: (List<com.cabsync.app.models.RideRequest>) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
        try {
            val db = FirebaseFirestore.getInstance()
            return db.collection("requests")
                .whereEqualTo("passengerId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Observe all my requests error: $error")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val reqs = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(com.cabsync.app.models.RideRequest::class.java)?.apply { id = doc.id }
                            } catch (e: Exception) { null }
                        }
                        onUpdate(reqs)
                    }
                }
        } catch (e: Exception) {
            return null
        }
    }

    fun cancelRequest(requestId: String, onSuccess: () -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("requests").document(requestId).delete()
                .addOnSuccessListener { onSuccess() }
        } catch (e: Exception) {
            println("Cancel request error: $e")
        }
    }

    fun requestToJoin(
        rideId: String,
        ridePickup: String,
        rideDestination: String,
        passengerId: String,
        passengerName: String,
        passengerPhoto: String,
        hostId: String,
        onSuccess: () -> Unit
    ) {
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("requests").document()
            val requestData = hashMapOf(
                "rideId" to rideId,
                "ridePickup" to ridePickup,
                "rideDestination" to rideDestination,
                "passengerId" to passengerId,
                "passengerName" to passengerName,
                "passengerPhoto" to passengerPhoto,
                "hostId" to hostId,
                "status" to "pending",
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            ref.set(requestData)
                .addOnSuccessListener { 
                    onSuccess() 
                    sendPushNotificationToHost(hostId, passengerName, ridePickup, rideDestination)
                }
        } catch (e: Exception) {
            println("Request to join error: $e")
        }
    }

    private fun sendPushNotificationToHost(hostId: String, passengerName: String, pickup: String, dest: String) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        
        db.collection("users").document(hostId).get().addOnSuccessListener { doc ->
            val hostToken = doc.getString("fcmToken")
            if (!hostToken.isNullOrEmpty()) {
                user.getIdToken(false).addOnSuccessListener { tokenResult ->
                    val authToken = tokenResult.token
                    if (authToken != null) {
                        Thread {
                            try {
                                val url = java.net.URL("https://cabsync.netlify.app/.netlify/functions/sendPush")
                                val connection = url.openConnection() as java.net.HttpURLConnection
                                connection.requestMethod = "POST"
                                connection.setRequestProperty("Content-Type", "application/json")
                                connection.doOutput = true
                                
                                val payload = org.json.JSONObject()
                                payload.put("token", authToken)
                                payload.put("targetToken", hostToken)
                                payload.put("title", "New Ride Request")
                                payload.put("body", "$passengerName wants to join your ride from $pickup to $dest.")
                                
                                val outputBytes = payload.toString().toByteArray(Charsets.UTF_8)
                                connection.outputStream.write(outputBytes)
                                connection.outputStream.flush()
                                connection.outputStream.close()
                                
                                val responseCode = connection.responseCode
                                println("Push notification sent: $responseCode")
                            } catch (e: Exception) {
                                println("Error sending push notification: $e")
                            }
                        }.start()
                    }
                }
            }
        }
    }

    fun acceptRequest(ride: com.cabsync.app.models.Ride, request: com.cabsync.app.models.RideRequest, onSuccess: () -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val rideRef = db.collection("rides").document(ride.id)
            val reqRef = db.collection("requests").document(request.id)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(rideRef)
                val currentSeats = snapshot.getLong("availableSeats") ?: snapshot.getLong("seatsLeft") ?: ride.seatsLeft.toLong()
                val updatedSeats = (currentSeats - 1).coerceAtLeast(0)

                transaction.update(reqRef, "status", "approved")
                transaction.update(rideRef, "availableSeats", updatedSeats)
                transaction.update(rideRef, "seatsLeft", updatedSeats)
                transaction.update(rideRef, "passengers", com.google.firebase.firestore.FieldValue.arrayUnion(request.passengerId))

                val joinDetail = hashMapOf(
                    "uid" to request.passengerId,
                    "name" to request.passengerName,
                    "photoUrl" to request.passengerPhoto
                )
                transaction.update(rideRef, "passengerDetails", com.google.firebase.firestore.FieldValue.arrayUnion(joinDetail))
            }.addOnSuccessListener { onSuccess() }
        } catch (e: Exception) {
            println("Accept request error: $e")
        }
    }

    fun declineRequest(requestId: String, onSuccess: () -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("requests").document(requestId)
                .update("status", "rejected")
                .addOnSuccessListener { onSuccess() }
        } catch (e: Exception) {
            println("Decline request error: $e")
        }
    }

    fun deleteRide(rideId: String, onSuccess: () -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("requests")
                .whereEqualTo("rideId", rideId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val batch = db.batch()
                    for (doc in querySnapshot.documents) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().addOnCompleteListener {
                        db.collection("rides").document(rideId).delete()
                            .addOnSuccessListener { onSuccess() }
                    }
                }
                .addOnFailureListener {
                    db.collection("rides").document(rideId).delete()
                        .addOnSuccessListener { onSuccess() }
                }
        } catch (e: Exception) {
            println("Delete ride error: $e")
        }
    }

    fun updateRideStatus(rideId: String, status: String, onSuccess: () -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("rides").document(rideId).update("status", status)
                .addOnSuccessListener { onSuccess() }
        } catch (e: Exception) {
            println("Update status error: $e")
        }
    }

    fun leaveRide(ride: com.cabsync.app.models.Ride, userId: String, onSuccess: () -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("rides").document(ride.id)
            val passengerDetailToRemove = ride.passengerDetails.find { it.userId == userId }

            db.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                val currentSeats = snapshot.getLong("availableSeats") ?: snapshot.getLong("seatsLeft") ?: ride.seatsLeft.toLong()
                val updatedSeats = currentSeats + 1
                transaction.update(ref, "availableSeats", updatedSeats)
                transaction.update(ref, "seatsLeft", updatedSeats)
                transaction.update(ref, "passengers", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                if (passengerDetailToRemove != null) {
                    transaction.update(ref, "passengerDetails", com.google.firebase.firestore.FieldValue.arrayRemove(passengerDetailToRemove))
                }
            }.addOnSuccessListener {
                db.collection("requests")
                    .whereEqualTo("rideId", ride.id)
                    .whereEqualTo("passengerId", userId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val batch = db.batch()
                        for (doc in querySnapshot.documents) {
                            batch.delete(doc.reference)
                        }
                        batch.commit().addOnSuccessListener { onSuccess() }
                    }
                    .addOnFailureListener { onSuccess() }
            }
        } catch (e: Exception) {
            println("Leave ride error: $e")
        }
    }
}
