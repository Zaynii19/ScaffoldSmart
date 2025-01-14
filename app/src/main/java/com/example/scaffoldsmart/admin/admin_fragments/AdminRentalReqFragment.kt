package com.example.scaffoldsmart.admin.admin_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.admin.admin_adapters.RequestRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.RentalModel
import com.example.scaffoldsmart.admin.admin_viewmodel.RentalViewModel
import com.example.scaffoldsmart.databinding.FragmentAdminRentalReqBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AdminRentalReqFragment : BottomSheetDialogFragment(), RequestRcvAdapter.OnItemActionListener {
    private val binding by lazy {
        FragmentAdminRentalReqBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: RequestRcvAdapter
    private lateinit var reqList: ArrayList<RentalModel>
    private lateinit var reqViewModel: RentalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reqViewModel = ViewModelProvider(requireActivity())[RentalViewModel::class.java]
        reqViewModel.retrieveRentalReq()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setRcv() {
        // Retrieve the list of rental requests from arguments safely
        arguments?.let {
            reqList = it.getSerializable(ARG_LIST) as ArrayList<RentalModel>
        }
        // Set up the RecyclerView only if reqList is not null
        binding.rcv.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapter = RequestRcvAdapter(requireActivity(), reqList, reqViewModel, this)  // Pass the whole list to the adapter
        binding.rcv.adapter = adapter
        binding.rcv.setHasFixedSize(true)
        binding.rcv.isNestedScrollingEnabled = false

    }

    companion object {
        private const val ARG_LIST = "arg_list"

        // Modify the function to accept a list of RentalReqModel
        fun newInstance(reqList: List<RentalModel>): AdminRentalReqFragment {
            val fragment = AdminRentalReqFragment()
            val args = Bundle()

            // Pass the list as Serializable
            args.putSerializable(ARG_LIST, ArrayList(reqList))  // Make sure to convert it to ArrayList
            fragment.arguments = args

            return fragment
        }
    }

    override fun onDialogDismissed() {
        dismiss()
    }
}