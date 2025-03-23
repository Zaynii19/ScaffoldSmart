package com.example.scaffoldsmart.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.StyleSpan
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object SmartContract {
    fun createScaffoldingContractPdf() {
        // Create a new PdfDocument
        val document = PdfDocument()

        // Define page dimensions (A4 size in points: 595 x 842)
        val pageWidth = 595
        val pageHeight = 842
        val fileName = "Scaffolding_Rental_Contract.pdf"

        // Create Page 1
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = document.startPage(pageInfo1)
        drawPage1Content(page1.canvas)
        document.finishPage(page1)

        // Write the document content to a file
        val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString(), fileName) // save file in external Documents folder
        try {
            document.writeTo(FileOutputStream(filePath))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Close the document
        document.close()
    }

    private fun drawPage1Content(canvas: Canvas) {
        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val conditionTitlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val conditionTextPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        // Draw the title
        canvas.drawText("Scaffolding Equipment Rental Contractual Agreement", 60f, 50f, titlePaint)

        // Draw the agreement details as a paragraph
        val agreementText = "This Rental Agreement (the \"Agreement\") is made and entered into as of the 22th September, 2025. " +
                "The rental period for the Equipment shall commence on 15 September, 2024 and shall end on 22 September, 2025, " +
                "unless terminated earlier in accordance with this Agreement."

        // Create a SpannableString to apply bold style to specific parts
        val spannableString = SpannableString(agreementText)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), agreementText.indexOf("\"Agreement\""), agreementText.indexOf("\"Agreement\"") + "\"Agreement\"".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), agreementText.indexOf("22th September, 2025"), agreementText.indexOf("22th September, 2025") + "22th September, 2025".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), agreementText.indexOf("15 September, 2024"), agreementText.indexOf("15 September, 2024") + "15 September, 2024".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), agreementText.indexOf("22 September, 2025"), agreementText.indexOf("22 September, 2025") + "22 September, 2025".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Use StaticLayout.Builder to create the layout
        val textWidth = canvas.width - 100 // Adjust width as needed
        val staticLayout = StaticLayout.Builder.obtain(spannableString, 0, spannableString.length, textPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0.0f, 1.0f) // Default line spacing
            .setIncludePad(false)
            .build()

        // Draw the StaticLayout
        canvas.save()
        canvas.translate(50f, 80f) // Set the starting position
        staticLayout.draw(canvas)
        canvas.restore()

        // Draw OWNER section
        canvas.drawText("OWNER:", 50f, 120f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "Sallar Ahmed Mirza", 90f, 120f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "Salar Private LTD", 50f, 140f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "Rawalpindi", 50f, 160f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "02743847435", 50f, 180f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "sdndei@nasd", 50f, 200f + staticLayout.height, textPaint)

        // Draw RENTER section
        canvas.drawText("RENTER: ", 50f, 240f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "Ali Zain", 93f, 240f + staticLayout.height, textPaint)
        canvas.drawText("Address: ", 50f, 260f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "Lahore", 90f, 260f + staticLayout.height, textPaint)
        canvas.drawText("CNIC: ", 50f, 280f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "378246823764", 75f, 280f + staticLayout.height, textPaint)
        canvas.drawText("Phone: ", 50f, 300f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "0932307434", 85f, 300f + staticLayout.height, textPaint)
        canvas.drawText("Email: ", 50f, 320f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "zauihsnwdo", 82f, 320f + staticLayout.height, textPaint)

        // Draw Place of Use & Rental Period
        canvas.drawText("Place of Use: ", 50f, 360f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "Islamabad", 110f, 360f + staticLayout.height, textPaint)
        canvas.drawText("Rental Period: ", 50f, 380f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "5 Weeks", 118f, 380f + staticLayout.height, textPaint)

        // Draw Equipment Rented table
        canvas.drawText("Equipment Rented", 230f, 420f + staticLayout.height, titlePaint)
        canvas.drawText("Item                           Quantity                            Length ", 160f, 460f + staticLayout.height, textPaint)
        canvas.drawText("1.Scaffolding Pipes", 140f, 480f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "1500", 275f, 480f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "10 feet", 390f, 480f + staticLayout.height, textPaint)
        canvas.drawText("2.Joints", 140f, 500f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "1500", 275f, 500f + staticLayout.height, textPaint)
        canvas.drawText("3.Electric Motors", 140f, 520f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "1500", 275f, 520f + staticLayout.height, textPaint)
        canvas.drawText("4.Generators", 140f, 540f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "1500", 275f, 540f + staticLayout.height, textPaint)
        canvas.drawText("5.Slug Pumps", 140f, 560f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "1500", 275f, 560f + staticLayout.height, textPaint)
        canvas.drawText("6.Wench", 140f, 580f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "1500", 275f, 580f + staticLayout.height, textPaint)
        canvas.drawText("7.Wheel Barrows", 140f, 600f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "1500", 275f, 600f + staticLayout.height, textPaint)

        // Draw Terms & Conditions
        canvas.drawText("TERMS & CONDITIONS", 50f, 640f + staticLayout.height, conditionTitlePaint)
        canvas.drawText("1. Late returns may incur additional charges at the rate of 1000 .Rs per day.", 50f, 660f + staticLayout.height, conditionTextPaint)
        canvas.drawText("2. The Lessor shall not be liable for any direct or indirect damages resulting from the Equipment's use.", 50f, 680f + staticLayout.height, conditionTextPaint)
        canvas.drawText("3. Any disputes arising from this Agreement shall be resolved through mediation before seeking legal recourse.", 50f, 700f + staticLayout.height, conditionTextPaint)

        // Draw signature lines
        canvas.drawText("Owner Signature", 50f, 740f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "Sallar Ahmed Mirza", 50f, 770f + staticLayout.height, textPaint)
        canvas.drawText("Renter Signature", 400f, 740f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, "Ali Zain", 400f, 770f + staticLayout.height, textPaint)
    }

    private fun drawUnderlinedText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        // Draw the text
        canvas.drawText(text, x, y, paint)

        // Calculate the width of the text
        val textWidth = paint.measureText(text)

        // Draw the underline
        canvas.drawLine(x, y + 5, x + textWidth, y + 5, paint)
    }
}