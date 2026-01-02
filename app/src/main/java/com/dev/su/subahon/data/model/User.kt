package com.dev.su.subahon.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class User(
    val name: String = "",
    val email: String = "",
    val phone: String = "",

    val role: String = "",
    val routeId: String = "",
    val studentId: String = "",

    val heading: String = "",
    val status: String = "",

    val isAdmin : Boolean = false,

    val location: GeoPoint? = null,
    val timestamp: Timestamp? = null
)
