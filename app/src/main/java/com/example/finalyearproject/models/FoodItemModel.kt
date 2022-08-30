package com.example.finalyearproject.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FoodItemModel (

    var itemName: String,

    var itemExpirationDate: String, /*TODO might need to change this to date format*/

    var key: String,

    var toDonate: Boolean

//    val barcode: String

): Parcelable