package com.example.scaffoldsmart.admin

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
import com.example.scaffoldsmart.ImageProcessingActivity
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.ActivityMainAdminBinding
import com.example.scaffoldsmart.admin.admin_service.AdminMessageListenerService
import com.example.scaffoldsmart.admin.admin_service.LowInventoryAlertService
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

class AdminMainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainAdminBinding.inflate(layoutInflater)
    }

    private lateinit var onesignal: OnesignalService
    private var prevNotiCompletedAt: String? = null
    private var prevNotificationId: String? = null
    private lateinit var reqPreferences: SharedPreferences
    private lateinit var chatPreferences: SharedPreferences
    private var senderUid: String? = null
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
                val pipes = data.optString("pipes", "N/A")
                val pipesLength = data.optString("pipesLength", "N/A")
                val joints = data.optString("joints", "N/A")
                val wench = data.optString("wench", "N/A")
                val pumps = data.optString("pumps", "N/A")
                val motors = data.optString("motors", "N/A")
                val generators = data.optString("generators", "N/A")
                val wheel = data.optString("wheel", "N/A")
                val totalRent = data.optString("rent", "N/A")

                storeRentalReq(clientID, requestId, clientName, clientAddress, clientEmail, clientPhone,
                    clientCnic, rentalAddress, startDuration, endDuration, pipes.toInt(), pipesLength.toInt(),
                    joints.toInt(), wench.toInt(), pumps.toInt(), motors.toInt(), generators.toInt(),
                    wheel.toInt(), totalRent.toInt())
            }
        }

        private fun storeRentalReq(
            clientID: String,
            requestId: String,
            clientName: String,
            clientAddress: String,
            clientEmail: String,
            clientPhone: String,
            clientCnic: String,
            rentalAddress: String,
            startDuration: String,
            endDuration: String,
            pipes: Int,
            pipesLength: Int,
            joints: Int,
            wench: Int,
            pumps: Int,
            motors: Int,
            generators: Int,
            wheel: Int,
            totalRent: Int
        ) {
            val databaseRef = Firebase.database.reference.child("Rentals")

            // Check if rental with this requestId already exists
            databaseRef.child(requestId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d("AdminMainDebug", "Rental request with ID $requestId already exists")
                    } else {
                        // Create new rental request model
                        val newReq = RentalModel(
                            clientID, requestId, clientName, clientEmail, clientCnic,
                            clientPhone, clientAddress, rentalAddress, startDuration,
                            endDuration, pipes, pipesLength, joints, wench, motors,
                            pumps, generators, wheel, totalRent, ""
                        )

                        // Store with requestId as the key
                        databaseRef.child(requestId).setValue(newReq)
                            .addOnSuccessListener {
                                Log.d("AdminMainDebug", "Rental data stored successfully with ID $requestId")
                            }
                            .addOnFailureListener {
                                Log.e("AdminMainDebug", "Failed to store rental data: ${it.message}")
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
