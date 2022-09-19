package com.example.finalyearproject.ui.settings



import android.content.Context.WINDOW_SERVICE
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat.getSystemService
import com.example.finalyearproject.R
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*

/**
 * Class responsible for the "Other" framgment
 * This includes QR code generator + showing the home address
 */
class SettingsFragment : Fragment() {

    private var database: DatabaseReference =
        Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app").reference
    private var userFirebaseUID: String = FirebaseAuth.getInstance().currentUser!!.uid

    private lateinit var mView: View

    var address = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_settings, container, false)

        val qrImageView = mView.findViewById<ImageView>(R.id.Qrcode_imageView)
        val homeText: TextView = mView.findViewById(R.id.settings2_textView)

        database.child("locations").child(userFirebaseUID).get().addOnSuccessListener { user ->
            val pos = LatLng(
                user.child("latitude").getValue(Double::class.java)!!,
                user.child("longitude").getValue(Double::class.java)!!)

            val geocoder = Geocoder(activity, Locale.getDefault())
            val adresses: List<Address> = geocoder.getFromLocation(pos.latitude, pos.longitude, 1)
            if(adresses[0].subThoroughfare != null && adresses[0].thoroughfare != null && adresses[0].locality != null){
                address = adresses[0].subThoroughfare + " " + adresses[0].thoroughfare + "," + adresses[0].locality
            } else if(adresses[0].subThoroughfare != null && adresses[0].thoroughfare != null){
                address = adresses[0].subThoroughfare + " " + adresses[0].thoroughfare
            } else {
                address = "Your address is not valid"
            }
            homeText.text = address
        }


        try {
            val bitmap = getQrCodeBitmap(userFirebaseUID)

            qrImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {

            e.printStackTrace()
        }




        return mView
    }

    private fun getQrCodeBitmap(userId: String): Bitmap {
        val size = 256 //pixels
        val qrCodeContent = "userFirebaseUID:$userId"
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }

}