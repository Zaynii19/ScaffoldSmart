package com.example.scaffoldsmart.util

import android.util.Log
import com.example.scaffoldsmart.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailToClient {
    fun sendEmailWithPdfWithCoroutine(pdfFile: File?, clientEmail: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!pdfFile!!.exists() || pdfFile.length() == 0L) {
                    Log.e("EmailToClient", "PDF file doesn't exist or is empty: ${pdfFile.absolutePath}")
                    return@launch
                }
                sendEmailWithPdf(pdfFile, clientEmail)
            } finally {
                // Delete the temporary file after sending
                try {
                    pdfFile!!.delete()
                } catch (e: Exception) {
                    Log.w("EmailToClient", "Could not delete temp file", e)
                }
            }
        }
    }

    private fun sendEmailWithPdf(pdfFile: File, clientEmail: String?) {
        try {
            Log.d("EmailToClient", "Attempting to send email with PDF: ${pdfFile.name}")
            Log.d("EmailToClient", "PDF file size: ${pdfFile.length()} bytes, exists: ${pdfFile.exists()}")

            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                throw IllegalStateException("PDF file is empty or doesn't exist")
            }

            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.connectiontimeout", "10000")
                put("mail.smtp.timeout", "10000")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    val senderEmail = "sallarmirza77@gmail.com"
                    val senderPassword = BuildConfig.GOOGLE_APP_PASSWORD
                    return PasswordAuthentication(senderEmail, senderPassword)
                }
            })

            session.debug = true // Enable SMTP debug logging

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress("zaynii1911491@gmail.com"))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(clientEmail))
                subject = "Rental Request Alert"

                val multipart = MimeMultipart()

                val textBodyPart = MimeBodyPart().apply {
                    setText("Your Rental Request Has Been Approved.")
                }

                val attachmentBodyPart = MimeBodyPart().apply {
                    try {
                        attachFile(pdfFile)
                    } catch (e: Exception) {
                        Log.e("EmailToClient", "Error attaching file", e)
                        throw e
                    }
                }

                multipart.addBodyPart(textBodyPart)
                multipart.addBodyPart(attachmentBodyPart)
                setContent(multipart)
            }

            Transport.send(message)
            Log.d("EmailToClient", "Email sent successfully to $clientEmail")
        } catch (e: Exception) {
            Log.e("EmailToClient", "Error sending email to $clientEmail", e)
            throw e // Re-throw to see full stacktrace in caller
        }
    }
}