package com.dev.su.subahon.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.su.subahon.data.model.User
import com.dev.su.subahon.utils.FirebaseUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel: ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    fun fetchUserData() {
        val uid = FirebaseUtil.auth.currentUser?.uid?:return

        viewModelScope.launch {
            try {
                val snapshot = FirebaseUtil.firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()
                val name = snapshot.getString("name")?:"Unknown"
                val email = snapshot.getString("email")?:"Unknown"

                _user.postValue(User(name,email))
            } catch (e: Exception) {

            }
        }
    }
}