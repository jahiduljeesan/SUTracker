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
import com.dev.su.subahon.utils.NetworkChangeReceiver

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var networkDialog: AlertDialog? = null
    private lateinit var networkChangeReceiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)

//        val message = """
//                 Md. Farhan Hossain
//            Junior Mobile App Developer
//                    BdCalling It
//        """.trimIndent()
//
//        val dialog = AlertDialog.Builder(this)
//            .setTitle("This app is property of ")
//            .setMessage(message)
//            .create()
//        dialog.show()

        navController.addOnDestinationChangedListener { _,destination,_ ->
            if(destination.id == R.id.fragDevInfo ) {
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
}