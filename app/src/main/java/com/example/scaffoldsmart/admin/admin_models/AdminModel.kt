package com.example.scaffoldsmart.admin.admin_models

class AdminModel {
    var id = ""
    var name = ""
    var email =  ""
    var company =  ""
    var address = ""
    var phone = ""

    constructor()

    constructor(id: String, name: String, email: String, company: String, address: String, phone: String) {
        this.id = id
        this.name = name
        this.email = email
        this.company = company
        this.address = address
        this.phone = phone
    }
}