package com.example.finalyearproject.ui.settings



import android.content.Context.WINDOW_SERVICE
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat.getSystemService
import com.example.finalyearproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter


class SettingsFragment : Fragment() {

    private var userFirebaseUID: String = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_settings, container, false)

        val qrImageView = mView.findViewById<ImageView>(R.id.Qrcode_imageView)

        try {
            val bitmap = getQrCodeBitmap(userFirebaseUID)

            qrImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {

            e.printStackTrace()
        }




        return mView
    }

    fun getQrCodeBitmap(userId: String): Bitmap {
        val size = 256 //pixels
        val qrCodeContent = "userFirebaseUID:$userId"
        //val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
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