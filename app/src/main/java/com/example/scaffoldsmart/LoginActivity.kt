package com.example.scaffoldsmart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scaffoldsmart.admin.AdminMainActivity
import com.example.scaffoldsmart.databinding.ActivityLoginBinding
import com.google.android.material.bottomsheet.BottomSheetDialog


class LoginActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Define the options for the Spinner
        val userTypeOptions = listOf("Admin", "Client")

        // Create an ArrayAdapter using the string array and a default Spinner layout
        val lengthAdapter = ArrayAdapter(this@LoginActivity, R.layout.spinner_item, userTypeOptions)

        // Specify the layout to use when the list of choices appears
        lengthAdapter.setDropDownViewResource(R.layout.spinner_item)

        // Apply the adapter to the Spinner
        binding.userType.adapter = lengthAdapter

        val userType = binding.userType.selectedItem.toString()

        binding.createAccountBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, AdminMainActivity::class.java))
            finish()
        }
    }
}