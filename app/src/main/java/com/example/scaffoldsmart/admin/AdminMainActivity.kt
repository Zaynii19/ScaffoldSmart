package com.example.scaffoldsmart.admin

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
import com.example.scaffoldsmart.client.client_fragments.ClientInventoryFragment
import com.example.scaffoldsmart.databinding.ActivityMainAdminBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminMainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainAdminBinding.inflate(layoutInflater)
    }

    // NOTE: Replace the below with your own ONESIGNAL_APP_ID
    private val ONESIGNAL_APP_ID = "4c3f5def-07a5-46b9-9fbb-f8336f1dfa8a"

    //private var currentNotiCompletedAt = ""
    private var prevNotiCompletedAt = ""
    private lateinit var reqPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        reqPreferences = getSharedPreferences("RENTALREQ", MODE_PRIVATE)
        prevNotiCompletedAt = reqPreferences.getString("CompletedAt", "")!!

        setStatusBarColor()
        setBottomNav()

        // Initialize OneSignal
        val onesignal = OnesignalService(this@AdminMainActivity)
        val tags: Map<String, String> = mapOf("role" to "Admin")
        onesignal.initializeOneSignal(this@AdminMainActivity, tags)

        val notificationId = ClientInventoryFragment.notificationId
        onesignal.getOneSignalNoti(notificationId, prevNotiCompletedAt)

        // Get the FCM device token
        /*FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("AdminMainDebug", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Log.d("AdminMainDebug", "Admin Token: $token")
        })*/

        // Subscribe to the FCM topic
        /*FirebaseMessaging.getInstance().subscribeToTopic("admin_topic")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AdminMainDebug", "Subscription successful", task.exception)
                } else{
                    Log.e("AdminMainDebug", "Subscription failed", task.exception)
                }
            }*/
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setBottomNav() {
        val navController = findNavController(R.id.fragmentView)
        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_bottom)
        bottomNav.setupWithNavController(navController)
    }
}