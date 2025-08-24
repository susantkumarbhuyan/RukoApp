package com.ruko.system.domain.model

import com.ruko.system.domain.model.enums.AccountStatus
import dev.gitlive.firebase.firestore.GeoPoint
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class BusinessAccount @OptIn(ExperimentalTime::class) constructor(
    val id: String = "",
    val ownerName: String = "",
    val businessName: String = "",
    val businessType: String = "",
    val phone: String = "",
    val address: String = "",
    val logoUrl: String? = null,
    val openingHours: Map<String, String>? = null,
    val servicesOffered: List<String> = emptyList(),
//    val avgServiceTime: Long = 600000, // 10 min default
//    val maxQueueSize: Int? = null,
    val location: GeoPoint? = null,
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val isVerified: Boolean = false,
    val registeredAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val fcmToken: String? = null,
    val language: String? = null,
    val timezone: String? = null,
    val status: AccountStatus = AccountStatus.ACTIVE
)

/*
* 🔹 Common Fields (for both Customer & BusinessAccount)
	•	createdAt: Long → timestamp when the account was created.
	•	updatedAt: Long → last profile update.
	•	profileImageUrl: String? → optional profile picture/logo.
	•	status: String → e.g., "active", "inactive", "banned".
	•	fcmToken: String? → Firebase Cloud Messaging token for push notifications.
	•	language: String? → preferred app language.
	•	timezone: String? → useful for queue reminders.

⸻

🔹 Extra for Customers
	•	dateOfBirth: String? → for birthday offers or age-based services.
	•	gender: String? → optional, but useful for targeted services.
	•	loyaltyPoints: Int = 0 → for reward systems later.
	•	lastActiveAt: Long? → last login time (analytics/engagement).
	•	defaultQueueNotifications: Boolean → whether they want reminders.

⸻

🔹 Extra for BusinessAccount
	•	logoUrl: String? → business logo.
	•	openingHours: Map<String, String> → e.g., { "Mon-Fri": "9AM-9PM", "Sat": "10AM-6PM" }.
	•	servicesOffered: List<String> → e.g., ["Haircut", "Shaving"] or ["Doctor Consultation"].
	•	avgServiceTime: Long = 600000 → default service time in ms (10 min).
	•	maxQueueSize: Int? → optional, limit queue length.
	•	location: GeoPoint (lat/lng) → for maps and nearby searches.
	•	rating: Double → average rating.
	•	reviewsCount: Int → number of reviews.
* */