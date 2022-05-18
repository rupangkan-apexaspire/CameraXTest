package com.example.cameraxtest.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.cameraxtest.ImagePreview
import com.example.cameraxtest.MainActivity2
import com.example.cameraxtest.R
import com.example.cameraxtest.databinding.FragmentImageBinding
import com.example.cameraxtest.models.AppViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageFragment : Fragment() {

    private var viewBinding: FragmentImageBinding? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val sharedViewModel: AppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentImageBinding = FragmentImageBinding.inflate(inflater, container, false)
        viewBinding = fragmentImageBinding
        return fragmentImageBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding?.imageFragment = this

        if(sharedViewModel.fileList.isNullOrEmpty()) {
            viewBinding?.done?.visibility = View.GONE
        } else if(sharedViewModel.fileList.isNotEmpty()) {
            viewBinding?.done?.visibility = View.VISIBLE
            Glide.with(requireActivity().applicationContext).load(sharedViewModel.fileList[0]).into(viewBinding!!.done)
        }

        traverse()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), ImageFragment.REQUIRED_PERMISSIONS, ImageFragment.REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding?.imageCaptureButton?.setOnClickListener {
            takePhoto()
            Log.d(TAG, "onCreate: takephoto cliked")
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        Log.d(TAG, "takePhoto: HEHE $imageCapture")

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(ImageFragment.FILENAME_FORMAT, Locale.US)
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
            .Builder(requireActivity().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireActivity().applicationContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val parcelFileDescriptor = requireActivity().contentResolver.openFileDescriptor(output.savedUri!!, "r", null) ?: return
                    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                    val file = File(requireActivity().cacheDir, requireActivity().contentResolver.getFileName(output.savedUri!!))
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    setFiles(file)
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(requireActivity().baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun setFiles(file: File) {
//        fileList.add(file.absolutePath)
//        Log.d(TAG, "Files $fileList")
        sharedViewModel.fileList.add(file.absolutePath)

        if(sharedViewModel.fileList.isNullOrEmpty()){
            viewBinding?.done?.visibility = View.GONE
        } else if(sharedViewModel.fileList.isNotEmpty()){
            viewBinding?.done?.visibility = View.VISIBLE
            Glide.with(requireActivity().applicationContext).load(file).into(viewBinding!!.done)
        }

        traverse()

    }

    private fun traverse() {
        if(viewBinding?.done?.visibility == View.VISIBLE){
            viewBinding?.done?.setOnClickListener{
                val action = ImageFragmentDirections.actionImageFragmentToImagePreviewFragment()
                this.findNavController().navigate(action)
            }
        }
    }

    @SuppressLint("Range")
    fun ContentResolver.getFileName(uri: Uri): String {
        val cursor: Cursor? = requireActivity().contentResolver.query(
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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity().applicationContext)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding?.viewFinder?.surfaceProvider)
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

        }, ContextCompat.getMainExecutor(requireActivity().applicationContext))
    }

    private fun allPermissionsGranted() = ImageFragment.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ImageFragment.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                activity?.finish()
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