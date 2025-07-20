package com.example.scaffoldsmart.client.client_adapters

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.client.client_models.CartModel
import com.example.scaffoldsmart.databinding.CartRcvItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.database.database

class CartRcvAdapter(
    val context: Context,
    private var itemList: ArrayList<CartModel>,
    private val clientId: String?,
) : RecyclerView.Adapter<CartRcvAdapter.MyItemViewHolder>() {

    class MyItemViewHolder(val binding: CartRcvItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
        return MyItemViewHolder(CartRcvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.binding.itemName.text = currentItem.itemName
        holder.binding.itemPrice.text = buildString {
            append(currentItem.itemPrice)
            append(" .Rs")
        }

        holder.binding.itemQty.text = buildString {
            append("Quantity: ")
            append(currentItem.itemQuantity)
        }

        val isPipe = currentItem.itemName?.lowercase()?.contains("pipe") == true
        if (isPipe) {
            holder.binding.pipesLength.visibility = ViewGroup.VISIBLE
            holder.binding.pipesLength.text = buildString {
                append("Length: ")
                append(currentItem.pipeLength)
                append(" feet")
            }
        } else {
            holder.binding.pipesLength.visibility = ViewGroup.GONE
        }

        holder.binding.delItem.setOnClickListener {
            dellInventoryItem(position)
        }
    }

    private fun dellInventoryItem(position: Int) {
        val itemToDelete = itemList[position]
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Delete ${itemToDelete.itemName}")
            .setBackground(ContextCompat.getDrawable(context, R.drawable.msg_view_received))
            .setMessage("Do you want to remove the item from cart?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Remove item from database
                itemToDelete.itemId?.let {
                    clientId?.let { cId ->
                        Firebase.database.reference.child("Cart").child(cId).child(it)
                            .removeValue()
                            .addOnSuccessListener {
                                // Remove the item from the list
                                itemList.remove(itemToDelete)
                                notifyItemRemoved(position)

                                undoDeleteItem(position, itemToDelete)
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alertDialog = builder.create()
        alertDialog.apply {
            show()
            // Set title text color
            val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
            titleView?.setTextColor(Color.BLACK)
            // Set message text color
            findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
            // Set button color
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    private fun undoDeleteItem(position: Int, itemToDelete: CartModel) {
        val snackbar: Snackbar = Snackbar.make((context as Activity).findViewById(android.R.id.content),
            "Item removed from cart", Snackbar.LENGTH_LONG)
        snackbar.setAction("UNDO") {
            itemList.add(position, itemToDelete)
            notifyItemInserted(position)
            itemToDelete.itemId?.let { it ->
                clientId?.let { cId ->
                    Firebase.database.reference.child("Cart").child(cId).child(it)
                        .setValue(itemToDelete) // Add it back to the database
                }
            }
        }
        snackbar.show()
        snackbar.setTextColor(Color.BLACK)
        snackbar.setActionTextColor(Color.BLUE)
        snackbar.setBackgroundTint(Color.WHITE)
    }

    fun updateList(newItems: ArrayList<CartModel>) {
        itemList = newItems
        notifyDataSetChanged()
    }
}
