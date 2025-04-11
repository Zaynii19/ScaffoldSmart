package com.example.scaffoldsmart.admin.admin_models

import java.io.Serializable

class InventoryModel : Serializable {
    var itemId = ""
    var itemName = ""
    var price = 0
    var quantity = 0
    var availability = ""
    var threshold = 0

    constructor()

    constructor(itemId: String, itemName: String, price: Int, quantity: Int, availability: String) {
        this.itemId = itemId
        this.itemName = itemName
        this.price = price
        this.quantity = quantity
        this.availability = availability
    }
}
