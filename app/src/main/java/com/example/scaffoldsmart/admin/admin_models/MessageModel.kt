package com.example.scaffoldsmart.admin.admin_models

class MessageModel {
    var senderId: String? = null
    var message: String? = null
    var timestamp: Long? = null
    var messageId: String? = null
    var imageUri: String? = null
    var senderName: String? = null
    var seen: Boolean? = null

    constructor() {}

    constructor(
        senderId: String?,
        message: String?,
        timestamp: Long?,
        senderName: String?,
        messageId: String?,
        seen: Boolean?
    ) {
        this.senderId = senderId
        this.message = message
        this.timestamp = timestamp
        this.senderName = senderName
        this.messageId = messageId
        this.seen = seen
    }

    // Copy method to create a new instance of Message
    fun copy(): MessageModel {
        return MessageModel(
            senderId = this.senderId,
            message = this.message,
            timestamp = this.timestamp,
            senderName = this.senderName,
            messageId = this.messageId,
            //imageUri = this.imageUri,
            seen = this.seen
        )
    }
}
