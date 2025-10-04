package com.dev.su.subahon.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.su.subahon.data.model.BusDisplay
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StudentMapViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _buses = MutableLiveData<List<BusDisplay>>()
    val buses: LiveData<List<BusDisplay>> = _buses

    private val _routePolylines = MutableLiveData<List<List<LatLng>>>()
    val routePolylines: LiveData<List<List<LatLng>>> = _routePolylines

    init {
        fetchActiveBuses()
    }

    fun fetchActiveBuses() {
        firestore.collection("users")
            .whereEqualTo("status", "started") // ensure case consistency
            .whereEqualTo("role", "driver")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    error?.printStackTrace()
                    return@addSnapshotListener
                }

                viewModelScope.launch(Dispatchers.IO) {
                    val busList = mutableListOf<BusDisplay>()
                    val polylineList = mutableListOf<List<LatLng>>()

                    Log.d("polylineTestp", "Snapshot size: ${snapshot.size()}")

                    snapshot.documents.forEach { doc ->
                        val busName = doc.getString("name") ?: "Unknown"
                        val phone = doc.getString("phone") ?: "N/A"
                        val routeId = doc.getString("routeId") ?: ""
                        val heading = doc.getString("heading") ?: "N/A"
                        val location = doc.getGeoPoint("location")
                        val lat = location?.latitude ?: 0.0
                        val lng = location?.longitude ?: 0.0

                        Log.d("polylineTest", "Bus: $busName")
                        Log.d("polylineTesty", "Phone: $phone")

                        if (routeId.isNotEmpty()) {
                            try {
                                val routeDoc = firestore.collection("routes")
                                    .document(routeId)
                                    .get()
                                    .await()

                                val routeName = routeDoc.getString("routeName") ?: "Unknown Route"
                                val polylinePoints = routeDoc["polylinePoints"] as? List<Map<String, Double>>
                                val points = polylinePoints?.map {
                                    LatLng(it["lat"] ?: 0.0, it["lng"] ?: 0.0)
                                } ?: emptyList()

                                busList.add(BusDisplay(busName, routeName, heading, phone, lat, lng))

                                if (points.isNotEmpty()) {
                                    polylineList.add(points)
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                // fallback if route fetch fails
                                busList.add(BusDisplay(busName, "Unknown Route", heading, phone, lat, lng))
                            }
                        } else {
                            // No routeId, add bus with unknown route
                            busList.add(BusDisplay(busName, "Unknown Route", heading, phone, lat, lng))
                        }
                    }

                    Log.d("polylineTestvm", "Buses fetched: ${busList.size}")

                    _buses.postValue(busList)
                    _routePolylines.postValue(polylineList)
                }
            }
    }


}
