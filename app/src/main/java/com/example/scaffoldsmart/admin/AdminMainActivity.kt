package com.example.scaffoldsmart.admin

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
import com.example.scaffoldsmart.ImageProcessingActivity
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalItem
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.ActivityMainAdminBinding
import com.example.scaffoldsmart.admin.admin_service.AdminMessageListenerService
import com.example.scaffoldsmart.admin.admin_service.LowInventoryAlertService
import com.example.scaffoldsmart.admin.admin_viewmodel.AdminViewModel
import com.example.scaffoldsmart.databinding.ChooseImgDialogBinding
import com.example.scaffoldsmart.util.OnesignalService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminMainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainAdminBinding.inflate(layoutInflater)
    }

    private lateinit var onesignal: OnesignalService
    private var prevNotiCompletedAt: String? = null
    private var prevNotificationId: String? = null
    private lateinit var reqPreferences: SharedPreferences
    private lateinit var chatPreferences: SharedPreferences
    private lateinit var viewModel: AdminViewModel
    private var senderUid: String? = null
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<Array<String>>
    //private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var galleryLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private var imageUri: Uri? = null

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

        chatPreferences = getSharedPreferences("CHATADMIN", MODE_PRIVATE)

        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        viewModel.retrieveAdminData()
        observeAdminLiveData()

        chatPreferences = getSharedPreferences("CHATADMIN", MODE_PRIVATE)
        reqPreferences = getSharedPreferences("RENTALREQ", MODE_PRIVATE)
        prevNotiCompletedAt = reqPreferences.getString("CompletedAt", null)
        prevNotificationId = reqPreferences.getString("NotificationId", null)
        senderUid = chatPreferences.getString("SenderUid", null)

        setStatusBarColor()
        setBottomNav()
        onesignal = OnesignalService(this@AdminMainActivity)
        onesignal.initializeOneSignal()

        // Get rental notification data first start
        onesignal.getOneSignalNoti(prevNotiCompletedAt, prevNotificationId)

        getMessageNoti()

        getLowInventoryAlert()

        setupPermissionLaunchers()
        setupCameraLauncher()
        setupGalleryLauncher()

        binding.countPipes.setOnClickListener {
            showImageOptionDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Get rental notification data on when noti clicks
        prevNotiCompletedAt = reqPreferences.getString("CompletedAt", null)
        prevNotificationId = reqPreferences.getString("NotificationId", null)
        onesignal.getOneSignalNoti(prevNotiCompletedAt, prevNotificationId)

        senderUid = chatPreferences.getString("SenderUid", null)
        if (senderUid == null) {
            Log.e("AdminMainDebug", "senderUid is null - Redirecting to login")
        } else {
            val currentTime = System.currentTimeMillis()
            val presenceMap = HashMap<String, Any>()
            presenceMap["status"] = "Online"
            presenceMap["lastSeen"] = currentTime
            senderUid?.let { it -> Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
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
            senderUid?.let { it -> Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
        }
    }

    private fun setStatusBarColor() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    private fun setBottomNav() {
        val navController = findNavController(R.id.fragmentView)
        val bottomNav = findViewById<BottomNavigationView>(R.id.nav_bottom)
        bottomNav.setupWithNavController(navController)
    }

    private fun observeAdminLiveData() {
        viewModel.observeAdminLiveData().observe(this) { admin ->
            if (admin != null) {
                chatPreferences.edit { putString("SenderUid", admin.id) }
            }
        }
    }

    private fun getMessageNoti() {
        val intent = Intent(this, AdminMessageListenerService::class.java)
        intent.putExtra("SenderUid", senderUid)
        startService(intent)
    }

    private fun getLowInventoryAlert() {
        val intent = Intent(this, LowInventoryAlertService::class.java)
        startService(intent)
    }

    private fun showImageOptionDialog() {
        val customDialog = LayoutInflater.from(this@AdminMainActivity).inflate(R.layout.choose_img_dialog, null)
        val binder: ChooseImgDialogBinding = ChooseImgDialogBinding.bind(customDialog)
        val dialog = MaterialAlertDialogBuilder(this@AdminMainActivity)
            .setTitle("Take Scaffolding Pipes Image")
            .setView(customDialog)
            .setBackground(ContextCompat.getDrawable(this@AdminMainActivity, R.drawable.msg_view_received))
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
        /*cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // Load the image from the saved file
                imageUri?.let { uri ->
                    val bitmap = contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                    bitmap?.let { launchImageProcessingActivity(it) }
                }
            }
        }*/
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
        if (ContextCompat.checkSelfPermission(this@AdminMainActivity, Manifest.permission.CAMERA)
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
            ContextCompat.checkSelfPermission(this@AdminMainActivity, permission)!= PackageManager.PERMISSION_GRANTED
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
            Toast.makeText(this@AdminMainActivity, "$permissionType permission is required", Toast.LENGTH_SHORT).show()
            openAppSettings()
        }
    }

    private fun showRationaleDialog(permissionType: String) {
        MaterialAlertDialogBuilder(this@AdminMainActivity)
            .setTitle("Permission Needed")
            .setMessage("$permissionType permission is required to use update profile")
            .setBackground(ContextCompat.getDrawable(this@AdminMainActivity, R.drawable.msg_view_received))
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
        /*val photoFile = createImageFile()
        imageUri = FileProvider.getUriForFile(
            this@AdminMainActivity, "${packageName}.provider", photoFile
        )
        imageUri?.let { uri ->
            cameraLauncher.launch(uri)
        }*/
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

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = cacheDir // Use app's cache directory
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun cleanupImageCache() {
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("JPEG_") && file.name.endsWith(".jpg")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e("AdminMainDebug", "Error cleaning cache: ${e.message}")
        }
    }

    companion object {
        fun handleReqData(reqData: JSONObject, requestId: String) {
            reqData.let { data ->
                val clientID = data.optString("clientID", "N/A")
                val clientName = data.optString("clientName", "N/A")
                val clientAddress = data.optString("clientAddress", "N/A")
                val clientEmail = data.optString("clientEmail", "N/A")
                val clientPhone = data.optString("clientPhone", "N/A")
                val clientCnic = data.optString("clientCnic", "N/A")
                val rentalAddress = data.optString("rentalAddress", "N/A")
                val startDuration = data.optString("startDuration", "N/A")
                val endDuration = data.optString("endDuration", "N/A")
                val totalRent = data.optInt("rent", 0)

                // Get the itemList from JSON reqData
                val itemList = arrayListOf<RentalItem>()
                val itemsArray = data.optJSONArray("itemList")

                itemsArray?.let { array ->
                    for (i in 0 until array.length()) {
                        val itemObj = array.getJSONObject(i)
                        val rentalItem = RentalItem(
                            itemName = itemObj.optString("itemName", "N/A"),
                            itemQuantity = itemObj.optInt("itemQuantity", 0),
                            itemPrice = itemObj.optInt("itemPrice", 0),
                            pipeLength = itemObj.optInt("pipeLength", 0)
                        )
                        itemList.add(rentalItem)
                    }
                }

                storeRentalReq(clientID, requestId, clientName, clientAddress, clientEmail, clientPhone,
                    clientCnic, rentalAddress, startDuration, endDuration, totalRent, itemList)
            }
        }

        private fun storeRentalReq(
            clientID: String?,
            requestId: String?,
            clientName: String?,
            clientAddress: String?,
            clientEmail: String?,
            clientPhone: String?,
            clientCnic: String?,
            rentalAddress: String?,
            startDuration: String?,
            endDuration: String?,
            totalRent: Int?,
            itemList: ArrayList<RentalItem>
        ) {
            // Check if rental with this requestId already exists
            requestId?.let { Firebase.database.reference.child("Rentals").child(it)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.d("AdminMainDebug", "Rental request with ID $requestId already exists")
                        } else {
                            // Create new rental request model
                            val newReq = RentalModel(
                                clientID, requestId, clientName, clientEmail, clientCnic,
                                clientPhone, clientAddress, rentalAddress, startDuration,
                                endDuration, "", totalRent, "", itemList
                            )

                            // Store with requestId as the key
                            Firebase.database.reference.child("Rentals").child(requestId).setValue(newReq)
                                .addOnSuccessListener {
                                    Log.d("AdminMainDebug", "Rental data stored successfully with ID $requestId")
                                }
                                .addOnFailureListener { failure ->
                                    Log.e("AdminMainDebug", "Failed to store rental data: ${failure.message}")
                                }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("AdminMainDebug", "Database query cancelled: ${databaseError.message}")
                    }
                })
            }
        }
    }
}
