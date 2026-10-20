package com.ruko.system.domain.model.enums

import com.ruko.system.data.repository.FirestoreCollections

enum class AccountType(val collectionName: String) {
    USER(FirestoreCollections.CUSTOMERS),
    OWNER(FirestoreCollections.STORES)
}
