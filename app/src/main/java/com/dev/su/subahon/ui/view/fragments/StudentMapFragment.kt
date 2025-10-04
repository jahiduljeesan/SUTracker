package com.dev.su.subahon.ui.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dev.su.subahon.R
import com.dev.su.subahon.data.model.BusDisplay
import com.dev.su.subahon.databinding.BusInfoAlertLayoutBinding
import com.dev.su.subahon.databinding.FragmentStudentMapBinding
import com.dev.su.subahon.ui.adapter.BusListAdapter
import com.dev.su.subahon.ui.viewmodel.StudentMapViewModel
import com.dev.su.subahon.utils.FirebaseUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.pow

class StudentMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentStudentMapBinding
    private lateinit var map: GoogleMap
    private val polylines = mutableListOf<Polyline>()
    private val markerMap = mutableMapOf<String, Marker>()

    private var viewModel: StudentMapViewModel by viewModels()
    private lateinit var adapter: BusListAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private var studentLocation: LatLng? = null
    private var isTracking = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lottieLoading.visibility = View.VISIBLE

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkUserRoleAndLoadUI()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        enableUserLocation()

        map.uiSettings.isZoomControlsEnabled = true
        val su = LatLng(23.75252286030257, 90.38705295701178)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(su, 17f))
        addUniversityMarker(su)
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                studentLocation = LatLng(location.latitude, location.longitude)
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    private fun addUniversityMarker(position: LatLng) {
        val image = BitmapDescriptorFactory.fromResource(R.drawable.su_logo)
        map.addMarker(MarkerOptions().position(position).title("Sonargaon University").icon(image))
    }

    private suspend fun getUserRole(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            val snapshot = FirebaseUtil.firestore.collection("users").document(userId).get().await()
            snapshot.getString("role")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkUserRoleAndLoadUI() {
        val userId = FirebaseUtil.auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            when (getUserRole(userId)) {
                "student" -> setupStudentUI()
                "driver" -> setupDriverUI()
            }
            observeViewModel() // OBSERVE BOTH student and driver
        }
    }

    private fun setupStudentUI() {
        binding.studentBottomView.visibility = View.VISIBLE
        binding.driverBottomView.visibility = View.GONE
        binding.rvRoutes.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupDriverUI() {
        binding.driverBottomView.visibility = View.VISIBLE
        binding.studentBottomView.visibility = View.GONE

        var isStarted = false
        binding.btnStart.setOnClickListener {
            if (!isStarted) {
                startDriverTracking()
                isStarted = true
                binding.btnStart.speed = 1f
                binding.btnStart.playAnimation()
            } else {
                stopDriverTracking()
                isStarted = false
                binding.btnStart.speed = -1f
                binding.btnStart.playAnimation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDriverTracking() {
        if (isTracking) return
        isTracking = true

        val selectedHeading = binding.spinnerHeading.selectedItem.toString()
        val userId = FirebaseUtil.auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            try {
                FirebaseUtil.firestore.collection("users").document(userId)
                    .update(mapOf("status" to "started", "heading" to selectedHeading))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                uploadDriverLocation(loc.latitude, loc.longitude)
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
    }

    //for git hub
    private fun stopDriverTracking() {
        if (!isTracking) return
        isTracking = false

        val userId = FirebaseUtil.auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            try {
                FirebaseUtil.firestore.collection("users").document(userId).update("status", "stopped")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        stopLocationUpdates() // STOP UPDATES IMMEDIATELY
    }

    private fun uploadDriverLocation(lat: Double, lng: Double) {
        val userId = FirebaseUtil.auth.currentUser?.uid ?: return
        val data = mapOf("location" to GeoPoint(lat, lng), "timestamp" to com.google.firebase.Timestamp.now())
        lifecycleScope.launch {
            try {
                FirebaseUtil.firestore.collection("users").document(userId).update(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.buses.observe(viewLifecycleOwner) { buses ->
            updateMarkers(buses)
            binding.lottieLoading.visibility = View.GONE
            if (::adapter.isInitialized.not()) {
                adapter = BusListAdapter(buses){
                    val callIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${it ?: ""}")
                    }
                    startActivity(callIntent)
                }
                binding.rvRoutes.adapter = adapter
            } else {
                adapter.updateList(buses) // You need to implement this in your adapter
            }
        }
        viewModel.routePolylines.observe(viewLifecycleOwner) { polylines ->
            drawPolylines(polylines)
        }
    }

    private fun updateMarkers(buses: List<BusDisplay>) {
        val activeKeys = buses.map { it.busName }.toSet()
        buses.forEach { bus ->
            val position = LatLng(bus.locationLat, bus.locationLng)
            val key = bus.busName
            val icon = BitmapDescriptorFactory.fromResource(R.drawable.bus_icon_su)

            if (markerMap.containsKey(key)) {
                animateMarker(markerMap[key]!!, position)
            } else {
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(bus.busName)
                        .icon(icon)
                        .snippet("Route: ${bus.routeName}\nHeading: ${bus.heading}\nPhone: ${bus.phone}")
                )
                marker?.tag = bus
                if (marker != null) markerMap[key] = marker
            }
        }

        map.setOnMarkerClickListener { marker ->
            val busData = marker.tag as? BusDisplay
            busData?.let { showBusDetailsDialog(it) }
            true
        }

        markerMap.keys.filter { it !in activeKeys }.forEach {
            markerMap[it]?.remove()
            markerMap.remove(it)
        }
    }

    private fun showBusDetailsDialog(bus: BusDisplay) {
        val dialogBinding = BusInfoAlertLayoutBinding.inflate(LayoutInflater.from(requireContext()))

        dialogBinding.apply {
            tvBusName.text = "🚌 ${bus.routeName ?: "Unknown"}"
            tvRouteName.text = "📍 ${bus.routeName ?: "Unknown Route"}"
            tvDriverName.text = "👨‍✈️ ${bus.busName ?: "Unknown Driver"}"
            tvHeading.text = "⬆️ Heading: ${bus.heading ?: "Unknown"}"

            // Calculate and display ETA and distance
            val studentLatLng = studentLocation ?: LatLng(23.75252286030257, 90.38705295701178)
            val distanceKm = calculateDistance(bus.locationLat, bus.locationLng, studentLatLng.latitude, studentLatLng.longitude)
            val etaMinutes = ((distanceKm / 20.0) * 60).toInt() // assume 20 km/h speed
            val etaText = if (etaMinutes < 1) "Arriving now" else "Arrives in $etaMinutes min"
            tvArrivalTime.text = "⏱ $etaText"
            tvDistance.text = "📏 ${"%.2f".format(distanceKm)} km away"

            // Call driver
            btnCall.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${bus.phone ?: ""}")
                }
                startActivity(callIntent)
            }
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        alertDialog.show()
    }


    private fun animateMarker(marker: Marker, toPosition: LatLng) {
        val handler = Handler(Looper.getMainLooper())
        val start = System.currentTimeMillis()
        val duration = 1000L
        val startLatLng = marker.position

        val interpolator = android.view.animation.LinearInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - start
                val t = (elapsed.toFloat() / duration).coerceAtMost(1f)
                val lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude
                val lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude
                marker.position = LatLng(lat, lng)
                if (t < 1f) handler.postDelayed(this, 16)
            }
        })
    }

    private fun drawPolylines(routePolylines: List<List<LatLng>>) {
        polylines.forEach { it.remove() }
        polylines.clear()
        routePolylines.forEach { points ->
            val polyline = map.addPolyline(
                PolylineOptions().addAll(points).color(Color.BLUE).width(10f)
            )
            polylines.add(polyline)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLocationUpdates()
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2).pow(2.0) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2.0)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c // Distance in kilometers
    }

}
