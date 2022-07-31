package com.example.finalyearproject

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.finalyearproject.models.FoodItemModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

//imports data from firebase to project -> use this in FoodItemsViewModel
class FoodItemsRepository {

    val database = Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app/")
    val FoodListReference = database.getReference("foodItems")



    fun fetchFoodList(liveData: MutableLiveData<List<FoodItemModel>>) {
        FoodListReference
            .orderByChild("itemExpirationDate") //TODO order by the expiration date
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("SNAPSHOT", snapshot.value.toString())

                val foodItems: List<FoodItemModel> = snapshot.children.map { dataSnapshot ->  
                    dataSnapshot.getValue(FoodItemModel::class.java)!!
                }
                //FOR TESTING PURPOSE: Log.i("ITEMS", foodItems.toString())
                //liveData.postValue(foodItems)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

   /* fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        newsFeedReference.child(id).child("favorite").setValue(isFavorite)
    }*/
}