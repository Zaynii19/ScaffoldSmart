package com.example.scaffoldsmart.util

import android.graphics.*
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import androidx.core.graphics.createBitmap

class RoboflowObjectDetector() {
    // Replace these with your actual Roboflow API details
    private val apiKey = "3GeU4CmPanc8KQlBja53" // Get from Roboflow account settings
    private val modelEndpoint = "pipes-n-joints/5"
    private val detectionUrl = "https://detect.roboflow.com/$modelEndpoint?api_key=$apiKey"

    private val client = OkHttpClient()
    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
        textSize = 32f
    }
    private val textPaint = Paint().apply {
        color = Color.RED
        textSize = 32f
    }

    data class PipeDetectionResult(
        val pipeCount: Int,
        val overlayBitmap: Bitmap?,
        val allDetections: List<Detection> = emptyList()
    )

    data class Detection(
        val className: String,
        val confidence: Float,
        val boundingBox: RectF
    )

    suspend fun detectPipes(bitmap: Bitmap): PipeDetectionResult? {
        return try {
            withContext(Dispatchers.IO) {
                // Convert bitmap to base64
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

                // Create request
                val mediaType = "application/x-www-form-urlencoded".toMediaType()
                val requestBody = base64Image.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(detectionUrl)
                    .post(requestBody)
                    .build()

                // Execute request
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("RoboflowDetector", "API request failed: ${response.code}")
                    return@withContext null
                }

                val responseBody = response.body?.string()
                responseBody?.let { parseResponse(it, bitmap) }
            }
        } catch (e: IOException) {
            Log.e("RoboflowDetector", "Network error", e)
            null
        } catch (e: Exception) {
            Log.e("RoboflowDetector", "Detection error", e)
            null
        }
    }

    private fun parseResponse(jsonResponse: String, originalBitmap: Bitmap): PipeDetectionResult {
        val jsonObject = JSONObject(jsonResponse)
        val predictions = jsonObject.getJSONArray("predictions")
        val imageWidth = jsonObject.getJSONObject("image").getInt("width")
        val imageHeight = jsonObject.getJSONObject("image").getInt("height")

        val scaleX = originalBitmap.width.toFloat() / imageWidth
        val scaleY = originalBitmap.height.toFloat() / imageHeight

        val detections = mutableListOf<Detection>()
        var pipeCount = 0

        // Create overlay bitmap
        val overlayBitmap = createBitmap(originalBitmap.width, originalBitmap.height)
        val canvas = Canvas(overlayBitmap)

        for (i in 0 until predictions.length()) {
            val prediction = predictions.getJSONObject(i)
            val className = prediction.getString("class")
            val confidence = prediction.getDouble("confidence").toFloat()
            val x = prediction.getDouble("x").toFloat()
            val y = prediction.getDouble("y").toFloat()
            val width = prediction.getDouble("width").toFloat()
            val height = prediction.getDouble("height").toFloat()

            Log.d("RoboflowDetector", "Detection Response : $prediction, $className, $confidence, $x, $y, $width, $height")

            // Calculate bounding box coordinates
            val left = (x - width / 2) * scaleX
            val top = (y - height / 2) * scaleY
            val right = (x + width / 2) * scaleX
            val bottom = (y + height / 2) * scaleY

            val boundingBox = RectF(left, top, right, bottom)

            detections.add(Detection(className, confidence, boundingBox))

            // Count pipes and draw bounding boxes
            if (className == "pipe") {
                pipeCount++
                // Draw bounding box
                canvas.drawRect(boundingBox, paint)
                // Draw class label and confidence
                canvas.drawText(
                    "Pipe ${(confidence * 100).toInt()}%",
                    left,
                    top - 10,
                    textPaint
                )
            } else {
                // Draw other objects with different color
                paint.color = Color.BLUE
                canvas.drawRect(boundingBox, paint)
                canvas.drawText(
                    "$className ${(confidence * 100).toInt()}%",
                    left,
                    top - 10,
                    textPaint
                )
                paint.color = Color.RED
            }
        }

        return PipeDetectionResult(pipeCount, overlayBitmap, detections)
    }

    fun close() {
        client.dispatcher.executorService.shutdown()
    }
}