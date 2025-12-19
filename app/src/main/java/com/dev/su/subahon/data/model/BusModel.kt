package com.dev.su.subahon.data.model

data class BusModel(
    var id: String = "",
    var busNo: String = "",
    var tripType: String = "",
    var phone: String = "",
    var driver: Driver = Driver(),
    var stops: List<String> = emptyList(),
    var schedule: Map<String, List<String>> = emptyMap(),
    var expanded: Boolean = false // UI only
)
