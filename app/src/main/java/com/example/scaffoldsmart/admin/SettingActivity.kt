package com.example.scaffoldsmart.admin

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
import com.example.scaffoldsmart.util.EncryptionUtil
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.ActivitySettingBinding
import com.example.scaffoldsmart.admin.admin_fragments.AdminUpdateFragment
import com.example.scaffoldsmart.admin.admin_fragments.HomeFragment
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class SettingActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySettingBinding.inflate(layoutInflater)
    }
    private lateinit var viewModel: AdminViewModel
    private var currentDecryptedPassword: String = ""

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
        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModel.retrieveAdminData()

        binding.accountSettingBtn.setOnClickListener {
            showBottomSheet()
        }

    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = AdminUpdateFragment.newInstance(object : AdminUpdateFragment.OnAdminUpdatedListener {
            override fun onAdminUpdated(name: String, email: String, pass: String, company: String, phone: String, address: String) {
                updateAdminData(name, email, pass, company, address, phone)
            }
        })
        bottomSheetDialog.show(this.supportFragmentManager, "Admin")
    }

    private fun updateAdminData(
        name: String,
        email: String,
        pass: String,
        company: String,
        address: String,
        phone: String
    ) {
        val currentUser = Firebase.auth.currentUser

        viewModel.observeAdminLiveData().observe(this) { admin ->
            if (admin != null) {
                currentDecryptedPassword = EncryptionUtil.decrypt(admin.pass)
            }
        }

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
                                    val databaseRef = Firebase.database.reference.child("Admin")
                                        .child(currentUser.uid)

                                    val encryptedPassword = EncryptionUtil.encrypt(pass)

                                    val updates = hashMapOf<String, Any>(
                                        "name" to name,
                                        "email" to email,
                                        "pass" to encryptedPassword, // Storing encrypted password
                                        "company" to company,
                                        "address" to address,
                                        "phone" to phone
                                    )

                                    databaseRef.updateChildren(updates)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this@SettingActivity,
                                                "Admin's data updated successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Step 5: Reload user to reflect changes
                                            currentUser.reload()
                                                .addOnSuccessListener {
                                                    Log.d("SettingDebug", "updateAdminData: User email updated. Changes are now reflected.")
                                                }
                                                .addOnFailureListener { reloadException ->
                                                    Toast.makeText(
                                                        this@SettingActivity,
                                                        "Failed to reload user data: ${reloadException.localizedMessage}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                        .addOnFailureListener { dbException ->
                                            Toast.makeText(
                                                this@SettingActivity,
                                                "Failed to update Admin's data in database: ${dbException.localizedMessage}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { passwordException ->
                                    Toast.makeText(
                                        this@SettingActivity,
                                        "Failed to update password: ${passwordException.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { verificationException ->
                            Toast.makeText(
                                this@SettingActivity,
                                "Failed to send verification email: ${verificationException.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { reauthException ->
                    Toast.makeText(
                        this@SettingActivity,
                        "Re-Authentication failed: ${reauthException.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(this@SettingActivity, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEmailVerificationDialog() {
        val builder = MaterialAlertDialogBuilder(this@SettingActivity)
        builder.setTitle("Email Verification")
            .setBackground(ContextCompat.getDrawable(this@SettingActivity, R.drawable.curved_msg_view_client))
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
}