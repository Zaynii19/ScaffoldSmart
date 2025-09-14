package com.example.scaffoldsmart.admin.admin_models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*class RentalModel : Serializable {
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
    var status: String? = null
    var rent: Int? = null
    var rentStatus: String? = null
    // List of Rental Items
    var items: ArrayList<RentalItem>? = null

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
        rent: Int?,
        status: String?,
        items: ArrayList<RentalItem>?
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
        this.rent = rent
        this.status = status
        this.items = items
    }
}

class RentalItem {
    var itemName: String? = null
    var itemQuantity: Int? = null
    var itemPrice: Int? = null
    var pipeLength: Int? = null

    constructor()

    constructor(
        itemName: String?,
        itemQuantity: Int?,
        itemPrice: Int?,
        pipeLength: Int?
    ) {
        this.itemName = itemName
        this.itemQuantity = itemQuantity
        this.itemPrice = itemPrice
        this.pipeLength = pipeLength
    }
}*/

@Parcelize
data class RentalModel(
    var clientID: String? = null,
    var rentalId: String? = null,
    var clientName: String? = null,
    var clientEmail: String? = null,
    var clientCnic: String? = null,
    var clientPhone: String? = null,
    var clientAddress: String? = null,
    var rentalAddress: String? = null,
    var startDuration: String? = null,
    var endDuration: String? = null,
    var status: String? = null,
    var rent: Int? = null,
    var rentStatus: String? = null,
    var items: ArrayList<RentalItem>? = null
) : Parcelable

@Parcelize
data class RentalItem(
    var itemName: String? = null,
    var itemQuantity: Int? = null,
    var itemPrice: Int? = null,
    var pipeLength: Int? = null
) : Parcelable
