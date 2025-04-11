package com.example.scaffoldsmart.admin

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
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.ActivityMainAdminBinding
import com.example.scaffoldsmart.admin.admin_service.AdminMessageListenerService
import com.example.scaffoldsmart.admin.admin_service.LowInventoryAlertService
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import org.json.JSONObject

class AdminMainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainAdminBinding.inflate(layoutInflater)
    }

    private lateinit var onesignal: OnesignalService
    private var prevNotiCompletedAt: String = ""
    private var prevNotificationId: String = ""
    private lateinit var reqPreferences: SharedPreferences
    private lateinit var chatPreferences: SharedPreferences
    private var senderUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chatPreferences = getSharedPreferences("CHATADMIN", MODE_PRIVATE)
        reqPreferences = getSharedPreferences("RENTALREQ", MODE_PRIVATE)
        prevNotiCompletedAt = reqPreferences.getString("CompletedAt", "")!!
        prevNotificationId = reqPreferences.getString("NotificationId", "")!!
        senderUid = chatPreferences.getString("SenderUid", null)

        setStatusBarColor()
        setBottomNav()
        onesignal = OnesignalService(this@AdminMainActivity)
        onesignal.initializeOneSignal()

        // Get rental notification data first start
        onesignal.getOneSignalNoti(prevNotiCompletedAt, prevNotificationId)

        getMessageNoti()

        getLowInventoryAlert()

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
        prevNotiCompletedAt = reqPreferences.getString("CompletedAt", "")!!
        prevNotificationId = reqPreferences.getString("NotificationId", "")!!
        onesignal.getOneSignalNoti(prevNotiCompletedAt, prevNotificationId)

        senderUid = chatPreferences.getString("SenderUid", null)
        if (senderUid == null) {
            Log.e("AdminMainDebug", "senderUid is null - Redirecting to login")
        } else {
            val currentTime = System.currentTimeMillis()
            val presenceMap = HashMap<String, Any>()
            presenceMap["status"] = "Online"
            presenceMap["lastSeen"] = currentTime
            Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
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
            Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
        }
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

    private fun getMessageNoti() {
        val intent = Intent(this, AdminMessageListenerService::class.java)
        intent.putExtra("SenderUid", senderUid)
        startService(intent)
    }

    private fun getLowInventoryAlert() {
        val intent = Intent(this, LowInventoryAlertService::class.java)
        startService(intent)
    }

    companion object {
        fun handleReqData(reqData: JSONObject, requestId: String) {
            reqData.let { data ->
                val clientID = data.optString("clientID", "N/A")
                val clientName = data.optString("clientName", "N/A")
                val clientAddress = data.optString("clientAddress", "N/A")
                val clientEmail = data.optString("clientEmail", "N/A")
                val clientPhone = data.optString("clientPhone", "N/A")
                val clientCnic = data.optString("clientCnic", "N/A")
                val rentalAddress = data.optString("rentalAddress", "N/A")
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
                val totalRent = data.optString("rent", "N/A")

                storeRentalReq(clientID, requestId, clientName, clientAddress, clientEmail, clientPhone,
                    clientCnic, rentalAddress, startDuration, endDuration, pipes.toInt(), pipesLength.toInt(),
                    joints.toInt(), wench.toInt(), pumps.toInt(), motors.toInt(), generators.toInt(),
                    wheel.toInt(), totalRent.toInt())
            }
        }

        private fun storeRentalReq(
            clientID: String,
            requestId: String,
            clientName: String,
            clientAddress: String,
            clientEmail: String,
            clientPhone: String,
            clientCnic: String,
            rentalAddress: String,
            startDuration: String,
            endDuration: String,
            pipes: Int,
            pipesLength: Int,
            joints: Int,
            wench: Int,
            pumps: Int,
            motors: Int,
            generators: Int,
            wheel: Int,
            totalRent: Int
        ) {
            val databaseRef = Firebase.database.reference.child("Rentals")

            // Check if rental with this requestId already exists
            databaseRef.child(requestId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d("AdminMainDebug", "Rental request with ID $requestId already exists")
                    } else {
                        // Create new rental request model
                        val newReq = RentalModel(
                            clientID, requestId, clientName, clientEmail, clientCnic,
                            clientPhone, clientAddress, rentalAddress, startDuration,
                            endDuration, pipes, pipesLength, joints, wench, motors,
                            pumps, generators, wheel, totalRent
                        )

                        // Store with requestId as the key
                        databaseRef.child(requestId).setValue(newReq)
                            .addOnSuccessListener {
                                Log.d("AdminMainDebug", "Rental data stored successfully with ID $requestId")
                            }
                            .addOnFailureListener {
                                Log.e("AdminMainDebug", "Failed to store rental data: ${it.message}")
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("AdminMainDebug", "Database query cancelled: ${databaseError.message}")
                }
            })
        }
    }
}