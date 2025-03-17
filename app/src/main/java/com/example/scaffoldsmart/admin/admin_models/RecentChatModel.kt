package com.example.scaffoldsmart.admin.admin_models

class RecentChatModel {
    var userName = ""
    var recentMsg = ""
    var recentMsgTime = ""
    var newMsgCount = 0

    constructor()

    constructor(userName: String, recentMsg: String, recentMsgTime: String, newMsgCount: Int) {
        this.userName = userName
        this.recentMsg = recentMsg
        this.recentMsgTime = recentMsgTime
        this.newMsgCount = newMsgCount
    }
}