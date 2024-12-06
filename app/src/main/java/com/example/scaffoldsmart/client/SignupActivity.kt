package com.example.scaffoldsmart.client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scaffoldsmart.LoginActivity
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
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
            Log.d("SignupDebug", "FirstName: $firstName, LastName: $lastName, Pass: $pass, Email: $email, ConfirmPass: $confirmPass")
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
}