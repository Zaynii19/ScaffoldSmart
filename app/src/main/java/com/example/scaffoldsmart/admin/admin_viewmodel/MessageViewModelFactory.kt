package com.example.scaffoldsmart.admin.admin_viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Create the ViewModel using the factory to pass any variable to view model
class MessageViewModelFactory(private val senderRoom: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessageViewModel(senderRoom) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}