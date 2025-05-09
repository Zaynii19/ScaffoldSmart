package com.example.scaffoldsmart.admin.admin_models

class AdminModel {
    var userType: String? = null
    var id: String? = null
    var name: String? = null
    var email: String? = null
    var pass: String? = null
    var company: String? = null
    var address: String? = null
    var phone: String? = null

    constructor()

    constructor(
        userType:String?,
        id: String?,
        name: String?,
        email: String?,
        pass: String?,
        company: String?,
        address: String?,
        phone: String?
    ) {
        this.userType = userType
        this.id = id
        this.name = name
        this.email = email
        this.pass = pass
        this.company = company
        this.address = address
        this.phone = phone
    }
}