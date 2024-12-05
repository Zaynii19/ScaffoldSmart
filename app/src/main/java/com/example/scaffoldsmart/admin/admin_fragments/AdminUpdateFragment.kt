package com.example.scaffoldsmart.admin.admin_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.databinding.FragmentAdminUpdateBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AdminUpdateFragment : BottomSheetDialogFragment() {

    private val binding by lazy {
        FragmentAdminUpdateBinding.inflate(layoutInflater)
    }
    private var onAdminUpdatedListener: OnAdminUpdatedListener? = null

    interface OnAdminUpdatedListener {
        fun onAdminUpdated()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveBtn.setOnClickListener {

            val name = binding.adminName.text
            if (name!!.isNotEmpty()) {
                //onAdminUpdatedListener?.onAdminUpdated(gender)
                dismiss() // Dismiss only after saving data
            } else {
                Toast.makeText(context, "Please add Name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(listener: OnAdminUpdatedListener): AdminUpdateFragment {
            val fragment = AdminUpdateFragment()
            fragment.onAdminUpdatedListener = listener
            return fragment
        }
    }

}