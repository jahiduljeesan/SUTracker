package com.dev.su.subahon.data.model

data class BusModel(
    val busNo: String,
    val tripType: String,
    val driverName: String,
    val driverPhone: String,
    val stops: List<String>,
    val schedule: Map<String, List<String>>,
    var isExpanded: Boolean = false
)


