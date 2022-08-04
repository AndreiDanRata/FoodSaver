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


    private lateinit var database: DatabaseReference
    private lateinit var mView: View
    private var foodList: MutableList<FoodItemModel> = ArrayList<FoodItemModel>()
    private var mAdapter = FoodListRecyclerAdapter(foodList)

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
        //deleteFoodItem()


        return mView
    }

    private fun fetchAPIData(barcode : String): Thread
    {
        return Thread {
            val url = URL(Constants.BARCODE_BASE_URL + barcode + "?apikey=" + Constants.BARCODE_API_KEY)
            val connection = url.openConnection() as HttpsURLConnection

            if(connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val request = Gson().fromJson(inputStreamReader, Request::class.java)

                addFoodItem(request.title, SimpleDateFormat("dd.MM.yyyy").format(Date()))  //TODO ADD ITEMEXPIRATION DATE INSTEAD OF CURRENT DAY

                inputStreamReader.close()
                inputSystem.close()
                Log.d("API_RESPONSE", request.description)
            }
            else {


                /*Toast.makeText(  //TODO IF THE BARCODE DOES NOT EXIST... THE APP QUITS OR THROWS A WEIRD ERROR....
                    context,
                    "Failed Connection...Please check your internet connection.",
                    Toast.LENGTH_SHORT
                )
                    .show()*/
            }
        }
    }

    public fun deleteFoodItem() {


        val delete_btn: Button = mView.findViewById(R.id.delete_button)

        val position = mView.food_recyclerview.getChildAdapterPosition(delete_btn)
       // mView.food_recyclerview.get


        delete_btn.setOnClickListener{
            foodList.removeAt(position)//FoodItemModel())
            mAdapter.notifyItemRemoved(position)

           // database.child("foodItems").child()
        }
    }

    private fun addFoodItem(itemName: String, itemExpirationDate: String) {
        //create a random string id
        val random = getRandomString(10)
        Log.d("random generated is ", random.toString())

        //push value to firebase
        //val item = FoodItemModel(foodItemName, foodItemExpirationDate) //TODO Does not work because: the key in the database(itemExpirationDate/itemName) is different from name/date (Model class fields)
        database.child("foodItems").child(random).child("itemExpirationDate").setValue(itemExpirationDate)
        database.child("foodItems").child(random).child("itemName").setValue(itemName)
        //database.child("foodItems").child(random).child("key").setValue(random)
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
                //val key = el.child("key").value.toString()
                foodList.add(FoodItemModel(name,expirationDate))
                Log.d("ITEM LIST", expirationDate + name)
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