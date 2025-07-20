package com.example.scaffoldsmart.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.admin_adapters.ContractDetailRcvAdapter
import com.example.scaffoldsmart.admin.admin_adapters.InvoiceDetailRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.RentalItem
import com.example.scaffoldsmart.databinding.ContractItemBinding

class SmartContract {

    private val emailToClient = EmailToClient()
    private lateinit var adapter: ContractDetailRcvAdapter

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
        rentalItems: ArrayList<RentalItem>?,
        contractView: View // Pass the inflated view from your activity/fragment){}
    ) {

        // Get binding from the view
        val binding = ContractItemBinding.bind(contractView)

        // First, update the view with all the data
        updateContractView(
            context,
            binding,
            ownerName,
            company,
            ownerEmail,
            ownerPhone,
            companyAddress,
            clientName,
            clientPhone,
            clientEmail,
            clientCnic,
            clientAddress,
            rentalAddress,
            startDuration,
            endDuration,
            rentalItems
        )

        // Use fixed dimensions that match your contract design
        val width = 900
        val height = 1800

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        // Measure and layout with exact dimensions
        contractView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
        )
        contractView.layout(0, 0, width, contractView.measuredHeight)
        contractView.draw(canvas)

        // Create PDF document
        val document = PdfDocument()

        // Convert pixels to points (1px = 0.75pt at 160dpi)
        val scale = 595f / bitmap.width // Scale to fit A4 width (595pt)
        val pageWidth = 595 // A4 width in points
        val pageHeight = (bitmap.height * scale).toInt()

        // Create Page
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)

        // Draw the bitmap on the PDF canvas
        page.canvas.scale(scale, scale)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)

        try {
            if (isApproved == true) {
                // Create PDF in memory and send directly
                val outputStream = ByteArrayOutputStream()
                document.writeTo(outputStream)
                val pdfBytes = outputStream.toByteArray()

                // Create a temporary file in cache
                val tempFile = File.createTempFile("Contract_${System.currentTimeMillis()}", ".pdf", context.cacheDir).apply {
                    deleteOnExit()
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
            bitmap.recycle()
        }
    }

    private fun updateContractView(
        context: Context,
        binding: ContractItemBinding,
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
        rentalItems: ArrayList<RentalItem>?
    ) {
        // Update all text views with the provided data
        val formatedStartDuration = DateFormater.formatDateString(startDuration)
        val formatedEndDuration = DateFormater.formatDateString(endDuration)

        binding.agreementClause.text = SpannableStringBuilder().apply {
            append("This Rental Agreement (the ")
            // Bold "Agreement"
            append("Agreement").apply {
                setSpan(StyleSpan(Typeface.BOLD), length - "Agreement".length, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(") is made and entered into as of the ")
            // Bold formatted date
            append(formatedStartDuration).apply {
                setSpan(StyleSpan(Typeface.BOLD), length - formatedStartDuration.length, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(". The rental period for the Equipment shall commence on ")
            append(formatedStartDuration).apply {
                setSpan(StyleSpan(Typeface.BOLD), length - formatedStartDuration.length, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(" and shall end on ")
            append(formatedEndDuration).apply {
                setSpan(StyleSpan(Typeface.BOLD), length - formatedEndDuration.length, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            append(", unless terminated earlier in accordance with this Agreement.")
        }

        binding.ownerName.text = ownerName
        binding.ownerName.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.companyValue.text = company
        binding.companyValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.phoneValue.text = ownerPhone
        binding.phoneValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.addressValue.text = companyAddress
        binding.addressValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.emailValue.text = ownerEmail
        binding.emailValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        binding.renterName.text = clientName
        binding.renterName.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.renterAddressValue.text = clientAddress
        binding.renterAddressValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.renterCnicValue.text = clientCnic
        binding.renterCnicValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.renterPhoneValue.text = clientPhone
        binding.renterPhoneValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.renterEmailValue.text = clientEmail
        binding.renterEmailValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        binding.placeOfUseValue.text = rentalAddress
        binding.placeOfUseValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        val durationInMonths = DateFormater.calculateDurationInMonths(startDuration, endDuration)
        binding.rentalPeriodValue.text = durationInMonths
        binding.rentalPeriodValue.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        setRcv(binding, rentalItems, context)

        binding.ownerSignature.text = ownerName
        binding.ownerSignature.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.renterSignature.text = clientName
        binding.renterSignature.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setRcv(
        binding: ContractItemBinding,
        rentalItems: ArrayList<RentalItem>?,
        context: Context
    ) {
        binding.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        // Provide empty list if items is null
        adapter = ContractDetailRcvAdapter(context, rentalItems ?: ArrayList())
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
    }
}