package com.example.scaffoldsmart.admin.admin_models

import java.io.Serializable

class RentalModel : Serializable {
    var clientID: String? = null
    var rentalId: String? = null
    var clientName: String? = null
    var clientEmail: String? = null
    var clientCnic: String? = null
    var clientPhone: String? = null
    var clientAddress: String? = null
    var rentalAddress: String? = null
    var startDuration: String? = null
    var endDuration: String? = null
    var pipes: Int? = null
    var pipesLength: Int? = null
    var joints: Int? = null
    var wench: Int? = null
    var motors: Int? = null
    var pumps: Int? = null
    var generators: Int? = null
    var wheel: Int? = null
    var status: String? = null
    var rent: Int? = null
    var rentStatus: String? = null

    constructor()

    constructor(
        clientID: String?,
        rentalId: String?,
        clientName: String?,
        clientEmail: String?,
        clientCnic: String?,
        clientPhone: String?,
        clientAddress: String?,
        rentalAddress: String?,
        startDuration: String?,
        endDuration: String?,
        pipes: Int?,
        pipesLength: Int?,
        joints: Int?,
        wench: Int?,
        motors: Int?,
        pumps: Int?,
        generators: Int?,
        wheel: Int?,
        rent: Int?,
        status: String?
    ) {
        this.clientID = clientID
        this.rentalId = rentalId
        this.clientName = clientName
        this.clientEmail = clientEmail
        this.clientCnic = clientCnic
        this.clientPhone = clientPhone
        this.clientAddress = clientAddress
        this.rentalAddress = rentalAddress
        this.startDuration = startDuration
        this.endDuration = endDuration
        this.pipes = pipes
        this.pipesLength = pipesLength
        this.joints = joints
        this.wench = wench
        this.motors = motors
        this.pumps = pumps
        this.generators = generators
        this.wheel = wheel
        this.rent = rent
        this.status = status
    }
}
