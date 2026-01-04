package com.dev.su.subahon.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.su.subahon.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProfileViewModel : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var listener: ListenerRegistration? = null

    fun startUserListener() {
        val uid = auth.currentUser?.uid ?: return

        listener = firestore
            .collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _user.postValue(null)
                    return@addSnapshotListener
                }

                snapshot?.toObject(User::class.java)?.let {
                    _user.postValue(it)
                }
            }
    }

    fun setUser(email: String,todo :(DocumentSnapshot) -> Task<Void>) {
        val usersRef = firestore.collection("users")

        usersRef
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    println("No user found with email: $email")
                    return@addOnSuccessListener
                }

                val doc = snap.documents.first()
                todo(doc)

            }
            .addOnFailureListener { e ->
                println("Query failed: ${e.message}")
            }
    }

    fun stopUserListener() {
        listener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        stopUserListener()
    }
}
