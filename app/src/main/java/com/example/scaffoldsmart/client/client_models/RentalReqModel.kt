package com.example.scaffoldsmart.client.client_models

class RentalReqModel{
    var rentalId = ""
    var clientName = ""
    var clientEmail = ""
    var rentalAddress = ""
    var clientCnic = ""
    var clientPhone = ""
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

    constructor()

    constructor(
        rentalId: String,
        clientName: String,
        clientEmail: String,
        rentalAddress: String,
        clientCnic: String,
        clientPhone: String,
        startDuration: String,
        endDuration: String,
        pipes: String,
        pipesLength: String,
        joints: String,
        wench: String,
        motors: String,
        pumps: String,
        generators: String,
        wheel: String
    ) {
        this.rentalId = rentalId
        this.clientName = clientName
        this.clientEmail = clientEmail
        this.rentalAddress = rentalAddress
        this.clientCnic = clientCnic
        this.clientPhone = clientPhone
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
    }
}
