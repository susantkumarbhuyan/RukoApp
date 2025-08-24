package com.ruko.system.domain.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class AccountStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    DELETED
}

/*
* 🔹 Why these values?
	•	ACTIVE → normal, can use the app.
	•	INACTIVE → voluntarily deactivated, or onboarding not finished.
	•	SUSPENDED → temporarily blocked (fraud, violation, spam).
	•	DELETED → soft-deleted, account not accessible but kept for history.
* */