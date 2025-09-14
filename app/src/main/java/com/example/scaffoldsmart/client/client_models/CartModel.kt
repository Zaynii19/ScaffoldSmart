package com.example.scaffoldsmart.client.client_models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
class CartModel {
    var itemId: String? = null
    var itemName: String? = null
    var itemQuantity: Int? = null
    var itemPrice: Int? = null
    var pipeLength: Int? = null

    constructor()

    constructor(
        itemId: String?,
        itemName: String?,
        itemQuantity: Int?,
        itemPrice: Int?,
        pipeLength: Int?
    ) {
        this.itemId = itemId
        this.itemName = itemName
        this.itemQuantity = itemQuantity
        this.itemPrice = itemPrice
        this.pipeLength = pipeLength
    }
}*/


@Parcelize
data class CartModel(
    var itemId: String? = null,
    var itemName: String? = null,
    var itemQuantity: Int? = null,
    var itemPrice: Int? = null,
    var pipeLength: Int? = null
) : Parcelable