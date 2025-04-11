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
    var pipes = 0
    var pipesLength = 0
    var joints = 0
    var wench = 0
    var motors = 0
    var pumps = 0
    var generators = 0
    var wheel = 0
    var status = ""
    var rent = 0
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
        pipes: Int,
        pipesLength: Int,
        joints: Int,
        wench: Int,
        motors: Int,
        pumps: Int,
        generators: Int,
        wheel: Int,
        rent: Int,
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
    }
}
