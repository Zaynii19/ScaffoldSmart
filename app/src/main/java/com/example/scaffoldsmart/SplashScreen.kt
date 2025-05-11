package com.example.scaffoldsmart

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scaffoldsmart.admin.AdminMainActivity
import com.example.scaffoldsmart.client.ClientMainActivity
import com.example.scaffoldsmart.databinding.ActivitySplashScreenBinding
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    private val binding by lazy {
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }
    private lateinit var userPreferences: SharedPreferences
    private var userType: String? = null
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

        /* add animation to image */
        val imageAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.welcome_image_animation)
        binding.imageView.startAnimation(imageAnimation)
        val text1Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.welcome_text1_animation)
        binding.imageView2.startAnimation(text1Animation)
        /* add animation to welcome text */
        val text2Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.welcome_text2_animation)
        binding.imageView3.startAnimation(text2Animation)

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

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }
}