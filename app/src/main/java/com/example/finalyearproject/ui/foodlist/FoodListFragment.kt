package com.example.finalyearproject.ui.foodlist


import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalyearproject.*
import com.example.finalyearproject.adapters.FoodListRecyclerAdapter
import com.example.finalyearproject.barcodescanner.BarcodeScannerActivity
import com.example.finalyearproject.barcodescanner.BarcodesList
import com.example.finalyearproject.models.FoodItemModel
import com.example.finalyearproject.util.Constants
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_food_list.view.*
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection


class FoodListFragment : Fragment() {


    private var database: DatabaseReference = Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app").reference
    private lateinit var mView: View
    private var foodList: MutableList<FoodItemModel> = ArrayList<FoodItemModel>()
    private var mAdapter = FoodListRecyclerAdapter(foodList,database)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        for(code in BarcodesList.barcodes) {
            fetchAPIData(code).start()
            BarcodesList.barcodes.remove(code)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        var foodItemName: String
        var foodItemExpirationDate: String

        // Inflate the layout for this fragment

        mView =  inflater.inflate(R.layout.fragment_food_list, container, false)

        //Create info fields for adding data to the dtb
        val edit_name: EditText = mView.findViewById(R.id.FoodItem_EditText)
        val edit_date: EditText = mView.findViewById(R.id.FoodItemDate_EditText)
        val btn : Button = mView.findViewById(R.id.Dtb_Button)
        btn.setOnClickListener {

            // Write a message to the database
                foodItemName = edit_name.text.toString()
                foodItemExpirationDate = edit_date.text.toString()
                addFoodItem(foodItemName, foodItemExpirationDate)


            Toast.makeText(this.context,"Added to Database", Toast.LENGTH_SHORT).show()

        }


        camera_fab()
        readDatabase()
        setDate()


        return mView
    }

    private fun fetchAPIData(barcode : String): Thread
    {
        return Thread {
            val itemExpirationDate = SimpleDateFormat("dd/MM/yyyy").format(Date())  //TODO ADD ITEMEXPIRATION DATE INSTEAD OF CURRENT DAY

            try {
                val url = URL(Constants.BARCODE_BASE_URL + barcode + "?apikey=" + Constants.BARCODE_API_KEY)
                val connection = url.openConnection() as HttpsURLConnection
                Log.d("CONNECTION_RESPONSE_CODE",connection.responseCode.toString())
                if(connection.responseCode == 200) {
                    val inputSystem = connection.inputStream
                    val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                    val request = Gson().fromJson(inputStreamReader, Request::class.java)
                    if(request.title != "") {
                        addFoodItem(request.title, itemExpirationDate)
                    }
                    else if(request.description != ""){
                        addFoodItem(request.description, itemExpirationDate)
                    }

                    inputStreamReader.close()
                    inputSystem.close()
                    Log.d("API_RESPONSE", request.description + request.title)
                }
                else {
                    Log.d("NO_CODE_API","")

                    /*Toast.makeText(  //TODO IF THE BARCODE DOES NOT EXIST... THE APP SOMETIMES RESTARTS OR THROWS A WEIRD ERROR.... IT DOES NOT ENTER THIS BRANCH CUZ THE RESPONSE CODE IS STILL 200
                        context,
                        "Failed Connection...Please check your internet connection.",
                        Toast.LENGTH_SHORT
                    )
                        .show()*/
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }



    private fun addFoodItem(itemName: String, itemExpirationDate: String) {
        //create a random string id
        val random_key = getRandomString(10)
        Log.d("random generated is ", random_key.toString())

        if(itemName!= "" && itemExpirationDate!= "") {
            //push value to firebase
            val item = FoodItemModel(itemName, itemExpirationDate,random_key)
            database.child("foodItems").child(random_key).setValue(item)
        }
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
        //readDatabase()
        mView.food_recyclerview.layoutManager = LinearLayoutManager(activity)
        //Log.d("lista", foodList.toString())
        mView.food_recyclerview.adapter = mAdapter        //TODO Make card dissaper 3 seconds after clicked(also include cancel if clicked again)
    }

    private fun camera_fab() {
        val fab: View = mView.findViewById(R.id.Camera_Fab)
        fab.setOnClickListener { view ->
            /*Snackbar.make(view, "Snackbar", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()*/


            startActivity(Intent(activity, BarcodeScannerActivity::class.java))
        }
    }

    private fun readDatabase() {

        database.child("foodItems").get().addOnSuccessListener {
            //Log.d("lista json", it.toString())
            for (el in it.children) {
                val expirationDate = el.child("itemExpirationDate").value.toString()
                val name = el.child("itemName").value.toString()
                val key = el.child("key").value.toString()
                //val key = el.child("key").value.toString()
                foodList.add(FoodItemModel(name,expirationDate,key))
                Log.d("ITEM LIST", "$expirationDate $name $key")
            }

            setupRecyclerView()

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