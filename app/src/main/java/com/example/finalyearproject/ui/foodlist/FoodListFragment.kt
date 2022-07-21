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
import com.example.finalyearproject.models.FoodItemModel2
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_food_list.view.*
import java.util.*


class FoodListFragment : Fragment() {

    private lateinit var database: DatabaseReference



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //Database
        //get arguments
        database = Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val args = arguments
        val myBundle: FoodItemModel2? = args?.getParcelable("foodItemBundle")

        var foodItemName: String
        var foodItemExpirationDate: String


        val binding:

        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_food_list, container, false)

        //Create info fields for adding data to the dtb
        val edit_name: EditText = view.findViewById(R.id.FoodItem_EditText)
        val edit_date: EditText = view.findViewById(R.id.FoodItemDate_EditText)
        val btn : Button = view.findViewById(R.id.Dtb_Button) //todo dont need this, why?
        //database.child("Andrei").child("2").setValue(FoodItemModel2("Ciuperci","macaroane"))
        view.Dtb_Button.setOnClickListener {

            // Write a message to the database
           // if (myBundle != null) {  //Need this if i push the bundle from main activity
                foodItemName = edit_name.text.toString()
                foodItemExpirationDate = edit_date.text.toString()
                addFoodItem(foodItemName, foodItemExpirationDate)


            Toast.makeText(this.context,"Added to Database", Toast.LENGTH_SHORT).show()

        }

        val dateTextView: TextView = view.findViewById(R.id.displayDate_textView)
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val currentDateandTime = sdf.format(Date())
        dateTextView.text = currentDateandTime

        view.food_recyclerview.layoutManager = LinearLayoutManager(activity)
        view.food_recyclerview.adapter = FoodListRecyclerAdapter()         //TODO Make card dissaper 3 seconds after clicked(also include cancel if clicked again)

        return view
    }

    private fun addFoodItem(foodItemName: String, foodItemExpirationDate: String) {
        //create a random string id
        val random = getRandomString(10)
        Log.d("random generated is ", random.toString())
        //push value to firebase
        val item = FoodItemModel2(foodItemName, foodItemExpirationDate)
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

}