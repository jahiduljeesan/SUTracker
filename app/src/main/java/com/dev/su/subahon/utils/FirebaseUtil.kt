package com.dev.su.subahon.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtil {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
}