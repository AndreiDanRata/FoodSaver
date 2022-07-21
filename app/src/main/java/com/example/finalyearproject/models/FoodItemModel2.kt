
package com.example.finalyearproject.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FoodItemModel2(
    val ItemName: String?,
    val ItemExpirationDate: String?   //TODO CHANGE TO DATE FORMAT FOR INPUT SANITIZATION
) :
    Parcelable {}

/*
package com.example.finalyearproject.models

import android.annotation.SuppressLint

class FoodItemModel2{

    private var itemId: String?
        get() {
            return itemId
        }
        set(value) {}
    private var itemName: String?
        get() {
            return itemName
        }
        set(value) {}
    private var itemExpirationDate: String?
        get() {
            return itemExpirationDate
        }
        set(value) {}

    public fun FoodItemModel2(){}


    public fun FoodItemModel2(itemId: String, itemName: String, itemExpirationDate: String) {
        this.itemId = itemId
        this.itemName = itemName
        this.itemExpirationDate = itemExpirationDate
    }

}*/
