package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.AdminModel
import com.example.scaffoldsmart.admin.admin_models.InventoryModel
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.databinding.GenerateInvoiceRcvItemBinding
import com.example.scaffoldsmart.databinding.InvoiceItemBinding
import com.example.scaffoldsmart.util.DateFormater
import com.example.scaffoldsmart.util.InvoiceGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InvoiceRcvAdapter(
    val context: Context,
    private var rentalList: ArrayList<RentalModel>,
    private var itemList: ArrayList<InventoryModel>,
    var adminObj: AdminModel
): RecyclerView.Adapter<InvoiceRcvAdapter.MyInvoiceViewHolder>() {

    class MyInvoiceViewHolder(val binding: GenerateInvoiceRcvItemBinding): RecyclerView.ViewHolder(binding.root)

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

        binder.companyName.text = adminObj.company
        binder.companyAddress.text = adminObj.address
        binder.companyPhone.text = adminObj.phone
        binder.companyEmail.text = adminObj.email

        binder.customerName.text = currentRent.clientName
        binder.customerPhone.text = currentRent.clientPhone

        binder.invoiceNumber.text = buildString {
            append("ShowInvoice ID# ")
            append(generateInvoiceId())
        }

        binder.issuedDate.text = buildString {
            append("Issued Date: ")
            append(currentRent.startDuration)
        }

        // Process each item
        setViewVisibilityAndText(currentRent, binder.item1, binder.item1Qty, binder.item1UnitPrice, binder.item1Amount, binder.item1Days, binder.item1Layout, binder)
        setViewVisibilityAndText(currentRent, binder.item2, binder.item2Qty, binder.item2UnitPrice, binder.item2Amount, binder.item2Days, binder.item2Layout, binder)
        setViewVisibilityAndText(currentRent, binder.item3, binder.item3Qty, binder.item3UnitPrice, binder.item3Amount, binder.item3Days, binder.item3Layout, binder)
        setViewVisibilityAndText(currentRent, binder.item4, binder.item4Qty, binder.item4UnitPrice, binder.item4Amount, binder.item4Days, binder.item4Layout, binder)
        setViewVisibilityAndText(currentRent, binder.item5, binder.item5Qty, binder.item5UnitPrice, binder.item5Amount, binder.item5Days, binder.item5Layout, binder)
        setViewVisibilityAndText(currentRent, binder.item6, binder.item6Qty, binder.item6UnitPrice, binder.item6Amount, binder.item6Days, binder.item6Layout, binder)
        setViewVisibilityAndText(currentRent, binder.item7, binder.item7Qty, binder.item7UnitPrice, binder.item7Amount, binder.item7Days, binder.item7Layout, binder)

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
                Toast.makeText(context, "ShowInvoice saved to gallery", Toast.LENGTH_SHORT).show()
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

    private fun setViewVisibilityAndText(
        currentRent: RentalModel,
        item: TextView,
        qty: TextView,
        unitRent: TextView,
        rent: TextView,
        days: TextView,
        entry: ConstraintLayout,
        binder: InvoiceItemBinding
    ): Int {
        val diffInDays = DateFormater.calculateDurationInDays(currentRent.startDuration, currentRent.endDuration)

        // Determine which item type this view should represent
        val (itemName, quantity, price) = when (entry) {
            binder.item1Layout -> findItemData(currentRent, "pipe")
            binder.item2Layout -> findItemData(currentRent, "joint")
            binder.item3Layout -> findItemData(currentRent, "wench")
            binder.item4Layout -> findItemData(currentRent, "pump")
            binder.item5Layout -> findItemData(currentRent, "generator")
            binder.item6Layout -> findItemData(currentRent, "motor")
            binder.item7Layout -> findItemData(currentRent, "wheel")
            else -> Triple("", 0, 0)
        }

        if (quantity != null && price != null && quantity > 0 && price > 0) {
            entry.visibility = View.VISIBLE
            item.text = itemName
            qty.text = quantity.toString()
            unitRent.text = price.toString()
            days.text = diffInDays.toString()
            val itemRent = price * quantity * diffInDays
            rent.text = itemRent.toString()
            return itemRent
        } else {
            entry.visibility = View.GONE
            return 0
        }
    }

    private fun findItemData(currentRent: RentalModel, itemType: String): Triple<String?, Int?, Int?> {
        val quantity = when (itemType) {
            "pipe" -> currentRent.pipes
            "joint" -> currentRent.joints
            "wench" -> currentRent.wench
            "pump" -> currentRent.pumps
            "generator" -> currentRent.generators
            "motor" -> currentRent.motors
            "wheel" -> currentRent.wheel
            else -> 0
        }

        if (quantity == 0) return Triple("", 0, 0)

        val item = itemList.find {
            it.itemName?.lowercase()!!.contains(itemType)
        }

        return (if (item != null) {
            Triple(item.itemName, quantity, item.price)
        } else {
            Triple("", 0, 0)
        })
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

    fun updateData(newRentalList: List<RentalModel>, newItemList: List<InventoryModel>, newAdminObj: AdminModel?) {
        rentalList.clear()
        rentalList.addAll(newRentalList)
        itemList.clear()
        itemList.addAll(newItemList)
        if (newAdminObj != null) {
            adminObj = newAdminObj
        }
        notifyDataSetChanged()
    }
}