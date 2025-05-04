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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
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
    private var dueSwitch = false
    private var overDueSwitch = false
    private var senderUid: String? = null
    private lateinit var chatPreferences: SharedPreferences
    private lateinit var settingPreferences: SharedPreferences
    private var dueRentalList = ArrayList<RentalModel>()
    private var overDueRentalList = ArrayList<RentalModel>()
    private lateinit var viewModelR: RentalViewModel
    private var permissionRequested = false

    // Register for permission result callback
    private val alarmPermissionRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // This callback will be invoked when user returns from permission request
        checkAndUpdateSwitches()
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

        chatPreferences = getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        settingPreferences = getSharedPreferences("CLIENTSETTINGS", MODE_PRIVATE)
        senderUid = chatPreferences.getString("SenderUid", null)

        // Load saved switch states
        dueSwitch = settingPreferences.getBoolean("DueSwitch", false)
        overDueSwitch = settingPreferences.getBoolean("OverDueSwitch", false)

        // Update UI immediately
        updateSwitchUI()

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
            handleDueDateSwitch()
        }

        binding.dueFeeSwitch.setOnClickListener {
            handleOverDueSwitch()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndUpdateSwitches()

        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Online"
        presenceMap["lastSeen"] = currentTime
        Firebase.database.reference.child("ChatUser").child(senderUid!!).updateChildren(presenceMap)
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

    private fun checkAndUpdateSwitches() {
        val hasPermission = hasAlarmPermission()

        if (!hasPermission && permissionRequested) {
            // User returned from permission request without granting
            Toast.makeText(this, "Alarm permission is required for notifications", Toast.LENGTH_LONG).show()

            // Force switches to off state
            dueSwitch = false
            overDueSwitch = false
            settingPreferences.edit {
                putBoolean("DueSwitch", false)
                putBoolean("OverDueSwitch", false)
            }

            // Cancel any existing alarms
            cancelAllAlarms()
        }

        permissionRequested = false
        updateSwitchUI()
    }

    private fun handleDueDateSwitch() {
        checkOngoingRentalList { hasOngoingRentals ->
            if (hasOngoingRentals) {
                if (hasAlarmPermission()) {
                    toggleDueDateSwitch()
                } else {
                    requestAlarmPermission()
                }
            } else {
                Toast.makeText(this, "No ongoing rental found", Toast.LENGTH_SHORT).show()
                updateSwitchUI()
            }
        }
    }

    private fun handleOverDueSwitch() {
        checkOverdueRentalList { hasOverDueRentals ->
            if (hasOverDueRentals) {
                if (hasAlarmPermission()) {
                    toggleOverDueSwitch()
                } else {
                    requestAlarmPermission()
                }
            } else {
                Toast.makeText(this, "No overdue rental found", Toast.LENGTH_SHORT).show()
                updateSwitchUI()
            }
        }
    }

    private fun toggleDueDateSwitch() {
        dueSwitch = !dueSwitch
        settingPreferences.edit { putBoolean("DueSwitch", dueSwitch) }

        if (dueSwitch) {
            scheduleDueDateAlarms()
            Toast.makeText(this, "Due Date Alert Activated", Toast.LENGTH_SHORT).show()
        } else {
            cancelDueDateAlarms()
            Toast.makeText(this, "Due Date Alert Deactivated", Toast.LENGTH_SHORT).show()
        }
        updateSwitchUI()
    }

    private fun toggleOverDueSwitch() {
        overDueSwitch = !overDueSwitch
        settingPreferences.edit { putBoolean("OverDueSwitch", overDueSwitch) }

        if (overDueSwitch) {
            scheduleOverDueAlarms()
            Toast.makeText(this, "Over Due Alert Activated", Toast.LENGTH_SHORT).show()
        } else {
            cancelOverDueAlarms()
            Toast.makeText(this, "Over Due Alert Deactivated", Toast.LENGTH_SHORT).show()
        }
        updateSwitchUI()
    }

    private fun hasAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // No permission needed on older versions
        }
    }

    private fun requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionRequested = true
            val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = "package:$packageName".toUri()
            }
            alarmPermissionRequest.launch(intent)
        } else {
            // On older versions, just proceed
            if (binding.dueDateSwitch.isPressed) {
                toggleDueDateSwitch()
            } else if (binding.dueFeeSwitch.isPressed) {
                toggleOverDueSwitch()
            }
        }
    }

    private fun updateSwitchUI() {
        binding.dueDateSwitch.setImageResource(
            if (dueSwitch && hasAlarmPermission()) R.drawable.switch_on
            else R.drawable.switch_off
        )
        binding.dueFeeSwitch.setImageResource(
            if (overDueSwitch && hasAlarmPermission()) R.drawable.switch_on
            else R.drawable.switch_off
        )
    }

    private fun cancelAllAlarms() {
        cancelDueDateAlarms()
        cancelOverDueAlarms()
    }

    private fun cancelDueDateAlarms() {
        dueRentalList.forEach {
            val dueDateMillis = DateFormater.combineAlarmDateTime(it.endDuration)
            DueDateAlarm.cancelAlarmsForDueDate(this, dueDateMillis)
        }
    }

    private fun cancelOverDueAlarms() {
        overDueRentalList.forEach {
            val dueDateMillis = DateFormater.combineAlarmDateTime(it.endDuration)
            OverDueFeeAlarm.cancelAlarmsForOverDue(this, dueDateMillis)
        }
    }

    private fun scheduleDueDateAlarms() {
        if (!hasAlarmPermission()) {
            dueSwitch = false
            settingPreferences.edit { putBoolean("DueSwitch", false) }
            updateSwitchUI()
            return
        }

        dueRentalList.forEach {
            val dueDate = it.endDuration
            val dueDateMillis = DateFormater.combineAlarmDateTime(dueDate)
            DueDateAlarm.scheduleDueDateAlarms(this, dueDateMillis)
        }
    }

    private fun scheduleOverDueAlarms() {
        if (!hasAlarmPermission()) {
            overDueSwitch = false
            settingPreferences.edit { putBoolean("OverDueSwitch", false) }
            updateSwitchUI()
            return
        }

        dueRentalList.forEach {
            val dueDate = it.endDuration
            val dueDateMillis = DateFormater.combineAlarmDateTime(dueDate)
            OverDueFeeAlarm.scheduleOverDueAlarms(this, dueDateMillis)
        }
    }

    private fun checkOngoingRentalList(callback: (Boolean) -> Unit) {
        viewModelR.observeRentalReqLiveData().observe(this) { rentals ->
            val filteredRentals = rentals?.filter {
                it.status.isNotEmpty() && it.clientID == senderUid && it.rentStatus == "ongoing"
            } ?: emptyList()

            dueRentalList.clear()
            dueRentalList.addAll(filteredRentals)
            callback(filteredRentals.isNotEmpty())
        }
    }

    private fun checkOverdueRentalList(callback: (Boolean) -> Unit) {
        viewModelR.observeRentalReqLiveData().observe(this) { rentals ->
            val filteredRentals = rentals?.filter {
                it.status.isNotEmpty() && it.clientID == senderUid && it.rentStatus == "overdue"
            } ?: emptyList()

            overDueRentalList.clear()
            overDueRentalList.addAll(filteredRentals)
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