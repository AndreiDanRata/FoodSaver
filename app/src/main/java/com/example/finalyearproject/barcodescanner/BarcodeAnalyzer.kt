package com.example.finalyearproject.barcodescanner

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.finalyearproject.R
import com.example.finalyearproject.util.Utils
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer (
    private val context: Context,
    ) : ImageAnalysis.Analyzer {

    val player = MediaPlayer.create(context, R.raw.sound_barcode_scanner)
    val delimiter = "/+/"
    var listQRitems = listOf<String>()


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val img = image.image

        if (img != null) {

            val inputImage = InputImage.fromMediaImage(img, image.imageInfo.rotationDegrees)

            val options = BarcodeScannerOptions.Builder()
                .build()

            val scanner = BarcodeScanning.getClient(options)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {

                        for (barcode in barcodes) {
                                addCodeToDatabase(barcode.rawValue as String)
                            }

                        }
                    }
                .addOnFailureListener { }
        }
        image.close()
    }

    /**
     * Saves Barcodes to an object called BarcodesList and ensures there are no duplicates.
     * When this is true, it then proceeds to either query the barcode list api or to add the item if its a qr code with the right format
     */
    private fun addCodeToDatabase(barcode : String) {
        if(barcode !=  BarcodesList.barcodes.lastOrNull()) {
            BarcodesList.barcodes.add(barcode)
            scanCues(barcode)

            //CASE WHEN BARCODE IS A QR CODE CONTAINING NAME/EXPIRATION DATE
                if( barcode.length > 16 && barcode.substring(0,16) == "Name/Expiration:")  {     //Name/Expiration:  has 16 characters
                    //Log.d("BARCODES_LIST", "qr code branch")
                    listQRitems = barcode.split(delimiter)
                    Utils.addFoodItem(listQRitems[1],listQRitems[2])
                }
                else {       //CASE WHEN BARCODE IS A SIMPLE CODE
                    val onlyDigits: Boolean = barcode.matches("[0-9]+".toRegex())
                    if(onlyDigits) {
                        Utils.fetchAPIData(barcode, this.context).start()
                    }
            }
            //Log.d("BARCODES_LIST", BarcodesList.barcodes.toString())
        }
    }

    private fun scanCues(barcode: String) {
        player!!.start()
        Toast.makeText(
            context,
            "Value: $barcode",
            Toast.LENGTH_SHORT
        ).show()
    }

}