package com.example.scaffoldsmart

import android.content.Intent
import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scaffoldsmart.admin.AdminMainActivity
import com.example.scaffoldsmart.client.ClientMainActivity
import com.example.scaffoldsmart.client.SignupActivity
import com.example.scaffoldsmart.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import androidx.core.content.edit
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private var userType: String? = null
    private var userEmail: String? = null
    private var userPass: String? = null
    private lateinit var userPreferences: SharedPreferences
    private lateinit var notiPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupPermissionLaunchers()
        requestNotificationPermission()

        userPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE)
        setStatusBarColor()
        onUserSelection()

        binding.loginBtn.setOnClickListener {
            getUserValues()
            loginUser()
        }

        binding.createAccountBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun onUserSelection() {
        userType = binding.clientBtn.text.toString()
        binding.adminBtn.setOnClickListener {
            userType = binding.adminBtn.text.toString()
            // Set the background tint using a color resource
            binding.adminBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttons_color)
            binding.clientBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.dark_gray)
            binding.notHaveAccountTxt.visibility = View.GONE
            binding.createAccountBtn.visibility = View.GONE
        }

        binding.clientBtn.setOnClickListener {
            userType = binding.clientBtn.text.toString()
            // Set the background tint using a color resource
            binding.clientBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.buttons_color)
            binding.adminBtn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.dark_gray)
            binding.notHaveAccountTxt.visibility = View.VISIBLE
            binding.createAccountBtn.visibility = View.VISIBLE
        }
    }

    private fun getUserValues() {
        if (SignupActivity.isEmailValid(binding.email.text.toString())) {
            userEmail = binding.email.text.toString()
        } else {
            binding.email.error = "Enter a valid email"
            binding.email.setText("")
            userEmail = ""
        }

        if (SignupActivity.isPasswordValid(binding.pass.text.toString())) {
            userPass = binding.pass.text.toString()
        } else {
            binding.pass.error = "Password must contain: at least one lowercase letter, one uppercase letter, one number, and one special character, and be at least 8 characters"
            binding.pass.setText("")
            userPass = ""
        }
    }

    private fun loginUser() {
        if (userEmail.isNullOrEmpty() || userPass.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill all the details", Toast.LENGTH_LONG).show()
        } else {
            binding.loading.visibility = View.VISIBLE
            userEmail?.let { uEmail ->
                userPass?.let { uPass ->
                    Firebase.auth.signInWithEmailAndPassword(uEmail, uPass)
                        .addOnCompleteListener { task ->
                            binding.loading.visibility = View.GONE
                            if (task.isSuccessful) {
                                // Sign in success, now get the user role
                                val userId = Firebase.auth.currentUser?.uid
                                if (userId != null) {
                                    // Check both Admin and Client nodes
                                    val database = FirebaseDatabase.getInstance()
                                    val adminRef = database.getReference("Admin/$userId")
                                    val clientRef = database.getReference("Client/$userId")
                                    // Check Admin node
                                    adminRef.child("userType").get().addOnSuccessListener { snapshot ->
                                        if (snapshot.exists()) {
                                            val actualUserType = snapshot.getValue(String::class.java)
                                            userType?.let { handleUserType(actualUserType, it) }
                                        } else {
                                            // Check Client node if Admin not found
                                            clientRef.child("userType").get().addOnSuccessListener { clientSnapshot ->
                                                if (clientSnapshot.exists()) {
                                                    val actualUserType = clientSnapshot.getValue(String::class.java)
                                                    userType?.let { handleUserType(actualUserType, it) }
                                                } else {
                                                    Toast.makeText(this, "User type not found.", Toast.LENGTH_SHORT).show()
                                                }
                                            }.addOnFailureListener {
                                                Toast.makeText(this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "Failed to retrieve user role.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(this, "SignIn Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }

    // Handle user type comparison and navigation
    private fun handleUserType(actualUserType: String?, expectedUserType: String) {
        if (actualUserType == expectedUserType) {
            Toast.makeText(this, "SignIn successful", Toast.LENGTH_SHORT).show()
            // Store user type in SharedPreferences
            userPreferences.edit { putString("USERTYPE", expectedUserType) }

            // Navigate to the correct dashboard
            when (expectedUserType) {
                "Admin" -> startActivity(Intent(this, AdminMainActivity::class.java))
                "Client" -> startActivity(Intent(this, ClientMainActivity::class.java))
            }
            finish()
        } else {
            // Role mismatch, sign out and show error
            Firebase.auth.signOut()
            Toast.makeText(this, "Please log in as the correct role.", Toast.LENGTH_SHORT).show()
            Log.d("LoginDebug", "Actual User: $actualUserType, Login as: $expectedUserType")
        }
    }

    private fun setupPermissionLaunchers() {
        notiPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                handlePermissionDenied()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this@LoginActivity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notiPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun handlePermissionDenied() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            // User denied before, show explanation
            showRationaleDialog()
        } else {
            // User permanently denied or first time with "Don't ask again"
            Toast.makeText(this@LoginActivity, "Notification permission is required", Toast.LENGTH_SHORT).show()
            openAppSettings()
        }
    }

    private fun showRationaleDialog() {
        MaterialAlertDialogBuilder(this@LoginActivity)
            .setTitle("Permission Needed")
            .setMessage("Notification permission is required for app functionality")
            .setBackground(ContextCompat.getDrawable(this@LoginActivity, R.drawable.msg_view_received))
            .setPositiveButton("Grant") { _, _ ->
                requestNotificationPermission()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}