package com.example.finalyearproject.barcodescanner

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer (
    private val context: Context,
    ) : ImageAnalysis.Analyzer {


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

                            saveBarcodeToList(barcode.rawValue as String)

                            Toast.makeText(
                                context,
                                "Value: " + barcode.rawValue,
                                Toast.LENGTH_SHORT
                            )
                                .show()

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
        } else {
            if(barcode !=  BarcodesList.barcodes.lastOrNull()) {
                BarcodesList.barcodes.add(barcode)
                Log.d("BARCODES_LIST", BarcodesList.barcodes.toString())
            }
        }



    }

}