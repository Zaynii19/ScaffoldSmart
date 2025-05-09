package com.example.scaffoldsmart.client

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.ImageProcessingActivity
import com.example.scaffoldsmart.databinding.ActivityClientMainBinding
import com.example.scaffoldsmart.client.client_service.ClientMessageListenerService
import com.example.scaffoldsmart.databinding.ChooseImgDialogBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.io.ByteArrayOutputStream

class ClientMainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityClientMainBinding.inflate(layoutInflater)
    }
    private lateinit var onesignal: OnesignalService
    private var senderUid: String? = null
    private lateinit var chatPreferences: SharedPreferences
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var currentImageBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chatPreferences = getSharedPreferences("CHATCLIENT", MODE_PRIVATE)
        senderUid = chatPreferences.getString("SenderUid", null)

        setStatusBarColor()
        setBottomNav()
        onesignal = OnesignalService(this@ClientMainActivity)
        onesignal.initializeOneSignal()

        getMessageNoti()

        setupCameraLauncher()
        setupGalleryLauncher()

        binding.countPipes.setOnClickListener {
            showImageOptionDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        senderUid = chatPreferences.getString("SenderUid", null)
        if (senderUid == null) {
            Log.e("ClientMainDebug", "senderUid is null - Redirecting to login")
        } else {
            val currentTime = System.currentTimeMillis()
            val presenceMap = HashMap<String, Any>()
            presenceMap["status"] = "Online"
            presenceMap["lastSeen"] = currentTime
            senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
        }
    }

    override fun onPause() {
        super.onPause()
        senderUid = chatPreferences.getString("SenderUid", null)
        if (senderUid != null) {
            val currentTime = System.currentTimeMillis()
            val presenceMap = HashMap<String, Any>()
            presenceMap["status"] = "Offline"
            presenceMap["lastSeen"] = currentTime
            senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setBottomNav() {
        val navController = findNavController(R.id.fragmentViewClient)
        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_bottomClient)
        bottomNav.setupWithNavController(navController)
    }

    private fun getMessageNoti() {
        val intent = Intent(this, ClientMessageListenerService::class.java)
        intent.putExtra("SenderUid", senderUid)
        startService(intent)
    }

    private fun showImageOptionDialog() {
        val customDialog = LayoutInflater.from(this@ClientMainActivity).inflate(R.layout.choose_img_dialog, null)
        val binder: ChooseImgDialogBinding = ChooseImgDialogBinding.bind(customDialog)
        val dialog = MaterialAlertDialogBuilder(this@ClientMainActivity)
            .setTitle("Take Scaffolding Pipes Image")
            .setView(customDialog)
            .setBackground(ContextCompat.getDrawable(this@ClientMainActivity, R.drawable.msg_view_received))
            .create().apply {
                show()
                // Set title text color
                val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
            }

        binder.cancel.setOnClickListener { dialog.dismiss() }
        binder.camera.setOnClickListener {requestPermissionsAndLaunchCamera(dialog)}
        binder.gallery.setOnClickListener {requestPermissionsAndLaunchGallery(dialog)}
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    currentImageBitmap = it
                    launchImageProcessingActivity(it)
                }
            }
        }
    }

    private fun setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        // Simple bitmap loading without complex sampling
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        currentImageBitmap = bitmap
                        launchImageProcessingActivity(bitmap)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                        Log.e("GalleryError", "Error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun launchImageProcessingActivity(bitmap: Bitmap) {
        try {
            // Compress the bitmap to reasonable quality
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArray = stream.toByteArray()

            val intent = Intent(this, ImageProcessingActivity::class.java).apply {
                putExtra("image_data", byteArray)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermissionsAndLaunchCamera(dialog: AlertDialog) {
        if (checkCameraPermission()) {
            launchCamera()
            dialog.dismiss()
        } else {
            requestCameraPermission()
            dialog.dismiss()
        }
    }

    private fun requestPermissionsAndLaunchGallery(dialog: AlertDialog) {
        if (checkStoragePermission()) {
            launchGallery()
            dialog.dismiss()
        } else {
            requestStoragePermission()
            dialog.dismiss()
        }
    }

    private fun launchCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                cameraLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun launchGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { pickImageIntent ->
            pickImageIntent.type = "image/*"
            galleryLauncher.launch(pickImageIntent)
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                when {
                    permissions[0] == Manifest.permission.CAMERA -> launchCamera()
                    permissions[0] == Manifest.permission.READ_MEDIA_IMAGES ||
                            permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE -> launchGallery()
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                openSettings()
            }
        }
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
    }
}