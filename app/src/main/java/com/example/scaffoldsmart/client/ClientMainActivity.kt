package com.example.scaffoldsmart.client

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.ImageProcessingActivity
import com.example.scaffoldsmart.databinding.ActivityClientMainBinding
import com.example.scaffoldsmart.client.client_service.ClientMessageListenerService
import com.example.scaffoldsmart.client.client_viewmodel.ClientViewModel
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
    private lateinit var viewModel: ClientViewModel
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //cleanupImageCache()

        viewModel = ViewModelProvider(this)[ClientViewModel::class.java]
        viewModel.retrieveClientData()
        observeClientLiveData()

        chatPreferences = getSharedPreferences("CHATCLIENT", MODE_PRIVATE)

        setStatusBarColor()
        setBottomNav()
        onesignal = OnesignalService(this@ClientMainActivity)
        onesignal.initializeOneSignal()

        getMessageNoti()

        setupPermissionLaunchers()
        setupCameraLauncher()
        setupGalleryLauncher()

        binding.countPipes.setOnClickListener {
            showImageOptionDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        senderUid = chatPreferences.getString("SenderUid", null)
        Log.d("ClientMainDebug", "senderUid: $senderUid")
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

    private fun observeClientLiveData() {
        viewModel.observeClientLiveData().observe(this) { client ->
            if (client != null) {
                chatPreferences.edit { putString("SenderUid", client.id) }
            }
        }
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
        binder.camera.setOnClickListener {
            requestCameraPermission()
            dialog.dismiss()
        }
        binder.gallery.setOnClickListener {
            requestStoragePermission()
            dialog.dismiss()
        }
    }

    private fun setupPermissionLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                handlePermissionDenied("Camera")
            }
        }

        storagePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isGranted = permissions.any { it.value }
            if (isGranted) {
                launchGallery()
            } else {
                handlePermissionDenied("Storage")
            }
        }
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                launchImageProcessingActivity(it)
            }
        }
    }

    private fun setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val imageBitmap = try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                imageBitmap?.let {
                    launchImageProcessingActivity(it)
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

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this@ClientMainActivity, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestStoragePermission() {
        val permissionsToRequest = getStoragePermissions()

        // Check if any permission is already granted
        val shouldRequest = permissionsToRequest.any { permission ->
            ContextCompat.checkSelfPermission(this@ClientMainActivity, permission)!= PackageManager.PERMISSION_GRANTED
        }

        if (!shouldRequest) {
            launchGallery()
        } else {
            storagePermissionLauncher.launch(permissionsToRequest)
        }
    }

    private fun getStoragePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                arrayOf(
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun handlePermissionDenied(permissionType: String) {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // User denied before, show explanation
            showRationaleDialog(permissionType)
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
            showRationaleDialog(permissionType)
        } else {
            // User permanently denied or first time with "Don't ask again"
            Toast.makeText(this@ClientMainActivity, "$permissionType permission is required", Toast.LENGTH_SHORT).show()
            openAppSettings()
        }
    }

    private fun showRationaleDialog(permissionType: String) {
        MaterialAlertDialogBuilder(this@ClientMainActivity)
            .setTitle("Permission Needed")
            .setMessage("$permissionType permission is required to use update profile")
            .setBackground(ContextCompat.getDrawable(this@ClientMainActivity, R.drawable.msg_view_received))
            .setPositiveButton("Grant") { _, _ ->
                when (permissionType) {
                    "Camera" -> requestCameraPermission()
                    "Storage" -> requestStoragePermission()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun launchCamera() {
        cameraLauncher.launch(null)
    }

    private fun launchGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}