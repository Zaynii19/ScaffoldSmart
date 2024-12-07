package com.example.scaffoldsmart

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Handler
import android.os.Looper
import androidx.core.view.WindowCompat
import com.example.scaffoldsmart.admin.AdminMainActivity
import com.example.scaffoldsmart.client.ClientMainActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    private lateinit var userPreferences: SharedPreferences
    private var userType: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setStatusBarColor()

        // User is signed in, determine role and redirect
        userPreferences = getSharedPreferences("LOGIN", MODE_PRIVATE)
        userType = userPreferences.getString("USERTYPE", "Admin")!!

        // Check if user is already signed in
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        Handler(Looper.getMainLooper()).postDelayed({
            if (user != null) {
                if (userType == "Admin") {
                    // Redirect to admin home screen
                    val intent = Intent(this, AdminMainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else if (userType == "Client") {
                    // Redirect to client home screen
                    val intent = Intent(this, ClientMainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                // User is not signed in
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        },3000)
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }
}