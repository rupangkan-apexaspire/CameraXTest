package com.example.cameraxtest.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cameraxtest.R
import com.example.cameraxtest.adapter.ImagePreviewAdapter
import com.example.cameraxtest.databinding.FragmentImagePreviewBinding
import com.example.cameraxtest.models.AppViewModel


class ImagePreviewFragment : Fragment() {

    private var viewBinding: FragmentImagePreviewBinding? = null
    private val sharedViewModel: AppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentImagePreviewBinding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        viewBinding = fragmentImagePreviewBinding
        return fragmentImagePreviewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding?.imagePreviewFragment = this
        val adapter = ImagePreviewAdapter(sharedViewModel.fileList)
        val manager = LinearLayoutManager(requireContext().applicationContext, LinearLayoutManager.HORIZONTAL, false)
        viewBinding?.imageViewRcView?.layoutManager = manager
        viewBinding?.imageViewRcView?.adapter = adapter
        viewBinding?.imageViewRcView?.setHasFixedSize(true)

        checkFileList()

        if(viewBinding?.done?.visibility == View.VISIBLE) {
            viewBinding?.done?.setOnClickListener{
                val action = ImagePreviewFragmentDirections.actionImagePreviewFragmentToFormFragment()
                this.findNavController().navigate(action)
            }
        }

        viewBinding?.delete?.setOnClickListener{
            val visiblePosition: Int = manager.findFirstCompletelyVisibleItemPosition()
//            Log.d(TAG, "onCreate: $visiblePosition")
//            Log.d(TAG, "onCreate: ${myApplication.fileList}")
            if(visiblePosition > -1) {
//                val position = adapter.onDelete(visiblePosition)
                sharedViewModel.fileList.removeAt(visiblePosition)
//                contentResolver.delete(Uri.parse(myApplication.fileList[visiblePosition]), null, null)
                adapter.notifyDataSetChanged()
                checkFileList()
//                Log.d(TAG, "deleteFiles: ${myApplication.fileList}")
            }
        }

    }

    private fun checkFileList() {
        if(sharedViewModel.fileList.isNotEmpty()){
            viewBinding?.done?.visibility = View.VISIBLE
        } else if(sharedViewModel.fileList.isNullOrEmpty()) {
            viewBinding?.done?.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }


}