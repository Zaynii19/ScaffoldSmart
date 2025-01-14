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
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.client.client_fragments.ClientInventoryFragment
import com.example.scaffoldsmart.databinding.ActivityMainAdminBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.database.database
import org.json.JSONObject

class AdminMainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainAdminBinding.inflate(layoutInflater)
    }

    private lateinit var onesignal: OnesignalService
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
        onesignal = OnesignalService(this@AdminMainActivity)
        onesignal.initializeOneSignal(this@AdminMainActivity)

        // Get rental notification data first start
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

    override fun onResume() {
        super.onResume()
        // Get rental notification data on when noti clicks
        val notificationId = ClientInventoryFragment.notificationId
        onesignal.getOneSignalNoti(notificationId, prevNotiCompletedAt)
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

    companion object {
        fun handleReqData(reqData: JSONObject) {
            reqData.let { data ->
                val clientID = data.optString("clientID", "N/A")
                val clientName = data.optString("clientName", "N/A")
                val rentalAddress = data.optString("rentalAddress", "N/A")
                val clientEmail = data.optString("clientEmail", "N/A")
                val clientPhone = data.optString("clientPhone", "N/A")
                val clientCnic = data.optString("clientCnic", "N/A")
                val startDuration = data.optString("startDuration", "N/A")
                val endDuration = data.optString("endDuration", "N/A")
                val pipes = data.optString("pipes", "N/A")
                val pipesLength = data.optString("pipesLength", "N/A")
                val joints = data.optString("joints", "N/A")
                val wench = data.optString("wench", "N/A")
                val pumps = data.optString("pumps", "N/A")
                val motors = data.optString("motors", "N/A")
                val generators = data.optString("generators", "N/A")
                val wheel = data.optString("wheel", "N/A")

                storeRentalReq(clientID, clientName, rentalAddress, clientEmail, clientPhone, clientCnic, startDuration, endDuration, pipes, pipesLength, joints, wench, pumps, motors, generators, wheel)

            }
        }

        private fun storeRentalReq(
            clientID: String,
            clientName: String,
            rentalAddress: String,
            clientEmail: String,
            clientPhone: String,
            clientCnic: String,
            startDuration: String,
            endDuration: String,
            pipes: String,
            pipesLength: String,
            joints: String,
            wench: String,
            pumps: String,
            motors: String,
            generators: String,
            wheel: String
        ) {
            // Reference to the inventory in Firebase
            val databaseRef = Firebase.database.reference.child("Rentals")
            val newItemRef = databaseRef.push()
            val rentalId = newItemRef.key // Get the generated key

            if (rentalId != null) {
                // Create new rental request model
                val newReq = RentalModel(clientID, rentalId, clientName, clientEmail, rentalAddress, clientCnic, clientPhone,
                    startDuration, endDuration, pipes, pipesLength, joints, wench, motors, pumps, generators, wheel,"","","")

                // Store the new request in Firebase
                newItemRef.setValue(newReq)
                    .addOnSuccessListener {
                        Log.d("AdminMainDebug", "Rental data stored successfully")
                    }
                    .addOnFailureListener {
                        Log.e("AdminMainDebug", "Failed to stored rental data: ${it.message}")
                    }
            } else {
                Log.d("AdminMainDebug", "Failed to generate request ID")
            }
        }
    }
}