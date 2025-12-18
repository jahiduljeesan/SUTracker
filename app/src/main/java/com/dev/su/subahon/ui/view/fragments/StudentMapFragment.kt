package com.dev.su.subahon.ui.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.pow

class StudentMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentStudentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private val markerMap = mutableMapOf<String, Marker>()
    private val polylines = mutableListOf<Polyline>()

    private val viewModel: StudentMapViewModel by viewModels()
    private var adapter: BusListAdapter? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private var studentLocation: LatLng? = null
    private var isTracking = false   // ← OLD LOGIC

    // ================= LIFECYCLE =================

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.lottieLoading.visibility = View.VISIBLE

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkUserRole()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        viewModel.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLocationUpdates()
        clearMap()
        _binding = null
    }

    // ================= MAP =================

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        enableUserLocation()

        val su = LatLng(23.75252286030257, 90.38705295701178)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(su, 16f))
        addUniversityMarker(su)
    }

    private fun addUniversityMarker(pos: LatLng) {
        map.addMarker(
            MarkerOptions()
                .position(pos)
                .title("Sonargaon University")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.su_logo))
        )
    }

    // ================= LOCATION =================

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            startStudentLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startStudentLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    studentLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    // ================= ROLE UI =================

    private fun checkUserRole() {
        val uid = FirebaseUtil.auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            val role = FirebaseUtil.firestore.collection("users").document(uid).get().await().getString("role")
            if (role == "driver") setupDriverUI() else setupStudentUI()
        }
    }

    private fun setupStudentUI() {
        binding.studentBottomView.visibility = View.VISIBLE
        binding.driverBottomView.visibility = View.GONE

        adapter = BusListAdapter(emptyList()) { phone ->
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone ?: ""}")))
        }

        binding.rvRoutes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StudentMapFragment.adapter
            visibility = View.VISIBLE
        }
    }

    private fun setupDriverUI() {
        binding.driverBottomView.visibility = View.VISIBLE
        binding.studentBottomView.visibility = View.GONE

        binding.btnStart.setOnClickListener {
            if (!isTracking) startDriverTracking() else stopDriverTracking()
        }
    }

    // ================= DRIVER TRACKING =================

    @SuppressLint("MissingPermission")
    private fun startDriverTracking() {
        if (isTracking) return
        isTracking = true

        binding.btnStart.speed = 1f
        binding.btnStart.playAnimation()

        val uid = FirebaseUtil.auth.currentUser?.uid ?: return
        val heading = binding.spinnerHeading.selectedItem.toString()

        FirebaseUtil.firestore.collection("users").document(uid)
            .update("status", "started", "heading", heading)

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    uploadDriverLocation(it.latitude, it.longitude)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    private fun stopDriverTracking() {
        if (!isTracking) return
        isTracking = false

        binding.btnStart.speed = -1f
        binding.btnStart.playAnimation()

        val uid = FirebaseUtil.auth.currentUser?.uid ?: return
        FirebaseUtil.firestore.collection("users").document(uid).update("status", "stopped")

        stopLocationUpdates()
    }

    private fun uploadDriverLocation(lat: Double, lng: Double) {
        val uid = FirebaseUtil.auth.currentUser?.uid ?: return
        FirebaseUtil.firestore.collection("users").document(uid)
            .update("location", GeoPoint(lat, lng), "timestamp", com.google.firebase.Timestamp.now())
    }

    // ================= OBSERVE =================

    private fun observeViewModel() {

        viewModel.buses.observe(viewLifecycleOwner) { buses ->
            if (!::map.isInitialized) return@observe

            binding.lottieLoading.visibility = View.GONE

            adapter?.updateList(buses)
            updateMarkers(buses)
        }

        viewModel.routePolylines.observe(viewLifecycleOwner) {
            if (!::map.isInitialized) return@observe
            drawPolylines(it)
        }
    }

    // ================= MAP UI =================

    private fun updateMarkers(buses: List<BusDisplay>) {
        val activeKeys = buses.map { it.busName }.toSet()

        buses.forEach { bus ->
            val pos = LatLng(bus.locationLat, bus.locationLng)
            val key = bus.busName

            if (markerMap.containsKey(key)) {
                animateMarker(markerMap[key]!!, pos)
            } else {
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(bus.busName)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_icon_su))
                )
                marker?.tag = bus
                marker?.let { markerMap[key] = it }
            }
        }

        markerMap.keys.filter { it !in activeKeys }.forEach {
            markerMap[it]?.remove()
            markerMap.remove(it)
        }

        map.setOnMarkerClickListener {
            (it.tag as? BusDisplay)?.let { bus -> showBusDetailsDialog(bus) }
            true
        }
    }

    private fun drawPolylines(routes: List<List<LatLng>>) {
        polylines.forEach { it.remove() }
        polylines.clear()

        routes.forEach {
            polylines.add(
                map.addPolyline(
                    PolylineOptions().addAll(it).width(10f).color(Color.BLUE)
                )
            )
        }
    }

    private fun clearMap() {
        markerMap.values.forEach { it.remove() }
        markerMap.clear()
        polylines.forEach { it.remove() }
        polylines.clear()
    }

    // ================= DIALOG =================

    private fun showBusDetailsDialog(bus: BusDisplay) {
        val dialog = BusInfoAlertLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        val userLatLng = studentLocation ?: LatLng(23.7525, 90.3870)

        val distance = calculateDistance(bus.locationLat, bus.locationLng, userLatLng.latitude, userLatLng.longitude)
        val eta = ((distance / 20) * 60).toInt()

        dialog.apply {
            tvBusName.text = "🚌 ${bus.routeName}"
            tvDriverName.text = "👨‍✈️ ${bus.busName}"
            tvHeading.text = "⬆️ ${bus.heading}"
            tvDistance.text = "📏 ${"%.2f".format(distance)} km"
            tvArrivalTime.text = if (eta <= 1) "Arriving now" else "Arrives in $eta min"

            btnCall.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${bus.phone}")))
            }
        }

        AlertDialog.Builder(requireContext()).setView(dialog.root).show()
    }

    // ================= UTILS =================

    private fun animateMarker(marker: Marker, to: LatLng) {
        val start = marker.position
        val startTime = System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())
        val duration = 1000L

        handler.post(object : Runnable {
            override fun run() {
                val t = ((System.currentTimeMillis() - startTime).toFloat() / duration).coerceAtMost(1f)
                marker.position = LatLng(
                    start.latitude + (to.latitude - start.latitude) * t,
                    start.longitude + (to.longitude - start.longitude) * t
                )
                if (t < 1f) handler.postDelayed(this, 16)
            }
        })
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2).pow(2.0) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2.0)

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}
