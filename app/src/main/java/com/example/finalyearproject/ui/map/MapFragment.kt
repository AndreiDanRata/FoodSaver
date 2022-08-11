package com.example.finalyearproject.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.finalyearproject.R
import com.example.finalyearproject.adapters.InfoWindowMapAdapter
import com.example.finalyearproject.models.FoodItemModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


//Live location works on android devices/ not the android studio emulator
class MapFragment : Fragment(), OnMapReadyCallback {

    private var database: DatabaseReference =
        Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app").reference
    private var userFirebaseUID: String = FirebaseAuth.getInstance().currentUser!!.uid

    private lateinit var mMap: GoogleMap

    private lateinit var mView: View

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
        setHomeLocation()
        showFreeFood()

        // Obtain the SupportMapFragment. childFragmentManager cuz we are in a fragment not an activity
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        return mView


    }

    override fun onMapReady(googleMap: GoogleMap) {


        mMap = googleMap

        //Custom infoWindow
        mMap.setInfoWindowAdapter(InfoWindowMapAdapter(requireContext()))


        // Add a marker in Sydney and move the camera
        getCurrentLocation()

    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * Function used to request Camera Permission.
     * If permission is granted, it will initialize the camera preview.
     * If not,it will raise an alert.
     */
    private fun checkLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
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
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (checkLocationPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(activity, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        Log.d("MY_LOCATION", location.latitude.toString())   //THIS WORKS
                        /*myCords.long = list[0].longitude
                        myCords.lat = list[0].latitude*/
                        val myPos = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(
                            MarkerOptions().position(myPos).title("My Location")
                        )
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                myPos,
                                15f
                            )
                        )
                        /*  mainBinding.apply {
                              tvLatitude.text = "Latitude\n${list[0].latitude}"
                              tvLongitude.text = "Longitude\n${list[0].longitude}"
                              tvCountryName.text = "Country Name\n${list[0].countryName}"
                              tvLocality.text = "Locality\n${list[0].locality}"
                              tvAddress.text = "Address\n${list[0].getAddressLine(0)}"
                          }*/
                    }
                }
            } else {
                Toast.makeText(activity, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestLocationPermissions()
        }

    }

    //for each user that has his location set, show adress and free items
    private fun showFreeFood() {

        database.child("locations").get().addOnSuccessListener {

            for (el in it.children) {//for each user with a location
                //lat, long
                val pos = LatLng(
                    el.child("latitude").getValue(Double::class.java)!!,
                    el.child("longitude").getValue(Double::class.java)!!
                )

                database.child("foodItems").child(el.key.toString()).get()  //for each user's food list
                    .addOnSuccessListener { foodList ->
                        for (item in foodList.children) {
                            if (item.child("toDonate").value == true) {
                                val expirationDate =
                                    item.child("itemExpirationDate").value.toString()
                                val name = item.child("itemName").value.toString()
                                val key = item.child("key").value.toString()
                                val donation = item.child("toDonate").value.toString().toBoolean()
                                toDonate.add(FoodItemModel(name, expirationDate, key, donation))
                            }
                        }

                        Log.d("LOCATION_DATA", toDonate.toString())
                        var info: String = ""
                        for (item in toDonate) {
                            info = info + "\n" + item.itemName + " : " + item.itemExpirationDate
                        }

                        mMap.addMarker(
                            MarkerOptions().position(pos)
                                .title("Free items:")  ////todo display adress instead of title???
                                .snippet(info)
                        )
                        toDonate.clear()
                    }


            }

        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
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
                    val list: List<Address> = //TODO DISPLAY ADRESS IN SETTINGS AS HOME LOCATION
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

