package com.example.finalyearproject.ui.foodlist


import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalyearproject.adapters.FoodListRecyclerAdapter
import com.example.finalyearproject.R
import com.example.finalyearproject.models.FoodItemModel

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_food_list.view.*
import java.util.*



class FoodListFragment : Fragment() {


    private lateinit var database: DatabaseReference
    private lateinit var mView: View
    private var foodList: MutableList<FoodItemModel> = ArrayList<FoodItemModel>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //Database
        //get arguments
        database = Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app").reference


        var foodItemName: String
        var foodItemExpirationDate: String

        // Inflate the layout for this fragment

        mView =  inflater.inflate(R.layout.fragment_food_list, container, false)

        //Create info fields for adding data to the dtb
        val edit_name: EditText = mView.findViewById(R.id.FoodItem_EditText)
        val edit_date: EditText = mView.findViewById(R.id.FoodItemDate_EditText)
        val btn : Button = mView.findViewById(R.id.Dtb_Button)
        //database.child("Andrei").child("2").setValue(FoodItemModel("Ciuperci","macaroane"))
        btn.setOnClickListener {

            // Write a message to the database
           // if (myBundle != null) {  //Need this if i push the bundle from main activity
                foodItemName = edit_name.text.toString()
                foodItemExpirationDate = edit_date.text.toString()
                addFoodItem(foodItemName, foodItemExpirationDate)


            Toast.makeText(this.context,"Added to Database", Toast.LENGTH_SHORT).show()

        }

        setDate()
        readDatabase()


        return mView
    }

    private fun addFoodItem(foodItemName: String, foodItemExpirationDate: String) {
        //create a random string id
        val random = getRandomString(10)
        Log.d("random generated is ", random.toString())
        //push value to firebase
        val item = FoodItemModel(foodItemName, foodItemExpirationDate)
        database.child("foodItems").child(random).setValue(item)
    }

    //Random String for the FoodItemId
    private fun getRandomString(length: Int): String {
        val sb = StringBuilder(length)
        val alphabet = "asdfgfhjklqwertyuiopzxcvbnmASDFGHJKLZXCVBNMQWERTYUIOP1234567890"
        val rand = Random()

        for (i in 0 until sb.capacity()) {
            val index = rand.nextInt(alphabet.length)
            sb.append(alphabet[index])
        }
        return sb.toString()
    }

    private fun setupRecyclerView() {
        database  = Firebase.database.reference
        // read data
        readDatabase()
        mView.food_recyclerview.layoutManager = LinearLayoutManager(activity)
        //Log.d("lista", foodList.toString())
        mView.food_recyclerview.adapter = FoodListRecyclerAdapter(foodList)         //TODO Make card dissaper 3 seconds after clicked(also include cancel if clicked again)
    }

    private fun readDatabase() {

        database.child("foodItems").get().addOnSuccessListener {
            //Log.d("lista json", it.toString())
            for (el in it.children) {
                val expirationDate = el.child("itemExpirationDate").value.toString()
                val name = el.child("itemName").value.toString()
                foodList.add(FoodItemModel(name,expirationDate))
                Log.d("ITEM LIST", expirationDate + name)
            }

            mView.food_recyclerview.layoutManager = LinearLayoutManager(activity)
           
            mView.food_recyclerview.adapter = FoodListRecyclerAdapter(foodList)         //TODO Make card dissaper 3 seconds after clicked(also include cancel if clicked again)

        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }

    }


    private fun setDate() {
        val dateTextView: TextView = mView.findViewById(R.id.displayDate_textView)
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val currentDateandTime = sdf.format(Date())
        dateTextView.text = currentDateandTime
    }

}