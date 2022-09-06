package com.example.finalyearproject.ui.foodlist


import android.app.DatePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.example.finalyearproject.R
import com.example.finalyearproject.adapters.FoodListRecyclerAdapter
import com.example.finalyearproject.barcodescanner.BarcodeScannerActivity
import com.example.finalyearproject.models.FoodItemModel
import com.example.finalyearproject.util.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_food_list.view.*
import java.util.*


/**
 * Class responsible for the food list fragment
 * */

class FoodListFragment : Fragment() {
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var database: DatabaseReference = Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app").reference
    private var userFirebaseUID: String = FirebaseAuth.getInstance().currentUser!!.uid

    private lateinit var mView: View

    private var foodList: MutableList<FoodItemModel> = ArrayList<FoodItemModel>()
    private var mAdapter = FoodListRecyclerAdapter(foodList, database, this, userFirebaseUID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment

        mView = inflater.inflate(R.layout.fragment_food_list, container, false)


        //Create info fields for adding data to the dtb
        val edit_name: EditText = mView.findViewById(R.id.FoodItem_EditText)
        val edit_date: EditText = mView.findViewById(R.id.FoodItemDate_EditText)
        val btn: Button = mView.findViewById(R.id.Dtb_Button)

        edit_date.showSoftInputOnFocus = false
        edit_date.setOnClickListener{

            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->

                    val dat = (dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
                    edit_date.setText(dat)
                },

                // pass year, month and day for the selected date in our date picker.
                year,
                month,
                day
            )
            datePickerDialog.show()
        }


        btn.setOnClickListener {
            // Add an item to the database

            var foodItemName = edit_name.text.toString()
            var foodItemExpirationDate = edit_date.text.toString()

            if (foodItemName == "") {
                Toast.makeText(this.context, "Failed...Check again your item name", Toast.LENGTH_LONG).show()
            } else {
                if (foodItemExpirationDate == "") {
                    Toast.makeText(this.context, "Failed...Pick an expiration date", Toast.LENGTH_LONG).show()
                }  else {

                    Utils.addFoodItem(foodItemName, foodItemExpirationDate)
                    mAdapter.addItem(FoodItemModel(foodItemName,foodItemExpirationDate,"0",false))
                    Toast.makeText(this.context, "Added to Database", Toast.LENGTH_LONG).show()
                }
            }
            edit_name.text.clear()
            edit_date.text.clear()
        }


        //Refresh recyclerView
        swipeRefreshLayout = mView.findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener{


            Handler().postDelayed(Runnable {
                foodList.clear()
                readDatabase()
//                val maAdapter = FoodListRecyclerAdapter(foodList, database, this, userFirebaseUID)
//                mView.food_recyclerview.adapter = maAdapter
                swipeRefreshLayout.isRefreshing = false
            }, 2000)
        }


        camera_fab()
        readDatabase()
        addBottomSheet()
        changeToDonations()
        setDate()


        return mView
    }


    private fun setupRecyclerView() {
        mView.food_recyclerview.layoutManager = LinearLayoutManager(activity)
        mView.food_recyclerview.hasFixedSize()
        mView.food_recyclerview.adapter = mAdapter
    }

    private fun camera_fab() {
        val fab: View = mView.findViewById(R.id.Camera_Fab)
        fab.setOnClickListener { view ->
            startActivity(Intent(activity, BarcodeScannerActivity::class.java))
        }
    }

    private fun readDatabase() {

        database.child("foodItems").child(userFirebaseUID).get().addOnSuccessListener {
            //Log.d("list json", it.toString())
            for (el in it.children) {
                val expirationDate = el.child("itemExpirationDate").value.toString()
                val name = el.child("itemName").value.toString()
                val key = el.child("key").value.toString()
                val donation = el.child("toDonate").value.toString().toBoolean()

                foodList.add(FoodItemModel(name, expirationDate, key,donation))
                //Log.d("ITEM LIST", "$expirationDate $name $key")
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

    private fun addBottomSheet() {

        mAdapter.onItemClick = {
                foodItemModel, position ->


            val key = foodItemModel.key
            var itemName = foodItemModel.itemName
            var expiration = foodItemModel.itemExpirationDate
            var toDonate = foodItemModel.toDonate

            val dialog = BottomSheetDialog(requireActivity())

            val view = layoutInflater.inflate(R.layout.fragment_details_bottom_sheet, null)

            val nameEdit = view.findViewById<EditText>(R.id.name_EditText)
            nameEdit.setText(itemName)

            nameEdit.doAfterTextChanged {
                itemName = nameEdit.text.toString()
                database
                    .child("foodItems").child(userFirebaseUID).child(key).child("itemName").setValue(itemName)
                //UPDATE RECYCLER VIEW ON ITEM CHANGE
                mAdapter.updateItem(position,FoodItemModel(itemName, expiration, key,toDonate))
            }

            //dialog.dismiss()    //for closing the bottomsheet



            //CALENDAR
            val datepicker = view.findViewById<DatePicker>(R.id.datePicker1)
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            cal.time = sdf.parse(foodItemModel.itemExpirationDate)      //works with this format: "14/08/2011"

            datepicker.init(
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            { _, year, month, day ->            //this adds code for when you select a date
                //SAVE TO DATABASE
                val month = month +1
                expiration = "$day/$month/$year"
                database
                    .child("foodItems").child(userFirebaseUID).child(key)
                    .child("itemExpirationDate").setValue(expiration)

                //UPDATE RECYCLER VIEW ON ITEM CHANGE
                mAdapter.updateItem(position,FoodItemModel(itemName, expiration, key, toDonate))
            }


            dialog.setCancelable(true)
            dialog.setCanceledOnTouchOutside(true)
            dialog.setContentView(view)
            dialog.show()

        }
    }
    private fun changeToDonations(){
        mAdapter.onItemLongClick = { foodItemModel, position ->

            val key = foodItemModel.key
            var itemName = foodItemModel.itemName
            var expiration = foodItemModel.itemExpirationDate
            var toDonate = foodItemModel.toDonate

            if(!toDonate) {

                toDonate = true

                database
                    .child("foodItems").child(userFirebaseUID).child(key)
                    .child("toDonate").setValue(toDonate)
                mAdapter.updateItem(position,FoodItemModel(itemName, expiration, key, toDonate))
            } else {

                toDonate = false

                database
                    .child("foodItems").child(userFirebaseUID).child(key)
                    .child("toDonate").setValue(toDonate)
                mAdapter.updateItem(position,FoodItemModel(itemName, expiration, key, toDonate))
            }

        }
    }

}