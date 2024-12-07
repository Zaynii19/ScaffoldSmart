package com.example.scaffoldsmart.client.client_models

class ClientModel {
    var id = ""
    var firstName = ""
    var lastName = ""
    var email = ""
    var pass = ""

    constructor()

    constructor(id: String, firstName: String, lastName: String, email: String, pass: String) {
        this.id = id
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.pass = pass
    }
}