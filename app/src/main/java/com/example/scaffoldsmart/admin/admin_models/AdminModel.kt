package com.example.scaffoldsmart.admin.admin_models

class AdminModel {
    var userType = ""
    var id = ""
    var name = ""
    var email =  ""
    var pass = ""
    var company =  ""
    var address = ""
    var phone = ""

    constructor()

    constructor(userType:String, id: String, name: String, email: String, pass: String, company: String, address: String, phone: String) {
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