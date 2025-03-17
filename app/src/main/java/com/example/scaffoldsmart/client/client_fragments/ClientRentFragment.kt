package com.example.scaffoldsmart.client.client_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.FragmentClientRentBinding

class ClientRentFragment : Fragment() {
    private val binding by lazy {
        FragmentClientRentBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }
}