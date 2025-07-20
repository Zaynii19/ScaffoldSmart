package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.GenerateInvoiceRcvItemBinding
import com.example.scaffoldsmart.databinding.InvoiceItemBinding
import com.example.scaffoldsmart.util.InvoiceGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InvoiceRcvAdapter(
    val context: Context,
    private var rentalList: ArrayList<RentalModel>,
    private var adminObj: AdminModel?
): RecyclerView.Adapter<InvoiceRcvAdapter.MyInvoiceViewHolder>() {
    class MyInvoiceViewHolder(val binding: GenerateInvoiceRcvItemBinding): RecyclerView.ViewHolder(binding.root)

    private lateinit var adapter: InvoiceDetailRcvAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyInvoiceViewHolder {
        return MyInvoiceViewHolder(GenerateInvoiceRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return rentalList.size
    }

    override fun onBindViewHolder(holder: MyInvoiceViewHolder, position: Int) {
        val currentRent = rentalList[position]
        holder.binding.clientName.text = currentRent.clientName
        holder.binding.rent.text = buildString {
            append(currentRent.rent)
            append(" .Rs")
        }

        holder.binding.root.setOnClickListener {
            showInvoiceDialog(currentRent)
        }
    }

    private fun showInvoiceDialog(currentRent: RentalModel) {
        val customDialog = LayoutInflater.from(context).inflate(R.layout.invoice_item, null)
        val builder = MaterialAlertDialogBuilder(context)
        val binder = InvoiceItemBinding.bind(customDialog)

        binder.companyName.text = adminObj?.company
        binder.companyAddress.text = adminObj?.address
        binder.companyPhone.text = adminObj?.phone
        binder.companyEmail.text = adminObj?.email

        binder.customerName.text = currentRent.clientName
        binder.customerPhone.text = currentRent.clientPhone

        binder.invoiceNumber.text = buildString {
            append("Invoice ID# ")
            append(generateInvoiceId())
        }

        binder.issuedDate.text = buildString {
            append("Issued Date: ")
            append(currentRent.startDuration)
        }

        setRcv(binder, currentRent)

        binder.totalRent.text = buildString {
            append(currentRent.rent)
            append(" .Rs")
        }

        // Create and show the dialog
        val dialog = builder.setView(customDialog)
            .setBackground(ContextCompat.getDrawable(context, R.drawable.msg_view_received))
            .show()

        binder.shareBtn.setOnClickListener {
            val bitmap = InvoiceGenerator.createBitmapFromView(binder.main)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, InvoiceGenerator.getImageUri(context, bitmap))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share ShowInvoice"))
            dialog.dismiss()
        }

        binder.saveImageBtn.setOnClickListener {
            val bitmap = InvoiceGenerator.createBitmapFromView(binder.main)
            val uri = InvoiceGenerator.saveBitmapToGallery(bitmap, context)
            if (uri != null) {
                Toast.makeText(context, "Invoice saved to gallery", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save invoice", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        binder.savePdfBtn.setOnClickListener {
            val bitmap = InvoiceGenerator.createBitmapFromView(binder.main)
            val pdfFile = InvoiceGenerator.createPdfFromBitmap(bitmap)
            Toast.makeText(context, "PDF saved to ${pdfFile.absolutePath}", Toast.LENGTH_SHORT).show()

            // Share the PDF after saving
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    pdfFile
                ))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF ShowInvoice"))
            dialog.dismiss()
        }

    }

    private fun setRcv(binder: InvoiceItemBinding, currentRent: RentalModel) {
        var rentalItems = currentRent.items
        binder.rcv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        // Provide empty list if items is null
        adapter = InvoiceDetailRcvAdapter(context, rentalItems ?: ArrayList(), currentRent)
        binder.rcv.adapter = adapter
        binder.rcv.setHasFixedSize(true)
    }

    private fun generateInvoiceId(): String {
        // Get current timestamp (last 5 digits)
        val timestampPart = (System.currentTimeMillis() % 100000).toString().padStart(5, '0')

        // Generate random 3 digits
        val randomPart = (100..999).random().toString()

        // Combine to make 8 digits (5 timestamp + 3 random)
        val invoiceId = (timestampPart + randomPart).take(8)

        // Ensure it's exactly 8 digits (in case of leading zeros)
        return invoiceId.padStart(8, '0').take(8)
    }

    fun updateData(newRentalList: ArrayList<RentalModel>, newAdminObj: AdminModel?) {
        rentalList.clear()
        rentalList.addAll(newRentalList)
        if (newAdminObj != null) {
            adminObj = newAdminObj
        }
        notifyDataSetChanged()
    }
}