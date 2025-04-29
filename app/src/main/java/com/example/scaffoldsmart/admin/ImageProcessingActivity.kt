package com.example.scaffoldsmart.admin

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.ActivityImageProcessingBinding
import com.example.scaffoldsmart.util.ScaffoldingPipeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageProcessingActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityImageProcessingBinding.inflate(layoutInflater)
    }
    private var currentImageBitmap: Bitmap? = null
    private var scaffoldingDetector: ScaffoldingPipeDetector? = null
    private val TAG = "ImageProcessingDebug"
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scaffoldingDetector = ScaffoldingPipeDetector(this@ImageProcessingActivity)
        progressDialog = ProgressDialog(this@ImageProcessingActivity)
        progressDialog?.setMessage("Counting Scaffolding Pipes...")
        progressDialog?.setCancelable(false)

        // Retrieve the image data from intent
        val byteArray = intent.getByteArrayExtra("image_data")
        if (byteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            currentImageBitmap = bitmap
            binding.imageToProcess.setImageBitmap(bitmap)
        } else {
            finish() // Close activity if no image was passed
        }

        binding.detectButton.setOnClickListener { detectPipes() }

        binding.backBtn.setOnClickListener { finish() }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this@ImageProcessingActivity, SettingActivity::class.java))
            finish()
        }
    }

    private fun detectPipes() {
        currentImageBitmap?.let { bitmap ->
            lifecycleScope.launch {
                try {
                    // Show loading UI
                    Log.d(TAG, "Starting pipe detection...")
                    progressDialog?.show()
                    binding.detectButton.isEnabled = false
                    binding.detectButton.backgroundTintList = ContextCompat.getColorStateList(this@ImageProcessingActivity, R.color.dark_gray)
                    binding.countResult.text = "" // Clear previous result

                    // Run detection on background thread
                    val pipeCount = withContext(Dispatchers.IO) {
                        scaffoldingDetector?.detectAndCountPipes(bitmap) ?: 0
                    }
                    Log.d(TAG, "Detection completed. Count: $pipeCount")

                    // Update UI with results
                    binding.countResult.text = "$pipeCount"

                    when {
                        pipeCount == 0 -> {
                            Toast.makeText(this@ImageProcessingActivity, "No pipes detected", Toast.LENGTH_SHORT).show()
                        }
                        pipeCount > 0 -> {
                            Toast.makeText(this@ImageProcessingActivity, "Detected $pipeCount pipes", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Detection error", e)
                    Toast.makeText(this@ImageProcessingActivity, "Detection failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    // Restore UI state
                    progressDialog?.dismiss()
                    binding.detectButton.backgroundTintList = ContextCompat.getColorStateList(this@ImageProcessingActivity, R.color.buttons_color)
                    binding.detectButton.isEnabled = true
                }
            }
        } ?: run {
            Toast.makeText(
                this@ImageProcessingActivity,
                "No image to process",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        scaffoldingDetector?.close()
        currentImageBitmap?.recycle()
        super.onDestroy()
    }
}
