package com.example.scaffoldsmart.admin.admin_models

class ChatUserModel {
    var uid: String? = null
    var userName: String? = null
    var status: String? = null
    var lastSeen: Long? = null
    var lastMsg: String? = null
    var lastMsgTime: Long? = null
    var clientNewMsgCount: Int? = null
    var adminNewMsgCount: Int? = null

    constructor() {}

    constructor(
        uid: String?,
        userName: String?,

    ) {
        this.uid = uid
        this.userName = userName
    }
}