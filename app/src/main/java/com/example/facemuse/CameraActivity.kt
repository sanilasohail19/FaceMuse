package com.example.facemuse

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        val captureBtn = findViewById<ImageButton>(R.id.btnCapture)
        val closeBtn = findViewById<ImageButton>(R.id.btnClose)

        cameraExecutor = Executors.newSingleThreadExecutor()

        captureBtn.setOnClickListener { checkFaceAndCapture() }
        closeBtn.setOnClickListener { finish() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                101
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1080, 1920))
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageCapture
            )

        }, ContextCompat.getMainExecutor(this))
    }

    // ---------------- BUTTON CLICK LOGIC ----------------
    private fun checkFaceAndCapture() {
        val bitmap = previewView.bitmap
        if (bitmap == null) {
            Toast.makeText(this, "Preview not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val image = InputImage.fromBitmap(bitmap, 0)

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setMinFaceSize(0.25f)
                .build()
        )

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (validateFace(faces, bitmap)) {
                    // Full face validated → capture high-res image
                    takePhoto()
                } else {
                    Toast.makeText(
                        this,
                        "Show full clear face to capture",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Face detection failed", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- FACE VALIDATION ----------------
    private fun validateFace(faces: List<Face>, bitmap: Bitmap): Boolean {
        if (faces.size != 1) return false

        val face = faces[0]
        val box = face.boundingBox

        val viewW = bitmap.width
        val viewH = bitmap.height

        val faceArea = box.width() * box.height()
        val screenArea = viewW * viewH

        // ❌ Face too small
        if (faceArea < screenArea * 0.25) return false

        // ❌ Face not centered
        if (abs(box.centerX() - viewW / 2) > viewW * 0.25 ||
            abs(box.centerY() - viewH / 2) > viewH * 0.25
        ) return false

        // ❌ Minimum width/height (avoid blurry/partial)
        if (box.width() < 220 || box.height() < 220) return false

        return true
    }

    // ---------------- CAPTURE HIGH RES IMAGE ----------------
    private fun takePhoto() {
        val capture = imageCapture ?: return

        val name = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss",
            Locale.US
        ).format(System.currentTimeMillis())

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FaceMuse")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val intent = Intent(this@CameraActivity, ResultActivity::class.java)
                    intent.data = output.savedUri
                    startActivity(intent)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}