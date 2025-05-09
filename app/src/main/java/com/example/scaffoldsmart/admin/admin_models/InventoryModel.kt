package com.example.scaffoldsmart.admin.admin_models

import java.io.Serializable

class InventoryModel : Serializable {
    var itemId: String? = null
    var itemName: String? = null
    var price: Int? = null
    var quantity: Int? = null
    var availability: String? = null
    var threshold: Int? = null

    constructor()

    constructor(itemId: String?, itemName: String?, price: Int?, quantity: Int?, availability: String?) {
        this.itemId = itemId
        this.itemName = itemName
        this.price = price
        this.quantity = quantity
        this.availability = availability
    }
}
