package com.ruko.system.domain.model

import com.ruko.system.data.repository.FirestoreCollections

enum class AccountType(val collectionName: String) {
    CUSTOMER(FirestoreCollections.CUSTOMERS),
    STORE(FirestoreCollections.STORES)
}
