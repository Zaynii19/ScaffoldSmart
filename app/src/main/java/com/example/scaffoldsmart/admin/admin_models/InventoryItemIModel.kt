package com.example.scaffoldsmart.admin.admin_models

import java.io.Serializable

class InventoryItemIModel : Serializable {
    var itemId = ""
    var itemName = ""
    var price = ""
    var quantity = ""
    var availability = ""

    constructor()

    constructor(itemId: String, itemName: String, price: String, quantity: String, availability: String) {
        this.itemId = itemId
        this.itemName = itemName
        this.price = price
        this.quantity = quantity
        this.availability = availability
    }
}
