package com.example.finalyearproject.models
//TODO not used atm, using the other FoodItemModel
import com.google.gson.annotations.SerializedName

data class FoodItemModel (
    @SerializedName("name")
    var name: String,
    @SerializedName("date")
    var date: String, /*TODO might need to change this to date format*/
//    @SerializedName("barcode")
//    val barcode: String

)