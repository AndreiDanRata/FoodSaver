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
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer (
    private val context: Context,
    ) : ImageAnalysis.Analyzer {

    val player = MediaPlayer.create(context, R.raw.sound_barcode_scanner)


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy,) {
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
                                saveBarcodeToList(barcode.rawValue as String)
                            }

                        }
                    }
                .addOnFailureListener { }
        }
        image.close()
    }

    /**
     * Saves Barcodes to an object called BarcodesList and ensures there are no duplicates.
     */
    private fun saveBarcodeToList(barcode : String) {
        if(BarcodesList.barcodes.isEmpty()) {

            BarcodesList.barcodes.add(barcode)
            scanCues(barcode)
            Utils.fetchAPIData(barcode, this.context).start()

        } else {

            if(barcode !=  BarcodesList.barcodes.lastOrNull()) {
                BarcodesList.barcodes.add(barcode)
                scanCues(barcode)
                Utils.fetchAPIData(barcode, this.context).start()


                //Log.d("BARCODES_LIST", BarcodesList.barcodes.toString())
            }
        }
    }

    private fun scanCues(barcode: String) {
        player!!.start()
        Toast.makeText(
            context,
            "Value: " + barcode,
            Toast.LENGTH_SHORT
        ).show()
    }

}