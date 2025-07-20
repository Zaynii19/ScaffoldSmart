package com.example.scaffoldsmart.client.client_viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Create the ViewModel using the factory to pass any variable to view model
class CartViewModelFactory(private val clientId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(clientId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}