package com.example.finalyearproject.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.finalyearproject.R
import com.example.finalyearproject.adapters.InfoWindowMapAdapter
import com.example.finalyearproject.models.FoodItemModel
import com.example.finalyearproject.util.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

/**
 * This class deals with the map functionality using the Google's map API
 */
//Live location works on android devices/ not the android studio emulator
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener{

    private var database: DatabaseReference =
        Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app").reference
    private var userFirebaseUID: String = FirebaseAuth.getInstance().currentUser!!.uid
    private var userFirebaseEmail: String = FirebaseAuth.getInstance().currentUser!!.email!!

    private lateinit var mMap: GoogleMap

    private lateinit var mView: View
    private lateinit var reserveButton: Button

    private var toDonate: MutableList<FoodItemModel> = ArrayList<FoodItemModel>()

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_map, container, false)
        reserveButton = mView.findViewById(R.id.reserve_button)

        setHomeLocation()
        showFreeFood()
        Thread.sleep(1500)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return mView
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        //Custom infoWindow
        getCurrentLocation()
        mMap.setInfoWindowAdapter(InfoWindowMapAdapter(requireContext()))
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("MAP_FRAGMENT_CHECK_LOCATION", "on permission granted")
                getCurrentLocation()

            } else {
                Toast.makeText(context, "Permission denied!", Toast.LENGTH_LONG).show();
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (Utils.checkLocationPermissions(requireContext())) {
            if (Utils.isLocationEnabled(requireContext())) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {

                        Log.d("MAP_FRAGMENT_MY_LOCATION", "long:" +location.latitude.toString() + " lat:" + location.latitude.toString())
                        val myPos = LatLng(location.latitude, location.longitude)
                        val mmarker = mMap.addMarker(
                            MarkerOptions()
                                .position(myPos)
                                .title("My Current Location")
                                .snippet("\nLook at other markers to \nfind free food around you")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location))
                        )
                        mmarker?.tag = userFirebaseUID
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                myPos,
                                15f
                            )
                        )
                    }
                }
            } else {
                Toast.makeText(activity, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

                startActivity(intent)
            }
        } else {
            Utils.requestLocationPermissions(requireActivity(),permissionId)
        }

    }

    //for each user that has his location set, show adress and free items
    @SuppressLint("SetTextI18n")
    private fun showFreeFood() {

        database.child("locations").get().addOnSuccessListener {

            for (el in it.children) {//for each user with a location
                //lat, long + get address using geocoder
                val pos = LatLng(
                    el.child("latitude").getValue(Double::class.java)!!,
                    el.child("longitude").getValue(Double::class.java)!!)

                val geocoder = Geocoder(activity, Locale.getDefault())
                val adresses: List<Address> = geocoder.getFromLocation(pos.latitude, pos.longitude, 1)
                var address = ""
                if(adresses[0].subThoroughfare != null && adresses[0].thoroughfare != null){
                    address = "at " + adresses[0].subThoroughfare + " " + adresses[0].thoroughfare
                }

                database.child("foodItems").child(el.key.toString()).get()  //for each user's food list
                    .addOnSuccessListener { foodList ->
                        for (item in foodList.children) {
                            if (item.child("toDonate").value == true) {
                                val expirationDate = item.child("itemExpirationDate").value.toString()
                                val name = item.child("itemName").value.toString()
                                val key = item.child("key").value.toString()
                                toDonate.add(FoodItemModel(name, expirationDate, key, true))
                            }
                        }
                        Log.d("MAP_FRAGMENT_USER_ID_ONCLICKMARKER", "toDonateSize:${toDonate.size} userfirebase: $userFirebaseUID   marker's user: ${ el.key.toString()}")
                        //if there is something to donate- add marker with button + all the data
                        // THE MARKER IS NOT SHOWN IF THE USER HAS NO ITEMS TO DONATE OR THE ITEMS TO DONATE ARE ALREADY EXPIRED
                        if(toDonate.size != 0 && (userFirebaseUID != el.key.toString())) {
                            var info: String = ""
                            for (item in toDonate) {
                                info = info + "\n" + item.itemName + " : " + item.itemExpirationDate
                            }

                            val mmarker = mMap.addMarker(
                                MarkerOptions()
                                    .position(pos)
                                    .title("Free items $address:")
                                    .snippet(info)

                            )
                            mmarker?.tag = el.key.toString()
                        }
                        toDonate.clear()
                    }
            }
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }

    /** Called when the user clicks a marker.  */
    override fun onMarkerClick(marker: Marker): Boolean {

        // Retrieve the data from the marker.
        val userId = marker.tag as? String
        Log.d("MAP_FRAGMENT_USER_ID_ONCLICKMARKER", userId!!)

        marker.showInfoWindow()
            if(userId == userFirebaseUID) {
                reserveButton.visibility = View.INVISIBLE
            } else {
                reserveButton.visibility = View.VISIBLE
                database.child("locations").child(userId!!).get()
                    .addOnSuccessListener { userMarker ->

                        //Decides what to show to the user: free to reserve or reserved bu somebody else
                        if (userMarker.child("reservedHour").exists()) {
                            val currentTime = Calendar.getInstance()
                            val hour = currentTime[Calendar.HOUR_OF_DAY]
                            val minute = currentTime[Calendar.MINUTE]
                            val resHour = userMarker.child("reservedHour").value.toString()
                            val resMinute = userMarker.child("reservedMinute").value.toString()
                            Log.d("MAP_FRAGMENT_RESERVE", "$resHour  $resMinute")

                            //if reservation is still valid
                            if ((resHour.toInt() * 60 + resMinute.toInt()) >= hour * 60 + minute) {

                                val reservedBy =
                                    userMarker.child("reservedBy").value.toString()   //if reserved by somebody else
                                if (reservedBy != userFirebaseEmail) {
                                    if (resMinute.toInt() <= 9)
                                        reserveButton.text = "Try to reserve after $resHour:0$resMinute"
                                    else
                                        reserveButton.text = "Try to reserve after $resHour:$resMinute"

                                } else {                                                       //if reserved by this account
                                    if (resMinute.toInt() <= 9)
                                        reserveButton.text = "Pick up items before $resHour:0$resMinute. Click to cancel"
                                    else
                                        reserveButton.text = "Pick up items before $resHour:$resMinute. Click to cancel"
                                    onClickReserveNow(userMarker.key.toString())
                                }
                            } else {                                                        //if free to reserve & time exists
                                reserveButton.text = "Reserve now"
                                onClickReserveNow(userMarker.key.toString())
                            }
                        } else {                                                            //if free to reserve & no time exists
                            reserveButton.text = "Reserve now"
                            onClickReserveNow(userMarker.key.toString())
                        }
                    }
            }

        return false
    }

    override fun onMapClick(p0: LatLng) {
        reserveButton.visibility =  View.INVISIBLE
    }

    private fun onClickReserveNow(userId : String) {

        reserveButton.setOnClickListener {
            if( reserveButton.text == "Reserve now") {
                Log.d("RESERVE_DEADLINE", "reserve now")

                val currentTime = Calendar.getInstance()
                val hour = currentTime[Calendar.HOUR_OF_DAY]+1
                val minute = currentTime[Calendar.MINUTE]

                onClickReserveNow(userId)
                database.child("locations").child(userId).child("reservedHour").setValue(hour)
                database.child("locations").child(userId).child("reservedMinute").setValue(minute)
                database.child("locations").child(userId).child("reservedBy").setValue(userFirebaseEmail)


                if(minute<=9)
                    reserveButton.text = "Pick up items before $hour:0$minute. Click to cancel"
                else {

                    reserveButton.text = "Pick up items before $hour:$minute. Click to cancel"
                }

            } else if(reserveButton.text.contains("Try", ignoreCase = true)){
                reserveButton.setOnClickListener {
                    val snackbar = Snackbar
                        .make(requireContext(),requireView(), "Somebody else book these items. Wait until later and try again.", Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
            } else {
                Log.d("RESERVE_DEADLINE", "cancel")

                reserveButton.text = "Reserve now"
                database.child("locations").child(userId).child("reservedHour").removeValue()
                database.child("locations").child(userId).child("reservedMinute").removeValue()
                database.child("locations").child(userId).child("reservedBy").removeValue()
                onClickReserveNow(userId)

            }
            Log.d("RESERVE_DEADLINE", "button clicked")
        }
    }


    @SuppressLint("MissingPermission")
    private fun setHomeLocation() {

        val setHomeBtn: ImageButton = mView.findViewById(R.id.setHomeLocation_ImageButton)
        setHomeBtn.setOnClickListener {

            mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                val location: Location? = task.result
                if (location != null) {
                    val geocoder = Geocoder(activity, Locale.getDefault())
                    val list: List<Address> =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    database
                        .child("locations").child(userFirebaseUID).child("latitude")
                        .setValue(location.latitude)

                    database
                        .child("locations").child(userFirebaseUID).child("longitude")
                        .setValue(location.longitude)

                    Toast.makeText(
                        activity,
                        "Home location set to current location:\n${list[0].getAddressLine(0)}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

