package com.example.scaffoldsmart.client

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
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
import com.example.scaffoldsmart.client.client_fragments.ClientUpdateFragment
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
import com.example.scaffoldsmart.databinding.ActivityClientSettingBinding
import com.example.scaffoldsmart.util.Encryption
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class ClientSettingActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientSettingBinding.inflate(layoutInflater)
    }
    private lateinit var viewModel: ClientViewModel
    //private var currentDecryptedPassword: String = ""
    private var switch = false
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

        setStatusBarColor()
        viewModel = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModel.retrieveClientData()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.accountSettingBtn.setOnClickListener {
            showBottomSheet()
        }

        binding.dueDateSwitch.setOnClickListener {
            if (switch){
                binding.dueDateSwitch.setImageResource(R.drawable.switch_off)
                switch = false
            }else{
                binding.dueDateSwitch.setImageResource(R.drawable.switch_on)
                switch = true
            }
        }

        binding.dueFeeSwitch.setOnClickListener {
            if (switch){
                binding.dueFeeSwitch.setImageResource(R.drawable.switch_off)
                switch = false
            }else{
                binding.dueFeeSwitch.setImageResource(R.drawable.switch_on)
                switch = true
            }
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = ClientUpdateFragment.newInstance(object : ClientUpdateFragment.OnClientUpdatedListener {
            override fun onClientUpdated(name: String, email: String, pass: String, cnic: String, phone: String, address: String) {
                viewModel.observeClientLiveData().observe(this@ClientSettingActivity) { client ->
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