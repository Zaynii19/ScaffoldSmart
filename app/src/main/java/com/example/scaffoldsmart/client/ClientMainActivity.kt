package com.example.scaffoldsmart.client

import android.content.Intent
import android.content.SharedPreferences
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
import com.example.scaffoldsmart.client.client_service.ClientMessageListenerService
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.database.database

class ClientMainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientMainBinding.inflate(layoutInflater)
    }
    private lateinit var onesignal: OnesignalService
    private var senderUid: String? = null
    private lateinit var chatPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chatPreferences = getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        senderUid = chatPreferences.getString("SenderUid", null)
        Log.d("ClientMainDebug", "onCreate: senderUid = $senderUid")

        setStatusBarColor()
        setBottomNav()
        onesignal = OnesignalService(this@ClientMainActivity)
        onesignal.initializeOneSignal()

        getMessageNoti()
    }

    override fun onResume() {
        super.onResume()
        senderUid = chatPreferences.getString("SenderUid", null)
        if (senderUid == null) {
            Log.e("ClientMainDebug", "senderUid is null - Redirecting to login")
        } else {
            val currentTime = System.currentTimeMillis()
            val presenceMap = HashMap<String, Any>()
            presenceMap["status"] = "Online"
            presenceMap["lastSeen"] = currentTime
            Firebase.database.reference.child("ChatUser").child(senderUid!!).setValue(presenceMap)
        }
    }

    override fun onPause() {
        super.onPause()
        senderUid = chatPreferences.getString("SenderUid", null)
        if (senderUid != null) {
            val currentTime = System.currentTimeMillis()
            val presenceMap = HashMap<String, Any>()
            presenceMap["status"] = "Offline"
            presenceMap["lastSeen"] = currentTime
            Firebase.database.reference.child("ChatUser").child(senderUid!!).setValue(presenceMap)
        }
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

    private fun getMessageNoti() {
        val intent = Intent(this, ClientMessageListenerService::class.java)
        intent.putExtra("SenderUid", senderUid)
        startService(intent)
    }
}