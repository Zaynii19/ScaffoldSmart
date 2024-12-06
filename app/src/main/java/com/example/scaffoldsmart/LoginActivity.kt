package com.example.scaffoldsmart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scaffoldsmart.admin.AdminMainActivity
import com.example.scaffoldsmart.client.ClientMainActivity
import com.example.scaffoldsmart.client.SignupActivity
import com.example.scaffoldsmart.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private var userType: String = ""
    private var userEmail: String = ""
    private var userPass: String = ""

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
        onUserSelection()

        binding.loginBtn.setOnClickListener {
            getUserValues()
            Log.d("LoginDebug", "UserEmail: $userEmail UserPass: $userPass")
            when (userType) {
                "Admin" -> {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                    finish()
                }
                "Client" -> {
                    startActivity(Intent(this, ClientMainActivity::class.java))
                    finish()
                }
                else -> {
                    startActivity(Intent(this, ClientMainActivity::class.java))
                    finish()
                }
            }
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
        userEmail = binding.email.text.toString()
        userPass = binding.pass.text.toString()
    }
}