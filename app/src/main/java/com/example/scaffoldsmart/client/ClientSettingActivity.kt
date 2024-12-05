package com.example.scaffoldsmart.client

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_fragments.AdminUpdateFragment
import com.example.scaffoldsmart.client.client_fragments.ClientUpdateFragment
import com.example.scaffoldsmart.databinding.ActivityClientSettingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClientSettingActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientSettingBinding.inflate(layoutInflater)
    }
    private var switch = false
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

        binding.accountSettingBtn.setOnClickListener {
            showBottomSheet()
        }

        binding.dueDateSwitch.setOnClickListener {
            if (switch){
                binding.dueDateSwitch.setImageResource(R.drawable.switch_off)
                switch = false
            }else{
                binding.dueDateSwitch.setImageResource(R.drawable.switch_on)
                switch = true
            }
        }

        binding.dueFeeSwitch.setOnClickListener {
            if (switch){
                binding.dueFeeSwitch.setImageResource(R.drawable.switch_off)
                switch = false
            }else{
                binding.dueFeeSwitch.setImageResource(R.drawable.switch_on)
                switch = true
            }
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = ClientUpdateFragment.newInstance(object : ClientUpdateFragment.OnClientUpdatedListener {
            override fun onClientUpdated() {
                TODO("Not yet implemented")
            }
        })
        bottomSheetDialog.show(this.supportFragmentManager, "Client")
    }
}