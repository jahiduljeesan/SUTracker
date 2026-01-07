package com.dev.su.subahon.ui.view.activity

import android.app.AlertDialog
import android.content.IntentFilter
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dev.su.subahon.R
import com.dev.su.subahon.databinding.ActivityMainBinding
import com.dev.su.subahon.databinding.NoInternetAlertBinding
import com.dev.su.subahon.utils.FirebaseUtil
import com.dev.su.subahon.utils.NetworkChangeReceiver
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var networkDialog: AlertDialog? = null
    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showNotificationRationaleDialog()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseUtil.auth
        FirebaseUtil.firestore

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _,destination,_ ->
            if(destination.id == R.id.fragDevInfo
                || destination.id == R.id.fragAdmin) {
                binding.bottomNav.visibility = View.GONE
            } else {
                binding.bottomNav.visibility = View.VISIBLE
            }
        }

        setupNetworkMonitoring()

    }

    private fun setupNetworkMonitoring() {
        networkChangeReceiver = NetworkChangeReceiver(
            onNetworkLost = { showNoInternetDialog() },
            onNetworkAvailable = { dismissNoInternetDialog() }
        )

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver,filter)
    }
    private fun dismissNoInternetDialog() {
        networkDialog?.dismiss()
    }

    private fun showNoInternetDialog() {
        if (networkDialog?.isShowing == true) return

        val binding = NoInternetAlertBinding.inflate(LayoutInflater.from(this))
        networkDialog = AlertDialog.Builder(this)
            .setView(binding.root)
            .setCancelable(false)
            .create()
        networkDialog?.window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@MainActivity,R.color.color_transparent)))
        networkDialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }

    fun showNotificationRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications")
            .setMessage(
                "We use notifications to:\n" +
                        "• Send bus arrival updates\n" +
                        "• Notify route changes and delays\n" +
                        "• Share important announcements\n\n" +
                        "You can turn them off anytime in Settings."
            )
            .setNegativeButton("Not now") { d, _ -> d.dismiss() }
            .setPositiveButton("Allow") { d, _ ->
                d.dismiss()
                // This will show Android notification permission dialog (Android 13+)
                CoroutineScope(Dispatchers.IO).launch {
                    OneSignal.Notifications.requestPermission(true)
                }
            }
            .show()
    }

}