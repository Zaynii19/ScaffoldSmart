package com.example.scaffoldsmart.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import android.os.Build
import java.io.ByteArrayOutputStream
import androidx.core.net.toUri

object InvoiceGenerator {

    // Convert view to bitmap of view's dimensions
    /*fun createBitmapFromView(view: View): Bitmap {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap = createBitmap(view.measuredWidth, view.measuredHeight)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
    }*/

    // Convert view to bitmap of fixed dimensions e.g A4 size
    fun createBitmapFromView(view: View): Bitmap {
        // Use fixed dimensions that match your invoice design
        val width = 1080
        val height = 1220

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        // Measure and layout with exact dimensions
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, width, view.measuredHeight)

        view.draw(canvas)
        return bitmap
    }

    // Save bitmap to gallery
    fun saveBitmapToGallery(bitmap: Bitmap, context: Context): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Invoice_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val resolver = context.contentResolver
        return try {
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Create PDF from view
    fun createPdfFromView(view: View): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = document.startPage(pageInfo)

        view.draw(page.canvas)
        document.finishPage(page)

        val documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val filePath = File(documentsDirectory, "Invoice_${System.currentTimeMillis()}.pdf")
        document.writeTo(FileOutputStream(filePath))
        document.close()

        return filePath
    }

    // Create PDF from bitmap
    fun createPdfFromBitmap(bitmap: Bitmap): File {
        // Create a new PDF document
        val document = PdfDocument()

        // Create page info matching the bitmap dimensions
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = document.startPage(pageInfo)

        // Draw the bitmap on the PDF canvas
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)

        // Create the output file
        val documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val filePath = File(documentsDirectory, "Invoice_${System.currentTimeMillis()}.pdf")

        // Save the PDF
        document.writeTo(FileOutputStream(filePath))
        document.close()

        return filePath
    }

    // Get URI from bitmap for sharing
    fun getImageUri(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "Invoice_${System.currentTimeMillis()}",
            null
        )
        return path.toUri()
    }
}