package com.example.scaffoldsmart.client.client_fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scaffoldsmart.R
import com.example.scaffoldsmart.admin.admin_adapters.ChatRcvAdapter
import com.example.scaffoldsmart.admin.admin_models.ChatModel
import com.example.scaffoldsmart.client.client_adapters.ClientChatRcvAdapter
import com.example.scaffoldsmart.client.client_models.ClientChatModel
import com.example.scaffoldsmart.databinding.FragmentClientChatBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClientChatFragment : Fragment() {
    private val binding by lazy {
        FragmentClientChatBinding.inflate(layoutInflater)
    }
    private var chatList = ArrayList<ClientChatModel>()
    private lateinit var adapter: ClientChatRcvAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeChatList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setRcv()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSearchView()
    }

    private fun setRcv() {
        binding.chatRCV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ClientChatRcvAdapter(requireActivity(), chatList)
        binding.chatRCV.adapter = adapter
        binding.chatRCV.setHasFixedSize(true)
    }

    private fun initializeChatList() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Get current time and date as strings
        val currentTimeString = timeFormat.format(Date())
        val currentDateString = dateFormat.format(Date())

        chatList.add(ClientChatModel("Danish", "Get the best deals on scaffolding rental equipment.", currentTimeString, 1))
        chatList.add(ClientChatModel("Rabia", "We provide scaffolding rentals for commercial and residential projects.", currentDateString, 2))
        chatList.add(ClientChatModel("Haider", "Fast and efficient scaffolding rental service.", currentTimeString, 7))
        chatList.add(ClientChatModel("Hasan", "Scaffolding rental available for your next project.", currentTimeString, 3))
        chatList.add(ClientChatModel("Fatima", "Secure your job site with our premium scaffolding rental.", currentDateString, 4))
        chatList.add(ClientChatModel("Ali", "Affordable scaffolding solutions for all construction needs.", currentTimeString, 5))
        chatList.add(ClientChatModel("Laiba", "Reliable scaffolding rental services near you.", currentDateString, 13))
    }

    private fun setSearchView() {
        // Change text color to white of search view
        binding.search.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.apply {
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
        }

        // Get app color from colors.xml
        val appColor = ContextCompat.getColor(requireContext(), R.color.item_color)

        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)?.setColorFilter(appColor)
        binding.search.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)?.setColorFilter(appColor)

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "") // Call filter method on text change
                return true
            }
        })
    }

}