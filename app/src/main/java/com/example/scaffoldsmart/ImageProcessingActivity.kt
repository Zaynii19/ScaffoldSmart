package com.example.scaffoldsmart

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.scaffoldsmart.admin.SettingActivity
import com.example.scaffoldsmart.databinding.ActivityImageProcessingBinding
import com.example.scaffoldsmart.util.RoboflowObjectDetector
import com.example.scaffoldsmart.util.TfliteObjectDetector
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageProcessingActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityImageProcessingBinding.inflate(layoutInflater)
    }
    private var originalBitmap: Bitmap? = null
    //private var tfliteObjectDetector: tfliteObjectDetector? = null
    private var roboflowObjectDetector: RoboflowObjectDetector? = null
    private val TAG = "ImageProcessingDebug"
    private var progressDialog: ProgressDialog? = null
    private var senderUid: String? = null
    private lateinit var chatPreferences: SharedPreferences

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

        //tfliteObjectDetector = tfliteObjectDetector(this)

        roboflowObjectDetector = RoboflowObjectDetector()

        progressDialog = ProgressDialog(this).apply {
            setMessage("Detecting pipes...")
            setCancelable(false)
        }

        // Load image from intent
        val byteArray = intent.getByteArrayExtra("image_data")
        if (byteArray != null) {
            originalBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.imageToProcess.setImageBitmap(originalBitmap)
        } else {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Setup buttons
        binding.detectButton.setOnClickListener { detectPipes() }

        binding.backBtn.setOnClickListener { finish() }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Online"
        presenceMap["lastSeen"] = currentTime
        senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
    }

    override fun onPause() {
        super.onPause()
        senderUid = chatPreferences.getString("SenderUid", null)
        val currentTime = System.currentTimeMillis()
        val presenceMap = HashMap<String, Any>()
        presenceMap["status"] = "Offline"
        presenceMap["lastSeen"] = currentTime
        senderUid?.let { Firebase.database.reference.child("ChatUser").child(it).updateChildren(presenceMap) }
    }

    private fun detectPipes() {
        originalBitmap?.let { bitmap ->
            lifecycleScope.launch {
                try {
                    // Show loading UI
                    progressDialog?.show()
                    binding.detectButton.isEnabled = false
                    binding.detectButton.backgroundTintList = ContextCompat.getColorStateList(this@ImageProcessingActivity, R.color.dark_gray)

                    // Run detection
                    val result = withContext(Dispatchers.IO) {
                        //tfliteObjectDetector?.detectPipes(bitmap)
                        roboflowObjectDetector?.detectPipes(bitmap)
                    }

                    // Update UI
                    result?.let {
                        displayResults(it)
                        Log.d(TAG, "Detection Result : ${it.pipeCount}, ${it.overlayBitmap}, ${it.allDetections}")
                    } ?: run {
                        Toast.makeText(this@ImageProcessingActivity, "Detection failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Detection error", e)
                    Toast.makeText(this@ImageProcessingActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    progressDialog?.dismiss()
                    binding.detectButton.isEnabled = true
                    binding.detectButton.backgroundTintList = ContextCompat.getColorStateList(this@ImageProcessingActivity, R.color.buttons_color)
                }
            }
        } ?: run {
            Toast.makeText(this, "No image to process", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayResults(result: RoboflowObjectDetector.PipeDetectionResult) {
        // Show overlay image with pipe detections
        result.overlayBitmap?.let { overlay ->
            // Combine original image with overlay
            val combined = createBitmap(originalBitmap!!.width, originalBitmap!!.height)
            val canvas = Canvas(combined)
            canvas.drawBitmap(originalBitmap!!, 0f, 0f, null)
            canvas.drawBitmap(overlay, 0f, 0f, null)
            binding.imageToProcess.setImageBitmap(combined)
        }

        // Update count display
        binding.countResult.text = "${result.pipeCount}"

        // Show appropriate toast
        when {
            result.pipeCount == 0 -> {
                Toast.makeText(this, "No pipes detected", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Found ${result.pipeCount} pipes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        //tfliteObjectDetector?.close()
        roboflowObjectDetector?.close()
        originalBitmap?.recycle()
        super.onDestroy()
    }
}