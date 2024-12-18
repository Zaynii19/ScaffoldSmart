package com.example.scaffoldsmart.client

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.ActivityClientMainBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClientMainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setStatusBarColor()
        setBottomNav()

        // Initialize OneSignal
        val tags: Map<String, String> = mapOf("role" to "Client")
        val onesignal = OnesignalService(this@ClientMainActivity)
        onesignal.initializeOneSignal(this@ClientMainActivity, tags)

        // Initialize Python
        // "context" must be an Activity, Service or Application object from your app.
       /* if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }*/
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setBottomNav() {
        val navController = findNavController(R.id.fragmentViewClient)
        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_bottomClient)
        bottomNav.setupWithNavController(navController)
    }
}