package com.dev.su.subahon.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.su.subahon.data.model.BusDisplay
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StudentMapViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private val _buses = MutableLiveData<List<BusDisplay>>()
    val buses: LiveData<List<BusDisplay>> = _buses

    private val _routePolylines = MutableLiveData<List<List<LatLng>>>()
    val routePolylines: LiveData<List<List<LatLng>>> = _routePolylines

    fun startListening() {
        if (listenerRegistration != null) return  // prevent duplicate listeners

        listenerRegistration = firestore.collection("users")
            .whereEqualTo("status", "started")
            .whereEqualTo("role", "driver")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                viewModelScope.launch(Dispatchers.IO) {
                    val busList = mutableListOf<BusDisplay>()
                    val polylineList = mutableListOf<List<LatLng>>()

                    snapshot.documents.forEach { doc ->
                        val busName = doc.getString("name") ?: "Unknown"
                        val phone = doc.getString("phone") ?: "N/A"
                        val routeId = doc.getString("routeId") ?: ""
                        val heading = doc.getString("heading") ?: "N/A"
                        val location = doc.getGeoPoint("location")

                        val lat = location?.latitude ?: return@forEach
                        val lng = location.longitude

                        var routeName = "Unknown Route"
                        var points: List<LatLng> = emptyList()

                        if (routeId.isNotEmpty()) {
                            try {
                                val routeDoc = firestore.collection("routes")
                                    .document(routeId)
                                    .get()
                                    .await()

                                routeName = routeDoc.getString("routeName") ?: routeName
                                val polylinePoints =
                                    routeDoc["polylinePoints"] as? List<Map<String, Double>>

                                points = polylinePoints?.map {
                                    LatLng(it["lat"]!!, it["lng"]!!)
                                } ?: emptyList()
                            } catch (_: Exception) {}
                        }

                        busList.add(
                            BusDisplay(busName, routeName, heading, phone, lat, lng)
                        )

                        if (points.isNotEmpty()) polylineList.add(points)
                    }

                    _buses.postValue(busList)
                    _routePolylines.postValue(polylineList)
                }
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
