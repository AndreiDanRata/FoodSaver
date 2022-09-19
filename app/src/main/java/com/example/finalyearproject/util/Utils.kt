package com.example.finalyearproject.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.finalyearproject.models.BarcodeApiRequest
import com.example.finalyearproject.models.FoodItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

object Utils {

    private var database: DatabaseReference = Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app").reference
    private var userFirebaseUID: String = FirebaseAuth.getInstance().currentUser!!.uid

    fun fetchAPIData(barcode: String, context: Context): Thread {
        return Thread {
            val itemExpirationDate = "11/11/2000"

            try {
                val url =
                    URL(Constants.BARCODE_BASE_URL + barcode + "?apikey=" + Constants.BARCODE_API_KEY)
                //Log.d("FOODLIST_URL", url.toString())
                val connection = url.openConnection() as HttpsURLConnection
                //Log.d("FOODLIST_CONNECTION_RESPONSE_CODE", connection.responseCode.toString())
                if (connection.responseCode == 200) {
                    val inputSystem = connection.inputStream
                    val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                    val barcodeApiRequest = Gson().fromJson(inputStreamReader, BarcodeApiRequest::class.java)
                    if (barcodeApiRequest.success) {
                        if(barcodeApiRequest.title != "") {
                            addFoodItem(barcodeApiRequest.title, itemExpirationDate)
                        } else if(barcodeApiRequest.description != "") {
                            addFoodItem(barcodeApiRequest.description, itemExpirationDate)
                        }
                    } else {
                        Toast.makeText(context, "Some items' barcodes are not yet", Toast.LENGTH_LONG).show()
                        //Log.d("API_BARCODE_NOT_FOUND", "The barcode was not found in the api database")
                    }
                    inputStreamReader.close()
                    inputSystem.close()
                    //Log.d("FOODLIST_API_RESPONSE", barcodeApiRequest.description + barcodeApiRequest.title)
                } else {
                    Log.d("FOODLIST_NO_CODE_API", "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addFoodItem(itemName: String, itemExpirationDate: String) {
        //create a random string id
        val random_key = getRandomString(10)

        if (itemName != "" && itemExpirationDate != "") {
            //push value to firebase
            val item = FoodItemModel(itemName, itemExpirationDate, random_key,false)

            database
                .child("foodItems").child(userFirebaseUID).child(random_key).setValue(item)
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





    //Enabling location
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * Function used to request Camera Permission.
     * If permission is granted, it will initialize the camera preview.
     * If not,it will raise an alert.
     */
    fun checkLocationPermissions(context: Context): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    /**
     * Function used after the user grants/denies permission.
     */
    fun requestLocationPermissions(activity: Activity, permissionId: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

}