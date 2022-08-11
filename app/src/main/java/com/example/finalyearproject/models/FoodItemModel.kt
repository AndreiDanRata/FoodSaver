package com.example.finalyearproject.models

data class FoodItemModel (

    var itemName: String,

    var itemExpirationDate: String, /*TODO might need to change this to date format*/

    var key: String,

    var toDonate: Boolean

//    val barcode: String

)