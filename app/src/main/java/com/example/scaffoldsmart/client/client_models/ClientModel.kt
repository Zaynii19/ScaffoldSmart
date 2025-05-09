package com.example.scaffoldsmart.client.client_models

import java.io.Serializable

class ClientModel: Serializable {
    var userType: String? = null
    var id: String? = null
    var name: String? = null
    var email: String? = null
    var pass: String? = null
    var cnic : String? = null
    var address: String? = null
    var phone: String? = null

    constructor()

    constructor(
        userType: String?,
        id: String?,
        name: String?,
        email: String?,
        pass: String?
    ) {
        this.userType = userType
        this.id = id
        this.name = name
        this.email = email
        this.pass = pass
    }
}