package com.example.scaffoldsmart.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object CheckNetConnectvity {
        fun hasInternetConnection(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // For Android 10 (API 29) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                return capabilities?.let {
                    it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                } ?: false
            }
            // For older versions
            else {
                @Suppress("DEPRECATION")
                val activeNetwork = connectivityManager.activeNetworkInfo
                return activeNetwork?.isConnectedOrConnecting == true && (
                        activeNetwork.type == ConnectivityManager.TYPE_WIFI ||
                                activeNetwork.type == ConnectivityManager.TYPE_MOBILE ||
                                activeNetwork.type == ConnectivityManager.TYPE_ETHERNET)
            }
        }
}