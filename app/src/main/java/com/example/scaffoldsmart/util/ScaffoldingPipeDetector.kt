package com.example.scaffoldsmart.util

import android.content.Context
import android.graphics.Bitmap
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

class ScaffoldingPipeDetector(context: Context) {

    private val TAG = "PipeDetectorDebug"
    private var interpreter: Interpreter? = null
    private val modelFileName = "image_processing.tflite"

    // Model input parameters
    private val inputSize = 640
    private val numChannels = 3
    private val floatSize = 4 // bytes per float

    init {
        try {
            interpreter = Interpreter(loadModelFile(context))
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

    suspend fun detectAndCountPipes(bitmap: Bitmap): Int = withContext(Dispatchers.Default) {
        printModelInputOutputDetails()
        try {
            Log.d(TAG, "Starting detection process")

            // 1. Preprocess image
            Log.d(TAG, "Preprocessing image...")
            val inputBuffer = preprocessImage(bitmap)
            Log.d(TAG, "Input buffer ready. Capacity: ${inputBuffer.capacity()} bytes")

            // 2. Prepare output buffer
            val outputShape = interpreter?.getOutputTensor(0)?.shape()
            val outputSize = outputShape?.let { it[1] * it[2] } ?: (7 * 8400)
            val outputBuffer = ByteBuffer.allocateDirect(outputSize * floatSize).apply {
                order(ByteOrder.nativeOrder())
            }
            Log.d(TAG, "Output buffer prepared with size: ${outputBuffer.capacity()} bytes")

            // 3. Run inference
            Log.d(TAG, "Running interpreter...")
            interpreter?.run(inputBuffer, outputBuffer)
            Log.d(TAG, "Inference completed")

            // 4. Process results
            return@withContext processOutput(outputBuffer)
        } catch (e: Exception) {
            Log.e(TAG, "Detection error", e)
            return@withContext 0
        }
    }

    private fun printModelInputOutputDetails() {
        interpreter?.let { interpreter ->
            val inputTensor = interpreter.getInputTensor(0)
            Log.d(TAG, "Input tensor shape: ${inputTensor.shape().contentToString()}")
            Log.d(TAG, "Input tensor data type: ${inputTensor.dataType()}")
            Log.d(TAG, "Input tensor byte size: ${inputTensor.numBytes()}")

            val outputTensor = interpreter.getOutputTensor(0)
            Log.d(TAG, "Output tensor shape: ${outputTensor.shape().contentToString()}")
            Log.d(TAG, "Output tensor data type: ${outputTensor.dataType()}")
            Log.d(TAG, "Output tensor byte size: ${outputTensor.numBytes()}")
        }
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        Log.d(TAG, "Original bitmap: ${bitmap.width}x${bitmap.height}, config: ${bitmap.config}")

        // Convert to mutable if needed and scale to 640x640
        val mutableBitmap = convertToMutable(bitmap)
        val scaledBitmap = mutableBitmap.scale(inputSize, inputSize)

        // Create buffer of exact size the model expects
        val inputBuffer = ByteBuffer.allocateDirect(floatSize * inputSize * inputSize * numChannels).apply {
            order(ByteOrder.nativeOrder())
        }

        val intValues = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        // Normalize pixels to 0-1 range (common for YOLO models)
        for (pixelValue in intValues) {
            inputBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255.0f) // R
            inputBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255.0f)  // G
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)        // B
        }

        // Clean up
        scaledBitmap.recycle()
        if (mutableBitmap != bitmap) {
            mutableBitmap.recycle()
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    private fun processOutput(outputBuffer: ByteBuffer): Int {
        outputBuffer.rewind()
        val detections = mutableListOf<Detection>()

        // YOLO output format: [1,7,8400] where 7 is (x,y,w,h,conf,class0,class1)
        val numDetections = 8400
        var count = 0

        for (i in 0 until numDetections) {
            // Get the confidence score (index 4 in each detection)
            val confidence = outputBuffer.getFloat(4 * (i * 7 + 4))

            if (confidence > 0.5f) { // Adjust threshold as needed
                count++

                // If you need bounding boxes:
                val x = outputBuffer.getFloat(4 * (i * 7))
                val y = outputBuffer.getFloat(4 * (i * 7 + 1))
                val w = outputBuffer.getFloat(4 * (i * 7 + 2))
                val h = outputBuffer.getFloat(4 * (i * 7 + 3))

                detections.add(Detection(x, y, w, h, confidence))
            }
        }

        Log.d(TAG, "Total detections: $count")
        return count
    }

    private fun convertToMutable(bitmap: Bitmap): Bitmap {
        return if (bitmap.isMutable) {
            bitmap
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    private data class Detection(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val confidence: Float
    )
}