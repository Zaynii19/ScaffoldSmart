package com.example.scaffoldsmart

import android.content.Intent
import android.content.SharedPreferences
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private var userType: String = ""
    private var userEmail: String = ""
    private var userPass: String = ""
    private lateinit var userPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

    private fun loginUser() {
        if (userEmail.isEmpty() || userPass.isEmpty()){
            Toast.makeText(this, "Please fill all the details", Toast.LENGTH_LONG).show()
        }else{
            Firebase.auth.signInWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "SignIn successful", Toast.LENGTH_SHORT).show()
                        when (userType) {
                            "Admin" -> {
                                // Storing alarm status value in shared preferences
                                val editor = userPreferences.edit()
                                editor.putString("USERTYPE", userType)
                                editor.apply()
                                startActivity(Intent(this, AdminMainActivity::class.java))
                                finish()
                            }
                            "Client" -> {
                                // Storing alarm status value in shared preferences
                                val editor = userPreferences.edit()
                                editor.putString("USERTYPE", userType)
                                editor.apply()
                                startActivity(Intent(this, ClientMainActivity::class.java))
                                finish()
                            }
                            else -> {
                                startActivity(Intent(this, ClientMainActivity::class.java))
                                finish()
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