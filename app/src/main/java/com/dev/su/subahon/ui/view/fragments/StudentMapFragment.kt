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

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStudentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lottieLoading.visibility = View.VISIBLE

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    private fun addUniversityMarker(position: LatLng) {
        val image = BitmapDescriptorFactory.fromResource(R.drawable.su_logo)
        map.addMarker(MarkerOptions().position(position).title("Sonargaon University").icon(image))
    }
}
