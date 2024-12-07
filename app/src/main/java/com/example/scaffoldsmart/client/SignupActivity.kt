package com.example.scaffoldsmart.client

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
    private var id: String = ""
    private var firstName: String = ""
    private var lastName: String = ""
    private var email: String = ""
    private var pass: String = ""
    private var confirmPass: String = ""

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
        firstName = binding.firstName.text.toString()
        lastName = binding.lastName.text.toString()
        email = binding.email.text.toString()
        pass = binding.pass.text.toString()
        if (binding.confrmPass.text.toString() != pass) {
            Toast.makeText(this@SignupActivity, "Confirm password not matched", Toast.LENGTH_SHORT).show()
            binding.confrmPass.setText("")
            confirmPass = ""
        }else {
            confirmPass = binding.confrmPass.text.toString()
        }
    }

    private fun signupClient() {
        //Checks if fields are empty or not
        if (binding.firstName.text.toString() == "" ||
            binding.lastName.text.toString() == "" ||
            binding.email.text.toString() == "" ||
            binding.pass.text.toString() == "" ||
            binding.confrmPass.text.toString() == ""
        )
        {
            Toast.makeText(this@SignupActivity, "Please fill all the details first", Toast.LENGTH_SHORT).show()
        }else{
            Firebase.auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = Firebase.auth.currentUser?.uid
                    if (userId != null) {
                        // Create client model with generated Firebase key
                        val clientRef = Firebase.database.reference.child("Client").child(userId)
                        id = clientRef.push().key ?: return@addOnCompleteListener // Safely handle null keys
                        val client = ClientModel(id, firstName, lastName, email, pass)

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

    //Handles and displays error messages.
    private fun handleError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}