package com.example.scaffoldsmart.admin.admin_models

import java.io.Serializable

class RentalModel : Serializable {
    var clientID = ""
    var rentalId = ""
    var clientName = ""
    var clientEmail = ""
    var clientCnic = ""
    var clientPhone = ""
    var clientAddress = ""
    var rentalAddress = ""
    var startDuration = ""
    var endDuration = ""
    var pipes = ""
    var pipesLength = ""
    var joints = ""
    var wench = ""
    var motors = ""
    var pumps = ""
    var generators = ""
    var wheel = ""
    var status = ""
    var rent = ""
    var rentStatus = ""

    constructor()

    constructor(
        clientID: String,
        rentalId: String,
        clientName: String,
        clientEmail: String,
        clientCnic: String,
        clientPhone: String,
        clientAddress: String,
        rentalAddress: String,
        startDuration: String,
        endDuration: String,
        pipes: String,
        pipesLength: String,
        joints: String,
        wench: String,
        motors: String,
        pumps: String,
        generators: String,
        wheel: String,
        status: String,
        rent: String,
        rentStatus: String
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
        this.status = status
        this.rent = rent
        this.rentStatus = rentStatus
    }
}
