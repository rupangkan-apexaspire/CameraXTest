package com.example.cameraxtest.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cameraxtest.R
import com.example.cameraxtest.adapter.ImagePreviewAdapter
import com.example.cameraxtest.databinding.FragmentFormBinding
import com.example.cameraxtest.models.AppViewModel

class FormFragment : Fragment() {

    private val TAG = "ReposeFormFragment"
    private val sharedViewModel: AppViewModel by activityViewModels()
    private var viewBinding: FragmentFormBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentFormBinding = FragmentFormBinding.inflate(inflater, container, false)
        viewBinding = fragmentFormBinding
        return fragmentFormBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding?.formFragment = this

        // Implementation of selection needed for the forms
        Log.d(TAG, "onViewCreated: ${sharedViewModel.fileList}")
        if(sharedViewModel.fileList.isNotEmpty()) {
            val adapter = ImagePreviewAdapter(sharedViewModel.fileList)
            val manager = LinearLayoutManager(requireContext().applicationContext, LinearLayoutManager.HORIZONTAL, false)
            viewBinding?.formRecyclerView?.layoutManager = manager
            viewBinding?.formRecyclerView?.adapter = adapter
            viewBinding?.formRecyclerView?.setHasFixedSize(true)
        }

        if(sharedViewModel.title != "") {
            viewBinding?.titleEditText?.setText(sharedViewModel.title)
        }

        if(sharedViewModel.description != "") {
            viewBinding?.descriptionEditText?.setText(sharedViewModel.description)
        }

        if(sharedViewModel.genderCheckedId != null) {
            viewBinding?.gender?.check(sharedViewModel.genderCheckedId!!)
        }

        if(sharedViewModel.standardCheckedId != null) {
            viewBinding?.standard?.check(sharedViewModel.standardCheckedId!!)
        }


//        if(sharedViewModel.interests != "") {
//            viewBinding?.interests?.setText(sharedViewModel.interests)
//        }

        viewBinding?.takePhoto?.setOnClickListener{
            sharedViewModel.title = viewBinding?.titleEditText?.text.toString()
            sharedViewModel.description = viewBinding?.descriptionEditText?.text.toString()
            sharedViewModel.genderCheckedId = viewBinding?.gender?.checkedRadioButtonId
            sharedViewModel.standardCheckedId = viewBinding?.standard?.checkedRadioButtonId
            sharedViewModel.interests = viewBinding?.interests?.text.toString()
            val action = FormFragmentDirections.actionFormFragmentToImageFragment()
            this.findNavController().navigate(action)
        }

    }


    override fun onResume() {
        super.onResume()
        if(sharedViewModel.fileList.isNotEmpty()) {
            val adapter = ImagePreviewAdapter(sharedViewModel.fileList)
            val manager = LinearLayoutManager(requireContext().applicationContext, LinearLayoutManager.HORIZONTAL, false)
            viewBinding?.formRecyclerView?.layoutManager = manager
            viewBinding?.formRecyclerView?.adapter = adapter
            viewBinding?.formRecyclerView?.setHasFixedSize(true)
        }

        val interests = resources.getStringArray(R.array.interests)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_layout, interests)
        viewBinding?.interests?.setAdapter(arrayAdapter)

        if(sharedViewModel.interests != "") {
            viewBinding?.interests?.setText(sharedViewModel.interests)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }


}