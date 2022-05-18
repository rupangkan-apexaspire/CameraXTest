package com.example.cameraxtest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.cameraxtest.R
import com.example.cameraxtest.databinding.FragmentStartBinding


class StartFragment : Fragment() {

    private var binding: FragmentStartBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentStartBinding = FragmentStartBinding.inflate(inflater, container, false)
        binding = fragmentStartBinding
        return fragmentStartBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.startFragment = this

        binding!!.Button.setOnClickListener {
            val action = StartFragmentDirections.actionStartFragmentToFormFragment()
            this.findNavController().navigate(action)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


}