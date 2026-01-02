package com.dev.su.subahon.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dev.su.subahon.data.model.User
import com.dev.su.subahon.utils.FirebaseUtil
import com.google.firebase.firestore.ListenerRegistration

class ProfileViewModel : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private var listener: ListenerRegistration? = null

    fun startUserListener() {
        val uid = FirebaseUtil.auth.currentUser?.uid ?: return

        listener = FirebaseUtil.firestore
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

    fun stopUserListener() {
        listener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        stopUserListener()
    }
}
