package com.example.finalyearproject.barcodescanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finalyearproject.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.IllegalArgumentException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.finalyearproject.databinding.ActivityBarcodeScannerBinding

/**
 * Class responsible for scanning QR Codes and Barcodes.
 */

class BarcodeScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarcodeScannerBinding
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_48);
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        binding = ActivityBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        checkCameraPermission()

    }



    override fun onDestroy() {
        super.onDestroy()

        cameraExecutor.shutdown()
    }

    /**
     * Function used to request Camera Permission.
     * If permission is granted, it will initialize the camera preview.
     * If not,it will raise an alert.
     */
    private fun checkCameraPermission() {
        try {
            val cameraPermission = arrayOf(Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this, cameraPermission, 0)
        } catch (e: IllegalArgumentException) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                startCamera()
            } else {
                // Permission denied
                MaterialAlertDialogBuilder(this)
                    .setTitle("Permission is required")
                    .setMessage("Application requires access to camera(for processing barcodes)")
                    .setPositiveButton("Ok") { _, _ ->
                        // Keep asking for permission until granted
                        checkCameraPermission()
                    }
                    .setCancelable(false)
                    .create()
                    .apply {
                        setCanceledOnTouchOutside(false)
                        show()
                    }
            }
        }
    }
    
    /**
     * Function used after the user grants/denies permission.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            // Permission denied
            MaterialAlertDialogBuilder(this)
                .setTitle("Permission is required")
                .setMessage("Application requires access to camera(for processing barcodes)")
                .setPositiveButton("Ok") { _, _ ->
                    // Keep asking for permission until granted
                    checkCameraPermission()
                }
                .setCancelable(false)
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                    show()
                }
        }
    }

    /**
     * This function is responsible for the setup of the camera preview and the image analyzer.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        BarcodeAnalyzer(this)
                    )
                }

            val cameraPreview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview, imageAnalyzer)

            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }
}