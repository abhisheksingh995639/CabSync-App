package com.cabsync.app.data

import android.content.SharedPreferences
import com.cabsync.app.models.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserService {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    private fun isCollegeEmail(email: String?): Boolean {
        if (email.isNullOrEmpty()) return false
        val lower = email.lowercase()
        return lower.endsWith(".edu") || lower.endsWith(".ac.in")
    }

    suspend fun getUserProfile(uid: String, sharedPrefs: SharedPreferences, fallbackEmail: String?, fallbackName: String?, fallbackPhone: String?, fallbackPhotoUrl: String?): UserProfile {
        return try {
            val snapshot = usersRef.document(uid).get().await()
            if (snapshot.exists() && snapshot.toObject(UserProfile::class.java) != null) {
                val profile = snapshot.toObject(UserProfile::class.java)!!
                // Ensure local SharedPreferences is updated with server values if valid
                sharedPrefs.edit()
                    .putString("user_name", profile.name)
                    .putString("user_phone", profile.phone)
                    .putString("user_avatar", profile.photoUrl)
                    .apply()
                
                // Auto-upgrade verification if applicable
                if (!profile.isVerified && isCollegeEmail(profile.email)) {
                    val updatedProfile = profile.copy(isVerified = true)
                    usersRef.document(uid).update("isVerified", true)
                    updatedProfile
                } else {
                    profile
                }
            } else {
                val profile = createDefaultProfile(uid, sharedPrefs, fallbackEmail, fallbackName, fallbackPhone, fallbackPhotoUrl)
                saveUserProfile(profile)
                profile
            }
        } catch (e: Exception) {
            createDefaultProfile(uid, sharedPrefs, fallbackEmail, fallbackName, fallbackPhone, fallbackPhotoUrl)
        }
    }

    fun saveUserProfile(profile: UserProfile) {
        try {
            val updates = mapOf(
                "name" to profile.name,
                "email" to profile.email,
                "phone" to profile.phone,
                "photoUrl" to profile.photoUrl,
                "phoneVisibility" to profile.phoneVisibility,
                "notificationsEnabled" to profile.notificationsEnabled,
                "rideHistoryVisible" to profile.rideHistoryVisible
            )
            usersRef.document(profile.uid).set(updates, com.google.firebase.firestore.SetOptions.merge())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateUserFields(uid: String, updates: Map<String, Any>) {
        val syncRelatedData = {
            val batch = db.batch()
            
            val hostName = updates["name"]
            val hostPhoto = updates["photoUrl"]
            
            if (hostName != null || hostPhoto != null) {
                db.collection("rides").whereEqualTo("hostId", uid).get().addOnSuccessListener { ridesSnap ->
                    for (doc in ridesSnap.documents) {
                        val rideUpdates = mutableMapOf<String, Any>()
                        hostName?.let { rideUpdates["hostName"] = it }
                        hostPhoto?.let { rideUpdates["hostPhoto"] = it }
                        batch.update(doc.reference, rideUpdates)
                    }
                    
                    db.collection("requests").whereEqualTo("passengerId", uid).get().addOnSuccessListener { reqsSnap ->
                        for (doc in reqsSnap.documents) {
                            val reqUpdates = mutableMapOf<String, Any>()
                            hostName?.let { reqUpdates["passengerName"] = it }
                            hostPhoto?.let { reqUpdates["passengerPhoto"] = it }
                            batch.update(doc.reference, reqUpdates)
                        }
                        
                        batch.commit()
                    }
                }
            }
        }

        try {
            usersRef.document(uid).update(updates).addOnSuccessListener {
                syncRelatedData()
            }.addOnFailureListener {
                // If update fails (e.g. document doesn't exist), fallback to set with merge
                usersRef.document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge()).addOnSuccessListener {
                    syncRelatedData()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun submitUserRating(
        targetUid: String,
        newRatingVal: Double,
        currentRating: Double,
        currentCount: Int,
        onComplete: (Double, Int) -> Unit = { _, _ -> }
    ) {
        val newCount = currentCount + 1
        val calculatedRating = ((currentRating * currentCount) + newRatingVal) / newCount
        val roundedRating = Math.round(calculatedRating * 10.0) / 10.0

        val updates = mapOf(
            "rating" to roundedRating,
            "ratingCount" to newCount
        )
        try {
            usersRef.document(targetUid).update(updates)
                .addOnSuccessListener {
                    onComplete(roundedRating, newCount)
                }
                .addOnFailureListener {
                    // Fallback to set merge if update fails
                    usersRef.document(targetUid).set(updates, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            onComplete(roundedRating, newCount)
                        }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDefaultProfile(uid: String, sharedPrefs: SharedPreferences, email: String?, name: String?, phone: String?, photoUrl: String?): UserProfile {
        val savedName = sharedPrefs.getString("user_name", null) ?: name ?: email?.substringBefore('@') ?: "Abhishek Singh"
        val savedPhone = sharedPrefs.getString("user_phone", null) ?: phone ?: ""
        val savedAvatar = sharedPrefs.getString("user_avatar", null) ?: photoUrl ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=$savedName"
        val finalEmail = email ?: "abhisheksingh@gmail.com"
        return UserProfile(
            uid = uid,
            name = savedName,
            email = finalEmail,
            phone = savedPhone,
            photoUrl = savedAvatar,
            isVerified = isCollegeEmail(finalEmail)
        )
    }
}
