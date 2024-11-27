package com.example.scaffoldsmart.admin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.ActivitySettingBinding
import com.example.scaffoldsmart.admin.admin_fragments.AdminUpdateFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySettingBinding.inflate(layoutInflater)
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

        binding.accountSettingBtn.setOnClickListener {
            showBottomSheet()
        }
    }

    private fun showBottomSheet() {
        val bottomSheetDialog: BottomSheetDialogFragment = AdminUpdateFragment.newInstance(object : AdminUpdateFragment.OnAdminUpdatedListener {
            override fun onAdminUpdated() {
                TODO("Not yet implemented")
            }
        })
        bottomSheetDialog.show(this.supportFragmentManager, "Client")
    }
}