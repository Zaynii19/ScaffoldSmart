package com.example.scaffoldsmart.client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scaffoldsmart.util.Security
import com.example.scaffoldsmart.LoginActivity
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.client.client_models.ClientModel
import com.example.scaffoldsmart.databinding.ActivitySignupBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class SignupActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
    private var id: String? = null
    private var name: String? = null
    private var email: String? = null
    private var pass: String? = null
    private var confirmPass: String? = null
    private var userType: String? = null
    private var encryptedPassword: String? = null

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

        binding.signupBtn.setOnClickListener {
            getClientValues()
            signupClient()
        }

        binding.backTxt.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun getClientValues() {
        name = binding.clientName.text.toString()
        if (binding.email.text.toString().contains("@")  || binding.email.text.toString().contains(".com")) {
            email = binding.email.text.toString()
        } else {
            binding.email.error = "Enter a valid email"
            binding.email.setText("")
            email = ""
        }

        val pattern = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}\$".toRegex()
        if (binding.pass.text.toString().matches(pattern)) {
            pass = binding.pass.text.toString()
        } else {
            binding.pass.error = "Password must contain: lowercase letters, numbers, symbols, and be at least 8 characters"
            binding.pass.setText("")
            pass = ""
        }
        encryptedPassword = pass?.let { Security.encrypt(it) }
        if (binding.confrmPass.text.toString() != pass) {
            binding.confrmPass.error = "Confirm password not matched"
            binding.confrmPass.setText("")
            confirmPass = ""
        }else {
            confirmPass = binding.confrmPass.text.toString()
        }
    }

    private fun signupClient() {
        //Checks if fields are empty
        if (binding.clientName.text.toString().isEmpty() ||
            binding.email.text.toString().isEmpty() ||
            binding.pass.text.toString().isEmpty() ||
            binding.confrmPass.text.toString().isEmpty()
        ) {
            Toast.makeText(this@SignupActivity, "Please fill all the details first", Toast.LENGTH_SHORT).show()
        } else{
            binding.loading.visibility = View.VISIBLE
            email?.let { e ->
                pass?.let { p ->
                    Firebase.auth.createUserWithEmailAndPassword(e, p).addOnCompleteListener { task ->
                        binding.loading.visibility = View.GONE
                        if (task.isSuccessful) {
                            val userId = Firebase.auth.currentUser?.uid
                            if (userId != null) {
                                // Create client model with generated Firebase key
                                val clientRef = Firebase.database.reference.child("Client").child(userId)
                                id = clientRef.push().key ?: return@addOnCompleteListener // Safely handle null keys
                                userType = "Client"
                                val client = ClientModel(userType, id, name, e, encryptedPassword)

                                // Store user data in Firebase Database
                                clientRef.setValue(client).addOnCompleteListener { storeTask ->
                                    if (storeTask.isSuccessful) {
                                        Toast.makeText(this, "SignUp Successful", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, ClientMainActivity::class.java))
                                        finish()
                                    } else {
                                        handleError("Failed to save client data: ${storeTask.exception?.localizedMessage}")
                                    }
                                }
                            } else {
                                handleError("Failed to retrieve user ID after signup.")
                            }
                        } else {
                            handleError("SignUp Failed: ${task.exception?.localizedMessage}")
                        }
                    }
                }
            }
        }
    }

    //Handles and displays error messages.
    private fun handleError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}