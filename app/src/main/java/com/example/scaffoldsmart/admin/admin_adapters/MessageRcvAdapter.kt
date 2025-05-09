package com.example.scaffoldsmart.admin.admin_adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_models.DateHeader
import com.example.scaffoldsmart.admin.admin_models.MessageModel
import com.example.scaffoldsmart.databinding.DateHeaderItemBinding
import com.example.scaffoldsmart.databinding.DeleteMessageDialogBinding
import com.example.scaffoldsmart.databinding.ReceiveMessageBinding
import com.example.scaffoldsmart.databinding.SendMessageBinding
import com.example.scaffoldsmart.util.DateFormater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase

class MessageRcvAdapter(
    val context: Context,
    private val messages: ArrayList<Any>?, // Hold both Message and DateHeader
    private val senderRoom: String?,
    private val receiverRoom: String?,
    private val senderUid: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val ITEM_DATE_HEADER = 3

    inner class SendMsgViewHolder(val bindingS: SendMessageBinding) : RecyclerView.ViewHolder(bindingS.root)
    inner class ReceiveMsgViewHolder(val bindingR: ReceiveMessageBinding) : RecyclerView.ViewHolder(bindingR.root)
    inner class DateHeaderViewHolder(val bindingD: DateHeaderItemBinding) : RecyclerView.ViewHolder(bindingD.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_SENT -> SendMsgViewHolder(SendMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            ITEM_RECEIVE -> ReceiveMsgViewHolder(ReceiveMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            ITEM_DATE_HEADER -> DateHeaderViewHolder(DateHeaderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages?.get(position)) {
            is MessageModel -> if (senderUid == (messages[position] as MessageModel).senderId) ITEM_SENT else ITEM_RECEIVE
            is DateHeader -> ITEM_DATE_HEADER
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    override fun getItemCount(): Int = messages?.size ?: 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentItem = messages?.get(position)) {
            is MessageModel -> {
                if (holder is SendMsgViewHolder) {
                    bindSentMessage(holder, currentItem)
                } else if (holder is ReceiveMsgViewHolder) {
                    bindReceivedMessage(holder, currentItem)
                }
            }
            is DateHeader -> {
                if (holder is DateHeaderViewHolder) {
                    holder.bindingD.dateHeaderTextView.text = currentItem.date
                }
            }
        }
    }

    private fun bindSentMessage(holder: SendMsgViewHolder, currentMessageModel: MessageModel) {
        // Reset visibility for all views
        holder.bindingS.sendImage.visibility = View.GONE
        holder.bindingS.lastSendImgMessageTime.visibility = View.GONE
        holder.bindingS.sendImgMessageSeen.visibility = View.GONE
        holder.bindingS.sendMessage.visibility = View.VISIBLE
        holder.bindingS.lastSendMessageTime.visibility = View.VISIBLE
        holder.bindingS.sendMessageSeen.visibility = View.GONE

        if (currentMessageModel.message == "photo") {
            showImageSendMessage(
                holder.bindingS.sendImage,
                holder.bindingS.lastSendImgMessageTime,
                holder.bindingS.sendImgMessageSeen,
                holder.bindingS.sendMessage,
                holder.bindingS.lastSendMessageTime,
                holder.bindingS.sendMessageSeen,
                currentMessageModel.imageUri,
                currentMessageModel.timestamp,
                currentMessageModel.seen
            )
        } else {
            holder.bindingS.sendMessage.text = currentMessageModel.message
        }
        holder.bindingS.sendMessageSeen.visibility = if (currentMessageModel.seen!! && currentMessageModel.message != "photo") View.VISIBLE else View.GONE
        holder.bindingS.lastSendMessageTime.text = DateFormater.formatTimestampForMsg(currentMessageModel.timestamp)
        holder.bindingS.root.setOnLongClickListener { showDeleteDialog(currentMessageModel, true) }
    }

    private fun bindReceivedMessage(holder: ReceiveMsgViewHolder, currentMessageModel: MessageModel) {
        // Reset visibility for all views
        holder.bindingR.receiveImage.visibility = View.GONE
        holder.bindingR.lastReceiveImgMessageTime.visibility = View.GONE
        holder.bindingR.receiveMessage.visibility = View.VISIBLE
        holder.bindingR.lastReceiveMessageTime.visibility = View.VISIBLE

        if (currentMessageModel.message == "photo") {
            showImageReceiveMessage(
                holder.bindingR.receiveImage,
                holder.bindingR.lastReceiveImgMessageTime,
                holder.bindingR.receiveMessage,
                holder.bindingR.lastReceiveMessageTime,
                currentMessageModel.imageUri,
                currentMessageModel.timestamp
            )
        } else {
            holder.bindingR.receiveMessage.text = currentMessageModel.message
        }
        holder.bindingR.lastReceiveMessageTime.text = DateFormater.formatTimestampForMsg(currentMessageModel.timestamp)
        holder.bindingR.root.setOnLongClickListener { showDeleteDialog(currentMessageModel, false) }
    }

    private fun showImageSendMessage(
        imageView: ImageView,
        imgMessageTime: TextView,
        imageMessageSeen: ImageView,
        textMessage: TextView,
        messageTime: TextView,
        messageSeen: ImageView,
        imageUrl: String?,
        timestamp: Long?,
        seen: Boolean?
    ) {
        imageView.visibility = View.VISIBLE
        imgMessageTime.visibility = View.VISIBLE
        if (seen!!) {
            imageMessageSeen.visibility = View.VISIBLE
        } else {
            imageMessageSeen.visibility = View.GONE
        }
        textMessage.visibility = View.GONE
        messageSeen.visibility = View.GONE
        messageTime.visibility = View.GONE
        Glide.with(context).load(imageUrl).placeholder(R.drawable.image_placeholder).into(imageView)
        imgMessageTime.text = DateFormater.formatTimestampForMsg(timestamp)
    }

    private fun showImageReceiveMessage(
        imageView: ImageView,
        imgMessageTime: TextView,
        textMessage: TextView,
        messageTime: TextView,
        imageUrl: String?,
        timestamp: Long?
    ) {
        imageView.visibility = View.VISIBLE
        imgMessageTime.visibility = View.VISIBLE
        textMessage.visibility = View.GONE
        messageTime.visibility = View.GONE
        Glide.with(context).load(imageUrl).placeholder(R.drawable.image_placeholder).into(imageView)
        imgMessageTime.text = DateFormater.formatTimestampForMsg(timestamp)
    }

    private fun showDeleteDialog(messageModel: MessageModel, isSender: Boolean): Boolean {
        val customDialog = LayoutInflater.from(context).inflate(R.layout.delete_message_dialog, null)
        val binder: DeleteMessageDialogBinding = DeleteMessageDialogBinding.bind(customDialog)
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Delete Message")
            .setView(customDialog)
            .setBackground(ContextCompat.getDrawable(context, R.drawable.msg_view_received))
            .create().apply {
                show()
                // Set title text color
                val titleView = findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                titleView?.setTextColor(Color.BLACK)
            }

        binder.cancel.setOnClickListener { dialog.dismiss() }

        if (isSender) {
            binder.delEveryone.setOnClickListener { deleteForEveryone(messageModel, dialog) }
        } else {
            binder.delEveryone.visibility = View.GONE
        }

        binder.delForMe.setOnClickListener { deleteForMe(messageModel, dialog) }

        return false
    }

    private fun deleteForEveryone(messageModel: MessageModel, dialog: AlertDialog) {
        // Create two message copies for sender and receiver
        val senderMessage = messageModel.copy() //Implemented a copy() method in the Message class
        val receiverMessage = messageModel.copy()

        // Update the message text for sender and receiver
        senderMessage.message = "You deleted this message"
        receiverMessage.message = "This message was deleted"

        senderMessage.messageId?.let {
            // Update message for sender
            senderRoom?.let { sr -> FirebaseDatabase.getInstance().reference.child("Chat").child(sr)
                .child("Messages").child(it).setValue(senderMessage)
            }

            // Update message for receiver
            receiverRoom?.let { rr -> FirebaseDatabase.getInstance().reference.child("Chat").child(rr)
                .child("Messages").child(it).setValue(receiverMessage)
            }
        }

        dialog.dismiss()
    }

    private fun deleteForMe(messageModel: MessageModel, dialog: AlertDialog) {
        messageModel.messageId?.let {
            senderRoom?.let { sr -> FirebaseDatabase.getInstance().reference.child("Chat").child(sr)
                .child("Messages").child(it).setValue(null)
            }
        }
        dialog.dismiss()
    }
}
