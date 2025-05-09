package com.example.scaffoldsmart.util

import android.content.Context
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
import android.util.Log
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.graphics.withTranslation

class SmartContract {

    private val emailToClient = EmailToClient()

    fun createScaffoldingContractPdf(
        context: Context,
        isApproved: Boolean?,
        ownerName: String?,
        company: String?,
        ownerEmail: String?,
        ownerPhone: String?,
        companyAddress: String?,
        clientName: String?,
        clientPhone: String?,
        clientEmail: String?,
        clientCnic: String?,
        clientAddress: String?,
        rentalAddress: String?,
        startDuration: String?,
        endDuration: String?,
        pipes: String?,
        pipesLength: String?,
        joints: String?,
        wench: String?,
        motors: String?,
        pumps: String?,
        generators: String?,
        wheel: String?
    ) {
        // Create a new PdfDocument
        val document = PdfDocument()

        // Define page dimensions (A4 size in points: 595 x 842)
        val pageWidth = 595
        val pageHeight = 842

        // Create Page 1
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = document.startPage(pageInfo1)
        drawPage1Content(
            page1.canvas, ownerName, company, ownerEmail, ownerPhone,
            companyAddress, clientName, clientPhone, clientEmail, clientCnic, clientAddress,
            rentalAddress, startDuration, endDuration, pipes, pipesLength, joints,
            wench, motors, pumps, generators, wheel
        )
        document.finishPage(page1)

        try {
            if (isApproved == true) {
                // Create PDF in memory and send directly
                val outputStream = ByteArrayOutputStream()
                document.writeTo(outputStream)
                val pdfBytes = outputStream.toByteArray()

                // Create a temporary file in cache
                val tempFile = File.createTempFile("contract", ".pdf", context.cacheDir).apply {
                    deleteOnExit() // Ensure it gets deleted eventually
                }
                FileOutputStream(tempFile).use { fos ->
                    fos.write(pdfBytes)
                }

                // Send email with the temporary PDF
                emailToClient.sendEmailWithPdfWithCoroutine(tempFile, clientEmail)
            } else {
                // Save to storage only if not approved
                val documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val filePath = File(documentsDirectory, "Contract_${System.currentTimeMillis()}.pdf")
                document.writeTo(FileOutputStream(filePath))
                Toast.makeText(context, "Smart Contract Downloaded", Toast.LENGTH_SHORT).show()
                Log.d("SmartContract", "PDF saved successfully to storage")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("SmartContract", "Error handling PDF", e)
        } finally {
            // Close the document in all cases
            document.close()
        }
    }

    private fun drawPage1Content(
        canvas: Canvas,
        ownerName: String?,
        company: String?,
        ownerEmail: String?,
        ownerPhone: String?,
        companyAddress: String?,
        clientName: String?,
        clientPhone: String?,
        clientEmail: String?,
        clientCnic: String?,
        clientAddress: String?,
        rentalAddress: String?,
        startDuration: String?,
        endDuration: String?,
        pipes: String?,
        pipesLength: String?,
        joints: String?,
        wench: String?,
        motors: String?,
        pumps: String?,
        generators: String?,
        wheel: String?
    ) {
        val formatedStartDate = DateFormater.formatDateString(startDuration)
        val formatedEndDate = DateFormater.formatDateString(endDuration)
        val durationInMonths = DateFormater.calculateDurationInMonths(startDuration, endDuration)

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

        // Create the agreement text with all variables
        val agreementText = "This Rental Agreement (the \"Agreement\") is made and entered into as of the $formatedStartDate. " +
                "The rental period for the Equipment shall commence on $formatedStartDate and shall end on $formatedEndDate, " +
                "unless terminated earlier in accordance with this Agreement."

        // Create a SpannableString to apply bold style
        val spannableString = SpannableString(agreementText)

        // Bold all occurrences of the start date
        var index = agreementText.indexOf(formatedStartDate)
        while (index >= 0) {
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                index,
                index + formatedStartDate.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            index = agreementText.indexOf(formatedStartDate, index + formatedStartDate.length)
        }

        // Bold the end date
        index = agreementText.indexOf(formatedEndDate)
        if (index >= 0) {
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                index,
                index + formatedEndDate.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Bold "Agreement"
        index = agreementText.indexOf("\"Agreement\"")
        if (index >= 0) {
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                index,
                index + "\"Agreement\"".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Use StaticLayout for the paragraph
        val textWidth = canvas.width - 100
        val staticLayout = StaticLayout.Builder.obtain(spannableString, 0, spannableString.length, textPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0.0f, 1.0f)
            .setIncludePad(false)
            .build()

        // Draw the StaticLayout
        canvas.withTranslation(50f, 80f) { staticLayout.draw(this) }

        // Draw OWNER section
        canvas.drawText("OWNER:", 50f, 120f + staticLayout.height, textPaint)
        ownerName?.let { drawUnderlinedText(canvas, it, 90f, 120f + staticLayout.height, textPaint) }
        company?.let { drawUnderlinedText(canvas, it, 50f, 140f + staticLayout.height, textPaint) }
        companyAddress?.let { drawUnderlinedText(canvas, it, 50f, 160f + staticLayout.height, textPaint) }
        ownerPhone?.let { drawUnderlinedText(canvas, it, 50f, 180f + staticLayout.height, textPaint) }
        ownerEmail?.let { drawUnderlinedText(canvas, it, 50f, 200f + staticLayout.height, textPaint) }

        // Draw RENTER section
        canvas.drawText("RENTER: ", 50f, 240f + staticLayout.height, textPaint)
        clientName?.let { drawUnderlinedText(canvas, it, 93f, 240f + staticLayout.height, textPaint) }
        canvas.drawText("Address: ", 50f, 260f + staticLayout.height, textPaint)
        clientAddress?.let { drawUnderlinedText(canvas, it, 90f, 260f + staticLayout.height, textPaint) }
        canvas.drawText("CNIC: ", 50f, 280f + staticLayout.height, textPaint)
        clientCnic?.let { drawUnderlinedText(canvas, it, 75f, 280f + staticLayout.height, textPaint) }
        canvas.drawText("Phone: ", 50f, 300f + staticLayout.height, textPaint)
        clientPhone?.let { drawUnderlinedText(canvas, it, 85f, 300f + staticLayout.height, textPaint) }
        canvas.drawText("Email: ", 50f, 320f + staticLayout.height, textPaint)
        clientEmail?.let { drawUnderlinedText(canvas, it, 82f, 320f + staticLayout.height, textPaint) }

        // Draw Place of Use & Rental Period
        canvas.drawText("Place of Use: ", 50f, 360f + staticLayout.height, textPaint)
        rentalAddress?.let { drawUnderlinedText(canvas, it, 110f, 360f + staticLayout.height, textPaint) }
        canvas.drawText("Rental Period: ", 50f, 380f + staticLayout.height, textPaint)
        drawUnderlinedText(canvas, durationInMonths, 118f, 380f + staticLayout.height, textPaint)

        // Draw Equipment Rented table
        canvas.drawText("Equipment Rented", 230f, 420f + staticLayout.height, titlePaint)
        canvas.drawText("Item                           Quantity                            Length ", 160f, 460f + staticLayout.height, textPaint)
        canvas.drawText("1.Scaffolding Pipes", 140f, 480f + staticLayout.height, textPaint)
        pipes?.let { drawUnderlinedText(canvas, it, 275f, 480f + staticLayout.height, textPaint) }
        pipesLength?.let { drawUnderlinedText(canvas, it, 390f, 480f + staticLayout.height, textPaint) }
        canvas.drawText("2.Joints", 140f, 500f + staticLayout.height, textPaint)
        joints?.let { drawUnderlinedText(canvas, it, 275f, 500f + staticLayout.height, textPaint) }
        canvas.drawText("3.Electric Motors", 140f, 520f + staticLayout.height, textPaint)
        motors?.let { drawUnderlinedText(canvas, it, 275f, 520f + staticLayout.height, textPaint) }
        canvas.drawText("4.Generators", 140f, 540f + staticLayout.height, textPaint)
        generators?.let { drawUnderlinedText(canvas, it, 275f, 540f + staticLayout.height, textPaint) }
        canvas.drawText("5.Slug Pumps", 140f, 560f + staticLayout.height, textPaint)
        pumps?.let { drawUnderlinedText(canvas, it, 275f, 560f + staticLayout.height, textPaint) }
        canvas.drawText("6.Wench", 140f, 580f + staticLayout.height, textPaint)
        wench?.let { drawUnderlinedText(canvas, it, 275f, 580f + staticLayout.height, textPaint) }
        canvas.drawText("7.Wheel Barrows", 140f, 600f + staticLayout.height, textPaint)
        wheel?.let { drawUnderlinedText(canvas, it, 275f, 600f + staticLayout.height, textPaint) }

        // Draw Terms & Conditions
        canvas.drawText("TERMS & CONDITIONS", 50f, 640f + staticLayout.height, conditionTitlePaint)
        canvas.drawText("1. Late returns may incur additional charges at the rate of 1000 .Rs per day.", 50f, 660f + staticLayout.height, conditionTextPaint)
        canvas.drawText("2. The Lessor shall not be liable for any direct or indirect damages resulting from the Equipment's use.", 50f, 680f + staticLayout.height, conditionTextPaint)
        canvas.drawText("3. Any disputes arising from this Agreement shall be resolved through mediation before seeking legal recourse.", 50f, 700f + staticLayout.height, conditionTextPaint)

        // Draw signature lines
        canvas.drawText("Owner Signature", 50f, 740f + staticLayout.height, textPaint)
        ownerName?.let { drawUnderlinedText(canvas, it, 50f, 770f + staticLayout.height, textPaint) }
        canvas.drawText("Renter Signature", 400f, 740f + staticLayout.height, textPaint)
        clientName?.let { drawUnderlinedText(canvas, it, 400f, 770f + staticLayout.height, textPaint) }
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