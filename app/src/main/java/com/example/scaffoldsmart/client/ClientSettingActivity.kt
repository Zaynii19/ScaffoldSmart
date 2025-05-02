package com.example.scaffoldsmart.client

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.client.client_fragments.ClientUpdateFragment
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.ActivityClientSettingBinding
import com.example.scaffoldsmart.util.DueDateAlarm
import com.example.scaffoldsmart.util.Encryption
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import androidx.core.net.toUri
import com.example.scaffoldsmart.util.DateFormater
import com.example.scaffoldsmart.util.OverDueFeeAlarm

class ClientSettingActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientSettingBinding.inflate(layoutInflater)
    }
    private lateinit var viewModelC: ClientViewModel
    private var switchOn = false
    private var senderUid: String? = null
    private lateinit var chatPreferences: SharedPreferences
    private var dueRentalList = ArrayList<RentalModel>()
    private var overDueRentalList = ArrayList<RentalModel>()
    private lateinit var viewModelR: RentalViewModel

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

        setStatusBarColor()
        viewModelC = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModelC.retrieveClientData()

        viewModelR = ViewModelProvider(this)[RentalViewModel::class.java]
        viewModelR.retrieveRentalReq()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.accountSettingBtn.setOnClickListener {
            showBottomSheet()
        }

        binding.dueDateSwitch.setOnClickListener {
            dueDateAlertOnOf()
        }

        binding.dueFeeSwitch.setOnClickListener {
            overDueAlertOnOf()
        }
    }

    private fun dueDateAlertOnOf() {
        checkOngoingRentalList { hasOngoingRentals ->
            if (hasOngoingRentals) {
                if (switchOn){
                    binding.dueDateSwitch.setImageResource(R.drawable.switch_off)
                    switchOn = false
                    // Canceling all scheduled alarms
                    dueRentalList.forEach {
                        val dueDateMillis = DateFormater.combineAlarmDateTime(it.endDuration)
                        DueDateAlarm.cancelAlarmsForDueDate(this, dueDateMillis)
                    }
                } else{
                    dueRentalList.forEach {
                        val dueDate = it.endDuration
                        scheduleDueDateAlarms(dueDate)
                        binding.dueDateSwitch.setImageResource(R.drawable.switch_on)
                        switchOn = true
                    }
                }
            } else {
                Toast.makeText(this@ClientSettingActivity, "No ongoing rental found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun overDueAlertOnOf() {
        checkOverdueRentalList { hasOverDueRentals ->
            if (hasOverDueRentals) {
                if (switchOn){
                    binding.dueFeeSwitch.setImageResource(R.drawable.switch_off)
                    switchOn = false
                    // Canceling all scheduled alarms
                    overDueRentalList.forEach {
                        val dueDateMillis = DateFormater.combineAlarmDateTime(it.endDuration)
                        OverDueFeeAlarm.cancelAlarmsForOverDue(this, dueDateMillis)
                    }
                } else{
                    overDueRentalList.forEach {
                        val dueDate = it.endDuration
                        scheduleOverDueAlarms(dueDate)
                        binding.dueFeeSwitch.setImageResource(R.drawable.switch_on)
                        switchOn = true
                    }
                }
            } else {
                Toast.makeText(this@ClientSettingActivity, "No overdue rental found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkOngoingRentalList(callback: (Boolean) -> Unit) {
        viewModelR.observeRentalReqLiveData().observe(this@ClientSettingActivity) { rentals ->
            val filteredRentals = rentals?.filter {
                it.status.isNotEmpty() && it.clientID == senderUid && it.rentStatus == "ongoing"
            } ?: emptyList()

            dueRentalList.clear()
            dueRentalList.addAll(filteredRentals)
            Log.d("ClientSettingActivityDebug", "observeRentalReqLiveData: ${dueRentalList.size}")
            // Invoke the callback
            callback(filteredRentals.isNotEmpty())
        }
    }

    private fun checkOverdueRentalList(callback: (Boolean) -> Unit) {
        viewModelR.observeRentalReqLiveData().observe(this@ClientSettingActivity) { rentals ->
            val filteredRentals = rentals?.filter {
                it.status.isNotEmpty() && it.clientID == senderUid && it.rentStatus == "overdue"
            } ?: emptyList()

            overDueRentalList.clear()
            overDueRentalList.addAll(filteredRentals)
            Log.d("ClientSettingActivityDebug", "observeRentalReqLiveData: ${overDueRentalList.size}")
            // Invoke the callback
            callback(filteredRentals.isNotEmpty())
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = ClientUpdateFragment.newInstance(object : ClientUpdateFragment.OnClientUpdatedListener {
            override fun onClientUpdated(name: String, email: String, pass: String, cnic: String, phone: String, address: String) {
                viewModelC.observeClientLiveData().observe(this@ClientSettingActivity) { client ->
                    if (client != null) {
                        val currentDecryptedPassword = Encryption.decrypt(client.pass)
                        updateClientData(name, email, pass, cnic, address, phone, currentDecryptedPassword)
                    }
                }
            }

            override fun onClientVerified(cnic: String, phone: String, address: String) {}
        }, false)
        bottomSheetDialog.show(this.supportFragmentManager, "Client")
    }

    private fun updateClientData(
        name: String,
        email: String,
        pass: String,
        cnic: String,
        address: String,
        phone: String,
        currentDecryptedPassword: String
    ) {
        val currentUser = Firebase.auth.currentUser

        if (currentUser != null) {
            // Step 1: Re-authenticate the user
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentDecryptedPassword)
            currentUser.reauthenticate(credential)
                .addOnSuccessListener {
                    // Step 2: Verify before updating email
                    currentUser.verifyBeforeUpdateEmail(email)
                        .addOnSuccessListener {

                            showEmailVerificationDialog()

                            // Step 3: Update password
                            currentUser.updatePassword(pass)
                                .addOnSuccessListener {
                                    // Step 4: Update other admin data in Firebase Database
                                    val databaseRef = Firebase.database.reference.child("Client")
                                        .child(currentUser.uid)

                                    val encryptedPassword = Encryption.encrypt(pass)

                                    val updates = hashMapOf<String, Any>(
                                        "name" to name,
                                        "email" to email,
                                        "pass" to encryptedPassword, // Storing encrypted password
                                        "cnic" to cnic,
                                        "address" to address,
                                        "phone" to phone
                                    )

                                    databaseRef.updateChildren(updates)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this@ClientSettingActivity,
                                                "User data updated successfully",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            // Step 5: Reload user to reflect changes
                                            currentUser.reload()
                                                .addOnSuccessListener {
                                                    Log.d("SettingDebug", "updateClientData: User email updated. Changes are now reflected.")
                                                }
                                                .addOnFailureListener { reloadException ->
                                                    Toast.makeText(
                                                        this@ClientSettingActivity,
                                                        "Failed to reload user data: ${reloadException.localizedMessage}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                        .addOnFailureListener { dbException ->
                                            Toast.makeText(
                                                this@ClientSettingActivity,
                                                "Failed to update your data in database: ${dbException.localizedMessage}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { passwordException ->
                                    Toast.makeText(
                                        this@ClientSettingActivity,
                                        "Failed to update password: ${passwordException.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .addOnFailureListener { verificationException ->
                            Toast.makeText(
                                this@ClientSettingActivity,
                                "Failed to send verification email: ${verificationException.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener { reAuthException ->
                    Toast.makeText(
                        this@ClientSettingActivity,
                        "Re-Authentication failed: ${reAuthException.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            Toast.makeText(this@ClientSettingActivity, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEmailVerificationDialog() {
        val builder = MaterialAlertDialogBuilder(this@ClientSettingActivity)
        builder.setTitle("Email Verification")
            .setBackground(ContextCompat.getDrawable(this@ClientSettingActivity, R.drawable.msg_view_received))
            .setMessage("We've sent a verification email to your new email address. Please verify it to complete the update. Once verified, relaunch the app to see changes.")
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }

        val alertDialog = builder.create()
        alertDialog.apply {
            show()
            // Set title text color
            val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(Color.BLACK)
            // Set message text color
            findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
            // Set button color
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
        }
    }

    override fun onResume() {
        super.onResume()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Online"
        presenceMap["lastSeen"] = currentTime
        Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
        checkAlarmPermissionAndSchedule()
    }

    override fun onPause() {
        super.onPause()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Offline"
        presenceMap["lastSeen"] = currentTime
        Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
    }

    private fun scheduleDueDateAlarms(dueDate: String) {
        getAlarmPermission()

        val dueDateMillis = DateFormater.combineAlarmDateTime(dueDate)

        // Proceed with scheduling alarms
        DueDateAlarm.scheduleDueDateAlarms(this, dueDateMillis)
    }

    private fun scheduleOverDueAlarms(dueDate: String) {
        getAlarmPermission()

        val dueDateMillis = DateFormater.combineAlarmDateTime(dueDate)

        // Proceed with scheduling alarms
        OverDueFeeAlarm.scheduleOverDueAlarms(this, dueDateMillis)
    }

    private fun getAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Launch permission request
                val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            }
        }
    }

    private fun checkAlarmPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Alarm permission is required for timely Alert", Toast.LENGTH_LONG).show()
                getAlarmPermission()
                return
            } else {
                // Permission granted, schedule alarms
                dueDateAlertOnOf()
            }
        } else {
            // No permission needed on older versions
            dueDateAlertOnOf()
        }
    }

    companion object {

        fun verifyClient(
            cnic: String,
            address: String,
            phone: String,
            currentDecryptedPassword: String,
            context: Context
        ) {
            val currentUser = Firebase.auth.currentUser

            if (currentUser != null) {
                // Step 1: Re-authenticate the user
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentDecryptedPassword)
                currentUser.reauthenticate(credential)
                    .addOnSuccessListener {
                        // Step 2: Update client data in Firebase Database
                        val databaseRef = Firebase.database.reference.child("Client")
                            .child(currentUser.uid)

                        val updates = hashMapOf<String, Any>(
                            "cnic" to cnic,
                            "address" to address,
                            "phone" to phone
                        )

                        databaseRef.updateChildren(updates)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Client verified successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { dbException ->
                                Toast.makeText(
                                    context,
                                    "Failed to verify client data: ${dbException.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { reAuthException ->
                        Toast.makeText(
                            context,
                            "Re-Authentication failed: ${reAuthException.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }
}