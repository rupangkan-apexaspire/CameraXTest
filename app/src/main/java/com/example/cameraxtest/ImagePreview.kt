package com.example.cameraxtest

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cameraxtest.adapter.ImagePreviewAdapter
import com.example.cameraxtest.databinding.ImageViewBinding

class ImagePreview : AppCompatActivity() {
    lateinit var viewBinding: ImageViewBinding
//    private lateinit var fileList: MutableList<String>
    private val TAG = "ImagePreview"
//    private lateinit var file: ArrayList<String>
    private lateinit var myApplication: MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ImageViewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        myApplication = application as MainApplication

        var bundle: Bundle? = intent.extras

//        file = bundle?.getStringArrayList("files") as ArrayList<String>
//        fileList = file.toMutableList()
//        Log.d(TAG, "onCreate: $file")

        val adapter = ImagePreviewAdapter(myApplication.fileList)
        val manager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        viewBinding.imageViewRcView.layoutManager = manager
        viewBinding.imageViewRcView.adapter = adapter
        viewBinding.imageViewRcView.setHasFixedSize(true)

        viewBinding.delete.setOnClickListener{
            val visiblePosition: Int = manager.findFirstCompletelyVisibleItemPosition()
            Log.d(TAG, "onCreate: $visiblePosition")
            Log.d(TAG, "onCreate: ${myApplication.fileList}")
            if(visiblePosition > -1) {
//                val position = adapter.onDelete(visiblePosition)
                myApplication.fileList.removeAt(visiblePosition)
//                contentResolver.delete(Uri.parse(myApplication.fileList[visiblePosition]), null, null)
                adapter.notifyDataSetChanged()
                Log.d(TAG, "deleteFiles: ${myApplication.fileList}")
            }
        }
    }
}