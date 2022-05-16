package com.example.cameraxtest.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cameraxtest.R

class ImagePreviewAdapter(private val fileList: MutableList<String>): RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>()  {

    private val TAG = "ReposeIPA"
    class ViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_view_list, parent, false)

        return ImagePreviewAdapter.ViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = fileList[position]
        Glide.with(holder.imageView.context).load(item).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun onDelete(position: Int): Int {
        fileList.removeAt(position)
        notifyDataSetChanged()
        Log.d(TAG, "onDelete: $fileList")
        return position
    }
}