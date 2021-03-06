package com.example.cameraxtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.video.Recorder
import com.example.cameraxtest.databinding.ActivityMain2Binding
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.PermissionChecker
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import android.provider.MediaStore

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideExtension
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.Serializable
import kotlin.collections.ArrayList


class MainActivity2 : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMain2Binding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var fileList: ArrayList<String>

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        fileList = ArrayList()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { 
            takePhoto()
            Log.d(TAG, "onCreate: takephoto cliked")
        }
//        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        Log.d(TAG, "takePhoto: HEHE $imageCapture")

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val parcelFileDescriptor = contentResolver.openFileDescriptor(output.savedUri!!, "r", null) ?: return
                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                    val file = File(cacheDir, contentResolver.getFileName(output.savedUri!!))
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    setFiles(file)
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun setFiles(file: File) {
        fileList.add(file.absolutePath)
        Log.d(TAG, "Files $fileList")

        if(fileList.isNotEmpty()) {
            viewBinding.done.visibility = View.VISIBLE
            Glide.with(this).load(file).into(viewBinding.done)
        }
        if(viewBinding.done.visibility == View.VISIBLE) {
            viewBinding.done.setOnClickListener{
                var intent = Intent(this, ImagePreview::class.java)
                intent.putExtra("files", fileList)
                startActivity(intent)

            }
        }
    }

    @SuppressLint("Range")
    fun ContentResolver.getFileName(uri: Uri): String {
        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null)

        var name = ""

        cursor?.use {
            if (it.moveToFirst()) {
                val displayName: String =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.i(TAG, "Display Name: $displayName")
                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                val size: String = if (!it.isNull(sizeIndex)) {
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.i(TAG, "Size: $size")
                name = cursor.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }

        return name
    }

    private fun startCamera() {
//        Toast.makeText(applicationContext, "Hehehe", Toast.LENGTH_SHORT).show()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXTest"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}
