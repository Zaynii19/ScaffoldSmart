package com.example.scaffoldsmart.util

import android.content.Context
import android.graphics.*
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.createBitmap

class ScaffoldingPipeDetector(context: Context) {

    private val TAG = "PipeDetectorDebug"
    private var interpreter: Interpreter? = null
    private val modelFileName = "image_processing.tflite"

    // Model parameters
    private val inputSize = 640
    private val numChannels = 3
    private val floatSize = 4 // bytes per float

    // Detection parameters
    private val PIPE_CLASS_ID = 0 // Assuming pipe is class 0
    private val MIN_CONFIDENCE = 0.5f
    private val NMS_THRESHOLD = 0.5f

    init {
        try {
            interpreter = Interpreter(loadModelFile(context))
            Log.d(TAG, "TFLite model loaded successfully")
            printModelInputOutputDetails()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TFLite interpreter", e)
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    suspend fun detectPipes(bitmap: Bitmap): PipeDetectionResult = withContext(Dispatchers.Default) {
        try {
            // 1. Preprocess image to model input format
            val inputBuffer = preprocessImage(bitmap)

            // 2. Prepare output buffer matching model's [1,7,8400] shape
            val outputBuffer = ByteBuffer.allocateDirect(1 * 7 * 8400 * floatSize).apply {
                order(ByteOrder.nativeOrder())
            }

            // 3. Run inference
            interpreter?.run(inputBuffer, outputBuffer)

            // 4. Process output and count pipes
            return@withContext processOutput(outputBuffer, bitmap.width, bitmap.height)
        } catch (e: Exception) {
            Log.e(TAG, "Detection error", e)
            return@withContext PipeDetectionResult(emptyList(), null, 0)
        }
    }

    private fun processOutput(outputBuffer: ByteBuffer, imgWidth: Int, imgHeight: Int): PipeDetectionResult {
        outputBuffer.rewind()
        val detections = mutableListOf<PipeDetection>()

        // YOLOv8 output format: [1,7,8400] where 7 = [x,y,w,h,conf,class0,class1]
        for (i in 0 until 8400) {
            if (outputBuffer.remaining() < 7 * floatSize) break

            val x = outputBuffer.getFloat()
            val y = outputBuffer.getFloat()
            val w = outputBuffer.getFloat()
            val h = outputBuffer.getFloat()
            val conf = outputBuffer.getFloat()
            val class0 = outputBuffer.getFloat()
            val class1 = outputBuffer.getFloat()

            // Only consider pipe detections (class0) with sufficient confidence
            if (class0 > class1 && conf > MIN_CONFIDENCE) {
                detections.add(PipeDetection(
                    x * imgWidth,       // Convert to image coordinates
                    y * imgHeight,
                    w * imgWidth,
                    h * imgHeight,
                    conf * class0       // Combined confidence
                ))
            }
        }

        // Apply Non-Max Suppression to remove duplicates
        val filtered = applyNMS(detections)

        // Create visualization overlay
        val overlay = createOverlayBitmap(imgWidth, imgHeight, filtered)

        return PipeDetectionResult(filtered, overlay, filtered.size)
    }

    private fun createOverlayBitmap(width: Int, height: Int, detections: List<PipeDetection>): Bitmap {
        val overlay = createBitmap(width, height)
        val canvas = Canvas(overlay)
        val paint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val textBgPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            alpha = 180
        }

        detections.forEachIndexed { index, detection ->
            // Draw bounding box
            val left = detection.x - detection.width/2
            val top = detection.y - detection.height/2
            val right = detection.x + detection.width/2
            val bottom = detection.y + detection.height/2
            canvas.drawRect(left, top, right, bottom, paint)

            // Draw label
            val label = "Pipe ${index + 1}"
            val textWidth = textPaint.measureText(label)
            val textHeight = textPaint.fontMetrics.run { descent - ascent }

            // Text background
            canvas.drawRect(
                left, top - textHeight - 8,
                left + textWidth + 16, top,
                textBgPaint
            )

            // Text
            canvas.drawText(label, left + 8, top - 8, textPaint)
        }

        return overlay
    }

    private fun applyNMS(detections: List<PipeDetection>): List<PipeDetection> {
        val filtered = mutableListOf<PipeDetection>()
        val sorted = detections.sortedByDescending { it.confidence }

        sorted.forEach { current ->
            var overlap = false
            for (selected in filtered) {
                if (calculateIOU(current, selected) > NMS_THRESHOLD) {
                    overlap = true
                    break
                }
            }
            if (!overlap) filtered.add(current)
        }
        return filtered
    }

    private fun calculateIOU(a: PipeDetection, b: PipeDetection): Float {
        val boxA = RectF(
            a.x - a.width/2, a.y - a.height/2,
            a.x + a.width/2, a.y + a.height/2
        )
        val boxB = RectF(
            b.x - b.width/2, b.y - b.height/2,
            b.x + b.width/2, b.y + b.height/2
        )

        val intersection = max(0f, min(boxA.right, boxB.right) - max(boxA.left, boxB.left)) *
                max(0f, min(boxA.bottom, boxB.bottom) - max(boxA.top, boxB.top))
        val areaA = boxA.width() * boxA.height()
        val areaB = boxB.width() * boxB.height()

        return intersection / (areaA + areaB - intersection + 1e-6f)
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val mutableBitmap = convertToMutable(bitmap)
        val scaledBitmap = mutableBitmap.scale(inputSize, inputSize)

        val inputBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * numChannels * floatSize).apply {
            order(ByteOrder.nativeOrder())
        }

        val intValues = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        for (pixelValue in intValues) {
            inputBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255.0f) // R
            inputBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255.0f)  // G
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)        // B
        }

        scaledBitmap.recycle()
        if (mutableBitmap != bitmap) {
            mutableBitmap.recycle()
        }

        return inputBuffer
    }

    private fun convertToMutable(bitmap: Bitmap): Bitmap {
        return if (bitmap.isMutable) {
            bitmap
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }
    }

    private fun printModelInputOutputDetails() {
        interpreter?.let { interpreter ->
            Log.d(TAG, "Input tensor shape: ${interpreter.getInputTensor(0).shape().contentToString()}")
            Log.d(TAG, "Output tensor shape: ${interpreter.getOutputTensor(0).shape().contentToString()}")
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    data class PipeDetection(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val confidence: Float
    )

    data class PipeDetectionResult(
        val detections: List<PipeDetection>,
        val overlayBitmap: Bitmap?,
        val pipeCount: Int
    )
}