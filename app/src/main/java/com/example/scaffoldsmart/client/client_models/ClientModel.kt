package com.example.scaffoldsmart.client.client_models

class ClientModel {
    var userType = ""
    var id = ""
    var name = ""
    var email = ""
    var pass = ""
    var cnic  = ""
    var address = ""
    var phone = ""

    constructor()

    constructor(userType: String, id: String, name: String, email: String, pass: String, cnic: String, address: String, phone: String) {
        this.userType = userType
        this.id = id
        this.name = name
        this.email = email
        this.pass = pass
        this.cnic = cnic
        this.address = address
        this.phone = phone
    }
}